# 图书管理系统 - API 接口文档

> **版本：** v1.0  
> **基础路径：** `/api`  
> **数据格式：** 请求/响应均为 JSON  
> **字符集：** UTF-8

---

## 目录

1. [通用约定](#1-通用约定)
2. [认证模块](#2-认证模块)
3. [图书模块](#3-图书模块)
4. [分类/出版社/馆藏地点](#4-分类出版社馆藏地点)
5. [借阅模块](#5-借阅模块)
6. [预约模块](#6-预约模块)
7. [罚款模块](#7-罚款模块)
8. [读者管理模块](#8-读者管理模块)
9. [统计分析模块](#9-统计分析模块)
10. [系统管理模块](#10-系统管理模块)
11. [通知公告模块](#11-通知公告模块)
12. [收藏模块](#12-收藏模块)
13. [推荐图书模块](#13-推荐图书模块)
14. [公共接口](#14-公共接口)

---

## 1. 通用约定

### 1.1 统一响应格式

**成功响应：**

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1700000000000
}
```

**分页响应：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  },
  "timestamp": 1700000000000
}
```

**失败响应：**

```json
{
  "code": 400,
  "message": "参数校验失败: 书名不能为空",
  "data": null,
  "timestamp": 1700000000000
}
```

### 1.2 业务状态码

| code | 含义 |
|---|---|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未认证 / Token 失效 |
| 403 | 无权限访问 |
| 404 | 资源不存在 |
| 409 | 业务冲突（如已借出） |
| 422 | 业务校验不通过 |
| 429 | 请求过于频繁 |
| 500 | 服务器内部错误 |

### 1.3 认证方式

- 使用 JWT Bearer Token，在请求头中传递：
  ```
  Authorization: Bearer <access_token>
  ```
- Access Token 有效期：2 小时
- Refresh Token 有效期：7 天，用于无感刷新

### 1.4 分页请求参数

所有列表接口统一使用以下分页参数：

| 参数 | 位置 | 类型 | 默认值 | 说明 |
|---|---|---|---|---|
| page | query | int | 1 | 当前页码 |
| size | query | int | 10 | 每页条数(最大50) |
| sort | query | String | — | 排序字段,如 `create_time,desc` |

---

## 2. 认证模块

### 2.1 登录

```
POST /auth/login
```

**权限：** 无需认证

**请求体：**

```json
{
  "username": "admin",
  "password": "admin123",
  "captchaKey": "uuid-string",
  "captchaCode": "A1B2"
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |
| captchaKey | String | 否 | 验证码Key(失败3次后必填) |
| captchaCode | String | 否 | 验证码值 |

**响应数据：**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": 7200,
  "refreshToken": "dGhpcyBpcyBhIHJlZnJl...",
  "userId": 1,
  "username": "admin",
  "realName": "系统管理员",
  "roles": ["ROLE_ADMIN"],
  "permissions": ["book:list", "book:create", "system:user:list", "..."]
}
```

### 2.2 登出

```
POST /auth/logout
```

**权限：** 已认证

**请求头：** `Authorization: Bearer <token>`

**响应：** 标准成功响应

### 2.3 刷新 Token

```
POST /auth/refresh
```

**权限：** 无需认证（使用 Refresh Token）

**请求体：**

```json
{
  "refreshToken": "dGhpcyBpcyBhIHJlZnJl..."
}
```

**响应数据：**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": 7200,
  "refreshToken": "bmV3IHJlZnJlcyB0b2tl..."
}
```

### 2.4 获取验证码

```
GET /auth/captcha
```

**权限：** 无需认证

**响应数据：**

```json
{
  "captchaKey": "a1b2c3d4-e5f6-...",
  "captchaImage": "data:image/png;base64,iVBORw0KGgo..."
}
```

### 2.5 获取当前登录用户信息

```
GET /auth/me
```

**权限：** 已认证

**响应数据：**

```json
{
  "userId": 1,
  "username": "admin",
  "realName": "系统管理员",
  "email": "admin@library.com",
  "phone": "13800138000",
  "avatar": null,
  "gender": 0,
  "roles": ["ROLE_ADMIN"],
  "permissions": ["book:list", "book:create", "..."]
}
```

### 2.6 修改密码

```
PUT /auth/password
```

**权限：** 已认证

**请求体：**

```json
{
  "oldPassword": "old123",
  "newPassword": "new456",
  "confirmPassword": "new456"
}
```

---

## 3. 图书模块

### 3.1 分页搜索图书

```
GET /books?keyword=三体&page=1&size=10
```

**权限：** 已认证（任意角色）

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| keyword | String | 否 | 关键词(同时匹配书名/作者/ISBN) |
| title | String | 否 | 书名(模糊) |
| author | String | 否 | 作者 |
| isbn | String | 否 | ISBN(精确) |
| categoryId | Long | 否 | 分类ID |
| publisherId | Long | 否 | 出版社ID |
| language | String | 否 | 语种 |
| binding | String | 否 | 装帧 |
| yearStart | Int | 否 | 出版年份起始 |
| yearEnd | Int | 否 | 出版年份结束 |
| status | Int | 否 | 图书状态 |
| availableOnly | Boolean | 否 | 仅显示有可借副本 |
| sort | String | 否 | 排序(borrow_count,desc 或 publish_date,desc) |

**响应数据：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "isbn": "9787536692930",
        "title": "三体",
        "author": "刘慈欣",
        "publisherName": "重庆出版社",
        "categoryName": "文学",
        "coverUrl": "/uploads/covers/1.jpg",
        "publishDate": "2008-01-01",
        "price": 23.00,
        "totalCopies": 5,
        "availableCopies": 2,
        "borrowCount": 128,
        "rating": 4.8,
        "summary": "文化大革命如火如荼进行的同时..."
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

### 3.2 获取图书详情

```
GET /books/{id}
```

**权限：** 已认证

**响应数据：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "isbn": "9787536692930",
    "title": "三体",
    "subTitle": "地球往事三部曲之一",
    "author": "刘慈欣",
    "translator": null,
    "publisherId": 1,
    "publisherName": "重庆出版社",
    "categoryId": 9,
    "categoryName": "文学",
    "categoryPath": "文学 > 中国文学 > 小说",
    "publishDate": "2008-01-01",
    "pages": 302,
    "price": 23.00,
    "binding": "平装",
    "language": "中文",
    "summary": "文化大革命如火如荼进行的同时...",
    "coverUrl": "/uploads/covers/1.jpg",
    "tableOfContents": "第一章 科学边界...",
    "totalCopies": 5,
    "availableCopies": 2,
    "borrowCount": 128,
    "rating": 4.8,
    "ratingCount": 1024,
    "status": 1,
    "createTime": "2026-01-01 10:00:00"
  }
}
```

### 3.3 获取图书副本列表

```
GET /books/{id}/copies
```

**权限：** 已认证

**响应数据：**

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "barcode": "9787536692930-001",
      "locationName": "一楼社科书库",
      "status": "in",
      "statusLabel": "在馆",
      "price": 23.00,
      "purchaseDate": "2026-01-15",
      "damageLevel": 0
    },
    {
      "id": 2,
      "barcode": "9787536692930-002",
      "locationName": "三楼科技书库",
      "status": "borrowed",
      "statusLabel": "已借出",
      "dueDate": "2026-08-10"
    }
  ]
}
```

副本状态映射：

| status | 标签 | 说明 |
|---|---|---|
| in | 在馆 | 可借阅 |
| borrowed | 已借出 | 已被读者借出 |
| reserved | 预约中 | 已被预约,等待取书 |
| maintenance | 维修中 | 正在维修 |
| lost | 丢失 | 已标记丢失 |
| discarded | 注销 | 已从馆藏移除 |

### 3.4 ISBN 查重

```
GET /books/check-isbn?isbn=9787536692930
```

**权限：** ROLE_CATALOGER, ROLE_ADMIN

**响应数据：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "exists": true,
    "bookId": 1,
    "title": "三体",
    "totalCopies": 5
  }
}
```

### 3.5 新增图书

```
POST /books
```

**权限：** ROLE_CATALOGER, ROLE_ADMIN

**请求体：**

```json
{
  "isbn": "9787544291163",
  "title": "百年孤独",
  "subTitle": null,
  "author": "加西亚·马尔克斯",
  "translator": "范晔",
  "publisherId": 1,
  "categoryId": 9,
  "publishDate": "2011-06-01",
  "pages": 360,
  "price": 39.50,
  "binding": "精装",
  "language": "中文",
  "summary": "《百年孤独》是魔幻现实主义文学的代表作...",
  "copies": [
    {
      "barcode": "9787544291163-001",
      "locationId": 2,
      "price": 39.50,
      "purchaseDate": "2026-07-01",
      "source": "采购"
    },
    {
      "barcode": "9787544291163-002",
      "locationId": 2,
      "price": 39.50,
      "purchaseDate": "2026-07-01",
      "source": "采购"
    }
  ]
}
```

**响应数据：** 返回新创建的图书 ID

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "bookId": 2,
    "totalCopies": 2
  }
}
```

### 3.6 编辑图书

```
PUT /books/{id}
```

**权限：** ROLE_CATALOGER, ROLE_ADMIN

**请求体：**（与新增类似，不含 copies 字段）

**注意：** 编辑 ISBN 不被允许（ISBN 作为唯一标识不可变更）

### 3.7 上下架图书

```
PUT /books/{id}/status
```

**权限：** ROLE_CATALOGER, ROLE_ADMIN

**请求体：**

```json
{
  "status": 0
}
```

| 值 | 含义 |
|---|---|
| 0 | 下架(读者端不可见) |
| 1 | 上架 |
| 2 | 待审核 |

### 3.8 删除图书

```
DELETE /books/{id}
```

**权限：** ROLE_ADMIN

**说明：** 逻辑删除，已删除图书不可恢复。如有未还副本，不允许删除。

### 3.9 上传封面

```
POST /books/{id}/cover
```

**权限：** ROLE_CATALOGER, ROLE_ADMIN

**请求格式：** `multipart/form-data`

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| file | File | 是 | 封面图片(JPG/PNG, 最大5MB) |

**响应数据：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "coverUrl": "/uploads/covers/2026/07/abc123.jpg"
  }
}
```

### 3.10 批量导入图书

```
POST /books/import
```

**权限：** ROLE_CATALOGER, ROLE_ADMIN

**请求格式：** `multipart/form-data`

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| file | File | 是 | Excel 文件(.xlsx, 最大50MB) |
| updateIfExists | Boolean | 否 | 已存在时是否更新(true/false,默认false) |

**Excel 模板字段：** ISBN(必填), 书名(必填), 作者(必填), 出版社, 分类, 出版日期, 定价, 摘要, 副本条码号, 馆藏地点编码

**响应数据：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 500,
    "success": 498,
    "fail": 2,
    "failDetails": [
      {"row": 3, "reason": "ISBN 格式不正确"},
      {"row": 25, "reason": "书名不能为空"}
    ]
  }
}
```

### 3.11 批量导出图书

```
GET /books/export
```

**权限：** ROLE_CATALOGER, ROLE_ADMIN

**请求参数：** 同搜索接口的筛选参数

**响应：** 返回 Excel 文件二进制流（`application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`）

### 3.12 获取热门图书

```
GET /books/hot?limit=20
```

**权限：** 已认证

**响应数据：**

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "title": "三体",
      "author": "刘慈欣",
      "coverUrl": "/uploads/covers/1.jpg",
      "borrowCount": 128,
      "availableCopies": 2,
      "rating": 4.8
    }
  ]
}
```

### 3.13 获取新上架图书

```
GET /books/new-arrivals?days=30&limit=10
```

**权限：** 无需认证（首页展示）

---

## 4. 分类/出版社/馆藏地点

### 4.1 分类管理

#### 4.1.1 获取分类树

```
GET /categories/tree
```

**权限：** 已认证

**响应数据：**

```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "name": "文学",
      "code": "I",
      "level": 1,
      "children": [
        {
          "id": 10,
          "name": "中国文学",
          "code": "I2",
          "level": 2,
          "children": [
            {
              "id": 101,
              "name": "小说",
              "code": "I24",
              "level": 3,
              "children": []
            }
          ]
        }
      ]
    }
  ]
}
```

#### 4.1.2 获取分类列表（平铺）

```
GET /categories?page=1&size=20
```

**权限：** ROLE_CATALOGER, ROLE_ADMIN

#### 4.1.3 新增分类

```
POST /categories
```

**权限：** ROLE_CATALOGER, ROLE_ADMIN

```json
{
  "name": "小说",
  "code": "I24",
  "parentId": 10,
  "sort": 1
}
```

#### 4.1.4 编辑/删除分类

```
PUT    /categories/{id}
DELETE /categories/{id}
```

**权限：** ROLE_CATALOGER, ROLE_ADMIN

**说明：** 删除分类时，如有子分类或关联图书则不允许删除。

### 4.2 出版社管理

```
GET     /publishers           # 获取所有出版社(下拉框用)
GET     /publishers/page      # 分页查询(管理后台)
POST    /publishers           # 新增
PUT     /publishers/{id}      # 编辑
DELETE  /publishers/{id}      # 删除
```

**权限：** ROLE_CATALOGER, ROLE_ADMIN

**出版社请求体示例：**

```json
{
  "name": "人民邮电出版社",
  "shortName": "人邮社",
  "phone": "010-67110000",
  "address": "北京市丰台区成寿寺路11号"
}
```

### 4.3 馆藏地点管理

```
GET     /locations            # 获取所有馆藏地点
GET     /locations/page       # 分页查询
POST    /locations            # 新增
PUT     /locations/{id}       # 编辑
DELETE  /locations/{id}       # 删除
```

**权限：** ROLE_CATALOGER, ROLE_ADMIN

---

## 5. 借阅模块

### 5.1 借书

```
POST /borrow
```

**权限：** ROLE_LIBRARIAN, ROLE_ADMIN

**请求体：**

```json
{
  "readerNo": "RD20260001",
  "barcodes": ["9787536692930-001", "9787536692930-002"],
  "remark": ""
}
```

**业务校验逻辑（按顺序）：**
1. 读者证号是否存在且卡状态正常
2. 读者是否逾期未还图书（有则拦截）
3. 读者当前借阅数是否已达上限
4. 每本图书是否为「在馆」状态
5. 是否有其他读者预约（如有，放弃后可借）
6. 重复借阅规则检测

**响应数据：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "successCount": 2,
    "failCount": 0,
    "records": [
      {
        "barcode": "9787536692930-001",
        "success": true,
        "title": "三体",
        "dueDate": "2026-08-18"
      },
      {
        "barcode": "9787536692930-002",
        "success": true,
        "title": "三体",
        "dueDate": "2026-08-18"
      }
    ],
    "failDetails": []
  }
}
```

### 5.2 还书

```
PUT /borrow/return
```

**权限：** ROLE_LIBRARIAN, ROLE_ADMIN

**请求体：**

```json
{
  "barcodes": ["9787536692930-001"],
  "damageInfo": null
}
```

**响应数据：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "barcode": "9787536692930-001",
        "title": "三体",
        "borrowDate": "2026-07-01 10:00:00",
        "dueDate": "2026-07-31",
        "returnDate": "2026-07-19 14:30:00",
        "overdueDays": 0,
        "fineAmount": 0.00
      }
    ]
  }
}
```

**逾期处理：** 如有逾期，系统自动生成罚款记录并在响应中返回。

### 5.3 续借

```
POST /borrow/{id}/renew
```

**权限：** 读者本人 或 ROLE_LIBRARIAN, ROLE_ADMIN

**请求体：**

```json
{
  "readerNo": "RD20260001"
}
```

**校验逻辑：**
- 距离应还日期 ≤ 7 天
- 图书未逾期
- 续借次数未达上限
- 该图书无其他读者预约

**响应数据：**

```json
{
  "code": 200,
  "data": {
    "oldDueDate": "2026-07-31",
    "newDueDate": "2026-08-15",
    "renewCount": 1
  }
}
```

### 5.4 读者当前借阅

```
GET /borrow/current?readerNo=RD20260001
```

**权限：** 读者本人（可查看自己的） / ROLE_LIBRARIAN / ROLE_ADMIN

**响应数据：**

```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 1,
        "bookTitle": "三体",
        "bookAuthor": "刘慈欣",
        "coverUrl": "/uploads/covers/1.jpg",
        "barcode": "9787536692930-001",
        "borrowDate": "2026-07-01",
        "dueDate": "2026-07-31",
        "renewCount": 0,
        "remainingDays": 12,
        "overdue": false,
        "overdueDays": 0,
        "canRenew": true
      }
    ],
    "total": 1,
    "current": 1,
    "size": 10
  }
}
```

### 5.5 读者借阅历史

```
GET /borrow/history?readerNo=RD20260001&page=1&size=10
```

**权限：** 读者本人 / ROLE_LIBRARIAN / ROLE_ADMIN

**请求参数：**

| 参数 | 类型 | 说明 |
|---|---|---|
| readerNo | String | 读者证号 |
| startDate | String | 起始日期 |
| endDate | String | 结束日期 |
| status | String | 借阅状态 |

### 5.6 逾期列表

```
GET /borrow/overdue?page=1&size=10
```

**权限：** ROLE_LIBRARIAN, ROLE_ADMIN

### 5.7 获取借阅记录详情

```
GET /borrow/record/{id}
```

**权限：** ROLE_LIBRARIAN, ROLE_ADMIN

---

## 6. 预约模块

### 6.1 创建预约

```
POST /reservations
```

**权限：** ROLE_READER（读者本人）

**请求体：**

```json
{
  "bookInfoId": 1,
  "pickLocationId": 1
}
```

**校验逻辑：**
- 图书所有副本均已借出
- 读者当前无同本书的在途预约
- 读者未借阅该书的副本
- 读者卡状态正常

**响应数据：**

```json
{
  "code": 200,
  "data": {
    "id": 1,
    "bookTitle": "三体",
    "reserveDate": "2026-07-19 10:00:00",
    "expectedWaitTime": "预计等待3-7天",
    "queuePosition": 1
  }
}
```

### 6.2 取消预约

```
DELETE /reservations/{id}
```

**权限：** 读者本人 / ROLE_LIBRARIAN, ROLE_ADMIN

**说明：** 仅「等待中」状态的预约可取消；「待取书」状态下是否可取消可配置。

### 6.3 当前预约列表

```
GET /reservations/current?readerNo=RD20260001
```

**权限：** 读者本人 / ROLE_LIBRARIAN, ROLE_ADMIN

**响应数据：**

```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 1,
        "bookTitle": "三体",
        "bookAuthor": "刘慈欣",
        "coverUrl": "/uploads/covers/1.jpg",
        "reserveDate": "2026-07-15 10:00:00",
        "status": "waiting",
        "statusLabel": "等待中",
        "queuePosition": 2,
        "pickLocationName": "一楼社科书库"
      },
      {
        "id": 2,
        "bookTitle": "百年孤独",
        "reserveDate": "2026-07-18 14:00:00",
        "status": "ready",
        "statusLabel": "待取书",
        "expireDate": "2026-07-20 14:00:00",
        "pickLocationName": "二楼文学书库"
      }
    ]
  }
}
```

预约状态说明：

| status | 标签 | 说明 |
|---|---|---|
| waiting | 等待中 | 排队等待，无可用副本 |
| ready | 待取书 | 已有归还副本，等待读者来取 |
| fulfilled | 已完成 | 已取书，完成借阅 |
| cancelled | 已取消 | 读者主动取消 |
| expired | 已过期 | 超时未取，自动过期 |

### 6.4 取书确认

```
POST /reservations/{id}/pickup
```

**权限：** ROLE_LIBRARIAN, ROLE_ADMIN

**说明：** 读者到馆后，管理员执行此操作。系统自动将预约状态改为「已完成」，同时创建一条借阅记录。

---

## 7. 罚款模块

### 7.1 罚款列表

```
GET /fines?readerNo=RD20260001&paid=0&page=1&size=10
```

**权限：** ROLE_LIBRARIAN, ROLE_ADMIN（读者角色只能查看自己的）

**筛选参数：**

| 参数 | 类型 | 说明 |
|---|---|---|
| readerNo | String | 读者证号 |
| paid | Int | 0未缴/1已缴 |
| fineType | String | overdue/damage/lost |

**响应数据：**

```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 1,
        "readerNo": "RD20260001",
        "readerName": "王同学",
        "bookTitle": "三体",
        "fineType": "overdue",
        "fineTypeLabel": "逾期罚款",
        "amount": 5.00,
        "overdueDays": 10,
        "paid": false,
        "createTime": "2026-07-19 14:30:00"
      }
    ],
    "total": 1
  }
}
```

### 7.2 缴纳罚款

```
POST /fines/{id}/pay
```

**权限：** ROLE_LIBRARIAN, ROLE_ADMIN

**请求体：**

```json
{
  "remark": "现金缴纳"
}
```

### 7.3 豁免罚款

```
PUT /fines/{id}/waive
```

**权限：** ROLE_LIBRARIAN, ROLE_ADMIN

**请求体：**

```json
{
  "waiveReason": "教师免逾期费"
}
```

### 7.4 批量缴纳/豁免

```
POST /fines/batch-pay
{
  "ids": [1, 2, 3]
}

PUT /fines/batch-waive
{
  "ids": [4, 5],
  "waiveReason": "系统维护期间逾期"
}
```

---

## 8. 读者管理模块

### 8.1 分页查询读者

```
GET /readers?page=1&size=10
```

**权限：** ROLE_LIBRARIAN, ROLE_CATALOGER, ROLE_ADMIN

**筛选参数：**

| 参数 | 类型 | 说明 |
|---|---|---|
| readerNo | String | 读者证号 |
| realName | String | 姓名(模糊) |
| readerTypeId | Long | 读者类型ID |
| cardStatus | Int | 卡状态 |
| phone | String | 手机号 |

**响应数据：**

```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 1,
        "userId": 4,
        "readerNo": "RD20260001",
        "realName": "王同学",
        "readerTypeName": "学生",
        "cardStatus": 1,
        "cardStatusLabel": "正常",
        "currentBorrowed": 2,
        "maxBorrow": 5,
        "totalBorrowed": 15,
        "totalFines": 5.00,
        "email": "wang@example.com",
        "phone": "13800138001",
        "registerDate": "2026-03-01",
        "expireDate": "2028-03-01"
      }
    ],
    "total": 50,
    "size": 10,
    "current": 1,
    "pages": 5
  }
}
```

### 8.2 新增读者

```
POST /readers
```

**权限：** ROLE_LIBRARIAN, ROLE_ADMIN

**请求体：**

```json
{
  "realName": "赵六",
  "readerTypeId": 1,
  "email": "zhao@example.com",
  "phone": "13900139000",
  "remark": ""
}
```

**说明：** 系统自动创建 sys_user 和 reader 记录，自动生成读者证号和初始密码。

**响应数据：**

```json
{
  "code": 200,
  "data": {
    "readerId": 5,
    "readerNo": "RD20260002",
    "username": "zhao6",
    "initialPassword": "123456"
  }
}
```

### 8.3 编辑读者

```
PUT /readers/{id}
```

### 8.4 挂失/解挂读者证

```
PUT /readers/{id}/card
{
  "action": "lost"  // lost=挂失, restore=解挂
}
```

### 8.5 冻结/解冻读者

```
PUT /readers/{id}/freeze
{
  "action": "freeze",  // freeze=冻结, unfreeze=解冻
  "reason": "违规借阅"
}
```

### 8.6 重置读者密码

```
PUT /readers/{id}/reset-pwd
```

### 8.7 获取读者借阅概况

```
GET /readers/{id}/summary
```

**权限：** ROLE_LIBRARIAN, ROLE_ADMIN

**响应数据：**

```json
{
  "code": 200,
  "data": {
    "currentBorrowed": 2,
    "overdueCount": 1,
    "pendingReservationCount": 1,
    "unpaidFineCount": 1,
    "unpaidFineAmount": 5.00,
    "totalBorrowed": 15,
    "totalFines": 5.00
  }
}
```

### 8.8 读者类型配置管理

```
GET     /reader-types              # 获取所有读者类型
POST    /reader-types              # 新增
PUT     /reader-types/{id}          # 编辑
DELETE  /reader-types/{id}          # 删除
```

**权限：** ROLE_ADMIN

**读者类型请求体示例：**

```json
{
  "name": "研究生",
  "code": "GRADUATE",
  "maxBorrow": 8,
  "borrowDays": 45,
  "renewCount": 2,
  "renewDays": 20,
  "overdueFineRate": 0.30,
  "reservationKeepHours": 48,
  "sort": 5
}
```

---

## 9. 统计分析模块

### 9.1 借阅统计

```
GET /statistics/borrow?startDate=2026-01-01&endDate=2026-07-19&type=month
```

**权限：** ROLE_LIBRARIAN, ROLE_CATALOGER, ROLE_ADMIN

**请求参数：**

| 参数 | 类型 | 说明 |
|---|---|---|
| startDate | String | 起始日期 |
| endDate | String | 结束日期 |
| type | String | 聚合粒度(day/week/month) |
| categoryId | Long | 分类ID(可选,按分类筛选) |
| readerTypeId | Long | 读者类型ID(可选) |

**响应数据：**

```json
{
  "code": 200,
  "data": {
    "chartData": [
      {"date": "2026-01", "borrowCount": 156, "returnCount": 98},
      {"date": "2026-02", "borrowCount": 203, "returnCount": 145},
      {"date": "2026-03", "borrowCount": 312, "returnCount": 287}
    ],
    "summary": {
      "totalBorrow": 3000,
      "totalReturn": 2800,
      "avgDailyBorrow": 15.8,
      "peakDay": "2026-03-15"
    }
  }
}
```

### 9.2 热门图书排行

```
GET /statistics/hot-books?type=month&limit=20
```

**权限：** 已认证

| 参数 | 类型 | 说明 |
|---|---|---|
| type | String | day/week/month/year/all |
| limit | Int | 取前N条(默认20) |

### 9.3 流通统计

```
GET /statistics/circulation
```

**权限：** ROLE_LIBRARIAN, ROLE_ADMIN

**响应数据：**

```json
{
  "code": 200,
  "data": {
    "today": {
      "borrowCount": 23,
      "returnCount": 18,
      "fineCount": 5
    },
    "overall": {
      "totalBorrow": 3000,
      "overdueRate": 3.5,
      "reservationFulfillRate": 82.5
    }
  }
}
```

### 9.4 馆藏统计

```
GET /statistics/collection
```

**权限：** ROLE_CATALOGER, ROLE_ADMIN

**响应数据：**

```json
{
  "code": 200,
  "data": {
    "totalBooks": 15000,
    "totalCopies": 35000,
    "categoryDistribution": [
      {"name": "文学", "count": 3200, "percentage": 21.3},
      {"name": "工业技术", "count": 2800, "percentage": 18.7}
    ],
    "locationDistribution": [
      {"name": "一楼社科书库", "count": 12000},
      {"name": "三楼科技书库", "count": 15000}
    ],
    "monthlyNewArrivals": [
      {"month": "2026-01", "count": 120},
      {"month": "2026-02", "count": 95}
    ]
  }
}
```

### 9.5 读者统计

```
GET /statistics/readers
```

**权限：** ROLE_ADMIN

**响应数据：**

```json
{
  "code": 200,
  "data": {
    "totalReaders": 2000,
    "activeReaders": 650,
    "activeRate": 32.5,
    "typeDistribution": [
      {"name": "学生", "count": 1500},
      {"name": "教师", "count": 300}
    ]
  }
}
```

---

## 10. 系统管理模块

### 10.1 用户管理

```
GET     /system/users?page=1&size=10&keyword=admin
POST    /system/users
PUT     /system/users/{id}
PUT     /system/users/{id}/status    # 启用/禁用
DELETE  /system/users/{id}
```

**权限：** ROLE_ADMIN

**新增用户请求体：**

```json
{
  "username": "zhangsan",
  "realName": "张三",
  "email": "zs@library.com",
  "phone": "13800138002",
  "roleIds": [2, 3],
  "status": 1
}
```

**说明：** 创建用户时，系统自动生成随机密码。编辑时如密码字段为空则保持不变。

### 10.2 角色管理

```
GET     /system/roles?page=1&size=10
GET     /system/roles/all              # 获取所有角色(下拉框用)
POST    /system/roles
PUT     /system/roles/{id}
DELETE  /system/roles/{id}
```

**权限：** ROLE_ADMIN

**新增角色请求体：**

```json
{
  "name": "阅览室管理员",
  "code": "ROLE_READING_ROOM",
  "description": "仅管理阅览室相关操作",
  "menuIds": [10, 11, 20, 21, 22],
  "sort": 5
}
```

### 10.3 菜单管理

```
GET     /system/menus/tree            # 获取菜单树(角色管理用)
GET     /system/menus                 # 扁平列表(管理后台)
POST    /system/menus
PUT     /system/menus/{id}
DELETE  /system/menus/{id}
```

**权限：** ROLE_ADMIN

**新增菜单请求体：**

```json
{
  "name": "统计分析",
  "type": 0,
  "path": "/admin/statistics",
  "component": "Layout",
  "icon": "DataAnalysis",
  "parentId": 0,
  "sort": 5,
  "visible": true
}
```

### 10.4 系统参数管理

```
GET     /system/config               # 获取所有参数(分页)
GET     /system/config/{key}         # 按key查询
PUT     /system/config/{id}          # 编辑参数
GET     /system/config/public        # 获取公开参数(读者端读取)
```

**权限：** 编辑需要 ROLE_ADMIN；查询仅 ROLE_ADMIN；public 接口无需认证

**编辑请求体：**

```json
{
  "configValue": "10",
  "remark": "最大借阅数(已更新)"
}
```

### 10.5 操作日志

```
GET /system/logs?page=1&size=20
```

**权限：** ROLE_ADMIN

**筛选参数：**

| 参数 | 类型 | 说明 |
|---|---|---|
| username | String | 操作用户名 |
| module | String | 模块(book/borrow/reader/system/auth) |
| operation | String | 操作描述(模糊) |
| startDate | String | 起始时间 |
| endDate | String | 结束时间 |
| result | String | success/fail |

**响应数据：**

```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 1001,
        "username": "admin",
        "realName": "系统管理员",
        "operation": "新增图书《百年孤独》",
        "module": "book",
        "moduleLabel": "图书管理",
        "requestIp": "192.168.1.100",
        "requestMethod": "POST",
        "requestUrl": "/api/books",
        "result": "success",
        "duration": 45,
        "createTime": "2026-07-19 10:00:00"
      }
    ]
  }
}
```

### 10.6 数据字典管理

```
GET     /system/dicts                    # 分页查询字典
GET     /system/dicts/{code}             # 按编码查询(含字典项)
GET     /system/dicts/{code}/items       # 查询字典项列表
POST    /system/dicts                    # 新增字典
PUT     /system/dicts/{id}               # 编辑字典
DELETE  /system/dicts/{id}               # 删除字典
POST    /system/dicts/items              # 新增字典项
PUT     /system/dicts/items/{id}         # 编辑字典项
DELETE  /system/dicts/items/{id}         # 删除字典项
```

**权限：** ROLE_ADMIN

**获取字典项（前端常用）：**

```
GET /system/dicts/{code}/items
```

**响应数据：**

```json
{
  "code": 200,
  "data": [
    {"label": "平装", "value": "平装", "defaultFlag": true},
    {"label": "精装", "value": "精装", "defaultFlag": false}
  ]
}
```

### 10.7 数据备份管理

```
GET     /system/backup                  # 备份列表
POST    /system/backup                  # 创建备份
GET     /system/backup/{id}/download    # 下载备份文件
POST    /system/backup/{id}/restore     # 恢复备份
DELETE  /system/backup/{id}             # 删除备份文件
```

**权限：** ROLE_ADMIN

**创建备份响应：**

```json
{
  "code": 200,
  "data": {
    "id": 1,
    "fileName": "library_backup_20260719_100000.sql",
    "fileSize": 5242880,
    "fileSizeDisplay": "5.00 MB",
    "status": 1,
    "createTime": "2026-07-19 10:00:00"
  }
}
```

### 10.8 公告管理

```
GET     /system/announcements            # 分页查询(管理端)
GET     /announcements                   # 已发布的公告(公共接口)
POST    /system/announcements            # 新增
PUT     /system/announcements/{id}       # 编辑
DELETE  /system/announcements/{id}       # 删除
PUT     /system/announcements/{id}/publish  # 发布
PUT     /system/announcements/{id}/draft    # 撤回到草稿
```

**权限：** ROLE_LIBRARIAN, ROLE_CATALOGER, ROLE_ADMIN

**请求体：**

```json
{
  "title": "暑假闭馆通知",
  "content": "图书馆将于7月20日至8月25日闭馆...",
  "type": "urgent",
  "targetRoles": "all",
  "topFlag": true
}
```

---

## 11. 通知公告模块

### 11.1 我的通知列表

```
GET /notifications?page=1&size=10&readFlag=0
```

**权限：** ROLE_READER（读者本人）

**响应数据：**

```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 1,
        "title": "逾期提醒",
        "content": "您借阅的《三体》已逾期2天，请尽快归还",
        "type": "overdue_due",
        "typeLabel": "逾期提醒",
        "readFlag": false,
        "createTime": "2026-07-19 09:00:00"
      }
    ],
    "total": 5,
    "unreadCount": 3
  }
}
```

### 11.2 标记已读

```
PUT /notifications/{id}/read
PUT /notifications/read-all          # 全部标记已读
```

### 11.3 删除通知

```
DELETE /notifications/{id}
DELETE /notifications                # 批量删除(请求体传ids数组)
```

### 11.4 获取未读通知数

```
GET /notifications/unread-count
```

**响应：** `{ "unreadCount": 3 }`

### 11.5 获取公开公告列表

```
GET /announcements?page=1&size=5
```

**权限：** 无需认证（首页展示）

---

## 12. 收藏模块

### 12.1 收藏列表

```
GET /favorites?page=1&size=20
```

**权限：** ROLE_READER（读者本人）

### 12.2 添加收藏

```
POST /favorites
{
  "bookInfoId": 1
}
```

### 12.3 取消收藏

```
DELETE /favorites/{id}
DELETE /favorites?bookInfoId=1    # 按图书ID取消
```

### 12.4 检查是否已收藏

```
GET /favorites/check?bookInfoId=1
```

**响应：** `{ "favorited": true }`

---

## 13. 推荐图书模块

### 13.1 获取推荐列表

```
GET /recommendations?limit=10
```

**权限：** 无需认证

**响应数据：**

```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "bookId": 1,
      "title": "三体",
      "author": "刘慈欣",
      "coverUrl": "/uploads/covers/1.jpg",
      "remark": "本学期推荐阅读",
      "sort": 1,
      "borrowCount": 128
    }
  ]
}
```

### 13.2 管理推荐（增删改）

```
POST   /recommendations   { "bookInfoId": 1, "remark": "推荐语", "sort": 1 }
PUT    /recommendations/{id}
DELETE /recommendations/{id}
```

**权限：** ROLE_CATALOGER, ROLE_ADMIN

---

## 14. 公共接口

### 14.1 获取数据字典项

```
GET /common/dict/{dictCode}
```

**权限：** 已认证

**响应数据：**

```json
{
  "code": 200,
  "data": [
    {"label": "在馆", "value": "in"},
    {"label": "已借出", "value": "borrowed"}
  ]
}
```

### 14.2 获取分类树（公共）

```
GET /common/categories
```

### 14.3 文件上传

```
POST /common/upload
```

**请求格式：** `multipart/form-data`

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| file | File | 是 | 上传文件 |
| type | String | 否 | 文件类型(cover/image/file,默认file) |

**响应数据：**

```json
{
  "code": 200,
  "data": {
    "url": "/uploads/2026/07/abc.jpg",
    "fileName": "abc.jpg",
    "fileSize": 102400
  }
}
```

### 14.4 获取系统公开配置

```
GET /common/public-config
```

**权限：** 无需认证

**说明：** 返回系统名称、Logo 等公开信息。
