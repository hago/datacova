<template>
  <div class="row" style="margin-left: 5px; margin-right: 5px">
    <div class="col-12">
      <h4>
        <b>Tasks</b>
        <span class="clickable" style="float: right" v-on:click="addTask()">+</span>
      </h4>
      <ul class="list-group">
        <li class="list-group-item bg-dark" v-for="task in tasks" v-bind:key="task.id">
          <span class="clickable" v-bind:title="task.description" v-on:click="editTask(task.id)">{{ task.name }}</span>
          <span class="clickable taskaction" style="float: right" v-on:click="deleteTask(task.id)"
            title="delete" v-if="workspace.users.Admin.indexOf(loginStatus.user.userId) >= 0">
            <img src="@/assets/remove.png" class="" />
          </span>
          <span class="clickable taskaction" style="float: right; margin-right: 5px" v-on:click="runTask(task.id)"
            title="execute">
            <img src="@/assets/execute.png" class="" />
            <input type="file" style="display:none"/>
          </span>
        </li>
      </ul>
    </div>
  </div>
</template>

<script>
import Vue from 'vue'
import Toasted from 'vue-toasted'
import router from '../../router'

import WorkspaceApiHelper from '@/apis/workspace.js'

Vue.use(Toasted)

export default {
  name: 'TaskList',
  props: {
    workspace: Object,
    loginStatus: Object
  },
  data () {
    return {
      tasks: []
    }
  },
  mounted: function () {
    this.loadTasks()
  },
  watch: {
    workspace: function () {
      this.loadTasks()
    }
  },
  methods: {
    loadTasks: function () {
      (new WorkspaceApiHelper()).getTasks(this.workspace.workspace.id).then(rsp => {
        this.tasks = rsp.data.data.tasks
      })
    },
    addTask: function () {
      router.push({
        name: 'Task',
        params: {
          'workspaceId': this.workspace.workspace.id,
          'id': 'add'
        }
      })
    },
    editTask: function (id) {
      router.push({
        name: 'Task',
        params: {
          'workspaceId': this.workspace.workspace.id,
          'id': id
        }
      })
    },
    deleteTask: function (id) {
      if (!confirm('Are you SURE to DELETE this task?')) {
        return
      }
      (new WorkspaceApiHelper()).deleteTask(this.workspace.workspace.id, id).then(rsp => {
        this.tasks = this.tasks.filter(task => task.id !== id)
      })
    },
    runTask: function (id) {
      router.push({
        name: 'TaskFileUpload',
        params: {
          workspaceId: this.workspace.workspace.id,
          id: id
        }
      })
    }
  }
}
</script>

<style scoped>
.taskaction img {
  width: 20px;
}
</style>
