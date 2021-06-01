<template>
  <div class="card bg-dark">
    <div class="card-header">
      <h5>
        <span>Validation Rules</span>
        <span class="clickable" style="float: right" v-on:click="addRule()" title="New Connection">+</span>
      </h5>
    </div>
    <div class="card-body">
      <ul class="list-group">
        <li class="list-group-item bg-dark" v-for="rule in rules" v-bind:key="rule.id">
          <span v-on:click="editRule(rule.id)" class="clickable">{{ rule.name }}</span>
          <span v-on:click="deleteRule(rule.id)" style="float: right" class="clickable"
            v-if="workspace.owner.id === loginStatus.user.id">-</span>
        </li>
      </ul>
    </div>
  </div>
</template>

<script>
import WorkspaceApiHelper from '@/apis/workspace.js'
import router from '../../router'

export default {
  name: 'RuleList',
  props: {
    workspace: Object,
    loginStatus: Object
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
  },
  methods: {
    addRule: function () {
      router.push({
        name: 'ValidationRule',
        params: {
          workspace: this.workspace,
          workspaceid: String(this.workspace.workspace.id),
          ruleid: 'new'
        }
      })
    },
    editRule: function (id) {
      router.push({
        name: 'ValidationRule',
        params: {
          workspace: this.workspace,
          workspaceid: String(this.workspace.workspace.id),
          ruleid: id
        }
      })
    },
    deleteRule: function (id) {
      //
    }
  }
}
</script>

<style scoped>

</style>
