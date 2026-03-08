# 校园招聘系统 API 文档

> **Base URL**: `http://localhost:8080`
> **版本**: v1.0
> **更新日期**: 2026-02-07

---

## 目录

1. [全局说明](#1-全局说明)
2. [认证模块](#2-认证模块)
3. [用户管理](#3-用户管理)
4. [学生档案](#4-学生档案)
5. [企业管理](#5-企业管理)
6. [岗位管理](#6-岗位管理)
7. [简历管理](#7-简历管理)
8. [投递管理](#8-投递管理)
9. [面试管理](#9-面试管理)
10. [Offer管理](#10-offer管理)
11. [宣讲会管理](#11-宣讲会管理)
12. [公告管理](#12-公告管理)
13. [消息通知](#13-消息通知)
14. [操作日志](#14-操作日志)
15. [数据统计](#15-数据统计)
16. [文件上传](#16-文件上传)

---

## 1. 全局说明

### 1.1 认证方式

系统采用 **JWT (JSON Web Token)** 认证。登录成功后，服务端返回 `token`，后续请求需在请求头中携带：

```
Authorization: Bearer <token>
```

### 1.2 请求格式

| 请求头 | 值 |
|---|---|
| `Content-Type` | `application/json`（文件上传接口为 `multipart/form-data`） |
| `Authorization` | `Bearer <token>`（需认证的接口） |

### 1.3 分页参数

所有列表接口支持分页查询：

| 参数 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `page` | Integer | 1 | 页码（从 1 开始） |
| `size` | Integer | 10 | 每页条数 |

### 1.4 统一响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... }
}
```

分页响应 `data` 结构：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 100,
    "list": [ ... ],
    "page": 1,
    "size": 10
  }
}
```

### 1.5 错误码

| 错误码 | 说明 | 常见场景 |
|---|---|---|
| `200` | 操作成功 | 请求正常处理完成 |
| `400` | 请求参数错误 | 表单验证失败、参数缺失或格式错误 |
| `401` | 未授权 | 未登录或 Token 已过期 |
| `403` | 禁止访问 | 无权限访问该资源 |
| `404` | 资源不存在 | 请求的数据不存在 |
| `500` | 服务器内部错误 | 系统异常 |

错误响应示例：

```json
{
  "code": 401,
  "message": "未授权，请先登录",
  "data": null
}
```

### 1.6 用户角色

| 角色标识 | 角色名称 | 说明 |
|---|---|---|
| `ADMIN` | 管理员 | 系统管理，审核企业、岗位、宣讲会，管理公告和用户 |
| `STUDENT` | 学生 | 浏览岗位、投递简历、参加面试、接收 Offer |
| `COMPANY` | 企业 | 发布岗位、筛选简历、发起面试、发放 Offer |

---

## 2. 认证模块

> 前缀：`/api/auth`

### 2.1 用户登录

**POST** `/api/auth/login`

**权限**：公开

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `username` | String | 是 | 用户名 |
| `password` | String | 是 | 密码 |

**请求示例**：

```json
{
  "username": "zhangsan",
  "password": "123456"
}
```

**响应示例**：

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "username": "zhangsan",
      "nickname": "张三",
      "role": "STUDENT",
      "avatar": "/uploads/avatar/default.png",
      "email": "zhangsan@example.com",
      "phone": "13800138000",
      "status": 1,
      "createTime": "2026-01-15 10:30:00"
    }
  }
}
```

**错误示例**：

```json
{
  "code": 400,
  "message": "用户名或密码错误",
  "data": null
}
```

---

### 2.2 用户注册

**POST** `/api/auth/register`

**权限**：公开

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `username` | String | 是 | 用户名（4-20位，字母数字下划线） |
| `password` | String | 是 | 密码（6-20位） |
| `nickname` | String | 是 | 昵称 |
| `role` | String | 是 | 角色（`STUDENT` 或 `COMPANY`） |
| `email` | String | 否 | 邮箱 |
| `phone` | String | 否 | 手机号 |

**请求示例**：

```json
{
  "username": "lisi2026",
  "password": "abc123456",
  "nickname": "李四",
  "role": "STUDENT",
  "email": "lisi@example.com",
  "phone": "13900139000"
}
```

**响应示例**：

```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "id": 25,
    "username": "lisi2026",
    "nickname": "李四",
    "role": "STUDENT",
    "status": 1,
    "createTime": "2026-02-07 14:00:00"
  }
}
```

**错误示例**：

```json
{
  "code": 400,
  "message": "用户名已存在",
  "data": null
}
```

---

## 3. 用户管理

> 前缀：`/api/users`

### 3.1 获取用户列表

**GET** `/api/users`

**权限**：管理员（`ADMIN`）

**查询参数**：

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `page` | Integer | 否 | 页码，默认 1 |
| `size` | Integer | 否 | 每页条数，默认 10 |
| `role` | String | 否 | 按角色筛选（`ADMIN`/`STUDENT`/`COMPANY`） |
| `keyword` | String | 否 | 搜索关键词（匹配用户名或昵称） |

**请求示例**：

```
GET /api/users?page=1&size=10&role=STUDENT&keyword=张
```

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 56,
    "list": [
      {
        "id": 1,
        "username": "zhangsan",
        "nickname": "张三",
        "role": "STUDENT",
        "avatar": "/uploads/avatar/1.png",
        "email": "zhangsan@example.com",
        "phone": "13800138000",
        "status": 1,
        "createTime": "2026-01-15 10:30:00"
      },
      {
        "id": 5,
        "username": "zhangwei",
        "nickname": "张伟",
        "role": "STUDENT",
        "avatar": "/uploads/avatar/default.png",
        "email": "zhangwei@example.com",
        "phone": "13700137000",
        "status": 1,
        "createTime": "2026-01-20 09:15:00"
      }
    ],
    "page": 1,
    "size": 10
  }
}
```

---

### 3.2 获取当前用户信息

**GET** `/api/users/current`

**权限**：已登录用户

**请求示例**：

```
GET /api/users/current
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "zhangsan",
    "nickname": "张三",
    "role": "STUDENT",
    "avatar": "/uploads/avatar/1.png",
    "email": "zhangsan@example.com",
    "phone": "13800138000",
    "status": 1,
    "createTime": "2026-01-15 10:30:00"
  }
}
```

---

### 3.3 获取用户详情

**GET** `/api/users/{id}`

**权限**：已登录用户

**路径参数**：

| 参数 | 类型 | 说明 |
|---|---|---|
| `id` | Long | 用户 ID |

**请求示例**：

```
GET /api/users/1
```

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "zhangsan",
    "nickname": "张三",
    "role": "STUDENT",
    "avatar": "/uploads/avatar/1.png",
    "email": "zhangsan@example.com",
    "phone": "13800138000",
    "status": 1,
    "createTime": "2026-01-15 10:30:00"
  }
}
```

---

### 3.4 更新用户信息

**PUT** `/api/users/{id}`

**权限**：本人或管理员

**路径参数**：

| 参数 | 类型 | 说明 |
|---|---|---|
| `id` | Long | 用户 ID |

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `nickname` | String | 否 | 昵称 |
| `avatar` | String | 否 | 头像路径 |
| `email` | String | 否 | 邮箱 |
| `phone` | String | 否 | 手机号 |
| `password` | String | 否 | 新密码（传入则修改密码） |

**请求示例**：

```json
{
  "nickname": "张三丰",
  "email": "zhangsan_new@example.com",
  "phone": "13800138001"
}
```

**响应示例**：

```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 1,
    "username": "zhangsan",
    "nickname": "张三丰",
    "role": "STUDENT",
    "avatar": "/uploads/avatar/1.png",
    "email": "zhangsan_new@example.com",
    "phone": "13800138001",
    "status": 1,
    "createTime": "2026-01-15 10:30:00"
  }
}
```

---

### 3.5 更新用户状态

**PUT** `/api/users/{id}/status`

**权限**：管理员（`ADMIN`）

**路径参数**：

| 参数 | 类型 | 说明 |
|---|---|---|
| `id` | Long | 用户 ID |

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `status` | Integer | 是 | 用户状态：`1` 正常、`0` 禁用 |

**请求示例**：

```json
{
  "status": 0
}
```

**响应示例**：

```json
{
  "code": 200,
  "message": "状态更新成功",
  "data": null
}
```

---

### 3.6 删除用户

**DELETE** `/api/users/{id}`

**权限**：管理员（`ADMIN`）

**路径参数**：

| 参数 | 类型 | 说明 |
|---|---|---|
| `id` | Long | 用户 ID |

**请求示例**：

```
DELETE /api/users/25
```

**响应示例**：

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

## 4. 学生档案

> 前缀：`/api/student-profile`

### 4.1 获取当前学生档案

**GET** `/api/student-profile/current`

**权限**：学生（`STUDENT`）

**请求示例**：

```
GET /api/student-profile/current
```

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "userId": 1,
    "realName": "张三",
    "gender": "男",
    "birthday": "2002-05-15",
    "university": "北京理工大学",
    "major": "计算机科学与技术",
    "education": "本科",
    "graduationYear": 2026,
    "gpa": "3.8",
    "skills": "Java, Spring Boot, Vue.js, MySQL",
    "selfIntroduction": "热爱编程，熟悉Java后端开发，有多个项目实战经验。",
    "expectedCity": "北京",
    "expectedSalary": "8000-12000",
    "expectedPosition": "Java开发工程师",
    "createTime": "2026-01-15 10:35:00",
    "updateTime": "2026-02-01 16:20:00"
  }
}
```

---

### 4.2 查看学生档案

**GET** `/api/student-profile/{userId}`

**权限**：已登录用户

**路径参数**：

| 参数 | 类型 | 说明 |
|---|---|---|
| `userId` | Long | 学生的用户 ID |

**请求示例**：

```
GET /api/student-profile/1
```

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "userId": 1,
    "realName": "张三",
    "gender": "男",
    "birthday": "2002-05-15",
    "university": "北京理工大学",
    "major": "计算机科学与技术",
    "education": "本科",
    "graduationYear": 2026,
    "gpa": "3.8",
    "skills": "Java, Spring Boot, Vue.js, MySQL",
    "selfIntroduction": "热爱编程，熟悉Java后端开发，有多个项目实战经验。",
    "expectedCity": "北京",
    "expectedSalary": "8000-12000",
    "expectedPosition": "Java开发工程师",
    "createTime": "2026-01-15 10:35:00",
    "updateTime": "2026-02-01 16:20:00"
  }
}
```

---

### 4.3 更新学生档案

**PUT** `/api/student-profile`

**权限**：学生（`STUDENT`）

**请求参数（StudentProfileDTO）**：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `realName` | String | 否 | 真实姓名 |
| `gender` | String | 否 | 性别（男/女） |
| `birthday` | String | 否 | 出生日期（yyyy-MM-dd） |
| `university` | String | 否 | 毕业院校 |
| `major` | String | 否 | 专业 |
| `education` | String | 否 | 学历（本科/硕士/博士） |
| `graduationYear` | Integer | 否 | 毕业年份 |
| `gpa` | String | 否 | 绩点/成绩 |
| `skills` | String | 否 | 技能标签（逗号分隔） |
| `selfIntroduction` | String | 否 | 自我介绍 |
| `expectedCity` | String | 否 | 期望工作城市 |
| `expectedSalary` | String | 否 | 期望薪资 |
| `expectedPosition` | String | 否 | 期望岗位 |

**请求示例**：

```json
{
  "realName": "张三",
  "gender": "男",
  "birthday": "2002-05-15",
  "university": "北京理工大学",
  "major": "计算机科学与技术",
  "education": "本科",
  "graduationYear": 2026,
  "gpa": "3.8",
  "skills": "Java, Spring Boot, Vue.js, MySQL, Redis",
  "selfIntroduction": "热爱编程，熟悉Java后端开发，有多个项目实战经验，曾在字节跳动实习3个月。",
  "expectedCity": "北京",
  "expectedSalary": "10000-15000",
  "expectedPosition": "Java开发工程师"
}
```

**响应示例**：

```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 1,
    "userId": 1,
    "realName": "张三",
    "gender": "男",
    "birthday": "2002-05-15",
    "university": "北京理工大学",
    "major": "计算机科学与技术",
    "education": "本科",
    "graduationYear": 2026,
    "gpa": "3.8",
    "skills": "Java, Spring Boot, Vue.js, MySQL, Redis",
    "selfIntroduction": "热爱编程，熟悉Java后端开发，有多个项目实战经验，曾在字节跳动实习3个月。",
    "expectedCity": "北京",
    "expectedSalary": "10000-15000",
    "expectedPosition": "Java开发工程师",
    "createTime": "2026-01-15 10:35:00",
    "updateTime": "2026-02-07 14:30:00"
  }
}
```

---

## 5. 企业管理

> 前缀：`/api/companies`

### 5.1 获取企业列表

**GET** `/api/companies`

**权限**：公开访问（公开端仅返回 `auditStatus=1` 已审核通过的企业），管理员可查看所有企业

**查询参数**：

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `page` | Integer | 否 | 页码，默认 1 |
| `size` | Integer | 否 | 每页条数，默认 10 |
| `auditStatus` | Integer | 否 | 审核状态：`0` 待审核、`1` 已通过、`2` 已拒绝 |
| `keyword` | String | 否 | 搜索关键词（匹配企业名称） |

**请求示例**：

```
GET /api/companies?page=1&size=10&keyword=科技
```

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 32,
    "list": [
      {
        "id": 1,
        "userId": 10,
        "companyName": "星辰科技有限公司",
        "logo": "/uploads/company/logo_1.png",
        "industry": "互联网/IT",
        "scale": "100-499人",
        "city": "北京",
        "address": "北京市海淀区中关村软件园8号楼",
        "contactPerson": "王经理",
        "contactPhone": "010-88886666",
        "contactEmail": "hr@xingchen.com",
        "website": "https://www.xingchen.com",
        "description": "星辰科技是一家专注于人工智能和大数据的高新技术企业，致力于为企业提供智能化解决方案。",
        "auditStatus": 1,
        "auditRemark": null,
        "createTime": "2026-01-10 08:00:00",
        "updateTime": "2026-01-12 10:00:00"
      },
      {
        "id": 3,
        "userId": 15,
        "companyName": "未来科技集团",
        "logo": "/uploads/company/logo_3.png",
        "industry": "互联网/IT",
        "scale": "500-999人",
        "city": "深圳",
        "address": "深圳市南山区科技园南区",
        "contactPerson": "李经理",
        "contactPhone": "0755-66668888",
        "contactEmail": "hr@weilai.com",
        "website": "https://www.weilai.com",
        "description": "未来科技集团是国内领先的云计算与SaaS服务提供商。",
        "auditStatus": 1,
        "auditRemark": null,
        "createTime": "2026-01-11 09:00:00",
        "updateTime": "2026-01-13 14:00:00"
      }
    ],
    "page": 1,
    "size": 10
  }
}
```

---

### 5.2 获取当前企业档案

**GET** `/api/companies/current`

**权限**：企业（`COMPANY`）

**请求示例**：

```
GET /api/companies/current
```

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "userId": 10,
    "companyName": "星辰科技有限公司",
    "logo": "/uploads/company/logo_1.png",
    "industry": "互联网/IT",
    "scale": "100-499人",
    "city": "北京",
    "address": "北京市海淀区中关村软件园8号楼",
    "contactPerson": "王经理",
    "contactPhone": "010-88886666",
    "contactEmail": "hr@xingchen.com",
    "website": "https://www.xingchen.com",
    "description": "星辰科技是一家专注于人工智能和大数据的高新技术企业。",
    "auditStatus": 1,
    "auditRemark": null,
    "createTime": "2026-01-10 08:00:00",
    "updateTime": "2026-01-12 10:00:00"
  }
}
```

---

### 5.3 获取企业详情

**GET** `/api/companies/{id}`

**权限**：公开

**路径参数**：

| 参数 | 类型 | 说明 |
|---|---|---|
| `id` | Long | 企业 ID |

**请求示例**：

```
GET /api/companies/1
```

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "userId": 10,
    "companyName": "星辰科技有限公司",
    "logo": "/uploads/company/logo_1.png",
    "industry": "互联网/IT",
    "scale": "100-499人",
    "city": "北京",
    "address": "北京市海淀区中关村软件园8号楼",
    "contactPerson": "王经理",
    "contactPhone": "010-88886666",
    "contactEmail": "hr@xingchen.com",
    "website": "https://www.xingchen.com",
    "description": "星辰科技是一家专注于人工智能和大数据的高新技术企业，致力于为企业提供智能化解决方案。",
    "auditStatus": 1,
    "auditRemark": null,
    "createTime": "2026-01-10 08:00:00",
    "updateTime": "2026-01-12 10:00:00"
  }
}
```

