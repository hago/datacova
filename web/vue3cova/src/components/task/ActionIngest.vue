<script lang="ts">
import connApiHelper from '@/api/connectionapi';
import { dbConfigStringify } from '@/entities/connection/workspaceconnection';
import { EVENT_REMOTE_API_ERROR } from '@/entities/events';
import type { Task, TaskAction } from '@/entities/task/task';
import type { TaskActionIngest } from '@/entities/task/taskingest';
import { identityStore } from '@/stores/identitystore';
import { eventBus } from '@/util/eventbus';
import { computed, defineComponent, reactive, type PropType } from 'vue';

interface SelectOption {
    value: number | string
    label: string
}

export default defineComponent({
    props: {
        action: {
            type: Object as PropType<TaskAction>,
            required: true
        },
        task: {
            type: Object as PropType<Task>,
            required: true
        },
        readonly: {
            type: Boolean,
            required: true
        }
    },
    setup(props) {
        let act = props.action as TaskActionIngest;
        let tablesMeta = {} as {
            [key: string]: {
                schema: string
                tableName: string
            }[]
        }
        return reactive({
            act,
            connections: [] as SelectOption[],
            tablesMeta,
            schemas: [] as SelectOption[],
            tables: [] as SelectOption[]
        })
    },
    mounted() {
        let user = identityStore().currentIdentity()
        connApiHelper.workspaceConnections(user, this.task.workspaceId).then(rsp => {
            this.connections = rsp.data.connections.map(c => {
                return {
                    value: c.id,
                    label: dbConfigStringify(c.configuration)
                }
            })
            let con = this.connections.find(c => c.value === this.act.connectionId)
            if (con !== undefined) {
                let conId = typeof con.value === 'number' ? con.value : parseInt(con.value)
                connApiHelper.listTables(user, this.task.workspaceId, conId).then(r => {
                    this.tablesMeta = r.data
                    this.calcSchemas()
                    this.calcTables()
                })
            } else {
                eventBus.send(EVENT_REMOTE_API_ERROR, `connection ${this.act.connectionId} doen's exist!`)
            }
        }).catch(err => {
            eventBus.send(EVENT_REMOTE_API_ERROR, err)
        })
    },
    methods: {
        calcSchemas() {
            console.log('compute schemas')
            let ret = []
            for (let i in this.tablesMeta) {
                console.log(`key ${i}`)
                ret.push(i)
            }
            this.schemas = ret.map(i => {
                return {
                    label: i,
                    value: i
                }
            })
        },
        calcTables() {
            let found = this.tablesMeta[(this.act.ingestOptions.targetSchema)]
            if (found === undefined) {
                return []
            } else {
                this.tables = found.map(t => {
                    return {
                        label: t.tableName,
                        value: t.tableName
                    }
                })
            }
        }
    }
})
</script>

<template>
    <n-grid cols="2">
        <n-gi>
            <span class="field">Connection</span>
            <n-select :options="connections" v-model:value="act.connectionId" :readonly="readonly" />
        </n-gi>
        <n-gi>
            <div>&nbsp;</div>
            <n-button type="info" class="connedit">Edit Connection</n-button>
        </n-gi>
        <n-gi>
            <n-popselect :options="schemas" v-model:value="act.ingestOptions.targetSchema" :readonly="readonly">
                <n-button>Schema: {{ act.ingestOptions.targetSchema }}</n-button>
            </n-popselect>
            <n-popselect :options="tables" v-model:value="act.ingestOptions.targetTable" :readonly="readonly">
                <n-button>Table: {{ act.ingestOptions.targetTable }}</n-button>
            </n-popselect>
        </n-gi>
        <n-gi></n-gi>
        <n-gi>
            <n-checkbox v-model:checked="act.ingestOptions.addBatch" :disabled="readonly">Add batch column automatically if
                absent</n-checkbox>
        </n-gi>
        <n-gi>
            <n-input :value="act.ingestOptions.batchColumnName" type="text" :readonly="readonly"
                v-if="act.ingestOptions.addBatch" />
        </n-gi>
        <n-gi>
            <n-checkbox v-model:checked="act.ingestOptions.clearTable" :disabled="readonly">
                Clear existing data
            </n-checkbox>
        </n-gi>
        <n-gi>
            <n-checkbox v-model:checked="act.ingestOptions.createTableIfNeeded" :disabled="readonly">
                Create table if not existed
            </n-checkbox>
        </n-gi>
    </n-grid>
</template>

<style scoped>
.connedit {
    vertical-align: bottom;
    float: right;
    width: 120px;
}
</style>
