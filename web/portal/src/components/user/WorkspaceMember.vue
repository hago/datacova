<template>
  <div style="margin-left: 5px">
    <div>
      <h4>pick users to add them as
        <b><i>{{ type === 0 ? 'Administrator' : (type === 1 ? 'Maintainer' : 'Loader') }}</i></b> of workspace
        <b><i>{{ workspace.name }}</i></b>
      </h4>
    </div>
    <div>
      <button class="btn btn-primary" v-on:click="save()" v-if="!this.readonly">Save</button>
      <button class="btn btn-warning" v-on:click="cancel()">Cancel</button>
    </div>
    <div>
      <b-tabs content-class="mt-3">
        <b-tab title="Internal" active>
          <div class="leftpanel">
            <div class="form-group">
              <label for="searchInternal" class="col-form-label">Search Internal User</label>
              <div class="form-row">
                <input class="form-control col-6" id="searchInternal" v-model="searchContent" v-on:keyup="userInputInternal()" />
                <div class="verifyArea col-1">
                  <img src="@/assets/gear-loading.gif" v-if="isLoading" />
                </div>
              </div>
              <ul class="list-group">
                <li class="list-group-item bg-dark" v-for="(user, index) in searchResult" v-bind:key="index"
                v-on:click="addUser(index)">
                  <span class="clickable">{{ user.fullName }}  <i>({{ user.userId }})</i></span>
                </li>
              </ul>
            </div>
          </div>
          <div class="rightpanel">
            <p>Internal Users selected</p>
            <ul class="list-group">
              <li class="list-group-item bg-dark" v-for="(user, index) in selected" v-bind:key="index"
              v-on:click="removeUser(index)">
                <span class="clickable">{{ user.fullName }}  <i>({{ user.userId }})</i></span>
              </li>
            </ul>
          </div>
        </b-tab>
        <b-tab title="External">
          <div class="leftpanel">
            <div class="form-group">
              <label for="searchExternal" class="col-form-label">Search External User</label>
              <div class="form-row">
                <input class="form-control col-6" id="searchExternal" v-model="externalSearchContent" v-on:keyup="userInputExternal()" />
                <div class="verifyArea col-1">
                  <img src="@/assets/gear-loading.gif" v-if="isLoading" />
                </div>
              </div>
              <ul class="list-group">
                <li class="list-group-item bg-dark" v-for="(user, index) in externalSearchResult" v-bind:key="index"
                v-on:click="addExternalUser(index)">
                  <span class="clickable">{{ user.fullName }}  <i>({{ user.userId }})</i></span>
                </li>
              </ul>
            </div>
          </div>
          <div class="rightpanel">
            <p>External Users selected</p>
            <ul class="list-group">
              <li class="list-group-item bg-dark" v-for="(user, index) in externalSelected" v-bind:key="index"
              v-on:click="removeExternalUser(index)">
                <span class="clickable">{{ user.fullName }}  <i>({{ user.userId }})</i></span>
              </li>
            </ul>
          </div>
        </b-tab>
      </b-tabs>
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
    type: Number,
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
      readonly: true,
      externalSelected: [],
      externalSearchResult: [],
      externalTimer: null,
      externalLastSearch: '',
      externalSearchContent: ''
    }
  },
  mounted: function () {
    this.readonly = (this.loginStatus.user.userId !== this.workspace.ownerId) &&
      (this.workspace.users.Admin.indexOf(this.loginStatus.user.userId) < 0)
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
    addExternalUser: function (index) {
      let user = this.externalSearchResult[index]
      if (this.externalSelected.indexOf(user) < 0) {
        this.externalSelected.push(user)
      }
    },
    removeExternalUser: function (index) {
      this.externalSelected.splice(index, 1)
    },
    searchInternal: function () {
      let w = this.searchContent.trim()
      if (w === '') {
        this.searchResult = []
        return
      }
      (new UserApiHelper()).searchInternalUser(w).then(rsp => {
        this.searchResult = rsp.data.data
      }).catch(err => {
        this.errorMessage = err
      })
    },
    userInputInternal: function () {
      if (this.lastSearch === this.searchContent.trim()) {
        return
      }
      this.lastSearch = this.searchContent.trim()
      clearTimeout(this.timer)
      this.timer = setTimeout(_ => {
        this.searchInternal()
      }, 500)
    },
    searchExternal: function () {
      let w = this.externalSearchContent.trim()
      if (w === '') {
        this.externalSearchResult = []
        return
      }
      (new UserApiHelper()).searchExternalUser(w).then(rsp => {
        this.externalSearchResult = rsp.data.data
      }).catch(err => {
        this.errorMessage = err
      })
    },
    userInputExternal: function () {
      if (this.externalLastSearch === this.externalSearchContent.trim()) {
        return
      }
      this.externalLastSearch = this.externalSearchContent.trim()
      clearTimeout(this.externalTimer)
      this.externalTimer = setTimeout(_ => {
        this.searchExternal()
      }, 500)
    },
    cancel: function () {
      if (confirm('Are you sure to cancel all edit?')) {
        router.back()
      }
    },
    save: function () {
      (new WorkspaceApiHelper()).addMember(
        this.workspace.id,
        this.type,
        this.selected.map(user => user.userId),
        this.externalSelected.map(user => user.userId))
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
