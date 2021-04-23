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
      <div class="col-2 form-group">
        <div class="form-check" v-if="minCheck">
          <input class="form-check-input" type="checkbox" :id="`mininclusive_${index}`" v-model="config.lowerBound.inclusive">
          <label class="form-check-label" :for="`mininclusive_${index}`">
            Inclusive
          </label>
        </div>
      </div>
      <div class="col-4">
        <input class="form-control" type="text" v-if="minCheck" v-model="config.lowerBound.value" />
      </div>
      <div class="col-2 form-group">
        <div class="form-check" v-if="maxCheck">
          <input class="form-check-input" type="checkbox" :id="`maxinclusive_${index}`" v-model="config.upperBound.inclusive">
          <label class="form-check-label" :for="`maxinclusive_${index}`">
            Inclusive
          </label>
        </div>
      </div>
      <div class="col-4">
        <input class="form-control" type="text" v-if="maxCheck" v-model="config.upperBound.value"/>
      </div>
    </div>
  </div>
</template>

<script>
import Vue from 'vue'

export default {
  name: 'NumberRangeVerifier',
  props: {
    config: Object,
    index: Number
  },
  data () {
    return {
      minCheck: (this.config.lowerBound !== undefined) && (this.config.lowerBound !== null),
      maxCheck: (this.config.upperBound !== undefined) && (this.config.upperBound !== null),
      default: {
        min: (this.config.lowerBound !== undefined) && (this.config.lowerBound !== null) ? this.config.lowerBound : { value: 0, inclusive: false },
        max: (this.config.upperBound !== undefined) && (this.config.upperBound !== null) ? this.config.upperBound : { value: 0, inclusive: false }
      }
    }
  },
  mounted: function () {
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
          return 'upper boundary is not a number'
        }
        if (isNaN(parseFloat(lb.value))) {
          return 'lower boundary is not a number'
        }
        if (parseFloat(ub.value) < parseFloat(lb.value)) {
          return 'lower boundary value is larger than upper boundary'
        }
      }
      return true
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
