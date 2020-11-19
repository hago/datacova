<template>
  <div class="form-row margin-left: 5px">
    <div class="form-group col-6">
      <label for="connection">Connection</label>
      <select v-model="action.connectionId" class="form-control" id="connection" v-on:change="switchConnection()">
        <option v-for="connection in connections" :key="connection.id" :value="connection.id">
          {{ connection.name }}
        </option>
      </select>
    </div>
    <div class="form-group col-3">
      <label for="schema">Target Schema</label>
      <select v-model="action.targetSchema" class="form-control" id="connection">
        <option v-for="(schema, index) in schemas" v-bind:key="index" v-bind:value="schema">
          {{ schema }}
        </option>
        <option disabled value=undefined>Choose Schema</option>
      </select>
    </div>
    <div class="form-group col-3">
      <label for="table">Target Table</label>
      <span style="float: right; margin-right: 5px">
        <img src="@/assets/create.png" width="20px" title="Create New Table" class="clickable"
           v-if="!createTable" v-on:click="toggleCreateTable(true)" />
        <img src="@/assets/choose.png" width="20px" title="Choose Existing Table" class="clickable"
          v-if="createTable" v-on:click="toggleCreateTable(false)"  />
      </span>
      <select v-model="action.targetTable" class="form-control" id="selectTable" v-if="!createTable">
        <option v-for="(table, index) in tablesInSchema" v-bind:key="index" v-bind:value="table.table">
          {{ table.table }}
        </option>
        <option disabled value=undefined>Choose Table</option>
      </select>
      <input type="text" v-model="action.targetTable" class="form-control" id="inputTable"
        v-if="createTable" ref="inputTable" />
    </div>
  </div>
</template>
<script>
import Vue from 'vue'

import WorkspaceApiHelper from '@/apis/workspace.js'
import ConnectionApiHelper from '@/apis/connection.js'

export default {
  name: 'TaskActionImport',
  props: {
    action: Object,
    workspaceId: Number
  },
  data () {
    return {
      actionTypes: {
        0: 'Databse Import',
        1: 'Data Verify',
        2: 'Data Distribute'
      },
      connections: [],
      connection: {},
      tables: [],
      createTable: false
    }
  },
  mounted: function () {
    this.action.validator = function (action) {
      // console.log(action)
      if (action.connectionId === undefined) {
        return 'connection is required'
      }
      if ((action.targetTable === undefined) || (action.targetTable.trim() === '')) {
        return 'target table is required'
      }
      return true
    }
    this.loadConnections()
  },
  computed: {
    schemas: function () {
      return Object.keys(this.tables)
    },
    tablesInSchema: function () {
      return this.action.targetSchema === undefined ? []
        : this.tables[this.action.targetSchema]
    }
  },
  methods: {
    loadConnections: function () {
      (new WorkspaceApiHelper()).getConnections(this.workspaceId).then(rsp => {
        this.connections = rsp.data.data.connections
        if (this.connections.length > 0) {
          if (this.action.connectionId === undefined) {
            this.action.connectionId = this.connections[0].id
            this.connection = this.connections[0]
          } else {
            this.connection = this.connections.find(conn => conn.id === this.action.connectionId)
          }
          this.loadTables()
        } else {
          this.connection = {}
          this.tables = []
        }
      })
    },
    loadTables: function () {
      this.$emit('errorOccurred')
      this.$emit('onLoadingStatusChange', true);
      (new ConnectionApiHelper()).getAvailableTables(this.connection.configuration.dbType, this.connection.configuration)
        .then(rsp => {
          this.$emit('onLoadingStatusChange', false)
          this.tables = rsp.data.data
          if ((this.schemas.length > 0) && (this.action.targetSchema !== undefined) &&
            (this.schemas.indexOf(this.action.targetSchema) < 0)) {
            this.action.targetSchema = this.schemas[0]
          }
          if ((this.tablesInSchema.length > 0) && (this.action.targetTable !== undefined) &&
            (this.tablesInSchema.filter(tbl => tbl.table === this.action.targetTable).length === 0)) {
            this.createTable = true
          }
        }).catch(err => {
          this.$emit('errorOccurred', err.response.data.error.message)
          this.$emit('onLoadingStatusChange', false)
        })
    },
    switchConnection: function () {
      this.connection = this.connections.find(conn => conn.id === this.action.connectionId)
      this.loadTables()
    },
    toggleCreateTable: function (state) {
      this.createTable = state
      if (state) {
        Vue.nextTick(_ => this.focusInputTable())
      }
    },
    focusInputTable: function () {
      this.$refs['inputTable'].focus()
    }
  }
}
</script>

<style scoped>

</style>
