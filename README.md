# 📚 图书管理系统 (Library Management System)

> 毕业设计项目 — 前后端分离架构，基于图书采编、流通、检索、统计的全流程数字化管理。

---

## 目录

1. [系统功能](#系统功能)
2. [技术栈](#技术栈)
3. [快速开始](#快速开始)
4. [测试账号](#测试账号)
5. [项目结构](#项目结构)
6. [API 文档](#api-文档)

---

## 系统功能

### 读者端

| 功能模块 | 说明 |
|---------|------|
| **图书搜索** | 关键词搜索（书名/作者/ISBN）+ 高级筛选（分类/出版社/语言/年份/馆藏地） |
| **图书详情** | 元信息展示、馆藏副本列表、收藏/预约操作、在线阅读跳转 |
| **当前借阅** | 在借图书列表、续借操作、逾期高亮提醒 |
| **借阅历史** | 已归还记录查询（时间范围筛选） |
| **我的预约** | 进行中/待取书/历史预约 Tab，取消预约 |
| **我的收藏** | 收藏图书网格展示，取消收藏 |
| **我的罚款** | 未缴/已缴罚款列表，缴纳金额查看 |
| **个人设置** | 修改姓名/邮箱/电话、修改密码、通知偏好 |
| **消息通知** | 🔔 铃铛图标 + 实时未读数量 + 下拉预览 + 完整通知弹窗（筛选/分页/标记已读） |
| **公告弹窗** | 管理员发布公告后，读者进入页面自动弹窗展示（支持多条翻阅） |

### 管理端

| 功能模块 | 角色 | 说明 |
|---------|------|------|
| **📊 工作台** | 全部 | 数据概览、统计卡片、最近活动、快捷操作 |
| **📖 借还管理** | 管理员 | 扫码借书（读者校验/条码扫描/批量借出）、扫码还书（逾期自动计费） |
| **📚 图书管理** | 采编员 | 图书列表（搜索/筛选/高级搜索）、新增图书（含馆藏副本信息）、编辑、查看详情、出版社管理、馆藏地点管理 |
| **👥 读者管理** | 管理员 | 读者列表（搜索/类型/卡状态筛选）、新增/编辑、挂失/解挂、冻结/解冻、重置密码、查看详情 |
| **📈 统计分析** | 全部 | 借阅统计（柱状图）、热门排行、馆藏统计（环形图）、读者统计 |
| **💰 罚款管理** | 管理员 | 罚款列表（搜索/类型/状态筛选）、缴纳/豁免、批量缴纳/批量豁免 |
| **⚙️ 系统设置** | 仅超管 | 用户管理、角色权限（树形）、菜单管理（树形）、系统参数、操作日志、数据字典、公告管理、数据备份 |

### 后台定时任务

| 任务 | 时间 | 说明 |
|------|------|------|
| 预约超时自动取消 | 每日 02:00 | 等待中/待取书超时自动取消/过期，释放副本 |
| 逾期自动生成罚款 | 每日 03:00 | 首次生成/每日递增，教师类型除外 |
| 逾期提醒通知 | 每日 08:00 | 逾期提醒 + 即将到期提醒（到期前 N 天，可配置） |

---

## 技术栈

### 前端（Vue 3）

| 类别 | 技术栈 |
|------|--------|
| 框架 | Vue 3 (Composition API / `<script setup>`) |
| 语言 | TypeScript 严格模式 |
| 构建 | Vite 5 |
| 路由 | Vue Router 4（懒加载 + 路由守卫） |
| HTTP | Axios（请求/响应拦截器，自动附加 JWT，Token 无感刷新） |
| 图表 | ECharts 5（按需导入，-53% 体积） |
| 样式 | 纯 CSS + CSS 自定义属性（设计 Token 系统） |
| 代码风格 | 无全局状态管理（使用 localStorage 存储用户信息） |

### 后端（Spring Boot）

| 类别 | 技术栈 |
|------|--------|
| 框架 | Spring Boot 3.4.1 |
| 语言 | JDK 21 |
| ORM | MyBatis-Plus 3.5.9（LambdaQueryWrapper，逻辑删除） |
| 安全 | Spring Security + JWT（jjwt 0.12.6，Access Token 2h + Refresh Token 7d） |
| 接口文档 | Knife4j 4.5 |
| 输入校验 | Hibernate Validator |
| 工具类 | Hutool 5.8, EasyExcel 4.0 |
| 定时任务 | Spring `@Scheduled` |
| 数据库 |  |
| 关系库 | MySQL 8.0（InnoDB + utf8mb4） |
| 连接池 | HikariCP |

---

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.8+
- Node.js 18+
- MySQL 8.0+

### 1. 初始化数据库

```bash
# 创建数据库并导入种子数据
mysql -u root -p < sql/init.sql
```

> 默认 root 密码为 `123456`，如需修改请见下方[配置文件修改](#配置文件修改)。

### 2. 启动后端

```bash
cd Server/librarySystemServer

# 编译
./mvnw clean compile

# 运行
./mvnw spring-boot:run

# 或打包后运行
./mvnw clean package -DskipTests
java -jar target/library-system-0.0.1-SNAPSHOT.jar
```

后端默认地址: **http://localhost:8080**

### 3. 启动前端

```bash
cd FRONT/librarySystemFront

# 安装依赖
npm install

# 开发模式运行（热重载）
npm run dev

# 构建
npm run build
```

前端开发地址: **http://localhost:5173**（Vite 自动代理 `/api` → `localhost:8080`）

### 配置文件修改

#### 后端配置

编辑 `Server/librarySystemServer/src/main/resources/application.yml`，修改以下配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/library_system?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: <你的MySQL用户名>
    password: <你的MySQL密码>
```

| 参数 | 需要修改的场景 |
|------|--------------|
| `spring.datasource.url` | MySQL 不在本机或端口不是 3306 时修改地址 |
| `spring.datasource.username` | 改为你的 MySQL 用户名 |
| `spring.datasource.password` | 改为你的 MySQL 密码 |
| `server.port` | 8080 被占用时修改端口 |

#### 前端配置

前端无需修改配置，默认已代理 `/api` → `localhost:8080`。如需修改后端地址，编辑 `FRONT/librarySystemFront/vite.config.ts`：

```ts
proxy: {
  '/api': {
    target: 'http://localhost:8080',  // 改为你的后端地址
    changeOrigin: true,
  },
},
```

---

## 测试账号

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 🔴 **系统管理员** | `admin` | `admin123` | 全部权限 |
| 🔵 **图书管理员** | `librarian` | `admin123` | 借还书、读者管理、罚款处理 |
| 🟢 **采编员** | `cataloger` | `admin123` | 图书编目、分类/出版社/馆藏地管理 |
| 🟡 **读者** | `reader01` | `admin123` | 图书搜索、借阅、预约、收藏、个人中心 |
| 🟡 **读者（教师）** | `reader02` | `admin123` | 读者身份，教师类型借阅规则 |

### 各角色可访问的管理端菜单

| 菜单 | admin | librarian | cataloger |
|------|:-----:|:---------:|:---------:|
| 📊 工作台 | ✅ | ✅ | ✅ |
| 📖 借还管理 | ✅ | ✅ | — |
| 📚 图书管理 | ✅ | — | ✅ |
| 👥 读者管理 | ✅ | ✅ | — |
| 📈 统计分析 | ✅ | ✅ | ✅ |
| 💰 罚款管理 | ✅ | ✅ | — |
| ⚙️ 系统设置 | ✅ | — | — |

---

## 项目结构

```
├── CLAUDE.md                # Claude Code 项目说明
├── README.md                # 本文件
├── API.md                   # API 接口文档（14 个模块）
├── PRD.md                   # 产品需求文档
├── role.md                  # 角色权限定义手册
├── test.md                  # 测试账号汇总
├── todo.md                  # 功能开发进度
├── sql/
│   └── init.sql             # 数据库建表 + 种子数据（24 张表）
├── apperance/               # 前端组件设计规格文档（13 份）
│
├── Server/
│   └── librarySystemServer/ # Spring Boot 后端
│       ├── pom.xml
│       └── src/main/
│           ├── java/com/library/librarysystem/
│           │   ├── controller/  # 14 个 REST API 控制器
│           │   ├── service/     # 业务逻辑层
│           │   ├── mapper/      # MyBatis-Plus 数据访问
│           │   ├── entity/      # 数据库实体
│           │   ├── dto/         # 数据传输对象
│           │   ├── config/      # 安全/MyBatis/CORS 配置
│           │   ├── security/    # JWT 认证过滤器
│           │   └── common/      # 统一响应体、异常处理
│           └── resources/
│               └── application.yml  # 数据源/文件上传/日志/文档
│
└── FRONT/
    └── librarySystemFront/   # Vue 3 前端
        ├── package.json
        ├── vite.config.ts
        └── src/
            ├── main.ts       # 应用入口
            ├── api/          # 7 个 API 模块
            ├── router/       # 路由配置 + 守卫
            ├── layouts/      # AdminLayout（管理员）、ReaderLayout（读者）
            ├── components/   # 7 个通用组件
            ├── views/        # 20+ 页面组件
            └── style.css     # 全局设计 Token
```

---

## API 文档

启动后端后访问 Knife4j 在线接口文档：

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **接口文档**: 详见 `API.md`

### 主要接口模块

| 模块 | 基础路径 | 说明 |
|------|---------|------|
| 认证 | `/api/auth` | 登录/注册/刷新 Token/验证码/修改密码 |
| 图书 | `/api/books` | 搜索/详情/副本/CRUD/导入导出/封面上传 |
| 分类/出版社/馆藏地 | `/api/categories`, `/api/publishers`, `/api/locations` | 分类树、出版社 CRUD、馆藏地 CRUD |
| 借阅 | `/api/borrow` | 借书/还书/续借/当前借阅/历史查询 |
| 预约 | `/api/reservations` | 创建/取消/当前预约/取书确认 |
| 罚款 | `/api/fines` | 列表/缴纳/豁免/批量操作 |
| 收藏 | `/api/favorites` | 增删查 |
| 通知 | `/api/notifications` | 列表/未读数/标记已读/删除 |
| 统计 | `/api/statistics` | 借阅统计/热门排行/馆藏统计/读者统计/流通统计 |
| 系统 | `/api/system` | 用户/角色/菜单/配置/字典/日志/公告/备份 |
