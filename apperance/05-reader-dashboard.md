# 读者中心 - Reader Dashboard

> **设计风格:** Soft Bento · Carbon Frost
> **页面尺寸:** 1440 × 900 px
> **背景色:** `#F7F8FA` (`$bg-secondary`)

---

## 布局结构

```
┌─────────────────────────────────────────────────────┐
│  Nav Bar (1440×66)                                   │
│  📚 LibraryOS  |  Home  Browse  My Books  Profile    │
├─────────────────────────────────────────────────────┤
│  Main (padding: [40,80], gap=32, horizontal)         │
│                                                      │
│  ┌──────────┐  ┌──────────────────────────────────┐ │
│  │ Profile  │  │  Welcome back, Wang Xiaoming 👋   │ │
│  │ ┌──────┐ │  │  Here's your activity overview    │ │
│  │ │ [W]  │ │  │                                  │ │
│  │ └──────┘ │  │  ┌──────┐ ┌──────┐ ┌──────┐ ┌──┐│ │
│  │ Wang     │  │  │Borrow│ │Over- │ │Reser-│ │Fi││ │
│  │RD20260001│  │  │ 3    │ │ 1    │ │ 2    │ │¥5││ │
│  │ [Student]│  │  │books │ │book  │ │pend. │ │un││ │
│  │          │  │  └──────┘ └──────┘ └──────┘ └──┘│ │
│  │ My Books │  │                                  │ │
│  │ Borrow.. │  │  Currently Borrowing    View all →│ │
│  │ History  │  │  ┌──────────────────────────────┐ │ │
│  │ Reserv.. │  │  │ ▌ The Great Gatsby          │ │ │
│  │ Favorites│  │  │ █  Due: Aug 20  28 days left │ │ │
│  │ Fines    │  │  ├──────────────────────────────┤ │ │
│  │ Settings │  │  │ ▌ To Kill a Mockingbird      │ │ │
│  └──────────┘  │  │ █  Due: Jul 30  5 days overdue│ │
│                │  └──────────────────────────────┘ │ │
│                └──────────────────────────────────┘ │
└─────────────────────────────────────────────────────┘
```

---

## 1. 导航栏 (Nav Bar)

**容器:** `R17Dt` — 1440 × 66px, 填充 `$bg-primary`
**内边距:** [16, 40]

| 元素 | 属性 |
|------|------|
| **Logo** (`zHMNH`) | "📚 LibraryOS", Inter 20px Bold 700 |
| **Nav Links** (`u7dQ7I`) | horizontal, gap=24px, [Home, Browse, My Books, Profile] |
| | 每个: Inter 14px, `$text-secondary` |

---

## 2. 主内容区 (Main)

**容器:** `K0e5S` — 1440 × 634px
**布局:** **horizontal**, gap=32px
**内边距:** [40, 80]

### 2.1 左侧边栏 (Sidebar — 280px)

#### 个人信息卡片 (Profile Card)
**容器:** `LcCpO` — 185 × 260px(measured)
**布局:** vertical, gap=12px, padding=24px, alignItems=center
**填充:** `$bg-primary`, 圆角 `$card-radius`(16px), 描边 1px `$border`

| 元素 | 属性 |
|------|------|
| **Avatar** (`xhESM`) | 72×72px, 圆角 999px, 填充 `$accent-light` |
| &nbsp;&nbsp;Initials (`l475V`) | "W" Inter 28px SemiBold 600, `$accent` |
| **Name** (`V8aPp6`) | Inter 18px SemiBold 600, `$text-primary`, "Wang Xiaoming" |
| **Reader No** (`h7uqsf`) | Geist Mono 12px, `$text-muted`, "RD20260001" |
| **Type Badge** (`JYtCp`) | padding [6,14], 圆角 999px, 填充 `$accent-light` |
| &nbsp;&nbsp;Type Label | Inter 12px Medium 500, `$accent`, "Student" |

#### 快捷链接列表 (Quick Links)
**容器:** `YXCUg` — 186 × 278px
**布局:** vertical, gap=4px, padding=12px
**填充:** `$bg-primary`, 圆角 `$card-radius`, 描边 1px `$border`

每个链接项: padding [8,12], gap=10px, 圆角 8px, horizontal, alignItems=center