---

### 5.4 更新企业档案

**PUT** `/api/companies`

**权限**：企业（`COMPANY`）

**请求参数（CompanyProfileDTO）**：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `companyName` | String | 否 | 企业名称 |
| `logo` | String | 否 | 企业 Logo 路径 |
| `industry` | String | 否 | 所属行业 |
| `scale` | String | 否 | 企业规模 |
| `city` | String | 否 | 所在城市 |
| `address` | String | 否 | 详细地址 |
| `contactPerson` | String | 否 | 联系人 |
| `contactPhone` | String | 否 | 联系电话 |
| `contactEmail` | String | 否 | 联系邮箱 |
| `website` | String | 否 | 官网地址 |
| `description` | String | 否 | 企业简介 |

**请求示例**：

```json
{
  "companyName": "星辰科技有限公司",
  "logo": "/uploads/company/logo_1_new.png",
  "industry": "互联网/IT",
  "scale": "500-999人",
  "city": "北京",
  "address": "北京市海淀区中关村软件园8号楼",
  "contactPerson": "王经理",
  "contactPhone": "010-88886666",
  "contactEmail": "hr@xingchen.com",
  "website": "https://www.xingchen.com",
  "description": "星辰科技是一家专注于人工智能和大数据的高新技术企业，团队已扩展至500人规模。"
}
```

**响应示例**：

```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 1,
    "userId": 10,
    "companyName": "星辰科技有限公司",
    "logo": "/uploads/company/logo_1_new.png",
    "industry": "互联网/IT",
    "scale": "500-999人",
    "city": "北京",
    "address": "北京市海淀区中关村软件园8号楼",
    "contactPerson": "王经理",
    "contactPhone": "010-88886666",
    "contactEmail": "hr@xingchen.com",
    "website": "https://www.xingchen.com",
    "description": "星辰科技是一家专注于人工智能和大数据的高新技术企业，团队已扩展至500人规模。",
    "auditStatus": 0,
    "auditRemark": null,
    "createTime": "2026-01-10 08:00:00",
    "updateTime": "2026-02-07 15:00:00"
  }
}
```

> **注意**：企业更新档案后，审核状态将重置为 `0`（待审核），需管理员重新审核。

---

### 5.5 审核企业

**PUT** `/api/companies/{id}/audit`

**权限**：管理员（`ADMIN`）

**路径参数**：

| 参数 | 类型 | 说明 |
|---|---|---|
| `id` | Long | 企业 ID |

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `auditStatus` | Integer | 是 | 审核状态：`1` 通过、`2` 拒绝 |
| `auditRemark` | String | 否 | 审核备注（拒绝时建议填写原因） |

**请求示例（通过）**：

```json
{
  "auditStatus": 1,
  "auditRemark": "资质齐全，审核通过"
}
```

**请求示例（拒绝）**：

```json
{
  "auditStatus": 2,
  "auditRemark": "营业执照信息不完整，请补充后重新提交"
}
```

**响应示例**：

```json
{
  "code": 200,
  "message": "审核成功",
  "data": null
}
```

---

## 6. 岗位管理

> 前缀：`/api/jobs`

### 6.1 获取岗位列表

**GET** `/api/jobs`

