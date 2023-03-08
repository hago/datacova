<script lang="ts">
import type { WorkspaceWithUser } from '@/api/workspaceapi';
import TaskList from '@/components/content/TaskList.vue';
import { EVENT_REMOTE_API_ERROR, EVENT_TASK_SELECTED } from '@/entities/events';
import type { Task } from '@/entities/task/task';
import { workspaceStore } from '@/stores/workspacestore';
import { eventBus } from '@/util/eventbus';
import { darkTheme } from 'naive-ui';
import { defineComponent, reactive, ref } from 'vue';
import TaskInfo from '@/components/content/TaskInfo.vue';
import EmptyTaskInfo from '@/components/content/EmptyTaskInfo.vue';

export default defineComponent({
  name: 'WorkspaceView',
  setup() {
    let wk: WorkspaceWithUser | null = null;
    return reactive({
      darkTheme,
      workspace: ref<WorkspaceWithUser | null>(wk),
      tasks: [] as Task[],
      selectedTask: null as Task | null
    });
  },
  mounted() {
    console.log(`mounted WorkspaceView`)
    eventBus.register(EVENT_TASK_SELECTED, this.onTaskSelected)
    this.loadWorkspace()
  },
  unmounted() {
    eventBus.unregister(EVENT_TASK_SELECTED, this.onTaskSelected)
  },
  updated() {
    console.log('updated WorkspaceView')
    this.loadWorkspace()
  },
  methods: {
    async onTaskSelected(t: Task): Promise<any> {
      this.selectedTask = t
      return Promise.resolve()
    },
    loadWorkspace() {
      let workspaceId = this.$route.params.id
      console.log(`WorkspaceView.id: ${workspaceId}`)
      if (typeof workspaceId === 'string') {
        this.workspace = workspaceStore().getWorkspace(parseInt(workspaceId))
        console.log(`WorkspaceView.workspace: ${this.workspace}`)
      } else {
        eventBus.send(EVENT_REMOTE_API_ERROR, `workspace id "${workspaceId}" is not a value`)
      }
    },
    selectTask(id: number) {
      let found = this.tasks.find(t => t.id === id)
      this.selectedTask = found as Task
    }
  },
  components: { TaskList, EmptyTaskInfo, TaskInfo }
})
</script>

<template>
  <n-config-provider :theme="darkTheme">
    <n-card v-if="workspace !== null" :title="workspace === null ? '' : workspace.workspace.name"
      style="margin-bottom: 16px">
      <n-tabs type="bar" animated v-if="workspace !== null">
        <n-tab-pane name="Tasks" tab="tasks">
          <n-grid cols="5">
            <n-gi span="2">
              <TaskList :workspace="workspace"></TaskList>
            </n-gi>
            <n-gi span="3">
              <TaskInfo v-if="selectedTask !== null" :task="selectedTask"></TaskInfo>
              <EmptyTaskInfo v-if="selectedTask === null"></EmptyTaskInfo>
            </n-gi>
          </n-grid>
        </n-tab-pane>
        <n-tab-pane name="Connections" tab="connections">
          <ConnectionView :workspace="workspace"></ConnectionView>
        </n-tab-pane>
        <n-tab-pane name="Execution Log" tab="executes">
          Executions
        </n-tab-pane>
      </n-tabs>
    </n-card>
  </n-config-provider>
</template>

<style scoped></style>