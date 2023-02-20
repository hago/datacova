<script lang="ts">
import router from '@/router';
import { identityStore } from '@/stores/identitystore';
import { defineComponent, reactive, ref } from 'vue';

export default defineComponent({
    name: 'WelcomeView',
    setup() {
        return reactive({});
    },
    mounted() {
        let id = identityStore().currentIdentity()
        if (id.isValidIdentity()) {
            this.$emit("loginStatusChanged");
        }
    },
    methods: {
        gotoWorkspace() {
            router.push("/login")
        }
    }
})
</script>

<template>
    <h1>Welcome</h1>
    <n-button @click="gotoWorkspace()" type="primary">Enter</n-button>
</template>

<style scoped>

</style>