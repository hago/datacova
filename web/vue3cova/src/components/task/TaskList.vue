<script lang="ts">
import taskApiHelper from '@/api/taskapi';
import { defineComponent, reactive, ref, type PropType } from 'vue';
import type { WorkspaceWithUser } from '@/api/workspaceapi'
import { newEmptyTask, type Task } from '@/entities/task/task';
import { identityStore } from '@/stores/identitystore';
import TaskInfo from '@/components/task/TaskInfo.vue';
import EmptyTaskInfo from '@/components/task/EmptyTaskInfo.vue';
import { EVENT_REMOTE_API_ERROR, EVENT_TASK_DELETED, EVENT_TASK_SELECTED } from '@/entities/events';
import { eventBus } from '@/util/eventbus';
import router from '@/router';

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
    eventBus.register(EVENT_TASK_DELETED, this.taskDeleted)
    this.loadTasks()
  },
  unmounted() {
    eventBus.unregister(EVENT_TASK_DELETED, this.taskDeleted)
  },
  updated() {
    this.loadTasks()
  },
  methods: {
    async taskDeleted(task: Task): Promise<any> {
      this.tasks = this.tasks.filter(item => item.id !== task.id)
      if ((this.selectedTask !== null) && (this.selectedTask.id === task.id)) {
        if (this.tasks.length > 0) {
          this.selectTask(this.tasks[0].id)
        } else {
          this.selectedTask = null
        }
      }
      eventBus.send(EVENT_TASK_SELECTED, this.selectedTask)
      return Promise.resolve()
    },
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
    },
    execute(task: Task) {
      router.push(`/task/run/${task.workspaceId}/${task.id}`)
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
    <n-list-item v-for="task in tasks" v-bind:key="task.id"
      :class="(selectedTask !== null) && (task.id === selectedTask.id) ? 'taskitem selectedtaskitem' : 'taskitem'">
      <n-tag :bordered="false" :title="task.description" @click="selectTask(task.id)">{{ task.name }}</n-tag>
      <n-button style="float: right;" @click="execute(task)">Execute</n-button>
    </n-list-item>
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
