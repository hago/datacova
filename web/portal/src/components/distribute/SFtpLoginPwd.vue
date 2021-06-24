<template>
  <div class="form-row">
    <div class="col-6">
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
