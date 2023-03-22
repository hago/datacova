<script lang="ts">
import type { RuleConfig } from '@/entities/task/taskverify';
import { fromRuleConfig, newRelativeTimeBoundary, TimeAnchor } from '@/entities/task/verify/relativetimerange';
import { timezones } from '@/entities/timezones';

import { computed } from '@vue/reactivity';
import { defineComponent, reactive, type PropType } from 'vue';
import TimeDiffComponent from './TimeDiffComponent.vue';

export default defineComponent({
    components: { TimeDiffComponent },
    props: {
        ruleConfig: {
            type: Object as PropType<RuleConfig>,
            required: true
        }
    },
    setup(props) {
        let conf = fromRuleConfig(props.ruleConfig)
        let anchorList = Object.keys(TimeAnchor)
        return reactive({
            conf,
            anchors: anchorList.map(i => ({ label: TimeAnchor[i] as string, value: i })),
            timezones: timezones.map(tz => ({ label: tz.name, value: tz.name })),
            hasLowerLimit: computed({
                get: (): boolean => conf.lowerBoundary !== null,
                set: (v: boolean) => {
                    conf.lowerBoundary = v ? newRelativeTimeBoundary() : null
                }
            }),
            hasUpperLimit: computed({
                get: (): boolean => conf.upperBoundary !== null,
                set: (v: boolean) => {
                    conf.upperBoundary = v ? newRelativeTimeBoundary() : null
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
            <n-select :options="anchors" v-model:value="conf.lowerBoundary!.anchor" v-if="hasLowerLimit"></n-select>
            <n-checkbox v-model:checked.boolean="conf.lowerBoundary!.inclusive" v-if="hasLowerLimit">
                Inclusive
            </n-checkbox>
            <n-input-group v-if="hasLowerLimit">
                <n-tag>Time Zone</n-tag>
                <n-select filterable :options="timezones" tag v-model:value="conf.lowerBoundary!.timeZoneName"
                    v-if="hasLowerLimit"></n-select>
            </n-input-group>
            <TimeDiffComponent :diff="conf.lowerBoundary!.diff" v-if="hasLowerLimit"></TimeDiffComponent>
        </n-gi>
        <n-gi>
            <n-checkbox v-model:checked.boolean="hasUpperLimit">Ends At</n-checkbox>
            <n-select :options="anchors" v-model:value="conf.upperBoundary!.anchor" v-if="hasUpperLimit"></n-select>
            <n-checkbox v-model:checked.boolean="conf.upperBoundary!.inclusive" v-if="hasUpperLimit">
                Inclusive
            </n-checkbox>
            <n-input-group v-if="hasUpperLimit">
                <n-tag>Time Zone</n-tag>
                <n-select filterable :options="timezones" tag v-model:value="conf.upperBoundary!.timeZoneName"
                    v-if="hasUpperLimit"></n-select>
            </n-input-group>
            <TimeDiffComponent :diff="conf.upperBoundary!.diff" v-if="hasUpperLimit"></TimeDiffComponent>
        </n-gi>
    </n-grid>
</template>

<style scoped></style>
