<script lang="ts">
import { defineComponent, h, reactive, ref } from "vue";
import { RouterLink, RouterView } from "vue-router";
import {
  currentActualIdentity,
  currentIdentity,
  identityStore,
} from "./stores/identitystore";

const anonymousMenu = [
  {
    label: "Anonymous",
    key: "user-name-display",
  },
];
let createMenu = () => {
  let id = identityStore();
  if (!currentIdentity(id).isValidIdentity()) {
    return anonymousMenu;
  } else {
    let u = currentIdentity(id);
    let u0 = currentActualIdentity(id);
    return [
      {
        label: u.name,
        key: u0.id,
        children: [
          {
            label: () =>
              h(RouterLink, { to: "/logout" }, { default: () => "Logout" }),
          },
        ],
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
      userMenu: createMenu(),
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
      <n-menu
        :options="userMenu"
        v-model:value="activeKey"
        mode="vertical"
        class="userprofile"
      ></n-menu>
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

.userprofile {
  color: aqua;
}
</style>
