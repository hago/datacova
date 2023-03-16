<script lang="ts">
import connApiHelper from '@/api/connectionapi';
import type { WorkspaceWithUser } from '@/api/workspaceapi';
import { newWorkspaceConnection, type WorkspaceConnection } from '@/entities/connection/workspaceconnection';
import { EVENT_CONNECTION_DELETED, EVENT_CONNECTION_SELECTED, EVENT_REMOTE_API_ERROR } from '@/entities/events';
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
                deletables: [] as number[],
                editables: [] as number[]
            }
        })
    },
    mounted() {
        this.loadConnections()
        eventBus.register(EVENT_CONNECTION_DELETED, this.connectionDeleted)
    },
    unmounted() {
        eventBus.unregister(EVENT_CONNECTION_DELETED, this.connectionDeleted)
    },
    methods: {
        loadConnections() {
            let user = identityStore().currentIdentity()
            connApiHelper.workspaceConnections(user, this.workspace.workspace.id).then(rsp => {
                this.connections = rsp.data.connections
                this.permissions.isOwner = rsp.data.owner
                this.permissions.deletables = rsp.data.canDelete
                this.permissions.editables = rsp.data.canModify
            }).catch(err => {
                eventBus.send(EVENT_REMOTE_API_ERROR, err)
            })
        },
        newConnection() {
            let con: WorkspaceConnection = newWorkspaceConnection(this.workspace.workspace.id)
            this.connections = [con].concat(this.connections)
            this.permissions.deletables.push(con.id)
            this.permissions.editables.push(con.id)
            this.selectConnection(con.id)
        },
        selectConnection(connectionId: number) {
            let conn = this.connections.find(c => c.id === connectionId)
            this.selectedConnection = conn === undefined ? null : conn
            eventBus.send(EVENT_CONNECTION_SELECTED, conn,
                this.permissions.editables.indexOf(connectionId) >= 0,
                this.permissions.deletables.indexOf(connectionId) >= 0)
        },
        async connectionDeleted(conn: WorkspaceConnection): Promise<any> {
            this.loadConnections()
            return Promise.resolve()
        }
    }
})
</script>

<template>
    <n-button type="primary" @click="newConnection">New Connection</n-button>
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
