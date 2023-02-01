<template>
  <div class="container">
    <n-grid :cols="2">
      <n-gi class="dockright">
        <span>User ID</span>
      </n-gi>
      <n-gi>
        <n-input v-model="name" type="text" placeholder="input id"></n-input>
      </n-gi>
    </n-grid>
    <n-grid :cols="2">
      <n-gi class="dockright">
        <span>Password</span>
      </n-gi>
      <n-gi>
        <n-input
          v-model="password"
          type="password"
          placeholder="input password"
        ></n-input>
      </n-gi>
    </n-grid>
    <n-grid :cols="2">
      <n-gi class="dockright">
        <span>Captcha</span>
      </n-gi>
      <n-gi>
        <n-input v-model="name" type="text" placeholder="input id"></n-input>
      </n-gi>
    </n-grid>
    <n-grid :cols="2">
      <n-gi></n-gi>
      <n-gi>
        <n-image width="300" :src="captcha_url" lazy preview-disabled/>
      </n-gi>
    </n-grid>
  </div>
</template>

<script lang="ts">
import { random } from "lodash";
import { defineComponent } from "vue";

const CAPTCHA_URL = "/api/auth/captcha"

export default defineComponent({
  setup() {
    return {
      name: "",
      password: "",
      captcha: "",
      captcha_url: CAPTCHA_URL
    };
  },
  mounted() {
    this.loadCaptcha()
  },
  methods: {
    loadCaptcha() {
      let r = random(new Date().getTime())
      this.captcha_url = `${CAPTCHA_URL}?${r}`
    }
  }
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
</style>