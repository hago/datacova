<template>
  <div>
    <div class="form-row">
      <div class="form-inline col-10">
        <div class="form-check">
          <input class="form-check-input" type="checkbox" :id="`inclusive_${index}`" v-model="boundary.inclusive">
          <label class="form-check-label" :for="`inclusive_${index}`">
            Inclusive
          </label>
        </div>
        <div class="input-group col-2 mb-2 mr-sm-2">
          <input class="form-control" type="number" v-model="monthDiff" />
          <div class="input-group-append">
            <div class="input-group-text">Month{{ monthDiff > 1 ? 's' : '' }}</div>
          </div>
        </div>
        <div class="input-group col-2 mb-2 mr-sm-2">
          <input class="form-control" type="number" v-model="dayDiff" />
          <div class="input-group-append">
            <div class="input-group-text">Day{{ dayDiff > 1 ? 's' : '' }}</div>
          </div>
        </div>
        <div class="input-group col-2 mb-2 mr-sm-2">
          <input class="form-control" type="number" v-model="hourDiff" />
          <div class="input-group-append">
            <div class="input-group-text">Hour{{ hourDiff > 1 ? 's' : '' }}</div>
          </div>
        </div>
        <select class="form-control col-2 mb-2" v-model="direction">
          <option v-for="(v, k) in directions" v-bind:key="v" v-bind:value="v">{{ k }}</option>
        </select>
      </div>
      <div class="form-group col-2">
        <select class="form-control" v-model="boundary.reference">
          <option value=undefined disabled>Pick Time Reference</option>
          <option v-for="(name, ref) in references" v-bind:key="ref" v-bind:value="ref">{{ name }}</option>
        </select>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'RelativeTimeBoundary',
  props: {
    boundary: Object,
    index: Number
  },
  data () {
    return {
      references: {
        Now: 'When Running',
        BeginOfToday: 'Begin of Running Day',
        EndOfToday: 'End of Running Day',
        BeginOfThisMonth: 'Begin of Running Month',
        EndOfThisMonth: 'End of Running Month',
        BeginOfThisWeek: 'Begin of Running Week',
        EndOfThisWeek: 'End of Running Week',
        BeginOfThisQuarter: 'Begin of Running Quarter',
        EndOfThisQuarter: 'End of Running Quarter',
        BeginOfThisYear: 'Begin of Running Quarter',
        EndOfThisYear: 'End of Running Year',
        BeginOfThisFinancialYear: 'Begin of Running Financial Year',
        EndOfThisFinancialYear: 'End of Running Financial Year'
      },
      directions: {
        before: -1,
        after: 1
      },
      direction: 1,
      monthDiff: 0,
      dayDiff: 0,
      hourDiff: 0
    }
  },
  created: function () {
    if (this.boundary.timeDiff !== undefined) {
      this.monthDiff = this.boundary.timeDiff.month
      this.hourDiff = this.boundary.timeDiff.hour
      this.dayDiff = this.boundary.timeDiff.day
    }
  },
  watch: {
    monthDiff: function (newValue) {
      this.boundary.timeDiff.month = newValue
    },
    dayDiff: function (newValue) {
      this.boundary.timeDiff.day = newValue
    },
    hourDiff: function (newValue) {
      this.boundary.timeDiff.hour = newValue
    }
  },
  methods: {
  }
}
</script>

<style scoped>

</style>
