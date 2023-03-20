<script lang="ts">
import type { RuleConfig } from '@/entities/task/taskverify';
import { newBoundary } from '@/entities/task/verify/numberboundary';
import { fromRuleConfig } from '@/entities/task/verify/numberrangeconfig';
import { computed } from '@vue/reactivity';
import { defineComponent, reactive, type PropType } from 'vue';

export default defineComponent({
    props: {
        ruleConfig: {
            type: Object as PropType<RuleConfig>,
            required: true
        }
    },
    setup(props) {
        let numberConfig = fromRuleConfig(props.ruleConfig)
        return reactive({
            numberConfig,
            lowerLimit: computed({
                get: (): boolean => {
                    return (numberConfig.lowerBoundary !== undefined) && (numberConfig.lowerBoundary !== null);
                },
                set: (v: boolean) => {
                    console.log('set ', typeof (v), v)
                    if (v) {
                        if ((numberConfig.lowerBoundary === undefined) || (numberConfig.lowerBoundary === null)) {
                            numberConfig.lowerBoundary = newBoundary()
                        }
                    } else {
                        numberConfig.lowerBoundary = null
                    }
                }
            }),
            upperLimit: computed({
                get: (): boolean => {
                    return (numberConfig.upperBoundary !== undefined) && (numberConfig.upperBoundary !== null);
                },
                set: (v: boolean) => {
                    console.log('set ', typeof (v), v)
                    if (v) {
                        if ((numberConfig.upperBoundary === undefined) || (numberConfig.upperBoundary === null)) {
                            numberConfig.upperBoundary = newBoundary()
                        }
                    } else {
                        numberConfig.upperBoundary = null
                    }
                }
            })
        })
    },
})
</script>

<template>
    <n-grid cols="2">
        <n-gi>
            <n-checkbox v-model:checked.boolean="lowerLimit" :value="lowerLimit.toString()">Lower Boundary</n-checkbox>
            <n-input-number v-if="lowerLimit" v-model:value="numberConfig.lowerBoundary!.value" />
            <n-checkbox v-if="lowerLimit"
                v-model:checked.boolean="numberConfig.lowerBoundary!.inclusive">Inclusive</n-checkbox>
        </n-gi>
        <n-gi>
            <n-checkbox v-model:checked.boolean="upperLimit" :value="upperLimit.toString()">Upper Boundary</n-checkbox>
            <n-input-number v-if="upperLimit" v-model:value="numberConfig.upperBoundary!.value" />
            <n-checkbox v-if="upperLimit"
                v-model:checked.boolean="numberConfig.upperBoundary!.inclusive">Inclusive</n-checkbox>
        </n-gi>
    </n-grid>
</template>

<style scoped></style>
