<template>
  <div>
    <div class="form-row">
      <div class="col-6 form-group">
        <div class="form-check">
          <input class="form-check-input" type="checkbox" id="mincheck" v-bind:checked="minCheck" v-on:click="toggleMin()">
          <label class="form-check-label" for="mincheck">
            Minimum Value
          </label>
        </div>
      </div>
      <div class="col-6 form-group">
        <div class="form-check">
          <input class="form-check-input" type="checkbox" id="maxcheck" v-bind:checked="maxCheck" v-on:click="toggleMax()">
          <label class="form-check-label" for="maxcheck">
            Maximum Value
          </label>
        </div>
      </div>
    </div>
    <div class="form-row">
      <div class="col-2 form-group">
        <div class="form-check" v-if="minCheck">
          <input class="form-check-input" type="checkbox" id="mininclusive" v-model="config.lowerBound.inclusive">
          <label class="form-check-label" for="mininclusive">
            Inclusive
          </label>
        </div>
      </div>
      <div class="col-4">
        <datetime format="YYYY/MM/DD H:i:s" v-if="minCheck" v-model="lowerDateTime"></datetime>
      </div>
      <div class="col-2 form-group">
        <div class="form-check" v-if="maxCheck">
          <input class="form-check-input" type="checkbox" id="maxinclusive" v-model="config.upperBound.inclusive">
          <label class="form-check-label" for="maxinclusive">
            Inclusive
          </label>
        </div>
      </div>
      <div class="col-4">
        <datetime format="YYYY/MM/DD H:i:s" v-if="maxCheck" v-model="upperDateTime"></datetime>
      </div>
    </div>
  </div>
</template>

<script>
import Vue from 'vue'
import datetime from 'vuejs-datetimepicker'
const dateFormat = require('dateformat')

export default {
  name: 'TimeRangeVerifier',
  components: {
    datetime
  },
  props: {
    config: Object
  },
  data () {
    return {
      minCheck: false,
      maxCheck: false,
      default: {
        min: { value: Date.now(), inclusive: false },
        max: { value: Date.now(), inclusive: false }
      },
      lowerDateTime: dateFormat(Date.now(), 'yyyy/mm/dd HH:MM:ss'),
      upperDateTime: dateFormat(Date.now(), 'yyyy/mm/dd HH:MM:ss')
    }
  },
  watch: {
    lowerDateTime: function (newValue, oldValue) {
      if (this.minCheck) {
        this.config.lowerBound.value = Date.parse(newValue)
      }
    },
    upperDateTime: function (newValue, oldValue) {
      if (this.maxCheck) {
        this.config.upperBound.value = Date.parse(newValue)
      }
    }
  },
  mounted: function () {
    if ((this.config.lowerBound !== undefined) && (this.config.lowerBound !== null)) {
      this.minCheck = true
      this.default.min = this.config.lowerBound
    }
    if ((this.config.upperBound !== undefined) && (this.config.upperBound !== null)) {
      this.maxCheck = true
      this.default.max = this.config.upperBound
    }
    this.config.validator = function (configuration) {
      let ub = (configuration.upperBound === undefined) || (configuration.upperBound === null) ? null : configuration.upperBound
      let lb = (configuration.lowerBound === undefined) || (configuration.lowerBound === null) ? null : configuration.lowerBound
      if ((ub === null) && (lb === null)) {
        return 'no boundary is defined'
      }
      if ((ub !== null) && (ub.value === undefined)) {
        return 'upper boundary value is not defined'
      }
      if ((lb !== null) && (lb.value === undefined)) {
        return 'lower boundary value is not defined'
      }
      if ((ub !== null) && (lb !== null)) {
        if (isNaN(parseFloat(ub.value))) {
          return 'upper boundary is not a Date'
        }
        if (isNaN(parseFloat(lb.value))) {
          return 'lower boundary is not a Date'
        }
        if (parseFloat(ub.value) < parseFloat(lb.value)) {
          return 'lower boundary date is later than upper boundary date'
        }
      }
      return true
    }
    if (this.config.lowerBound !== undefined) {
      this.lowerDateTime = dateFormat(this.config.lowerBound.value, 'yyyy/mm/dd HH:MM:ss')
    }
    if (this.config.upperBound !== undefined) {
      this.upperDateTime = dateFormat(this.config.upperBound.value, 'yyyy/mm/dd HH:MM:ss')
    }
  },
  methods: {
    toggleMax: function () {
      this.maxCheck = !this.maxCheck
      console.log(`maxCheck ${this.maxCheck}`)
      if (this.maxCheck) {
        Vue.set(this.config, 'upperBound', this.default.max)
      } else {
        this.default.max = this.config.upperBound
        Vue.set(this.config, 'upperBound', null)
      }
    },
    toggleMin: function () {
      this.minCheck = !this.minCheck
      console.log(`minCheck ${this.minCheck}`)
      if (this.minCheck) {
        Vue.set(this.config, 'lowerBound', this.default.min)
      } else {
        this.default.min = this.config.lowerBound
        Vue.set(this.config, 'lowerBound', null)
      }
    }
  }
}
</script>

<style scoped>

</style>
