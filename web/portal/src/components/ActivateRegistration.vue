<template>
  <div v-title data-title="Activate">
    <p v-if="succeeded === true">Activated</p>
    <p v-if="succeeded === false">{{ errorMessage }}</p>
  </div>
</template>

<script>
import UserApiHelper from '@/apis/userapi.js'

export default {
  name: 'ActivateRegistration',
  data: function () {
    return {
      succeeded: null,
      errorMessage: ''
    }
  },
  created: function () {
    let code = this.$route.params.code;
    (new UserApiHelper()).activate(code).then(rsp => {
      this.succeeded = true
    }).catch(err => {
      this.succeeded = false
      this.errorMessage = err.response.data.error.message
    })
  }
}
</script>

<style scoped>

</style>
