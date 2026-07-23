# 统计卡片组件 - Stat Card

> **风格:** Soft Bento · Carbon Frost
> **组件ID:** `cWzlU` | **reusable:** true

---

## 概述

数据仪表盘使用的统计卡片，显示指标标签、数值、趋势变化。用于读者中心和管理后台。

```text
┌──────────────────────────┐
│  Total Stat              │  ← Inter 12px, #888888
│                          │
│  12,345                  │  ← Geist Mono 30px Bold 700, #1A1A1A
│                          │
│  ↑ +12.5% from last month│  ← Inter 12px, #34D399 (绿色/上升)
└──────────────────────────┘
  220px
```

---

## 属性明细

| 属性 | 值 |
|------|-----|
| 宽度 | 220px (固定) |
| 布局 | vertical, gap=6px |
| 内边距 | 20px (四周统一) |
| 填充 | `$bg-primary` (`#FFFFFF`) |
| 圆角 | `$card-radius` (16px) |
| 描边 | 1px solid `$border` (`#E5E7EB`) |

---

### 子元素明细

| 元素 | ID | 属性 | 值 |
|------|-----|------|-----|
| **Label** | `RVznL` | 字体 | Inter 12px |
| | | 颜色 | `$text-muted` (`#888888`) |
| | | 内容 | "Total Stat" |
| **Value** | `SP5uE` | 字体 | **Geist Mono** 30px **Bold 700** |
| | | 颜色 | `$text-primary` (`#1A1A1A`) |
| | | 内容 | "12,345" |
| **Trend Row** | `B2wyA3` | 布局 | horizontal, gap=6px, alignItems=center |
| &nbsp;&nbsp;Arrow | `k67tmY` | 字体 | Inter 12px |
| | | 颜色 | `$success` (`#34D399`) — 上升趋势绿 |
| | | 内容 | "↑" |
| &nbsp;&nbsp;Trend | `TvaAI` | 字体 | Inter 12px |
| | | 颜色 | `$success` (`#34D399`) |
| | | 内容 | "+12.5% from last month" |

### 趋势颜色方案

| 趋势 | 颜色 Token | 颜色值 | 箭头 |
|------|-----------|--------|------|
| 上升 (↑) | `$success` | `#34D399` | "↑" |
| 下降 (↓) | `$danger` | `#F87171` | "↓" (实例化时覆盖) |
| 持平 (→) | `$text-muted` | `#888888` | "→" (实例化时覆盖) |

---

## 实例化用法

```js
// 基本用法
Insert(parent, {
  type: "ref",
  ref: "cWzlU",
  name: "Total Books",
  descendants: {
    "RVznL": { content: "Total Books" },
    "SP5uE": { content: "15,234" },
    "TvaAI": { content: "+128 this week" },
    "k67tmY": { content: "↑", fill: "$accent" }
  }
})

// 危险趋势 (下降)
Insert(parent, {
  type: "ref",
  ref: "cWzlU",
  name: "Overdue",
  descendants: {
    "RVznL": { content: "Overdue" },
    "SP5uE": { content: "23" },
    "TvaAI": { content: "-5 this week", fill: "$danger" },
    "k67tmY": { content: "↓", fill: "$danger" }
  }
})
```

---

## 层级关系

```
Stat Card (cWzlU) — 220px, reusable
├── Label (RVznL) — Inter 12px, #888888
├── Value (SP5uE) — Geist Mono 30px Bold 700, #1A1A1A
└── Trend Row (B2wyA3) — horizontal, gap=6
    ├── Arrow (k67tmY) — "↑" or "↓" or "→"
    └── Trend (TvaAI) — "+12.5% from last month"
```

---

## 代码复刻建议

```html
<div class="stat-card">
  <p class="stat-label">Total Books</p>
  <p class="stat-value">15,234</p>
  <div class="stat-trend">
    <span class="trend-arrow trend-up">↑</span>
    <span class="trend-text trend-up">+128 this week</span>
  </div>
</div>
```

```css
.stat-card {
  width: 220px;
  padding: 20px;
  background: #FFFFFF;
  border-radius: 16px;
  border: 1px solid #E5E7EB;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.stat-label {
  font: 12px Inter;
  color: #888888;
  margin: 0;
}
.stat-value {
  font: 700 30px 'Geist Mono', monospace;
  color: #1A1A1A;
  margin: 0;
}
.stat-trend {
  display: flex;
  gap: 6px;
  align-items: center;
}
.trend-arrow { font: 12px Inter; }
.trend-text { font: 12px Inter; }
.trend-up { color: #34D399; }
.trend-down { color: #F87171; }
```
