<script lang="ts">
import connApiHelper from '@/api/connectionapi';
import { dbConfigStringify } from '@/entities/connection/workspaceconnection';
import { EVENT_REMOTE_API_ERROR } from '@/entities/events';
import type { Task, TaskAction } from '@/entities/task/task';
import type { TaskActionIngest } from '@/entities/task/taskingest';
import { identityStore } from '@/stores/identitystore';
import { eventBus } from '@/util/eventbus';
import { defineComponent, reactive, type PropType } from 'vue';

interface ConnectionOption {
    value: number
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
        let act = props.action as TaskActionIngest
        return reactive({
            act,
            connections: [] as ConnectionOption[]
        })
    },
    mounted() {
        connApiHelper.workspaceConnections(identityStore().currentIdentity(), this.task.workspaceId).then(rsp => {
            this.connections = rsp.data.connections.map(c => {
                return {
                    value: c.id,
                    label: dbConfigStringify(c.configuration)
                }
            })
        }).catch(err => {
            eventBus.send(EVENT_REMOTE_API_ERROR, err)
        })
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
    </n-grid>
    <div>{{ action.name }} action ingest</div>
</template>

<style scoped>
.connedit {
    vertical-align: bottom;
    float: right;
    width: 120px;
}
</style>
