# 管理后台 - Admin Dashboard

> **设计风格:** Soft Bento · Carbon Frost
> **页面尺寸:** 1440 × 900 px
> **背景色:** `#F7F8FA` (`$bg-secondary`)

---

## 布局结构

```
┌─────────────────────────────────────────────────────────┐
│  Sidebar (240px)  │  Main Content (1200px)              │
│  ┌─────────────┐  │  ┌──────────────────────────────┐   │
│  │ 📚 LibraryOS│  │  │ Dashboard    [Jul 23, 2026]   │   │
│  │             │  │  ├──────────────────────────────┤   │
│  │ 📊 Dashboard│  │  │ Total   Active   Today   Over│   │
│  │ 📖 Borrow.. │  │  │ Books   Readers  Borrow  due │   │
│  │ 📚 Books    │  │  │ 15,234  2,847    47      23  │   │
│  │ 👥 Readers  │  │  │ +128    +43     +12    -5   │   │
│  │ 📈 Stats    │  │  ├──────────────┬───────────────┤   │
│  │ 💰 Fines    │  │  │ 📋 Activity  │ ⚡ Quick Actn │   │
│  │ ⚙️ Settings │  │  │ ● Wang borro │ 📕 Borrow     │   │
│  │             │  │  │   Gatsby     │ 📗 Return     │   │
│  │             │  │  │ ● Li returned│ ➕ Add Book   │   │
│  │ [SA]        │  │  │ ● Zhang res. │ 👤 Register   │   │
│  │ System Admn │  │  │ ● Chen over. │               │   │
│  └─────────────┘  │  │ ● Liu borrow │ 📊 Collection │   │
│                   │  │              │ Literature ██ │   │
│                   │  │              │ Science ██   │   │
│                   │  │              │ SocSci ██    │   │
│                   │  │              │ Arts ██      │   │
│                   │  │              │ Others █     │   │
│                   │  └──────────────┴───────────────┘   │
└─────────────────────────────────────────────────────────┘
```

---

## 1. 侧边栏 (Sidebar)

**容器:** `nKSX2` — 240 × 900px, 填充 `$bg-inverse` (`#0A0A0A`)
**布局:** vertical

### 1.1 Logo 区域
**容器:** `yq5UA` — padding [20,20], horizontal, gap=10px, alignItems=center

| 元素 | 属性 |
|------|------|
| **Logo Icon** (`RaH3S`) | "📚", Inter 22px, `$text-inverse` |
| **Logo Text** (`r44lz`) | "LibraryOS", Inter 18px Bold 700, `$text-inverse` |

### 1.2 导航菜单 (Nav Items)
**容器:** `p2CaNd` — vertical, gap=4px, padding=[12,12], width=`fill_container`

| 菜单项 | 激活态样式 | 默认样式 |
|--------|-----------|---------|
| **Dashboard** (`CiAld`) | 填充 `$accent`, 文字 `$text-inverse` 600 | — (激活) |
| **Borrow/Return** (`SxD62`) | — | 文字 `$text-muted` 400 |
| **Books** (`jJRwU`) | — | 同上 |
| **Readers** (`PT2LV`) | — | 同上 |
| **Statistics** (`pKLrK`) | — | 同上 |
| **Fines** (`t1V4Hn`) | — | 同上 |
| **Settings** (`ZBaUd`) | — | 同上 |

每个菜单项: padding [10,14], gap=12px, 圆角 10px, horizontal, alignItems=center
- Icon: 16px
- Label: Inter 13px

### 1.3 底部用户信息
**容器:** `UCkov` — 240×64px, horizontal, padding=[16,20], gap=10px, alignItems=center

| 元素 | 属性 |
|------|------|
| **Avatar** (`DAmTh`) | 32×32px, 圆角 999px, 填充 `$accent-light` |
| &nbsp;&nbsp;Initials (`DMf8u`) | "SA", Inter 11px SemiBold 600, `$accent` |
| **Name** (`c9Q8A`) | Inter 12px, `$text-inverse`, "System Admin" |

---

## 2. 主内容区 (Main Content)

**容器:** `XVWno` — 1200 × 750px
**布局:** vertical, 内边距 [32, 40], gap=24px

### 2.1 Header
**容器:** `L5FDp` — horizontal, justifyContent=space_between, alignItems=center

| 元素 | 属性 |
|------|------|
| **Page Title** (`kvEa5`) | Inter 24px Bold 700, `$text-primary`, "Dashboard" |
| **Date Badge** (`rLl1K`) | padding [8,14], 圆角 999px, `$bg-primary`, 描边 1px `$border` |
| &nbsp;&nbsp;Date (`fwKDP`) | Inter 12px, `$text-secondary`, "Thursday, Jul 23, 2026" |

### 2.2 Stats Row
**容器:** `aJNVR` — horizontal, gap=16px

每个统计卡片: vertical, padding=20px, gap=8px, `$bg-primary`, 圆角 `$card-radius`(16px), 描边 1px `$border`

| 卡片 | Label | Value (Geist Mono 28px Bold) | Change (Inter 12px) |
|------|-------|------------------------------|---------------------|
| **Total Books** | "Total Books" | "15,234" `$text-primary` | "+128 this week" `$accent` |
| **Active Readers** | "Active Readers" | "2,847" `$text-primary` | "+43 this week" `$success` |
| **Borrowed Today** | "Borrowed Today" | "47" `$text-primary` | "+12 today" `$warning` |
| **Overdue** | "Overdue" | "23" `$text-primary` | "-5 this week" `$danger` |

