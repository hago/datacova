<template>
  <div>
    <select class="form-control col-6" v-model="config.type">
      <option value=undefined disabled>Select Distribute Type</option>
      <option v-for="(t, item) in types" v-bind:key="t" v-bind:value="t">{{ item }}</option>
    </select>
    <div>
      <div class="form-row">
        <div class="col-3 form-group">
          <div class="form-check">
            <input class="form-check-input" type="checkbox" :id="'copyOriginal' + actionIndex" v-model="config.copyOriginal">
            <label class="form-check-label" for="copyOriginal">
              Copy Original
            </label>
          </div>
        </div>
        <div class="col-3 form-group">
          <div class="form-check">
            <input class="form-check-input" type="checkbox" :id="'overwriteExisted' + actionIndex" v-model="config.overwriteExisted">
            <label class="form-check-label" for="overwriteExisted">
              Overwrite if existed
            </label>
          </div>
        </div>
      </div>
    </div>
    <FtpDistribute v-if="config.type === 1"
      v-bind:config="config"
      ></FtpDistribute>
    <SFtpDistribute v-if="config.type === 2"
      v-bind:config="config"
      ></SFtpDistribute>
  </div>
</template>

<script>
import Vue from 'vue'
import FtpDistribute from '@/components/distribute/FtpDistribute.vue'
import SFtpDistribute from '@/components/distribute/SFtpDistribute.vue'

export default {
  name: 'TaskActionDistribute',
  components: {
    FtpDistribute,
    SFtpDistribute
  },
  props: {
    action: Object,
    actionIndex: Number
  },
  data () {
    return {
      types: {
        'FTP Server': 1,
        'Secure Shell FTP Server': 2
      },
      config: {}
    }
  },
  created: function () {
    if (this.action.configuration === undefined) {
      Vue.set(this.action, 'configuration', this.config)
    } else {
      this.config = this.action.configuration
    }
    this.action.validator = function (action) {
      if (action.configuration.validator === undefined) {
        return 'validator not defined'
      }
      let r = action.configuration.validator(action.configuration)
      if (r !== true) {
        return r
      }
      return action.configuration.type !== undefined
    }
  }
}
</script>

<style scoped>

</style>
