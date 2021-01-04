<template>
  <div>
    <div class="loginarea">
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
      <div class="form-group row" v-if="!external && frominternet">
        <label for="otp" class="col-form-label col-4">OTP</label>
        <div class="col-6">
          <input
            type="text"
            placeholder="OTP Code"
            id="otp"
            v-model="otpcode"
            class="form-control" />
        </div>
      </div>
      <div class="form-group row" v-if="external">
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
      <div class="row" v-if="external">
        <img v-bind:src="captchaUrl"/>
        <button class="btn btn-info" v-on:click="refreshCaptcha()">Refresh</button>
      </div>
      <div class="row">
        <span class="badge badge-danger veryfyMessage">{{ errorMessage }}</span>
      </div>
      <div class="form-group row">
        <label for="captcha" class="col-form-label col-4">Internal / External</label>
        <div class="col-6">
          <div class="form-check">
            <input class="form-check-input" type="checkbox" id="chkexternal" v-model="external">
            <label class="form-check-label" for="chkexternal">
              External
            </label>
          </div>
        </div>
      </div>
      <button class="btn btn-primary" v-on:click="login()">Login</button>
    </div>
  </div>
</template>

<script>
import UserAPIHelper from '../apis/userapi.js'
import User from '../apis/user.js'
import router from '../router'

export default {
  name: 'Login',
  data () {
    return {
      user: '',
      password: '',
      otpcode: '',
      captcha: '',
      external: false,
      captchaUrl: `${process.env.SERVICE_BASE_URL === undefined ? '' : process.env.SERVICE_BASE_URL}/api/auth/captcha`,
      errorMessage: '',
      frominternet: false
    }
  },
  props: {
    return: String,
    loginStatus: Object
  },
  created: function () {
    (new UserAPIHelper(process.env.SERVICE_BASE_URL)).checkNetwork().then(rsp => {
      this.frominternet = rsp.data.data.isExtraNet
    }).catch(err => console.log(`check network error: ${err}`))
  },
  methods: {
    login () {
      if (!this.check()) {
        this.errorMessage = 'empty field found'
        return
      }
      let api = new UserAPIHelper(process.env.SERVICE_BASE_URL)
      let logincall = this.external ? api.loginExternal(this.user.trim(), this.password.trim(), this.captcha.trim())
        : api.login(this.user.trim(), this.password, this.otpcode.trim())
      logincall.then(rsp => {
        this.errorMessage = '';
        (new User()).setLogin(rsp.data.data)
        this.loginStatus = Object.assign(this.loginStatus, rsp.data.data)
        router.push(this.return === undefined ? '/' : this.return)
      }).catch(err => {
        console.log(err)
        this.errorMessage = err.response.data.error.message
      })
    },
    check () {
      let userId = this.user.trim()
      if (!this.external) {
        return this.isExtraNet ? (userId !== '' && this.password !== '' && this.otpcode.trim() !== '')
          : (userId !== '' && this.password !== '')
      } else {
        return userId !== '' && this.password !== '' && this.captcha.trim() !== ''
      }
    },
    refreshCaptcha () {
      this.captchaUrl = `${process.env.SERVICE_BASE_URL === undefined ? '' : process.env.SERVICE_BASE_URL}/api/auth/captcha?${Math.random()}`
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
</style>
