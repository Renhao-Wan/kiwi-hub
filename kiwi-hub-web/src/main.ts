import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'
import '@/assets/styles/reset.css'
import '@/assets/styles/global.css'
import 'highlight.js/styles/github.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.mount('#app')
