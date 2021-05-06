<template>
  <div>
    <div class="row" v-if="succeeded">
      <div class="col">
        <span class="linecount">{{ detail.lineCount}}</span> rows inserted in <span class="timeused">{{ timeused }}</span>.
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
    </div>
  </div>
</template>

<script>
import {formatDuration, objectExists} from '@/utils.js'

export default {
  props: {
    detail: Object,
    actiondetail: Object
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
    }
  },
  data: function () {
    return {}
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
</style>
