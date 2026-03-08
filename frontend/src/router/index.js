import { createRouter, createWebHistory } from 'vue-router'
import DefaultLayout from '@/layouts/DefaultLayout.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/',
    component: DefaultLayout,
    children: [
      // ===== 公开页面 =====
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'jobs',
        name: 'JobList',
        component: () => import('@/views/jobs/JobList.vue'),
        meta: { title: '岗位列表' }
      },
      {
        path: 'jobs/:id',
        name: 'JobDetail',
        component: () => import('@/views/jobs/JobDetail.vue'),
        meta: { title: '岗位详情' }
      },
      {
        path: 'companies',
        name: 'CompanyList',
        component: () => import('@/views/companies/CompanyList.vue'),
        meta: { title: '企业列表' }
      },
      {
        path: 'companies/:id',
        name: 'CompanyDetail',
        component: () => import('@/views/companies/CompanyDetail.vue'),
        meta: { title: '企业详情' }
      },
      {
        path: 'jobfairs',
        name: 'JobFairList',
        component: () => import('@/views/jobfairs/JobFairList.vue'),
        meta: { title: '宣讲会列表' }
      },
      {
        path: 'announcements',
        name: 'AnnouncementList',
        component: () => import('@/views/announcements/AnnouncementList.vue'),
        meta: { title: '公告列表' }
      },
      {
        path: 'announcements/:id',
        name: 'AnnouncementDetail',
        component: () => import('@/views/announcements/AnnouncementDetail.vue'),
        meta: { title: '公告详情' }
      },

      // ===== 学生端页面 =====
      {
        path: 'my/profile',
        name: 'StudentProfile',
        component: () => import('@/views/student/Profile.vue'),
        meta: { title: '个人档案', requireAuth: true, role: 'STUDENT' }
      },
      {
        path: 'my/resumes',
        name: 'StudentResumes',
        component: () => import('@/views/student/ResumeList.vue'),
        meta: { title: '我的简历', requireAuth: true, role: 'STUDENT' }
      },
      {
        path: 'my/applications',
        name: 'StudentApplications',
        component: () => import('@/views/student/ApplicationList.vue'),
        meta: { title: '投递记录', requireAuth: true, role: 'STUDENT' }
      },
      {
        path: 'my/interviews',
        name: 'StudentInterviews',
        component: () => import('@/views/student/InterviewList.vue'),
        meta: { title: '我的面试', requireAuth: true, role: 'STUDENT' }
      },
      {
        path: 'my/offers',
        name: 'StudentOffers',
        component: () => import('@/views/student/OfferList.vue'),
        meta: { title: '我的Offer', requireAuth: true, role: 'STUDENT' }
      },
      {
        path: 'my/messages',
        name: 'StudentMessages',
        component: () => import('@/views/student/MessageList.vue'),
        meta: { title: '消息中心', requireAuth: true, role: 'STUDENT' }
      },

      // ===== 企业端页面 =====
      {
        path: 'company/dashboard',
        name: 'CompanyDashboard',
        component: () => import('@/views/company/Dashboard.vue'),
        meta: { title: '企业工作台', requireAuth: true, role: 'COMPANY' }
      },
      {
        path: 'company/profile',
        name: 'CompanyProfileEdit',
        component: () => import('@/views/company/CompanyProfile.vue'),
        meta: { title: '企业信息', requireAuth: true, role: 'COMPANY' }
      },
      {
        path: 'company/jobs',
        name: 'CompanyJobs',
        component: () => import('@/views/company/JobManage.vue'),
        meta: { title: '岗位管理', requireAuth: true, role: 'COMPANY' }
      },
      {
        path: 'company/applications',
        name: 'CompanyApplications',
        component: () => import('@/views/company/ApplicationManage.vue'),
        meta: { title: '收到的投递', requireAuth: true, role: 'COMPANY' }
      },
      {
        path: 'company/interviews',
        name: 'CompanyInterviews',
        component: () => import('@/views/company/InterviewManage.vue'),
        meta: { title: '面试管理', requireAuth: true, role: 'COMPANY' }
      },
      {
        path: 'company/offers',
        name: 'CompanyOffers',
        component: () => import('@/views/company/OfferManage.vue'),
        meta: { title: 'Offer管理', requireAuth: true, role: 'COMPANY' }
      },
      {
        path: 'company/job-fairs',
        name: 'CompanyJobFairs',
        component: () => import('@/views/company/JobFairManage.vue'),
        meta: { title: '宣讲会管理', requireAuth: true, role: 'COMPANY' }
      },
      {
        path: 'company/messages',
        name: 'CompanyMessages',
        component: () => import('@/views/company/MessageList.vue'),
        meta: { title: '消息中心', requireAuth: true, role: 'COMPANY' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/Home.vue'),
    meta: { title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior () {
    return { top: 0 }
  }
})

// 路由守卫
router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 校园招聘平台` : '校园招聘平台'

  if (to.meta.requireAuth) {
    const token = localStorage.getItem('token')
    if (!token) {
      next({ path: '/login', query: { redirect: to.fullPath } })
      return
    }
    // 检查角色权限
    if (to.meta.role) {
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      if (userInfo.role !== to.meta.role) {
        next({ path: '/' })
        return
      }
    }
  }
  next()
})

export default router
