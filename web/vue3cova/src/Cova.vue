<script lang="ts">
import type { DropdownOption } from "naive-ui";
import { defineComponent, h, reactive, ref } from "vue";
import workspaceApiHelper from "./api/workspaceapi";
import { anonymousIdentity } from "./entities/identity";
import router from "./router";
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

interface SelectOption {
  label: string,
  value: string | number
}

export default defineComponent({
  emits: [
    'loginStatusChanged'
  ],
  setup() {
    let id = currentIdentity(identityStore());
    if (!id.isValidIdentity()) {
      id = anonymousIdentity()
    }
    let emptySpaces: SelectOption[] = []
    return reactive({
      userIdentity: id,
      activeKey: ref<string | null>(null),
      options: createOptions(),
      workspaceId: ref<number | null>(null),
      workspaces: emptySpaces
    });
  },
  mounted() {
    this.loadWorkspaces()
  },
  methods: {
    dropdownClick(key: string | number, option: DropdownOption) {
      switch (key) {
        case "logout":
          identityStore().logout()
          this.userIdentity = anonymousIdentity()
          router.push("/login")
          break
        default:
          return
      }
    },
    logonChanged() {
      console.log("logonChanged triggered")
      let logst = identityStore()
      this.userIdentity = currentIdentity(logst)
      if (!this.userIdentity.isValidIdentity()) {
        this.userIdentity = anonymousIdentity()
      }
      this.loadWorkspaces()
    },
    loadWorkspaces() {
      let id = identityStore();
      if (!currentIdentity(id).isValidIdentity()) {
        this.workspaces = []
        this.workspaceId = null
      } else {
        workspaceApiHelper.userWorksapces(currentIdentity(id), {
          success: (rsp) => {
            this.workspaces = rsp.data.map(wk => {
              return {
                label: wk.workspace.name,
                value: wk.workspace.id
              }
            })
            this.workspaceId = rsp.data.length > 0 ? rsp.data[0].workspace.id : null
          },
          fail: () => { }
        })
      }
    }
  }
});
</script>

<template>
  <n-grid :cols="3" class="navbar">
    <n-gi class="tm">
      <span class="tmData">Data </span>
      <span class="tmCo">CO</span><span>llect </span>
      <span class="tmVa">VA</span><span>lidate</span>
    </n-gi>
    <n-gi class="tm">
      <n-select v-model:value="workspaceId" :options="workspaces" class="workspaceselect"
        v-if="(userIdentity !== null) && userIdentity.isValidIdentity()" />
    </n-gi>
    <n-gi class="userarea">
      <!--<n-menu
        :options="userMenu"
        v-model:value="activeKey"
        mode="vertical"
        class="userprofile"
      ></n-menu>-->
      <n-dropdown trigger="click" :options="options" @select="dropdownClick">
        <n-button style="color: aqua">{{ userIdentity.name }}</n-button>
      </n-dropdown>
    </n-gi>
  </n-grid>
  <n-grid>
    <n-gi :span="24">
      <RouterView @loginStatusChanged="logonChanged" />
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

.workspaceselect {
  padding-top: 10px;
}
</style>
