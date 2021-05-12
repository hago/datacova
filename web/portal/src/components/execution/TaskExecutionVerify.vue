<template>
  <div>
    <div  v-if="succeeded">
      <div class="row">
        <div class="col">
          <span class="linecount">{{ detail.lineCount}}</span> rows verified in <span class="timeused">{{ timeused }}</span>.
        </div>
      </div>
      <div class="row" v-if="hasDataMessage">
        <h4 class="col">Following data failed to pass verification</h4>
      </div>
      <div class="row title" v-if="hasDataMessage">
        <div class="col-1">Line</div>
        <div class="col-2">Field</div>
        <div class="col-4">Value</div>
        <div class="col-5">Description</div>
      </div>
      <div class="row" v-for="(message, i) in flatDataMessages" v-bind:key="i">
        <div class="col-1">{{ message.line }}</div>
        <div class="col-2">{{ message.field }}</div>
        <div class="col-4 error">{{ message.value }}</div>
        <div class="col-5">{{ message.descriptionExpected }}</div>
      </div>
    </div>
    <div class="row" v-if="!succeeded">
      <div class="col">
        Time used: <span class="timeused">{{ timeused }}</span>.
      </div>
    </div>
    <div class="row" v-if="!succeeded">
      <div class="col-1">Error detail: </div>
      <div class="col error">
        {{ errormessage }}
      </div>
    </div>  </div>
</template>

<script>
import {formatDuration, objectExists} from '@/utils.js'

export default {
  name: 'TaskExecutionVerify',
  props: {
    detail: Object,
    actiondetail: Object
  },
  data: function () {
    return {}
  },
  computed: {
    succeeded: function () {
      return this.actiondetail.error === null
    },
    timeused: function () {
      return formatDuration(this.actiondetail.endTime - this.actiondetail.startTime)
    },
    errormessage: function () {
      if (this.actiondetail.error === null) {
        return null
      }
      let err = this.actiondetail.error
      if (objectExists(err.cause)) {
        return err.cause.detailMessage
      } else {
        return err.detailMessage
      }
    },
    hasDataMessage: function () {
      return Object.keys(this.actiondetail.dataMessages).length > 0
    },
    flatDataMessages: function () {
      let flat = []
      let keys = Object.keys(this.actiondetail.dataMessages).sort((a, b) => a - b)
      keys.forEach(line => {
        // console.log(line)
        let lineList = this.actiondetail.dataMessages[line]
        lineList.forEach(message => {
          flat.push(Object.assign({}, message, {line: line}))
        })
      })
      return flat
    }
  }
}
</script>

<style scoped>
.linecount {
  color: cyan;
  font-weight: bold;
  font-style: italic;
}
.timeused {
  color: cyan;
  font-weight: bold;
  font-style: italic;
}
.error {
  color: red;
}
.title {
  color: green
}
</style>
