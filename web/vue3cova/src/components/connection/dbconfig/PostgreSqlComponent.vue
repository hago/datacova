<script lang="ts">
import type { BaseDbConfig } from '@/entities/connection/dbconfigbase';
import { fromBaseConfig } from '@/entities/connection/pgconfig';
import { computed, defineComponent, reactive, type PropType } from 'vue';

export default defineComponent({
    props: {
        config: {
            type: Object as PropType<BaseDbConfig>,
            required: true
        },
        editable: Boolean
    },
    setup(props) {
        let conf = fromBaseConfig(props.config)
        return reactive({
            conf,
            port: computed({
                get: () => (conf.port.toString()),
                set: (v: any) => {
                    if (typeof (v) === 'string') {
                        conf.port = parseInt(v)
                    }
                }
            })
        })
    }
})
</script>

<template>
    <n-grid cols="2">
        <n-gi>
            <span class="field">Host</span>
            <n-input class="taskname" v-model:value="conf.host" type="text" placeholder="Host" :readonly="!editable" />
        </n-gi>
        <n-gi>
            <span class="field">Port</span>
            <n-input class="taskname" v-model:value="port" type="number" placeholder="port" :readonly="!editable" />
        </n-gi>
    </n-grid>
</template>

<style scoped></style>
