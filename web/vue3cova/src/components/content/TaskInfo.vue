<script lang="ts">
import taskApiHelper from '@/api/taskapi';
import { defineComponent, reactive, ref, type PropType } from 'vue';
import type { WorkspaceWithUser } from '@/api/workspaceapi'
import type { Task } from '@/entities/task/task';
import { currentIdentity, identityStore } from '@/stores/identitystore';

export default defineComponent({
    name: "TaskInfo",
    props: {
        task: Object as PropType<Task>
    },
    setup() {
        return {}
    }
})
</script>

<template>
    <n-grid cols="2">
        <n-gi>
            <h2>{{ `Workspace ${task?.name}` }}</h2>
        </n-gi>
    </n-grid>
</template>
