# 管理端 - 罚款管理 — 组件规格

> Pencil: `Admin Fines Management` (id: `DrioN`)  
> 菜单: 管理端侧边栏 → 罚款管理  
> API 接口: `GET /api/fines`, `POST /api/fines/{id}/pay`, `PUT /api/fines/{id}/waive`, `POST /api/fines/batch-pay`, `PUT /api/fines/batch-waive`  
> 页面尺寸: 1440×900

---

## 1. 页面结构

```
┌─ Sidebar (240px) ──────────────────┬──────────────────────────────────────────┐
│ 📚 LibraryOS                       │ 罚款管理                    2026-07-29   │
│                                     │                                          │
│ 📊 工作台                          │ ┌─── 统计卡片行 ──────────────────────┐ │
│ 📖 借还管理                        │ │ 未缴罚款  未缴总额  本月新增  已缴本月│ │
│ 📚 图书管理                        │ │  5 笔    ¥16.50   3 笔    ¥8.00   │ │
│ 👥 读者管理                        │ └────────────────────────────────────┘ │
│ 📈 统计分析                        │                                          │
│ 💰 罚款管理 (active)                │ ┌─── 工具栏 ──────────────────────┐    │
│ ⚙️ 系统设置                        │ │ [🔍 搜索读者证号..][搜索]        │    │
│                                     │ │ [罚款类型 ▼] [状态 ▼]           │    │
│ ┌─ 头像 ────┐                      │ └────────────────────────────────┘    │
│ │ 管理员     │                      │ ┌─── 批量操作 ─────────────────────┐    │
│ └───────────┘                      │ │ 批量操作：[批量缴纳][批量豁免]    │    │
└─────────────────────────────────────┼──────────────────────────────────────────┤
                                      │ ┌─── 罚款表格 ─────────────────────────┐│
                                      │ │ □ │ 证号 │姓名│书名│类型│金额│天数│时间│ 操作││
                                      │ ├───┼──────┼───┼───┼───┼───┼──┼───┼────┤│
                                      │ │ □ │RD...│王同│三体│逾期│4.5│9天│07-│[缴纳][豁免]││
                                      │ │ □ │RD...│李华│JVM│逾期│7.0│14天│...│[缴纳][豁免]││
                                      │ │ □ │RD...│王同│百年│损坏│20 │— │...│[缴纳][豁免]││
                                      │ │ □ │RD...│张三│算法│逾期│5.5│11天│...│已缴纳    ││
                                      │ └─────────────────────────────────────┘│
                                      │ 显示 4 条，共 12 条    ← 1 2 3 →       │
                                      └──────────────────────────────────────────┘
```

## 2. 侧边栏 (Sidebar)

见管理端统一布局。当前选中「罚款管理」，高亮为 `$accent` 背景 + `$text-inverse` 白色文字。

## 3. 页面标题

| 属性 | 值 |
|------|-----|
| 标题 | "罚款管理" — Inter 24px Bold 700, `$text-primary` |
| 日期 | "2026-07-29 星期二" — Inter 12px, `$text-secondary` |

## 4. 统计卡片行

| 属性 | 值 |
|------|-----|
| 布局 | 水平 |
| 间距 | 16px |

每张卡片:
| 属性 | 值 |
|------|-----|
| 背景 | `$bg-primary` |
| 圆角 | `$card-radius` (16px) |
| 边框 | 1px solid `$border` |
| 内边距 | 20px |
| 布局 | 垂直, 间距 6px, 文字居中 |

