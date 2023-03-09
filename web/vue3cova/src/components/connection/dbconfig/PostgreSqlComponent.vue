<script lang="ts">
import type { BaseDbConfig } from '@/entities/connection/dbconfigbase';
import type { PostgreSqlConfig } from '@/entities/connection/pgconfig';
import { defineComponent, reactive, type PropType } from 'vue';

export default defineComponent({
    props: {
        config: {
            type: Object as PropType<BaseDbConfig>,
            required: true
        }
    },
    setup(props) {
        let conf = props.config as PostgreSqlConfig
        return reactive({
            conf
        })
    }
})
</script>

<template>
    
</template>

<style scoped>
</style>
