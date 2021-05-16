<template>
  <div v-title data-title="Register New User">
    <div class="loginarea">
      <div class="form-group row">
        <label for="username" class="col-form-label col-4">User ID</label>
        <div class="col-8">
          <input
            type="text"
            placeholder="user id"
            id="username"
            v-model="user.userId"
            class="form-control" />
        </div>
      </div>
      <div class="form-group row">
        <label for="password" class="col-form-label col-4">Password</label>
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
        <label for="password2" class="col-form-label col-4">Repeat Password</label>
        <div class="col-8">
          <input
            type="password"
            placeholder="Retype password"
            id="password2"
            v-model="password2"
            class="form-control" />
        </div>
      </div>
      <div class="row" v-if="errorMessage !== null">
        <span class="badge badge-danger veryfyMessage">{{ errorMessage }}</span>
      </div>
      <div class="row">
        <div class="col-12 text-right">
          <button class="btn btn-primary" v-on:click="register()">Register</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Register',
  data: function () {
    return {
      user: {},
      password2: '',
      errorMessage: null
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
