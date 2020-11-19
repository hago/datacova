<template>
  <div class="form-row">
    <div class="col">
      <b>Not Implemented</b>
    </div>
    <div class="col">
      <textarea class="form-control" v-model="config.publicKey" placeholder="public key here">
      </textarea>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SFtpPublicKey',
  props: {
    config: Object
  }
}
</script>
