
<template>
    <form>
      <div class="form-row">
        <div class="col">
          <label for="inputHost">Host</label>
          <input type="text" class="form-control" id="inputHost" v-model="config.host" />
        </div>
        <div class="col">
          <label for="inputPort">Port</label>
          <input type="number" class="form-control" v-model="config.port" id="inputPort" placeholder="5432" />
        </div>
      </div>
      <div class="form-row">
        <div class="col">
          <label for="inputUser">User Name</label>
          <input type="text" class="form-control" v-model="config.username" id="inputUser" />
        </div>
        <div class="col">
          <label for="inputPassword">Password</label>
          <input type="password" class="form-control" v-model="config.password" id="inputPassword" />
        </div>
      </div>
      <div class="form-row">
        <div class="col-6">
          <label for="inputDatabase">Database</label>
          <select class="form-control" v-model="config.databaseName">
            <option disabled value=undefined v-if="databases.length == 0">No database found</option>
            <option v-for="database in databases" v-bind:key="database" v-bind:value="database">{{ database }}</option>
          </select>
        </div>
      </div>
      <div class="form-row" style="margin-top: 15px">
        <div class="col-3">
          <button type="button" class="form-control btn btn-primary" v-on:click="verifyConnection()">Verify</button>
        </div>
        <div class="col-1 verifyArea">
          <img src="../../assets/gear-loading.gif" v-show="loading" />
        </div>
        <div class="col-1 verifyArea">
          <img src="../../assets/success.png" v-if="verifyResult === true" />
          <img src="../../assets/fail.png" v-if="verifyResult === false" v-bind:title="errorMessage"/>
        </div>
      </div>
   </form>
</template>

<script>
import ConnectionApiHelper from '../../apis/connection.js'

export default {
  name: 'PostgreSQLConfig',
  props: {
    pgconfig: Object,
    extra: Object
  },
  mounted: function () {
    if (this.pgconfig.host !== undefined) {
      this.verifyConnection()
    }
  },
  data () {
    return {
      config: this.pgconfig,
      verifyResult: undefined,
      errorMessage: undefined,
      loading: false,
      databases: []
    }
  },
  methods: {
    verifyConnection: function () {
      this.loading = true
      this.verifyResult = undefined;
      (new ConnectionApiHelper()).verifyConnection(this.config).then(rsp => {
        this.loading = false
        if (rsp.status === 200) {
          if (rsp.data.data.result) {
            this.verifyResult = true
            this.databases = rsp.data.data.databases
            if ((this.config.databaseName === undefined) || (this.databases.indexOf(this.config.databaseName) < 0)) {
              this.config.databaseName = this.databases.length > 0 ? this.databases[0] : undefined
            }
            this.$emit('onErrorUpdate')
          } else {
            this.verifyResult = false
            this.clearDatabases()
            this.errorMessage = rsp.data.data.message
            this.$emit('onErrorUpdate', rsp.data.data.message)
          }
          this.$emit('onConnectionVerified', this.verifyResult)
        } else {
          this.$emit('onConnectionVerified', false)
        }
      }).catch(err => {
        // console.log(err.response)
        this.loading = false
        this.verifyResult = false
        this.errorMessage = err.response.data.error.message
        this.$emit('onConnectionVerified', false)
        this.$emit('onErrorUpdate', err.response.data.error.message)
      })
    }
  }
}
</script>

<style scoped>
.verifyArea img {
  width: 25px;
}
.veryfyMessage {
  white-space: normal;
  word-wrap: break-word;
  word-break: break-all;
}
</style>