**权限**：公开访问（公开端仅返回 `status=2` 已上线的岗位），企业/管理员可查看更多状态

**查询参数**：

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `page` | Integer | 否 | 页码，默认 1 |
| `size` | Integer | 否 | 每页条数，默认 10 |
| `keyword` | String | 否 | 搜索关键词（匹配岗位名称或企业名称） |
| `city` | String | 否 | 工作城市 |
| `category` | String | 否 | 岗位类别 |
| `jobType` | String | 否 | 工作类型（全职/实习/兼职） |
| `status` | Integer | 否 | 岗位状态 |
| `companyId` | Long | 否 | 企业 ID（查看指定企业的岗位） |

**岗位状态说明**：

| 状态值 | 说明 |
|---|---|
| `0` | 草稿 |
| `1` | 待审核 |
| `2` | 已上线（审核通过） |
| `3` | 已下线 |
| `4` | 审核拒绝 |

**请求示例**：

```
GET /api/jobs?page=1&size=10&keyword=Java&city=北京&jobType=全职
```

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 128,
    "list": [
      {
        "id": 1,
        "companyId": 1,
        "companyName": "星辰科技有限公司",
        "companyLogo": "/uploads/company/logo_1.png",
        "title": "Java后端开发工程师",
        "category": "技术",
        "jobType": "全职",
        "city": "北京",
        "address": "北京市海淀区中关村软件园",
        "salaryMin": 12000,
        "salaryMax": 20000,
        "education": "本科",
        "experience": "应届生",
        "description": "负责公司核心业务系统的后端开发，参与系统架构设计与优化。",
        "requirement": "1. 计算机相关专业本科及以上学历；\n2. 熟悉 Java、Spring Boot 框架；\n3. 熟悉 MySQL 数据库；\n4. 有良好的编码习惯和团队协作能力。",
        "benefits": "五险一金, 带薪年假, 弹性工作, 免费三餐, 定期团建",
        "headcount": 5,
        "status": 2,
        "viewCount": 256,
        "applicationCount": 32,
        "createTime": "2026-01-20 09:00:00",
        "updateTime": "2026-01-22 14:00:00"
      },
      {
        "id": 5,
        "companyId": 3,
        "companyName": "未来科技集团",
        "companyLogo": "/uploads/company/logo_3.png",
        "title": "Java全栈开发实习生",
        "category": "技术",
        "jobType": "实习",
        "city": "北京",
        "address": "北京市朝阳区望京SOHO",
        "salaryMin": 4000,
        "salaryMax": 6000,
        "education": "本科",
        "experience": "在校生",
        "description": "参与公司内部管理平台的全栈开发，有导师一对一带教。",
        "requirement": "1. 计算机相关专业在读；\n2. 了解 Java 和前端基础知识；\n3. 学习能力强，态度积极。",
        "benefits": "实习津贴, 免费午餐, 转正机会",
        "headcount": 3,
        "status": 2,
        "viewCount": 180,
        "applicationCount": 45,
        "createTime": "2026-01-25 10:00:00",
        "updateTime": "2026-01-25 10:00:00"
      }
    ],
    "page": 1,
    "size": 10
  }
}
```

---

### 6.2 获取岗位详情

**GET** `/api/jobs/{id}`

**权限**：公开

**路径参数**：

| 参数 | 类型 | 说明 |
|---|---|---|
| `id` | Long | 岗位 ID |

**请求示例**：

```
GET /api/jobs/1
```

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "companyId": 1,
    "companyName": "星辰科技有限公司",
    "companyLogo": "/uploads/company/logo_1.png",
    "title": "Java后端开发工程师",
    "category": "技术",
    "jobType": "全职",
    "city": "北京",
    "address": "北京市海淀区中关村软件园",
    "salaryMin": 12000,
    "salaryMax": 20000,
    "education": "本科",
    "experience": "应届生",
    "description": "负责公司核心业务系统的后端开发，参与系统架构设计与优化。",
    "requirement": "1. 计算机相关专业本科及以上学历；\n2. 熟悉 Java、Spring Boot 框架；\n3. 熟悉 MySQL 数据库；\n4. 有良好的编码习惯和团队协作能力。",
    "benefits": "五险一金, 带薪年假, 弹性工作, 免费三餐, 定期团建",
    "headcount": 5,
    "status": 2,
    "viewCount": 257,
    "applicationCount": 32,
    "createTime": "2026-01-20 09:00:00",
    "updateTime": "2026-01-22 14:00:00"
  }
}
```

---

### 6.3 发布岗位

**POST** `/api/jobs`

**权限**：企业（`COMPANY`）

**请求参数（JobDTO）**：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `title` | String | 是 | 岗位名称 |
| `category` | String | 是 | 岗位类别（技术/产品/设计/运营/市场/财务/人事/其他） |
| `jobType` | String | 是 | 工作类型（全职/实习/兼职） |
| `city` | String | 是 | 工作城市 |
| `address` | String | 否 | 工作详细地址 |
| `salaryMin` | Integer | 是 | 最低薪资（元/月） |
| `salaryMax` | Integer | 是 | 最高薪资（元/月） |
| `education` | String | 否 | 学历要求（不限/大专/本科/硕士/博士） |
| `experience` | String | 否 | 经验要求（不限/应届生/在校生/1-3年/3-5年） |
| `description` | String | 是 | 岗位描述 |
| `requirement` | String | 是 | 任职要求 |
| `benefits` | String | 否 | 福利待遇（逗号分隔） |
| `headcount` | Integer | 否 | 招聘人数 |

**请求示例**：

```json
{
  "title": "前端开发工程师",
  "category": "技术",
  "jobType": "全职",
  "city": "上海",
  "address": "上海市浦东新区张江高科技园区",
  "salaryMin": 15000,
  "salaryMax": 25000,
  "education": "本科",
  "experience": "应届生",
  "description": "负责公司 Web 前端产品的开发和维护，参与产品需求分析和技术方案设计。",
  "requirement": "1. 计算机相关专业本科及以上学历；\n2. 熟练掌握 HTML5、CSS3、JavaScript；\n3. 熟悉 Vue.js 或 React 框架；\n4. 有良好的代码规范和团队协作意识。",
  "benefits": "五险一金, 带薪年假, 弹性工作, 股票期权",
  "headcount": 3
}
```

**响应示例**：

```json
{
  "code": 200,
  "message": "发布成功，等待审核",
  "data": {
    "id": 50,
    "companyId": 1,
    "title": "前端开发工程师",
    "category": "技术",
    "jobType": "全职",
    "city": "上海",
    "address": "上海市浦东新区张江高科技园区",
    "salaryMin": 15000,
    "salaryMax": 25000,
    "education": "本科",
    "experience": "应届生",
    "description": "负责公司 Web 前端产品的开发和维护，参与产品需求分析和技术方案设计。",
    "requirement": "1. 计算机相关专业本科及以上学历；...",
    "benefits": "五险一金, 带薪年假, 弹性工作, 股票期权",
    "headcount": 3,
    "status": 1,
    "viewCount": 0,
    "applicationCount": 0,
    "createTime": "2026-02-07 15:30:00",
    "updateTime": "2026-02-07 15:30:00"
  }
}
```

---

### 6.4 更新岗位

**PUT** `/api/jobs/{id}`

**权限**：企业（`COMPANY`，仅限本企业岗位）

**路径参数**：

| 参数 | 类型 | 说明 |
|---|---|---|
| `id` | Long | 岗位 ID |

**请求参数**：与发布岗位相同（JobDTO），仅传需要更新的字段。

**请求示例**：

```json
{
  "salaryMin": 18000,
  "salaryMax": 28000,
  "headcount": 5,
  "benefits": "五险一金, 带薪年假, 弹性工作, 股票期权, 年终奖"
}
```

**响应示例**：

```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 50,
    "companyId": 1,
    "title": "前端开发工程师",
    "category": "技术",
    "jobType": "全职",
    "city": "上海",
    "salaryMin": 18000,
    "salaryMax": 28000,
    "headcount": 5,
    "status": 1,
    "createTime": "2026-02-07 15:30:00",
    "updateTime": "2026-02-07 16:00:00"
  }
}
```

---

### 6.5 删除岗位

**DELETE** `/api/jobs/{id}`

**权限**：企业（本企业岗位）或管理员

**路径参数**：

| 参数 | 类型 | 说明 |
|---|---|---|
| `id` | Long | 岗位 ID |

**请求示例**：

```
DELETE /api/jobs/50
```

**响应示例**：

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

### 6.6 审核岗位

**PUT** `/api/jobs/{id}/audit`

**权限**：管理员（`ADMIN`）

**路径参数**：

| 参数 | 类型 | 说明 |
|---|---|---|
| `id` | Long | 岗位 ID |

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `auditStatus` | Integer | 是 | 审核状态：`2` 通过（上线）、`4` 拒绝 |
| `auditRemark` | String | 否 | 审核备注 |

**请求示例（通过）**：

```json
{
  "auditStatus": 2,
  "auditRemark": "岗位信息完整，审核通过"
}
```

**请求示例（拒绝）**：

```json
{
  "auditStatus": 4,
  "auditRemark": "岗位描述过于简略，请补充详细的职责和要求"
}
```

**响应示例**：

```json
{
  "code": 200,
  "message": "审核成功",
  "data": null
}
```

---

## 7. 简历管理

> 前缀：`/api/resumes`

### 7.1 获取我的简历列表

**GET** `/api/resumes`

**权限**：学生（`STUDENT`）

**请求示例**：

