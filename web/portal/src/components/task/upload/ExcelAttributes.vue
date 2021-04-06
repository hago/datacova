<template>
  <div>
    <div class="form-row">
      <div class="form-group col-3">
        <div class="form-check">
          <input class="form-check-input" type="checkbox" id="forIndex" name="sheetBy" checked="checked"
            v-on:change="checkIndex()" v-model="useIndex">
          <label class="form-check-label" for="forIndex">
            Using sheet by index
          </label>
        </div>
      </div>
      <div class="form-group col-3">
        <div class="form-check">
          <input class="form-check-input" type="checkbox" id="forName" name="sheetBy" v-on:change="checkName()" v-model="useName">
          <label class="form-check-label" for="forName">
            Using sheet by name
          </label>
        </div>
      </div>
    </div>
    <div class="form-row">
      <div class="form-group col-3">
        <input type="number" v-model="extraInfo.sheetIndex" class="form-control" :disabled="useName" min="0" />
      </div>
      <div class="form-group col-3">
        <input type="text" v-model="extraInfo.sheetName" class="form-control" :disabled="useIndex"/>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ExcelAttributes',
  props: {
    extraInfo: Object,
    file: Object
  },
  created: function () {
    this.extraInfo.sheetIndex = 0
    this.extraInfo.sheetName = null
    this.extraInfo.type = this.file.name.indexOf('xlsx') > 0 ? 3 : 2
  },
  data () {
    return {
      useIndex: true,
      useName: false
    }
  },
  methods: {
    checkIndex () {
      this.useName = !this.useIndex
    },
    checkName () {
      this.useIndex = !this.useName
    }
  }
}
</script>

<style scoped>

</style>
