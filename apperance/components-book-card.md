# 图书卡片组件 - Book Card

> **风格:** Soft Bento · Carbon Frost
> **组件ID:** `b0Gu3d` | **reusable:** true

---

## 概述

图书搜索结果页和热门推荐区使用的图书展示卡片，包含封面区、书名、作者、评分标签和可借标签。

```text
┌────────────────────┐
│ ┌────────────────┐ │
│ │                │ │
│ │     📖         │ │  封面区 (180px)
│ │    40px        │ │  填充: $accent-light (#E8F4FD)
│ │                │ │  圆角: 14px 14px 0 0
│ └────────────────┘ │
│                     │
│  Book Title         │  Inter 14px SemiBold 600
│  Author Name        │  Inter 12px, #666666
│                     │
│  [★ 4.5] [3/5]     │  评分(黄) + 可借(绿) 标签
│                     │
└────────────────────┘
  200px
```

---

## 属性明细

| 属性 | 值 |
|------|-----|
| 宽度 | 200px (固定) |
| 布局 | vertical, gap=10px |
| 填充 | `$bg-primary` (`#FFFFFF`) |
| 圆角 | `$card-radius` (16px) |
| 描边 | 1px solid `$border` (`#E5E7EB`) |

---

### 1. Cover — 封面区

**ID:** `IQ629`
**尺寸:** `fill_container` × 180px

| 属性 | 值 |
|------|-----|
| 圆角 | [14, 14, 0, 0] (上圆下直) |
| 填充 | `$accent-light` (`#E8F4FD`) |
| 布局 | alignItems=center, justifyContent=center |

**图标:** `gwr8s` — "📖", Inter 40px, `$accent` (`#4A9FD8`), 居中

---

### 2. Info — 信息区

**ID:** `l0hYB`
**尺寸:** `fill_container`, 83px
**内边距:** [12, 14]
**布局:** vertical, gap=4px

| 子元素 | ID | 属性 | 值 |
|--------|-----|------|-----|
| **Title** | `UbxEl` | 字体 | Inter 14px SemiBold 600 |
| | | 颜色 | `$text-primary` (`#1A1A1A`) |
| | | 文字 | "Book Title" |
| | | 换行 | textGrowth="fixed-width", width="fill_container" |
| **Author** | `z8hlhn` | 字体 | Inter 12px |
| | | 颜色 | `$text-secondary` (`#666666`) |
| | | 文字 | "Author Name" |
| **Meta1** | `fLmz4` | 布局 | horizontal, gap=8px |

#### 评分标签 (Rating Tag)

**ID:** `uWgIN`
| 属性 | 值 |
|------|-----|
| 填充 | `$warning` (`#FBBF24`) |
| 圆角 | 999px |
| 内边距 | [3, 8] |

**Rating 文字:** `h1dsm` — Inter 11px Medium 500, `$text-inverse`, "★ 4.5"

#### 可借标签 (Avail Tag)

**ID:** `FGEL6`
| 属性 | 值 |
|------|-----|
| 填充 | `$success` (`#34D399`) |
| 圆角 | 999px |
| 内边距 | [3, 8] |

**Avail 文字:** `BXAIK` — Inter 11px Medium 500, `$text-inverse`, "3/5"

---

## 实例化用法

```js
// 在页面中实例化并覆盖内容
Insert(parent, {
  type: "ref",
  ref: "b0Gu3d",
  name: "Gatsby",
  descendants: {
    "UbxEl": { content: "The Great Gatsby" },
    "z8hlhn": { content: "F. Scott Fitzgerald" },
    "h1dsm": { content: "★ 4.5" },
    "BXAIK": { content: "4/5" }
  }
})
```

---

## 层级关系

```
Book Card (b0Gu3d) — 200px, reusable
├── Cover (IQ629) — fill_container × 180px
│   └── Icon (gwr8s) — 📖 40px
├── Info (l0hYB) — vertical, gap=4, padding [12,14]
│   ├── Title (UbxEl) — Inter 14px SemiBold 600
│   ├── Author (z8hlhn) — Inter 12px, #666666
│   └── Meta1 (fLmz4) — horizontal, gap=8
│       ├── Rating Tag (uWgIN) — bg #FBBF24, pill
│       │   └── Rating (h1dsm) — "★ 4.5"
│       └── Avail Tag (FGEL6) — bg #34D399, pill
│           └── Avail (BXAIK) — "3/5"
```

---

## 代码复刻建议

```html
<div class="book-card">
  <div class="book-cover">
    <span class="book-icon">📖</span>
  </div>
  <div class="book-info">
    <h3 class="book-title">The Great Gatsby</h3>
    <p class="book-author">F. Scott Fitzgerald</p>
    <div class="book-meta">
      <span class="badge badge--warning">★ 4.5</span>
      <span class="badge badge--success">3/5</span>
    </div>
  </div>
</div>
```

```css
.book-card {
  width: 200px;
  background: #FFFFFF;
  border-radius: 16px;
  border: 1px solid #E5E7EB;
  overflow: hidden;
}
.book-cover {
  height: 180px;
  background: #E8F4FD;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
}
.book-info {
  padding: 12px 14px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.book-title {
  font: 600 14px Inter;
  color: #1A1A1A;
  margin: 0;
}
.book-author {
  font: 12px Inter;
  color: #666666;
  margin: 0;
}
.book-meta { display: flex; gap: 8px; }
.badge {
  padding: 3px 8px;
  border-radius: 999px;
  font: 500 11px Inter;
  color: #FFFFFF;
}
.badge--warning { background: #FBBF24; }
.badge--success { background: #34D399; }
```
