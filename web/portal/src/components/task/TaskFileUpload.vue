<template>
  <div class="container" v-title data-title="Upload file to execute a task">
    <h3 class="col-8">
      <span>Upload file for Task {{ taskId }}</span>
    </h3>
    <div class="form-row" >
      <b-form-file type="file" class="form-control col-8" v-model="file"></b-form-file>
    </div>
    <div class="form-row" v-if="file !== undefined">
      <div class="col-3">Name: {{ file.name }}</div>
      <div class="col-3">Size: {{ file.size }}</div>
    </div>
    <div class="form-row" v-if="file !== undefined">
      <div class="col">Last modified at: {{ dateFormat(Date(file.lastModified), 'isoDateTime') }}</div>
    </div>
    <div class="form-row" v-if="file !== undefined">
      <div class="col">Type: {{ file.type }}</div>
    </div>
    <div class="form-row">
      <button class="btn btn-primary col-1" v-on:click="upload()">Upload</button>
      <button class="btn btn-info col-1" v-on:click="cancel()">Cancel</button>
      <button class="btn btn-info col-1" v-on:click="runtask()" v-if="execid !== null">Run</button>
    </div>
    <CsvAttributes v-if="fileType === 'csv'"
      v-bind:extraInfo="extraInfo">
    </CsvAttributes>
    <ExcelAttributes v-if="fileType === 'excel'"
      v-bind:file="file"
      v-bind:extraInfo="extraInfo">
    </ExcelAttributes>
    <UnsupportedFile v-if="(fileType !== undefined) && (['csv', 'excel'].indexOf(fileType) === -1)"
      v-bind:fileType="fileType">
    </UnsupportedFile>
    <div class="form-row">
      <fancy-grid-vue :config="gridConfig"></fancy-grid-vue>
    </div>
  </div>
</template>

<script>
import router from '../../router'
import CsvAttributes from './upload/CsvAttributes'
import ExcelAttributes from './upload/ExcelAttributes'
import UnsupportedFile from './upload/UnsupportedFile'
import Vue from 'vue'
import Toasted from 'vue-toasted'
import WorkspaceApiHelper from '@/apis/workspace.js'
import FancyGridVue from 'fancy-grid-vue'

Vue.use(Toasted)
const dateFormat = require('dateformat')

export default {
  name: 'TaskFileUpload',
  components: {
    CsvAttributes,
    ExcelAttributes,
    UnsupportedFile,
    FancyGridVue
  },
  data () {
    return {
      uploads: [{id: 1}],
      workspaceId: this.$route.params.workspaceId,
      taskId: this.$route.params.id,
      task: {},
      file: undefined,
      extraInfo: {},
      execid: null,
      gridConfig: {
        title: 'Vue with FancyGrid',
        theme: 'gray',
        width: 700,
        height: 400,
        data: [],
        resizable: true,
        defaults: {
          type: 'string',
          width: 100,
          sortable: true,
          editable: true,
          resizable: true
        },
        selModel: 'rows',
        trackOver: true,
        columns: [
          {'type': 'select'}
        ]
      }
    }
  },
  computed: {
    fileType: function () {
      if (this.file === undefined) {
        return undefined
      }
      let parts = this.file.name.toLowerCase().split('.')
      if (parts.length < 2) {
        return undefined
      } else if (parts[1] === 'csv') {
        return 'csv'
      } else if (['xls', 'xlsx'].indexOf(parts[1]) >= 0) {
        return 'excel'
      } else {
        return parts[1]
      }
    }
  },
  methods: {
    addFile: function () {
      this.uploads.push({id: this.uploads.length})
    },
    removeFile: function (index) {
      this.uploads.splice(index, 1)
    },
    cancel: function () {
      if (confirm('Are you SURE to cancel uploading?')) {
        router.back()
      }
    },
    dateFormat: function (date, fmt) {
      return dateFormat(date, fmt)
    },
    upload: function () {
      console.log(this.file)
      if (this.file === undefined) {
        return
      }
      (new WorkspaceApiHelper()).uploadTaskFile(this.workspaceId, this.taskId, this.file, this.extraInfo)
        .then(rsp => {
          this.execid = rsp.data.data.id
          this.readdata()
        })
    },
    runtask: function () {
      (new WorkspaceApiHelper()).runExec(this.workspaceId, this.execid).then(rsp => {
        console.log(rsp.data)
        Vue.toasted.show('file uploaded, execution is queued.', {
          position: 'bottom-center',
          duration: 1000,
          type: 'success',
          onComplete: function () {
            history.back()
          }
        })
      }).catch(err => console.log(err))
    },
    readdata: function () {
      (new WorkspaceApiHelper()).readExecutionData(this.workspaceId, this.execid).then(rsp => {
        this.gridConfig.columns = rsp.data.data.columns
        this.gridConfig.data = rsp.data.data.rows
      })
    }
  }
}
</script>

<style scoped>
.filename {
  color: white
}
</style>
