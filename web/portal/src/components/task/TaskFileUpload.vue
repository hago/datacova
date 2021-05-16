<template>
  <div class="container">
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

Vue.use(Toasted)
const dateFormat = require('dateformat')

export default {
  name: 'TaskFileUpload',
  components: {
    CsvAttributes,
    ExcelAttributes,
    UnsupportedFile
  },
  data () {
    return {
      uploads: [{id: 1}],
      workspaceId: this.$route.params.workspaceId,
      taskId: this.$route.params.id,
      task: {},
      file: undefined,
      extraInfo: {}
    }
  },
  created: function () {
    this.$root.$emit('onNeedLogin', user => {})
  },
  computed: {
    url: function () {
      return `/api/workspace/${this.workspaceId}/task/${this.taskId}/run`
    },
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
      (new WorkspaceApiHelper()).runTask(this.workspaceId, this.taskId, this.file, this.extraInfo)
        .then(rsp => {
          console.log(rsp.data)
          Vue.toasted.show('file uploaded, execution is queued.', {
            position: 'bottom-center',
            duration: 1000,
            type: 'success',
            onComplete: function () {
              history.back()
            }
          })
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
