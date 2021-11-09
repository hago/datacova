<template>
  <div v-title data-title="Status of execution">
    <TaskExecutionCompleted v-bind:execution="execution" v-if="(execution !== null) && ([2, -1].indexOf(status) >= 0)"
      ></TaskExecutionCompleted>
    <TaskExecutionNotCompleted v-bind:execution="execution" v-if="(execution !== null) && ([0, 1].indexOf(status) >= 0)"
      ></TaskExecutionNotCompleted>
  </div>
</template>

<script>
import WorkspaceApiHelper from '@/apis/workspace.js'
import TaskExecutionCompleted from '@/components/execution/TaskExecutionCompleted.vue'
import TaskExecutionNotCompleted from '@/components/execution/TaskExecutionNotCompleted.vue'

export default {
  name: 'TaskExecution',
  components: {
    TaskExecutionCompleted,
    TaskExecutionNotCompleted
  },
  data: function () {
    return {
      id: this.$route.params.id,
      execution: null,
      status: NaN
    }
  },
  created: function () {
    let str = sessionStorage.getItem(`execution_${this.id}`)
    if (str !== null) {
      this.execution = JSON.parse(str)
      this.status = parseInt(this.execution.status)
    } else {
      (new WorkspaceApiHelper()).loadTaskExecution(this.id).then(rsp => {
        this.execution = rsp.data.data
        this.status = parseInt(this.execution.status)
      }).catch(err => {
        console.log(err)
      })
    }
  }
}
</script>

<style scoped>
</style>
