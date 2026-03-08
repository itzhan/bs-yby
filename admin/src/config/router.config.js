// eslint-disable-next-line
import { UserLayout, BasicLayout, BlankLayout } from '@/layouts'
import { bxAnaalyse } from '@/core/icons'

const RouteView = {
  name: 'RouteView',
  render: h => h('router-view')
}

export const asyncRouterMap = [
  {
    path: '/',
    name: 'index',
    component: BasicLayout,
    meta: { title: '首页' },
    redirect: '/dashboard/workplace',
    children: [
      // 工作台
      {
        path: '/dashboard',
        name: 'dashboard',
        redirect: '/dashboard/workplace',
        component: RouteView,
        meta: { title: '工作台', keepAlive: true, icon: bxAnaalyse, permission: ['dashboard'] },
        children: [
          {
            path: '/dashboard/workplace',
            name: 'Workplace',
            component: () => import('@/views/dashboard/Workplace'),
            meta: { title: '数据概览', keepAlive: true, permission: ['dashboard'] }
          }
        ]
      },
      // 用户管理
      {
        path: '/recruit/users',
        name: 'UserList',
        component: () => import('@/views/recruit/UserList'),
        meta: { title: '用户管理', icon: 'team', permission: ['user'] }
      },
      // 企业管理
      {
        path: '/recruit/companies',
        name: 'CompanyList',
        component: () => import('@/views/recruit/CompanyList'),
        meta: { title: '企业审核', icon: 'bank', permission: ['company'] }
      },
      // 招聘管理
      {
        path: '/recruit',
        name: 'recruit',
        component: RouteView,
        meta: { title: '招聘管理', icon: 'solution', permission: ['job'] },
        children: [
          {
            path: '/recruit/jobs',
            name: 'JobList',
            component: () => import('@/views/recruit/JobList'),
            meta: { title: '岗位管理', permission: ['job'] }
          },
          {
            path: '/recruit/applications',
            name: 'ApplicationList',
            component: () => import('@/views/recruit/ApplicationList'),
            meta: { title: '投递管理', permission: ['application'] }
          },
          {
            path: '/recruit/job-fairs',
            name: 'JobFairList',
            component: () => import('@/views/recruit/JobFairList'),
            meta: { title: '宣讲会管理', permission: ['jobfair'] }
          }
        ]
      },
      // 公告管理
      {
        path: '/recruit/announcements',
        name: 'AnnouncementList',
        component: () => import('@/views/recruit/AnnouncementList'),
        meta: { title: '公告管理', icon: 'notification', permission: ['announcement'] }
      },
      // 系统管理
      {
        path: '/system',
        name: 'system',
        component: RouteView,
        meta: { title: '系统管理', icon: 'setting', permission: ['system'] },
        children: [
          {
            path: '/system/operation-logs',
            name: 'OperationLog',
            component: () => import('@/views/recruit/OperationLog'),
            meta: { title: '操作日志', permission: ['system'] }
          }
        ]
      }
    ]
  },
  {
    path: '*',
    redirect: '/404',
    hidden: true
  }
]

/**
 * 基础路由
 * @type { *[] }
 */
export const constantRouterMap = [
  {
    path: '/user',
    component: UserLayout,
    redirect: '/user/login',
    hidden: true,
    children: [
      {
        path: 'login',
        name: 'login',
        component: () => import(/* webpackChunkName: "user" */ '@/views/user/Login')
      },
      {
        path: 'register',
        name: 'register',
        component: () => import(/* webpackChunkName: "user" */ '@/views/user/Register')
      },
      {
        path: 'register-result',
        name: 'registerResult',
        component: () => import(/* webpackChunkName: "user" */ '@/views/user/RegisterResult')
      }
    ]
  },

  {
    path: '/404',
    component: () => import(/* webpackChunkName: "fail" */ '@/views/exception/404')
  }
]
