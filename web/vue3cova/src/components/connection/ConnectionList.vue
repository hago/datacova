<script lang="ts">
import connApiHelper from '@/api/connectionapi';
import type { WorkspaceWithUser } from '@/api/workspaceapi';
import type { WorkspaceConnection } from '@/entities/connection/workspaceconnection';
import { EVENT_CONNECTION_SELECTED, EVENT_REMOTE_API_ERROR } from '@/entities/events';
import { identityStore } from '@/stores/identitystore';
import { eventBus } from '@/util/eventbus';
import { defineComponent, reactive, type PropType } from 'vue';

export default defineComponent({
    props: {
        workspace: {
            type: Object as PropType<WorkspaceWithUser>,
            required: true
        }
    },
    setup(props) {
        return reactive({
            connections: [] as WorkspaceConnection[],
            selectedConnection: null as WorkspaceConnection | null,
            permissions: {
                isOwner: false,
                canModify: false,
                canDelete: false
            }
        })
    },
    mounted() {
        let user = identityStore().currentIdentity()
        connApiHelper.workspaceConnections(user, this.workspace.workspace.id).then(rsp => {
            this.connections = rsp.data.connections
            this.permissions.isOwner = rsp.data.owner
            this.permissions.canDelete = rsp.data.canDelete
            this.permissions.canModify = rsp.data.canModify
        }).catch(err => {
            eventBus.send(EVENT_REMOTE_API_ERROR, err)
        })
    },
    methods: {
        newConnection() {

        },
        selectConnection(connectionId: number) {
            let conn = this.connections.find(c => c.id === connectionId)
            this.selectedConnection = conn === undefined ? null : conn
            eventBus.send(EVENT_CONNECTION_SELECTED, conn)
        }
    }
})
</script>

<template>
    <n-button type="primary" @click="newConnection">New Task</n-button>
    <n-list style="width: 90%">
        <n-space v-for="conn in connections" v-bind:key="conn.id"
            :class="(selectedConnection !== null) && (conn.id === selectedConnection.id) ? 'taskitem selectedtaskitem' : 'taskitem'">
            <div @click="selectConnection(conn.id)" :title="conn.description">{{ conn.name }}</div>
        </n-space>
    </n-list>
</template>

<style scoped>
.taskitem {
    font-size: large;
    border-top: 0px;
    border-left: 0px;
    border-right: 0px;
    border-bottom: 1px;
    border-style: solid;
    margin-top: 3px;
}

.taskitem:hover {
    cursor: pointer;
    color: aqua;
}

.selectedtaskitem {
    color: aqua;
    background-color: green;
}
</style>
