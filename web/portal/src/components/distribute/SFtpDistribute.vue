<template>
  <div>
    <div class="form-row">
      <div class="col-3">
        <label for="inputHost">Host</label>
        <input type="text" class="form-control" id="inputHost" v-model="config.host" />
      </div>
      <div class="col-3">
        <label for="inputPort">Port</label>
        <input type="number" class="form-control" v-model="config.port" id="inputPort" placeholder="22" />
      </div>
      <div class="col">
        <button class="btn btn-info" v-on:click="verify()">Test SFtp Connectivity</button>
      </div>
    </div>
    <div class="form-row">
      <div class="form-group col-6">
        <select class="form-control" v-model="config.authType">
          <option value=undefined disabled>Select Authentication Type</option>
          <option v-for="(name, key) in authTypes" v-bind:value="key" v-bind:key="key">{{ name }}</option>
        </select>
      </div>
    </div>
    <div class="form-row">
      <div class="col-6">
        <label for="inputUser">User Name</label>
        <input type="text" class="form-control" v-model="config.login" id="inputUser" placeholder="anonymous" />
      </div>
    </div>
    <SFtpLoginPwd v-if="config.authType === 'Password'"
      v-bind:config="config"
      ></SFtpLoginPwd>
    <SFtpPrivateKey v-if="config.authType === 'PrivateKey'"
      v-bind:config="config"
      ></SFtpPrivateKey>
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
  </div>
</template>

<script>
import SFtpLoginPwd from '@/components/distribute/SFtpLoginPwd.vue'
import SFtpPrivateKey from '@/components/distribute/SFtpPrivateKey.vue'
import Vue from 'vue'
import DistributorApiHelper from '@/apis/distributor.js'
import Toasted from 'vue-toasted'

Vue.use(Toasted)

export default {
  name: 'SFtpDistribute',
  props: {
    config: Object
  },
  components: {
    SFtpLoginPwd,
    SFtpPrivateKey
  },
  data () {
    return {
      authTypes: {
        Password: 'UserName / Password',
        PrivateKey: 'Private Key (DSA,RSA,ECDSA)'
      }
    }
  },
  mounted: function () {
    this.config.validator = function (config) {
      if ((config.host === undefined) || (config.host.trim() === '')) {
        return 'host not defined'
      }
      if ((config.authType === 'Password') && ((config.login === undefined) || (config.login.trim() === ''))) {
        return 'user name is empty!'
      }
      if ((config.authType === 'PublicKey') && ((config.publicKey === undefined) || (config.publicKey.trim() === ''))) {
        return 'public key is empty!'
      }
      if ((config.remotePath === undefined) || (config.remotePath.trim() === '')) {
        return 'remote path not defined'
      }
      return true
    }
  },
  methods: {
    verify: function () {
      (new DistributorApiHelper()).verifySFtp(this.config).then(rsp => {
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
