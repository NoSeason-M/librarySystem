# 输入框组件 - Input Fields

> **风格:** Soft Bento · Carbon Frost
> **所有输入框都是 reusable 组件**

---

## 通用规范

| 属性 | 值 |
|------|-----|
| 布局 | vertical，gap=6px |
| 宽度 | 300px (可通过实例覆盖) |
| Label 字体 | Inter, 13px, Medium 500 |
| Input 盒内边距 | [12, 16] |
| Input 圆角 | `$input-radius` (12px) |
| Placeholder/Value 字体 | Inter, 14px |

---

## 1. Input Default — 默认状态

**组件ID:** `UXBeu`

```text
┌──────────────────────────────────┐
│ Field Label                      │  ← Inter 13px Medium 500, #1A1A1A
┌──────────────────────────────────┐
│                                  │
│  Placeholder text                │  ← Inter 14px, #888888
│                                  │
└──────────────────────────────────┘
  填充: #F7F8FA  描边: 1.5px #E5E7EB
```

| 区域 | 属性 | 值 |
|------|------|-----|
| **Label** (`ZlSoy`) | 字体 | Inter 13px Medium 500 |
| | 颜色 | `$text-primary` (`#1A1A1A`) |
| | 内容 | "Field Label" |
| **Input Box** (`Nd0jI`) | 填充 | `$bg-secondary` (`#F7F8FA`) |
| | 描边 | 1.5px solid `$border` (`#E5E7EB`) |
| | 圆角 | `$input-radius` (12px) |
| | 内边距 | [12, 16] |
| **Placeholder** (`bx0kX`) | 字体 | Inter 14px |
| | 颜色 | `$text-muted` (`#888888`) |
| | 内容 | "Placeholder text" |

---

## 2. Input Focus — 聚焦状态

**组件ID:** `goprK`

```text
┌──────────────────────────────────┐
│ Field Label                      │  ← 颜色切换为 $accent (#4A9FD8)
┌──────────────────────────────────┐
│                                  │
│  Typing value...                 │  ← Inter 14px, #1A1A1A
│                                  │
└──────────────────────────────────┘
  填充: #FFFFFF  描边: 2px #4A9FD8
  Helper text goes here            ← Inter 11px, #888888
```

| 区域 | 属性 | 值 |
|------|------|-----|
| **Label** (`nBHLr`) | 字体 | Inter 13px Medium 500 |
| | 颜色 | `$accent` (`#4A9FD8`) — 聚焦时切换为强调色 |
| | 内容 | "Field Label" |
| **Input Box** (`wnunN`) | 填充 | `$bg-primary` (`#FFFFFF`) — 聚焦时切换为白色 |
| | 描边 | **2px** solid `$accent` (`#4A9FD8`) — 蓝色加粗描边 |
| | 圆角 | `$input-radius` (12px) |
| | 内边距 | [12, 16] |
| **Value** (`dz6gV`) | 字体 | Inter 14px |
| | 颜色 | `$text-primary` (`#1A1A1A`) |
| | 内容 | "Typing value..." |
| **Helper** (`LJgB7`) | 字体 | Inter 11px |
| | 颜色 | `$text-muted` (`#888888`) |
| | 内容 | "Helper text goes here" |

---

## 3. Input Error — 错误状态

**组件ID:** `nNujk`

```text
┌──────────────────────────────────┐
│ Field Label                      │  ← 颜色切换为 $danger (#F87171)
┌──────────────────────────────────┐
│                                  │
│  Invalid input                   │  ← Inter 14px, #1A1A1A
│                                  │
└──────────────────────────────────┘
  填充: #FFFFFF  描边: 2px #F87171
  This field is required           ← Inter 11px, #F87171
```

| 区域 | 属性 | 值 |
|------|------|-----|
| **Label** (`bkaoB`) | 字体 | Inter 13px Medium 500 |
| | 颜色 | `$danger` (`#F87171`) — 错误时切换为红色 |
| | 内容 | "Field Label" |
| **Input Box** (`qWcQ9`) | 填充 | `$bg-primary` (`#FFFFFF`) |
| | 描边 | **2px** solid `$danger` (`#F87171`) — 红色加粗描边 |
| | 圆角 | `$input-radius` (12px) |
| | 内边距 | [12, 16] |
| **Value** (`UQqlg`) | 字体 | Inter 14px |
| | 颜色 | `$text-primary` (`#1A1A1A`) |
| | 内容 | "Invalid input" |
| **Error** (`FboGb`) | 字体 | Inter 11px |
| | 颜色 | `$danger` (`#F87171`) |
| | 内容 | "This field is required" |

---

## 状态切换表

| 状态 | Label 颜色 | 背景色 | 描边色 | 描边粗细 | 底部文字 |
|------|-----------|--------|--------|---------|---------|
| **Default** | `$text-primary` | `$bg-secondary` | `$border` | 1.5px | 无 |
| **Focus** | `$accent` | `$bg-primary` | `$accent` | 2px | Helper text (`$text-muted`) |
| **Error** | `$danger` | `$bg-primary` | `$danger` | 2px | Error msg (`$danger`) |
| **Disabled** | `$text-muted` | `$bg-secondary` | `$border` | 1.5px | — (暂未实现) |

---

## 层级关系

```
Input Fields (document root, reusable)
├── Input Default (UXBeu) — 300px
│   ├── Label (ZlSoy)
│   └── Input Box (Nd0jI)
│       └── Placeholder (bx0kX)
├── Input Focus (goprK) — 300px
│   ├── Label (nBHLr)
│   ├── Input Box (wnunN)
│   │   └── Value (dz6gV)
│   └── Helper (LJgB7)
└── Input Error (nNujk) — 300px
    ├── Label (bkaoB)
    ├── Input Box (qWcQ9)
    │   └── Value (UQqlg)
    └── Error (FboGb)
```

---

## 代码复刻建议

```html
<!-- Default -->
<div class="field">
  <label class="field-label">Field Label</label>
  <input class="input-default" placeholder="Placeholder text" />
</div>

<!-- Focus -->
<div class="field">
  <label class="field-label field-label--focus">Field Label</label>
  <input class="input-focus" value="Typing value..." />
  <span class="field-helper">Helper text goes here</span>
</div>

<!-- Error -->
<div class="field">
  <label class="field-label field-label--error">Field Label</label>
  <input class="input-error" value="Invalid input" />
  <span class="field-error">This field is required</span>
</div>
```

```css
.field { display: flex; flex-direction: column; gap: 6px; width: 300px; }
.field-label { font: 500 13px Inter; color: #1A1A1A; }
.field-label--focus { color: #4A9FD8; }
.field-label--error { color: #F87171; }
.field-helper { font: 11px Inter; color: #888888; }
.field-error { font: 11px Inter; color: #F87171; }

input {
  padding: 12px 16px;
  border-radius: 12px;
  font: 14px Inter;
  outline: none;
}
.input-default {
  background: #F7F8FA;
  border: 1.5px solid #E5E7EB;
  color: #1A1A1A;
}
.input-default::placeholder { color: #888888; }
.input-focus {
  background: #FFFFFF;
  border: 2px solid #4A9FD8;
  color: #1A1A1A;
}
.input-error {
  background: #FFFFFF;
  border: 2px solid #F87171;
  color: #1A1A1A;
}
```
