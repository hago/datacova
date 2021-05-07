<template>
  <div style="margin: 15px">
    <h2>
      <span>
        The execution with id {{ execution.id }} of task
        <a href="javascript:void(0);" v-on:click="gotoTask()">{{ execution.task.name }}</a> is
      </span>
      <span class="succeeded" v-if="succeeded"> succeeded</span>
      <span class="failed" v-if="!succeeded"> failed</span>
    </h2>
    <ul>
      <li v-for="(actiondetail, index) in detail.actionDetailMap" v-bind:key="index">
        <h4>
          <div class="row">
            <div class="col actiontitle">Action {{ index }} - {{ actiondetail.action.name }}: </div>
            <div class="col succeeded" v-if="actiondetail.error === null">Succeeded</div>
            <div class="col failed" v-if="actiondetail.error !== null">Failed</div>
          </div>
        </h4>
        <TaskExecutionIngest v-if="actiondetail.action.type === 1"
          v-bind:detail="detail"
          v-bind:actiondetail="actiondetail"
        ></TaskExecutionIngest>
      </li>
    </ul>
  </div>
</template>

<script>
import Router from '../../router'
import WorkspaceApiHelper from '@/apis/workspace.js'
import TaskExecutionIngest from '@/components/execution/TaskExecutionIngest.vue'

export default {
  name: 'TaskExecutionCompleted',
  components: {
    TaskExecutionIngest
  },
  props: {
    execution: Object
  },
  computed: {
    detail: function () {
      return this.execution.detail
    },
    succeeded: function () {
      return (this.execution.detail.dataLoadingError === null) &&
      (Object.values(this.execution.detail.actionDetailMap).filter(it => it.error === null).length === 0)
    }
  },
  data: function () {
    return {}
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
  },
  methods: {
    gotoTask: function () {
      Router.push({
        name: 'Task',
        params: {
          id: this.execution.task.id,
          workspaceId: this.execution.task.workspaceId
        }
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
.succeeded {
  color: green;
}
.failed {
  color: red;
}
.actiontitle {
  color: blue;
}
</style>
