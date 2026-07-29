# 角色权限定义手册

> 基于 PRD 功能矩阵 + sys_menu 权限数据  
> 四种预设角色，权限由低到高

---

## 1. 角色总览

| 角色编码 | 角色名称 | 说明 |
|---------|---------|------|
| `ROLE_READER` | 读者 | 在校学生/教职工/注册读者 — 可检索图书、查看个人借阅、预约、续借 |
| `ROLE_LIBRARIAN` | 图书管理员 | 承担日常流通工作 — 借还书、读者管理、罚款处理 |
| `ROLE_CATALOGER` | 采编员 | 专注于图书 metadata 管理 — 图书编目、分类、导入导出 |
| `ROLE_ADMIN` | 系统管理员 | 拥有全部权限 — 用户管理、系统配置、日志审计 |

---

## 2. 权限矩阵（按功能模块）

| 功能模块 | 读者 | 图书管理员 | 采编员 | 系统管理员 |
|---------|:---:|:--------:|:-----:|:---------:|
| 图书搜索/浏览 | ✅ | ✅ | ✅ | ✅ |
| 图书详情查看 | ✅ | ✅ | ✅ | ✅ |
| 个人借阅查询 | ✅ | ✅ | ✅ | ✅ |
| 预约图书 | ✅ | ✅ | ✅ | ✅ |
| 续借图书 | ✅ | ✅ | ✅ | ✅ |
| **借书操作** | — | ✅ | — | ✅ |
| **还书操作** | — | ✅ | — | ✅ |
| 逾期罚款处理 | — | ✅ | — | ✅ |
| **图书编目** | — | — | ✅ | ✅ |
| **分类管理** | — | — | ✅ | ✅ |
| 出版社管理 | — | — | ✅ | ✅ |
| 馆藏地点管理 | — | — | ✅ | ✅ |
| **批量导入/导出** | — | — | ✅ | ✅ |
| 封面上传 | — | — | ✅ | ✅ |
| 公告发布 | — | ✅ | ✅ | ✅ |
| 读者管理（CRUD/挂失/冻结） | — | ✅ | — | ✅ |
| 用户管理 | — | — | — | ✅ |
| 角色管理 | — | — | — | ✅ |
| 菜单/权限管理 | — | — | — | ✅ |
| 操作日志查看 | — | — | — | ✅ |
| 系统参数配置 | — | — | — | ✅ |
| 数据字典管理 | — | — | — | ✅ |
| 数据备份/恢复 | — | — | — | ✅ |
| 借阅统计 | — | ✅ | ✅ | ✅ |
| 流通统计 | — | ✅ | — | ✅ |
| 热门排行查看 | ✅ | ✅ | ✅ | ✅ |
| 馆藏统计 | — | ✅ | ✅ | ✅ |
| 读者统计 | — | — | — | ✅ |
| 公告管理（增删改查） | — | ✅ | ✅ | ✅ |

---

## 3. 菜单/页面访问权限

### 3.1 读者端菜单（ROLE_READER）

| 菜单 | 路由 | 说明 |
|------|------|------|
| 首页 | `/home` | 图书搜索入口，分类浏览，热门推荐 |
| 搜索结果 | `/books` | 图书搜索列表（含高级筛选） |
| 图书详情 | `/books/:id` | 图书元信息、副本列表 |
| 当前借阅 | `/reader` | 当前借阅列表、统计摘要、续借操作 |
| 借阅历史 | `/reader/history` | 历史借阅记录（时间筛选） |
| 我的预约 | `/reader/reservations` | 预约列表（进行中/待取书/历史） |
| 我的收藏 | `/reader/favorites` | 收藏的图书网格 |
| 我的罚款 | `/reader/fines` | 未缴/已缴罚款列表 |
| 个人信息设置 | `/reader/settings` | 修改姓名/邮箱/电话/密码 |

### 3.2 管理端菜单

#### 一级目录

| 菜单 | 路由 | 可见角色 |
|------|------|---------|
| 📊 工作台 | `/admin` | 管理员/采编员/图书管理员 |
| 📖 流通管理 | `/admin/borrow` | 管理员/图书管理员 |
| 📚 图书管理 | `/admin/books` | 管理员/采编员 |
| 👥 读者管理 | `/admin/readers` | 管理员/图书管理员 |
| 📈 统计分析 | `/admin/statistics` | 管理员/采编员/图书管理员 |
| ⚙️ 系统管理 | `/admin/system` | 仅管理员 |
| 📢 公告管理 | `/admin/announcement` | 管理员/采编员/图书管理员 |

#### 流通管理子菜单

