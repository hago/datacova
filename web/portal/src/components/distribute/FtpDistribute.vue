<template>
  <div>
    <div class="form-row">
      <div class="col-3">
        <label for="inputHost">Host</label>
        <input type="text" class="form-control" id="inputHost" v-model="config.host" />
      </div>
      <div class="col-3">
        <label for="inputPort">Port</label>
        <input type="number" class="form-control" v-model="config.port" id="inputPort" placeholder="21" />
      </div>
      <div class="col">
        <button class="btn btn-info" v-on:click="verify()">Verify FTP Connectivity</button>
      </div>
    </div>
    <div class="form-row">
      <div class="col">
        <label for="inputUser">User Name</label>
        <input type="text" class="form-control" v-model="config.login" id="inputUser" placeholder="anonymous" />
      </div>
      <div class="col">
        <label for="inputPassword">Password</label>
        <input type="password" class="form-control" v-model="config.password" id="inputPassword" />
      </div>
    </div>
    <div class="form-row">
      <div class="col">
        <label for="remotePath">Remote Path</label>
        <input type="text" class="form-control" v-model="config.remotePath" id="remotePath" placeholder="/" />
      </div>
      <div class="col">
        <label for="remoteName">Remote Filename</label>
        <input type="text" class="form-control" v-model="config.remoteName" id="remoteName" />
      </div>
    </div>
    <div class="form-row">
      <div class="col-3 form-group">
        <div class="form-check">
          <input class="form-check-input" type="checkbox" id="passive" v-model="config.passive">
          <label class="form-check-label" for="passive">
            Passive Mode
          </label>
        </div>
      </div>
      <div class="col-3 form-group">
        <div class="form-check">
          <input class="form-check-input" type="checkbox" id="binary" v-model="config.binaryTransport">
          <label class="form-check-label" for="binary">
            Binary Transport Mode
          </label>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Vue from 'vue'
import DistributorApiHelper from '@/apis/distributor.js'
import Toasted from 'vue-toasted'

Vue.use(Toasted)

export default {
  name: 'FtpDistribute',
  props: {
    config: Object
  },
  data () {
    return {}
  },
  mounted: function () {
    if (this.config.binaryTransport === undefined) {
      Vue.set(this.config, 'binaryTransport', true)
    }
    this.config.validator = function (config) {
      if ((config.host === undefined) || (config.host.trim() === '')) {
        return 'host not defined'
      }
      if ((config.remotePath === undefined) || (config.remotePath.trim() === '')) {
        return 'remote path not defined'
      }
      if ((config.remoteName === undefined) || (config.remoteName.trim() === '')) {
        return 'remote file name not defined'
      }
      return true
    }
  },
  methods: {
    verify: function () {
      (new DistributorApiHelper()).verifyFtp(this.config).then(rsp => {
        this.$toasted.show('ftp verification succeeded', {
          position: 'bottom-center',
          duration: 1000,
          type: 'success'
        })
      }).catch(err => {
        this.$toasted.show(err.response.data.error.message, {
          position: 'bottom-center',
          duration: 1000,
          type: 'error'
        })
      })
    }
  }
}
</script>

<style scoped>

</style>