```
GET /api/resumes
```

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "userId": 1,
      "title": "Java开发工程师求职简历",
      "realName": "张三",
      "phone": "13800138000",
      "email": "zhangsan@example.com",
      "education": "本科",
      "university": "北京理工大学",
      "major": "计算机科学与技术",
      "graduationYear": 2026,
      "skills": "Java, Spring Boot, MySQL, Redis, Vue.js",
      "projectExperience": "1. 校园招聘系统 - 负责后端API开发\n2. 在线商城系统 - 负责订单模块开发",
      "internshipExperience": "字节跳动 - 后端开发实习生（2025.07-2025.09）",
      "selfEvaluation": "热爱技术，学习能力强，有良好的团队协作精神。",
      "attachmentUrl": "/uploads/resume/resume_1.pdf",
      "isDefault": 1,
      "createTime": "2026-01-18 10:00:00",
      "updateTime": "2026-02-05 09:30:00"
    },
    {
      "id": 2,
      "userId": 1,
      "title": "前端开发实习简历",
      "realName": "张三",
      "phone": "13800138000",
      "email": "zhangsan@example.com",
      "education": "本科",
      "university": "北京理工大学",
      "major": "计算机科学与技术",
      "graduationYear": 2026,
      "skills": "Vue.js, React, TypeScript, HTML5, CSS3",
      "projectExperience": "1. 个人博客系统 - Vue3 + Vite 全栈开发",
      "internshipExperience": null,
      "selfEvaluation": "对前端技术充满热情，熟悉主流前端框架。",
      "attachmentUrl": null,
      "isDefault": 0,
      "createTime": "2026-02-01 14:00:00",
      "updateTime": "2026-02-01 14:00:00"
    }
  ]
}
```

---

### 7.2 获取简历详情

**GET** `/api/resumes/{id}`

**权限**：本人、投递目标企业或管理员

**路径参数**：

| 参数 | 类型 | 说明 |
|---|---|---|
| `id` | Long | 简历 ID |

**请求示例**：

```
GET /api/resumes/1
```

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "userId": 1,
    "title": "Java开发工程师求职简历",
    "realName": "张三",
    "phone": "13800138000",
    "email": "zhangsan@example.com",
    "education": "本科",
    "university": "北京理工大学",
    "major": "计算机科学与技术",
    "graduationYear": 2026,
    "skills": "Java, Spring Boot, MySQL, Redis, Vue.js",
    "projectExperience": "1. 校园招聘系统 - 负责后端API开发\n2. 在线商城系统 - 负责订单模块开发",
    "internshipExperience": "字节跳动 - 后端开发实习生（2025.07-2025.09）",
    "selfEvaluation": "热爱技术，学习能力强，有良好的团队协作精神。",
    "attachmentUrl": "/uploads/resume/resume_1.pdf",
    "isDefault": 1,
    "createTime": "2026-01-18 10:00:00",
    "updateTime": "2026-02-05 09:30:00"
  }
}
```

---

### 7.3 创建简历

**POST** `/api/resumes`

**权限**：学生（`STUDENT`）

**请求参数（ResumeDTO）**：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `title` | String | 是 | 简历标题 |
| `realName` | String | 是 | 真实姓名 |
| `phone` | String | 是 | 联系电话 |
| `email` | String | 是 | 联系邮箱 |
| `education` | String | 否 | 学历 |
| `university` | String | 否 | 毕业院校 |
| `major` | String | 否 | 专业 |
| `graduationYear` | Integer | 否 | 毕业年份 |
| `skills` | String | 否 | 技能标签（逗号分隔） |
| `projectExperience` | String | 否 | 项目经历 |
| `internshipExperience` | String | 否 | 实习经历 |
| `selfEvaluation` | String | 否 | 自我评价 |
| `attachmentUrl` | String | 否 | 附件简历文件路径（通过文件上传接口获取） |
| `isDefault` | Integer | 否 | 是否默认简历：`1` 是、`0` 否 |

**请求示例**：

```json
{
  "title": "数据分析岗位求职简历",
  "realName": "张三",
  "phone": "13800138000",
  "email": "zhangsan@example.com",
  "education": "本科",
  "university": "北京理工大学",
  "major": "计算机科学与技术",
  "graduationYear": 2026,
  "skills": "Python, SQL, Pandas, Tableau, 数据分析",
  "projectExperience": "1. 电商用户行为分析项目 - 使用 Python 对用户购买数据进行清洗和可视化分析",
  "internshipExperience": null,
  "selfEvaluation": "具备扎实的数据分析能力和良好的逻辑思维。",
  "attachmentUrl": null,
  "isDefault": 0
}
```

**响应示例**：

```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 3,
    "userId": 1,
    "title": "数据分析岗位求职简历",
    "realName": "张三",
    "phone": "13800138000",
    "email": "zhangsan@example.com",
    "education": "本科",
    "university": "北京理工大学",
    "major": "计算机科学与技术",
    "graduationYear": 2026,
    "skills": "Python, SQL, Pandas, Tableau, 数据分析",
    "projectExperience": "1. 电商用户行为分析项目 - 使用 Python 对用户购买数据进行清洗和可视化分析",
    "internshipExperience": null,
    "selfEvaluation": "具备扎实的数据分析能力和良好的逻辑思维。",
    "attachmentUrl": null,
    "isDefault": 0,
    "createTime": "2026-02-07 16:00:00",
    "updateTime": "2026-02-07 16:00:00"
  }
}
```

---

### 7.4 更新简历

**PUT** `/api/resumes/{id}`

**权限**：学生（`STUDENT`，仅限本人简历）

**路径参数**：

| 参数 | 类型 | 说明 |
|---|---|---|
| `id` | Long | 简历 ID |

**请求参数**：与创建简历相同（ResumeDTO），仅传需要更新的字段。

**请求示例**：

```json
{
  "skills": "Python, SQL, Pandas, Tableau, Power BI, 数据分析, 机器学习",
  "selfEvaluation": "具备扎实的数据分析能力和良好的逻辑思维，掌握机器学习基础算法。"
}
```

**响应示例**：

```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 3,
    "userId": 1,
    "title": "数据分析岗位求职简历",
    "skills": "Python, SQL, Pandas, Tableau, Power BI, 数据分析, 机器学习",
    "selfEvaluation": "具备扎实的数据分析能力和良好的逻辑思维，掌握机器学习基础算法。",
    "updateTime": "2026-02-07 16:30:00"
  }
}
```

---

### 7.5 删除简历

**DELETE** `/api/resumes/{id}`

**权限**：学生（`STUDENT`，仅限本人简历）

**路径参数**：

| 参数 | 类型 | 说明 |
|---|---|---|
| `id` | Long | 简历 ID |

**请求示例**：

```
DELETE /api/resumes/3
```

**响应示例**：

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

## 8. 投递管理

> 前缀：`/api/applications`

### 投递状态说明

| 状态值 | 说明 |
|---|---|
| `0` | 待查看 |
| `1` | 已查看 |
| `2` | 通过筛选 |
| `3` | 不合适 |
| `4` | 已面试 |
| `5` | 已撤回 |

### 8.1 投递简历

**POST** `/api/applications`

**权限**：学生（`STUDENT`）

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `jobId` | Long | 是 | 岗位 ID |
| `resumeId` | Long | 是 | 简历 ID |

**请求示例**：

```json
{
  "jobId": 1,
  "resumeId": 1
}
```

**响应示例**：

```json
{
  "code": 200,
  "message": "投递成功",
  "data": {
    "id": 100,
    "jobId": 1,
    "jobTitle": "Java后端开发工程师",
    "companyName": "星辰科技有限公司",
    "resumeId": 1,
    "resumeTitle": "Java开发工程师求职简历",
    "userId": 1,
    "status": 0,
    "remark": null,
    "createTime": "2026-02-07 16:30:00",
    "updateTime": "2026-02-07 16:30:00"
  }
}
```

**错误示例**：

```json
{
  "code": 400,
  "message": "您已投递过该岗位，请勿重复投递",
  "data": null
}
```

---

### 8.2 我的投递记录

**GET** `/api/applications/my`

**权限**：学生（`STUDENT`）

**查询参数**：

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `page` | Integer | 否 | 页码，默认 1 |
| `size` | Integer | 否 | 每页条数，默认 10 |
| `status` | Integer | 否 | 投递状态筛选 |

**请求示例**：

```
GET /api/applications/my?page=1&size=10&status=2
```

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 8,
    "list": [
      {
        "id": 100,
        "jobId": 1,
        "jobTitle": "Java后端开发工程师",
        "companyId": 1,
        "companyName": "星辰科技有限公司",
        "companyLogo": "/uploads/company/logo_1.png",
        "resumeId": 1,
        "resumeTitle": "Java开发工程师求职简历",
        "userId": 1,
        "status": 2,
        "remark": "简历匹配度较高，进入面试环节",
        "createTime": "2026-02-01 10:00:00",
        "updateTime": "2026-02-03 14:00:00"
      },
      {
        "id": 105,
        "jobId": 8,
        "jobTitle": "后端开发工程师（Go方向）",
        "companyId": 5,
        "companyName": "云帆数据科技",
        "companyLogo": "/uploads/company/logo_5.png",
        "resumeId": 1,
        "resumeTitle": "Java开发工程师求职简历",
        "userId": 1,
        "status": 2,
        "remark": "技术基础扎实，安排面试",
        "createTime": "2026-02-02 11:00:00",
        "updateTime": "2026-02-04 09:00:00"
      }
    ],
    "page": 1,
    "size": 10
  }
}
```

---

### 8.3 岗位投递列表

**GET** `/api/applications/job/{jobId}`

**权限**：企业（`COMPANY`，仅限本企业岗位）

**路径参数**：

| 参数 | 类型 | 说明 |
|---|---|---|
| `jobId` | Long | 岗位 ID |

**请求示例**：

```
GET /api/applications/job/1
```

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 32,
    "list": [
      {
        "id": 100,
        "jobId": 1,
        "jobTitle": "Java后端开发工程师",
        "resumeId": 1,
        "resumeTitle": "Java开发工程师求职简历",
        "userId": 1,
        "studentName": "张三",
        "university": "北京理工大学",
        "major": "计算机科学与技术",
        "education": "本科",
        "status": 2,
        "remark": "简历匹配度较高，进入面试环节",
        "createTime": "2026-02-01 10:00:00",
        "updateTime": "2026-02-03 14:00:00"
      },
      {
        "id": 102,
        "jobId": 1,
        "jobTitle": "Java后端开发工程师",
        "resumeId": 8,
        "resumeTitle": "后端开发求职简历",
        "userId": 3,
        "studentName": "李明",
        "university": "清华大学",
        "major": "软件工程",
        "education": "硕士",
        "status": 0,
        "remark": null,
        "createTime": "2026-02-03 09:00:00",
        "updateTime": "2026-02-03 09:00:00"
      }
    ],
    "page": 1,
    "size": 10
  }
}
```

