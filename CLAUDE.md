# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

图书管理系统 (Library Management System) — 毕业设计项目。前后端分离架构，基于图书采编、流通、检索、统计的全流程数字化管理。

**技术栈:** Vue 3 + TypeScript + Vite (前端) / Spring Boot 3.4 + JDK 21 (后端) / MySQL 8.0 (数据库)

---

## Quick Start

### Backend (Spring Boot)

```bash
# 进入后端目录
cd Server/librarySystemServer

# 用 Maven 编译
./mvnw clean compile

# 运行
./mvnw spring-boot:run

# 打包
./mvnw clean package -DskipTests

# 仅运行测试
./mvnw test
```

**后端默认地址:** http://localhost:8080
**API 文档 (Knife4j):** http://localhost:8080/swagger-ui.html

### Frontend (Vue 3)

```bash
# 进入前端目录
cd FRONT/librarySystemFront

# 安装依赖
npm install

# 开发模式运行 (热重载，默认端口 5173)
npm run dev

# 类型检查 + 构建
npm run build

# 预览构建产物
npm run preview
```

**前端开发地址:** http://localhost:5173 (Vite 代理 `/api` → `localhost:8080`)

### Database

```bash
# 初始化数据库 (需要 MySQL 8.0 运行中)
"D:\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 < sql/init.sql
```

数据库配置参数: `localhost:3306`, 用户名 `root`, 密码 `123456`, 库名 `library_system`

---

## Project Structure

```
├── PRD.md                      # 产品需求文档 (角色定义/功能全景/路由设计)
├── API.md                      # API 接口文档 (14 个模块)
├── CLAUDE.md                   # 本文件
├── sql/
│   └── init.sql                # 数据库建表 + 种子数据脚本 (24 张表)
├── apperance/                   # 前端组件设计规格文档
│   ├── 01-login-page.md        # 登录页组件规格
│   ├── 02-register-page.md     # 注册页组件规格
│   ├── 03-reader-home.md       # 读者首页组件规格
│   ├── 04-book-detail.md       # 图书详情页组件规格
│   ├── 05-reader-dashboard.md  # 读者中心组件规格
│   ├── 06-admin-dashboard.md   # 管理后台组件规格
│   └── components-*.md         # 可复用组件规格 (按钮/输入框/卡片/标签等)
│
├── Server/
│   └── librarySystemServer/    # 后端 Spring Boot
│       ├── pom.xml             # Maven 配置 (Spring Boot 3.4.1, JDK 21)
│       └── src/main/
│           ├── java/com/library/librarysystem/
│           │   ├── LibrarySystemServerApplication.java  # 启动入口
│           │   ├── controller/  # REST API 入口
│           │   ├── service/     # 业务逻辑层
│           │   ├── mapper/      # MyBatis-Plus 数据访问
│           │   ├── entity/      # 数据库实体
│           │   ├── dto/         # 数据传输对象
│           │   ├── config/      # 安全/MyBatis/CORS 配置
│           │   ├── security/    # JWT 认证过滤器
│           │   └── common/      # 统一响应体、异常处理
│           └── resources/
│               └── application.yml  # 数据源/MyBatis-Plus/日志/文件上传
│
└── FRONT/
    └── librarySystemFront/      # 前端 Vue 3 + TypeScript
        ├── package.json         # 依赖配置 (vue-router, axios, echarts)
        ├── tsconfig.app.json    # TypeScript 严格模式配置
        ├── vite.config.ts       # Vite 构建 + 开发代理配置
        └── src/
            ├── main.ts          # 应用入口
            ├── App.vue          # 根组件
            ├── style.css        # 全局样式 + CSS 自定义属性 (设计 Token)
            ├── components/      # 6 个通用组件 (导出见 index.ts)
            ├── api/             # Axios 封装 + 模块化 API 层
            ├── router/          # Vue Router (懒加载路由)
            ├── layouts/         # AdminLayout
            └── views/           # 页面组件 (login/reader/admin)
```

---

## Architecture

### Backend Architecture

Spring Boot **分层架构**，按功能模块分包:

```
controller/  →  REST API (@RestController, @RequestMapping)
service/     →  业务逻辑 (@Service, @Transactional)
mapper/      →  MyBatis-Plus (@Mapper, LambdaQueryWrapper)
entity/      →  数据库实体 (@TableName, @Data)
dto/         →  请求/响应数据传输对象
config/      →  安全配置/MyBatis-Plus/CORS
security/    →  JWT 过滤器 + 认证逻辑
common/      →  Result<T> 统一响应体, BusinessException
```

