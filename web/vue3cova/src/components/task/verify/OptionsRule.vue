<script lang="ts">
import type { RuleConfig } from '@/entities/task/taskverify';
import { fromRuleConfig, type OptionsRuleConfig } from '@/entities/task/verify/optionsruleconfig';
import { defineComponent, type PropType } from 'vue';

export default defineComponent({
    props: {
        ruleConfig: {
            type: Object as PropType<RuleConfig>,
            required: true
        },
        lineCount: Number
    },
    setup(props) {
        let conf = fromRuleConfig(props.ruleConfig)
        return {
            conf,
            maxLineItemCount: props.lineCount === undefined ? 3 : props.lineCount
        }
    }
})
</script>

<template>
    <n-grid :cols="maxLineItemCount">
        <n-gi :span="maxLineItemCount - 1">
            <n-checkbox v-model:checked="conf.caseSensitive">Case Sensitive</n-checkbox>
        </n-gi>
        <n-gi>
            <n-button primary size="small" class="rightbutton" @click="conf.options.push('')">+Option</n-button>
        </n-gi>
        <n-gi v-for="(opt, i) in conf.options" :key="i">
            <n-input-group>
                <n-input type="text" v-model:value="conf.options[i]" />
                <n-button warning size="small" @click="conf.options.splice(i, 1)">-</n-button>
            </n-input-group>
        </n-gi>
    </n-grid>
</template>

<style scoped>
.rightbutton {
    float: right;
}
</style>
