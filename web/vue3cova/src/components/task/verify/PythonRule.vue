<script lang="ts">
import type { RuleConfig } from '@/entities/task/taskverify';
import { fromRuleConfig } from '@/entities/task/verify/jythonconfig';
import { defineComponent, reactive, type PropType } from 'vue';

export default defineComponent({
    props: {
        ruleConfig: {
            type: Object as PropType<RuleConfig>,
            required: true
        }
    },
    setup(props) {
        let config = fromRuleConfig(props.ruleConfig)
        return reactive({
            config,
            sampleInput: "",
            verifying: false,
            result: {
                success: undefined as boolean | undefined,
                message: ''
            }
        })
    },
    methods: {
        evaluate() {

        }
    }
})
</script>

<template>
    <n-checkbox v-model:checked.boolean="config.allowImports" :value="config.allowImports.toString()">Allow Imports</n-checkbox>
    <n-input type="textarea" v-model:value="config.snippet"></n-input>
    <n-input type="textarea" v-model:value="sampleInput" v-if="verifying"></n-input>
    <n-button type="default" @click="verifying = !verifying">Test</n-button>
    <n-button type="default" @click="evaluate" v-if="verifying">Evaluate</n-button>
</template>

<style scoped></style>
