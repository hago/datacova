import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'

const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)

import { createApp } from 'vue'

//import App from './App.vue'
import Cova from './Cova.vue'
import router from './router'
import naive from 'naive-ui'

import './assets/main.css'

const app = createApp(Cova)

app.use(pinia)
    .use(router)
    .use(naive)
    .mount('#app')
