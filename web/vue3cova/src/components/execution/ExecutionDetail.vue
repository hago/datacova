<script lang="ts">
import executionApiHelper from '@/api/executionapi';
import { EVENT_REMOTE_API_ERROR } from '@/entities/events';
import { formatStatus, type ExecutionStatus, type TaskExecution } from '@/entities/execution/taskexecution';
import { identityStore } from '@/stores/identitystore';
import { eventBus } from '@/util/eventbus';
import { defineComponent, reactive } from 'vue';
import dayjs from 'dayjs'

export default defineComponent({
  props: {
    workspaceId: {
      type: Number,
      required: true
    },
    id: {
      type: Number,
      required: true
    }
  },
  setup(props) {
    return reactive({
      execution: null as TaskExecution | null
    })
  },
  mounted() {
    this.loadDetail()
  },
  methods: {
    loadDetail() {
      let user = identityStore().currentIdentity()
      executionApiHelper.getExecution(user, this.$props.id).then(rsp => {
        this.execution = rsp.data
      }).catch(err => {
        eventBus.send(EVENT_REMOTE_API_ERROR, err)
      })
    },
    formatTime(x: number | undefined | null): string {
      if ((x !== undefined) && (x !== null)) {
        return dayjs(x).format()
      } else {
        return ''
      }
    },
    showStatus(x: ExecutionStatus | undefined): string {
      return x === undefined ? "" : formatStatus(x)
    }
  }
})
</script>

<template>
  <n-grid :cols="4">
    <n-gi>id</n-gi>
    <n-gi class="content">{{ execution?.id }}</n-gi>
    <n-gi>Task Name</n-gi>
    <n-gi class="content">{{ execution?.task.name }}</n-gi>
    <n-gi>Status</n-gi>
    <n-gi class="content">{{ showStatus(execution?.status) }}</n-gi>
    <n-gi>Started At</n-gi>
    <n-gi class="content">{{ formatTime(execution?.startTime) }}</n-gi>
    <n-gi>End At</n-gi>
    <n-gi class="content">{{ formatTime(execution?.endTime) }}</n-gi>
  </n-grid>
</template>

<style scoped>
.content {
  color: aliceblue;
}
</style>
