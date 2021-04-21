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
        <a v-if="logged" href="javascript:void(0);" v-on:click="logout()">{{this.loginStatus.user.name}}</a>
        <span v-if="!logged">Anonymous</span>
      </div>
      <div class="notification" title="Notifications">
        <a href="" v-if="logged">
          <img src="@/assets/notify.png" />
        </a>
      </div>
    </div>
    <router-view v-bind:loginStatus="loginStatus" />
  </div>
</template>

<script>
import 'bootstrap/dist/css/bootstrap.css'
// import WSConnection from '@/apis/wsconnection.js'
import router from '@/router'
import User from '@/apis/user.js'

export default {
  name: 'DataCoVa',
  computed: {
    logged: function () {
      return this.loginStatus.user !== undefined
    }
  },
  data () {
    return {
      loginStatus: {}
    }
  },
  created: function () {
    this.$root.$on('onLoginEvent', this.onLogin);
    (new User()).checkLogin(false, this.onLogin, this.gotoLogin)
  },
  methods: {
    logout: function () {
      (new User()).logout().then(rsp => {
        this.loginStatus = {}
        router.push('/')
      })
    },
    gotoLogin: function () {
      console.log('CoVa not logged, refdirect')
      router.push('/login')
    },
    onLogin: function (user) {
      console.log('onLogin called')
      console.log('CoVa logged')
      this.loginStatus = Object.assign({}, user)
      router.push('/main')
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
.notification {
  margin: 5px;
  float: right;
  font-size: 18px
}
.notification img {
  width: 20px;
  height: 20px;
}
.title_red {
  color: red;
}
</style>
