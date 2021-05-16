<template>
  <div class="container-fluid" v-title data-title="Task">
    <div class="form-row">
      <div class="form-group col-6">
        <label for="conname">Name</label>
        <input type="text" class="form-control" id="conname" v-model="task.name" />
      </div>
      <div class="form-group col-6">
        <label for="locale">Language of Feedback</label>
        <select class="form-control" id="locale" v-model="task.extra.locale">
          <option v-for="(value, name) in languages" v-bind:key="name" v-bind:value="name">{{ value }}</option>
        </select>
      </div>
    </div>
    <div class="form-row">
      <div class="form-group col-6">
        <label for="condesp">Description</label>
        <textarea class="form-control" id="condesp" v-model="task.description"></textarea>
      </div>
    </div>
    <div class="form-row">
      <div class="col-6">
        Recipients
        <button class="btn btn-secondary clickable" style="float: right" v-on:click="addRecipient()">Add Recipient</button>
      </div>
      <div class="input-group col-2" v-for="(recipient, i) in task.extra.mailRecipients" v-bind:key="i">
        <div class="input-group-prepend">
          <div class="input-group-text">
            <img src="../../assets/remove.png" v-on:click="removeRecipient(i)" class="rmvbtn clickable" />
          </div>
        </div>
        <input v-model="task.extra.mailRecipients[i]" type="text" class="form-control" />
      </div>
    </div>
    <div class="form-row">
      <div class="col-6">
        CC Recipients
        <button class="btn btn-secondary clickable" style="float: right" v-on:click="addCCRecipient()">Add CC Recipient</button>
      </div>
      <div class="input-group col-2" v-for="(recipient, i) in task.extra.mailCCRecipients" v-bind:key="i">
        <div class="input-group-prepend">
          <div class="input-group-text">
            <img src="../../assets/remove.png" v-on:click="removeCCRecipient(i)" class="rmvbtn clickable" />
          </div>
        </div>
        <input v-model="task.extra.mailCCRecipients[i]" type="text" class="form-control" />
      </div>
    </div>
    <div class="form-row">
      <div class="col-6">
        BCC Recipients
        <button class="btn btn-secondary clickable" style="float: right" v-on:click="addBCCRecipient()">Add BCC Recipient</button>
      </div>
      <div class="input-group col-2" v-for="(recipient, i) in task.extra.mailBCCRecipients" v-bind:key="i">
        <div class="input-group-prepend">
          <div class="input-group-text">
            <img src="../../assets/remove.png" v-on:click="removeBCCRecipient(i)" class="rmvbtn clickable" />
          </div>
        </div>
        <input v-model="task.extra.mailBCCRecipients[i]" type="text" class="form-control" />
      </div>
    </div>
    <div class="form-row">
      <h5 class="col-6">
        Actions
        <span class="clickable" style="float: right" title="Add action" v-on:click="addAction()">+</span>
      </h5>
    </div>
    <div>
      <b-list-group>
        <b-list-group-item class="bg-dark" v-for="(action, index) in task.actions" v-bind:key="index">
          <TaskAction
            v-bind:action="action"
            v-bind:actionIndex="index"
            v-bind:workspaceId="$route.params.workspaceId"
            v-on:onError="errorOccurred"
            v-on:removeAction="removeAction"
            ></TaskAction>
        </b-list-group-item>
        <b-list-group-item class="bg-dark" v-if="task.actions.length == 0">No Actions</b-list-group-item>
      </b-list-group>
    </div>
    <div style="margin: 2px">
      <button class="btn btn-primary" v-on:click="save()" v-if="!readonly">Save</button>
      <button class="btn btn-primary disabled" v-if="readonly">Save</button>
      <button class="btn btn-warning" v-on:click="cancel()">Cancel</button>
    </div>
    <div class="form-row" style="margin-top: 15px">
      <div class="col-7" v-if="errorMessage !== undefined">
        <h3><span class="badge badge-danger errorMessage">{{ errorMessage }}</span></h3>
      </div>
    </div>
  </div>
</template>

<script>
import Vue from 'vue'
import Router from '../../router'
import TaskAction from './TaskAction'
import Toasted from 'vue-toasted'

import WorkspaceApiHelper from '@/apis/workspace.js'

Vue.use(Toasted)

export default {
  name: 'Task',
  props: {},
  components: {
    TaskAction
  },
  data () {
    return {
      task: {
        id: isNaN(parseInt(this.$route.params.id)) ? 0 : parseInt(this.$route.params.id),
        actions: [],
        extra: {
          mailRecipients: [],
          mailCCRecipients: [],
          mailBCCRecipients: []
        },
        workspaceId: this.$route.params.workspaceId
      },
      readonly: false,
      errorMessage: undefined,
      languages: {
        en_US: 'English',
        zh_CN: 'Chinese'
      }
    }
  },
  created: function () {
    this.$root.$emit('onNeedLogin', user => {
      this.loadTask()
    })
  },
  methods: {
    loadTask: function () {
      if (this.task.id === 0) {
        return
      }
      (new WorkspaceApiHelper()).getTask(this.task.workspaceId, this.task.id).then(rsp => {
        this.task = rsp.data.data
      })
    },
    addAction: function () {
      this.task.actions.push({
        extra: {}
      })
    },
    removeAction: function (index) {
      this.task.actions.splice(index, 1)
    },
    save: function () {
      if (!this.validate()) {
        return
      }
      (new WorkspaceApiHelper()).updateTask(this.task).then(rsp => {
        if (rsp.data.code === 0) {
          // this.task = rsp.data.data
          this.$toasted.show('Task Updated Successfully', {
            position: 'bottom-center',
            duration: 1000,
            type: 'success',
            onComplete: _ => {
              Router.back()
            }
          })
        } else {
          this.errorMessage = rsp.data.error.message
        }
      }).catch(err => {
        this.errorMessage = err.response.data.error.message
      })
    },
    validate: function () {
      if ((this.task.name === undefined) || (this.task.name.trim() === '')) {
        this.errorMessage = 'name is required'
        return false
      }
      if (this.task.actions.length === 0) {
        this.errorMessage = 'no action defined'
        return false
      }
      for (let i in this.task.actions) {
        let action = this.task.actions[i]
        if (action.validator === undefined) {
          this.errorMessage = `no validator found for action ${i}`
          return false
        }
        if ((action.name === undefined) || (action.name.trim() === '')) {
          this.errorMessage = `name is required for action ${i}`
          return false
        }
        let r = action.validator(action)
        if (r === true) {
          this.errorMessage = ''
        } else {
          this.errorMessage = `error in action ${i}: ${r}`
          return false
        }
      }
      return true
    },
    cancel: function () {
      if (confirm('Are you sure to discard your modifications?')) {
        Router.back()
      }
    },
    errorOccurred: function (msg) {
      this.errorMessage = msg
    },
    addRecipient: function () {
      this.task.extra.mailRecipients.push('')
    },
    removeRecipient: function (i) {
      this.task.extra.mailRecipients.splice(i, 1)
    },
    addCCRecipient: function () {
      this.task.extra.mailCCRecipients.push('')
    },
    removeCCRecipient: function (i) {
      this.task.extra.mailCCRecipients.splice(i, 1)
    },
    addBCCRecipient: function () {
      this.task.extra.mailBCCRecipients.push('')
    },
    removeBCCRecipient: function (i) {
      this.task.extra.mailBCCRecipients.splice(i, 1)
    }
  }
}
</script>

<style scoped>
.rmvbtn {
  width: 20px
}
</style>
