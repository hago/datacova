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
  </div>
</template>

<script>
import router from '../../router'

import WorkspaceApiHelper from '@/apis/workspace.js'
const dateFormat = require('dateformat')

export default {
  name: 'TaskFileUpload',
  data () {
    return {
      uploads: [{id: 1}],
      workspaceId: this.$route.params.workspaceId,
      taskId: this.$route.params.id,
      task: {},
      file: undefined
    }
  },
  mounted: function () {
    //
  },
  computed: {
    url: function () {
      return `/api/workspace/${this.workspaceId}/task/${this.taskId}/run`
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
      let extra = {a: 1, b: 'c'};
      (new WorkspaceApiHelper()).runTask(this.workspaceId, this.taskId, this.file, extra)
        .then(rsp => {
          console.log(rsp.data)
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
