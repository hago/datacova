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
        <a v-if="logged" href="javascript:void(0);" v-on:click="logout()">
          <span>{{this.loginStatus.user.name}}</span>
          <img class="avatar" src="@/assets/avatar.png" v-if="!haspic" />
          <img class="avatar" :src='useravatar' id='useravatar' v-if="haspic" />
        </a>
        <button class="btn btn-primary login" v-on:click="gotoLoginRegister()" v-if="!logged">Login / Register</button>
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
    },
    haspic: function () {
      return (this.loginStatus.user !== undefined) && (this.loginStatus.user.thumbnail !== null)
    }
  },
  watch: {
    loginStatus: function (newValue) {
      if ((newValue.user === undefined) || (newValue.user.thumbnail === null)) {
        return
      }
      let b64pic = newValue.user.thumbnail
      this.useravatar = `data:image/jpg;base64,${b64pic}`
    }
  },
  data () {
    return {
      loginStatus: {},
      useravatar: '@/assets/avatar.png'
    }
  },
  created: function () {
    let user = (new User()).getUser()
    if (user !== null) {
      this.loginStatus = user
      let ws = new WSConnection()
      ws.open()
    } else {
      this.$root.$on('onLogin', this.onLogged)
    }
  },
  methods: {
    logout: function () {
      (new User()).logout().then(rsp => {
        this.loginStatus = {}
        this.$root.$emit('onLogout')
        router.push({path: '/', params: {loginStatus: this.loginStatus}})
          .catch(err => console.log(`redirect err: ${err}`))
      })
    },
    onNotLogged: function () {
      console.log('CoVa not logged, refdirect to index')
      this.gotoLoginRegister()
    },
    onLogged: function (user) {
      console.log('onLogged called')
      console.log('CoVa logged')
      this.loginStatus = Object.assign({}, user)
      let ws = new WSConnection()
      ws.open()
    },
    gotoLoginRegister: function () {
      router.push({name: 'Login', params: {return: this.$route}}).catch(err => console.log(`redirect err: ${err}`))
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
.avatar {
  width: 50px;
  height: 40px;
}
</style>
