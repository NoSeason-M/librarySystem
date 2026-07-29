# 读者设置页 — 组件规格

> 对应 Pencil 设计: `Reader Settings` (id: `EZ3S7`)  
> 路由: `/reader/settings`  
> 页面尺寸: 1440×1024

---

## 1. 布局结构

```
┌─ Nav Bar (1440×68) ─────────────────────────┐
│ 📚 LibraryOS │ 首页 浏览 我的 个人中心(高亮) │ 👤 王同学 │
├──────────────────────────────────────────────┤
│ 个人设置 (标题)                                │
│ 管理你的个人信息和偏好设置 (副标题)              │
├──────────────────────┬───────────────────────┤
│                      │                       │
│  ┌─ 个人信息 ────┐   │  ┌─ 通知偏好 ──────┐  │
│  │ 👤 王同学     │   │  │ 选择你希望接收… │  │
│  │    RD20260001 │   │  │ 📢 逾期提醒   ○─ │  │
│  │               │   │  │ ⏰ 即将到期   ○─ │  │
│  │ 姓名 [王同学]  │   │  │ 📬 预约到书   ○─ │  │
│  │ 邮箱 [wang@]  │   │  │ 📋 系统公告   ○─ │  │
│  │ 电话 [138...] │   │  └──────────────────┘  │
│  │ 读者类型 [学生]│   │                       │
│  │     (只读)     │   │  ┌─ 快捷操作 ──────┐  │
│  │     [保存修改]  │   │  │ 📖 查看借阅记录 │  │
│  └────────────────┘   │  │ ⭐ 我的收藏    │  │
│  ┌─ 修改密码 ────┐   │  │ 📌 我的预约    │  │
│  │ 当前密码 [••] │   │  │ 💬 联系客服    │  │
│  │ 新密码 [••]   │   │  └──────────────────┘  │
│  │ 确认新密码 [••]│   │                       │
│  │     [修改密码]  │   │                       │
│  └────────────────┘   │                       │
└──────────────────────┴───────────────────────┘
```

## 2. 导航栏 (Nav Bar)

