<template>
  <div style="margin-left: 5px; border: 1px solid grey">
    <div class="form-row">
      <div class="form-group col-6">
        <label for="conname">Name</label>
        <input type="text" class="form-control" id="conname" v-model="action.name" />
      </div>
      <div class="form-group col-5">
        <label for="typeSelect">Type</label>
        <select class="form-control" id="typeSelect" v-model="action.type">
          <option v-for="(name, value) in actionTypes" v-bind:key="value" v-bind:value="value">{{ name }}</option>
          <option disabled value=undefined>Choose Action Type</option>
        </select>
      </div>
      <div class="form-group col-1">
        <label for="rmv" class="disabled">Action {{ actionIndex }}</label>
        <button class="btn btn-info" v-on:click="removeAction()" id="rmv">Remove</button>
      </div>
    </div>
    <div class="form-row">
      <div class="form-group col-6">
        <label :for="'condesp' + actionIndex">Description</label>
        <textarea class="form-control" :id="'condesp' + actionIndex" v-model="action.description"></textarea>
      </div>
      <div class="form-group col-3">
        <div class="form-check">
          <input class="form-check-input" type="checkbox" :id="'continueNext' + actionIndex" v-model="action.extra.continueNextWhenError">
          <label class="form-check-label" :for="'continueNext' + actionIndex">
            Continue Next Action When Error Occurs
          </label>
        </div>
      </div>
      <div class="col-1 verifyArea">
        <img src="../../assets/gear-loading.gif" v-show="loading" />
      </div>
    </div>
    <TaskActionIngest v-if="action.type == 1"
      v-bind:action="action"
      v-bind:workspaceId="workspaceId"
      v-bind:actionIndex="actionIndex"
      v-on:onLoadingStatusChange="updateLoadingStatus"
      v-on:errorOccurred="onErrorOccurred"
      ></TaskActionIngest>
    <TaskActionVerify v-if="action.type == 2"
      v-bind:action="action"
      v-bind:workspaceId="workspaceId"
      v-bind:actionIndex="actionIndex"
      v-on:onLoadingStatusChange="updateLoadingStatus"
      v-on:errorOccurred="onErrorOccurred"
      ></TaskActionVerify>
    <TaskActionDistribute v-if="action.type == 3"
      v-bind:action="action"
      v-bind:workspaceId="workspaceId"
      v-bind:actionIndex="actionIndex"
      v-on:onLoadingStatusChange="updateLoadingStatus"
      v-on:errorOccurred="onErrorOccurred"
      ></TaskActionDistribute>
  </div>
</template>

<script>
import TaskActionIngest from './TaskActionIngest'
import TaskActionVerify from './TaskActionVerify'
import TaskActionDistribute from './TaskActionDistribute'

export default {
  name: 'TaskAction',
  components: {
    TaskActionIngest,
    TaskActionVerify,
    TaskActionDistribute
  },
  props: {
    action: Object,
    workspaceId: Number,
    actionIndex: Number
  },
  data () {
    return {
      actionTypes: {
        1: 'Databse Ingest',
        2: 'Data Verify',
        3: 'Data Distribute'
      },
      loading: false
    }
  },
  mounted: function () {
    //
  },
  methods: {
    updateLoadingStatus: function (isLoading) {
      this.loading = isLoading
    },
    removeAction: function (actionIndex) {
      if (confirm('Are you SURE to DELETE this action??')) {
        this.$emit('removeAction', this.actionIndex)
      }
    },
    onErrorOccurred: function (message) {
      this.$emit('onError', message)
    }
  }
}
</script>

<style scoped>
.verifyArea img {
  width: 25px;
}
</style>
