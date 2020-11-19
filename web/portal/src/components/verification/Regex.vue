<template>
  <div>
    <div class="form-group">
      <div class="form-check form-check-inline">
        <input class="form-check-input" type="checkbox" id="ignoreCase" v-model="config.ignoreCase">
        <label class="form-check-label" for="ignoreCase">
          Ignore Case
        </label>
      </div>
      <div class="form-check form-check-inline">
        <input class="form-check-input" type="checkbox" id="dotAll" v-model="config.dotAll">
        <label class="form-check-label" for="dotAll">
          Dot(.) match all
        </label>
      </div>
    </div>
    <div style="margin: 0px 5px 10px 5px">
      <textarea class="col" v-model="config.pattern" rows="2" v-on:change="codesChange()"></textarea>
    </div>
    <button class="btn btn-info" style="margin-left: 5px" v-on:click="evaluate()">Evaluate</button>
  </div>
</template>

<script>
import Vue from 'vue'
import EvaluateApiHelper from '@/apis/evaluate.js'
import Toasted from 'vue-toasted'

Vue.use(Toasted)

export default {
  name: 'RegexVerifier',
  props: {
    config: Object
  },
  mounted: function () {
    this.config.validator = function (configuration) {
      if ((configuration.pattern === undefined) || (configuration.pattern.trim() === '')) {
        return 'regular expression is required'
      }
      return configuration.evaluated === true ? configuration.evaluated : 'regular expression not evaluated'
    }
  },
  data () {
    return {}
  },
  methods: {
    evaluate: function () {
      if (this.config.pattern.trim === '') {
        this.$toasted.show('pattern is empty', {
          position: 'bottom-center',
          duration: 1000,
          type: 'warning'
        })
        return
      }
      (new EvaluateApiHelper()).evaluateRegex(this.config.pattern).then(rsp => {
        this.config.evaluated = true
        this.$toasted.show('evaluation succeeded', {
          position: 'bottom-center',
          duration: 1000,
          type: 'success'
        })
      }).catch(err => {
        this.config.evaluated = false
        this.$toasted.show(err.response.data.error.message, {
          position: 'bottom-center',
          duration: 1000,
          type: 'danger'
        })
      })
    },
    codesChange: function () {
      this.config.evaluated = false
    }
  }
}
</script>

<style scoped>

</style>
