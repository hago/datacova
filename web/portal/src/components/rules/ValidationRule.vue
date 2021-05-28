<template>
  <div></div>
</template>

<script>
import WorkspaceApiHelper from '@/apis/workspace.js'
// import router from '../../router'

export default {
  name: 'ValidationRule',
  props: {
    workspace: Object,
    wkid: Number,
    ruleid: String
  },
  data: function () {
    return {}
  },
  created: function () {
    if (this.workspace === undefined) {
      (new WorkspaceApiHelper().getWorkspace(this.wkid)).then(rsp => {
        this.workspace = rsp.data.data
      }).catch(err => console.log(err))
    }
  }
}
</script>

<style scoped>

</style>
