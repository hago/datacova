<script lang="ts">
import type { WorkspaceWithUser } from '@/api/workspaceapi';
import type { WorkspaceConnection } from '@/entities/connection/workspaceconnection';
import { defineComponent, reactive, type PropType } from 'vue';
import ConnectionList from '@/components/connection/ConnectionList.vue'
import ConnectionInfo from '@/components/connection/ConnectionInfo.vue'
import EmptyConnectionInfo from '@/components/connection/EmptyConnectionInfo.vue'
import { eventBus } from '@/util/eventbus';
import { EVENT_CONNECTION_DELETED, EVENT_CONNECTION_SELECTED } from '@/entities/events';

export default defineComponent({
    components: {
        ConnectionInfo, ConnectionList, EmptyConnectionInfo
    },
    props: {
        workspace: {
            type: Object as PropType<WorkspaceWithUser>,
            required: true
        }
    },
    setup(props) {
        return reactive({
            selectedConnection: null as WorkspaceConnection | null,
            editable: false,
            deletable: false
        })
    },
    mounted() {
        eventBus.register(EVENT_CONNECTION_SELECTED, this.onConnectionSelected)
        eventBus.register(EVENT_CONNECTION_DELETED, this.onConnectionDeleted)
    },
    unmounted() {
        eventBus.unregister(EVENT_CONNECTION_SELECTED, this.onConnectionSelected)
        eventBus.unregister(EVENT_CONNECTION_DELETED, this.onConnectionDeleted)
    },
    methods: {
        onConnectionSelected(connection: WorkspaceConnection, edit: boolean, deletable: boolean): Promise<any> {
            this.selectedConnection = connection
            this.editable = edit
            this.deletable = deletable
            return Promise.resolve()
        },
        onConnectionDeleted(connection: WorkspaceConnection): Promise<any> {
            console.log(`Connection ${connection.name} (${connection.id}) deleted!!`)
            this.selectedConnection = null
            return Promise.resolve()
        }
    }
})
</script>

<template>
    <n-grid cols="5">
        <n-gi span="2">
            <ConnectionList :workspace="workspace"></ConnectionList>
        </n-gi>
        <n-gi span="3">
            <ConnectionInfo v-if="selectedConnection !== null" :connection="selectedConnection" :editable="editable"
                :deletable="deletable"></ConnectionInfo>
            <EmptyConnectionInfo v-if="selectedConnection === null"></EmptyConnectionInfo>
        </n-gi>
    </n-grid>
</template>

<style scoped></style>
