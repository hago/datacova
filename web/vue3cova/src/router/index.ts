import { identityStore } from '@/stores/identitystore'
import { createRouter, createWebHistory } from 'vue-router'
import RouteErrorView from '@/views/auth/RouteErrorView.vue'
import LoginView from '@/views/auth/LoginView.vue'
import WorkspaceView from '@/views/WorkspaceView.vue'
import WelcomeView from '@/views/WelcomeView.vue'

declare module 'vue-router' {
  interface RouteMeta {
    login: {
      requireLogin: boolean,
      jumpToLogin: boolean
    },
    data?: any[]
    prerequisiteCheck?: (data?: any[]) => boolean
  }
}

const DEFAULT_LOGIN_ROUTE = "/login"
const DEFAULT_ROUTE_ERROR_ROUTE = "/error/route"
const DEFAULT_ROUTE_ERROR_ROUTE_LACK_PARAM = "/error/route/lackparam"

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),

  routes: [
    {
      path: '/',
      name: 'welcome',
      component: WelcomeView,
      meta: {
        login: {
          requireLogin: false,
          jumpToLogin: false
        }
      }
    },
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
      path: '/workspace/:id',
      name: 'workspace',
      component: WorkspaceView,
      props: true,
      meta: {
        login: {
          requireLogin: true,
          jumpToLogin: true
        },
        prerequisiteCheck: (data) => {
          if (data === undefined) {
            return false
          }
          return true
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
  // if ((to.meta.prerequisiteCheck !== undefined) && !to.meta.prerequisiteCheck(to.meta.data)) {
  //   return DEFAULT_ROUTE_ERROR_ROUTE_LACK_PARAM
  // }
  return true
})

export default router