| 属性 | 值 |
|------|-----|
| 高度 | 68px |
| 背景 | `$bg-primary` (#FFFFFF) |
| 内边距 | 水平 40px, 垂直 16px |
| 子元素间距 | 32px |

### 2.1 Logo
| 属性 | 值 |
|------|-----|
| 内容 | 📚 LibraryOS |
| 字体 | Inter, 20px, Bold 700 |
| 颜色 | `$text-primary` (#1A1A1A) |

### 2.2 导航链接
| 属性 | 值 |
|------|-----|
| 间距 | 24px |
| 对齐 | 垂直居中 |

| 链接 | 文字 | 颜色 | 字重 |
|------|------|------|------|
| 首页 | 首页 | `$text-secondary` | normal |
| 浏览 | 浏览 | `$text-secondary` | normal |
| 我的 | 我的 | `$text-secondary` | normal |
| 个人中心 | 个人中心 | `$accent` | 600 |

字体统一: Inter, 14px

### 2.3 用户信息
| 属性 | 值 |
|------|-----|
| 间距 | 10px |
| 对齐 | 垂直居中 |

- **用户名**: Inter 13px Medium 500, `$text-secondary`
- **头像**: 36×36px, 圆形裁切 (`cornerRadius: 999`), `$accent-light` 背景, 首字居中, `$accent` 色, Inter 14px Semibold 600

## 3. 主内容区 (Main)

| 属性 | 值 |
|------|-----|
| 内边距 | 上 32px, 左/右 80px |
| 布局 | 垂直 |
| 间距 | 28px |

### 3.1 页面标题区

| 元素 | 内容 | 字体 | 颜色 |
|------|------|------|------|
| 标题 | 个人设置 | Inter 24px Bold 700 | `$text-primary` |
| 副标题 | 管理你的个人信息和偏好设置 | Inter 14px | `$text-secondary` |

间距: 4px (垂直)

### 3.2 两栏内容区

| 属性 | 值 |
|------|-----|
| 布局 | 水平 |
| 间距 | 24px |
| 左栏宽度 | `fill_container` (自适应) |
| 右栏宽度 | 420px (固定) |

---

## 4. 左栏 — 个人信息卡片

| 属性 | 值 |
|------|-----|
| 背景 | `$bg-primary` (#FFFFFF) |
| 圆角 | `$card-radius` (16px) |
| 边框 | 1px solid `$border` |
| 内边距 | 24px |
| 内部间距 | 20px |

### 4.1 标题
- "个人信息" — Inter 18px Semibold 600, `$text-primary`

### 4.2 头像行
- **大号头像**: 72×72px, 圆形, `$accent-light` 背景, 首字 "王" 居中, Inter 28px Semibold 600, `$accent`
- **姓名**: "王同学" — Inter 16px Semibold 600, `$text-primary`
- **读者证号**: "RD20260001" — Inter 12px, `$text-muted`

### 4.3 表单行 (两列, 间距 16px)

每个字段包含:
1. **标签**: Inter 13px Medium 500, `$text-primary`
2. **输入框**: 填充 `$bg-secondary`, 圆角 `$input-radius` (12px), 边框 1.5px solid `$border`, 内边距 10px 14px
3. **值文字**: Inter 13px, `$text-primary`

| 标签 | 值 |
|------|-----|
| 姓名 | 王同学 |
| 邮箱 | wang@example.com |
| 电话 | 13800138001 |
| 读者类型 | 学生（不可更改）— `$text-muted` 色, 只读 |

### 4.4 保存按钮
- 对齐: 右对齐
- 按钮: Inter 14px Semibold 600, `$text-inverse` (#FFF), `$accent` 背景, `$button-radius` (10px), 内边距 10px 24px

---

## 5. 左栏 — 修改密码卡片

| 属性 | 值 |
|------|-----|
| 背景 | `$bg-primary` (#FFFFFF) |
| 圆角 | `$card-radius` (16px) |
| 边框 | 1px solid `$border` |
| 内边距 | 24px |
| 内部间距 | 16px |

### 5.1 标题
- "修改密码" — Inter 18px Semibold 600, `$text-primary`

### 5.2 表单行 (两列, 间距 16px)
| 标签 | 占位展示 |
|------|---------|
| 当前密码 | •••••••• |
| 新密码 | •••••••• |
| 确认新密码 | •••••••• |

输入框样式同个人信息卡片。

### 5.3 修改密码按钮
- 对齐: 右对齐
- 按钮: Inter 14px Medium 500, `$text-secondary`, `$bg-primary` 背景, `$button-radius`, 1px solid `$border`, 内边距 10px 24px

---

## 6. 右栏 — 通知偏好卡片

| 属性 | 值 |
|------|-----|
| 宽度 | 420px |
| 背景 | `$bg-primary` (#FFFFFF) |
| 圆角 | `$card-radius` (16px) |
| 边框 | 1px solid `$border` |
| 内边距 | 24px |
| 内部间距 | 16px |

### 6.1 标题
- "通知偏好" — Inter 18px Semibold 600, `$text-primary`

### 6.2 描述
- "选择你希望接收的通知类型" — Inter 13px, `$text-secondary`

### 6.3 通知项列表

每项包含:
- **文字组** (flex:1, 垂直间距 2px)
  - **标签**: Inter 14px Medium 500, `$text-primary`
  - **描述**: Inter 12px, `$text-muted`
- **开关 (Toggle)**:
  - 容器: 40×22px, 圆角 11px, 内边距 3px 0
  - 开启: `$accent` 背景, 圆点靠右 (margin-start 18px)
  - 关闭: `$border` 背景, 圆点靠左 (margin-start 4px)
  - 圆点: 16×16px, #FFFFFF, 圆角 8px

| 通知项 | 描述 | 默认 |
|--------|------|------|
| 逾期提醒 | 图书逾期时发送通知 | ✅ 开启 |
| 即将到期 | 到期前 3 天提醒 | ✅ 开启 |
| 预约到书 | 预约图书已可领取 | ✅ 开启 |
| 系统公告 | 图书馆发布的重要通知 | ❌ 关闭 |

---

## 7. 右栏 — 快捷操作卡片

| 属性 | 值 |
|------|-----|
| 背景 | `$bg-primary` (#FFFFFF) |
| 圆角 | `$card-radius` (16px) |
| 边框 | 1px solid `$border` |
| 内边距 | 24px |
| 内部间距 | 12px |

### 7.1 标题
- "快捷操作" — Inter 18px Semibold 600, `$text-primary`

### 7.2 链接列表
每项: 水平排列, 间距 10px, 内边距 8px 0

| 图标 | 标签 | 路由 |
|------|------|------|
| 📖 | 查看借阅记录 | /reader/borrowing |
| ⭐ | 我的收藏 | /reader/favorites |
| 📌 | 我的预约 | /reader/reservations |
| 💬 | 联系客服 | — |

- 图标: Inter 16px, `$text-secondary`
- 标签: Inter 14px Medium 500, `$text-primary`

---

## 8. 设计 Token 引用

| Token | CSS Variable | 值 |
|-------|-------------|-----|
| bg-primary | `--bg-primary` | #FFFFFF |
| bg-secondary | `--bg-secondary` | #F7F8FA |
| bg-inverse | `--bg-inverse` | #0A0A0A |
| text-primary | `--text-primary` | #1A1A1A |
| text-secondary | `--text-secondary` | #666666 |
| text-muted | `--text-muted` | #888888 |
| text-inverse | `--text-inverse` | #FFFFFF |
| accent | `--accent` | #4A9FD8 |
| accent-light | `--accent-light` | #E8F4FD |
| success | `--success` | #34D399 |
| warning | `--warning` | #FBBF24 |
| danger | `--danger` | #F87171 |
| border | `--border` | #E5E7EB |
| card-radius | `--card-radius` | 16px |
| input-radius | `--input-radius` | 12px |
| button-radius | `--button-radius` | 10px |

字体: Inter (正文/标题), Geist Mono (数据)
