<script lang="ts">
import { defineComponent, type PropType } from 'vue';
import type { Task } from '@/entities/task/task';

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
