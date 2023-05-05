<script lang="ts">
import { EVENT_NEW_WORKSPACE_DIALOG_CLOSED } from '@/entities/events';
import { identityStore } from '@/stores/identitystore';
import { eventBus } from '@/util/eventbus';
import { defineComponent, reactive } from 'vue';
import workspaceApiHelper from '@/api/workspaceapi'
import { EVENT_REMOTE_API_ERROR } from '@/entities/events';

export default defineComponent({
  setup() {
    return reactive({
      show: true,
      name: "",
      description: "",
    })
  },
  methods: {
    cancel() {
      this.show = false
      eventBus.send(EVENT_NEW_WORKSPACE_DIALOG_CLOSED, false)
    },
    update() {
      let user = identityStore().currentIdentity()
      workspaceApiHelper.newWorkspace(user, this.name, this.description).then(rsp => {
        this.show = false
        eventBus.send(EVENT_NEW_WORKSPACE_DIALOG_CLOSED, true)
      }).catch(err => {
        eventBus.send(EVENT_REMOTE_API_ERROR, err)
      })
    }
  }
})
</script>

<template>
  <n-modal :show="show">
    <n-card style="width: 600px" title="" size="small" :bordered="false" role="dialog" aria-modal="true">
      <n-card title="Workspace Name" :bordered="true">
        <n-input v-model:value="name" type="text"></n-input>
      </n-card>
      <n-card title="Description" :bordered="true">
        <n-input v-model:value="description" type="textarea"></n-input>
      </n-card>
      <div style="margin-top: 5px;">
        <n-button type="error" style="float: right;" @click="cancel">Cancel</n-button>
        <n-button type="primary" style="float: right; margin-right: 10px;" @click="update">Update</n-button>
      </div>
    </n-card>
  </n-modal>
</template>

<style scoped></style>
