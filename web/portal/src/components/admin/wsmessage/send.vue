<template>
  <div v-title data-title="Web Socket Message">
    <div>
      <h3>Clients</h3>
      <ul>
        <li v-for="session in sessions" v-bind:key="session.id">
          <span>client.ip</span>
        </li>
      </ul>
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
  }
}
</script>

<style scoped>

</style>
