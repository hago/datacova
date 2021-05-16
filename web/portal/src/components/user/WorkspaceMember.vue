<template>
  <div style="margin-left: 5px" v-title data-title="Member Management">
    <div>
      <h4>pick users to add them as
        <b><i>{{ type === 0 ? 'Administrator' : (type === 1 ? 'Maintainer' : 'Loader') }}</i></b> of workspace
        <b><i>{{ workspace.workspace.name }}</i></b>
      </h4>
    </div>
    <div>
      <button class="btn btn-primary" v-on:click="save()" v-if="!this.readonly">Save</button>
      <button class="btn btn-warning" v-on:click="cancel()">Cancel</button>
    </div>
    <div>
      <div class="leftpanel">
        <div class="form-group">
          <label for="search" class="col-form-label">Search User</label>
          <div class="form-row">
            <input class="form-control col-6" id="search" v-model="searchContent" v-on:keyup="userInput()" />
            <div class="verifyArea col-1">
              <img src="@/assets/gear-loading.gif" v-if="isLoading" />
            </div>
          </div>
          <ul class="list-group">
            <li class="list-group-item bg-dark" v-for="(user, index) in searchResult" v-bind:key="index"
            v-on:click="addUser(index)">
              <span class="clickable">{{ user.name }}  <i>({{ user.userId }})</i></span>
            </li>
          </ul>
        </div>
      </div>
      <div class="rightpanel">
        <p>Users selected</p>
        <ul class="list-group">
          <li class="list-group-item bg-dark" v-for="(user, index) in selected" v-bind:key="index"
          v-on:click="removeUser(index)">
            <span class="clickable">{{ user.name }}  <i>({{ user.userId }})</i></span>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script>
import Vue from 'vue'
import { BootstrapVue, IconsPlugin } from 'bootstrap-vue'
import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue/dist/bootstrap-vue.css'
import UserApiHelper from '@/apis/userapi.js'
import router from '../../router'
import WorkspaceApiHelper from '@/apis/workspace.js'

Vue.use(BootstrapVue)
Vue.use(IconsPlugin)

export default {
  name: 'WorkspaceMember',
  props: {
    loginStatus: Object,
    workspace: Object,
    type: String,
    id: String
  },
  data () {
    return {
      isLoading: false,
      searchResult: [],
      selected: [],
      timer: null,
      lastSearch: '',
      searchContent: '',
      errorMessage: '',
      readonly: true
    }
  },
  created: function () {
    this.$root.$emit('onNeedLogin', user => {
      this.readonly = (this.loginStatus.user.id !== this.workspace.owner.id) &&
        (this.workspace.users.find(it => this.loginStatus.user.id === it.user.id && it.roles.indexOf('0') >= 0) !== undefined)
    })
  },
  methods: {
    addUser: function (index) {
      let user = this.searchResult[index]
      if (this.selected.indexOf(user) < 0) {
        this.selected.push(user)
      }
    },
    removeUser: function (index) {
      this.selected.splice(index, 1)
    },
    search: function () {
      let w = this.searchContent.trim()
      if (w === '') {
        this.searchResult = []
        return
      }
      (new UserApiHelper()).searchUser(w).then(rsp => {
        this.searchResult = rsp.data.data
      }).catch(err => {
        this.errorMessage = err
      })
    },
    userInput: function () {
      if (this.lastSearch === this.searchContent.trim()) {
        return
      }
      this.lastSearch = this.searchContent.trim()
      clearTimeout(this.timer)
      this.timer = setTimeout(_ => {
        this.search()
      }, 500)
    },
    cancel: function () {
      if (confirm('Are you sure to cancel all edit?')) {
        router.back()
      }
    },
    save: function () {
      (new WorkspaceApiHelper()).addMember(
        this.workspace.workspace.id,
        this.type,
        this.selected.map(user => user.id))
        .then(rsp => {
          this.$toasted.show('Members Added', {
            position: 'bottom-center',
            duration: 1000,
            type: 'success'
          })
        }).catch(err => {
          this.errorMessage = err
        })
    }
  }
}
</script>

<style scoped>
.verifyArea img {
  width: 25px;
}
.leftpanel {
  float: left;
  width: 50%
}
.rightpanel {
  float: right;
  width: 50%
}
.clickable {
  cursor: pointer
}
</style>
