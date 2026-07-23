# 按钮组件 - Buttons

> **风格:** Soft Bento · Carbon Frost
> **所有按钮都是 reusable 组件**

---

## 通用规范

| 属性 | 值 |
|------|-----|
| 布局 | horizontal, alignItems=center, justifyContent=center |
| 内边距 | [12, 24] (上下/左右) |
| 间距 (gap) | 8px (图标与文字之间) |
| 圆角 | `$button-radius` (10px) |
| 字体 | Inter, 14px |

---

## 1. Btn Primary — 主要操作按钮

**组件ID:** `RX5zK`
**用途:** 核心操作（登录、提交、借书、确认等）

| 属性 | 值 |
|------|-----|
| 填充 | `$accent` (`#4A9FD8`) |
| 文字颜色 | `$text-inverse` (`#FFFFFF`) |
| 字重 | SemiBold 600 |

```text
┌──────────────────────┐
│       Primary        │  ← 填充: $accent (#4A9FD8)
│                      │     文字: #FFFFFF SemiBold 600
└──────────────────────┘
   padding [12, 24px]
```

---

## 2. Btn Secondary — 次要操作按钮

**组件ID:** `G0TSa`
**用途:** 次要/备选操作，与 Primary 成对出现

| 属性 | 值 |
|------|-----|
| 填充 | `$bg-primary` (`#FFFFFF`) |
| 描边 | 1.5px solid `$border` (`#E5E7EB`) |
| 文字颜色 | `$text-secondary` (`#666666`) |
| 字重 | Medium 500 |

```text
┌──────────────────────┐
│      Secondary       │  ← 填充: #FFFFFF
│ ┌ ─ ─ ─ ─ ─ ─ ─ ─ ┤     描边: 1.5px #E5E7EB
└──────────────────────┘     文字: #666666 Medium 500
```

---

## 3. Btn Danger — 危险操作按钮

**组件ID:** `G8UQq`
**用途:** 删除、取消、罚款等警示操作

| 属性 | 值 |
|------|-----|
| 填充 | `$danger` (`#F87171`) |
| 文字颜色 | `$text-inverse` (`#FFFFFF`) |
| 字重 | SemiBold 600 |

```text
┌──────────────────────┐
│        Danger        │  ← 填充: $danger (#F87171)
│                      │     文字: #FFFFFF SemiBold 600
└──────────────────────┘
```

---

## 4. Btn Ghost — 幽灵按钮

**组件ID:** `PBBKA`
**用途:** 轻量级操作（取消、链接式操作），无填充背景

| 属性 | 值 |
|------|-----|
| 填充 | 无 |
| 文字颜色 | `$accent` (`#4A9FD8`) |
| 字重 | Medium 500 |

```text
┌──────────────────────┐
│        Ghost         │  ← 无背景填充
│                      │     文字: #4A9FD8 Medium 500
└──────────────────────┘
```

---

## 层级关系

```
Buttons (document root, reusable)
├── Btn Primary (RX5zK)
│   └── Label (yHEHS) — "Primary"
├── Btn Secondary (G0TSa)
│   └── Label (jQDN8) — "Secondary"
├── Btn Danger (G8UQq)
│   └── Label (cSlik) — "Danger"
└── Btn Ghost (PBBKA)
    └── Label (aDXyd) — "Ghost"
```

---

## 代码复刻建议

```css
.btn-primary {
  background: #4A9FD8;
  color: #FFFFFF;
  font: 600 14px/1 Inter;
  padding: 12px 24px;
  border-radius: 10px;
  border: none;
}
.btn-secondary {
  background: #FFFFFF;
  color: #666666;
  font: 500 14px/1 Inter;
  padding: 12px 24px;
  border-radius: 10px;
  border: 1.5px solid #E5E7EB;
}
.btn-danger {
  background: #F87171;
  color: #FFFFFF;
  font: 600 14px/1 Inter;
  padding: 12px 24px;
  border-radius: 10px;
  border: none;
}
.btn-ghost {
  background: transparent;
  color: #4A9FD8;
  font: 500 14px/1 Inter;
  padding: 12px 24px;
  border-radius: 10px;
  border: none;
}
```
