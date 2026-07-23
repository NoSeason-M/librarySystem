# 管理后台 - Admin Books (图书管理)

> **设计风格:** Soft Bento · Carbon Frost
> **页面尺寸:** 1440 × 900 px
> **背景色:** `#F7F8FA` (`$bg-secondary`)

---

## 布局结构

```
┌─────────┬───────────────────────────────────────────────────────────┐
│ Sidebar │  Main Content                                             │
│ (240px) │                                                           │
│         │  Books                                    [+ Add New Book] │
│ 📊 Dash │                                                           │
│ 📖 Brw  │  ┌──────────────────────┬────────────┬────────────┐       │
│ 📚 Books│  │ 🔍 Search by title.. │All Categ.. ▼│All Status ▼│      │
│ 👥 Rdr  │  └──────────────────────┴────────────┴────────────┘       │
│ 📈 Stat │                                                           │
│ 💰 Fine │  ┌────┬──────────┬──────────┬──────────┬────┬────┬──────┐ │
│ ⚙️ Set  │  │Cover│ Title   │ Author   │ ISBN     │Cat.│Cop.│Status│ │
│         │  ├────┼──────────┼──────────┼──────────┼────┼────┼──────┤ │
│ [SA]    │  │ 📖 │ Gatsby   │F.Scott.. │0743...   │Lit │4/5 │ 🟢   │ │
│ Admin   │  │ 📖 │ 1984     │Orwell    │0451...   │Lit │0/3 │ 🟡   │ │
│         │  │ 📖 │ 百年孤独 │马尔克斯  │9787...   │Lit │1/3 │ 🟡   │ │
│         │  │ 📖 │ 三体     │刘慈欣    │9787...   │Lit │2/5 │ 🟡   │ │
│         │  │ 📖 │Mocking.. │Harper Lee│9780...   │Lit │2/5 │ 🟢   │  │
│         │  │ 📖 │人类简史  │赫拉利    │9787...   │His │3/4 │ 🟢   │  │
│         │  │ 📖 │算法导论  │Cormen    │9787...   │Tech│0/2 │ 🟡   │  │
│         │  │ 📖 │Pride..   │J. Austen │9780...   │Lit │3/4 │ 🟢   │  │
│         │  └────┴──────────┴──────────┴──────────┴────┴────┴──────┘ │
│         │                                      [Edit] [Del] 靠右对齐│
│         │  Showing 1-8 of 15    ← [1][2][3]... [5] →                │
└─────────┴───────────────────────────────────────────────────────────┘
```

---

## 1. 侧边栏 (Sidebar)

**容器:** `W8Nv3w` — 240 × 900px, 填充 `$bg-inverse` (`#0A0A0A`)
**布局:** vertical

### 1.1 Logo 区域
| 元素 | 属性 |
|------|------|
| **Logo Icon** (`TPTu7`) | "📚", Inter 22px, `$text-inverse` |
| **Logo Text** (`sUGPi`) | "LibraryOS", Inter 18px Bold 700, `$text-inverse` |

### 1.2 导航菜单 (Nav Items)
每个菜单项: padding [10,14], gap=12px, 圆角 10px, horizontal, alignItems=center
- 默认: Icon 16px `$text-muted`, Label Inter 13px `$text-muted` 400
- **Books 当前激活**: 填充 `$accent`, 文字 `$text-inverse` 600

| 菜单项 | 说明 |
|--------|------|
| 📊 Dashboard | — |
| 📖 Borrow/Return | — |
| **📚 Books** | 🟦 **当前激活** |
| 👥 Readers | — |
| 📈 Statistics | — |
| 💰 Fines | — |
| ⚙️ Settings | — |

### 1.3 底部用户信息
| 元素 | 属性 |
|------|------|
| **Avatar** (`Zy2VY`) | 32×32px, 圆角 999px, 填充 `$accent-light` |
| &nbsp;&nbsp;Initials (`LkHSt`) | "SA", Inter 11px SemiBold 600, `$accent` |
| **Name** (`hgsWQ`) | Inter 12px, `$text-inverse`, "System Admin" |

---

## 2. 主内容区 (Main Content)

**容器:** `c1R9JA` — 1200 × 907px
**布局:** vertical, 内边距 [32, 40], gap=24px

### 2.1 Header
**容器:** `V8ZhWi` — horizontal, justifyContent=space_between, alignItems=center

