<template>
  <div >
    <div style="margin: 0px 5px 10px 5px">
      <textarea class="col" v-model="snippet" rows="8" v-on:change="codesChange()" placeholder="example:
row['col1'] + row['col2'] < row['col3']

explanation:
1. The code is in Python language.
2. The first line of code should be external argument declaration.
  2.1 Values of fields will be passed into as a dict named 'row'.
  2.2 Inside snippet, field can be referenced by name.
3. The last line should return a value or be an expression. It will be treated as value of whole snippet.
  3.1 It should be 'return ...' statement.
  3.2 The value should be a boolean.
  3.3 Any other type of value will be considered as False.
      "></textarea>
    </div>
    <div class="form-row">
      <div class="col-3 input-group" v-for="(field, j) in config.fields" v-bind:key="j">
        <input class="form-control w-75" type="text" v-bind:placeholder="`value of ${field ? field : ('field' + j)}`"
          v-bind:change="evalValueChange(j)"
          v-model="params[j].value"/>
        <select class="form-control w-25" v-model="paramTypes[j]" v-bind:change="changeType(j)" >
          <option value="Number">Number</option>
          <option value="Boolean">Boolean</option>
          <option value="DateTime">DateTime</option>
          <option value="Text" selected>Text</option>
        </select>
      </div>
    </div>
    <button class="btn btn-info" style="margin-left: 5px" v-on:click="evaluate()">Evaluate</button>
    <button class="btn btn-secondary" v-if="evaluateResult !== undefined">Result: {{ evaluateResult }}</button>
  </div>
</template>

<script>
import Vue from 'vue'
import EvaluateApiHelper from '@/apis/evaluate.js'
import Toasted from 'vue-toasted'

Vue.use(Toasted)

export default {
  name: 'PythonScriptVerifier',
  props: {
    config: Object,
    index: Number
  },
  mounted: function () {
    this.config.validator = function (configuration) {
      if ((configuration.snippet === undefined) || (configuration.snippet.trim() === '')) {
        return 'codes is required'
      }
      return configuration.evaluated === true ? configuration.evaluated : 'codes not evaluated'
    }
  },
  data () {
    return {
      params: Object.assign(this.config.fields.map(function () {
        return {
          value: '',
          type: 'Text'
        }
      })),
      paramTypes: Object.assign(this.config.fields.map(_ => 'Text')),
      evaluateResult: undefined,
      snippet: this.config.snippet === undefined ? '' : this.config.snippet.replace('\\r', '\r').replace('\\n', '\n')
    }
  },
  watch: {
    snippet: function (newValue) {
      this.config.snippet = newValue.replace('\r', '\\r').replace('\n', '\\n')
    }
  },
  methods: {
    evalValueChange: function (fieldIndex) {
      let v = this.params[fieldIndex].value.toLowerCase().trim()
      if (!isNaN(Number(v))) {
        this.params[fieldIndex].type = 'Number'
      } else if ((v === 'true') || (v === 'false')) {
        this.params[fieldIndex].type = 'Boolean'
      } else if (this.isDateTime(v)) {
        this.params[fieldIndex].type = 'DateTime'
      } else {
        this.params[fieldIndex].type = 'Text'
      }
    },
    isDateTime: function (str) {
      return !isNaN(new Date(str).getTime())
    },
    changeType: function (j) {
      this.params[j].type = this.paramTypes[j]
    },
    evaluate: function () {
      if ((this.config.snippet === undefined) || (this.config.snippet.trim() === '')) {
        this.$toasted.show('code block is empty', {
          position: 'bottom-center',
          duration: 1000,
          type: 'warning'
        })
        return
      }
      for (let param of this.params) {
        if (param.value.trim() === '') {
          this.$toasted.show('field value is empty', {
            position: 'bottom-center',
            duration: 1000,
            type: 'warning'
          })
          return
        }
      }
      let fieldValues = {}
      for (let i in this.params) {
        fieldValues[this.config.fields[i]] = this.params[i]
      }
      (new EvaluateApiHelper()).evaluatePython(this.config.snippet, fieldValues).then(rsp => {
        this.config.evaluated = true
        this.evaluateResult = rsp.data.data
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
