# 读者首页 - Reader Home (图书搜索)

> **设计风格:** Soft Bento · Carbon Frost
> **页面尺寸:** 1440 × 900 px
> **背景色:** `#F7F8FA` (`$bg-secondary`)

---

## 布局结构

```
┌─────────────────────────────────────────────────────┐
│  Nav Bar (1440×68)                                  │
│  📚 LibraryOS  |  Home  Browse  Categories  My Books │  [W]
├─────────────────────────────────────────────────────┤
│  Hero Section (白底)                                 │
│  ┌─────────────────────────────────────────────┐    │
│  │  What book are you looking for?             │    │
│  │  Search from thousands of books...          │    │
│  │  ┌──────────────────────────────┐  [Search] │    │
│  │  │ 🔍 Search by title...        │           │    │
│  │  └──────────────────────────────┘           │    │
│  │  [All]  [Available]  [E-Books]  [Audio Books]│    │
│  └─────────────────────────────────────────────┘    │
├─────────────────────────────────────────────────────┤
│  Browse by Category                    View all →    │
│  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐ ┌───┐ │
│  │ 📖   │ │ 🔬  │ │ 💻  │ │ 🏛️  │ │ 💭  │ │ 🎨 │...│
│  │Liter.│ │Science│ │ Tech │ │History│ │Philo.│ │Art│ │
│  └──────┘ └──────┘ └──────┘ └──────┘ └──────┘ └───┘ │
├─────────────────────────────────────────────────────┤
│  🔥 Hot Picks This Week                 View all →  │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐│
│  │ 📖       │ │ 📖       │ │ 📖       │ │ 📖       ││
│  │ Gatsby   │ │ Mockbird │ │ 1984     │ │ Pride    ││
│  │ ★ 4.5    │ │ ★ 4.8    │ │ ★ 4.6    │ │ ★ 4.7   ││
│  │ 4/5 avail │ │ 2/5 avail│ │ 0/5 avail│ │ 3/5 avail││
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘│
└─────────────────────────────────────────────────────┘
```

---

## 1. 导航栏 (Nav Bar)

**容器:** `NqIOq` — 1440 × 68px
**布局:** horizontal, alignItems=center, gap=32px
**填充:** `$bg-primary` (`#FFFFFF`)
**内边距:** [16, 40] (上下/左右)

| 元素 | 属性 | 值 |
|------|------|-----|
| **Logo** (`vqOsV`) | 字体 | Inter, 20px, Bold 700 |
| | 颜色 | `$text-primary` |
| | 内容 | "📚 LibraryOS" |
| | 位置 | x=40, y=17, 尺寸 124×34px |
| **Nav Links** (`OzbcC`) | 布局 | horizontal, gap=24px, alignItems=center |
| | Home | Inter 14px, `$text-secondary`, 40×17px |
| | Browse | Inter 14px, `$text-secondary`, 50×17px |
| | Categories | Inter 14px, `$text-secondary`, 73×17px |
| | My Books | Inter 14px, `$text-secondary`, 66×17px |
| **User Avatar** (`jL4A1`) | 尺寸 | 36×36px, 圆角 999px |
| | 填充 | `$accent-light` (`#E8F4FD`) |
| | 文字 "W" | Inter 13px SemiBold 600, `$accent` |

---

## 2. 搜索区域 (Hero Section)

**容器:** `tM6Nr` — 1440 × 322px
**填充:** `$bg-primary` (`#FFFFFF`)
**内边距:** [60, 80]
**布局:** vertical, gap=20px

### 2.1 标题文字
| 元素 | 属性 | 值 |
|------|------|-----|
| **Hero Title** (`W0xug`) | 字体 | Inter, 36px, Bold 700 |
| | 颜色 | `$text-primary` |
| | 内容 | "What book are you looking for?" |
| | 位置 | x=80, y=60, 尺寸 546×44px |
| **Hero Sub** (`C8ney`) | 字体 | Inter, 15px |
| | 颜色 | `$text-secondary` |
| | 内容 | "Search from thousands of books in our collection" |
| | 位置 | x=80, y=124, 尺寸 349×18px |

### 2.2 搜索栏 (Search Bar)
**容器:** `big6k` — 600 × 48px
**布局:** horizontal, gap=8px, alignItems=center
**圆角:** 999px（药丸形）
**填充:** `$bg-secondary` (`#F7F8FA`)
**描边:** 1.5px solid `$border`
**内边距:** [4, 6, 4, 20]

| 元素 | 属性 | 值 |
|------|------|-----|
| **Search Icon** (`A20Quv`) | "🔍", 16px, `$text-muted` | 固定宽度，自然排列 |
| **Input Area** (`Dlnjv`) | Frame, `fill_container` 自动撑满 | 包裹占位文字，推动按钮靠右 |
| &nbsp;&nbsp;Placeholder (`Z0rfM9`) | Inter 14px, `$text-muted` | "Search by title, author, or ISBN..." |
| **Search Button** (`jdVDo`) | padding [10,20], 圆角 999px, 填充 `$accent` | 位于搜索栏最右侧 |
| &nbsp;&nbsp;Button Text (`YHuIi`) | Inter 13px SemiBold 600, `$text-inverse`, "Search" | — |

