<template>
  <div style="margin-left: 5px" v-title data-title="Member Management">
    <div>
      <h4>pick users to add them as
        <b><i>{{ rolename }}</i></b> of workspace
        <b><i>{{ workspace.workspace.name }}</i></b>
      </h4>
    </div>
    <div>
      <button class="btn btn-primary" v-on:click="save()" v-if="!this.readonly">Save</button>
      <button class="btn btn-warning" v-on:click="cancel()">Cancel</button>
    </div>
    <div class="row" v-if="userproviders.length > 0">
      <div class="col-2" v-for="(provider, index) in userproviders" v-bind:key="index">
        <div class="form-check">
          <input class="form-check-input" type="checkbox" checked :id="'provider' + provider.providerType"
            v-on:change="toggleProvider(provider.providerType)" />
          <label class="form-check-label" :for="'provider' + provider.providerType">
            {{ provider.name }}
          </label>
        </div>
      </div>
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
          <div class="form-row" v-if="searchResult.foundInDatabase.length>0">
            <div>
              <div>Found existed users:</div>
              <ul class="list-group">
                <li class="list-group-item bg-dark" v-for="(user, index) in searchResult.foundInDatabase" v-bind:key="index"
                v-on:click="addUser(user)">
                  <span class="clickable">{{ user.name }}  <i>({{ user.userId }})</i></span>
                </li>
              </ul>
            </div>
          </div>
          <div class="form-row" v-if="Object.keys(searchResult.foundInProviders).length>0">
            <div v-for="(users, providerType) in searchResult.foundInProviders" v-bind:key="providerType">
              <div>Found users from <i>{{ findprovider(parseInt(providerType)).name }}</i>:</div>
              <ul class="list-group">
                <li class="list-group-item bg-dark" v-for="(user, index) in users" v-bind:key="index"
                    v-on:click="addUser(user)">
                  <span class="clickable">{{ user.name }}  <i>({{ user.userId }})</i></span>
                </li>
              </ul>
            </div>
          </div>
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
import SiteSetting from '@/apis/sitesetting.js'

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
      searchResult: {
        foundInDatabase: [],
        foundInProviders: []
      },
      selected: [],
      timer: null,
      lastSearch: '',
      searchContent: '',
      errorMessage: '',
      readonly: true,
      userproviders: [],
      selectedproviders: []
    }
  },
  computed: {
    rolename: function () {
      switch (parseInt(this.type)) {
        case 0:
          return 'Administrator'
        case 1:
          return 'Maintainer'
        case 2:
          return 'Loader'
        default:
          return `unknown role ${this.type}`
      }
    }
  },
  created: function () {
    this.readonly = (this.loginStatus.user.id !== this.workspace.owner.id) &&
      (this.workspace.users.find(it => this.loginStatus.user.id === it.user.id && it.roles.indexOf('0') >= 0) !== undefined)
    new SiteSetting().getSettings().then(settings => {
      console.log(settings.userProviders)
      Vue.set(this, 'userproviders', settings.userProviders.filter(provider => provider.providerType !== 0))
      Vue.set(this, 'selectedproviders', settings.userProviders.filter(provider => provider.providerType !== 0)
        .map(provider => provider.providerType))
    })
  },
  methods: {
    findprovider: function (providertype) {
      return this.userproviders.find(p => p.providerType === providertype)
    },
    addUser: function (user) {
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
      this.isLoading = true;
      (new UserApiHelper()).searchUser(w, this.selectedproviders).then(rsp => {
        this.isLoading = false
        this.searchResult = rsp.data.data
      }).catch(err => {
        this.isLoading = false
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
        this.selected)
        .then(rsp => {
          this.$toasted.show('Members Added', {
            position: 'bottom-center',
            duration: 1000,
            type: 'success'
          })
        }).catch(err => {
          this.errorMessage = err
        })
    },
    toggleProvider: function (providerType) {
      let i = this.selectedproviders.indexOf(providerType)
      if (i >= 0) {
        this.selectedproviders.splice(i, 1)
      } else {
        this.selectedproviders.push(providerType)
      }
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
