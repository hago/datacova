<template>
  <div class="container">
    <div class="row">
      <div class="form-group col-5">
        <label for="name">Name</label>
        <input type="text" class="form-control" id="name" v-model="rule.name" />
      </div>
    </div>
    <div class="row">
      <div class="form-group col-11">
        <label for="description">Description</label>
        <textarea type="text" class="form-control" id="description" v-model="rule.description" />
      </div>
    </div>
    <div class="row">
      <div class="form-group col-5">
        <label for="typeSelect">Type</label>
        <select class="form-control" id="typeSelect" v-model="rule.ruleconfig.type" v-on:change="changetype()">
          <option v-for="(value, name) in configs" v-bind:key="value" v-bind:value="value">{{ name }}</option>
          <option disabled value=undefined>Choose Type</option>
        </select>
      </div>
    </div>
    <div class="row">
      <div class="col-11">
        <NumberRangeVerifier v-if="rule.ruleconfig.type == 2"
          v-bind:config="rule.ruleconfig"
          v-bind:index="0"
          ></NumberRangeVerifier>
        <TimeRangeVerifier v-if="rule.ruleconfig.type == 4"
          v-bind:config="rule.ruleconfig"
          v-bind:index="0"
          ></TimeRangeVerifier>
        <RelativeTimeRangeVerifier v-if="rule.ruleconfig.type == 6"
          v-bind:config="rule.ruleconfig"
          v-bind:index="0"
          ></RelativeTimeRangeVerifier>
        <OptionsVerifier v-if="rule.ruleconfig.type == 3"
          v-bind:config="rule.ruleconfig"
          v-bind:index="0"
          ></OptionsVerifier>
        <LuaScriptVerifier v-if="rule.ruleconfig.type == 5"
          v-bind:config="rule.ruleconfig"
          v-bind:index="0"
          ></LuaScriptVerifier>
        <RegexVerifier v-if="rule.ruleconfig.type == 1"
          v-bind:config="rule.ruleconfig"
          v-bind:index="0"
          ></RegexVerifier>
      </div>
    </div>
    <div class="row">
      <button class="btn btn-primary" v-on:click="save()">Save</button>
      <button class="btn btn-info" v-on:click="cancel()">Cancel</button>
    </div>
  </div>
</template>

<script>
import Vue from 'vue'
import WorkspaceApiHelper from '@/apis/workspace.js'
import NumberRangeVerifier from '@/components/verification/NumberRange.vue'
import TimeRangeVerifier from '@/components/verification/TimeRange.vue'
import OptionsVerifier from '@/components/verification/Options.vue'
import LuaScriptVerifier from '@/components/verification/LuaScript.vue'
import RegexVerifier from '@/components/verification/Regex.vue'
import RelativeTimeRangeVerifier from '@/components/verification/RelativeTimeRange.vue'
import ValidationTemplateApi, {defaultValidatorConfig} from '@/libs/validation.js'

export default {
  name: 'ValidationRule',
  props: {
    workspace: Object,
    wkid: String,
    ruleid: String
  },
  components: {
    NumberRangeVerifier,
    TimeRangeVerifier,
    OptionsVerifier,
    LuaScriptVerifier,
    RegexVerifier,
    RelativeTimeRangeVerifier
  },
  data: function () {
    return {
      rule: {
        id: -1,
        name: '',
        description: '',
        ruleconfig: {}
      },
      wk: this.workspace,
      configs: {
        'Regular Expression': 1,
        'Number Range': 2,
        'Option List': 3,
        'Time Range': 4,
        'Coding Lua': 5,
        'Relative Time Range': 6
      }
    }
  },
  created: function () {
    if (this.wk === undefined) {
      (new WorkspaceApiHelper().getWorkspace(parseInt(this.wkid))).then(rsp => {
        this.wk = rsp.data.data
      }).catch(err => console.log(err))
    }
    if (!isNaN(parseInt(this.ruleid))) {
      if (this.rule === undefined) {
        (new WorkspaceApiHelper().getWorkspace(this.wkid)).then(rsp => {
          this.rule = rsp.data.data
        }).catch(err => console.log(err))
      }
    }
  },
  methods: {
    changetype: function () {
      let x = defaultValidatorConfig(this.rule.ruleconfig.type)
      Vue.set(this.rule, 'ruleconfig', Object.assign({type: this.rule.ruleconfig.type}, x))
    },
    cancel: function () {
      if (confirm('Discard all changes')) {
        this.$router.back()
      }
    },
    save: function () {
      (new ValidationTemplateApi()).saveRuleTemplate(this.rule).then(rsp => {
        //
      }).catch(err => console.log(err))
    }
  }
}
</script>

<style scoped>

</style>