---

### 8.4 所有投递记录

**GET** `/api/applications`

**权限**：管理员（`ADMIN`）

**查询参数**：

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `page` | Integer | 否 | 页码，默认 1 |
| `size` | Integer | 否 | 每页条数，默认 10 |
| `status` | Integer | 否 | 投递状态筛选 |
| `keyword` | String | 否 | 搜索关键词（匹配岗位名称、企业名称、学生姓名） |

**请求示例**：

```
GET /api/applications?page=1&size=10&keyword=Java
```

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 256,
    "list": [
      {
        "id": 100,
        "jobId": 1,
        "jobTitle": "Java后端开发工程师",
        "companyId": 1,
        "companyName": "星辰科技有限公司",
        "resumeId": 1,
        "userId": 1,
        "studentName": "张三",
        "status": 2,
        "remark": "简历匹配度较高",
        "createTime": "2026-02-01 10:00:00",
        "updateTime": "2026-02-03 14:00:00"
      }
    ],
    "page": 1,
    "size": 10
  }
}
```

---

### 8.5 更新投递状态

**PUT** `/api/applications/{id}/status`

**权限**：企业（`COMPANY`）或管理员（`ADMIN`）

**路径参数**：

| 参数 | 类型 | 说明 |
|---|---|---|
| `id` | Long | 投递记录 ID |

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `status` | Integer | 是 | 投递状态（参见投递状态说明） |
| `remark` | String | 否 | 备注说明 |

**请求示例**：

```json
{
  "status": 2,
  "remark": "简历匹配度较高，安排面试"
}
```

**响应示例**：

```json
{
  "code": 200,
  "message": "状态更新成功",
  "data": null
}
```

---

### 8.6 撤回投递

**PUT** `/api/applications/{id}/withdraw`

**权限**：学生（`STUDENT`，仅限本人投递）

**路径参数**：

| 参数 | 类型 | 说明 |
|---|---|---|
| `id` | Long | 投递记录 ID |

**请求示例**：

```
PUT /api/applications/100/withdraw
```

**响应示例**：

```json
{
  "code": 200,
  "message": "撤回成功",
  "data": null
}
```

**错误示例**：

```json
{
  "code": 400,
  "message": "当前投递状态不可撤回",
  "data": null
}
```

---

## 9. 面试管理

> 前缀：`/api/interviews`

### 面试状态说明

| 状态值 | 说明 |
|---|---|
| `0` | 待确认 |
| `1` | 已确认 |
| `2` | 已完成 |
| `3` | 已取消 |
| `4` | 已拒绝 |

### 9.1 发起面试邀约

**POST** `/api/interviews`

**权限**：企业（`COMPANY`）

**请求参数（InterviewDTO）**：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `applicationId` | Long | 是 | 投递记录 ID |
| `interviewTime` | String | 是 | 面试时间（yyyy-MM-dd HH:mm:ss） |
| `interviewType` | String | 是 | 面试形式（线上/线下） |
| `interviewAddress` | String | 否 | 面试地点（线下面试时必填） |
| `onlinePlatform` | String | 否 | 线上平台（线上面试时填写，如腾讯会议、Zoom） |
| `onlineLink` | String | 否 | 线上会议链接 |
| `contactPerson` | String | 否 | 面试联系人 |
| `contactPhone` | String | 否 | 联系电话 |
| `remark` | String | 否 | 面试备注 |

**请求示例**：

```json
{
  "applicationId": 100,
  "interviewTime": "2026-02-15 14:00:00",
  "interviewType": "线上",
  "onlinePlatform": "腾讯会议",
  "onlineLink": "https://meeting.tencent.com/dm/xxx",
  "contactPerson": "王经理",
  "contactPhone": "010-88886666",
  "remark": "请提前10分钟进入会议室，准备自我介绍和项目介绍。"
}
```

**响应示例**：

```json
{
  "code": 200,
  "message": "面试邀约已发送",
  "data": {
    "id": 30,
    "applicationId": 100,
    "jobId": 1,
    "jobTitle": "Java后端开发工程师",
    "companyId": 1,
    "companyName": "星辰科技有限公司",
    "studentId": 1,
    "studentName": "张三",
    "interviewTime": "2026-02-15 14:00:00",
    "interviewType": "线上",
    "interviewAddress": null,
    "onlinePlatform": "腾讯会议",
    "onlineLink": "https://meeting.tencent.com/dm/xxx",
    "contactPerson": "王经理",
    "contactPhone": "010-88886666",
    "remark": "请提前10分钟进入会议室，准备自我介绍和项目介绍。",
    "status": 0,
    "createTime": "2026-02-07 17:00:00",
    "updateTime": "2026-02-07 17:00:00"
  }
}
```

---

### 9.2 我的面试列表

**GET** `/api/interviews/my`

**权限**：学生（`STUDENT`）或企业（`COMPANY`），根据角色返回对应数据

**请求示例**：

```
GET /api/interviews/my
```

**响应示例（学生视角）**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 30,
      "applicationId": 100,
      "jobId": 1,
      "jobTitle": "Java后端开发工程师",
      "companyId": 1,
      "companyName": "星辰科技有限公司",
      "companyLogo": "/uploads/company/logo_1.png",
      "interviewTime": "2026-02-15 14:00:00",
      "interviewType": "线上",
      "onlinePlatform": "腾讯会议",
      "onlineLink": "https://meeting.tencent.com/dm/xxx",
      "contactPerson": "王经理",
      "contactPhone": "010-88886666",
      "remark": "请提前10分钟进入会议室",
      "status": 1,
      "createTime": "2026-02-07 17:00:00",
      "updateTime": "2026-02-08 09:00:00"
    }
  ]
}
```

**响应示例（企业视角）**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 30,
      "applicationId": 100,
      "jobId": 1,
      "jobTitle": "Java后端开发工程师",
      "studentId": 1,
      "studentName": "张三",
      "university": "北京理工大学",
      "major": "计算机科学与技术",
      "interviewTime": "2026-02-15 14:00:00",
      "interviewType": "线上",
      "onlinePlatform": "腾讯会议",
      "onlineLink": "https://meeting.tencent.com/dm/xxx",
      "contactPerson": "王经理",
      "contactPhone": "010-88886666",
      "remark": "请提前10分钟进入会议室",
      "status": 1,
      "createTime": "2026-02-07 17:00:00",
      "updateTime": "2026-02-08 09:00:00"
    }
  ]
}
```

---

### 9.3 更新面试状态

**PUT** `/api/interviews/{id}/status`

**权限**：学生（确认/拒绝）或企业（完成/取消）

**路径参数**：

| 参数 | 类型 | 说明 |
|---|---|---|
| `id` | Long | 面试 ID |

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `status` | Integer | 是 | 面试状态（参见面试状态说明） |

**请求示例（学生确认面试）**：

```json
{
  "status": 1
}
```

**请求示例（学生拒绝面试）**：

```json
{
  "status": 4
}
```

**请求示例（企业标记面试完成）**：

```json
{
  "status": 2
}
```

**响应示例**：

```json
{
  "code": 200,
  "message": "状态更新成功",
  "data": null
}
```

---

## 10. Offer管理

> 前缀：`/api/offers`

### Offer状态说明

| 状态值 | 说明 |
|---|---|
| `0` | 待回复 |
| `1` | 已接受 |
| `2` | 已拒绝 |
| `3` | 已撤回 |

### 10.1 发放Offer

**POST** `/api/offers`

**权限**：企业（`COMPANY`）

**请求参数（OfferDTO）**：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `applicationId` | Long | 是 | 投递记录 ID |
| `position` | String | 是 | 录用岗位 |
| `department` | String | 否 | 所属部门 |
| `salary` | String | 是 | 薪资待遇 |
| `workCity` | String | 是 | 工作城市 |
| `reportDate` | String | 是 | 入职日期（yyyy-MM-dd） |
| `deadline` | String | 是 | 回复截止日期（yyyy-MM-dd） |
| `benefits` | String | 否 | 福利说明 |
| `remark` | String | 否 | 附加说明 |

**请求示例**：

```json
{
  "applicationId": 100,
  "position": "Java后端开发工程师",
  "department": "技术研发部",
  "salary": "15000元/月 × 14薪",
  "workCity": "北京",
  "reportDate": "2026-07-01",
  "deadline": "2026-03-01",
  "benefits": "五险一金、带薪年假15天、弹性工作制、免费三餐、年度体检、股票期权",
  "remark": "欢迎加入星辰科技！如有任何疑问，请联系HR王经理（010-88886666）。"
}
```

**响应示例**：

```json
{
  "code": 200,
  "message": "Offer已发放",
  "data": {
    "id": 15,
    "applicationId": 100,
    "jobId": 1,
    "jobTitle": "Java后端开发工程师",
    "companyId": 1,
    "companyName": "星辰科技有限公司",
    "studentId": 1,
    "studentName": "张三",
    "position": "Java后端开发工程师",
    "department": "技术研发部",
    "salary": "15000元/月 × 14薪",
    "workCity": "北京",
    "reportDate": "2026-07-01",
    "deadline": "2026-03-01",
    "benefits": "五险一金、带薪年假15天、弹性工作制、免费三餐、年度体检、股票期权",
    "remark": "欢迎加入星辰科技！",
    "status": 0,
    "createTime": "2026-02-07 18:00:00",
    "updateTime": "2026-02-07 18:00:00"
  }
}
```

---

### 10.2 我的Offer列表

**GET** `/api/offers/my`

**权限**：学生（`STUDENT`）或企业（`COMPANY`），根据角色返回对应数据

**请求示例**：

```
GET /api/offers/my
```

**响应示例（学生视角）**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 15,
      "applicationId": 100,
      "jobId": 1,
      "jobTitle": "Java后端开发工程师",
      "companyId": 1,
      "companyName": "星辰科技有限公司",
      "companyLogo": "/uploads/company/logo_1.png",
      "position": "Java后端开发工程师",
      "department": "技术研发部",
      "salary": "15000元/月 × 14薪",
      "workCity": "北京",
      "reportDate": "2026-07-01",
      "deadline": "2026-03-01",
      "benefits": "五险一金、带薪年假15天、弹性工作制、免费三餐、年度体检、股票期权",
      "remark": "欢迎加入星辰科技！",
      "status": 0,
      "createTime": "2026-02-07 18:00:00",
      "updateTime": "2026-02-07 18:00:00"
    }
  ]
}
```

