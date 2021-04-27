<template>
  <div>
  </div>
</template>

<script>
import WorkspaceApiHelper from '@/apis/workspace.js'

export default {
  name: 'TaskExecution',
  data: function () {
    return {
      id: this.$route.params.id,
      execution: null
    }
  },
  created: function () {
    let str = sessionStorage.getItem(`execution_${this.id}`)
    if (str !== null) {
      this.execution = JSON.parse(str)
    } else {
      (new WorkspaceApiHelper()).loadTaskExecution(this.id).then(rsp => {
        this.execution = rsp.data.data
      }).catch(err => {
        console.log(err)
      })
    }
  }
}
</script>

<style scoped>
.icon {
  width: 20px
}
.head {
  font-weight: bold;
}
</style>
