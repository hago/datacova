<script lang="ts">
import type { RuleConfig } from '@/entities/task/taskverify';
import { fromRuleConfig, newTimeBoundary } from '@/entities/task/verify/timerange';
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
        let conf = fromRuleConfig(props.ruleConfig)
        return reactive({
            conf,
            hasLowerLimit: computed({
                get: (): boolean => conf.lowerBoundary !== null,
                set: (v: boolean) => {
                    conf.lowerBoundary = v ? newTimeBoundary() : null
                }
            }),
            hasUpperLimit: computed({
                get: (): boolean => conf.upperBoundary !== null,
                set: (v: boolean) => {
                    conf.upperBoundary = v ? newTimeBoundary() : null
                }
            })
        })
    }
})
</script>

<template>
    <n-grid cols="2">
        <n-gi>
            <n-checkbox v-model:checked.boolean="hasLowerLimit">Starting At</n-checkbox>
            <n-input-group>
                <n-date-picker v-model:value="conf.lowerBoundary!.timeStamp" type="datetime" v-if="hasLowerLimit" />
                <n-checkbox v-model:checked.boolean="conf.lowerBoundary!.inclusive" v-if="hasLowerLimit">
                    Inclusive
                </n-checkbox>
            </n-input-group>
        </n-gi>
        <n-gi>
            <n-checkbox v-model:checked.boolean="hasUpperLimit">Ends At</n-checkbox>
            <n-input-group>
                <n-date-picker v-model:value="conf.upperBoundary!.timeStamp" type="datetime" v-if="hasUpperLimit" />
                <n-checkbox v-model:checked.boolean="conf.upperBoundary!.inclusive" v-if="hasUpperLimit">
                    Inclusive
                </n-checkbox>
            </n-input-group>
        </n-gi>
    </n-grid>
</template>

<style scoped></style>
