<template>
  <div class="card bg-dark">
    <div class="card-header">
      <h5>
        <span>Data Connections</span>
        <span class="clickable" style="float: right" v-on:click="addConnection()" title="New Connection">+</span>
      </h5>
    </div>
    <div class="card-body">
      <ul class="list-group">
        <li class="list-group-item bg-dark" v-for="connection in connectionInfo.connections" v-bind:key="connection.id">
          <span v-on:click="editConnection(connection.id)" class="clickable">{{ connection.name }}</span>
          <span v-on:click="deleteConnection(connection.id)" style="float: right" class="clickable"
            v-if="connectionInfo.canDelete.indexOf(connection.id) >= 0">-</span>
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
  name: 'ConnectionList',
  props: {
    connectionInfo: Object,
    workspace: Object
  },
  data () {
    return {}
  },
  methods: {
    addConnection: function () {
      let wkid = this.workspace.id
      router.push(`/connection/${wkid}/add`)
    },
    editConnection: function (id) {
      let wkid = this.workspace.id
      router.push(`/connection/${wkid}/${id}`)
    },
    deleteConnection: function (id) {
      (new WorkspaceApiHelper()).deleteConn4Workspace(this.workspace.id, id).then(rsp => {
        if (rsp.data.code === 0) {
          this.connectionInfo.connections = this.connectionInfo.connections.filter(function (value, index, arr) {
            return value.id !== id
          })
          this.$emit('onConnectionChanged', this.connectionInfo.connections)
          this.$toasted.show('Connection Deleted', {
            position: 'bottom-center',
            duration: 1000,
            type: 'success'
          })
        } else {
          this.$toasted.show('Connection Deletion Failed', {
            position: 'bottom-center',
            duration: 2000,
            type: 'error'
          })
        }
      }).catch(err => {
        this.$toasted.show(err.response.data.error.message, {
          position: 'bottom-center',
          duration: 2000,
          type: 'error'
        })
      })
    }
  }
}
</script>

<style scoped>
.clickable {
  cursor: pointer;
}
</style>
