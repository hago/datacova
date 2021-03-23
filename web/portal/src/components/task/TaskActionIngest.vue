<template>
  <div>
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
        <select v-model="action.ingestOptions.targetSchema" class="form-control" id="connection">
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
        <select v-model="action.ingestOptions.targetTable" class="form-control" id="selectTable" v-if="!createTable">
          <option v-for="(item, index) in tablesInSchema" v-bind:key="index" v-bind:value="item.tableName">
            {{ item.tableName }}
          </option>
          <option disabled value=undefined>Choose Table</option>
        </select>
        <input type="text" v-model="action.ingestOptions.targetTable" class="form-control" id="inputTable"
          v-if="createTable" ref="inputTable" />
      </div>
    </div>
    <div class="form-row margin-left: 5px">
      <div class="form-group col-3">
        <div class="form-check">
          <input class="form-check-input" type="checkbox" id="addBatch" v-model="action.ingestOptions.addBatch">
          <label class="form-check-label" for="addBatch">
            Add batch column
          </label>
        </div>
      </div>
      <div class="form-group col-3">
        <input type="text" v-model="action.ingestOptions.batchColumnName" class="form-control" id="batchColumnName"
          ref="batchColumnName" :disabled="!action.ingestOptions.addBatch" placeholder="BatchId" />
      </div>
      <div class="form-group col-3">
        <div class="form-check">
          <input class="form-check-input" type="checkbox" id="clearTable" v-model="action.ingestOptions.clearTable">
          <label class="form-check-label" for="clearTable">
            Purge table
          </label>
        </div>
      </div>
      <div class="form-group col-3">
        <div class="form-check">
          <input class="form-check-input" type="checkbox" id="createTable" v-model="action.ingestOptions.createTableIfNeeded">
          <label class="form-check-label" for="createTable">
            Create table automatically if not existed
          </label>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import Vue from 'vue'

import WorkspaceApiHelper from '@/apis/workspace.js'
import ConnectionApiHelper from '@/apis/connection.js'

export default {
  name: 'TaskActionIngest',
  props: {
    action: Object,
    workspaceId: Number
  },
  data () {
    return {
      connections: [],
      connection: {},
      tables: [],
      createTable: false
    }
  },
  mounted: function () {
    if (this.action.ingestOptions === undefined) {
      this.action.ingestOptions = {}
    }
    this.action.validator = function (action) {
      // console.log(action)
      if (action.connectionId === undefined) {
        return 'connection is required'
      }
      if ((action.ingestOptions.targetTable === undefined) || (action.ingestOptions.targetTable.trim() === '')) {
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
      (new ConnectionApiHelper()).getConnectionTables(this.workspaceId, this.connection.id)
        .then(rsp => {
          this.$emit('onLoadingStatusChange', false)
          this.tables = rsp.data.data
          if ((this.schemas.length > 0) && (this.action.ingestOptions.targetSchema !== undefined) &&
            (this.schemas.indexOf(this.action.ingestOptions.targetSchema) < 0)) {
            this.action.ingestOptions.targetSchema = this.schemas[0]
          }
          if ((this.action.ingestOptions.targetTable !== undefined) &&
            (this.tablesInSchema.filter(tbl => tbl.table === this.action.ingestOptions.targetTable).length === 0)) {
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
