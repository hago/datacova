<script lang="ts">
import type { WorkspaceWithUser } from '@/api/workspaceapi';
import TaskList from '@/components/content/TaskList.vue';
import { workspaceStore } from '@/stores/workspacestore';
import { defineComponent, reactive, ref } from 'vue';

export default defineComponent({
  name: 'WorkspaceView',
  setup() {
    let wk: WorkspaceWithUser | null = null;
    return reactive({
      workspace: ref<WorkspaceWithUser | null>(wk)
    });
  },
  mounted() {
    console.log(`mounted WorkspaceView`)
    this.loadWorkspace()
  },
  updated() {
    console.log('updated WorkspaceView')
    this.loadWorkspace()
  },
  methods: {
    loadWorkspace() {
      let workspaceId = this.$route.params.id
      console.log(`WorkspaceView.id: ${workspaceId}`)
      if (typeof workspaceId === 'string') {
        this.workspace = workspaceStore().getWorkspace(parseInt(workspaceId))
        console.log(`WorkspaceView.workspace: ${this.workspace}`)
      } else {
        this.$emit('errorOccurs', `workspace id "${workspaceId}" is not a value`)
      }
    }
  },
  components: { TaskList }
})
</script>

<template>
  <n-card v-if="workspace !== null" :title="workspace === null ? '' : workspace.workspace.name"
    style="margin-bottom: 16px">
    <n-tabs type="bar" animated>
      <n-tab-pane name="Tasks" tab="tasks">
        <TaskList :workspace="workspace"></TaskList>
      </n-tab-pane>
      <n-tab-pane name="Connections" tab="connections">
        Hey Jude
      </n-tab-pane>
      <n-tab-pane name="Execution Log" tab="executes">
        七里香
      </n-tab-pane>
    </n-tabs>
  </n-card>
</template>

<style scoped>

</style>