### 2.3 快速筛选 (Quick Filters)
**容器:** `Rkrvc` — 水平排列, gap=8px

| 筛选标签 | 样式 |
|---------|------|
| **All** (`UnSsf`) | 48×36px, 圆角 999px, 填充 `$accent`, 文字 `$text-inverse` "All" |
| **Available** (`IMXEV`) | 87×36px, 圆角 999px, 填充 `$bg-secondary`, 描边 1px `$border`, 文字 `$text-secondary` "Available" |
| **E-Books** (`BSt5F`) | 84×36px, 同上, "E-Books" |
| **Audio Books** (`QCIne`) | 110×36px, 同上, "Audio Books" |

---

## 3. 分类浏览区 (Categories)

**容器:** `uPatD` — 1440 × 228px
**布局:** vertical, gap=20px
**内边距:** [40, 80]

### 3.1 Section Header
- horizontal, justifyContent=space_between, alignItems=center

| 元素 | 属性 |
|------|------|
| **Title** (`Sv4wJ`) | Inter 20px SemiBold 600, `$text-primary`, "Browse by Category" |
| **View All** (`UG0DN`) | Inter 13px, `$accent`, "View all →" |

### 3.2 Category Grid
**容器:** `jRmgi` — horizontal, gap=12px

每个分类卡片 — 150×104px, vertical, padding=16, gap=8, alignItems=center
- 填充: `$bg-primary`, 圆角 `$card-radius`(16px), 描边 1px `$border`
- Emoji: 28px, `$text-primary`
- Label: Inter 13px Medium 500, `$text-primary`

| 卡片 | Emoji | Label |
|------|-------|-------|
| Literature | 📖 | Literature |
| Science | 🔬 | Science |
| Technology | 💻 | Technology |
| History | 🏛️ | History |
| Philosophy | 💭 | Philosophy |
| Art | 🎨 | Art |
| Economics | 📊 | Economics |
| Education | 📝 | Education |

---

## 4. 热门推荐区 (Hot Books)

**容器:** `f0D4N` — 1440 × 353px
**布局:** vertical, gap=20px
**内边距:** [0, 80, 40, 80]

### 4.1 Section Header
| Title | "🔥 Hot Picks This Week" |
|-------|-------------------------|
| View All | Inter 13px, `$accent`, "View all →" |

### 4.2 Book Grid
**容器:** `joRD3` — horizontal, gap=16px

每本图书卡片 — 308×269px:
- **布局:** vertical, gap=10px
- **填充:** `$bg-primary`, 圆角 `$card-radius`(16px), 描边 1px `$border`

| 区域 | 属性 |
|------|------|
| **Cover** | 308×180px, 圆角 [14,14,0,0], 填充 `$accent-light` |
| &nbsp;&nbsp;Icon | "📖" 36px, `$accent` (居中) |
| **Info** | padding [12,14], gap=4px |
| &nbsp;&nbsp;Title | Inter 14px SemiBold 600, `$text-primary` |
| &nbsp;&nbsp;Author | Inter 12px, `$text-secondary` |
| &nbsp;&nbsp;Meta | horizontal, gap=12px |
| &nbsp;&nbsp;&nbsp;&nbsp;Rating | Inter 12px, `$warning`, "★ 4.5" |
| &nbsp;&nbsp;&nbsp;&nbsp;Avail | Inter 12px, `$success`, "4/5" |

示例图书:
| Title | Author | Rating | Available |
|-------|--------|--------|-----------|
| The Great Gatsby | F. Scott Fitzgerald | 4.5 | 4/5 |
| To Kill a Mockingbird | Harper Lee | 4.8 | 2/5 |
| 1984 | George Orwell | 4.6 | 0/5 |
| Pride and Prejudice | Jane Austen | 4.7 | 3/5 |

---

## 组件层级关系

```
ReaderHome (ZmLqp) — 1440×900, vertical
├── NavBar (NqIOq) — 1440×68
│   ├── Logo (vqOsV)
│   ├── NavLinks (OzbcC) — [Home, Browse, Categories, My Books]
│   └── UserAvatar (jL4A1)
├── HeroSection (tM6Nr) — 1440×322
│   ├── HeroTitle (W0xug)
│   ├── HeroSub (C8ney)
│   ├── SearchBar (big6k) — 600×48
│   │   ├── SearchIcon (A20Quv)
│   │   ├── InputArea (Dlnjv) [fill_container]
│   │   │   └── Placeholder (Z0rfM9)
│   │   └── SearchButton (jdVDo)
│   └── QuickFilters (Rkrvc) — [All, Available, E-Books, Audio Books]
├── CategoriesSection (uPatD) — 1440×228
│   ├── SectionHeader (K7MY7)
│   └── CategoryGrid (jRmgi) — 8 cat cards
└── HotBooksSection (f0D4N) — 1440×353
    ├── SectionHeader (pYacT)
    └── BookGrid (joRD3) — 4 book cards
```
