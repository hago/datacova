<script lang="ts">
import type { WorkspaceWithUser } from '@/api/workspaceapi';
import { defineComponent, reactive, type PropType } from 'vue';

export default defineComponent({
    props: {
        connection: {
            type: Object as PropType<WorkspaceWithUser>,
            required: true
        }
    },
    setup(props) {
        return reactive({})
    }
})
</script>

<template></template>