**核心依赖:**
- Spring Boot 3.4.1 (JDK 21)
- MyBatis-Plus 3.5.9 (ORM)
- Spring Security + JWT (jjwt 0.12.6)
- Knife4j 4.5 (API 文档)
- Hutool 5.8 (工具类)
- EasyExcel 4.0 (Excel 导入导出)
- Lombok (`@Data`, `@RequiredArgsConstructor`)
- Redis 依赖已注释待启用

#### Backend Coding Patterns

1. **依赖注入:** 使用 `@RequiredArgsConstructor` + `private final` 构造器注入
2. **控制器:** 每个 Controller 使用 `@RestController` + `@RequestMapping("/api/xxx")` + `@RequiredArgsConstructor`
3. **权限控制:** `@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CATALOGER')")` 在方法级别，全局 SecurityConfig 默认需要认证
4. **请求处理:** 增删改接口接收 `Map<String, Object>` 作为请求体 (灵活)，列表接口用 `@RequestParam` 参数
5. **响应:** 所有 Controller 方法返回 `Result<T>` (统一 `{code, message, data, timestamp}`)
6. **分页:** MyBatis-Plus `Page` + `LambdaQueryWrapper`，返回 `{records, total, size, current, pages}`
7. **事务:** 写操作 (增删改) 加 `@Transactional`
8. **异常:** 业务异常抛出 `BusinessException`，由 `GlobalExceptionHandler` 统一处理
9. **Mapper:** 字段映射为 `map-underscore-to-camel-case: true`，启用逻辑删除

### Frontend Architecture

Vue 3 + Composition API (`<script setup lang="ts">`)，TypeScript 严格模式:

```
src/
├── api/           # Axios 实例 + 拦截器 (自动附加 JWT, Token 刷新)
│   ├── index.ts   # Axios 实例创建, 请求/响应拦截器, 自动刷新 Token
│   ├── auth.ts    # 认证相关 API
│   ├── books.ts   # 图书相关 API
│   ├── borrow.ts  # 借阅相关 API
│   ├── readers.ts # 读者管理 API
│   ├── statistics.ts # 统计 API
│   └── system.ts  # 系统管理 API
├── router/        # Vue Router (createWebHistory, 懒加载路由)
├── components/    # 通用组件 (index.ts 统一导出)
│   ├── AppBtn.vue       # 按钮 (primary/secondary/danger/ghost)
│   ├── AppInput.vue     # 输入框 (default/focus/error/success)
│   ├── AppBadge.vue     # 标签组件
│   ├── AppNavItem.vue   # 导航项
│   ├── AppBookCard.vue  # 图书卡片
│   └── AppStatCard.vue  # 统计卡片
├── layouts/       # AdminLayout (管理员布局)
└── views/         # 页面组件
    ├── login/     # Login.vue, Register.vue
    ├── reader/    # ReaderHome, ReaderDashboard, BookDetail
    └── admin/     # AdminDashboard, AdminBooks, AdminReaders, AdminStatistics, AdminSettings*
```

#### Frontend Coding Patterns

1. **组件命名:** 通用组件使用 `App` 前缀 (AppBtn, AppInput)，使用 `defineOptions({ name: '...' })`
2. **样式方案:** 纯 CSS + CSS 自定义属性 (无 TailwindCSS、无 SCSS)，作用域样式 `<style scoped>`
3. **状态管理:** 无 Pinia/全局状态库 — 使用 `localStorage` 直接存储 token 和用户信息
4. **路由:** 懒加载 `() => import('...')`，`router.beforeEach` 守卫检查 token
5. **HTTP 请求:** Axios 实例带拦截器，自动在请求头附加 `Bearer` token，401 时自动尝试 refresh token
6. **设计系统:** 使用 CSS 自定义属性 (参考 `style.css` 和 `apperance/` 设计规格)
7. **Vite 代理:** `vite.config.ts` 配置 `/api` → `http://localhost:8080`

---

## Security & Auth

