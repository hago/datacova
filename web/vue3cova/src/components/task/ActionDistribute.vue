<script lang="ts">
import type { Task, TaskAction } from '@/entities/task/task';
import type { TaskActionDistribute } from '@/entities/task/taskdist';
import { defineComponent, reactive, type PropType } from 'vue';

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
        let act = props.action as TaskActionDistribute
        return reactive({
            act
        })
    }
})
</script>

<template>
    <div>{{ action.name }} action dist</div>
</template>
