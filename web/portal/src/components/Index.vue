<template>
  <div>
    <div class="row">
      <div class="col-6">
        <h1>What is Data<span class="brand">CoVa</span></h1>
      </div>
    </div>
    <div class="row">
      <div class="col-1"></div>
      <div class="col-10">
        <p class="description">
          &nbsp;&nbsp;Data<span class="brand">CoVa</span> is a set of services to process data files.
          It provides capability to:
        </p>
        <ul class="featrures">
          <li>Extract tabular data from files</li>
          <li>Detect column definitions and create table automatically</li>
          <li>Ingest into table in databases</li>
          <li>Validate data using customized restrictions</li>
          <li>Distribute data to various destinations</li>
        </ul>
        <p class="description">
          &nbsp;&nbsp;It supports all mainstream databases; supports distribution to multiple destinations; provides
          many validation methods and pre-built rules, and providers flexible workspace and user managemnet.
        </p>
      </div>
    </div>
  </div>
</template>

<script>
import 'bootstrap/dist/css/bootstrap.css'

export default {
  name: 'Index',
  data () {
    return {
    }
  },
  props: {
    loginStatus: Object
  },
  methods: {
  }
}
</script>

<style>
.loading {
    display: block;
    margin: auto auto auto auto;
    width: 150px;
    height: 150px;
}
h1 {
  text-align: center;
  font-size: 500%
}
.brand {
  color: red;
}
.description {
  font-size: 280%
}
.description {
  font-size: 240%
}
</style>
