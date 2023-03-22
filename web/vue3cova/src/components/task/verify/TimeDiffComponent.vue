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
                <n-tag type="info" class="timepart">Year</n-tag>
                <n-input-number v-model:value="diff.year" clearable />
            </n-input-group>
        </n-gi>
        <n-gi>
            <n-input-group>
                <n-tag type="info" class="timepart">Month</n-tag>
                <n-input-number v-model:value="diff.month" clearable />
            </n-input-group>
        </n-gi>
        <n-gi>
            <n-input-group>
                <n-tag type="info" class="timepart">Day</n-tag>
                <n-input-number v-model:value="diff.day" clearable />
            </n-input-group>
        </n-gi>
        <n-gi>
            <n-input-group>
                <n-tag type="info" class="timepart">Hour</n-tag>
                <n-input-number v-model:value="diff.hour" clearable />
            </n-input-group>
        </n-gi>
        <n-gi>
            <n-input-group>
                <n-tag type="info" class="timepart">Month</n-tag>
                <n-input-number v-model:value="diff.minute" clearable />
            </n-input-group>
        </n-gi>
        <n-gi>
            <n-input-group>
                <n-tag type="info" class="timepart">Second</n-tag>
                <n-input-number v-model:value="diff.second" clearable />
            </n-input-group>
        </n-gi>
    </n-grid>
</template>

<style scoped>
.timepart {
    width: 120px;
}
</style>
