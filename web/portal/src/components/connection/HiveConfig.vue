<template>
  <form>
    <div class="area">
      <div>
        <em>Only Hive server 2 protocol is supported</em>
      </div>
      <div class="form-row">
        <div class="col-6">
          <label for="inputServiceDiscoverMode">Service Discovery Mode</label>
          <select type="text" class="form-control" id="inputServiceDiscoverMode" v-model="config.serviceDiscoveryMode">
            <option disabled value="undefined">Select discovery mode</option>
            <option v-for="(name, value) in serviceDiscoveryModes" :key="value" v-bind:value="value">{{ name }}</option>
          </select>
        </div>
        <div class="col-5" v-if="config.serviceDiscoveryMode === '1'">
          <label for="inputZKNS">ZooKeeper Name Space</label>
          <input type="text" v-model="config.zookeeperNamespace" class="form-control" id="inputZKNS" />
        </div>
        <div class="col-1" v-if="config.serviceDiscoveryMode === '1'">
          <a href="javascript:void(0)" v-on:click="addNode">
            <img src="../../assets/add_node.png" class="nodeaction" style="width:30px" title="New Node" />
          </a>
        </div>
      </div>
      <div class="form-row" v-if="config.serviceDiscoveryMode === '0'">
        <div class="col-6">
          <label for="inputHost">Host</label>
          <input type="text" v-model="config.host" class="form-control" id="inputHost" />
        </div>
        <div class="col-6">
          <label for="inputPort">Port</label>
          <input type="text" v-model="config.port" class="form-control" placeholder="10000" id="inputPort"/>
        </div>
      </div>
      <div v-if="config.serviceDiscoveryMode === '1'">
        <div class="form-row" v-for="(node, index) in config.zookeeperNodes" :key="index">
          <div class="col-6" >{{ node.index }}
            <label for="inputHost">Node {{ index+1 }} Host</label>
            <input type="text" v-model="node.host" class="form-control" id="inputHost" />
          </div>
          <div class="col-5">
            <label for="inputPort">Port</label>
            <input type="text" v-model="node.port" class="form-control" placeholder="2181" id="inputPort"/>
          </div>
          <div class="col-1">
            <img src="../../assets/remove.png" v-on:click="removeNode(index)" class="nodeaction" />
          </div>
        </div>
      </div>
    </div>
    <div class="area">
      <div class="form-row">
        <div class="col-6">
          <label for="inputAuthMech">Authentication Mechanism</label>
          <select type="text" class="form-control" id="inputAuthMech" v-model="config.authMech">
            <option disabled value="undefined">Select auth mechanism</option>
            <option v-for="(name, value) in authMechs" :key="value" v-bind:value="value">{{ name }}</option>
          </select>
        </div>
      </div>
      <div class="form-row" v-if="(config.authMech === '2') || (config.authMech === '3') ">
        <div class="col-6">
          <label for="inputUsername">User Name</label>
          <input type="text" class="form-control" v-model="config.userName" id="inputUsername" />
        </div>
        <div class="col-6" v-if="config.authMech === '3'">
          <label for="inputPassword">Password</label>
          <input type="password" class="form-control" v-model="config.password" id="inputPassword" />
        </div>
      </div>
    </div>
    <div class="area">
      <div class="form-row">
        <div class="col-6">
          <label for="inputTransport">Thrift Transport Mode</label>
          <select type="text" class="form-control" id="inputTransport" v-model="config.transportMode">
            <option disabled value="undefined">Select transport mode</option>
            <option v-for="(name, value) in transports" :key="value" v-bind:value="value">{{ name }}</option>
          </select>
        </div>
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
        <img src="../../assets/fail.png" v-if="verifyResult === false" />
      </div>
    </div>
  </form>
</template>

<script>
import ConnectionApiHelper from '../../apis/connection.js'
import Vue from 'vue'

export default {
  name: 'HiveConfig',
  props: {
    hiveconfig: Object,
    extra: Object
  },
  mounted: function () {
    if (((this.hiveconfig.host !== undefined) && (this.hiveconfig.host.trim() !== '')) ||
      ((this.hiveconfig.zookeeperNodes !== undefined) && (this.hiveconfig.zookeeperNodes.length > 0))) {
      this.verifyConnection()
    }
  },
  data () {
    return {
      config: this.hiveconfig,
      verifyResult: undefined,
      errorMessage: undefined,
      loading: false,
      serviceDiscoveryModes: {
        0: 'No Service Discovery',
        1: 'ZooKeeper'
      },
      // serviceDiscoveryMode: '0',
      authMechs: {
        0: 'No Authentication',
        2: 'User Name',
        3: 'User Name and Password'
      },
      transports: {
        binary: 'Binary',
        http: 'Http',
        sasl: 'SASL'
      },
      databases: []
    }
  },
  /* watch: {
    serviceDiscoveryMode: function (newMode, oldMode) {
      this.config.serviceDiscoveryMode = newMode
    }
  }, */
  methods: {
    addNode: function () {
      console.log('adding')
      let x = this.config.zookeeperNodes === undefined ? [] : this.config.zookeeperNodes
      x.push({host: '', port: ''})
      Vue.set(this.config, 'zookeeperNodes', x)
    },
    removeNode: function (index) {
      console.log('deleting')
      this.config.zookeeperNodes.splice(index, 1)
    },
    verifyConnection: function () {
      this.loading = true
      this.verifyResult = undefined;
      (new ConnectionApiHelper()).verifyConnection('hive', this.config).then(rsp => {
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
.nodeaction {
    width: 20px;
    position: absolute;
    margin: auto auto auto auto;
    top: 0;
    bottom: 0;
    left: 0;
    right: 0;
}
.area {
  border: 1px solid azure;
  padding: 10px 10px 10px 10px;
  margin-top: 10px
}
.verifyArea img {
  width: 25px;
}
</style>
