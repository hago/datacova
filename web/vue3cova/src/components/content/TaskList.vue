<script lang="ts">
import taskApiHelper from '@/api/taskapi';
import { defineComponent, reactive, ref, type PropType } from 'vue';
import type { WorkspaceWithUser } from '@/api/workspaceapi'
import type { Task } from '@/entities/task/task';
import { currentIdentity, identityStore } from '@/stores/identitystore';
import TaskInfo from '@/components/content/TaskInfo.vue';
import EmptyTaskInfo from '@/components/content/EmptyTaskInfo.vue';

export default defineComponent({
  props: {
    workspace: {
      type: Object as PropType<WorkspaceWithUser>,
      required: true
    }
  },
  setup() {
    return reactive({
      tasks: [] as Task[],
      selectedTask: ref<Task | null>(null)
    })
  },
  mounted() {
    this.loadTasks()
  },
  updated() {
    this.loadTasks()
  },
  methods: {
    loadTasks() {
      if (this.workspace !== null) {
        let user = currentIdentity(identityStore())
        taskApiHelper.getTasksOfWorkspace(user, this.workspace.workspace.id, {
          success: (rsp) => {
            if (rsp.code !== 0) {
              // error
            } else {
              this.tasks = rsp.data.tasks
            }
          },
          fail: () => { }
        })
      }
    },
    selectTask(id: number) {
      let found = this.tasks.find(t => t.id === id)
      this.selectedTask = found as Task
    }
  },
  components: {
    TaskInfo,
    EmptyTaskInfo
  }
}) 
</script>

<template>
  <n-grid cols="5">
    <n-gi span="2">
      <n-list style="width: 90%">
        <n-space v-for="task in tasks" v-bind:key="task.id" class="taskitem">
          <div @click="selectTask(task.id)" :title="task.description">{{ task.name }}</div>
        </n-space>
      </n-list>
    </n-gi>
    <n-gi span="3">
      <TaskInfo v-if="selectedTask !== null" :task="selectedTask"></TaskInfo>
      <EmptyTaskInfo v-if="selectedTask === null" :task="selectedTask"></EmptyTaskInfo>
    </n-gi>
</n-grid>
</template>

<style scoped>
.taskitem {
  font-size: large;
  border-top: 0px;
  border-left: 0px;
  border-right: 0px;
  border-bottom: 1px;
  border-style: solid;
  margin-top: 3px;
}

.taskitem:hover {
  cursor: pointer;
  color: aqua;
}
</style>
