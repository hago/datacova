<template>
  <div class="form-row">
    <div class="col">
      <label for="inputUser">User Name</label>
      <input type="text" class="form-control" v-model="config.login" id="inputUser" placeholder="anonymous" />
    </div>
    <div class="col">
      <label for="inputPassword">Password</label>
      <input type="password" class="form-control" v-model="config.password" id="inputPassword" />
    </div>
  </div>
</template>

<script>
export default {
  name: 'SFtpLoginPwd',
  props: {
    config: Object
  }
}
</script>