| 元素 | 属性 |
|------|------|
| **Page Title** (`Ush7F`) | Inter 24px Bold 700, `$text-primary`, "Books" |
| **Add Button** (`KYAyc`) | padding [10,20], 圆角 10px, 填充 `$accent`, horizontal, gap=8px |
| &nbsp;&nbsp;Plus Icon (`vf8EQ`) | "+", Inter 16px, `$text-inverse` |
| &nbsp;&nbsp;Add Label (`c9JR1N`) | "Add New Book", Inter 14px SemiBold 600, `$text-inverse` |

### 2.2 Toolbar (搜索 + 筛选)
**容器:** `ZiwZC` — horizontal, gap=12px, alignItems=center

#### 搜索框 (Search Box)
| 属性 | 值 |
|------|-----|
| 尺寸 | 360 × 44px |
| 圆角 | `$input-radius` (12px) |
| 填充 | `$bg-primary` (`#FFFFFF`) |
| 描边 | 1.5px solid `$border` |
| 内边距 | [10, 16] |
| 布局 | horizontal, gap=8px, alignItems=center |

| 元素 | 属性 |
|------|------|
| **Search Icon** (`SMUuy`) | "🔍", Inter 14px, `$text-muted` |
| **Placeholder** (`AHtpu`) | "Search by title, author or ISBN...", Inter 13px, `$text-muted` |

#### 分类筛选 (Category Filter)
| 属性 | 值 |
|------|-----|
| 尺寸 | 160 × 36px |
| 圆角 | 10px |
| 填充 | `$bg-primary` |
| 描边 | 1.5px solid `$border` |
| 布局 | horizontal, justifyContent=space_between, alignItems=center |

| 元素 | 属性 |
|------|------|
| **Filter Label** (`vOOYs`) | Inter 13px, `$text-secondary`, "All Categories" |
| **Arrow Down** (`KE0AV`) | "▼", Inter 10px, `$text-muted` |

#### 状态筛选 (Status Filter)
| 属性 | 值 |
|------|-----|
| 尺寸 | 140 × 36px |
| 样式 | 同分类筛选 |
| **Filter Label** | Inter 13px, `$text-secondary`, "All Status" |

---

### 2.3 数据表格 (Table)

**容器:** `M5wtld` — 1120 × 640px
**布局:** vertical, gap=2px
**填充:** `$bg-primary`, 圆角 `$card-radius`(16px), 描边 1px `$border`

#### 表头行 (Table Header)
**容器:** `HfgAk` — 1120 × 48px, padding [14,20], 填充 `$bg-secondary`

| 列 | 宽度 | 字体 | 对齐 |
|----|------|------|------|
| **Cover** | 48px | Inter 12px SemiBold 600, `$text-muted` | 左 |
| **Title** | 220px | 同上 | 左 |
| **Author** | 160px | 同上 | 左 |
| **ISBN** | 180px | 同上 | 左 |
| **Category** | 120px | 同上 | 左 |
| **Copies** | 70px | 同上 | 左 |
| **Status** | 100px | 同上 | 左 |
| **Spacer** | `fill_container` | — | 弹性撑满 |
| **Actions** | 100px | 同上 | **右对齐** |

#### 数据行 (每行)
**容器:** 每行 1120 × 72px, padding [12,20], horizontal, alignItems=center, 描边 0.5px `$border`

##### Cover 缩略图
- 尺寸: 36 × 48px, 圆角 6px
- 填充: `$accent-light` (`#E8F4FD`)
- Icon: "📖", Inter 16px, `$accent` (居中)

##### 数据列

| 列 | 字体 | 颜色 | 宽度 |
|----|------|------|------|
| Title | Inter 13px Medium 500 | `$text-primary` | 220px |
| Author | Inter 12px | `$text-secondary` | 160px |
| ISBN | **Geist Mono** 11px | `$text-muted` | 180px |
| Category | Inter 12px | `$text-secondary` | 120px |
| Copies | Inter 12px | `$text-secondary` | 70px |

##### Status 状态标签
**容器:** padding [4,10], 圆角 999px, alignItems=center, justifyContent=center, 宽度 100px
**文字:** Inter 11px Medium 500, `$text-inverse`

| 状态 | 填充色 |
|------|--------|
| 🟢 Available | `$success` (`#34D399`) |
| 🟡 Borrowed | `$warning` (`#FBBF24`) |
| 🔵 Reserved | `$accent` (`#4A9FD8`) |

##### Spacer (弹性撑满)
- 高度 40px, `fill_container` → 将 Actions 推到右侧

##### Actions 操作按钮 (靠右对齐)
**容器:** `H0eQo` — 100px, horizontal, gap=8px, alignItems=center, justifyContent=flex-end

