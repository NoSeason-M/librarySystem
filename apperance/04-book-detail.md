# 图书详情页 - Book Detail

> **设计风格:** Soft Bento · Carbon Frost
> **页面尺寸:** 1440 × 900 px
> **背景色:** `#F7F8FA` (`$bg-secondary`)

---

## 布局结构

```
┌─────────────────────────────────────────────────────┐
│  Nav Bar (1440×66)                                   │
│  📚 LibraryOS                                        │
├─────────────────────────────────────────────────────┤
│  Main Content (padding: [40,80])                     │
│                                                      │
│  ← Back to results                                   │
│                                                      │
│  ┌──────────┬──────────────────────────────────────┐ │
│  │ 📖 Cover │  The Great Gatsby                    │ │
│  │          │  by F. Scott Fitzgerald              │ │
│  │ 220×300  │  ★★★★½  4.5  (1,024 reviews)        │ │
│  │          │  [Fiction] [Classic] [Amer. Lit.]     │ │
│  │ ISBN:... │  ╔══════════╗ ╔══════════╗           │ │
│  │          │  ║ Available ║ ║ Borrowed ║           │ │
│  │          │  ║    3     ║ ║    2     ║           │ │
│  │          │  ╚══════════╝ ╚══════════╝           │ │
│  │          │  [Borrow This Book] [♡]              │ │
│  └──────────┴──────────────────────────────────────┘ │
│                                                      │
│  [Description]  [Copies]  [Reviews]  [History]       │
│  ─────────────────────────────────────────────────── │
│  About this book                                     │
│  The Great Gatsby is a 1925 novel...                 │
│                                                      │
│  Copies in Library                                   │
│  ┌──────────┬──────────────┬────────┬──────────┐    │
│  │ Barcode  │ Location     │ Status │ Due Date │    │
│  ├──────────┼──────────────┼────────┼──────────┤    │
│  │ GTB-001  │ Main Library │ ✅     │ -        │    │
│  │ GTB-002  │ Main Library │ ⚠️     │ Aug 15   │    │
│  │ ...      │ ...          │ ...    │ ...      │    │
│  └──────────┴──────────────┴────────┴──────────┘    │
└─────────────────────────────────────────────────────┘
```

---

## 1. 导航栏 (Nav Bar)

**容器:** `kG5FF` — 1440 × 66px, 填充 `$bg-primary`
**内边距:** [16, 40]

| 元素 | 属性 |
|------|------|
| **Logo** (`YRIGd`) | "📚 LibraryOS", Inter 20px Bold 700, `$text-primary` |
| | 位置: x=40, y=16, 尺寸 124×34px |

---

## 2. 主内容区 (Main Content)

**容器:** `ub8yR` — 1440 × 999px
**布局:** vertical, gap=28px
**内边距:** [40, 80]

### 2.1 返回按钮
| 元素 | 属性 |
|------|------|
| **Arrow** (`lq8qK`) | "←" Inter 16px, `$accent` |
| **Back Link** (`iHccX`) | "Back to results" Inter 14px, `$accent` |
| 位置 | x=80, y=40 |

### 2.2 图书主区域 (Book Hero)
**容器:** `aUoCh` — horizontal, gap=40px

#### 左侧封面区 (Cover Section)
**容器:** `ZZeSp` — 宽 240px, vertical, gap=16px, alignItems=center

| 元素 | 属性 |
|------|------|
| **Cover** (`v0TBvx`) | 220×300px, 圆角 12px, 填充 `$accent-light` |
| &nbsp;&nbsp;Cover Icon (`cTol5`) | "📖" 64px, `$accent` (居中) |
| **ISBN** (`PgiKi`) | Geist Mono 11px, `$text-muted` |
| | 内容: "ISBN: 978-7-5308-1234-5" |

#### 右侧图书信息 (Book Info)
**容器:** `Gu81l` — width `fill_container`, vertical, gap=16px

##### Title Section
- **Title** (`s0TvH`): Inter 32px Bold 700, `$text-primary`, "The Great Gatsby", width=500px
- **Author** (`V8lraz`): Inter 16px, `$text-secondary`, "by F. Scott Fitzgerald"

##### Meta Row
- **Rating**: Inter 15px, `$warning`, "★★★★½  4.5"
- **Reviews**: Inter 13px, `$text-muted`, "(1,024 reviews)"

##### Tags (Tag Row — horizontal, gap=8px)
每个标签: padding [6,12], 圆角 999px, 填充 `$accent-light`, 文字 Inter 12px `$accent`

| 标签 |
|------|
| Fiction |
| Classic |
| American Literature |
| 1920s |

##### Availability Row
两个卡片并排: horizontal, gap=16px

