<script lang="ts">
import { defineComponent, reactive } from 'vue';

export default defineComponent({
  props: {
    workspaceId: {
      type: Number,
      required: true
    },
    id: {
      type: Number,
      required: true
    }
  },
  setup(props) {
    return reactive({})
  }
})
</script>

<template>
  <div>
    Execution Detail
  </div>
</template>

<style scoped>
</style>
