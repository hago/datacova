<template>
  <div>
    <div class="head">
      <div class="logo">
        <span>Data</span>
        <span class="title_red">Co</span><span>llecting</span>
        <span> &amp; </span>
        <span class="title_red">Va</span><span>lidation</span>
      </div>
      <div class="user">
        <a v-if="this.loginStatus.user !== undefined" href="javascript:void(0);" v-on:click="logout()">{{this.loginStatus.user.name}}</a>
        <span v-if="this.loginStatus.user === undefined">Anonymous</span>
      </div>
    </div>
    <router-view v-bind:loginStatus="loginStatus" />
  </div>
</template>

<script>
import 'bootstrap/dist/css/bootstrap.css'
import router from '@/router'
import Vue from 'vue'
import User from '@/apis/user.js'

export default {
  name: 'DataCoVa',
  data () {
    return {
      loginStatus: {},
      name: null
    }
  },
  created: function () {
    (new User()).checkLogin(true, this.init)
  },
  methods: {
    init: function (user) {
      for (let k in user) {
        Vue.set(this.loginStatus, k, user[k])
      }
      this.name = user.user.displayName
      router.push('/main')
    },
    logout: function () {
      (new User()).logout().then(rsp => {
        Vue.set(this, 'loginStatus', {})
        this.name = null
        router.push('/')
      })
    }
  }
}
</script>
<style>
#app {
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color:whitesmoke;
}
.head {
  overflow: auto
}
.logo {
  margin: 5px;
  float: left;
  font-size: 30px
}
.user {
  margin: 5px;
  float: right;
  font-size: 20px
}
.title_red {
  color: red;
}
</style>