| 项目 | 说明 |
|------|------|
| 认证方式 | JWT Bearer Token (Access Token 2h, Refresh Token 7d) |
| Token 存储 | 前端 `localStorage` (`accessToken`, `refreshToken`) |
| Token 刷新 | Axios 响应拦截器自动检测 401，调用 `/api/auth/refresh` 无感刷新 |
| 安全配置 | Spring Security STATELESS 模式，CSRF 禁用 |
| 公开接口 | `/api/auth/login`, `/api/auth/register`, `/api/auth/refresh`, `/api/auth/captcha` |
| 权限粒度 | 方法级 `@PreAuthorize` (四种角色: ROLE_ADMIN, ROLE_CATALOGER, ROLE_LIBRARIAN, ROLE_READER) |

## Database

24 张表，MySQL 8.0 + InnoDB + utf8mb4：

**系统管理 (10):** `sys_user`, `sys_role`, `sys_user_role`, `sys_menu`, `sys_role_menu`, `sys_config`, `sys_dict`, `sys_dict_item`, `sys_operation_log`, `sys_backup`

**读者管理 (2):** `reader`, `reader_type`

**图书管理 (5):** `book_info`, `book_copy`, `category`, `publisher`, `location`

**流通管理 (3):** `borrow_record`, `reservation`, `fine_record`

**读者功能 (4):** `favorite`, `notification`, `announcement`, `book_recommendation`

MyBatis-Plus 配置: 逻辑删除字段 `deleted` (1=已删除, 0=未删除)，ID 策略 `auto`，开启下划线转驼峰

---

## API 模块总览

| 模块 | 基础路径 | 说明 |
|------|----------|------|
| Auth | `/api/auth` | 登录/注册/刷新Token/验证码 |
| Books | `/api/books` | 图书搜索/详情/副本/热搜/新书 + 管理端CRUD/导入导出/封面上传 |
| Categories | `/api/categories` | 图书分类管理 |
| Borrow | `/api/borrow` | 当前借阅/读者摘要/借还书操作 |
| Readers | `/api/readers` | 读者管理CRUD |
| Statistics | `/api/statistics` | 流通统计/馆藏统计/读者统计/借阅趋势/热门图书/最近活动 |
| System | `/api/system` | 用户/角色/菜单/配置/字典/日志/公告/备份 |

**响应格式:** `{"code": 200, "message": "success", "data": {}, "timestamp": 1700000000000}`

---

## Design Tokens

CSS 自定义属性定义在 `FRONT/librarySystemFront/src/style.css`，所有页面和组件沿用统一风格:

| Token | CSS Variable | 值 | 用途 |
|-------|-------------|-----|------|
| bg-primary | `--bg-primary` | `#FFFFFF` | 卡片/输入框背景 |
| bg-secondary | `--bg-secondary` | `#F7F8FA` | 页面背景 |
| accent | `--accent` | `#4A9FD8` | 蓝色强调色 |
| accent-light | `--accent-light` | `#E8F4FD` | 浅蓝氛围色 |
| success | `--success` | `#34D399` | 成功/可用 |
| warning | `--warning` | `#FBBF24` | 警告/等待 |
| danger | `--danger` | `#F87171` | 危险/逾期 |
| text-primary | `--text-primary` | `#1A1A1A` | 主文字 |
| text-secondary | `--text-secondary` | `#666666` | 次要文字 |
| text-muted | `--text-muted` | `#888888` | 弱化文字 |
| border | `--border` | `#E5E7EB` | 边框色 |
| card-radius | `--card-radius` | `16px` | 卡片圆角 |
| input-radius | `--input-radius` | `12px` | 输入框圆角 |
| button-radius | `--button-radius` | `10px` | 按钮圆角 |

字体: Inter (正文/标题, `--font-sans`), Geist Mono (数据, `--font-mono`)

组件设计规格文档位于 `apperance/` 目录，开发 UI 时直接参考。

---

## Key Notes

- **API 路径前缀:** 全部接口以 `/api` 开头 (如 `/api/books`, `/api/auth/login`)
- **Redis 暂未启用** — `pom.xml` 和 `application.yml` 中已注释，启用时需取消注释并启动 Redis 服务
- **前端无全局状态管理** — 不使用 Pinia/Vuex，用户信息和 token 存储在 `localStorage`
- **前端构建含类型检查:** `npm run build` = `vue-tsc -b && vite build` (TypeScript 严格检查)
- **样式:** 使用纯 CSS + CSS 自定义属性，不含 TailwindCSS 或 SCSS
- **设计工具:** 使用 Pencil (.pen 文件) 做 UI 设计，通过 MCP `mcp__pencil__*` 工具操作
- **Router 守卫:** `router.beforeEach` 检查 `requiresAuth` meta 和 token
