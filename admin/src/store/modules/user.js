import storage from 'store'
import expirePlugin from 'store/plugins/expire'
import { login, getInfo, logout } from '@/api/login'
import { ACCESS_TOKEN } from '@/store/mutation-types'
import { welcome } from '@/utils/util'

storage.addPlugin(expirePlugin)
const user = {
  state: {
    token: '',
    name: '',
    welcome: '',
    avatar: '',
    roles: [],
    info: {}
  },

  mutations: {
    SET_TOKEN: (state, token) => {
      state.token = token
    },
    SET_NAME: (state, { name, welcome }) => {
      state.name = name
      state.welcome = welcome
    },
    SET_AVATAR: (state, avatar) => {
      state.avatar = avatar
    },
    SET_ROLES: (state, roles) => {
      state.roles = roles
    },
    SET_INFO: (state, info) => {
      state.info = info
    }
  },

  actions: {
    // 登录
    Login ({ commit }, userInfo) {
      return new Promise((resolve, reject) => {
        login(userInfo).then(response => {
          const result = response.data
          storage.set(ACCESS_TOKEN, result.token, new Date().getTime() + 24 * 60 * 60 * 1000)
          commit('SET_TOKEN', result.token)
          resolve()
        }).catch(error => {
          reject(error)
        })
      })
    },

    // 获取用户信息
    GetInfo ({ commit }) {
      return new Promise((resolve, reject) => {
        getInfo().then(response => {
          const userInfo = response.data
          // 构建角色权限结构
          const role = {
            id: userInfo.role,
            name: userInfo.role,
            permissions: [{
              permissionId: 'dashboard',
              permissionName: '仪表盘',
              actionList: ['query']
            }]
          }
          // 管理员拥有所有权限
          if (userInfo.role === 'ADMIN') {
            role.permissions = [
              { permissionId: 'dashboard', permissionName: '仪表盘', actionList: ['query', 'add', 'edit', 'delete'] },
              { permissionId: 'user', permissionName: '用户管理', actionList: ['query', 'add', 'edit', 'delete'] },
              { permissionId: 'company', permissionName: '企业管理', actionList: ['query', 'add', 'edit', 'delete'] },
              { permissionId: 'job', permissionName: '岗位管理', actionList: ['query', 'add', 'edit', 'delete'] },
              { permissionId: 'application', permissionName: '投递管理', actionList: ['query', 'add', 'edit', 'delete'] },
              { permissionId: 'jobfair', permissionName: '宣讲会', actionList: ['query', 'add', 'edit', 'delete'] },
              { permissionId: 'announcement', permissionName: '公告管理', actionList: ['query', 'add', 'edit', 'delete'] },
              { permissionId: 'system', permissionName: '系统管理', actionList: ['query', 'add', 'edit', 'delete'] }
            ]
          }
          role.permissionList = role.permissions.map(p => p.permissionId)

          const result = {
            ...userInfo,
            role: role,
            name: userInfo.nickname || userInfo.username,
            avatar: userInfo.avatar || '/avatar2.jpg'
          }

          commit('SET_ROLES', role)
          commit('SET_INFO', result)
          commit('SET_NAME', { name: result.name, welcome: welcome() })
          commit('SET_AVATAR', result.avatar)
          resolve(result)
        }).catch(error => {
          reject(error)
        })
      })
    },

    // 登出
    Logout ({ commit, state }) {
      return new Promise((resolve) => {
        logout(state.token).then(() => {
          commit('SET_TOKEN', '')
          commit('SET_ROLES', [])
          storage.remove(ACCESS_TOKEN)
          resolve()
        }).catch(() => {
          commit('SET_TOKEN', '')
          commit('SET_ROLES', [])
          storage.remove(ACCESS_TOKEN)
          resolve()
        })
      })
    }
  }
}

export default user
