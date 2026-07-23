# 状态标签组件 - Badges

> **风格:** Soft Bento · Carbon Frost
> **所有标签类型都是 reusable 组件**

---

## 概述

用于展示图书副本状态、业务状态码的小型胶囊标签。4 种颜色对应不同业务语义。

```text
┌──────────────┐
│   Available  │  ← $success (#34D399) — 可用/成功
├──────────────┤
│    Pending   │  ← $warning (#FBBF24) — 等待/警告
├──────────────┤
│   Overdue    │  ← $danger (#F87171) — 逾期/危险
├──────────────┤
│   Reserved   │  ← $accent (#4A9FD8) — 预约/信息
└──────────────┘
```

---

## 通用属性

| 属性 | 值 |
|------|-----|
| 布局 | horizontal, alignItems=center, justifyContent=center |
| 内边距 | [4, 10] |
| 间距 (gap) | 6px |
| 圆角 | 999px (全圆角胶囊) |
| 文字字体 | Inter 11px, Medium 500 |
| 文字颜色 | `$text-inverse` (`#FFFFFF`) |

---

## 1. Status Badge — 成功/可用

**组件ID:** `XhcIm`
**填充色:** `$success` (`#34D399`)

| 元素 | 属性 | 值 |
|------|------|-----|
| **填充** | — | `#34D399` |
| **文字** (`xrDmS`) | 内容 | "Available" |
| | 字体 | Inter 11px Medium 500 |
| | 颜色 | `#FFFFFF` |

**业务映射:** 在馆、可用、已归还、已缴、已通过

---

## 2. Warning Badge — 等待/警告

**组件ID:** `JC3vc`
**填充色:** `$warning` (`#FBBF24`)

| 元素 | 属性 | 值 |
|------|------|-----|
| **填充** | — | `#FBBF24` |
| **文字** (`StqZs`) | 内容 | "Pending" |
| | 字体 | Inter 11px Medium 500 |
| | 颜色 | `#FFFFFF` |

**业务映射:** 等待中、待取书、待审核、部分逾期

---

## 3. Danger Badge — 逾期/危险

**组件ID:** `tFDAk`
**填充色:** `$danger` (`#F87171`)

| 元素 | 属性 | 值 |
|------|------|-----|
| **填充** | — | `#F87171` |
| **文字** (`WAAqM`) | 内容 | "Overdue" |
| | 字体 | Inter 11px Medium 500 |
| | 颜色 | `#FFFFFF` |

**业务映射:** 逾期、丢失、损坏、禁用、失败

---

## 4. Accent Badge — 预约/信息

**组件ID:** `Adbpu`
**填充色:** `$accent` (`#4A9FD8`)

| 元素 | 属性 | 值 |
|------|------|-----|
| **填充** | — | `#4A9FD8` |
| **文字** (`O7RqL`) | 内容 | "Reserved" |
| | 字体 | Inter 11px Medium 500 |
| | 颜色 | `#FFFFFF` |

**业务映射:** 预约中、维修中、进行中

---

## 颜色映射表

| Token | 颜色值 | Badge | 业务语义 |
|-------|--------|-------|---------|
| `$success` | `#34D399` | 🟢 **Available** | 在馆、可用、已归还、已缴 |
| `$warning` | `#FBBF24` | 🟡 **Pending** | 等待中、待取书、待审核 |
| `$danger` | `#F87171` | 🔴 **Overdue** | 逾期、丢失、损坏、禁用 |
| `$accent` | `#4A9FD8` | 🔵 **Reserved** | 预约中、维修中、进行中 |
| *(还可扩展)* | | 灰色标签 | 下架、已取消、草稿 (用 `$text-muted` 背景) |

---

## 层级关系

```
Badges (document root, reusable)
├── Status Badge (XhcIm) — bg #34D399
│   └── Label (xrDmS) — "Available"
├── Warning Badge (JC3vc) — bg #FBBF24
│   └── Label (StqZs) — "Pending"
├── Danger Badge (tFDAk) — bg #F87171
│   └── Label (WAAqM) — "Overdue"
└── Accent Badge (Adbpu) — bg #4A9FD8
    └── Label (O7RqL) — "Reserved"
```

---

## 代码复刻建议

```html
<span class="badge badge--success">Available</span>
<span class="badge badge--warning">Pending</span>
<span class="badge badge--danger">Overdue</span>
<span class="badge badge--accent">Reserved</span>
```

```css
.badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  font: 500 11px Inter;
  color: #FFFFFF;
  white-space: nowrap;
}
.badge--success { background: #34D399; }
.badge--warning { background: #FBBF24; }
.badge--danger  { background: #F87171; }
.badge--accent  { background: #4A9FD8; }
```
