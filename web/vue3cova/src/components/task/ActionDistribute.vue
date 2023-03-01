<script lang="ts">
import type { Task, TaskAction } from '@/entities/task/task';
import { emptyDistConfiguration, type TaskActionDistribute } from '@/entities/task/taskdist';
import { defineComponent, reactive, type PropType } from 'vue';
import ActionDistFtp from '@/components/task/distribute/ActionDistFtp.vue'
import ActionDistSFtp from '@/components/task/distribute/ActionDistSFtp.vue'

export default defineComponent({
    components: {
        ActionDistFtp, ActionDistSFtp
    },
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
        if (act.configuration === undefined) {
            act.configuration = emptyDistConfiguration()
        }
        return reactive({
            act,
            methodOptions: [
                {
                    label: "Ftp",
                    value: "ftp"
                },
                {
                    label: "SFtp",
                    value: "sftp"
                }
            ]
        })
    },
    mounted() {
    }
})
</script>

<template>
    <n-grid cols="2">
        <n-gi>
            <span>Method</span>
        </n-gi>
        <n-gi>
            <n-select :options="methodOptions" v-model:value="act.configuration.type" :fallback-option="(v: any) => ({
                label: 'Select a Distribution Method', value: v
            })"></n-select>
        </n-gi>
        <ActionDistFtp :action="act" :task="task" :readonly="readonly" v-if="act.configuration.type === 'ftp'" />
        <ActionDistSFtp :action="act" :task="task" :readonly="readonly" v-if="act.configuration.type === 'sftp'" />
    </n-grid>
</template>

<style scoped>
.field {
    margin-right: 5px;
}
</style>
