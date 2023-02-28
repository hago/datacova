<script lang="ts">
import type { TaskAction } from '@/entities/task/task';
import { defineComponent, reactive, type PropType } from 'vue';

export default defineComponent({
    props: {
        action: {
            type: Object as PropType<TaskAction>,
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
        }
    }
})
</script>

<template>
    <div>
        <span>Action: {{ action.name }}</span>
        <n-button quaternary type="warning" style="float:right" :render-icon="renderFoldIcon" v-model="action.expand"
            @click="collapse"></n-button>
    </div>
</template>
