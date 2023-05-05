<script lang="ts">
import executionApiHelper from '@/api/executionapi';
import type { WorkspaceWithUser } from '@/api/workspaceapi';
import { EVENT_REMOTE_API_ERROR } from '@/entities/events';
import type { ExecutionStatus, TaskExecution } from '@/entities/execution/taskexecution';
import { identityStore } from '@/stores/identitystore';
import { eventBus } from '@/util/eventbus';
import { defineComponent, reactive, type PropType } from 'vue';

export default defineComponent({
  props: {
    workspace: {
      type: Object as PropType<WorkspaceWithUser>,
      required: true
    }
  },
  mounted() {
    let user = identityStore().currentIdentity()
    executionApiHelper.getExecutions(user, this.workspace.workspace.id).then(rsp => {
      this.executions = rsp.data.executions
      this.pos = rsp.data.start
      this.totalCount = rsp.data.count
    }).catch(err => {
      eventBus.send(EVENT_REMOTE_API_ERROR, err)
    })
  },
  setup(props) {
    return reactive({
      executions: [] as TaskExecution[],
      totalCount: 0,
      pos: 0
    })
  },
  methods: {
    formatTime(d: number | null): string {
      return d === null ? "" : new Date(d).toDateString()
    },
    formatStatus(s: ExecutionStatus): string {
      switch (s) {
        case 0:
        case "0":
          return "ready"
        case 1:
        case "1":
          return "running"
        case 2:
        case "2":
          return "succeeded"
        case -1:
        case "-1":
          return "failed"
      }
    }
  }
})
</script>

<template>
  <n-list style="width: 100%">
    <n-list-item v-for="exec in executions" :key="exec.id">
      <n-grid :cols="4">
        <n-gi>
          {{ exec.task.name }}
        </n-gi>
        <n-gi>
          {{ formatTime(exec.startTime) }}
        </n-gi>
        <n-gi>
          {{ formatTime(exec.endTime) }}
        </n-gi>
        <n-gi>
          {{ formatStatus(exec.status) }}
        </n-gi>
      </n-grid>
    </n-list-item>
  </n-list>
</template>

<style scoped></style>
