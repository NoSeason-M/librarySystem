# 管理后台 - Admin Statistics (统计分析)

> **设计风格:** Soft Bento · Carbon Frost
> **页面尺寸:** 1440 × 900 px
> **背景色:** `#F7F8FA` (`$bg-secondary`)

---

## 布局结构

```
┌─────────┬───────────────────────────────────────────────────────────────┐
│ Sidebar │  Main Content                                                 │
│ (240px) │                                                               │
│         │  Statistics          [📅 Jan 1 - Jul 23, 2026 ▼] [Monthly ▼] │
│ 📊 Dash │                                                               │
│ 📖 Brw  │  ┌──────────────────────────────┬─────────────────────────┐   │
│ 📚 Books│  │  📊 Borrow Statistics         │  🔥 Hot Books          │   │
│ 👥 Rdr  │  │  ┌──────┬──────┬──────┬────┐ │  ┌──┐ ┌──────────────┐ │   │
│ 📈 Stat │  │  │Total │Return│Daily │Peak│ │  │1 │ │Great Gatsby 128│ │   │
│ 💰 Fine │  │  │3,000 │2,800 │15.8  │Mar │ │  │2 │ │1984         95│ │   │
│ ⚙️ Set  │  │  └──────┴──────┴──────┴────┘ │  │3 │ │Mockingbird 72│ │   │
│         │  │  [Bar Chart: Monthly Trends] │  │4 │ │Pride       67│ │   │
│         │  │                               │  │5 │ │Catcher     53│ │   │
│         │  ├──────────────────────────────┴─────────────────────────┤   │
│         │  │  📚 Collection Stats          │  👥 Reader Stats       │   │
│         │  │  ┌──────────┬──────────┐      │  ┌─────────────────┐  │   │
│         │  │  │15,000    │ 35,000   │      │  │ Total: 2,000    │  │   │
│         │  │  │Total Bks │Total Cps │      │  │ Active: 650     │  │   │
│         │  │  └──────────┴──────────┘      │  │ Rate: 32.5%     │  │   │
│ [SA]    │  │  By Category: bars            │  └─────────────────┘  │   │
│ Admin   │  │  Literature ████████ 35%       │  By Reader Type:     │   │
│         │  │  Science    ██████   25%       │  Student ████████75% │   │
│         │  │  Social     █████    20%       │  Teacher ████    15% │   │
│         │  │  Arts       ████     12%       │  Other   ███     10% │   │
│         │  │  Others     ███      8%        │  ──────────────────  │   │
│         │  └───────────────────────────────┴───────────────────────┘   │
└─────────┴───────────────────────────────────────────────────────────────┘
```

---

## 1. 侧边栏 (Sidebar)

同 `07-admin-books.md`，此时 **Statistics** 为激活态（蓝色高亮 `$accent`）。

---

## 2. 主内容区 (Main Content)

**容器:** `SyiLV` — 1200 × 1005px, vertical, gap=24px, padding=[32, 40]

### 2.1 Header
| 元素 | 属性 |
|------|------|
| **Title** (`C4HfC`) | Inter 24px Bold 700, `$text-primary`, "Statistics" |
| **Date Filter** (`BR8kV`) | padding [8,14], 圆角 10px, `$bg-primary`, 描边 1.5px `$border` |
| &nbsp;&nbsp;Icon | "📅" Inter 12px, `$text-muted` |
| &nbsp;&nbsp;Range | "Jan 1 - Jul 23, 2026" Inter 12px, `$text-secondary` |
| &nbsp;&nbsp;Arrow | "▼" Inter 10px, `$text-muted` |
| **Granularity** (`XRMZb`) | 同 Date Filter 样式, "Monthly" ▼ |

---

## 3. Row 1 — Borrow Statistics + Hot Books

**容器:** `ci8AO` — horizontal, gap=20px

### 3.1 Borrow Statistics (左侧, 700px)
**容器:** `pcApv` — 700px, vertical, padding=20, gap=16, `$bg-primary`, 圆角 `$card-radius`, 描边 1px

#### Section Header
- Title: "📊 Borrow Statistics", Inter 16px SemiBold 600
- View Link: "View Details", Inter 12px, `$accent`

#### Summary Row (4 个指标)
每个指标卡片: padding=12, gap=4, `$bg-secondary`, 圆角 10px, 居中

| 指标 | Label (Inter 11px) | Value (Geist Mono 22px Bold) |
|------|-------------------|------------------------------|
| Total Borrow | "Total Borrow" | "3,000" `$text-primary` |
| Total Return | "Total Return" | "2,800" `$text-primary` |
| Avg Daily | "Avg Daily" | "15.8" `$text-primary` |
| Peak Day | "Peak Day" | "Mar 15" `$accent` |

#### Chart Area
- 尺寸: `fill_container` × 150px
- 填充: `$bg-secondary`, 圆角 12px
- 文字: "Bar chart area - Monthly trends", Inter 13px, `$text-muted` (居中)

### 3.2 Hot Books (右侧, fill_container)
**容器:** `D0HCt` — vertical, padding=20, gap=12, `$bg-primary`, 圆角 `$card-radius`, 描边 1px

#### Section Header
- Title: "🔥 Hot Books", Inter 16px SemiBold 600
- Period: "This Month", Inter 11px, `$accent`

#### Top 5 Ranking

每行: horizontal, gap=10px, width=fill_container

