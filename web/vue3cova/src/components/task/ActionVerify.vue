<script lang="ts">
import type { Task, TaskAction } from '@/entities/task/task';
import {
    newTaskActionVerify, VerificationTypes, isRegexRule, isOptionsRule, newVerifyConfiguration, isNumberRangeRule,
    isPythonRule, isTimeRangeRule, isRelativeTimeRangeRule
} from '@/entities/task/taskverify';
import { defineComponent, type PropType } from 'vue';
import RegexRule from './verify/RegexRule.vue';
import OptionsRule from './verify/OptionsRule.vue';
import NumberRangeRule from './verify/NumberRangeRule.vue';
import PythonRule from './verify/PythonRule.vue';
import TimeRangeRule from './verify/TimeRangeRule.vue';

export default defineComponent({
    components: { RegexRule, OptionsRule, NumberRangeRule, PythonRule, TimeRangeRule },
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
            isRegexRule,
            isOptionsRule,
            isNumberRangeRule,
            isPythonRule,
            isTimeRangeRule,
            isRelativeTimeRangeRule
        };
    },
    methods: {
        deleteField(index: number, j: number) {
            console.log(index, j);
            this.act.configurations[index].fields.splice(j, 1);
        },
        newRuleConfig() {
            this.act.configurations = [newVerifyConfiguration()].concat(this.act.configurations)
        }
    }
})
</script>

<template>
    <n-button type="default" class="rightbutton" @click="newRuleConfig">New Rule</n-button>
    <n-grid cols="2" v-for="(config, i) in act.configurations" :key="i"
        style="margin-bottom: 2px; border-bottom-style: dashed;">
        <n-gi>
            <span>Rules</span>
        </n-gi>
        <n-gi span="2">
            <span>Field{{ config.fieldCountLimit > 1 ? "s" : "" }}</span>
            <n-button type="success" size="x-small" class="fieldbutton rightbutton" @click="config.fields.push('')"
                title="Add Field">+</n-button>
        </n-gi>
        <n-gi span="2">
            <n-grid cols="2">
                <n-gi v-for="(field, j) in config.fields" :key="j">
                    <n-input-group>
                        <n-input type="text" v-model:value="config.fields[j]" />
                        <n-button type="warning" size="x-small" class="fieldbutton" @click="deleteField(i, j)"
                            v-if="config.fields.length > 1">-</n-button>
                    </n-input-group>
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
            <n-checkbox v-model:checked.boolean="config.ruleConfig.nullable">
                Make verification true for Null value
            </n-checkbox>
        </n-gi>
        <n-gi span="2">
            <RegexRule :ruleConfig="config.ruleConfig" v-if="isRegexRule(config.ruleConfig)"></RegexRule>
            <OptionsRule :ruleConfig="config.ruleConfig" v-if="isOptionsRule(config.ruleConfig)"></OptionsRule>
            <NumberRangeRule :ruleConfig="config.ruleConfig" v-if="isNumberRangeRule(config.ruleConfig)"></NumberRangeRule>
            <PythonRule :verifyConfig="config" v-if="isPythonRule(config.ruleConfig)"></PythonRule>
            <TimeRangeRule :ruleConfig="config.ruleConfig" v-if="isTimeRangeRule(config.ruleConfig)"></TimeRangeRule>
        </n-gi>
        <n-gi span="2">
            <n-button type="warning" class="rightbutton" @click="act.configurations.splice(i, 1)">Remove Rule</n-button>
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
