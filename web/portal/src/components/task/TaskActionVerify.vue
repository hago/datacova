<template>
  <div>
    <div class="col">
      <h5>
        <i>Verification Configurations</i>
        <span style="float: right; margin-right: 10px" class="clickable" v-on:click="addConfig()">+ Add Verificator</span>
      </h5>
    </div>
    <div v-for="(configuration, index) in action.configurations" v-bind:key="index" style="border: 1px dotted yellow; margin: 3px">
      <div class="col">
        <img src='@/assets/recycling_bin.png' class="clickable rmvbtn" v-on:click="removeConfig(index)"/>
        <h5>
          Fields to verify
          <span style="float: right; margin-right: 10px" class="clickable" v-on:click="addField(index)">+ Add Field</span>
        </h5>
      </div>
      <diV class="form-row">
        <div class="col-3 input-group" v-for="(field, fieldIndex) in configuration.fields" v-bind:key="fieldIndex">
          <div class="input-group-prepend">
            <div class="input-group-text">
              <img src="../../assets/remove.png" v-on:click="removeField(index, fieldIndex)" class="rmvbtn clickable" />
            </div>
          </div>
          <input class="form-control" type="text" v-model="configuration.fields[fieldIndex]"/>
        </div>
      </div>
      <div class="form-row">
        <div class="col-3 form-group">
          <div class="form-check">
            <input class="form-check-input" type="checkbox" id="nullable" v-model="configuration.nullable">
            <label class="form-check-label" for="nullable">
              Nullable
            </label>
          </div>
        </div>
        <div class="col-3 form-group">
          <div class="form-check">
            <input class="form-check-input" type="checkbox" id="ignorefieldcase" v-model="configuration.ignoreFieldCase">
            <label class="form-check-label" for="ignorefieldcase">
              Ignore Case of Fields
            </label>
          </div>
        </div>
      </div>
      <div class="form-row">
        <div class="form-group col-6">
          <label for="verifyType" class="form-label">Verify Type</label>
          <select class="form-control" id="verifyType" v-model="configuration.type">
            <option value="undefined" disabled>Verification Types</option>
            <option v-for="(desp, name) in verifyTypes" v-bind:key="name" v-bind:value="name">{{ desp }}</option>
          </select>
        </div>
      </div>
      <NumberRangeVerifier v-if="configuration.type == 'NumberRange'"
        v-bind:config="configuration"
        ></NumberRangeVerifier>
      <TimeRangeVerifier v-if="configuration.type == 'TimeRange'"
        v-bind:config="configuration"
        ></TimeRangeVerifier>
      <RelativeTimeRangeVerifier v-if="configuration.type == 'RelativeTimeRange'"
        v-bind:config="configuration"
        ></RelativeTimeRangeVerifier>
      <OptionsVerifier v-if="configuration.type == 'Options'"
        v-bind:config="configuration"
        ></OptionsVerifier>
      <LuaScriptVerifier v-if="configuration.type == 'LuaScript'"
        v-bind:config="configuration"
        ></LuaScriptVerifier>
      <RegexVerifier v-if="configuration.type == 'Regex'"
        v-bind:config="configuration"
        ></RegexVerifier>
    </div>
  </div>
</template>

<script>
import Vue from 'vue'
import NumberRangeVerifier from '@/components/verification/NumberRange.vue'
import TimeRangeVerifier from '@/components/verification/TimeRange.vue'
import OptionsVerifier from '@/components/verification/Options.vue'
import LuaScriptVerifier from '@/components/verification/LuaScript.vue'
import RegexVerifier from '@/components/verification/Regex.vue'
import RelativeTimeRangeVerifier from '@/components/verification/RelativeTimeRange.vue'

export default {
  name: 'TaskActionVerify',
  props: {
    action: Object,
    workspaceId: Number
  },
  components: {
    NumberRangeVerifier,
    TimeRangeVerifier,
    OptionsVerifier,
    LuaScriptVerifier,
    RegexVerifier,
    RelativeTimeRangeVerifier
  },
  data () {
    return {
      verifyTypes: {
        Regex: 'Text pattern verification with Regular Expression',
        NumberRange: 'Number Range Verification',
        Options: 'Optional Values Verification',
        TimeRange: 'Time Range Verification',
        LuaScript: 'Customized Verification in Coding',
        RelativeTimeRange: 'Time Range Verification Relative to A Point'
      }
    }
  },
  created: function () {
    if (this.action.configurations === undefined) {
      Vue.set(this.action, 'configurations', [])
    }
  },
  mounted: function () {
    this.action.validator = function (action) {
      if ((action.configurations === undefined) || (action.configurations.length === 0)) {
        return 'no configuration defined'
      }
      for (let i in action.configurations) {
        let conf = action.configurations[i]
        if (conf.fields.length === 0) {
          return `no fields defined for configuration ${i}`
        }
        for (let field of conf.fields) {
          if (field.trim() === '') {
            return `empty fields found for configuration ${i}`
          }
        }
        if (conf.validator === undefined) {
          return `no validator defined for configuration ${i}`
        }
        if (conf.type === undefined) {
          return `no verification type selected for configuration ${i}`
        }
        let r = conf.validator(conf)
        if (r !== true) {
          return `configuration ${i}: ${r}`
        }
      }
      return true
    }
  },
  methods: {
    addField: function (confIndex) {
      let limit = this.action.configurations[confIndex].FieldsCountLimit
      let max = limit === undefined || limit <= 0 ? Infinity : limit
      if (this.action.configurations[confIndex].fields.length < max) {
        this.action.configurations[confIndex].fields.push('')
      }
    },
    removeField: function (confIndex, i) {
      let limit = this.action.configurations[confIndex].FieldsCountLimit
      let min = limit === undefined || limit <= 0 ? 1 : limit
      if (this.action.configurations[confIndex].fields.length > min) {
        this.action.configurations[confIndex].fields.splice(i, 1)
      }
    },
    addConfig: function () {
      this.action.configurations.push({
        fields: [''],
        nullable: false,
        ignoreFieldCase: false
      })
    },
    removeConfig: function (i) {
      this.action.configurations.splice(i, 1)
    }
  }
}
</script>

<style scoped>
.rmvbtn {
  width: 20px
}
</style>