| 菜单 | 路由 | 权限点 | 操作角色 |
|------|------|--------|---------|
| 借书 | `/admin/borrow/borrow` | `borrow:create` | 管理员/图书管理员 |
| 还书 | `/admin/borrow/return` | `borrow:return` | 管理员/图书管理员 |
| 预约管理 | `/admin/borrow/reserve` | `reservation:list` | 管理员/图书管理员 |
| 罚款管理 | `/admin/borrow/fine` | `fine:list` | 管理员/图书管理员 |

#### 图书管理子菜单

| 菜单 | 路由 | 权限点 | 操作角色 |
|------|------|--------|---------|
| 图书列表 | `/admin/books/list` | `book:list` | 管理员/采编员 |
| 新增图书 | `/admin/books/create` | `book:create` | 管理员/采编员 |
| 批量导入 | `/admin/books/import` | `book:import` | 管理员/采编员 |
| 分类管理 | `/admin/books/category` | `category:list` | 管理员/采编员 |
| 出版社管理 | `/admin/books/publisher` | `publisher:list` | 管理员/采编员 |
| 馆藏地点 | `/admin/books/location` | `location:list` | 管理员/采编员 |
| 图书盘点 | `/admin/books/inventory` | `inventory:import` | 管理员/采编员 |

#### 统计分析子菜单

| 菜单 | 路由 | 操作角色 |
|------|------|---------|
| 借阅统计 | `/admin/statistics/borrow` | 管理员/采编员/图书管理员 |
| 热门排行 | `/admin/statistics/hot` | 管理员/采编员/图书管理员 |
| 馆藏统计 | `/admin/statistics/collection` | 管理员/采编员 |

#### 系统管理子菜单（仅 ROLE_ADMIN）

| 菜单 | 路由 | 权限点 |
|------|------|--------|
| 用户管理 | `/admin/system/users` | `system:user:list/create/edit/delete/reset-pwd` |
| 角色管理 | `/admin/system/roles` | `system:role:list/create/edit/delete` |
| 菜单管理 | `/admin/system/menus` | `system:menu:list/create/edit/delete` |
| 系统参数 | `/admin/system/config` | `system:config:list/edit` |
| 操作日志 | `/admin/system/logs` | `system:log:list` |
| 数据字典 | `/admin/system/dict` | `system:dict:list/create/edit/delete` |
| 备份恢复 | `/admin/system/backup` | `system:backup:create/download/restore` |

---

## 4. 按钮级权限（完整列表）

### 4.1 流通管理按钮

| 权限标识 | 名称 | 操作角色 |
|---------|------|---------|
| `borrow:create` | 执行借书 | 管理员/图书管理员 |
| `borrow:return` | 执行还书 | 管理员/图书管理员 |
| `reservation:list` | 查询预约 | 管理员/图书管理员 |
| `reservation:pickup` | 取书确认 | 管理员/图书管理员 |
| `reservation:cancel` | 取消预约 | 管理员/图书管理员 |
| `fine:list` | 查询罚款 | 管理员/图书管理员 |
| `fine:pay` | 缴纳罚款 | 管理员/图书管理员 |
| `fine:waive` | 豁免罚款 | 管理员/图书管理员 |

### 4.2 图书管理按钮

| 权限标识 | 名称 | 操作角色 |
|---------|------|---------|
| `book:list` | 查询图书 | 管理员/采编员 |
| `book:create` | 新增图书 | 管理员/采编员 |
| `book:edit` | 编辑图书 | 管理员/采编员 |
| `book:delete` | 删除图书 | 管理员/采编员 |
| `book:status` | 上架下架 | 管理员/采编员 |
| `book:export` | 导出图书 | 管理员/采编员 |
| `book:import` | 导入图书 | 管理员/采编员 |
| `category:list` | 查询分类 | 管理员/采编员 |
| `category:create` | 新增分类 | 管理员/采编员 |
| `category:edit` | 编辑分类 | 管理员/采编员 |
| `category:delete` | 删除分类 | 管理员/采编员 |
| `publisher:list` | 查询出版社 | 管理员/采编员 |
| `publisher:create` | 新增出版社 | 管理员/采编员 |
| `publisher:edit` | 编辑出版社 | 管理员/采编员 |
| `publisher:delete` | 删除出版社 | 管理员/采编员 |
| `location:list` | 查询地点 | 管理员/采编员 |
| `location:create` | 新增地点 | 管理员/采编员 |
| `location:edit` | 编辑地点 | 管理员/采编员 |
| `location:delete` | 删除地点 | 管理员/采编员 |
| `inventory:import` | 盘点导入 | 管理员/采编员 |
| `inventory:report` | 盘点报告 | 管理员/采编员 |

### 4.3 读者管理按钮