| 排名 | 样式 | 图书 | 借阅数 |
|------|------|------|--------|
| **1** 🟦 | 蓝底圆圈, `$accent` | The Great Gatsby | 128 |
| **2** 🟦 | 蓝底圆圈 | 1984 | 95 |
| **3** 🟦 | 蓝底圆圈 | To Kill a Mockingbird | 72 |
| **4** ⚪ | 灰底圆圈, `$bg-secondary` | Pride and Prejudice | 67 |
| **5** ⚪ | 灰底圆圈 | The Catcher in the Rye | 53 |

Rank Badge: 24×24px, 圆角 999px, `$accent` (1-3名) 或 `$bg-secondary` (4-5名)
- 文字: Inter 11px, Top3 为 `$text-inverse` 600, 其余为 `$text-secondary` 400

Book Title: Inter 13px Medium 500, `$text-primary`, flex 撑满
Count: Geist Mono 12px, `$accent`

---

## 4. Row 2 — Collection Stats + Reader Stats

**容器:** `kJo3F` — horizontal, gap=20px

### 4.1 Collection Stats (左侧, fill_container)
**容器:** `GOKMX` — vertical, padding=20, gap=16, `$bg-primary`, 圆角 `$card-radius`, 描边 1px

#### Section Header
- Title: "📚 Collection Stats", Inter 16px SemiBold 600

#### Overview Row (2 个指标)
| 指标 | Value (Geist Mono 22px Bold) |
|------|------------------------------|
| Total Books | "15,000" `$text-primary` |
| Total Copies | "35,000" `$text-primary` |

#### Category Distribution (5 个分类)
每个分类行: horizontal, gap=8px, alignItems=center

| 分类 | Label (Inter 12px, 90px) | Bar (圆角 999px, 高 8px, 140px 背景) | Percent |
|------|--------------------------|--------------------------------------|---------|
| Literature | "Literature" | 98px `$accent` fill | 35% |
| Science Tech | "Science Tech" | 70px `$accent` fill | 25% |
| Social Sciences | "Social Sciences" | 56px `$accent` fill | 20% |
| Arts | "Arts" | 34px `$accent` fill | 12% |
| Others | "Others" | 22px `$accent` fill | 8% |

### 4.2 Reader Stats (右侧, 320px)
**容器:** `g8sSX` — 320px, vertical, padding=20, gap=16, `$bg-primary`, 圆角 `$card-radius`, 描边 1px

#### Section Header
- Title: "👥 Reader Stats", Inter 16px SemiBold 600

#### Overview (3 个指标, vertical)
| 指标 | Value (Geist Mono 26px Bold) |
|------|------------------------------|
| Total Registered | "2,000" `$text-primary` |
| Active (30 days) | "650" `$success` |
| Active Rate | "32.5%" `$accent` |

每个指标卡片: padding=14, gap=4, `$bg-secondary`, 圆角 10px, 居中

#### By Reader Type (3 个类型)

| 类型 | Bar (圆角 999px, 高 8px, 200px 背景) | Percent |
|------|--------------------------------------|---------|
| Student | 150px `$accent` fill | 75% |
| Teacher | 90px `$success` fill | 15% |
| Other | 40px `$warning` fill | 10% |

---

## 组件层级关系

```
AdminStatistics (MH8oX) — 1440×900
├── Sidebar (gcwmh) — 240×900
│   ├── LogoArea (F6hQm)
│   ├── NavItems (wbEsM) — Statistics 激活态
│   └── Bottom (ifrmv)
├── MainContent (SyiLV)
│   ├── Header (XNQZR)
│   │   ├── Title (C4HfC) — "Statistics"
│   │   ├── DateFilter (BR8kV) — 📅 + range + ▼
│   │   └── Granularity (XRMZb) — "Monthly" + ▼
│   ├── Row 1 (ci8AO) — horizontal, gap=20
│   │   ├── BorrowStats (pcApv) — 700px
│   │   │   ├── SectionHeader (Z4ipU)
│   │   │   ├── SummaryRow (W2NPwP) — 4 metrics
│   │   │   └── ChartArea (faRxR) — bar chart placeholder
│   │   └── HotBooks (D0HCt) — fill_container
│   │       ├── SectionHeader (Ufao6)
│   │       ├── Rank 1 (tCBFW) — Great Gatsby
│   │       ├── Rank 2 (W9plBJ) — 1984
│   │       ├── Rank 3 (dWLn0) — Mockingbird
│   │       ├── Rank 4 (hmR09) — Pride
│   │       └── Rank 5 (OghjO) — Catcher
│   └── Row 2 (kJo3F) — horizontal, gap=20
│       ├── CollectionStats (GOKMX) — fill_container
│       │   ├── SectionHeader (k2DLM)
│       │   ├── Overview (pGAdV) — 2 metrics
│       │   ├── CategoryDist (vymUG) — 5 category bars
│       │   └── MonthlyNew (hATKo) — (可扩展)
│       └── ReaderStats (g8sSX) — 320px
│           ├── SectionHeader (WOTw3)
│           ├── Overview (SHHoR) — 3 metrics
│           └── TypeDist (x9QDi) — 3 type bars
```

---

## API 映射

| 区域 | API 端点 | 来自 API.md |
|------|----------|-------------|
| Borrow Statistics | `GET /api/statistics/borrow` + `GET /api/statistics/circulation` | §9.1 + §9.3 |
| Hot Books | `GET /api/statistics/hot-books?type=month&limit=5` | §9.2 |
| Collection Stats | `GET /api/statistics/collection` | §9.4 |
| Reader Stats | `GET /api/statistics/readers` | §9.5 |
