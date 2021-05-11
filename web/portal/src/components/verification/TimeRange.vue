<template>
  <div>
    <div class="form-row">
      <div class="col-6 form-group">
        <div class="form-check">
          <input class="form-check-input" type="checkbox" :id="`mincheck_${index}`" v-bind:checked="minCheck" v-on:click="toggleMin()">
          <label class="form-check-label" :for="`mincheck_${index}`">
            Minimum Value
          </label>
        </div>
      </div>
      <div class="col-6 form-group">
        <div class="form-check">
          <input class="form-check-input" type="checkbox" :id="`maxcheck_${index}`" v-bind:checked="maxCheck" v-on:click="toggleMax()">
          <label class="form-check-label" :for="`maxcheck_${index}`">
            Maximum Value
          </label>
        </div>
      </div>
    </div>
    <div class="form-row">
      <div class="col-1 form-group">
        <div class="form-check" v-if="minCheck">
          <input class="form-check-input" type="checkbox" :id="`mininclusive_${index}`" v-model="config.lowerBound.inclusive">
          <label class="form-check-label" :for="`mininclusive_${index}`">
            Inclusive
          </label>
        </div>
      </div>
      <div class="col-3">
        <datetime format="YYYY/MM/DD H:i:s" v-if="minCheck" v-model="lowerDateTime"></datetime>
      </div>
      <div class="col-2">
        <select class="form-control" v-if="minCheck" v-model="lowertzname">
          <option v-for="timezone in timezones" v-bind:key="timezone.name" v-bind:value="timezone.name">{{ timezone.name }}</option>
        </select>
      </div>
      <div class="col-1 form-group">
        <div class="form-check" v-if="maxCheck">
          <input class="form-check-input" type="checkbox" :id="`maxinclusive_${index}`" v-model="config.upperBound.inclusive">
          <label class="form-check-label" :for="`maxinclusive_${index}`">
            Inclusive
          </label>
        </div>
      </div>
      <div class="col-3">
        <datetime format="YYYY/MM/DD H:i:s" v-if="maxCheck" v-model="upperDateTime"></datetime>
      </div>
      <div class="col-2">
        <select class="form-control" v-if="maxCheck" v-model="uppertzname">
          <option v-for="timezone in timezones" v-bind:key="timezone.name" v-bind:value="timezone.name">{{ timezone.name }}</option>
        </select>
      </div>
    </div>
  </div>
</template>

<script>
import Vue from 'vue'
import datetime from 'vuejs-datetimepicker'
import {loadtimezones, getLocalTimeOffset} from '@/apis/timezone.js'
const dateFormat = require('dateformat')

export default {
  name: 'TimeRangeVerifier',
  components: {
    datetime
  },
  props: {
    config: Object,
    index: Number
  },
  data () {
    return {
      minCheck: (this.config.lowerBound !== undefined) && (this.config.lowerBound !== null),
      maxCheck: (this.config.upperBound !== undefined) && (this.config.upperBound !== null),
      default: {
        min: {
          value: (this.config.lowerBound !== undefined) && (this.config.lowerBound !== null) ? this.config.lowerBound : Date.now(),
          inclusive: false
        },
        max: {
          value: (this.config.upperBound !== undefined) && (this.config.upperBound !== null) ? this.config.upperBound : Date.now(),
          inclusive: false
        }
      },
      timezones: [],
      lowertzname: '',
      uppertzname: '',
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
    },
    lowertzname: function (newValue, oldValue) {
      //
    },
    uppertzname: function (newValue, oldValue) {
      //
    }
  },
  created: function () {
    loadtimezones().then(data => {
      this.timezones = data
      let lo = getLocalTimeOffset()
      this.uppertzname = data.find(tz => tz.offset === lo).name
      this.lowertzname = data.find(tz => tz.offset === lo).name
    })
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
    if ((this.config.lowerBound !== undefined) && (this.config.lowerBound !== null)) {
      this.lowerDT = new Date(this.config.lowerBound.value)
      this.lowerDateTime = dateFormat(this.config.lowerBound.value, 'yyyy/mm/dd HH:MM:ss')
    }
    if ((this.config.upperBound !== undefined) && (this.config.upperBound !== null)) {
      this.upperDT = new Date(this.config.upperBound.value)
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
    },
    calculateTime: function (timestr, offset) {
      return Date.parse(timestr) + offset
    }
  }
}
</script>

<style scoped>

</style>
