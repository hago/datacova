<template>
  <div v-title data-title="Login">
    <div class="loginarea">
      <div class="form-group row channel">
        <label for="channel" class="col-form-label col-4">Channel</label>
        <div class="col-6">
          <select class="form-control" v-model="loginChoice" id="channel">
            <option v-if="loginMethods.length === 0" value="-1">Login Disabled</option>
            <option v-for="(providerType, method) in loginMethods" v-bind:value="providerType" v-bind:key="providerType">
              {{ method }}
            </option>
          </select>
        </div>
      </div>
      <div class="form-group row">
        <label for="username" class="col-form-label col-4">User</label>
        <div class="col-6">
          <input
            type="text"
            placeholder="user id"
            id="username"
            v-model="user"
            class="form-control" />
        </div>
      </div>
      <div class="form-group row">
        <label for="password" class="col-form-label col-4">Password</label>
        <div class="col-6">
          <input
            type="password"
            placeholder="password"
            id="password"
            v-model="password"
            class="form-control" />
        </div>
      </div>
      <div class="form-group row" v-if="loginChoice==0">
        <label for="captcha" class="col-form-label col-4">Captcha</label>
        <div class="col-6">
          <input
            type="text"
            placeholder="Captcha Code"
            id="captcha"
            v-model="captcha"
            class="form-control" />
        </div>
      </div>
      <div class="row" v-if="loginChoice==0">
        <img v-bind:src="captchaUrl" class="captcha"/>
        <button class="btn btn-info" v-on:click="refreshCaptcha()">Refresh</button>
      </div>
      <div class="row">
        <span class="badge badge-danger veryfyMessage">{{ errorMessage }}</span>
      </div>
      <div class="row">
        <div class="col-6">
          <button class="btn btn-info" v-on:click="register()">Register</button>
          <span style="color:green">Doesn't have account?</span>
        </div>
        <div class="col-6 text-right">
          <button class="btn btn-primary" v-on:click="login()">Log in</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import UserAPIHelper from '../apis/userapi.js'
import SiteHelper from '../apis/site.js'
import User from '../apis/user.js'
import router from '@/router'

export default {
  name: 'Login',
  data () {
    return {
      user: '',
      password: '',
      captcha: '',
      captchaUrl: `${process.env.SERVICE_BASE_URL === undefined ? '' : process.env.SERVICE_BASE_URL}/api/auth/captcha?${Math.random()}`,
      errorMessage: '',
      loginChoice: 0,
      loginMethods: {
        'Regular(username, password and captcha)': 0
      }
    }
  },
  props: {
    return: Object
  },
  created: function () {
    let api = new SiteHelper(process.env.SERVICE_BASE_URL)
    api.loadsettings().then(rsp => {
      let extraProviders = {}
      for (var k in rsp.data.settings.userProviders) {
        let element = rsp.data.settings.userProviders[k]
        extraProviders[element.name] = element.providerType
      }
      console.log(extraProviders)
      this.loginMethods = Object.assign({}, this.loginMethods, extraProviders)
    })
  },
  methods: {
    login () {
      if (!this.check()) {
        this.errorMessage = 'empty field found'
        return
      }
      let api = new UserAPIHelper(process.env.SERVICE_BASE_URL)
      let logincall = null
      switch (this.loginChoice) {
        case 0:
          logincall = api.loginRegular(this.user.trim(), this.password.trim(), this.captcha.trim())
          break
        case 1:
          logincall = api.loginLdap(this.user.trim(), this.password.trim())
          break
      }
      if (logincall == null) {
        this.errorMessage = 'undefined login method'
        return
      }
      logincall.then(rsp => {
        console.log('login successfully')
        this.errorMessage = '';
        (new User()).setLogin(rsp.data.data)
        this.$root.$emit('onLogin', rsp.data.data)
        let dest = (this.return !== undefined) && (this.return !== null) ? this.return : '/main'
        router.push(dest).catch()
      }).catch(err => {
        console.log(err)
        this.errorMessage = err.response.data.error.message
        this.refreshCaptcha()
      })
    },
    check () {
      let userId = this.user.trim()
      switch (this.loginChoice) {
        case 0:
          return userId !== '' && this.password.trim() !== '' && this.captcha.trim() !== ''
        case 1:
          return userId !== '' && this.password.trim() !== ''
        default:
          return false
      }
    },
    refreshCaptcha () {
      this.captchaUrl = `${process.env.SERVICE_BASE_URL === undefined ? '' : process.env.SERVICE_BASE_URL}/api/auth/captcha?${Math.random()}`
    },
    register () {
      router.push('/register').catch(err => console.log(err))
    }
  }
}
</script>

<style scoped>
.loginarea {
  width: 40%;
  margin-left: auto;
  margin-right: auto
}
.row {
  margin-top: 5px
}
.captcha {
  color: red
}
.channel {
  font-style: italic;
  color: aqua;
}
</style>