| 权限标识 | 名称 | 操作角色 |
|---------|------|---------|
| `reader:list` | 查询读者 | 管理员/图书管理员 |
| `reader:create` | 新增读者 | 管理员/图书管理员 |
| `reader:edit` | 编辑读者 | 管理员/图书管理员 |
| `reader:card` | 挂失解挂 | 管理员/图书管理员 |
| `reader:freeze` | 冻结解冻 | 管理员/图书管理员 |
| `reader:reset-pwd` | 重置密码 | 管理员/图书管理员 |

### 4.4 系统管理按钮（仅 ROLE_ADMIN）

| 权限标识 | 名称 |
|---------|------|
| `system:user:list` | 查询用户 |
| `system:user:create` | 新增用户 |
| `system:user:edit` | 编辑用户 |
| `system:user:delete` | 删除用户 |
| `system:user:reset-pwd` | 重置用户密码 |
| `system:role:list` | 查询角色 |
| `system:role:create` | 新增角色 |
| `system:role:edit` | 编辑角色 |
| `system:role:delete` | 删除角色 |
| `system:menu:list` | 查询菜单 |
| `system:menu:create` | 新增菜单 |
| `system:menu:edit` | 编辑菜单 |
| `system:menu:delete` | 删除菜单 |
| `system:config:list` | 查询参数 |
| `system:config:edit` | 编辑参数 |
| `system:log:list` | 查询日志 |
| `system:dict:list` | 查询字典 |
| `system:dict:create` | 新增字典 |
| `system:dict:edit` | 编辑字典 |
| `system:dict:delete` | 删除字典 |
| `system:backup:create` | 创建备份 |
| `system:backup:download` | 下载备份 |
| `system:backup:restore` | 恢复备份 |

---

## 5. API 接口权限

| 模块 | 公开接口 | 任意角色 | 管理员/图书管理员 | 管理员/采编员 | 仅管理员 |
|------|---------|---------|----------------|-------------|---------|
| 认证 | 登录/注册/刷新/验证码 | GET /auth/me, PUT /auth/password | | | |
| 图书 | | GET /books, /hot, /new-arrivals, /{id}, /{id}/copies | | POST/PUT/DELETE /books, import/export/cover | |
| 分类 | | GET /categories/tree | | POST/PUT/DELETE /categories | |
| 出版社 | | | | GET/POST/PUT/DELETE /publishers | |
| 馆藏地点 | | | | GET/POST/PUT/DELETE /locations | |
| 借阅 | | GET /borrow/current, /summary, /history POST /borrow/{id}/renew | POST /borrow, PUT /borrow/return | | |
| 预约 | | GET /reservations/current | POST/DELETE /reservations | | |
| 罚款 | | GET /fines | POST/PUT /fines/{id}/pay, /waive | | |
| 读者 | | GET /readers/my-profile, PUT /readers/my-profile | GET/POST/PUT /readers | | |
| 统计 | | GET /statistics/hot-books | GET /statistics/borrow, /circulation | GET /statistics/collection | GET /statistics/readers |
| 系统 | | | | | 全部 /api/system/** |

---

## 6. 读者类型配置（借阅规则）

存储在 `reader_type` 表，每种读者类型的借阅参数可以独立配置：

| 配置项 | 学生 | 教师 | 教职工 | 校外读者 |
|-------|:---:|:---:|:-----:|:-------:|
| 最大借阅数 | 5 | 10 | 8 | 3 |
| 借阅天数 | 30 | 60 | 45 | 14 |
| 续借次数 | 1 | 2 | 1 | 0 |
| 续借天数 | 15 | 30 | 20 | 0 |
| 逾期费率(元/天) | 0.50 | 0.00 | 0.30 | 1.00 |
| 预约保留时长(小时) | 48 | 72 | 48 | 24 |

### 系统参数默认值（sys_config）

| 配置键 | 默认值 | 说明 |
|--------|-------|------|
| `borrow.max_books` | 5 | 最大借阅数（读者类型未覆盖时使用） |
| `borrow.days` | 30 | 默认借阅天数 |
| `borrow.renew_count` | 1 | 默认续借次数 |
| `borrow.renew_days` | 15 | 默认续借天数 |
| `borrow.renew_advance_days` | 7 | 续借提前天数（距应还≤N天可续借） |
| `fine.overdue_rate` | 0.50 | 逾期费率(元/天) |
| `fine.damage_multiple` | 2.00 | 损坏赔偿倍数 |
| `fine.lost_multiple` | 3.00 | 丢失赔偿倍数 |
| `reservation.keep_hours` | 48 | 预约保留时长(小时) |
| `reader.initial_password` | 123456 | 读者初始密码 |
