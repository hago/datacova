<script lang="ts">
import { darkTheme, type DrawerPlacement } from "naive-ui";
import { defineComponent, h, reactive, ref } from "vue";
import workspaceApiHelper, { type Workspace } from "./api/workspaceapi";
import { EVENT_LOGIN_STATUS_CHANGED, EVENT_REMOTE_API_ERROR } from "./entities/events";
import { anonymousIdentity } from "./entities/identity";
import router from "./router";
import { identityStore } from "./stores/identitystore";
import { workspaceStore } from "./stores/workspacestore";
import { eventBus } from "./util/eventbus";

let createOptions = () => {
  let id = identityStore();
  if (!id.currentIdentity().isValidIdentity()) {
    return [];
  } else {
    let u = id.currentIdentity();
    // let u0 = id.currentActualIdentity();
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
  emits: [
    EVENT_LOGIN_STATUS_CHANGED,
    EVENT_REMOTE_API_ERROR
  ],
  setup() {
    let id = identityStore().currentIdentity();
    if (!id.isValidIdentity()) {
      id = anonymousIdentity()
    }
    let emptySpaces: Workspace[] = []
    return reactive({
      userIdentity: id,
      activeKey: ref<string | null>(null),
      options: createOptions(),
      workspaceId: ref<number | null>(null),
      workspaces: emptySpaces,
      showErrorDrawer: false,
      errorMessage: '',
      topPlacement: ref<DrawerPlacement>('top'),
      darkTheme
    });
  },
  mounted() {
    eventBus.register(EVENT_REMOTE_API_ERROR, this.errorMessageReceived)
    this.loadWorkspaces()
  },
  methods: {
    dropdownClick(key: string | number) {
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
      this.userIdentity = logst.currentIdentity()
      if (!this.userIdentity.isValidIdentity()) {
        this.userIdentity = anonymousIdentity()
      }
      this.loadWorkspaces()
    },
    async errorMessageReceived(...message: string[]): Promise<any> {
      console.log('show drawer', message)
      this.errorMessage = message[0]
      this.showErrorDrawer = true
      setTimeout(() => {
        this.showErrorDrawer = false
      }, 5000)
      return Promise.resolve()
    },
    loadWorkspaces() {
      let id = identityStore();
      if (!id.currentIdentity().isValidIdentity()) {
        this.workspaces = []
        this.workspaceId = null
      } else {
        workspaceApiHelper.userWorksapces(id.currentIdentity()).then(rsp => {
          this.workspaces = rsp.data.map(wk => {
            workspaceStore().setWorkspace(wk)
            return wk.workspace
          })
          this.workspaceId = rsp.data.length > 0 ? rsp.data[0].workspace.id : null
          // workspaceStore().selectWorkspace(rsp.data[0])
          this.workspaceSelect(this.workspaceId)
        }).catch(err => {
          eventBus.send(EVENT_REMOTE_API_ERROR, err)
        })
      }
    },
    workspaceSelect(value: number | null) {
      console.log(`selected ${value}`)
      if ((value === undefined) || (value === null)) {
        //this.$emit('errorOccurred', `select workspace ${value} not applicable`)
      } else {
        router.push({ name: 'workspace', params: { id: value } });
      }
    }
  }
});
</script>

<template>
  <n-config-provider :theme="darkTheme">
    <n-drawer v-model:show="showErrorDrawer" :placement="topPlacement">
      <n-drawer-content title="Error">
        <span class="error">{{ errorMessage }}</span>
      </n-drawer-content>
    </n-drawer>
  </n-config-provider>
  <n-grid :cols="3" class="navbar">
    <n-gi class="tm">
      <span class="tmData">Data </span>
      <span class="tmCo">CO</span><span>llect </span>
      <span class="tmVa">VA</span><span>lidate</span>
    </n-gi>
    <n-gi class="tm">
      <n-select label-field="name" value-field="id" @update:value="workspaceSelect" v-model:value="workspaceId"
        :options="workspaces" class="workspaceselect" v-if="(userIdentity !== null) && userIdentity.isValidIdentity()" />
    </n-gi>
    <n-gi class="userarea">
      <n-dropdown trigger="click" :options="options" @select="dropdownClick">
        <n-button style="color: aqua">{{ userIdentity.name }}</n-button>
      </n-dropdown>
    </n-gi>
  </n-grid>
  <n-grid>
    <n-gi span="24">
      <RouterView @loginStatusChanged="logonChanged" @apiErrorOccurred="errorMessageReceived" />
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

.errorbox {
  color: red;
  font-size: xx-large;
}

.error {
  color: red;
  font-size: x-large;
}
</style>
