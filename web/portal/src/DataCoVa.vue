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
        <button class="btn btn-primary login" v-on:click="gotoLogin()" v-if="!logged">Login</button>
      </div>
      <div class="notification" title="Notifications">
        <a href="javascript:void(0);" v-if="logged">
          <img src="@/assets/notify.png" />
        </a>
      </div>
    </div>
    <router-view v-bind:loginStatus="loginStatus" />
  </div>
</template>

<script>
import 'bootstrap/dist/css/bootstrap.css'
import WSConnection from '@/apis/wsconnection.js'
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
      loginStatus: {},
      currentpath: this.$route.path
    }
  },
  created: function () {
    this.$root.$on('onLoginEvent', this.onLogin);
    (new User()).checkLogin(false, this.onLogin, this.gotoIndex)
  },
  methods: {
    logout: function () {
      (new User()).logout().then(rsp => {
        this.loginStatus = {}
        router.push('/').catch(err => console.log(`redirect err: ${err}`))
      })
    },
    gotoIndex: function () {
      console.log('CoVa not logged, refdirect to index')
      router.push('/index').catch(err => console.log(`redirect err: ${err}`))
    },
    onLogin: function (user) {
      console.log('onLogin called')
      console.log('CoVa logged')
      this.loginStatus = Object.assign({}, user)
      let ws = new WSConnection()
      ws.open()
      let dest = (this.currentpath === undefined) || (this.currentpath === '/') ? '/main' : this.currentpath
      router.push(dest).catch(err => console.log(`redirect err: ${err}`))
    },
    gotoLogin: function () {
      router.push('/login').catch()
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
.login {
  margin: 10px;
}
</style>
