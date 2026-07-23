# 管理后台 - Admin Readers (读者管理)

> **设计风格:** Soft Bento · Carbon Frost
> **页面尺寸:** 1440 × 900 px
> **背景色:** `#F7F8FA` (`$bg-secondary`)

---

## 布局结构

```
┌─────────┬───────────────────────────────────────────────────────────┐
│ Sidebar │  Main Content                                             │
│ (240px) │                                                           │
│         │  Readers                                 [+ Add New Reader]│
│ 📊 Dash │                                                           │
│ 📖 Brw  │  ┌──────────────────────┬────────────┬────────────┐       │
│ 📚 Books│  │ 🔍 Search name/no..  │All Types ▼ │ CardStatus▼│      │
│ 👥 Rdr. │  └──────────────────────┴────────────┴────────────┘       │
│ 📈 Stat │                                                           │
│ 💰 Fine │  ┌────┬──────────┬──────────┬─────┬─────┬─────┬─────┐    │
│ ⚙️ Set  │  │Av.│ Name     │ ReaderNo │Type │Status│ Brw │Phone│    │
│         │  ├────┼──────────┼──────────┼─────┼─────┼─────┼─────┤    │
│ [SA]    │  │ [W]│Wang..    │RD20260001│Stu  │ 🟢N │  3  │138..│    │
│ System  │  │ [L]│Li Hua    │RD20260002│Teac │ 🟢N │  0  │139..│    │
│ Admin   │  │ [Z]│Zhang Wei │RD20260003│Stu  │ 🔴L │  2  │136..│    │
│         │  │ [C]│Chen Mei  │RD20260004│Stu  │ ⚪F │  5  │137..│    │
│         │  └────┴──────────┴──────────┴─────┴─────┴─────┴─────┘    │
│         │                                       [Edit] [Card] 靠右  │
│         │  Showing 1-8 of 128     ← [1][2][3][4][5] →              │
└─────────┴───────────────────────────────────────────────────────────┘

┌─────────────────────────────────────┐
│  Card Management                    ✕│
│                                     │
│  ┌─────────────────────────────┐    │
│  │ Zhang Wei                   │    │
│  │ RD20260003 · Student · 2    │    │
│  │ [LOST]                      │    │
│  └─────────────────────────────┘    │
│                                     │
│  ┌─────────────────────────────┐    │
│  │ 🔴 Report Lost              │    │
│  ├─────────────────────────────┤    │
│  │ 🟢 Restore Card             │    │
│  ├─────────────────────────────┤    │
│  │ ⏸ Freeze Card               │    │
│  ├─────────────────────────────┤    │
│  │ ▶️ Unfreeze Card             │    │
│  └─────────────────────────────┘    │
└─────────────────────────────────────┘
```

---

## 1. 侧边栏 (Sidebar)

同 `07-admin-books.md`，此时 **Readers** 为激活态（蓝色高亮 `$accent`）。

---

## 2. 主内容区 (Main Content)

### 2.1 Header
| 元素 | 属性 |
|------|------|
| **Page Title** (`KI3Tv`) | Inter 24px Bold 700, `$text-primary`, "Readers" |
| **Add Button** (`wtuN6`) | padding [10,20], 圆角 10px, 填充 `$accent` |
| &nbsp;&nbsp;Plus (`AcBec`) | "+", Inter 16px, `$text-inverse` |
| &nbsp;&nbsp;Label (`ifzmx`) | "Add New Reader", Inter 14px SemiBold 600, `$text-inverse` |

### 2.2 Toolbar
| 元素 | 宽度 | 属性 |
|------|------|------|
| **Search Box** | 320px | 🔍 + "Search by name, reader no or phone..." |
| **Type Filter** | 140px | "All Types" ▼, 圆角 10px, `$bg-primary`, 描边 1.5px `$border` |
| **Status Filter** | 140px | "Card Status" ▼, 同上样式 |

### 2.3 数据表格 (Table)

**列宽定义:**

| 列 | 宽度 | 对齐 |
|----|------|------|
| Avatar | 50px | 左 |
| Name | 150px | 左 |
| Reader No | 130px | 左 |
| Type | 90px | 左 |
| Card Status | 110px | 左 |
| Borrowed | 70px | 左 |
| Phone | 130px | 左 |
| Email | 150px | 左 |
| Spacer | `fill_container` | 弹性撑满 |
| Actions | 110px | **右对齐** |

#### 每行数据 (72px 高, padding [12,20])

