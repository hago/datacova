<script lang="ts">
import type { Task, TaskAction } from '@/entities/task/task';
import { newTaskActionVerify } from '@/entities/task/taskverify';
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
        let act = newTaskActionVerify(props.action)
        return reactive({
            act
        })
    }
})
</script>

<template>
    <div>{{ action.name }} action verify</div>
</template>
