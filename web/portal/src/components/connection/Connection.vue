<template>
  <div style="margin:5px">
    <div class="form-row">
      <div class="form-group col-6">
        <label for="conname">Name</label>
        <input type="text" class="form-control" id="conname" v-model="connection.name" />
      </div>
    </div>
    <div class="form-row">
      <div class="form-group col-6">
        <label for="condesp">Description</label>
        <textarea class="form-control" id="condesp" v-model="connection.description"></textarea>
      </div>
    </div>
    <div class="form-row">
      <select v-model="connection.configuration.dbType" class="form-control col-6" :disabled="!isNew"
        v-on:change="changeDbType()">
        <option disabled value="undefined">Data Connection Type</option>
        <option v-for="(name, id) in databaseTypes" :key="id" v-bind:value="id">
            {{ name }}
        </option>
      </select>
    </div>
    <PostgreSQLConfig v-if="connection.configuration.dbType==='PostgreSql'"
      v-bind:pgconfig="connection.configuration"
      v-bind:extra="connection.extra"
      v-on:onConnectionVerified="connectionVerified"
      v-on:onErrorUpdate="errorUpdate"
    ></PostgreSQLConfig>
    <SQLServerConfig v-if="connection.configuration.dbType==='MsSqlServer'"
      v-bind:msconfig="connection.configuration"
      v-bind:extra="connection.extra"
      v-on:onConnectionVerified="connectionVerified"
      v-on:onErrorUpdate="errorUpdate"
    ></SQLServerConfig>
    <HiveConfig v-if="connection.configuration.dbType==='Hive'"
      v-bind:hiveconfig="connection.configuration"
      v-bind:extra="connection.extra"
      v-on:onConnectionVerified="connectionVerified"
      v-on:onErrorUpdate="errorUpdate"
    ></HiveConfig>
    <MariaDBConfig v-if="connection.configuration.dbType==='MariaDb'"
      v-bind:mariadbconfig="connection.configuration"
      v-bind:extra="connection.extra"
      v-on:onConnectionVerified="connectionVerified"
      v-on:onErrorUpdate="errorUpdate"
    ></MariaDBConfig>
    <div style="margin: 2px">
      <button class="btn btn-primary" v-on:click="save()" v-if="!this.readonly">Save</button>
      <button class="btn btn-primary disabled" v-if="this.readonly">Save</button>
      <button class="btn btn-warning" v-on:click="cancel()">Cancel</button>
    </div>
    <div class="form-row" style="margin-top: 15px">
      <div class="col-7" v-if="errorMessage !== null">
        <h3><span class="badge badge-danger errorMessage">{{ errorMessage }}</span></h3>
      </div>
    </div>
  </div>
</template>

<script>
import Vue from 'vue'
import Toasted from 'vue-toasted'
import 'bootstrap/dist/css/bootstrap.css'
import router from '../../router'
import WorkspaceApiHelper from '@/apis/workspace.js'
// import ConnectionApiHelper from '@/apis/connection.js'

import PostgreSQLConfig from './PostgreSQLConfig.vue'
import SQLServerConfig from './SQLServerConfig.vue'
import HiveConfig from './HiveConfig.vue'
import MariaDBConfig from './MariaDBConfig.vue'

Vue.use(Toasted)

export default {
  name: 'Connection',
  components: {
    PostgreSQLConfig,
    SQLServerConfig,
    HiveConfig,
    MariaDBConfig
  },
  data () {
    return {
      databaseTypes: {
        PostgreSql: 'PostgreSQL',
        MsSqlServer: 'SQL Server',
        Hive: 'Hadoop Hive',
        MariaDb: 'MySQL / MariaDB'
      },
      isNew: true,
      connection: {
        name: '',
        description: '',
        configuration: {dbType: undefined},
        extra: {}
      },
      readonly: false,
      verified: false,
      errorMessage: null
    }
  },
  created: function () {
    if (this.$route.params.id > 0) {
      this.isNew = false
      this.loadConnection()
    }
  },
  methods: {
    changeDbType: function () {
      this.verified = false
      this.connection.configuration.databaseName = undefined
    },
    connectionVerified: function (result) {
      this.verified = result
    },
    errorUpdate: function (error) {
      this.errorMessage = error === undefined || error === null ? null : error
    },
    loadConnection: function () {
      (new WorkspaceApiHelper()).getConnection(this.$route.params.workspaceId, this.$route.params.id)
        .then(rsp => {
          this.connection = rsp.data.data.connection
          this.readonly = !rsp.data.data.permission.update
        })
        .catch(err => {
          this.errorMessage = err.response.data.error.message
        })
    },
    save: function () {
      if ((this.verified === undefined) || !this.verified) {
        this.errorMessage = 'Connection not verified'
        return
      }
      if (this.connection.name.trim() === '') {
        this.errorMessage = 'Not named yet!'
        return
      }
      console.log(this.connection);
      (new WorkspaceApiHelper()).saveConn4Workspace(this.$route.params.workspaceId, this.connection)
        .then(rsp => {
          this.$toasted.show('Connection saved successfully', {
            position: 'bottom-center',
            duration: 1000,
            type: 'success',
            onComplete: function () {
              router.back()
            }
          })
        }).catch(err => {
          // console.log('error', Object.assign({}, err))
          this.errorMessage = err.response.data.error.message
        })
    },
    cancel: function () {
      if (confirm('Are you sure to cancel?')) {
        router.back()
      }
    }
  }
}
</script>

<style scoped>
.errorMessage {
  white-space: normal;
  word-wrap: break-word;
  word-break: break-all;
}
</style>
