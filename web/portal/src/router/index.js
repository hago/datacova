import Vue from 'vue'
import Router from 'vue-router'
import Vuex from 'vuex'
// import HelloWorld from '@/components/HelloWorld'
// import Partners from '@/components/Partners'
// import JobList from '@/components/JobList'
// import JobEdit from '@/components/JobEdit'
// import PartnerEdit from '@/components/PartnerEdit'
import Login from '@/components/Login'
import Main from '@/components/Main'
import Index from '@/components/Index'
import Connection from '@/components/connection/Connection'
import WorkspaceMember from '@/components/user/WorkspaceMember'
import User from '@/components/user/User'
import Task from '@/components/task/Task'
import TaskFileUpload from '@/components/task/TaskFileUpload'
import TaskExecution from '@/components/execution/TaskExecution'
import Register from '@/components/Register.vue'

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
      props: true
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
      component: User,
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
      props: true
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
    }
  ],
  linkActiveClass: 'nav-link',
  linkExactActiveClass: 'active'
})

export default route
