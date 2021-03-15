<template>
  <div>
    <div class="card bg-dark" v-if="isAdmin">
      <div class="card-header">
        <h5>
          <span>Administrators</span>
          <span class="clickable" style="float: right" v-on:click="addMember(0)" title="Add Administrator">+</span>
        </h5>
      </div>
      <ul class="list-group">
        <li class="list-group-item bg-dark">
          <span class="clickable"><i>{{ workspace.owner.name }}</i></span>
        </li>
        <li class="list-group-item bg-dark" v-for="(user, index) in workspace.users.Admin" v-bind:key="index">
          <span class="clickable"><i>{{ user }}</i></span>
          <span v-on:click="removeMember(user, 0)" style="float: right" class="clickable"
            v-if="(workspace.ownerId === loginStatus.user.userId) || isAdmin" v-bind:title="`Remove ${user}`">-</span>
        </li>
      </ul>
    </div>
    <div class="card bg-dark" v-if="isAdmin">
      <div class="card-header">
        <h5>
          <span>Maintainers</span>
          <span class="clickable" style="float: right" v-on:click="addMember(1)" title="Add Maintainer">+</span>
        </h5>
      </div>
      <ul class="list-group">
        <li class="list-group-item bg-dark" v-for="(user, index) in workspace.users.Maintainer" v-bind:key="index">
          <span class="clickable"><i>{{ user }}</i></span>
          <span v-on:click="removeMember(user, 1)" style="float: right" class="clickable"
            v-if="(workspace.ownerId === loginStatus.user.userId) || isAdmin" v-bind:title="`Remove ${user}`">-</span>
        </li>
      </ul>
    </div>
    <div class="card bg-dark" v-if="isAdmin">
      <div class="card-header">
        <h5>
          <span>Data Loaders</span>
          <span class="clickable" style="float: right" v-on:click="addMember(2)" title="Add Data Loader">+</span>
        </h5>
      </div>
      <ul class="list-group">
        <li class="list-group-item bg-dark" v-for="(user, index) in workspace.users.Loader" v-bind:key="index">
          <span class="clickable"><i>{{ user }}</i></span>
          <span v-on:click="removeMember(user, 2)" style="float: right" class="clickable"
            v-if="(workspace.ownerId === loginStatus.user.userId) || isAdmin" v-bind:title="`Remove ${user}`">-</span>
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
    }
  },
  methods: {
    addMember: function (type) {
      router.push({
        name: 'WorkspaceMember',
        params: {workspaceId: this.workspace.id, type: type, id: 'add', workspace: this.workspace}
      })
    },
    removeMember: function (user, type) {
      if (user === this.workspace.ownerId) {
        this.$toasted.show(`The owner ${user} CAN NOT be removed`, {
          position: 'bottom-center',
          duration: 2000,
          type: 'error'
        })
        return
      }
      if (!confirm(`Are you sure to DELETE "${user}"?`)) {
        return
      }
      (new WorkspaceApiHelper()).removeMember(this.workspace.id, user).then(rsp => {
        if (rsp.data.code === 0) {
          let wk = Object.assign(this.workspace)
          let arr = (type === 0 ? wk.users.Admin : (type === 1 ? wk.users.Maintainer : wk.users.Loader))
            .filter((value, index, arr) => {
              return value !== user
            })
          console.log(arr)
          switch (type) {
            case 0:
              wk.users.Admin = arr
              break
            case 1:
              wk.users.Maintainer = arr
              break
            case 2:
              wk.users.Loader = arr
              break
          }
          this.workspace = wk
          this.$toasted.show(`User ${user} removed successfully`, {
            position: 'bottom-center',
            duration: 1000,
            type: 'success'
          })
        } else {
          this.$toasted.show(`user ${user} removing failed`, {
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
