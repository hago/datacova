<script lang="ts">
import taskApiHelper from '@/api/taskapi';
import { defineComponent, reactive, ref, type PropType } from 'vue';
import type { WorkspaceWithUser } from '@/api/workspaceapi'
import type { Task } from '@/entities/task/task';
import { currentIdentity, identityStore } from '@/stores/identitystore';

export default defineComponent({
  props: {
    workspace: {
      type: Object as PropType<WorkspaceWithUser>,
      required: true
    }
  },
  setup() {
    return reactive({
      tasks: [] as Task[]
    })
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
    }
  }
}) 
</script>

<template>
  <div>Task</div>
</template>

<style scoped>

</style>
