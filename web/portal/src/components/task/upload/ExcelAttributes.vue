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
        <input type="number" v-model="extraInfo.sheetIndex" class="form-control" :disabled="useName" min="0" :max="maxSheetIndex" />
      </div>
      <div class="form-group col-3" v-if="!parsed">
        <input type="text" v-model="extraInfo.sheetName" class="form-control" :disabled="useIndex"/>
      </div>
      <div class="form-group col-3" v-if="parsed">
        <select v-model="extraInfo.sheetName" class="form-control" :disabled="useIndex">
          <option v-for="sheet in info" v-bind:key="sheet.name" v-bind:id="sheet.name">{{ sheet.name }}</option>
        </select>
      </div>
      <div class="form-group">
        <button class="btn btn-primary form-control" v-on:click="preview()">Preview</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ExcelAttributes',
  props: {
    extraInfo: Object,
    file: File,
    info: Object
  },
  created: function () {
    this.extraInfo.sheetIndex = 0
    this.extraInfo.sheetName = null
    this.extraInfo.type = this.file.name.indexOf('xlsx') > 0 ? 3 : 2
  },
  computed: {
    sheetNames: function () {
      return this.info.map(sheet => sheet.name)
    }
  },
  data () {
    return {
      useIndex: true,
      useName: false,
      maxSheetIndex: false,
      parsed: false
    }
  },
  methods: {
    checkIndex () {
      this.useName = !this.useIndex
    },
    checkName () {
      this.useIndex = !this.useName
    },
    preview () {
      this.$emit('filePreview')
    }
  }
}
</script>

<style scoped>

</style>
