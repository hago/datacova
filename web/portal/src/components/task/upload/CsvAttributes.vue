<template>
  <div class="form-row">
    <div class="form-group col-3">
      <label for="quote">Quote</label>
      <input class="form-control" id="quote" type="text" placeholder='"' v-model="extraInfo.quote">
    </div>
    <div class="form-group col-3">
      <label for="delimiter">Delimiter</label>
      <input class="form-control" id="delimiter" type="text" placeholder=',' v-model="extraInfo.delimiter">
    </div>
    <div class="form-group col-6">
      <label for="Charset">Character Set</label>
      <select id="Charset" v-model="extraInfo.encoding" class="form-control">
        <option v-for="(name, enc) in charsets" v-bind:key="enc" v-bind:value="enc">{{ name }}</option>
      </select>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CsvAttributes',
  props: {
    extraInfo: Object
  },
  created: function () {
    this.extraInfo.quote = null
    this.extraInfo.delimiter = null
    this.extraInfo.encoding = null
    this.extraInfo.type = 1
  },
  data () {
    return {
      charset: 'UTF-8',
      charsets: {
        'UTF-8': 'UTF-8',
        'GBK': 'GBK',
        'ISO8859-1': 'ISO8859-1',
        'US_ASCII': 'US_ASCII'
      }
    }
  }
}
</script>

<style scoped>

</style>