| 列 | 样式 |
|----|------|
| **Avatar** | 36×36px 圆, `$accent-light` 背景, 首字母 |
| **Name** | Inter 13px Medium 500, `$text-primary` |
| **Reader No** | Geist Mono 12px, `$text-secondary` |
| **Type** | Student / Teacher / Staff / External |
| **Card Status** | 胶囊标签 |
| **Borrowed** | 当前借阅数 |
| **Phone** | 脱敏显示 |
| **Email** | 邮箱 |

#### Card Status 标签

| 状态 | 填充色 | 语义 |
|------|--------|------|
| 🟢 **Normal** | `$success` (`#34D399`) | 正常 |
| 🔴 **Lost** | `$danger` (`#F87171`) | 挂失 |
| ⚪ **Frozen** | `$text-muted` (`#888888`) | 冻结 |

#### Actions (靠右对齐, gap=4px)
| 按钮 | 样式 |
|------|------|
| **Edit** | padding [5,8], 圆角 6px, 填充 `$accent-light`, 文字 `$accent`, "Edit" |
| **Card** | padding [5,8], 圆角 6px, 填充 `rgba(74,159,216,0.08)`, 文字 `$accent`, "Card" |

### 2.4 分页

同 `07-admin-books.md`，页码按钮 1-5，第 1 页激活。

---

## 3. Card Action Modal (挂失/解挂/冻结/解冻)

**容器:** `P66ud7` — 360 × 420px, 圆角 `$card-radius`, 填充 `$bg-primary`, 描边 1px `$border`
**布局:** vertical, gap=20px, padding=24px

### 3.1 Modal Header
- **Title**: "Card Management", Inter 18px SemiBold 600, `$text-primary`
- **Close**: "X" Inter 16px, `$text-muted`

### 3.2 Reader Info
**容器:** `YlyEK` — padding 14px, 填充 `$bg-secondary`, 圆角 10px

| 元素 | 属性 |
|------|------|
| **Name** (`u9b2N`) | Inter 14px SemiBold 600, `$text-primary`, "Zhang Wei" |
| **Detail** (`jfN4Z`) | Inter 12px, `$text-muted`, "RD20260003 - Student - Current: 2 books" |
| **Status Badge** (`N40wc`) | padding [4,10], 圆角 999px, 填充 `$danger` |
| &nbsp;&nbsp;Label (`QyjWm`) | "LOST", Inter 11px Medium 500, `$text-inverse` |

### 3.3 Action Buttons (4个)

每个按钮: padding [14,16], gap=12px, 圆角 12px, `$bg-primary`, 描边 1px `$border`

| 按钮 | 图标色 | 文字 (Inter 14px Medium 500) |
|------|--------|------------------------------|
| 🔴 **Report Lost** | `$danger` | 挂失 |
| 🟢 **Restore Card** | `$success` | 解挂 |
| ⏸ **Freeze Card** | `$text-muted` | 冻结 |
| ▶️ **Unfreeze Card** | `$accent` | 解冻 |

---

## 组件层级关系

```
AdminReaders (z7kMI) — 1440×900
├── Sidebar (ydEqA) — 240×900
│   ├── LogoArea (XT6QX)
│   ├── NavItems (iyNzj) — Readers 激活态
│   └── Bottom (jQAa4)
├── MainContent (VJENT)
│   ├── Header (d9dhn) — "Readers" + [Add New Reader]
│   ├── Toolbar (H9djbh) — search + 2 filters
│   ├── Table (YDWsD) — 8 reader rows
│   │   ├── TableHeader (Lk2Da)
│   │   ├── Wang Xiaoming (sCnls)
│   │   ├── Li Hua (bsweF)
│   │   ├── Zhang Wei (v3HOz5)
│   │   ├── Chen Mei (dEz7R)
│   │   ├── Liu Yang (UG9wT)
│   │   ├── Zhao Liu (k97DS)
│   │   ├── Sun Qian (vfxq1)
│   │   └── Wang Fang (C7MsJS)
│   └── Pagination (csqSM)
│       └── Page Buttons [1][2][3][4][5]
│
└── Card Action Modal (P66ud7) — 360×420
    ├── ModalHeader (xhYYe)
    ├── ReaderInfo (YlyEK) — name + detail + status badge
    └── ModalActions (mBy81)
        ├── ActionLost (rboug) — Report Lost
        ├── ActionRestore (Y0lYUk) — Restore Card
        ├── ActionFreeze (X4Akk) — Freeze Card
        └── ActionUnfreeze (cuMx7) — Unfreeze Card
```