---

### 10.3 回复Offer

**PUT** `/api/offers/{id}/respond`

**权限**：学生（`STUDENT`）

**路径参数**：

| 参数 | 类型 | 说明 |
|---|---|---|
| `id` | Long | Offer ID |

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `status` | Integer | 是 | 回复状态：`1` 接受、`2` 拒绝 |

**请求示例（接受）**：

```json
{
  "status": 1
}
```

**请求示例（拒绝）**：

```json
{
  "status": 2
}
```

**响应示例**：

```json
{
  "code": 200,
  "message": "回复成功",
  "data": null
}
```

---

## 11. 宣讲会管理

> 前缀：`/api/job-fairs`

### 宣讲会状态说明

| 状态值 | 说明 |
|---|---|
| `0` | 待审核 |
| `1` | 已通过（报名中） |
| `2` | 已结束 |
| `3` | 已取消 |
| `4` | 审核拒绝 |

### 11.1 获取宣讲会列表

**GET** `/api/job-fairs`

**权限**：公开访问（公开端仅返回 `status=1` 已审核通过的宣讲会）

**查询参数**：

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `page` | Integer | 否 | 页码，默认 1 |
| `size` | Integer | 否 | 每页条数，默认 10 |
| `status` | Integer | 否 | 宣讲会状态 |
| `companyId` | Long | 否 | 企业 ID |

**请求示例**：

```
GET /api/job-fairs?page=1&size=10
```

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 15,
    "list": [
      {
        "id": 1,
        "companyId": 1,
        "companyName": "星辰科技有限公司",
        "companyLogo": "/uploads/company/logo_1.png",
        "title": "星辰科技2026校园春季招聘宣讲会",
        "description": "星辰科技将于2026年春季面向全国高校开展校园招聘，本次宣讲会将介绍公司文化、技术团队、岗位信息和发展前景。",
        "startTime": "2026-03-01 14:00:00",
        "endTime": "2026-03-01 16:00:00",
        "location": "北京理工大学 综合教学楼 报告厅A101",
        "capacity": 200,
        "bookedCount": 85,
        "contactPerson": "王经理",
        "contactPhone": "010-88886666",
        "status": 1,
        "createTime": "2026-02-01 09:00:00",
        "updateTime": "2026-02-05 10:00:00"
      },
      {
        "id": 3,
        "companyId": 3,
        "companyName": "未来科技集团",
        "companyLogo": "/uploads/company/logo_3.png",
        "title": "未来科技2026春招宣讲会 — 北京站",
        "description": "未来科技集团春季校园招聘宣讲会，涵盖研发、产品、设计多个方向。",
        "startTime": "2026-03-05 15:00:00",
        "endTime": "2026-03-05 17:00:00",
        "location": "清华大学 主楼 大礼堂",
        "capacity": 300,
        "bookedCount": 120,
        "contactPerson": "李经理",
        "contactPhone": "0755-66668888",
        "status": 1,
        "createTime": "2026-02-03 10:00:00",
        "updateTime": "2026-02-06 11:00:00"
      }
    ],
    "page": 1,
    "size": 10
  }
}
```

---

### 11.2 获取宣讲会详情

**GET** `/api/job-fairs/{id}`

**权限**：公开

**路径参数**：

| 参数 | 类型 | 说明 |
|---|---|---|
| `id` | Long | 宣讲会 ID |

**请求示例**：

```
GET /api/job-fairs/1
```

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "companyId": 1,
    "companyName": "星辰科技有限公司",
    "companyLogo": "/uploads/company/logo_1.png",
    "title": "星辰科技2026校园春季招聘宣讲会",
    "description": "星辰科技将于2026年春季面向全国高校开展校园招聘，本次宣讲会将介绍公司文化、技术团队、岗位信息和发展前景。现场还有互动问答环节及精美礼品赠送。",
    "startTime": "2026-03-01 14:00:00",
    "endTime": "2026-03-01 16:00:00",
    "location": "北京理工大学 综合教学楼 报告厅A101",
    "capacity": 200,
    "bookedCount": 85,
    "contactPerson": "王经理",
    "contactPhone": "010-88886666",
    "status": 1,
    "isBooked": false,
    "createTime": "2026-02-01 09:00:00",
    "updateTime": "2026-02-05 10:00:00"
  }
}
```

> **说明**：`isBooked` 字段仅在学生登录时返回，表示当前学生是否已预约该宣讲会。

---

### 11.3 创建宣讲会

**POST** `/api/job-fairs`

**权限**：企业（`COMPANY`）

**请求参数（JobFairDTO）**：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `title` | String | 是 | 宣讲会标题 |
| `description` | String | 是 | 宣讲会简介 |
| `startTime` | String | 是 | 开始时间（yyyy-MM-dd HH:mm:ss） |
| `endTime` | String | 是 | 结束时间 |
| `location` | String | 是 | 宣讲会地点 |
| `capacity` | Integer | 否 | 最大参与人数 |
| `contactPerson` | String | 否 | 联系人 |
| `contactPhone` | String | 否 | 联系电话 |

**请求示例**：

```json
{
  "title": "星辰科技2026校园春季招聘宣讲会",
  "description": "星辰科技将于2026年春季面向全国高校开展校园招聘，本次宣讲会将介绍公司文化、技术团队和岗位信息。",
  "startTime": "2026-03-01 14:00:00",
  "endTime": "2026-03-01 16:00:00",
  "location": "北京理工大学 综合教学楼 报告厅A101",
  "capacity": 200,
  "contactPerson": "王经理",
  "contactPhone": "010-88886666"
}
```

**响应示例**：

```json
{
  "code": 200,
  "message": "创建成功，等待审核",
  "data": {
    "id": 10,
    "companyId": 1,
    "companyName": "星辰科技有限公司",
    "title": "星辰科技2026校园春季招聘宣讲会",
    "description": "星辰科技将于2026年春季面向全国高校开展校园招聘...",
    "startTime": "2026-03-01 14:00:00",
    "endTime": "2026-03-01 16:00:00",
    "location": "北京理工大学 综合教学楼 报告厅A101",
    "capacity": 200,
    "bookedCount": 0,
    "contactPerson": "王经理",
    "contactPhone": "010-88886666",
    "status": 0,
    "createTime": "2026-02-07 18:30:00",
    "updateTime": "2026-02-07 18:30:00"
  }
}
```

---

### 11.4 更新宣讲会

**PUT** `/api/job-fairs/{id}`

**权限**：企业（`COMPANY`，仅限本企业宣讲会）

**路径参数**：

| 参数 | 类型 | 说明 |
|---|---|---|
| `id` | Long | 宣讲会 ID |

**请求参数**：与创建宣讲会相同（JobFairDTO），仅传需要更新的字段。

**请求示例**：

```json
{
  "capacity": 250,
  "description": "本次宣讲会新增AI技术分享环节，欢迎同学们参加！"
}
```

**响应示例**：

```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 10,
    "title": "星辰科技2026校园春季招聘宣讲会",
    "capacity": 250,
    "description": "本次宣讲会新增AI技术分享环节，欢迎同学们参加！",
    "updateTime": "2026-02-07 19:00:00"
  }
}
```

---

### 11.5 审核宣讲会

**PUT** `/api/job-fairs/{id}/audit`

**权限**：管理员（`ADMIN`）

**路径参数**：

| 参数 | 类型 | 说明 |
|---|---|---|
| `id` | Long | 宣讲会 ID |

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `auditStatus` | Integer | 是 | 审核状态：`1` 通过、`4` 拒绝 |
| `auditRemark` | String | 否 | 审核备注 |

**请求示例**：

```json
{
  "auditStatus": 1,
  "auditRemark": "审核通过，场地已确认"
}
```

**响应示例**：

```json
{
  "code": 200,
  "message": "审核成功",
  "data": null
}
```

---

### 11.6 预约宣讲会

**POST** `/api/job-fairs/{id}/book`

**权限**：学生（`STUDENT`）

**路径参数**：

| 参数 | 类型 | 说明 |
|---|---|---|
| `id` | Long | 宣讲会 ID |

**请求示例**：

```
POST /api/job-fairs/1/book
```

**响应示例**：

```json
{
  "code": 200,
  "message": "预约成功",
  "data": null
}
```

**错误示例**：

```json
{
  "code": 400,
  "message": "宣讲会名额已满",
  "data": null
}
```

---

### 11.7 取消预约宣讲会

**DELETE** `/api/job-fairs/{id}/book`

**权限**：学生（`STUDENT`）

**路径参数**：

| 参数 | 类型 | 说明 |
|---|---|---|
| `id` | Long | 宣讲会 ID |

**请求示例**：

```
DELETE /api/job-fairs/1/book
```

**响应示例**：

```json
{
  "code": 200,
  "message": "已取消预约",
  "data": null
}
```

---

### 11.8 获取宣讲会预约列表

**GET** `/api/job-fairs/{id}/bookings`

**权限**：企业（`COMPANY`）或管理员（`ADMIN`）

**路径参数**：

| 参数 | 类型 | 说明 |
|---|---|---|
| `id` | Long | 宣讲会 ID |

**请求示例**：

