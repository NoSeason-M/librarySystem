# 管理后台 - Admin Settings (系统管理)

> **设计风格:** Soft Bento · Carbon Frost
> **页面尺寸:** 1440 × 900 px
> **背景色:** `#F7F8FA` (`$bg-secondary`)

---

## 布局结构

```
┌─────────┬───────────────────────────────────────────────────────────────┐
│ Sidebar │  Main Content                                                 │
│ (240px) │                                                               │
│         │  Settings                                                     │
│ 📊 Dash │                                                               │
│ 📖 Brw  │  ┌────────────┐  ┌────────────┐  ┌────────────┐              │
│ 📚 Books│  │ [U] Users  │  │ [R] Roles  │  │ [M] Menus  │              │
│ 👥 Rdr  │  └────────────┘  └────────────┘  └────────────┘              │
│ 📈 Stat │                                                               │
│ 💰 Fine │  ┌────────────┐  ┌────────────┐  ┌────────────┐              │
│ ⚙️ Set  │  │ [C] Config │  │ [D] Dict   │  │ [L] Logs   │              │
│         │  └────────────┘  └────────────┘  └────────────┘              │
│ [SA]    │                                                               │
│ Admin   │  ┌────────────┐  ┌────────────┐                               │
│         │  │ [A] Announce│  │ [B] Backup │                               │
│         │  └────────────┘  └────────────┘                               │
└─────────┴───────────────────────────────────────────────────────────────┘
```

---

## 1. 侧边栏 (Sidebar)

**组件ID:** `p6aRRg` — 240 × 900px, 填充 `$bg-inverse` (`#0A0A0A`)
**布局:** vertical

7 项导航菜单，**Settings** 为激活态（蓝色高亮 `$accent`）。

---

## 2. 主内容区 — 功能卡片网格

**容器:** `xqHGX` — 1200 × 558px
**布局:** vertical, gap=24px, padding=[32, 40]

### 2.1 Header

| 元素 | 属性 |
|------|------|
| **Title** (`qYM2v`) | Inter 24px Bold 700, `$text-primary`, "Settings" |

### 2.2 卡片网格（3行）

行间距: gap=24px，卡片间 gap=20px

#### 可复用组件 — Settings Card (`IhQ9v` — reusable)

| 属性 | 值 |
|------|-----|
| 尺寸 | 340 × 131px（实例中 `fill_container` 自适应） |
| 布局 | vertical, gap=12px |
| 填充 | `$bg-primary`, 圆角 `$card-radius`(16px), 描边 1px `$border` |
| 内边距 | 20px |

| 子元素 | 属性 |
|--------|------|
| **Card Icon** (`tuzsL`) | Inter 28px, `$accent` (蓝色), 首字母缩写 |
| **Card Title** (`psdoM`) | Inter 15px SemiBold 600, `$text-primary` |
| **Card Desc** (`uY1JM`) | Inter 12px, `$text-muted`, textGrowth fixed-width 300px |

### 2.3 Row 1 — 3张卡片

**容器:** `O5nbaq` — 1120 × 131px, horizontal, gap=20px

| 卡片 | ID | Icon | Title | Description |
|------|-----|------|-------|-------------|
| 👥 User Management | `r9nqV7` | "U" | User Management | Create, edit and manage system users |
| 🔐 Role Management | `Cdl1v` | "R" | Role Management | Configure roles and permission trees |
| 📋 Menu Management | `YfpDD` | "M" | Menu Management | Manage navigation menus and permissions |

### 2.4 Row 2 — 3张卡片

**容器:** `zETqN` — 1120 × 131px, horizontal, gap=20px

| 卡片 | ID | Icon | Title | Description |
|------|-----|------|-------|-------------|
| ⚙️ System Config | `D7970S` | "C" | System Config | Configure borrowing rules and system settings |
| 📖 Data Dictionary | `tlHUj` | "D" | Data Dictionary | Manage dictionary entries and classifications |
| 📝 Operation Logs | `BmYOt` | "L" | Operation Logs | View system audit trail (append-only) |

### 2.5 Row 3 — 2张卡片

**容器:** `gZneP` — 1120 × 131px, horizontal, gap=20px

| 卡片 | ID | Icon | Title | Description |
|------|-----|------|-------|-------------|
| 📢 Announcements | `sD5nx` | "A" | Announcements | Publish announcements and notifications |
| 💾 Data Backup | `Q0mRVN` | "B" | Data Backup | Manual backup, download and restore |

---

## 组件层级关系

```
AdminSettings (I2w7mf) — 1440×900
├── Sidebar (p6aRRg) — 240×900
│   ├── LogoArea (GPmFM)
│   ├── NavItems (m9b9ZX) — 7 items, Settings 激活态
│   └── Bottom (ODkWG)
└── MainContent (xqHGX)
    ├── Header (JkYuA) — "Settings"
    ├── Row 1 (O5nbaq) — 3 cards
    │   ├── UserManagement (r9nqV7)
    │   ├── RoleManagement (Cdl1v)
    │   └── MenuManagement (YfpDD)
    ├── Row 2 (zETqN) — 3 cards
    │   ├── SystemConfig (D7970S)
    │   ├── DataDictionary (tlHUj)
    │   └── OperationLogs (BmYOt)
    └── Row 3 (gZneP) — 2 cards
        ├── Announcements (sD5nx)
        └── DataBackup (Q0mRVN)
```
