<script lang="ts">
import { defineComponent, h, reactive, ref } from "vue";
import {
  currentActualIdentity,
  currentIdentity,
  identityStore,
} from "./stores/identitystore";

let createOptions = () => {
  let id = identityStore();
  if (!currentIdentity(id).isValidIdentity()) {
    return [];
  } else {
    let u = currentIdentity(id);
    let u0 = currentActualIdentity(id);
    return [
      {
        label: `User Id: ${u.id}`,
        key: "userid",
        disabled: true,
      },
      {
        label: "Logout",
        key: "logout",
      },
    ];
  }
};

export default defineComponent({
  setup() {
    let id = currentIdentity(identityStore());
    return reactive({
      userIdentity: id,
      activeKey: ref<string | null>(null),
      options: createOptions(),
    });
  },
});
</script>

<template>
  <n-grid :cols="2" class="navbar">
    <n-gi class="tm">
      <span class="tmData">Data </span>
      <span class="tmCo">CO</span><span>llect </span>
      <span class="tmVa">VA</span><span>lidate</span>
    </n-gi>
    <n-gi class="userarea">
      <!--<n-menu
        :options="userMenu"
        v-model:value="activeKey"
        mode="vertical"
        class="userprofile"
      ></n-menu>-->
      <n-dropdown trigger="click" :options="options">
        <n-button style="color: aqua">{{ userIdentity.name }}</n-button>
      </n-dropdown>
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
  color: aqua;
}
</style>
