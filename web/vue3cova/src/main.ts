import { createApp } from 'vue'
import { createPinia } from 'pinia'

//import App from './App.vue'
import Cova from './Cova.vue'
import router from './router'
import naive from 'naive-ui'

import './assets/main.css'

const app = createApp(Cova)

app.use(createPinia())
    .use(router)
    .use(naive)
    .mount('#app')