| 按钮 | 属性 |
|------|------|
| **Edit** (`LCARs`) | padding [6,12], 圆角 6px, 填充 `$accent-light` |
| &nbsp;&nbsp;Label (`GbXYQ`) | Inter 11px Medium 500, `$accent`, "Edit" |
| **Delete** (`OKEep`) | padding [6,12], 圆角 6px, 填充 `rgba(248,113,113,0.1)` (10% 红色) |
| &nbsp;&nbsp;Label (`cBxCz`) | Inter 11px Medium 500, `$danger`, "Del" |

##### 示例数据 (8行)

| Title | Author | ISBN | Category | Copies | Status |
|-------|--------|------|----------|--------|--------|
| The Great Gatsby | F. Scott Fitzgerald | 9780743273565 | Literature | 4/5 | 🟢 Available |
| 1984 | George Orwell | 9780451524935 | Literature | 0/3 | 🟡 Borrowed |
| 百年孤独 | 加西亚·马尔克斯 | 9787544291163 | Literature | 1/3 | 🟡 Borrowed |
| 三体 | 刘慈欣 | 9787536692930 | Literature | 2/5 | 🟡 Borrowed |
| To Kill a Mockingbird | Harper Lee | 9780061120084 | Literature | 2/5 | 🟢 Available |
| 人类简史 | 尤瓦尔·赫拉利 | 9787508672069 | History | 3/4 | 🟢 Available |
| 算法导论 | Thomas H. Cormen | 9787115428028 | Technology | 0/2 | 🟡 Borrowed |
| Pride and Prejudice | Jane Austen | 9780141439518 | Literature | 3/4 | 🟢 Available |

---

### 2.4 分页 (Pagination)

**容器:** `xTxHu` — 1120 × 48px, horizontal, justifyContent=space_between, alignItems=center

| 元素 | 属性 |
|------|------|
| **Info** (`o1Y4g4`) | Inter 12px, `$text-muted`, "Showing 1-8 of 15 results" |
| **Page Buttons** (`V1Jer`) | horizontal, gap=4px, alignItems=center |

页码按钮:
| 元素 | 样式 |
|------|------|
| ← (Prev) | Inter 12px, `$text-muted` |
| **Page 1** 🟦 | 32×32px, 圆角 8px, **填充 `$accent`**, 文字 `$text-inverse` 600 |
| Page 2 | 32×32px, 圆角 8px, 填充 `$bg-primary`, 描边 1px `$border`, 文字 `$text-secondary` |
| Page 3 | 同上 |
| ... | Inter 12px, `$text-muted` |
| Page 5 | 同 Page 2 |
| → (Next) | Inter 12px, `$accent` |

---

## 表格列宽概览

```
| Cover(48) | Title(220) | Author(160) | ISBN(180) | Category(120) | Copies(70) | Status(100) | ← Spacer(fill) → | Actions(100)右对齐 |
```

总固定宽度: 48+220+160+180+120+70+100+100 = **998px**
Spacer 弹性撑满至父容器宽度 1120px

---

## 组件层级关系

```
AdminBooks (RNbkv) — 1440×900, horizontal
├── Sidebar (W8Nv3w) — 240×900, vertical
│   ├── LogoArea (nYvHx)
│   ├── NavItems (V2pCK) — 7 items, Books 激活态
│   └── BottomSection (WZixc)
│
└── MainContent (c1R9JA) — 1200×907, vertical
    ├── Header (V8ZhWi)
    │   ├── PageTitle (Ush7F) — "Books"
    │   └── AddButton (KYAyc) — [+ Add New Book]
    ├── Toolbar (ZiwZC) — search + 2 filters
    │   ├── SearchBox (wxYR2) — 🔍 + placeholder
    │   ├── CategoryFilter (i9vg6W) — dropdown
    │   └── StatusFilter (DJ5hv) — dropdown
    ├── Table (M5wtld) — 8 rows × 9 cols
    │   ├── TableHeader (HfgAk)
    │   ├── Row (G1ZGT) — The Great Gatsby
    │   ├── Row (p2Rtn) — 1984
    │   ├── Row (wQPfO) — 百年孤独
    │   ├── Row (KH5tV) — 三体
    │   ├── Row (JtLnU) — To Kill a Mockingbird
    │   ├── Row (cQ8D7) — 人类简史
    │   ├── Row (nTh76) — 算法导论
    │   └── Row (C0lSv) — Pride and Prejudice
    └── Pagination (xTxHu)
        ├── Info (o1Y4g4)
        └── PageButtons (V1Jer) — 7 page elements
```
