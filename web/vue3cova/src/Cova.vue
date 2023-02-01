<script setup lang="ts">
import { RouterLink, RouterView } from "vue-router";
</script>

<template>
  <n-grid :cols="2" class="navbar">
    <n-gi class="tm">
      <span class="tmData">Data </span>
      <span class="tmCo">CO</span><span>llect </span>
      <span class="tmVa">VA</span><span>lidate</span>
    </n-gi>
    <n-gi class="userarea">
      <div>User Profile</div>
    </n-gi>
  </n-grid>
  <n-grid>
    <n-gi :span="24">
      <RouterView />
    </n-gi>
  </n-grid>
</template>

<style scoped>
.navbar {
  font-size: x-large;
}
.tm {
  font-size: medium;
}
.tmData {
  font-size: x-large;
  color: aqua;
}
.tmCo {
  font-size: xx-large;
  color: crimson;
}
.tmVa {
  font-size: xx-large;
  color: red;
}

.userarea {
  text-align: right;
}
</style>
