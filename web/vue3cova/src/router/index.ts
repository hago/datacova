import { identityStore } from '@/stores/identity'
import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import RouteErrorView from '@/components/auth/RouteErrorView.vue'

declare module 'vue-router' {
  interface RouteMeta {
    login: {
      requireLogin: boolean,
      jumpToLogin: boolean
    }
  }
}

const DEFAULT_LOGIN_URL = "/login"
const DEFAULT_ROUTE_ERROR_URL = "/error/route"

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),

  routes: [
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
      name: 'home',
      component: HomeView,
      meta: {
        login: {
          requireLogin: true,
          jumpToLogin: false
        }
      }
    },
    {
      path: '/about',
      name: 'about',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('../views/AboutView.vue')
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
  if (!user.isValidIdentity(user)) {
    console.log('user login status is not found')
    if (to.meta.login.jumpToLogin) {
      console.log('redirect to default login page')
      return DEFAULT_LOGIN_URL
    } else {
      console.log('stop routing')
      return DEFAULT_ROUTE_ERROR_URL
    }
  }
  console.log('user login status found')
  return to
})

export default router
