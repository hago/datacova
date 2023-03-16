<script lang="ts">
import taskApiHelper from '@/api/taskapi';
import { defineComponent, reactive, ref, type PropType } from 'vue';
import type { WorkspaceWithUser } from '@/api/workspaceapi'
import { newEmptyTask, type Task } from '@/entities/task/task';
import { identityStore } from '@/stores/identitystore';
import TaskInfo from '@/components/content/TaskInfo.vue';
import EmptyTaskInfo from '@/components/content/EmptyTaskInfo.vue';
import { EVENT_REMOTE_API_ERROR, EVENT_TASK_SELECTED } from '@/entities/events';
import { eventBus } from '@/util/eventbus';

export default defineComponent({
  props: {
    workspace: {
      type: Object as PropType<WorkspaceWithUser>,
      required: true
    }
  },
  setup(props) {
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
        let user = identityStore().currentIdentity()
        taskApiHelper.getTasksOfWorkspace(user, this.workspace.workspace.id).then(tr => {
          this.tasks = tr.data.tasks
        }).catch(reason => {
          eventBus.send(EVENT_REMOTE_API_ERROR, reason)
        })
      }
    },
    selectTask(id: number) {
      let found = this.tasks.find(t => t.id === id)
      this.selectedTask = found as Task
      eventBus.send(EVENT_TASK_SELECTED, found)
    },
    newTask() {
      console.log('newTask')
      const task: Task = newEmptyTask(this.workspace!.workspace.id)
      this.tasks = [task].concat(this.tasks)
      this.selectTask(task.id)
    }
  },
  components: {
    TaskInfo,
    EmptyTaskInfo
  }
}) 
</script>

<template>
  <n-button type="primary" @click="newTask">New Task</n-button>
  <n-list style="width: 90%">
    <n-space v-for="task in tasks" v-bind:key="task.id"
      :class="(selectedTask !== null) && (task.id === selectedTask.id) ? 'taskitem selectedtaskitem' : 'taskitem'">
      <div @click="selectTask(task.id)" :title="task.description">{{ task.name }}</div>
    </n-space>
  </n-list>
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

.selectedtaskitem {
  color: aqua;
  background-color: green;
}
</style>
