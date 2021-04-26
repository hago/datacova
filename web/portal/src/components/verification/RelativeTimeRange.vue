<template>
  <div>
    <div class="form-row">
      <div class="col-6 form-group">
        <div class="form-check">
          <input class="form-check-input" type="checkbox" :id="`mincheck_${index}`" v-bind:checked="minCheck" v-on:click="toggleMin()">
          <label class="form-check-label" :for="`mincheck_${index}`">
            Minimum Date
          </label>
        </div>
      </div>
    </div>
    <RelativeTimeBoundary v-if="minCheck"
      v-bind:boundary="config.lowerBound"
      v-bind:index="index"
      ></RelativeTimeBoundary>
    <div class="form-row">
      <div class="col-6 form-group">
        <div class="form-check">
          <input class="form-check-input" type="checkbox" :id="`maxCheck_${index}`" v-bind:checked="maxCheck" v-on:click="toggleMax()">
          <label class="form-check-label" :for="`maxCheck_${index}`">
            Maximum Date
          </label>
        </div>
      </div>
    </div>
    <RelativeTimeBoundary v-if="maxCheck"
      v-bind:boundary="config.upperBound"
      v-bind:index="index"
      ></RelativeTimeBoundary>
  </div>
</template>

<script>
import Vue from 'vue'
import datetime from 'vuejs-datetimepicker'
import RelativeTimeBoundary from '@/components/verification/RelativeTime.vue'
const dateFormat = require('dateformat')

export default {
  name: 'RelativeTimeRangeVerifier',
  components: {
    datetime,
    RelativeTimeBoundary
  },
  props: {
    config: Object,
    index: Number
  },
  data () {
    return {
      minCheck: false,
      maxCheck: false,
      default: {
        min: {
          inclusive: false,
          reference: 'Now',
          timeDiff: {
            month: 0,
            day: 0,
            hour: 0
          }
        },
        max: {
          inclusive: false,
          reference: 'Now',
          timeDiff: {
            month: 0,
            day: 0,
            hour: 0
          }
        }
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
      if ((ub !== null) && (ub.reference === undefined)) {
        return 'upper boundary value is not defined'
      }
      if ((lb !== null) && (lb.reference === undefined)) {
        return 'lower boundary value is not defined'
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
