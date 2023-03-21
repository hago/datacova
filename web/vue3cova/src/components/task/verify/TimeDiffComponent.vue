<script lang="ts">
import type { TimeDiff } from '@/entities/task/verify/relativetimerange';
import { defineComponent, reactive, type PropType } from 'vue';

export default defineComponent({
    props: {
        diff: {
            type: Object as PropType<TimeDiff>,
            required: true
        }
    },
    setup(props) {
        return reactive({})
    }
})
</script>

<template>
    <n-grid cols="2">
        <n-gi>
            <n-input-group>
                <n-tag>Year</n-tag>
                <n-input-number v-model:value="diff.year" clearable />
            </n-input-group>
        </n-gi>
        <n-gi>
            <n-input-group>
                <n-tag>Month</n-tag>
                <n-input-number v-model:value="diff.month" clearable />
            </n-input-group>
        </n-gi>
        <n-gi>
            <n-input-group>
                <n-tag>Day</n-tag>
                <n-input-number v-model:value="diff.day" clearable />
            </n-input-group>
        </n-gi>
        <n-gi>
            <n-input-group>
                <n-tag>Hour</n-tag>
                <n-input-number v-model:value="diff.hour" clearable />
            </n-input-group>
        </n-gi>
        <n-gi>
            <n-input-group>
                <n-tag>Month</n-tag>
                <n-input-number v-model:value="diff.minute" clearable />
            </n-input-group>
        </n-gi>
        <n-gi>
            <n-input-group>
                <n-tag>Second</n-tag>
                <n-input-number v-model:value="diff.second" clearable />
            </n-input-group>
        </n-gi>
    </n-grid>
</template>

<style scoped></style>
