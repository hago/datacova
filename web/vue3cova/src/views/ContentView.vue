<script lang="ts">
import type { WorkspaceWithUser } from '@/api/workspaceapi';
import { workspaceStore } from '@/stores/workspacestore';
import { defineComponent, reactive, ref } from 'vue';


export default defineComponent({
  props: {
    wkId: Number
  },
  setup() {
    let wk: WorkspaceWithUser | null = null
    return reactive({
      workspace: ref<WorkspaceWithUser | null>(wk)
    })
  },
  computed: {
    workspaceName() {
      return this.workspace === null ? 'Loading' : this.workspace.workspace.name
    }
  },
  mounted() {
    if ((this.wkId === undefined) || (this.wkId === null)) {
      let w = workspaceStore().getSelectedWorkspace()
      this.workspace = w
    } else {
      let w = workspaceStore().getWorkspace(this.wkId);
      this.workspace = w
    }
  }
})
</script>

<template>
  <n-card :title="workspaceName" style="margin-bottom: 16px">
    <n-tabs type="line" animated>
      <n-tab-pane name="oasis" tab="Oasis">
        Wonderwall
      </n-tab-pane>
      <n-tab-pane name="the beatles" tab="the Beatles">
        Hey Jude
      </n-tab-pane>
      <n-tab-pane name="jay chou" tab="周杰伦">
        七里香
      </n-tab-pane>
    </n-tabs>
  </n-card>
</template>

<style scoped>

</style>