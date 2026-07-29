# 读者端布局 — 组件规格

> 适用于: 当前借阅 / 借阅历史 / 我的预约 / 我的收藏 / 我的罚款  
> 路由: `/reader/*`  
> 页面尺寸: 1440×900

---

## 1. 通用布局结构

所有 5 个读者端功能页面共享完全相同的布局骨架，区别仅在于右侧内容区。

```
┌─ Nav Bar (1440×68) ──────────────────────────────────┐
│ 📚 LibraryOS │ 首页  浏览  我的(高亮)  个人中心  │ 👤 王同学 │
├──────────────────────────┬───────────────────────────┤
│  ┌─ Sidebar (280px) ──┐ │  ┌─ Content ───────────┐  │
│  │  ┌─ Profile Card ─┐│ │  │  (每个页面不同)      │  │
│  │  │   👤 头像       ││ │  │                     │  │
│  │  │   王同学        ││ │  │                     │  │
│  │  │   RD20260001    ││ │  │                     │  │
│  │  │   [学生]        ││ │  │                     │  │
│  │  └────────────────┘│ │  │                     │  │
│  │  ┌─ Quick Links ──┐│ │  │                     │  │
│  │  │ 📖 当前借阅     ││ │  │                     │  │
│  │  │ ⏱ 借阅历史     ││ │  │                     │  │
│  │  │ 📌 我的预约     ││ │  │                     │  │
│  │  │ ⭐ 我的收藏     ││ │  │                     │  │
│  │  │ 💰 我的罚款     ││ │  │                     │  │
│  │  │ ⚙️ 设置        ││ │  │                     │  │
│  │  └────────────────┘│ │  │                     │  │
│  └────────────────────┘ │  └─────────────────────┘  │
└──────────────────────────┴───────────────────────────┘
```

## 2. 导航栏 (Nav Bar) — 所有页面共享

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
| 颜色 | `$text-primary` |

### 2.2 导航链接
| 属性 | 值 |
|------|-----|
| 间距 | 24px |
| 对齐 | 垂直居中 |

| 链接 | 默认色 | 高亮(当前页) |
|------|--------|-------------|
| 首页 | `$text-secondary` | — |
| 浏览 | `$text-secondary` | — |
| **我的** | — | `$accent`, 字重 600 |
| 个人中心 | `$text-secondary` | — |

字体: Inter 14px

### 2.3 用户信息
- **用户名**: Inter 13px Medium 500, `$text-secondary`
- **头像**: 36×36px, 圆形, `$accent-light` 背景, 首字居中, `$accent` 色, Inter 14px Semibold 600

## 3. 侧边栏 (Sidebar) — 所有页面共享

| 属性 | 值 |
|------|-----|
| 宽度 | 280px |
| 布局 | 垂直 |
| 间距 | 16px |

### 3.1 个人信息卡片
| 属性 | 值 |
|------|-----|
| 背景 | `$bg-primary` |
| 圆角 | `$card-radius` (16px) |
| 边框 | 1px solid `$border` |
| 内边距 | 24px |
| 内部间距 | 12px |
| 对齐 | 水平居中 |

| 元素 | 规格 |
|------|------|
| 头像 | 72×72px, 圆形, `$accent-light` 背景 |
| 姓名 | Inter 18px Semibold 600, `$text-primary` |
| 读者证号 | Inter 12px, `$text-muted` |
| 类型标签 | `$accent-light` 背景, 圆角 999px, 内边距 6px 14px, Inter 12px Medium 500, `$accent` |

### 3.2 快捷链接列表
| 属性 | 值 |
|------|-----|
| 背景 | `$bg-primary` |
| 圆角 | `$card-radius` (16px) |
| 边框 | 1px solid `$border` |
| 内边距 | 12px |
| 内部间距 | 4px |

**每个链接:**
| 属性 | 值 |
|------|-----|
| 布局 | 水平 |
| 间距 | 10px |
| 内边距 | 8px 12px |
| 圆角 | 8px |
| 默认背景 | transparent |
| **选中背景** | `$accent-light` (rgba(74,159,216,0.08)) |
| **选中文字色** | `$accent`, 字重 600 |
| 默认文字色 | `$text-primary`, 字重 normal |

**图标**: Inter 16px, `$text-secondary`
**标签**: Inter 13px

| 链接 | 图标 | 路由 |
|------|------|------|
| 当前借阅 | 📖 | /reader (或 /reader/borrowing) |
| 借阅历史 | ⏱ | /reader/history |
| 我的预约 | 📌 | /reader/reservations |
| 我的收藏 | ⭐ | /reader/favorites |
| 我的罚款 | 💰 | /reader/fines |
| 设置 | ⚙️ | /reader/settings |

## 4. 主内容区 (Content)

| 属性 | 值 |
|------|-----|
| 宽度 | `fill_container` (自适应) |
| 布局 | 垂直 |
| 间距 | 24px |
| 内边距 | 上 0px |

---

## 5. 设计 Token 引用

| Token | CSS Variable | 值 |
|-------|-------------|-----|
| bg-primary | `--bg-primary` | #FFFFFF |
| bg-secondary | `--bg-secondary` | #F7F8FA |
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
