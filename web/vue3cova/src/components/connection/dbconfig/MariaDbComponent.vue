<script lang="ts">
import type { BaseDbConfig } from '@/entities/connection/dbconfigbase';
import { fromBaseConfig, type MariaDBConfig } from '@/entities/connection/mariadbconfig';
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
            }),
            engineOptions: [
                { label: 'innodb', value: 'innodb' },
                { label: 'MyISAM', value: 'MyISAM' }
            ]
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
        <n-gi span="2">
            <span class="field">Default Engine</span>
            <n-popselect :options="engineOptions" v-model:value="conf.storeEngine">
                <n-button>{{ conf.storeEngine }}</n-button>
            </n-popselect>
        </n-gi>
    </n-grid>
</template>

<style scoped></style>
