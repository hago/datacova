<template>
  <div class="form-row">
    <div class="col">
      <label for="inputKeyFile">Key File</label>
      <b-form-file type="file" class="form-control" id="inputKeyFile" v-model="file" ></b-form-file>
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
  },
  data: function () {
    return {
      file: undefined
    }
  },
  watch: {
    file: function (newFile, oldFile) {
      this.$emit('keyfileset', newFile)
    }
  }
}
</script>
