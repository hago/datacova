<script lang="ts">
import { EVENT_TASK_ACTION_DELETE } from '@/entities/events';
import type { TaskAction } from '@/entities/task/task';
import { eventBus } from '@/util/eventbus';
import { defineComponent, reactive, type PropType } from 'vue';

export default defineComponent({
    props: {
        action: {
            type: Object as PropType<TaskAction>,
            required: true
        },
        index: {
            type: Number,
            required: true
        },
        readonly: {
            type: Boolean,
            required: true
        }
    },
    setup(props) {
        return reactive({})
    },
    methods: {
        renderFoldIcon() {
            return (this.action.expand === undefined) || this.action.expand ? "hide" : "show"
        },
        collapse() {
            if (this.action.expand === undefined) {
                this.action.expand = false
            } else {
                this.action.expand = !this.action.expand
            }
        },
        deleteAction(index: number) {
            eventBus.send(EVENT_TASK_ACTION_DELETE, index)
        }
    }
})
</script>

<template>
    <div>
        <span class="actionname">Action: {{ action.name }}</span>
        <n-button quaternary type="warning" style="float:right" :render-icon="renderFoldIcon" v-model="action.expand"
            @click="collapse"></n-button>
        <n-button quaternary type="error" style="float:right" @click="deleteAction(index)">Remove</n-button>
    </div>
</template>

<style scoped>
.actionname {
    color: cornflowerblue;
}
</style>
