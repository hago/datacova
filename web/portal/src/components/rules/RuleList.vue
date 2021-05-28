<template>
  <div></div>
</template>

<script>
import WorkspaceApiHelper from '@/apis/workspace.js'

export default {
  name: 'RuleList',
  props: {
    workspace: Object
  },
  data: function () {
    return {
      rules: []
    }
  },
  created: function () {
    (new WorkspaceApiHelper()).getRules(this.workspace.workspace.id).then(rsp => {
      this.rules = rsp.data.data
    }).catch(err => console.log(err))
  }
}
</script>

<style scoped>

</style>
