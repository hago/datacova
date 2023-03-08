<script lang="ts">
import type { WorkspaceConnection } from '@/entities/connection/workspaceconnection';
import { defineComponent, reactive, type PropType } from 'vue';

export default defineComponent({
    props: {
        connection: {
            type: Object as PropType<WorkspaceConnection>,
            required: true
        }
    },
    setup(props) {
        return reactive({})
    }
})
</script>

<template></template>
