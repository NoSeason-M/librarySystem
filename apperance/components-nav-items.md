# 导航项组件 - Nav Items

> **风格:** Soft Bento · Carbon Frost
> **组件ID:** `Gpb9p` (默认) / `AFKAx` (激活态) | **reusable:** true

---

## 概述

管理后台侧边栏使用的导航菜单项，分为默认态和激活态两种。

```text
┌────────────────────────────┐
│  ○  Menu Item              │  ← 默认态
│                            │     填充: 无, 文字: #666666
│                            │     图标: ○ #888888
└────────────────────────────┘

┌────────────────────────────┐
│  ●  Menu Item              │  ← 激活态
│                            │     填充: $accent (#4A9FD8)
│                            │     文字: #FFFFFF SemiBold 600
│                            │     图标: ● #FFFFFF
└────────────────────────────┘
  200px
```

---

## 1. Nav Item — 默认状态

**组件ID:** `Gpb9p`

| 属性 | 值 |
|------|-----|
| 宽度 | 200px |
| 布局 | horizontal, gap=12px, alignItems=center |
| 内边距 | [10, 16] |
| 圆角 | 10px |

| 元素 | ID | 属性 | 值 |
|------|-----|------|-----|
| **Icon** | `KTSRG` | 字体 | Inter 18px |
| | | 颜色 | `$text-secondary` (`#666666`) |
| | | 内容 | "○" |
| **Label** | `gpKof` | 字体 | Inter 14px |
| | | 颜色 | `$text-secondary` (`#666666`) |
| | | 内容 | "Menu Item" |

---

## 2. Nav Item Active — 激活状态

**组件ID:** `AFKAx`

| 属性 | 值 |
|------|-----|
| 宽度 | 200px |
| 布局 | horizontal, gap=12px, alignItems=center |
| 内边距 | [10, 16] |
| 圆角 | 10px |
| **填充** | **`$accent` (`#4A9FD8`)** — 蓝色高亮 |

| 元素 | ID | 属性 | 值 |
|------|-----|------|-----|
| **Icon** | `y7SsO` | 字体 | Inter 18px |
| | | 颜色 | `$text-inverse` (`#FFFFFF`) |
| | | 内容 | "●" |
| **Label** | `dGVl2` | 字体 | Inter 14px **SemiBold 600** |
| | | 颜色 | `$text-inverse` (`#FFFFFF`) |
| | | 内容 | "Menu Item" |

---

## 激活态 vs 默认态差异

| 特性 | 默认态 | 激活态 |
|------|--------|--------|
| 背景填充 | 无 | `$accent` (`#4A9FD8`) |
| 图标 | "○" `#666666` | "●" `#FFFFFF` |
| 文字颜色 | `#666666` | `#FFFFFF` |
| 字重 | 400 (Regular) | 600 (SemiBold) |
| 圆角 | 10px | 10px |

---

## 层级关系

```
Nav Items (document root, reusable)
├── Nav Item — Default (Gpb9p) — 200px
│   ├── Icon (KTSRG) — ○ #666666
│   └── Label (gpKof) — "Menu Item" #666666
└── Nav Item — Active (AFKAx) — 200px, bg #4A9FD8
    ├── Icon (y7SsO) — ● #FFFFFF
    └── Label (dGVl2) — "Menu Item" #FFFFFF 600
```

---

## 代码复刻建议

```html
<!-- Default -->
<a class="nav-item" href="#">
  <span class="nav-icon">○</span>
  <span class="nav-label">Dashboard</span>
</a>

<!-- Active -->
<a class="nav-item nav-item--active" href="#">
  <span class="nav-icon">●</span>
  <span class="nav-label">Dashboard</span>
</a>
```

```css
.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  border-radius: 10px;
  text-decoration: none;
  width: 200px;
  transition: all 0.15s ease;
}
.nav-icon {
  font: 18px Inter;
  color: #666666;
  width: 20px;
  text-align: center;
}
.nav-label {
  font: 14px Inter;
  color: #666666;
}
.nav-item--active {
  background: #4A9FD8;
}
.nav-item--active .nav-icon { color: #FFFFFF; }
.nav-item--active .nav-label {
  color: #FFFFFF;
  font-weight: 600;
}
/* Hover effect */
.nav-item:hover:not(.nav-item--active) {
  background: rgba(74, 159, 216, 0.08);
}
```
