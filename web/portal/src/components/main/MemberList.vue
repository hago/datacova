<template>
  <div>
    <div class="card bg-dark" v-if="isAdmin">
      <div class="card-header">
        <h5>
          <span>Administrators</span>
          <span class="clickable" style="float: right" v-on:click="addMember('0')" title="Add Administrator">+</span>
        </h5>
      </div>
      <ul class="list-group">
        <li class="list-group-item bg-dark">
          <span class="clickable"><i>{{ workspace.owner.name }}</i></span>
        </li>
        <li class="list-group-item bg-dark" v-for="(user, index) in admins" v-bind:key="index">
          <span class="clickable"><i>{{ user.user.name }}</i></span>
          <span v-on:click="removeMember(user.user, '0')" style="float: right" class="clickable"
            v-if="(workspace.owner.id === loginStatus.user.userId) || isAdmin" v-bind:title="`Remove ${user.user.name}`">-</span>
        </li>
      </ul>
    </div>
    <div class="card bg-dark" v-if="isAdmin">
      <div class="card-header">
        <h5>
          <span>Maintainers</span>
          <span class="clickable" style="float: right" v-on:click="addMember('1')" title="Add Maintainer">+</span>
        </h5>
      </div>
      <ul class="list-group">
        <li class="list-group-item bg-dark" v-for="(user, index) in maintainers" v-bind:key="index">
          <span class="clickable"><i>{{ user.user.name }}</i></span>
          <span v-on:click="removeMember(user.user, '1')" style="float: right" class="clickable"
            v-if="(workspace.owner.id === loginStatus.user.userId) || isAdmin" v-bind:title="`Remove ${user.user.name}`">-</span>
        </li>
      </ul>
    </div>
    <div class="card bg-dark" v-if="isAdmin">
      <div class="card-header">
        <h5>
          <span>Data Loaders</span>
          <span class="clickable" style="float: right" v-on:click="addMember('2')" title="Add Data Loader">+</span>
        </h5>
      </div>
      <ul class="list-group">
        <li class="list-group-item bg-dark" v-for="(user, index) in loaders" v-bind:key="index">
          <span class="clickable"><i>{{ user.user.name }}</i></span>
          <span v-on:click="removeMember(user.user, '2')" style="float: right" class="clickable"
            v-if="(workspace.owner.id === loginStatus.user.userId) || isAdmin" v-bind:title="`Remove ${user.user.name}`">-</span>
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
  name: 'MemberList',
  props: {
    workspace: Object,
    loginStatus: Object
  },
  data () {
    return {}
  },
  computed: {
    isAdmin: function () {
      return this.workspace.users.find(item => item.user.id === this.loginStatus.user.id).roles.indexOf('0') >= 0
    },
    admins: function () {
      return this.workspace.users.filter(item => item.roles.indexOf('0') >= 0 && item.user.id !== this.loginStatus.user.id)
    },
    maintainers: function () {
      return this.workspace.users.filter(item => item.roles.indexOf('1') >= 0)
    },
    loaders: function () {
      return this.workspace.users.filter(item => item.roles.indexOf('2') >= 0)
    }
  },
  methods: {
    addMember: function (type) {
      router.push({
        name: 'WorkspaceMember',
        params: {workspaceId: this.workspace.workspace.id, type: type, id: 'add', workspace: this.workspace}
      })
    },
    removeMember: function (user, type) {
      if (user.id === this.workspace.owner.id) {
        this.$toasted.show(`The owner ${user} CAN NOT be removed`, {
          position: 'bottom-center',
          duration: 2000,
          type: 'error'
        })
        return
      }
      if (!confirm(`Are you sure to DELETE "${user.name}"?`)) {
        return
      }
      (new WorkspaceApiHelper()).removeMember(this.workspace.workspace.id, user.id, type).then(rsp => {
        if (rsp.data.code === 0) {
          let cloneUsers = Object.assign(this.workspace.users)
          let i = cloneUsers.findIndex(u => u.user.id === user.id)
          if (i !== -1) {
            cloneUsers[i].roles = cloneUsers[i].roles.filter(r => r !== type)
            if (cloneUsers[i].roles.length === 0) {
              cloneUsers.splice(i, 1)
            }
          }
          Vue.set(this.workspace, 'users', cloneUsers)
          this.$toasted.show(`User ${user.name} removed successfully`, {
            position: 'bottom-center',
            duration: 1000,
            type: 'success'
          })
        } else {
          this.$toasted.show(`user ${user.name} removing failed`, {
            position: 'bottom-center',
            duration: 2000,
            type: 'error'
          })
        }
      }).catch(err => {
        this.$toasted.show(err, {
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
