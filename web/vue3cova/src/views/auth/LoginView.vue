<template>
  <div class="container">
    <n-grid :cols="2" :y-gap="10">
      <n-gi class="dockright">
        <span>User ID</span>
      </n-gi>
      <n-gi>
        <n-input
          v-model:value="name"
          type="text"
          placeholder="input id"
        ></n-input>
      </n-gi>
      <n-gi class="dockright">
        <span>Password</span>
      </n-gi>
      <n-gi>
        <n-input
          v-model:value="password"
          type="password"
          placeholder="input password"
        ></n-input>
      </n-gi>
      <n-gi class="dockright">
        <span>Captcha</span>
      </n-gi>
      <n-gi>
        <n-input
          v-model:value="captcha"
          type="text"
          placeholder="input captcha"
        ></n-input>
      </n-gi>
      <n-gi class="dockright vcenter">
        <n-button type="info" size="tiny" v-on:click="refreshCaptcha()"
          >refresh</n-button
        >
      </n-gi>
      <n-gi>
        <n-image width="300" v-model:src="captcha_url" preview-disabled />
      </n-gi>
      <n-gi class="dockright"> </n-gi>
      <n-gi class="dockright">
        <n-button type="primary" @click="login()">Login</n-button>
      </n-gi>
    </n-grid>
    <n-alert
      v-if="errormessage != ''"
      title="Error"
      type="error"
      class="dockright"
    >
      {{ errormessage }}
    </n-alert>
  </div>
</template>

<script lang="ts">
import { random } from "lodash";
import { defineComponent, reactive } from "vue";
import loginHelper from "@/api/userauthapi";
import { identityStore } from "@/stores/identitystore";
import { newIdentity } from "@/entities/identity";
import router from "@/router";

const CAPTCHA_URL = "/api/auth/captcha";

export default defineComponent({
  setup() {
    return reactive({
      name: "",
      password: "",
      captcha: "",
      captcha_url: CAPTCHA_URL,
      errormessage: "",
    });
  },
  mounted() {
    this.loadCaptcha();
  },
  methods: {
    loadCaptcha() {
      let r = random(new Date().getTime());
      this.captcha_url = `${CAPTCHA_URL}?${r}`;
    },
    refreshCaptcha() {
      console.log("refresh");
      this.loadCaptcha();
    },
    login() {
      const u = this.name.trim();
      const p = this.password.trim();
      const c = this.captcha.trim();
      if (u === "" || p === "" || c === "") {
        this.errormessage = "Fields must not be empty!";
        return;
      }
      this.errormessage = "";
      loginHelper.login(
        {
          userId: u,
          password: p,
          captcha: c,
        },
        {
          success: (rsp) => {
            console.log(`${rsp.data.user.name} logged`);
            identityStore().login(
              newIdentity(
                rsp.data.user.userId,
                rsp.data.user.name,
                rsp.data.token
              )
            );
            this.$emit("loginStatusChanged");
          },
          fail: (status, reason, data?) => {
            console.log("login view failed");
            this.errormessage = `login failed: ${reason}`;
          },
        }
      );
    },
  },
});
</script>

<style scoped>
.container {
  position: absolute;
  margin: auto;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  width: 400px;
  font-size: x-large;
}

.formtext {
  font-size: x-large;
}
.dockright {
  text-align: right;
  margin-right: 10px;
}

.vcenter {
  margin-top: auto;
  margin-bottom: auto;
}
</style>
