<template>
  <div style="height: 100%; width: 100%;">
    <img src="@/assets/gear-loading.gif"/>
  </div>
</template>

<script>
import router from '@/router'
import Vue from 'vue'
import User from '@/apis/user.js'

export default {
  name: 'Index',
  data () {
    return {
    }
  },
  mounted: function () {
    (new User()).checkLogin(false, this.logged)
  },
  props: {
    loginStatus: Object
  },
  methods: {
    logged: function (user) {
      for (let k in user) {
        Vue.set(this.loginStatus, k, user[k])
      }
      this.name = user.user.name
      router.push('/main')
    }
  }
}
</script>

<style>
.loading {
    display: block;
    margin: auto auto auto auto;
}
</style>
