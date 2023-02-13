import { identityStore } from '@/stores/identitystore'
import { createRouter, createWebHistory } from 'vue-router'
import RouteErrorView from '@/views/auth/RouteErrorView.vue'
import LoginView from '@/views/auth/LoginView.vue'
import ContentView from '@/views/ContentView.vue'

declare module 'vue-router' {
  interface RouteMeta {
    login: {
      requireLogin: boolean,
      jumpToLogin: boolean
    }
  }
}

const DEFAULT_LOGIN_ROUTE = "/login"
const DEFAULT_ROUTE_ERROR_ROUTE = "/error/route"

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),

  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: {
        login: {
          requireLogin: false,
          jumpToLogin: false
        }
      }
    },
    {
      path: '/error/route',
      name: 'route_error',
      component: RouteErrorView,
      meta: {
        login: {
          requireLogin: false,
          jumpToLogin: false
        }
      }
    },
    {
      path: '/',
      name: 'root',
      component: ContentView,
      meta: {
        login: {
          requireLogin: true,
          jumpToLogin: true
        }
      }
    }
  ]
})

router.beforeEach((to, from) => {
  console.log('router.beforeEach', to, from)
  if (to.meta.login === undefined) {
    console.log('login meta is missed, assuming no auth needed ', to.fullPath)
    return true
  }
  if (!to.meta.login.requireLogin) {
    console.log('login is not required')
    return true
  }
  console.log('login is required')
  let user = identityStore().current
  if (!user.isValidIdentity()) {
    console.log('user login status is not found')
    if (to.meta.login.jumpToLogin) {
      console.log('redirect to default login page')
      return DEFAULT_LOGIN_ROUTE
    } else {
      console.log('stop routing')
      return DEFAULT_ROUTE_ERROR_ROUTE
    }
  }
  console.log('user login status found')
  return true
})

export default router