Change 颜色: `$accent`(`#4A9FD8`) / `$success`(`#34D399`) / `$warning`(`#FBBF24`) / `$danger`(`#F87171`)

### 2.3 Two Column 布局
**容器:** `unSJn` — horizontal, gap=20px

#### 左列: Recent Activity (`zA9Nc`)
**容器:** `zA9Nc` — 740px, vertical, gap=16px

- **Section Title**: "📋 Recent Activity", Inter 16px SemiBold 600, `$text-primary`

**Activity List** (`NOQzv`): 650×307px, vertical, gap=4px, `$bg-primary`, 圆角 `$card-radius`, 描边 1px `$border`, padding=8px

每条活动: horizontal, padding=[12,8], gap=12px, alignItems=center

| 元素 | 属性 |
|------|------|
| **Dot** | 8×8px, 圆角 999px |
| **Desc** | Inter 13px, `$text-primary` |
| **Time** | Inter 11px, `$text-muted` |

| 活动 | Dot Color | 描述 |
|------|-----------|------|
| Wang Xiaoming | `$accent` | "Wang Xiaoming borrowed 'The Great Gatsby'" |
| Li Hua | `$success` | "Li Hua returned '1984'" |
| Zhang Wei | `$warning` | "Zhang Wei reserved 'Pride and Prejudice'" |
| Chen Mei | `$danger` | "Chen Mei returned overdue 'To Kill a Mockingbird'" |
| Liu Yang | `$accent` | "Liu Yang borrowed 'The Catcher in the Rye'" |

Dot 颜色 — 操作类型映射:
| 颜色 | 操作 |
|------|------|
| `$accent` (#4A9FD8) | 借书 (Borrow) |
| `$success` (#34D399) | 还书 (Return) |
| `$warning` (#FBBF24) | 预约 (Reserve) |
| `$danger` (#F87171) | 逾期 (Overdue) |

#### 右列: Quick Actions + Chart (`zYzsK`)
**容器:** `zYzsK` — 360px, vertical, gap=16px

##### Quick Actions
**容器:** `oJUMQ` — vertical, gap=8px

每个按钮: horizontal, padding=[14,16], gap=12px, 圆角 12px, `$bg-primary`, 描边 1px `$border`, alignItems=center

| 按钮 | Icon (20px, `$accent`) | Label (Inter 14px Medium 500) |
|------|----------------------|-------------------------------|
| Borrow Book | 📕 | "Borrow Book" |
| Return Book | 📗 | "Return Book" |
| Add New Book | ➕ | "Add New Book" |
| Register Reader | 👤 | "Register Reader" |

##### Category Chart
**容器:** `YDpv2` — 328×155px, vertical, padding=16px, gap=12px, `$bg-primary`, 圆角 `$card-radius`, 描边 1px `$border`

- **Section Title**: "📊 Collection by Category", Inter 16px SemiBold 600

每个分类行: horizontal, gap=8px, alignItems=center

| 分类 | Label (Inter 12px, 100px) | Bar (圆角 999px, 高 8px) | Percent (Geist Mono 11px) |
|------|--------------------------|--------------------------|--------------------------|
| Literature | "Literature" | 35×2.8=98px `$accent` on 140px `$bg-secondary` | "35%" |
| Science & Tech | "Science & Tech" | 70px `$accent` | "25%" |
| Social Sciences | "Social Sciences" | 56px `$accent` | "20%" |
| Arts | "Arts" | 34px `$accent` | "12%" |
| Others | "Others" | 22px `$accent` | "8%" |

---

## 组件层级关系

```
AdminDashboard (yKDVx) — 1440×900, horizontal
├── Sidebar (nKSX2) — 240×900, vertical
│   ├── LogoArea (yq5UA)
│   │   ├── LogoIcon (RaH3S)
│   │   └── LogoText (r44lz)
│   ├── NavItems (p2CaNd) — vertical, gap=4
│   │   ├── Dashboard (CiAld) [active]
│   │   ├── Borrow/Return (SxD62)
│   │   ├── Books (jJRwU)
│   │   ├── Readers (PT2LV)
│   │   ├── Statistics (pKLrK)
│   │   ├── Fines (t1V4Hn)
│   │   └── Settings (ZBaUd)
│   └── BottomSection (UCkov)
│       ├── Avatar (DAmTh)
│       └── Name (c9Q8A)
│
└── MainContent (XVWno) — 1200×750, vertical
    ├── Header (L5FDp)
    │   ├── PageTitle (kvEa5)
    │   └── DateBadge (rLl1K)
    ├── StatsRow (aJNVR) — 4 cards
    │   ├── TotalBooks (FHtiL)
    │   ├── ActiveReaders (brpTz)
    │   ├── BorrowedToday (XlTFs)
    │   └── Overdue (J7EX0)
    └── TwoColumn (unSJn) — horizontal, gap=20
        ├── LeftColumn (zA9Nc) — 740px
        │   └── ActivityList (NOQzv) — 5 items
        └── RightColumn (zYzsK) — 360px
            ├── QuickActions (oJUMQ) — 4 action buttons
            └── CategoryChart (YDpv2) — 5 category bars
```
