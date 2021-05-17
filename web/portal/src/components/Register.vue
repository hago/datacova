<template>
  <div v-title data-title="Register New User">
    <h2 class="title text-center">Register new user</h2>
    <div class="loginarea">
      <div class="form-group row">
        <label for="username" class="col-form-label col-3">User ID</label>
        <div class="col-8">
          <input
            type="text"
            placeholder="user id"
            id="username"
            v-model="user.userId"
            class="form-control" />
        </div>
        <div class="col-1" v-if="useridcheck !== null">
          <img class="checkflag" :src="useridcheck ? fieldok : fieldfail" />
        </div>
      </div>
      <div class="form-group row">
        <label for="password" class="col-form-label col-3">Password</label>
        <div class="col-8">
          <input
            type="password"
            placeholder="password"
            id="password"
            v-model="user.pwdHash"
            class="form-control" />
        </div>
      </div>
      <div class="form-group row">
        <label for="password2" class="col-form-label col-3">Repeat Password</label>
        <div class="col-8">
          <input
            type="password"
            placeholder="Retype password"
            id="password2"
            v-model="password2"
            class="form-control" />
        </div>
      </div>
      <div class="form-group row">
        <label for="email" class="col-form-label col-3">Email</label>
        <div class="col-8">
          <input
            type="text"
            placeholder="Email Address"
            id="email"
            v-model="user.email"
            class="form-control" />
        </div>
        <div class="col-1" v-if="useridcheck !== null">
          <img class="checkflag" :src="emailcheck ? fieldok : fieldfail" />
        </div>
      </div>
      <div class="form-group row">
        <label for="mobile" class="col-form-label col-3">Mobile</label>
        <div class="col-8">
          <input
            type="text"
            placeholder="Mobile Number"
            id="mobile"
            v-model="user.mobile"
            class="form-control" />
        </div>
        <div class="col-1" v-if="useridcheck !== null">
          <img class="checkflag" :src="mobilecheck ? fieldok : fieldfail" />
        </div>
      </div>
      <div class="form-group row" >
        <label for="avatar" class="col-form-label col-3">Avatar</label>
        <div class="col-8 text-center">
          <img class="avatar" src="@/assets/avatar.png" id="avatar" />
        </div>
      </div>
      <div class="row">
        <b-form-file type="file" class="form-control col-11" v-model="avatarfile" accept=".png, .jpg"></b-form-file>
      </div>
      <div class="form-group row">
        <label for="captcha" class="col-form-label col-3">Captcha</label>
        <div class="col-8">
          <input
            type="text"
            placeholder="Captcha Code"
            id="captcha"
            v-model="captcha"
            class="form-control" />
        </div>
      </div>
      <div class="row">
        <div class="col-8 text-center">
          <img v-bind:src="captchaUrl" class="captcha"/>
        </div>
        <div class="col-3 text-right" style="margin: auto">
          <button class="btn btn-info" v-on:click="refreshCaptcha()">Refresh</button>
        </div>
        <div class="col-1"></div>
      </div>
      <div class="row" v-if="errorMessage !== null">
        <span class="badge badge-danger veryfyMessage">{{ errorMessage }}</span>
      </div>
      <div class="row" v-if="!succeeded">
        <div class="col-11 text-right">
          <button class="btn btn-primary" v-on:click="register()">Register</button>
        </div>
      </div>
      <div class="row" v-if="succeeded">
        <h2 style="color: green">
          Registration is successful, an activaion email was sent. Please follow instructions in the email to activate your account.
        </h2>
      </div>
    </div>
  </div>
</template>

<script>
import UserApiHelper from '@/apis/userapi.js'

export default {
  name: 'Register',
  data: function () {
    return {
      user: {
        usertype: 0
      },
      password2: '',
      errorMessage: null,
      useridcheck: null,
      emailcheck: null,
      mobilecheck: null,
      passwordmatch: null,
      fieldok: '@/assets/success.png',
      fieldfail: '@/assets/fail.png',
      captchaUrl: `${process.env.SERVICE_BASE_URL === undefined ? '' : process.env.SERVICE_BASE_URL}/api/auth/captcha?${Math.random()}`,
      captcha: '',
      succeeded: false,
      avatarfile: null
    }
  },
  watch: {
    avatarfile: function (newValue) {
      console.log(newValue)
      let fr = new FileReader()
      let reg = this
      fr.onload = function (e) {
        let buffer = e.target.result
        console.log(buffer.length)
        reg.setpic(newValue.type, buffer)
      }
      fr.readAsBinaryString(newValue)
    }
  },
  methods: {
    checkuserid: function () {
      if ((this.user.userid === undefined) || (this.user.userid === null) || (this.user.userid.trim() === '')) {
        this.useridcheck = null
      }
      (new UserApiHelper()).checkRegisterUserId(this.user.userid)
        .then(rsp => { this.useridcheck = true })
        .catch(_ => { this.useridcheck = false })
    },
    checkemail: function () {
      if ((this.user.email === undefined) || (this.user.email === null) || (this.user.email.trim() === '')) {
        this.emailcheck = null
      }
      (new UserApiHelper()).checkRegisterEmail(this.user.userid)
        .then(rsp => { this.emailcheck = true })
        .catch(_ => { this.emailcheck = false })
    },
    checkmobile: function () {
      if ((this.user.mobile === undefined) || (this.user.mobile === null) || (this.user.mobile.trim() === '')) {
        this.mobilecheck = null
      }
      (new UserApiHelper()).checkRegisterMobile(this.user.userid)
        .then(rsp => { this.mobilecheck = true })
        .catch(_ => { this.mobilecheck = false })
    },
    register: function () {
      if (this.password2 !== this.userid.pwdHash) {
        this.errorMessage = 'password not match'
        return
      }
      if (!this.checkempty(this.user.userid) || !this.checkempty(this.user.email) ||
        !this.checkempty(this.user.pwdHash) || !this.checkempty(this.user.mobile)) {
        this.errorMessage = 'Information is not complete'
        return
      }
      if (this.avatarfile === null) {
        this.errorMessage = 'Avatar is not set'
        return
      }
      (new UserApiHelper()).register(this.user, this.captcha).then(rsp => {
        this.succeeded = true
        this.errorMessage = ''
      }).catch(err => {
        this.errorMessage = err.response.data.message
      })
    },
    checkempty: function (input) {
      return input !== undefined && input !== null && input.trim() !== ''
    },
    refreshCaptcha () {
      this.captchaUrl = `${process.env.SERVICE_BASE_URL === undefined ? '' : process.env.SERVICE_BASE_URL}/api/auth/captcha?${Math.random()}`
    },
    setpic (type, content) {
      this.user.thumbnail = window.btoa(content)
      document.getElementById('avatar').src = `data:${type};base64,${this.user.thumbnail}`
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
.title {
  margin-bottom: 15px;
}
.checkflag {
  width: 35px;
  height: 35px;
}
.avatar {
  width: 100px;
  height: 80px;
  margin: auto;
}
</style>
