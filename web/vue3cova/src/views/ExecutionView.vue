<script lang="ts">
import executionApiHelper from '@/api/executionapi';
import type { WorkspaceWithUser } from '@/api/workspaceapi';
import { EVENT_REMOTE_API_ERROR } from '@/entities/events';
import type { ExecutionStatus, TaskExecution } from '@/entities/execution/taskexecution';
import { identityStore } from '@/stores/identitystore';
import { eventBus } from '@/util/eventbus';
import { defineComponent, reactive, type PropType } from 'vue';
import { RouterLink } from 'vue-router';

export default defineComponent({
  props: {
    workspace: {
      type: Object as PropType<WorkspaceWithUser>,
      required: true
    }
  },
  mounted() {
    let user = identityStore().currentIdentity();
    executionApiHelper.getExecutions(user, this.workspace.workspace.id).then(rsp => {
      this.executions = rsp.data.executions;
      this.pos = rsp.data.start;
      this.totalCount = rsp.data.count;
    }).catch(err => {
      eventBus.send(EVENT_REMOTE_API_ERROR, err);
    });
  },
  setup(props) {
    return reactive({
      executions: [] as TaskExecution[],
      totalCount: 0,
      pos: 0
    });
  },
  methods: {
    formatTime(d: number | null): string {
      return d === null ? "" : new Date(d).toLocaleString();
    },
    formatStatus(s: ExecutionStatus): string {
      switch (s) {
        case 0:
        case "0":
          return "ready";
        case 1:
        case "1":
          return "running";
        case 2:
        case "2":
          return "succeeded";
        case -1:
        case "-1":
          return "failed";
      }
    }
  },
  components: { RouterLink }
})
</script>

<template>
  <n-list style="width: 100%">
    <template #header>
      <n-list-item>
        <n-grid :cols="5" class="execheader">
          <n-gi>Task Name</n-gi>
          <n-gi>Started At</n-gi>
          <n-gi>Ended At</n-gi>
          <n-gi>Result</n-gi>
          <n-gi>Detail</n-gi>
        </n-grid>
      </n-list-item>
    </template>
    <n-list-item v-for="exec in executions" :key="exec.id">
      <n-grid :cols="5">
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
        <n-gi>
          <RouterLink :to='{ name: "taskExecutionDetail", params: { workspaceid: workspace.workspace.id, id: exec.id } }'>
            detail
          </RouterLink>
        </n-gi>
      </n-grid>
    </n-list-item>
  </n-list>
</template>

<style scoped>
.execheader {
  font-display: initial;
  font-style: italic;
  font-size: larger;
}
</style>
