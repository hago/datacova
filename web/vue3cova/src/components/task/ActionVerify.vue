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
            act,
            ruleOptions: [
                { label: 'Python Script', value: 'com.hagoapp.embed.jython' },
                { label: 'Number Range', value: 'com.hagoapp.number.range' },
                { label: 'Options', value: 'com.hagoapp.options' },
                { label: 'Regular Expression Matching', value: 'com.hagoapp.regex' },
                { label: 'Time Range', value: 'com.hagoapp.time.range' },
                { label: 'Relative Time Range', value: 'com.hagoapp.relative.time.range' },
            ]
        })
    }
})
</script>

<template>
    <n-grid cols="2" v-for="(rule, index) in act.configurations" :key="index">
        <n-gi span="2">
            <span>Rule Type</span>
            <n-select :options="ruleOptions" :fallback-option="(v: any) => ({
                label: 'Choose Rule Type', value: v
            })" :model:value="rule.ruleConfig"></n-select>
        </n-gi>
    </n-grid>
</template>
