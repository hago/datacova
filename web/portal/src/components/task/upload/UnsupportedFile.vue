<template>
  <div class="form-row">
    <h3>Unsupported File Type: {{ fileType }}</h3>
  </div>
</template>

<script>
export default {
  name: 'UnsupportedFile',
  props: {
    fileType: String
  },
  data () {
    return {}
  }
}
</script>

<style scoped>
  h3 {
    color: red
  }
</style>