```
GET /api/job-fairs/1/bookings
```

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "jobFairId": 1,
      "userId": 1,
      "studentName": "张三",
      "university": "北京理工大学",
      "major": "计算机科学与技术",
      "phone": "13800138000",
      "bookTime": "2026-02-06 10:00:00"
    },
    {
      "id": 2,
      "jobFairId": 1,
      "userId": 3,
      "studentName": "李明",
      "university": "清华大学",
      "major": "软件工程",
      "phone": "13700137000",
      "bookTime": "2026-02-06 11:30:00"
    },
    {
      "id": 3,
      "jobFairId": 1,
      "userId": 7,
      "studentName": "王芳",
      "university": "北京大学",
      "major": "信息管理与信息系统",
      "phone": "13600136000",
      "bookTime": "2026-02-06 14:00:00"
    }
  ]
}
```

---

## 12. 公告管理

> 前缀：`/api/announcements`

### 12.1 获取公告列表

**GET** `/api/announcements`

**权限**：公开访问（公开端仅返回 `status=1` 已发布的公告），管理员可查看所有状态

**查询参数**：

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `page` | Integer | 否 | 页码，默认 1 |
| `size` | Integer | 否 | 每页条数，默认 10 |

**请求示例**：

```
GET /api/announcements?page=1&size=10
```

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 12,
    "list": [
      {
        "id": 1,
        "title": "关于2026年春季校园招聘工作安排的通知",
        "content": "各位同学、各用人单位：\n\n2026年春季校园招聘即将启动，现将相关工作安排通知如下：\n\n一、招聘时间：2026年3月1日至5月31日\n二、招聘形式：线上+线下双轨并行\n三、注意事项：请同学们及时完善个人简历...",
        "type": "通知",
        "status": 1,
        "isTop": 1,
        "viewCount": 520,
        "publishTime": "2026-02-01 09:00:00",
        "createTime": "2026-02-01 08:30:00",
        "updateTime": "2026-02-01 09:00:00"
      },
      {
        "id": 2,
        "title": "毕业生就业指导讲座开始报名",
        "content": "为帮助2026届毕业生更好地应对就业求职，学校就业指导中心将举办系列就业指导讲座...",
        "type": "活动",
        "status": 1,
        "isTop": 0,
        "viewCount": 230,
        "publishTime": "2026-02-03 10:00:00",
        "createTime": "2026-02-03 09:30:00",
        "updateTime": "2026-02-03 10:00:00"
      }
    ],
    "page": 1,
    "size": 10
  }
}
```

---

### 12.2 获取公告详情

**GET** `/api/announcements/{id}`

**权限**：公开

**路径参数**：

| 参数 | 类型 | 说明 |
|---|---|---|
| `id` | Long | 公告 ID |

**请求示例**：

```
GET /api/announcements/1
```

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "title": "关于2026年春季校园招聘工作安排的通知",
    "content": "各位同学、各用人单位：\n\n2026年春季校园招聘即将启动，现将相关工作安排通知如下：\n\n一、招聘时间：2026年3月1日至5月31日\n二、招聘形式：线上+线下双轨并行\n三、企业入驻流程：企业需在平台完成注册并通过资质审核\n四、学生注意事项：\n  1. 请及时完善个人简历和学生档案\n  2. 仔细阅读岗位要求后再投递\n  3. 面试前做好充分准备\n\n祝各位同学求职顺利！\n\n校园招聘管理中心\n2026年2月1日",
    "type": "通知",
    "status": 1,
    "isTop": 1,
    "viewCount": 521,
    "publishTime": "2026-02-01 09:00:00",
    "createTime": "2026-02-01 08:30:00",
    "updateTime": "2026-02-01 09:00:00"
  }
}
```

---

### 12.3 创建公告

**POST** `/api/announcements`

**权限**：管理员（`ADMIN`）

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `title` | String | 是 | 公告标题 |
| `content` | String | 是 | 公告内容 |
| `type` | String | 否 | 公告类型（通知/活动/政策/其他） |
| `status` | Integer | 否 | 状态：`0` 草稿、`1` 已发布，默认 1 |
| `isTop` | Integer | 否 | 是否置顶：`0` 否、`1` 是，默认 0 |

**请求示例**：

```json
{
  "title": "关于简历填写规范的通知",
  "content": "各位同学：\n\n为提高求职成功率，请在填写简历时注意以下事项：\n\n1. 真实填写个人信息，不得造假\n2. 项目经历描述应具体，突出个人贡献\n3. 技能描述应与岗位匹配\n4. 联系方式务必准确有效\n\n校园招聘管理中心",
  "type": "通知",
  "status": 1,
  "isTop": 0
}
```

**响应示例**：

```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 13,
    "title": "关于简历填写规范的通知",
    "content": "各位同学：\n\n为提高求职成功率...",
    "type": "通知",
    "status": 1,
    "isTop": 0,
    "viewCount": 0,
    "publishTime": "2026-02-07 19:00:00",
    "createTime": "2026-02-07 19:00:00",
    "updateTime": "2026-02-07 19:00:00"
  }
}
```

---

### 12.4 更新公告

**PUT** `/api/announcements/{id}`

**权限**：管理员（`ADMIN`）

**路径参数**：

| 参数 | 类型 | 说明 |
|---|---|---|
| `id` | Long | 公告 ID |

**请求参数**：与创建公告相同，仅传需要更新的字段。

**请求示例**：

```json
{
  "title": "【重要】关于简历填写规范的通知",
  "isTop": 1
}
```

**响应示例**：

```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 13,
    "title": "【重要】关于简历填写规范的通知",
    "isTop": 1,
    "updateTime": "2026-02-07 19:30:00"
  }
}
```

---

### 12.5 删除公告

**DELETE** `/api/announcements/{id}`

**权限**：管理员（`ADMIN`）

**路径参数**：

| 参数 | 类型 | 说明 |
|---|---|---|
| `id` | Long | 公告 ID |

**请求示例**：

```
DELETE /api/announcements/13
```

**响应示例**：

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

## 13. 消息通知

> 前缀：`/api/messages`

### 13.1 获取消息列表

**GET** `/api/messages`

**权限**：已登录用户

**查询参数**：

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `page` | Integer | 否 | 页码，默认 1 |
| `size` | Integer | 否 | 每页条数，默认 10 |
| `type` | String | 否 | 消息类型（system/application/interview/offer） |
| `isRead` | Integer | 否 | 已读状态：`0` 未读、`1` 已读 |

**请求示例**：

```
GET /api/messages?page=1&size=10&isRead=0
```

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 5,
    "list": [
      {
        "id": 101,
        "userId": 1,
        "type": "interview",
        "title": "面试邀约通知",
        "content": "您投递的「星辰科技有限公司 - Java后端开发工程师」岗位已通过筛选，企业向您发出了面试邀约，请及时查看并确认。",
        "relatedId": 30,
        "relatedType": "interview",
        "isRead": 0,
        "createTime": "2026-02-07 17:00:00"
      },
      {
        "id": 98,
        "userId": 1,
        "type": "application",
        "title": "投递状态更新",
        "content": "您投递的「云帆数据科技 - 后端开发工程师（Go方向）」简历已被查看。",
        "relatedId": 105,
        "relatedType": "application",
        "isRead": 0,
        "createTime": "2026-02-06 15:00:00"
      },
      {
        "id": 95,
        "userId": 1,
        "type": "system",
        "title": "系统通知",
        "content": "2026年春季校园招聘已正式启动，欢迎浏览最新岗位信息。",
        "relatedId": null,
        "relatedType": null,
        "isRead": 0,
        "createTime": "2026-02-01 09:00:00"
      }
    ],
    "page": 1,
    "size": 10
  }
}
```

---

### 13.2 标记消息已读

**PUT** `/api/messages/{id}/read`

**权限**：已登录用户（仅限本人消息）

**路径参数**：

| 参数 | 类型 | 说明 |
|---|---|---|
| `id` | Long | 消息 ID |

**请求示例**：

```
PUT /api/messages/101/read
```

**响应示例**：

```json
{
  "code": 200,
  "message": "已标记为已读",
  "data": null
}
```

---

### 13.3 全部标记已读

**PUT** `/api/messages/read-all`

**权限**：已登录用户

**请求示例**：

```
PUT /api/messages/read-all
```

**响应示例**：

```json
{
  "code": 200,
  "message": "已全部标记为已读",
  "data": null
}
```

---

### 13.4 获取未读消息数

**GET** `/api/messages/unread-count`

**权限**：已登录用户

**请求示例**：

```
GET /api/messages/unread-count
```

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 5,
    "system": 1,
    "application": 2,
    "interview": 1,
    "offer": 1
  }
}
```

---

## 14. 操作日志

> 前缀：`/api/operation-logs`

### 14.1 获取操作日志列表

**GET** `/api/operation-logs`

**权限**：管理员（`ADMIN`）

**查询参数**：

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `page` | Integer | 否 | 页码，默认 1 |
| `size` | Integer | 否 | 每页条数，默认 10 |
| `keyword` | String | 否 | 搜索关键词（匹配操作内容、操作人） |

**请求示例**：

```
GET /api/operation-logs?page=1&size=10&keyword=审核
```

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 350,
    "list": [
      {
        "id": 500,
        "userId": 99,
        "username": "admin",
        "nickname": "系统管理员",
        "module": "企业管理",
        "operation": "审核企业",
        "description": "审核通过企业「星辰科技有限公司」",
        "method": "PUT",
        "url": "/api/companies/1/audit",
        "ip": "192.168.1.100",
        "requestParams": "{\"auditStatus\":1,\"auditRemark\":\"资质齐全，审核通过\"}",
        "status": 1,
        "costTime": 45,
        "createTime": "2026-02-05 10:00:00"
      },
      {
        "id": 498,
        "userId": 99,
        "username": "admin",
        "nickname": "系统管理员",
        "module": "岗位管理",
        "operation": "审核岗位",
        "description": "审核通过岗位「Java后端开发工程师」",
        "method": "PUT",
        "url": "/api/jobs/1/audit",
        "ip": "192.168.1.100",
        "requestParams": "{\"auditStatus\":2,\"auditRemark\":\"岗位信息完整\"}",
        "status": 1,
        "costTime": 32,
        "createTime": "2026-02-04 16:00:00"
      }
    ],
    "page": 1,
    "size": 10
  }
}
```

