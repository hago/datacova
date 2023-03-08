<script lang="ts">
import type { WorkspaceWithUser } from '@/api/workspaceapi';
import type { WorkspaceConnection } from '@/entities/connection/workspaceconnection';
import { defineComponent, reactive, type PropType } from 'vue';
import ConnectionList from '@/components/connection/ConnectionList.vue'
import ConnectionInfo from '@/components/connection/ConnectionInfo.vue'
import EmptyConnectionInfo from '@/components/connection/EmptyConnectionInfo.vue'

export default defineComponent({
    props: {
        workspace: {
            type: Object as PropType<WorkspaceWithUser>,
            required: true
        }
    },
    setup(props) {
        return reactive({
            selectedConnection: null as WorkspaceConnection | null
        })
    }
})
</script>

<template>
    <n-grid cols="5">
        <n-gi span="2">
            <ConnectionList :workspace="workspace"></ConnectionList>
        </n-gi>
        <n-gi span="3">
            <ConnectionInfo v-if="selectedConnection !== null" :task="selectedConnection"></ConnectionInfo>
            <EmptyConnectionInfo v-if="selectedConnection === null"></EmptyConnectionInfo>
        </n-gi>
    </n-grid>
</template>

<style scoped></style>
