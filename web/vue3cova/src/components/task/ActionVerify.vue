<script lang="ts">
import type { Task, TaskAction } from '@/entities/task/task';
import { newTaskActionVerify, VerificationTypes, isRegexRule } from '@/entities/task/taskverify';
import { defineComponent, reactive, type PropType } from 'vue';
import RegexRule from './verify/RegexRule.vue';

export default defineComponent({
    components: { RegexRule },
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
        let act = newTaskActionVerify(props.action);
        return {
            act,
            ruleOptions: VerificationTypes,
            isRegexRule
        };
    },
    methods: {
        deleteField(index: number, j: number) {
            console.log(index, j);
            this.act.configurations[index].fields.splice(j, 1);
        }
    }
})
</script>

<template>
    <n-grid cols="2" v-for="(config, i) in act.configurations" :key="i">
        <n-gi span="2">
            <span>Fields</span>
            <n-button type="success" size="x-small" class="fieldbutton rightbutton"
                @click="config.fields.push('')">+</n-button>
        </n-gi>
        <n-gi span="2">
            <n-grid cols="2">
                <n-gi v-for="(field, j) in config.fields" :key="j">
                    <n-input type="text" v-model:value="config.fields[j]" />
                    <n-button type="warning" size="x-small" class="fieldbutton" @click="deleteField(i, j)">-</n-button>
                </n-gi>
                <n-gi v-if="config.fields.length % 2 === 1" :key="-1">
                    <!--placeholder-->
                </n-gi>
            </n-grid>
        </n-gi>
        <n-gi>
            <span>Rule Type</span>
        </n-gi>
        <n-gi>
            <n-select :options="ruleOptions" :fallback-option="(v: any) => ({
                label: 'Choose Rule Type', value: v
            })" v-model:value="config.ruleConfig.configType"></n-select>
        </n-gi>
        <n-gi span="2">
            <n-checkbox v-model:checked="config.ruleConfig.nullable">Make verification true for Null value</n-checkbox>
        </n-gi>
        <n-gi span="2">
            <RegexRule :rule-config="config.ruleConfig" v-if="isRegexRule(config.ruleConfig)"></RegexRule>
        </n-gi>
    </n-grid>
</template>

<style scoped>
.rightbutton {
    float: right;
}

.fieldbutton {
    width: 30px;
    font-size: large;
}
</style>