---

## 15. 数据统计

> 前缀：`/api/dashboard`

### 15.1 获取统计数据

**GET** `/api/dashboard`

**权限**：公开

**请求示例**：

```
GET /api/dashboard
```

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "totalStudents": 1280,
    "totalCompanies": 156,
    "totalJobs": 432,
    "totalApplications": 3560,
    "recentJobs": [
      {
        "id": 48,
        "title": "产品经理",
        "companyName": "星辰科技有限公司",
        "city": "北京",
        "salaryMin": 12000,
        "salaryMax": 18000,
        "createTime": "2026-02-07 10:00:00"
      },
      {
        "id": 47,
        "title": "UI设计师",
        "companyName": "未来科技集团",
        "city": "深圳",
        "salaryMin": 10000,
        "salaryMax": 16000,
        "createTime": "2026-02-06 16:00:00"
      },
      {
        "id": 46,
        "title": "数据分析师",
        "companyName": "云帆数据科技",
        "city": "上海",
        "salaryMin": 14000,
        "salaryMax": 22000,
        "createTime": "2026-02-06 14:00:00"
      },
      {
        "id": 45,
        "title": "前端开发工程师",
        "companyName": "星辰科技有限公司",
        "city": "北京",
        "salaryMin": 15000,
        "salaryMax": 25000,
        "createTime": "2026-02-06 10:00:00"
      },
      {
        "id": 44,
        "title": "测试工程师",
        "companyName": "锐智软件科技",
        "city": "杭州",
        "salaryMin": 10000,
        "salaryMax": 15000,
        "createTime": "2026-02-05 15:00:00"
      }
    ],
    "applicationStatusStats": [
      { "status": "待查看", "count": 1200 },
      { "status": "已查看", "count": 890 },
      { "status": "通过筛选", "count": 620 },
      { "status": "不合适", "count": 530 },
      { "status": "已面试", "count": 280 },
      { "status": "已撤回", "count": 40 }
    ],
    "industryStats": [
      { "industry": "互联网/IT", "count": 198 },
      { "industry": "金融", "count": 56 },
      { "industry": "教育", "count": 42 },
      { "industry": "制造业", "count": 38 },
      { "industry": "医疗健康", "count": 30 },
      { "industry": "电子商务", "count": 28 },
      { "industry": "人工智能", "count": 22 },
      { "industry": "其他", "count": 18 }
    ],
    "monthlyStats": [
      { "month": "2025-09", "jobs": 35, "applications": 180 },
      { "month": "2025-10", "jobs": 52, "applications": 320 },
      { "month": "2025-11", "jobs": 68, "applications": 480 },
      { "month": "2025-12", "jobs": 45, "applications": 350 },
      { "month": "2026-01", "jobs": 120, "applications": 1200 },
      { "month": "2026-02", "jobs": 112, "applications": 1030 }
    ]
  }
}
```

---

## 16. 文件上传

> 前缀：`/api/files`

### 16.1 上传文件

**POST** `/api/files/upload`

**权限**：已登录用户

**请求格式**：`multipart/form-data`

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `file` | File | 是 | 上传的文件（支持图片、PDF、Word 等常见格式） |

**请求示例（cURL）**：

```bash
curl -X POST http://localhost:8080/api/files/upload \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -F "file=@/path/to/resume.pdf"
```

**响应示例**：

```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "url": "/uploads/2026/02/07/a1b2c3d4e5f6.pdf",
    "filename": "a1b2c3d4e5f6.pdf",
    "originalFilename": "张三_求职简历.pdf",
    "size": 256000,
    "contentType": "application/pdf"
  }
}
```

**错误示例**：

```json
{
  "code": 400,
  "message": "文件大小超过限制（最大10MB）",
  "data": null
}
```

**文件大小限制**：单文件最大 10MB

**支持格式**：

| 类型 | 支持格式 |
|---|---|
| 图片 | jpg, jpeg, png, gif, webp |
| 文档 | pdf, doc, docx |
| 其他 | xls, xlsx, ppt, pptx |

---

## 附录

### A. 常用 HTTP 状态码

| 状态码 | 说明 |
|---|---|
| `200 OK` | 请求成功 |
| `400 Bad Request` | 请求参数错误 |
| `401 Unauthorized` | 认证失败 |
| `403 Forbidden` | 权限不足 |
| `404 Not Found` | 资源不存在 |
| `500 Internal Server Error` | 服务器内部错误 |

### B. 接口汇总表

| 模块 | 方法 | 路径 | 说明 | 权限 |
|---|---|---|---|---|
| 认证 | POST | `/api/auth/login` | 登录 | 公开 |
| 认证 | POST | `/api/auth/register` | 注册 | 公开 |
| 用户 | GET | `/api/users` | 用户列表 | ADMIN |
| 用户 | GET | `/api/users/current` | 当前用户信息 | 已登录 |
| 用户 | GET | `/api/users/{id}` | 用户详情 | 已登录 |
| 用户 | PUT | `/api/users/{id}` | 更新用户 | 本人/ADMIN |
| 用户 | PUT | `/api/users/{id}/status` | 更新用户状态 | ADMIN |
| 用户 | DELETE | `/api/users/{id}` | 删除用户 | ADMIN |
| 学生档案 | GET | `/api/student-profile/current` | 当前学生档案 | STUDENT |
| 学生档案 | GET | `/api/student-profile/{userId}` | 查看学生档案 | 已登录 |
| 学生档案 | PUT | `/api/student-profile` | 更新学生档案 | STUDENT |
| 企业 | GET | `/api/companies` | 企业列表 | 公开/ADMIN |
| 企业 | GET | `/api/companies/current` | 当前企业档案 | COMPANY |
| 企业 | GET | `/api/companies/{id}` | 企业详情 | 公开 |
| 企业 | PUT | `/api/companies` | 更新企业档案 | COMPANY |
| 企业 | PUT | `/api/companies/{id}/audit` | 审核企业 | ADMIN |
| 岗位 | GET | `/api/jobs` | 岗位列表 | 公开 |
| 岗位 | GET | `/api/jobs/{id}` | 岗位详情 | 公开 |
| 岗位 | POST | `/api/jobs` | 发布岗位 | COMPANY |
| 岗位 | PUT | `/api/jobs/{id}` | 更新岗位 | COMPANY |
| 岗位 | DELETE | `/api/jobs/{id}` | 删除岗位 | COMPANY/ADMIN |
| 岗位 | PUT | `/api/jobs/{id}/audit` | 审核岗位 | ADMIN |
| 简历 | GET | `/api/resumes` | 我的简历列表 | STUDENT |
| 简历 | GET | `/api/resumes/{id}` | 简历详情 | 本人/企业/ADMIN |
| 简历 | POST | `/api/resumes` | 创建简历 | STUDENT |
| 简历 | PUT | `/api/resumes/{id}` | 更新简历 | STUDENT |
| 简历 | DELETE | `/api/resumes/{id}` | 删除简历 | STUDENT |
| 投递 | POST | `/api/applications` | 投递简历 | STUDENT |
| 投递 | GET | `/api/applications/my` | 我的投递 | STUDENT |
| 投递 | GET | `/api/applications/job/{jobId}` | 岗位投递列表 | COMPANY |
| 投递 | GET | `/api/applications` | 所有投递 | ADMIN |
| 投递 | PUT | `/api/applications/{id}/status` | 更新投递状态 | COMPANY/ADMIN |
| 投递 | PUT | `/api/applications/{id}/withdraw` | 撤回投递 | STUDENT |
| 面试 | POST | `/api/interviews` | 发起面试邀约 | COMPANY |
| 面试 | GET | `/api/interviews/my` | 我的面试 | STUDENT/COMPANY |
| 面试 | PUT | `/api/interviews/{id}/status` | 更新面试状态 | STUDENT/COMPANY |
| Offer | POST | `/api/offers` | 发放Offer | COMPANY |
| Offer | GET | `/api/offers/my` | 我的Offer | STUDENT/COMPANY |
| Offer | PUT | `/api/offers/{id}/respond` | 回复Offer | STUDENT |
| 宣讲会 | GET | `/api/job-fairs` | 宣讲会列表 | 公开 |
| 宣讲会 | GET | `/api/job-fairs/{id}` | 宣讲会详情 | 公开 |
| 宣讲会 | POST | `/api/job-fairs` | 创建宣讲会 | COMPANY |
| 宣讲会 | PUT | `/api/job-fairs/{id}` | 更新宣讲会 | COMPANY |
| 宣讲会 | PUT | `/api/job-fairs/{id}/audit` | 审核宣讲会 | ADMIN |
| 宣讲会 | POST | `/api/job-fairs/{id}/book` | 预约宣讲会 | STUDENT |
| 宣讲会 | DELETE | `/api/job-fairs/{id}/book` | 取消预约 | STUDENT |
| 宣讲会 | GET | `/api/job-fairs/{id}/bookings` | 预约列表 | COMPANY/ADMIN |
| 公告 | GET | `/api/announcements` | 公告列表 | 公开 |
| 公告 | GET | `/api/announcements/{id}` | 公告详情 | 公开 |
| 公告 | POST | `/api/announcements` | 创建公告 | ADMIN |
| 公告 | PUT | `/api/announcements/{id}` | 更新公告 | ADMIN |
| 公告 | DELETE | `/api/announcements/{id}` | 删除公告 | ADMIN |
| 消息 | GET | `/api/messages` | 消息列表 | 已登录 |
| 消息 | PUT | `/api/messages/{id}/read` | 标记已读 | 已登录 |
| 消息 | PUT | `/api/messages/read-all` | 全部已读 | 已登录 |
| 消息 | GET | `/api/messages/unread-count` | 未读消息数 | 已登录 |
| 日志 | GET | `/api/operation-logs` | 操作日志列表 | ADMIN |
| 统计 | GET | `/api/dashboard` | 统计数据 | 公开 |
| 文件 | POST | `/api/files/upload` | 上传文件 | 已登录 |
