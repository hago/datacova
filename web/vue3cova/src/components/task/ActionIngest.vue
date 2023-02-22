<script lang="ts">
import type { Task, TaskAction } from '@/entities/task/task';
import type { TaskActionIngest } from '@/entities/task/taskingest';
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
        }
    },
    setup(props) {
        let act = props.action as TaskActionIngest
        return reactive({
            act
        })
    }
})
</script>

<template>
    <div>{{ action.name }} action ingest</div>
</template>
