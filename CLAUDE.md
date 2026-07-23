# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

图书管理系统 (Library Management System) — 毕业设计项目。前后端分离架构，基于图书采编、流通、检索、统计的全流程数字化管理。

**技术栈:** Vue 3 + TypeScript + Vite (前端) / Spring Boot 4.1 + JDK 21 (后端) / MySQL 8.0 (数据库)

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

# 开发模式运行 (热重载)
npm run dev

# 构建
npm run build

# 预览构建产物
npm run preview
```

### Database

```bash
# 初始化数据库 (需要 MySQL 8.0 运行中)
"D:\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 < sql/init.sql
```

数据库配置参数: `localhost:3306`, 用户名 `root`, 密码 `123456`, 库名 `library_system`

---

## Project Structure

```
├── PRD.md                      # 产品需求文档
├── API.md                      # API 接口文档
├── CLAUDE.md                   # 本文件
├── sql/
│   └── init.sql                # 数据库建表 + 种子数据脚本 (24 张表)
├── apperance/                   # 前端设计规格文档
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
│       ├── pom.xml             # Maven 配置 (Spring Boot 4.1, JDK 21)
│       └── src/main/
│           ├── java/com/library/librarysystem/
│           │   ├── LibrarySystemServerApplication.java  # 启动入口
│           │   └── ...          # controllers/services/mappers/entities/dto
│           └── resources/
│               └── application.yml  # 数据源/MyBatis-Plus/日志配置
│
└── FRONT/
    └── librarySystemFront/      # 前端 Vue 3
        ├── package.json         # 依赖配置
        ├── vite.config.ts       # Vite 构建配置
        └── src/
            ├── main.ts          # 入口
            ├── App.vue          # 根组件
            ├── style.css        # 全局样式
            └── components/      # Vue 组件
```

---

## Architecture

### Backend Architecture

Spring Boot 标准分层架构，按模块分包:

```
controller/  →  REST API 入口 (@RestController)
service/     →  业务逻辑层 (@Service)
mapper/      →  MyBatis-Plus 数据访问层 (@Mapper)
entity/      →  数据库实体映射 (@TableName)
dto/         →  数据传输对象 (请求/响应)
config/      →  配置类 (Security, MyBatis-Plus, Swagger)
common/      →  公共工具 (统一响应体, 异常处理, 常量)
```

**核心依赖:**
- Spring Boot 4.1 (JDK 21)
- MyBatis-Plus 3.5.9 (ORM)
- Spring Security + JWT (认证授权)
- Knife4j 4.5 (API 文档)
- Hutool 5.8 (工具类)
- EasyExcel 4.0 (Excel 导入导出)

### Frontend Architecture

Vue 3 + Composition API (`<script setup>`):

```
src/
├── api/           # Axios 请求封装
├── router/        # Vue Router 路由配置 (见 PRD.md 路由表)
├── stores/        # Pinia 状态管理
├── views/         # 页面组件
│   ├── login/     # 登录/注册
│   ├── reader/    # 读者端 (搜索/借阅/个人中心)
│   └── admin/     # 管理后台 (Dashboard/图书/读者/统计/系统)
├── components/    # 通用组件
├── layouts/       # 布局组件 (读者端/管理端)
├── utils/         # 工具函数
├── styles/        # 全局样式 (TailwindCSS + SCSS)
├── types/         # TypeScript 类型定义
└── assets/        # 静态资源
```

**前端路由:** 读者端 `/reader/*`, 管理端 `/admin/*` (详见 PRD.md §8)

### Database

24 张表，MySQL 8.0 + InnoDB + utf8mb4：

**系统管理:** `sys_user`, `sys_role`, `sys_user_role`, `sys_menu`, `sys_role_menu`, `sys_config`, `sys_dict`, `sys_dict_item`, `sys_operation_log`, `sys_backup`

**读者管理:** `reader`, `reader_type`

**图书管理:** `book_info`, `book_copy`, `category`, `publisher`, `location`

**流通管理:** `borrow_record`, `reservation`, `fine_record`

**读者功能:** `favorite`, `notification`, `announcement`, `book_recommendation`

---

## Design Tokens

定义在 Pencil 设计文件中，所有页面和组件沿用统一风格:

| Token | 值 | 用途 |
|-------|-----|------|
| `$bg-primary` | `#FFFFFF` | 卡片/输入框背景 |
| `$bg-secondary` | `#F7F8FA` | 页面背景 |
| `$bg-inverse` | `#0A0A0A` | 深色面板背景 |
| `$accent` | `#4A9FD8` | 蓝色强调色 |
| `$accent-light` | `#E8F4FD` | 浅蓝氛围色 |
| `$success` | `#34D399` | 成功/可用 |
| `$warning` | `#FBBF24` | 警告/等待 |
| `$danger` | `#F87171` | 危险/逾期 |
| `$text-primary` | `#1A1A1A` | 主文字 |
| `$text-secondary` | `#666666` | 次要文字 |
| `$text-muted` | `#888888` | 弱化文字 |
| `$border` | `#E5E7EB` | 边框色 |
| `$card-radius` | `16px` | 卡片圆角 |
| `$input-radius` | `12px` | 输入框圆角 |

字体: Inter (正文/标题), Geist Mono (数据)

---

## Key Notes

- **Redis 暂未启用** — `pom.xml` 和 `application.yml` 中已注释，启用时需取消注释并启动 Redis 服务
- **API 路径前缀:** 全部接口以 `/api` 开头 (如 `/api/books`, `/api/auth/login`)
- **认证方式:** JWT Bearer Token (Access Token 2h, Refresh Token 7d)
- **响应格式:** 统一 `{code, message, data, timestamp}`
- **样式设计参考:** 所有页面的组件规格放在 `apperance/` 目录，代码复刻时直接参考
- **设计工具:** 使用 Pencil (.pen 文件) 做 UI 设计，通过 MCP `mcp__pencil__*` 工具操作
