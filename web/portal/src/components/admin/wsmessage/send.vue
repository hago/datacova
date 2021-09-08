<template>
  <div v-title data-title="Web Socket Message">
    <div>
      <h3>Clients</h3>
      <div class="row title">
        <div class="col-5">Device</div>
        <div class="col-2">User</div>
        <div class="col-2">IP</div>
      </div>
      <div class="row" v-for="session in sessions" v-bind:key="session.id">
        <div class="col-5">{{ session.deviceIdentity }}</div>
        <div class="col-2">{{ session.userInfo.name }}</div>
        <div class="col-2">{{ session.remoteIp }}</div>
        <div class="col-3"><button class="btn btn-info">Message</button></div>
      </div>
    </div>
  </div>
</template>

<script>
import AdminApi from '@/apis/adminapi.js'

export default {
  name: 'WsMessageSend',
  data: function () {
    return {
      sessions: []
    }
  },
  created: function () {
    (new AdminApi()).getwssessions().then(rsp => {
      this.sessions = rsp.data.data
    })
  },
  methods: {
    composeMessage: function () {
      //
    }
  }
}
</script>

<style scoped>
.title {
  color: aquamarine;
  font-weight: bold;
}
</style>
