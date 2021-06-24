<template>
  <div class="form-row">
    <div class="col">
      <label for="inputKeyFile">Key File</label>
      <b-form-file type="file" class="form-control" id="inputKeyFile" v-model="config.filex" ></b-form-file>
    </div>
    <div class="col">
      <label for="inputPassPhrase">PassPhrase</label>
      <input type="password" class="form-control" v-model="config.passPhrase" id="inputPassPhrase" />
    </div>
  </div>
</template>

<script>
export default {
  name: 'SFtpPrivateKey',
  props: {
    config: Object
  }
}
</script>
