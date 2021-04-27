<template>
  <div style="border: 2px solid">
    <div class="container-fluid">
      <h5>Task Execution Status</h5>
      <div class="form-row head" style="margin-bottom: 15px">
        <div class="col-4">Name of Task</div>
        <div class="col-3">Started At</div>
        <div class="col-3">Submitted By</div>
        <div class="col-2" style="text-align: right">Status</div>
      </div>
      <div class="form-row" v-if="executions.length === 0">No Task Executions</div>
      <div class="form-row" v-for="exe in executions" v-bind:key="exe.id">
        <div class="col-4 clickable" v-on:click="showDetail(exe)">{{ exe.task.name }}</div>
        <div class="col-4">{{ fmtTime(exe.addTime) }}</div>
        <div class="col-3">{{ exe.addByUser.name }}</div>
        <div class="col-1" style="float: right">
          <img src="@/assets/oops.png" class="icon" v-if="exe.status == '-1'" title="Failed" />
          <img src="@/assets/queuing.png" class="icon" v-if="exe.status == '0'" title="Queueing" />
          <img src="@/assets/processing.png" class="icon" v-if="exe.status == '1'" title="Processing" />
          <img src="@/assets/success.png" class="icon" v-if="exe.status == '2'" title="Succeeded" />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Vue from 'vue'
import Router from '../../router'
import WorkspaceApiHelper from '@/apis/workspace.js'
import UserApiHelper from '@/apis/userapi.js'
import Toasted from 'vue-toasted'
const dateFormat = require('dateformat')

Vue.use(Toasted)

export default {
  name: 'TaskExecutionList',
  props: {
    workspace: Object,
    loginStatus: Object
  },
  data () {
    return {
      executions: []
    }
  },
  mounted: function () {
    this.loadExecutions(this.workspace.workspace.id)
  },
  watch: {
    workspace: function (newWorkspace) {
      this.loadExecutions(newWorkspace.workspace.id)
    }
  },
  methods: {
    loadExecutions: function (workspaceId) {
      (new WorkspaceApiHelper()).loadTaskExecutions(workspaceId).then(rsp => {
        let executionList = rsp.data.data.executions;
        (new UserApiHelper()).batchGetUser(executionList.map(e => e.addBy)).then(rsp => {
          let users = rsp.data.data
          this.executions = executionList.map(e => {
            e.addByUser = users.find(u => u.id === e.addBy)
            return e
          })
        })
      }).catch(err => {
        this.$toasted.show(err.response.data.error.message, {
          position: 'bottom-center',
          duration: 1000,
          type: 'error'
        })
      })
    },
    getStatus: function (xstatus) {
      switch (xstatus) {
        case '0':
          return 'Queuing'
        case '1':
          return 'Running'
        case '2':
          return 'Succeeded'
        case '-1':
          return 'Failed'
        default:
          return xstatus
      }
    },
    fmtTime: function (timeStamp) {
      return dateFormat(new Date(timeStamp), 'yyyy/mm/dd HH:MM:ss')
    },
    showDetail: function (execution) {
      sessionStorage.setItem(`execution_${execution.id}`, JSON.stringify(execution))
      Router.push(`/execution/${execution.id}`)
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
