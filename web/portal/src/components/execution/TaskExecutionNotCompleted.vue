<template>
  <div style="margin: 15px">
    <h2>
      <span>
        The execution with id {{ execution.id }} of task
        <a href="javascript:void(0);" v-on:click="gotoTask()">{{ execution.task.name }}</a> is
      </span>
      <span v-if="execution.status == 0"> created and waiting in queue.</span>
      <span v-if="execution.status == 1"> running.</span>
    </h2>
  </div>
</template>

<script>
export default {
  name: 'TaskExecutionNotCompleted',
  props: {
    execution: Object
  },
  data: function () {
    return {}
  }
}
</script>

<style scoped>

</style>
