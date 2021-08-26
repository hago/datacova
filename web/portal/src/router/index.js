import Vue from 'vue'
import Router from 'vue-router'
import Vuex from 'vuex'
import Login from '@/components/Login'
import Main from '@/components/Main'
import Index from '@/components/Index'
import Connection from '@/components/connection/Connection'
import WorkspaceMember from '@/components/user/WorkspaceMember'
import * as UserComponent from '@/components/user/User'
import Task from '@/components/task/Task'
import TaskFileUpload from '@/components/task/TaskFileUpload'
import TaskExecution from '@/components/execution/TaskExecution'
import Register from '@/components/Register.vue'
import ActivateRegistration from '@/components/ActivateRegistration.vue'
import ValidationRule from '@/components/rules/ValidationRule.vue'
import User from '@/apis/user.js'
import AdminIndex from '@/components/admin/Index.vue'

Vue.use(Vuex)
Vue.use(Router)

const route = new Router({
  mode: 'history',
  routes: [
    {
      path: '/',
      name: 'Index',
      component: Index,
      props: true
    },
    {
      path: '/index',
      name: 'Index',
      component: Index,
      props: true
    },
    {
      path: '/main',
      name: 'Main',
      component: Main,
      props: true,
      meta: {
        requireAuth: true
      }
    },
    {
      path: '/login',
      name: 'Login',
      component: Login,
      props: true
    },
    {
      path: '/connection/:workspaceId/:id',
      name: 'Connection',
      component: Connection,
      props: true
    },
    {
      path: '/user/:workspaceId/:type/:id',
      name: 'WorkspaceMember',
      component: WorkspaceMember,
      props: true
    },
    {
      path: '/user/:id',
      name: 'User',
      component: UserComponent,
      props: true
    },
    {
      path: '/task/:workspaceId/:id',
      name: 'Task',
      component: Task,
      props: true
    },
    {
      path: '/task/:workspaceId/:id/upload',
      name: 'TaskFileUpload',
      component: TaskFileUpload,
      props: true,
      meta: {
        requireAuth: true
      }
    },
    {
      path: '/execution/:id',
      name: 'TaskExecution',
      component: TaskExecution,
      props: true
    },
    {
      path: '/register',
      name: 'Register',
      component: Register,
      props: true
    },
    {
      path: '/user/activate/:code',
      name: 'ActivateRegistration',
      component: ActivateRegistration,
      props: false
    },
    {
      path: '/workspace/:wkid/validation/:ruleid',
      name: 'ValidationRule',
      component: ValidationRule,
      props: true
    },
    {
      path: '/admin',
      name: 'AdminIndex',
      component: AdminIndex,
      props: true,
      meta: {
        requireAuth: true,
        requireAdmin: true
      }
    }
  ],
  linkActiveClass: 'nav-link',
  linkExactActiveClass: 'active'
})

route.beforeEach((to, from, next) => {
  console.log(`from: ${from.path}, to: ${to.path}`)
  if (to.meta === undefined) {
    console.log('doesn\'t require auth')
    next()
    return
  }
  console.log('require auth')
  let u = (new User()).getUser()
  if (to.meta.requireAuth) {
    if (u == null) {
      console.log('not logged')
      if (next.path !== '/login') {
        console.log('goto /login')
        next({
          name: 'Login',
          params: {
            return: to
          }
        })
      } else {
        console.log('goto /login by intension')
        next()
      }
      return
    }
  }
  console.log(u.admin)
  console.log(to.meta)
  if (to.meta.requireAdmin && (u.admin === undefined)) {
    next({
      name: 'NotFound',
      params: {
        message: 'Access Denied'
      }
    })
    return
  }
  next()
})

export default route
