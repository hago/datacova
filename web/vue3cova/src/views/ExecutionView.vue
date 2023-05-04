<script lang="ts">
import type { WorkspaceWithUser } from '@/api/workspaceapi';
import { defineComponent, reactive, type PropType } from 'vue';

export default defineComponent({
  props: {
        workspace: {
            type: Object as PropType<WorkspaceWithUser>,
            required: true
        }
    },
    setup(props) {
        return reactive({
            
        })
    },
})
</script>

<template>
  <div>
    Executions Here
  </div>
</template>

<style scoped>
</style>
