<template>
  <div style="height: 100%; width: 100%;">
    <img src="@/assets/gear-loading.gif" v-if="loginStatus.user === undefined" class="loading"/>
    <button class="btn btn-primary" v-on:click="gotoLogin()">Login</button>
  </div>
</template>

<script>
import 'bootstrap/dist/css/bootstrap.css'
import router from '@/router'

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
    gotoLogin: function () {
      router.push('/login').catch()
    }
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
