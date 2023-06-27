<script lang="ts">
import connApiHelper from '@/api/connectionapi';
import { dbConfigStringify } from '@/entities/connection/workspaceconnection';
import { EVENT_REMOTE_API_ERROR } from '@/entities/events';
import type { Task, TaskAction } from '@/entities/task/task';
import { newTaskActionIngest } from '@/entities/task/taskingest';
import { identityStore } from '@/stores/identitystore';
import { eventBus } from '@/util/eventbus';
import { defineComponent, reactive, type PropType } from 'vue';

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
        let act = newTaskActionIngest(props.action);
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
            tables: [] as SelectOption[],
            useExistingTable: true
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
            if (this.act.connectionId < 0) {
                return
            }
            let con = this.connections.find(c => c.value === this.act.connectionId)
            if (con !== undefined) {
                let conId = typeof con.value === 'number' ? con.value : parseInt(con.value)
                this.loadTables(conId)
            } else {
                eventBus.send(EVENT_REMOTE_API_ERROR, `connection ${this.act.connectionId} doesn't exist!`)
            }
        }).catch(err => {
            eventBus.send(EVENT_REMOTE_API_ERROR, err)
        })
    },
    methods: {
        loadTables(newConnectionId: number) {
            let user = identityStore().currentIdentity()
            connApiHelper.listTables(user, this.task.workspaceId, newConnectionId).then(rsp => {
                this.act.connectionId = newConnectionId
                this.tablesMeta = rsp.data
                this.calcSchemas()
            }).catch(err => {
                eventBus.send(EVENT_REMOTE_API_ERROR, err)
            })
        },
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
            if (this.act.ingestOptions.targetSchema === '') {
                this.act.ingestOptions.targetSchema = this.schemas.length > 0 ?
                    this.schemas[0].label : ""
            }
            this.calcTables(this.act.ingestOptions.targetSchema)
        },
        calcTables(schema: string) {
            let found = this.tablesMeta[schema]
            if ((found === undefined) || (found.length === 0)) {
                this.act.ingestOptions!.targetTable = ""
                this.tables = []
            } else {
                this.tables = found.map(t => {
                    return {
                        label: t.tableName,
                        value: t.tableName
                    }
                })
                if (this.act.ingestOptions.targetTable === "") {
                    this.act.ingestOptions.targetTable = found[0].tableName
                }
            }
        },
        tblSelectChange(v: boolean) {
            this.useExistingTable = v
        }
    }
})
</script>

<template>
    <n-grid cols="2">
        <n-gi>
            <span class="field">Connection</span>
            <n-select :options="connections" v-model:value="act.connectionId" :readonly="readonly"
                :on-update:value="loadTables" :fallback-option="(v: any) => ({ label: 'Select a Connection', value: v })" />
        </n-gi>
        <n-gi>
            <div>&nbsp;</div>
            <n-button type="info" class="connedit">Edit Connection</n-button>
        </n-gi>
        <n-gi :span="2">
            <n-popselect :options="schemas" v-model:value="act.ingestOptions!.targetSchema" :readonly="readonly"
                @update:value="calcTables">
                <n-button>Schema: {{ act.ingestOptions!.targetSchema }}</n-button>
            </n-popselect>
            <n-radio name="tbl" value="existing" :checked="useExistingTable" @change="tblSelectChange(true)">
                Existing Tables
            </n-radio>
            <n-popselect v-if="useExistingTable" :options="tables" v-model:value="act.ingestOptions!.targetTable"
                :readonly="readonly">
                <n-button>{{ act.ingestOptions!.targetTable }}</n-button>
            </n-popselect>
            <n-radio name="tbl" value="new" :checked="!useExistingTable" @change="tblSelectChange(false)">
                New Table
            </n-radio>
            <n-input v-if="!useExistingTable" v-model:value="act.ingestOptions!.targetTable" :readonly="readonly" autosize
                style="min-width: 50%">
            </n-input>
        </n-gi>
        <n-gi>
            <n-checkbox v-model:checked="act.ingestOptions!.addBatch" :disabled="readonly">
                Add batch column automatically if absent
            </n-checkbox>
        </n-gi>
        <n-gi>
            <n-input :value="act.ingestOptions!.batchColumnName" type="text" :readonly="readonly"
                v-if="act.ingestOptions!.addBatch" />
        </n-gi>
        <n-gi>
            <n-checkbox v-model:checked="act.ingestOptions!.clearTable" :disabled="readonly">
                Clear existing data
            </n-checkbox>
        </n-gi>
        <n-gi>
            <n-checkbox v-model:checked="act.ingestOptions!.createTableIfNeeded" :disabled="readonly">
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
