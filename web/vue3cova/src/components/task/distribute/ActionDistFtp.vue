<script lang="ts">
import type { Task, TaskAction } from '@/entities/task/task';
import { defineComponent, type PropType } from 'vue';

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

    }
})
</script>

<template></template>
