<template>
  <div class="container-fluid">
    <div class="form-row" style="margin-bottom:10px">
        <div class="col form-inline">
          <h4><b>My Workspaces</b></h4>
          <select class="wklist custom-select" v-model="workspaceIndex" v-on:change="switchWorkspace()">
            <option v-if="workspaces.length === 0" value="-1">No Workspace</option>
            <option v-for="(workspace, index) in workspaces" v-bind:value="index" v-bind:key="index">
              {{ workspace.name }}
            </option>
          </select>
        </div>
        <div class="col text-right">
          <button class="btn btn-primary" v-if="!edit_workspace.show" v-on:click="showWorkspace()">Create Workspace</button>
        </div>
    </div>
    <div class="leftpanel">
      <div v-if="edit_workspace.show" class="showwk">
        <div class="form-row"><h2>Workspace <i>{{ edit_workspace.name }}</i></h2></div>
        <div class="form-group">
          <label for="showwkname">Name</label>
          <input type="text" class="form-control" id="showwkname" v-model="edit_workspace.name" />
        </div>
        <div class="form-group">
          <label for="showwkdesp">Description</label>
          <textarea class="form-control" id="showwkdesp" v-model="edit_workspace.description"></textarea>
        </div>
        <button class="btn btn-primary" v-on:click="submitWorkspace()">Save</button>
        <button class="btn btn-secondary" v-on:click="edit_workspace.show=false">Cancel</button>
      </div>
      <div v-if="(workspaces.length > 0) && !edit_workspace.show" style="margin:5px 0px 0px 0px">
        <div class="card bg-dark">
          <h4 class="card-title">{{ workspaces[workspaceIndex].name }}</h4>
          <h6 class="card-subtitle">Owner: {{ workspaces[workspaceIndex].ownerId }}</h6>
          <h6 class="card-subtitle">My Role: {{ myRoles.join(', ') }}</h6>
          <p class="card-body">{{ workspaces[workspaceIndex].description }}</p>
        </div>
        <div v-if="workspaces[workspaceIndex].ownerId === loginStatus.user.userId" style="margin-top:5px">
          <button class="btn btn-info" v-on:click="showWorkspace(false)">Edit</button>
          <button class="btn btn-warning">Transfer</button>
        </div>
      </div>
      <MemberList v-if="workspaceIndex >= 0"
        v-bind:workspace="workspaces[workspaceIndex]"
        v-bind:loginStatus="loginStatus"
        ></MemberList>
    </div>
    <div class="rightpanel">
      <b-tabs content-class="mt-3" active>
        <b-tab title="Tasks">
          <TaskList v-if="workspaceIndex >= 0"
            v-bind:workspace="workspaces[workspaceIndex]"
            v-bind:loginStatus="loginStatus"
            ></TaskList>
        </b-tab>
        <b-tab title="Connections">
          <ConnectionList v-if="(workspaceIndex >= 0) || (myRoles.filter(it => it !== 'Loader').length > 0)"
            v-bind:workspace="workspaces[workspaceIndex]"
            v-bind:connectionInfo="conninfo"
            v-bind:onConnectionChanged="onConnectionChanged"
            ></ConnectionList>
        </b-tab>
        <b-tab title="Verification Rules">
          Rules here
        </b-tab>
      </b-tabs>
    </div>
    <div class="centerpanel">
      <TaskExecutionList v-if="workspaceIndex >= 0"
        v-bind:workspace="workspaces[workspaceIndex]"
        v-bind:loginStatus="loginStatus"
        ></TaskExecutionList>
    </div>
  </div>
</template>

<script>
import Vue from 'vue'
import 'bootstrap/dist/css/bootstrap.css'

import WorkspaceApiHelper from '@/apis/workspace.js'

import ConnectionList from './main/ConnectionList.vue'
import MemberList from './main/MemberList.vue'
import TaskList from './main/TaskList.vue'
import TaskExecutionList from '@/components/execution/TaskExecutionList.vue'

export default {
  name: 'Main',
  props: {
    loginStatus: Object
  },
  components: {
    ConnectionList,
    MemberList,
    TaskList,
    TaskExecutionList
  },
  data () {
    return {
      workspaces: [],
      workspaceIndex: -1,
      edit_workspace: {
        show: false,
        isnew: false,
        name: '',
        description: ''
      },
      conninfo: {
        connections: []
      }
    }
  },
  mounted: function () {
    console.log(this.loginStatus)
    this.loadWorkspaces()
  },
  computed: {
    myRoles: function () {
      if (this.workspaceIndex < 0) {
        return []
      }
      let wkug = this.workspaces[this.workspaceIndex].users
      return [wkug.Admin.indexOf(this.loginStatus.user.userId) >= 0 ? 'Administrator' : null,
        wkug.Maintainer.indexOf(this.loginStatus.user.userId) >= 0 ? 'Maintainer' : null,
        wkug.Loader.indexOf(this.loginStatus.user.userId) >= 0 ? 'Data Loader' : null]
        .filter(it => it !== null)
    }
  },
  watch: {
    loginStatus: function () {
      this.loadWorkspaces()
    }
  },
  methods: {
    loadWorkspaces: function () {
      if (this.loginStatus.user !== undefined) {
        (new WorkspaceApiHelper()).getMyWorkspaces().then(rsp => {
          console.log(rsp)
          this.workspaces = rsp.data.data
          this.workspaceIndex = this.workspaces.length > 0 ? 0 : -1
          this.loadConnections()
        })
      }
    },
    switchWorkspace: function () {
      this.loadConnections()
    },
    onConnectionChanged: function (connections) {
      Vue.set(this.conninfo, 'connections', connections)
    },
    loadConnections: function () {
      let wkid = this.workspaces[this.workspaceIndex].id;
      (new WorkspaceApiHelper()).getConnections(wkid).then(rsp => {
        this.conninfo = rsp.data.data
      })
    },
    showWorkspace: function (isNew = true) {
      this.edit_workspace.show = true
      this.edit_workspace.isnew = isNew
      this.edit_workspace.description = isNew ? '' : this.workspaces[this.workspaceIndex].description
      this.edit_workspace.name = isNew ? '' : this.workspaces[this.workspaceIndex].name
    },
    submitWorkspace: function () {
      let wsapi = new WorkspaceApiHelper()
      let call = (this.edit_workspace.isnew ? wsapi.newWorkspace({
        name: this.edit_workspace.name,
        description: this.edit_workspace.description,
        ownerId: this.loginStatus.user.cn,
        users: {}
      })
        : wsapi.updateWorkspace({
          id: this.workspaces[this.workspaceIndex].id,
          name: this.edit_workspace.name,
          description: this.edit_workspace.description,
          ownerId: this.loginStatus.user.userId,
          users: null
        }))
      call.then(wks => {
        console.log(wks)
        this.edit_workspace.show = false
        this.loadWorkspaces()
      })
    }
  }
}
</script>
<style>
.leftpanel {
  float: left;
  width: 20%
}
.centerpanel {
  float: right;
  width: 40%
}
.rightpanel {
  float: right;
  width: 40%
}
.wklist {
  margin-left: 5px;
}
.showwk {
  margin: 8px
}
.clickable {
  cursor: pointer;
}
</style>