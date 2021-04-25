<template>
  <div>
    <div class="col">
      <h5>Option List  <button class="btn btn-info" v-on:click="addOption()">&nbsp;+&nbsp;</button></h5>
    </div>
    <div class="form-group">
      <div class="form-check form-check-inline">
        <input class="form-check-input" type="checkbox" :id="`ignoreCase_${index}`" v-model="config.ignoreCase">
        <label class="form-check-label" :for="`ignoreCase_${index}`">
          Ignore Option Case
        </label>
      </div>
      <div class="form-check form-check-inline">
        <input class="form-check-input" type="checkbox" :id="`allowEmpty_${index}`" v-model="config.allowEmpty">
        <label class="form-check-label" :for="`allowEmpty_${index}`">
          Allow Empty Value
        </label>
      </div>
    </div>
    <div class="row">
      <div class="col-3 input-group" v-for="(option, j) in options" v-bind:key="j">
        <div class="input-group-prepend">
          <div class="input-group-text">
            <img src="../../assets/remove.png" v-on:click="removeOption(j)" class="rmvbtn clickable" />
          </div>
        </div>
        <input class="form-control" type="text" v-model="options[j]"/>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'OptionsVerifier',
  props: {
    config: Object,
    index: Number
  },
  data () {
    return {
      options: []
    }
  },
  mounted: function () {
    if (this.config.options === undefined) {
      this.config.options = ['']
    } else if (this.config.options.length === 0) {
      this.config.options.push('')
    }
    this.options = this.config.options
    this.config.validator = function (configuration) {
      if (configuration.options.length === 0) {
        return 'no options defined'
      }
      for (let option of configuration.options) {
        if (option.trim() === '') {
          return 'empty option is not allowed. "NUllable" can be checked to allow empty value'
        }
      }
      return true
    }
  },
  methods: {
    addOption: function () {
      this.options.push('')
    },
    removeOption: function (index) {
      this.options.splice(index, 1)
    }
  }
}
</script>

<style scoped>
.rmvbtn {
  width: 20px
}
</style>