| 卡片 | Available | Borrowed |
|------|-----------|----------|
| 尺寸 | 120px, padding 14px | 120px, padding 14px |
| 填充 | `$bg-primary`, 描边 1px `$border` | 同上 |
| 圆角 | 12px | 12px |
| Label | Inter 11px, `$text-muted`, "Available" | Inter 11px, `$text-muted`, "Borrowed" |
| Value | Geist Mono 24px Bold 600, `$success`, "3" | Geist Mono 24px Bold 600, `$warning`, "2" |

##### Action Row
| 按钮 | 属性 |
|------|------|
| **Borrow Button** (`RWufz`) | padding [14,28], 圆角 10px, 填充 `$accent` |
| &nbsp;&nbsp;Label | Inter 14px SemiBold 600, `$text-inverse`, "Borrow This Book" |
| **Favorite Btn** (`gGqGb`) | 48×48px, 圆角 10px, 填充 `$bg-primary`, 描边 1px `$border` |
| &nbsp;&nbsp;Heart | "♡" Inter 20px, `$danger` |

### 2.3 Tabs 区域

**容器:** `uzMA5` — width `fill_container`, vertical, gap=16px

#### Tab Bar
**容器:** `UEWr5` — horizontal, gap=24px

| Tab | 属性 |
|-----|------|
| **Description** (激活) | Inter 14px SemiBold 600, `$accent` + 底部指示线 60×2px `$accent` |
| **Copies** | Inter 14px, `$text-secondary` |
| **Reviews** | Inter 14px, `$text-secondary` |
| **History** | Inter 14px, `$text-secondary` |

#### Tab Divider
- 1280×1px, 填充 `$border`

#### Description Content
- **Section Title**: "About this book", Inter 16px SemiBold 600, `$text-primary`
- **Desc Text**: Inter 14px, lineHeight 1.6, `$text-secondary`
- 内容: "The Great Gatsby is a 1925 novel by American writer F. Scott Fitzgerald..."

#### Copies Section
- **Section Title**: "Copies in Library"

##### 表格 (Table)
**容器:** `Tkdb5` — 1280×298px
**布局:** vertical, gap=2px
**填充:** `$bg-primary`, 圆角 12px, 描边 1px `$border`

**表头行:** padding [14,16], 填充 `$bg-secondary`
| 列 | 字体 | 宽度 |
|----|------|------|
| Barcode | Inter 12px SemiBold 600, `$text-muted` | — |
| Location | 同上 | — |
| Status | 同上 | — |
| Due Date | 同上 | — |

**数据行:** padding [14,16], 描边 0.5px `$border`

| Barcode (Geist Mono 12px) | Location (Inter 12px) | Status (标签) | Due Date |
|--------------------------|----------------------|-------------|----------|
| GTB-001 | Main Library - Floor 2 | 🟢 Available | — |
| GTB-002 | Main Library - Floor 2 | 🟡 Borrowed | Aug 15, 2026 |
| GTB-003 | Science Library | 🟢 Available | — |
| GTB-004 | Science Library | 🔵 Reserved | Aug 20, 2026 |
| GTB-005 | Main Library - Floor 2 | 🟡 Borrowed | Sep 1, 2026 |

**Status 标签样式:** padding [4,10], 圆角 999px, 文字 Inter 11px Medium 500 `$text-inverse`

| 状态 | 填充色 |
|------|--------|
| Available | `$success` (`#34D399`) |
| Borrowed | `$warning` (`#FBBF24`) |
| Reserved | `$accent` (`#4A9FD8`) |

---

## 组件层级关系

```
BookDetail (Slxcj) — 1440×900, vertical
├── NavBar (kG5FF) — 1440×66
│   └── Logo (YRIGd)
├── MainContent (ub8yR) — padding [40,80], gap=28
│   ├── BackRow (V9wnY)
│   │   ├── Arrow (lq8qK)
│   │   └── BackLink (iHccX)
│   ├── BookHero (aUoCh) — horizontal, gap=40
│   │   ├── CoverSection (ZZeSp) — 240px
│   │   │   ├── Cover (v0TBvx) — 220×300
│   │   │   └── ISBN (PgiKi)
│   │   └── BookInfo (Gu81l) — fill_container
│   │       ├── TitleSection (Zx1oH)
│   │       ├── MetaRow (RNwtU)
│   │       ├── TagRow (c70Wgi)
│   │       ├── AvailabilityRow (zmPX2)
│   │       └── ActionRow (GObY6)
│   └── Tabs (uzMA5)
│       ├── TabBar (UEWr5)
│       ├── TabDivider (SE6wo)
│       ├── DescriptionContent (YGxwx)
│       └── CopiesSection (v65G5P)
│           └── Table (Tkdb5) — 5 rows
```