| 卡片 | 数值颜色 |
|------|---------|
| 未缴罚款 | `$danger` (#F87171) |
| 未缴总额 | `$danger` (#F87171) |
| 本月新增 | `$accent` (#4A9FD8) |
| 已缴本月 | `$success` (#34D399) |

数值: Inter 28px Bold 700

## 5. 工具栏

| 属性 | 值 |
|------|-----|
| 间距 | 12px |
| 对齐 | 垂直居中 |

### 搜索栏
| 属性 | 值 |
|------|-----|
| 背景 | `$bg-primary` (#FFF) |
| 圆角 | 999px (pill) |
| 边框 | 1.5px solid `$border` |
| 宽度 | 320px |
| 🔍 图标 | Inter 14px, `$text-muted` |
| 占位文字 | "搜索读者证号或姓名..." — Inter 13px, `$text-muted` |
| 搜索按钮 | `$accent` 背景, 圆角 999px, 内边距 10px 20px, `$text-inverse`, Inter 13px Semibold 600 |

### 筛选下拉框
| 属性 | 值 |
|------|-----|
| 背景 | `$bg-primary` (#FFF) |
| 圆角 | 10px |
| 边框 | 1.5px solid `$border` |
| 内边距 | 10px 14px |

| 筛选 | 默认值 |
|------|--------|
| 罚款类型 | 全部类型 |
| 状态 | 全部状态 |

## 6. 批量操作栏

| 属性 | 值 |
|------|-----|
| 间距 | 12px |
| 对齐 | 垂直居中 |

| 元素 | 属性 |
|------|------|
| 标签 "批量操作：" | Inter 13px Medium 500, `$text-secondary` |
| 批量缴纳 | `$accent` 背景, 圆角 8px, 内边距 8px 16px, `$text-inverse`, Inter 12px Semibold 600 |
| 批量豁免 | 透明背景, 圆角 8px, 边框 1px solid `$border`, 内边距 8px 16px, `$text-secondary`, Inter 12px Medium 500 |

## 7. 罚款表格

| 属性 | 值 |
|------|-----|
| 背景 | `$bg-primary` |
| 圆角 | `$card-radius` (16px) |
| 边框 | 1px solid `$border` |
| 内部间距 | 2px |

### 表头
| 属性 | 值 |
|------|-----|
| 背景 | `$bg-secondary` |
| 内边距 | 14px 20px |

| 列 | 宽度 |
|----|------|
| 勾选框 (□) | 32px |
| 读者证号 | 120px |
| 读者姓名 | 110px |
| 书名 | 220px |
| 罚款类型 | 100px |
| 金额 | 80px |
| 逾期天数 | 80px |
| 生成时间 | 120px |
| 操作 | 160px |

### 数据行
| 属性 | 值 |
|------|-----|
| 内边距 | 12px 20px |
| 对齐 | 垂直居中 |

| 列 | 样式 |
|----|------|
| 勾选框 | Inter 14px, `$text-muted` |
| 读者证号 | Inter 12px, `$text-secondary` |
| 读者姓名 | Inter 13px Medium 500, `$text-primary` |
| 书名 | Inter 13px Medium 500, `$text-primary` |
| 罚款类型 | Inter 12px, `$text-secondary` |
| 金额 (未缴) | Inter 13px Semibold 600, `$danger` |
| 金额 (已缴) | Inter 13px Semibold 600, `$success` |
| 逾期天数 | Inter 12px, `$text-secondary` |
| 生成时间 | Inter 11px, `$text-muted` |
| **缴纳按钮** | `$accent` 背景, 圆角 6px, 内边距 5px 10px, `$text-inverse`, Inter 11px Medium 500 |
| **豁免按钮** | 透明背景, 圆角 6px, 边框 1px solid `$border`, 内边距 5px 10px, `$text-secondary`, Inter 11px Medium 500 |
| **已缴纳标签** | Inter 12px Medium 500, `$success` |

### 状态颜色
| 状态 | 金额色 | 操作区 |
|------|--------|--------|
| 未缴 (paid=false) | `$danger` (#F87171) | 显示 [缴纳][豁免] 按钮 |
| 已缴 (paid=true) | `$success` (#34D399) | 显示 "已缴纳" 文字 |

## 8. 分页

| 属性 | 值 |
|------|-----|
| 对齐 | 两端对齐 |
| 内边距 | 8px 0 |

| 元素 | 样式 |
|------|------|
| 信息文字 "显示 4 条，共 12 条" | Inter 12px, `$text-muted` |
| 页码按钮 | 32×32px, 圆角 8px, 选中=`$accent`+`$text-inverse`, 未选中=`$bg-primary`+`$border`+`$text-secondary` |
| 上一页/下一页 | Inter 12px, `$accent` |

---

## 9. API 对接说明

| 操作 | 方法 | 路径 |
|------|------|------|
| 查询罚款列表 | GET | `/api/fines?readerNo=&paid=&fineType=&page=&size=` |
| 缴纳罚款 | POST | `/api/fines/{id}/pay` |
| 豁免罚款 | PUT | `/api/fines/{id}/waive` |
| 批量缴纳 | POST | `/api/fines/batch-pay` |
| 批量豁免 | PUT | `/api/fines/batch-waive` |

### 筛选参数

| 参数 | 类型 | 说明 |
|------|------|------|
| readerNo | String | 读者证号 |
| paid | Int | 0=未缴, 1=已缴 |
| fineType | String | overdue(逾期)/damage(损坏)/lost(丢失) |
| page | Int | 页码 |
| size | Int | 每页条数 |

---

## 10. 设计 Token 引用

同 [08-reader-layout.md](08-reader-layout.md) 的设计 Token。