| 链接 | Icon | Label |
|------|------|-------|
| My Borrowing | 📖 | "My Borrowing" |
| Borrowing History | ⏱ | "Borrowing History" |
| My Reservations | 📌 | "My Reservations" |
| My Favorites | ⭐ | "My Favorites" |
| My Fines | 💰 | "My Fines" |
| Settings | ⚙️ | "Settings" |

Icon: 16px, `$text-secondary` / Label: Inter 13px, `$text-primary`

### 2.2 右侧内容区 (Content Area — fill_container)

**容器:** `fBifN` — 968 × 486px
**布局:** vertical, gap=24px

#### Welcome Row
- **Greeting**: Inter 24px Bold 700, `$text-primary`, "Welcome back, Wang Xiaoming 👋"
- **Sub**: Inter 14px, `$text-secondary`, "Here's your library activity overview"

#### Stats Row
**容器:** `cXMAH` — horizontal, gap=16px

每个统计卡片: vertical, padding=20px, gap=6px, `$bg-primary`, 圆角 `$card-radius`, 描边 1px `$border`

| 卡片 | Label | Value (Geist Mono 28px Bold) | Unit |
|------|-------|------------------------------|------|
| **Currently Borrowed** | "Currently Borrowed" | "3" `$accent` | "books" |
| **Overdue** | "Overdue" | "1" `$danger` | "book" |
| **Reservations** | "Reservations" | "2" `$warning` | "pending" |
| **Fines** | "Fines" | "¥5.00" `$danger` | "unpaid" |

Label: Inter 12px, `$text-muted`
Unit: Inter 12px, `$text-secondary`

#### Borrow Section
**容器:** `zid5E` — vertical, gap=16px

| Section Header | "Currently Borrowing" | "View all →" `$accent` |
|----------------|----------------------|------------------------|

**借阅列表** (`Ncc9t`): vertical, gap=8px

每行: horizontal, padding=16px, gap=16px, `$bg-primary`, 圆角 12px, 描边 1px `$border`, alignItems=center

| 列 | 属性 |
|----|------|
| **Color Block** | 4×40px, 圆角 2px — `$success` (正常) / `$danger` (逾期) |
| **Title** | Inter 14px SemiBold 600, `$text-primary` |
| **Due** | Inter 12px — `$text-secondary` (正常) / `$danger` (逾期) |
| **Days** | Geist Mono 13px Medium 500 — `$text-secondary` (正常) / `$danger` (逾期) |
| **Action** | Inter 13px, `$accent`, "Renew →" |

| 图书 | Color | Due | 天数 |
|------|-------|-----|------|
| The Great Gatsby | 🟢 `$success` | Aug 20, 2026 | "28 days left" |
| To Kill a Mockingbird | 🔴 `$danger` | Jul 30, 2026 (逾期) | "5 days overdue" |
| 1984 | 🟢 `$success` | Sep 5, 2026 | "44 days left" |

---

## 组件层级关系

```
ReaderDashboard (q5tea) — 1440×900, vertical
├── NavBar (R17Dt) — 1440×66
│   ├── Logo (zHMNH)
│   └── NavLinks (u7dQ7I)
├── Main (K0e5S) — 1440×634, horizontal, gap=32
│   ├── Sidebar (yBx2y) — 280px
│   │   ├── ProfileCard (LcCpO)
│   │   │   ├── Avatar (xhESM)
│   │   │   ├── Name (V8aPp6)
│   │   │   ├── ReaderNo (h7uqsf)
│   │   │   └── TypeBadge (JYtCp)
│   │   └── QuickLinks (YXCUg)
│   │       ├── MyBorrowing (sMGc0)
│   │       ├── BorrowingHistory (Rag7i)
│   │       ├── MyReservations (BpBgh)
│   │       ├── MyFavorites (UCZ8O)
│   │       ├── MyFines (jH7PC)
│   │       └── Settings (xRyFO)
│   └── ContentArea (fBifN) — fill_container
│       ├── WelcomeRow (hQGs9)
│       │   ├── Greeting (E2xx3)
│       │   └── Sub (jiUzs)
│       ├── StatsRow (cXMAH) — 4 stat cards
│       │   ├── CurrentlyBorrowed (etPcE)
│       │   ├── Overdue (C6umj)
│       │   ├── Reservations (F9zE8)
│       │   └── Fines (BKTKZ)
│       └── BorrowSection (zid5E)
│           ├── SectionHeader (X4JgFh)
│           └── BorrowList (Ncc9t) — 3 borrow items
```
