<template>
  <div style="height: 100%; width: 100%;">
    <img src="@/assets/gear-loading.gif" v-if="loginStatus.user === undefined" class="loading"/>
  </div>
</template>

<script>
import 'bootstrap/dist/css/bootstrap.css'

export default {
  name: 'Index',
  data () {
    return {
    }
  },
  props: {
    loginStatus: Object
  },
  methods: {
  }
}
</script>

<style>
.loading {
    display: block;
    margin: auto auto auto auto;
    width: 150px;
    height: 150px;
}
</style>
