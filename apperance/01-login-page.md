# 登录页 - Login Page

> **设计风格:** Soft Bento · Carbon Frost
> **页面尺寸:** 1440 × 900 px
> **背景色:** `#F7F8FA` (`$bg-secondary`)

---

## 全局设计 Token

| Token | 值 | 用途 |
|-------|-----|------|
| `$bg-primary` | `#FFFFFF` | 卡片/输入框背景 |
| `$bg-secondary` | `#F7F8FA` | 页面背景 |
| `$bg-inverse` | `#0A0A0A` | 左侧深色面板 |
| `$text-primary` | `#1A1A1A` | 主文字色 |
| `$text-secondary` | `#666666` | 次要文字色 |
| `$text-muted` | `#888888` | 弱化文字/占位符 |
| `$text-inverse` | `#FFFFFF` | 深色背景上的文字 |
| `$accent` | `#4A9FD8` | 蓝色强调色（按钮/链接/激活态） |
| `$accent-light` | `#E8F4FD` | 浅蓝背景（验证码/标签） |
| `$border` | `#E5E7EB` | 边框色 |
| `$card-radius` | `16px` | 卡片圆角 |
| `$input-radius` | `12px` | 输入框圆角 |
| `$button-radius` | `10px` | 按钮圆角 |

---

## 布局结构

```
┌──────────────────────────────────────────────────────────┐
│  Left Panel (560px)              Right Panel (880px)     │
│  ┌──────────────────┐           ┌─────────────────────┐  │
│  │ Logo + Tagline    │           │                     │  │
│  │                   │           │   Form Card         │  │
│  │  Quote Area       │           │   ┌─────────────┐   │  │
│  │  "A library..."   │           │   │ Welcome back │   │  │
│  │                   │           │   │ Username     │   │  │
│  │                   │           │   │ Password     │   │  │
│  │                   │           │   │ Captcha      │   │  │
│  │                   │           │   │ Remember me  │   │  │
│  │                   │           │   │ Sign In btn  │   │  │
│  │  Footer           │           │   │ Register     │   │  │
│  └──────────────────┘           │   └─────────────┘   │  │
│                                  │                     │  │
│                                  │   Demo Accounts     │  │
│                                  └─────────────────────┘  │
└──────────────────────────────────────────────────────────┘
```

---

## 左侧面板 (Left Panel)

**容器:** `czleX` — 宽 560px，高 `fill_container`(900px)
**布局:** vertical，`justifyContent: space_between`
**内边距:** 60px（四边统一）
**填充:** `$bg-inverse` (`#0A0A0A`)

### 1.1 顶部区域 (Panel Top)
- **间距 (gap):** 8px
- **位置:** x=60, y=60

| 元素 | 属性 | 值 |
|------|------|-----|
| **Logo** (`IDs62`) | 字体 | Inter, 24px, Bold 700 |
| | 颜色 | `$text-inverse` (`#FFFFFF`) |
| | 内容 | "📚 LibraryOS" |
| | 尺寸 | 149 × 41px |
| **Tagline** (`z9EP6u`) | 字体 | Inter, 15px, Regular 400 |
| | 颜色 | `$text-muted` (`#888888`) |
| | 内容 | "Next-generation library management system" |
| | 尺寸 | 317 × 18px |

### 1.2 引言区域 (Quote Area)
- **间距 (gap):** 16px
- **位置:** x=60, y=378

| 元素 | 属性 | 值 |
|------|------|-----|
| **Quote1** (`xYXkB`) | 字体 | Inter, 36px, Medium 500 |
| | 颜色 | `$text-inverse` (`#FFFFFF`) |
| | 内容 | "\"A library is not a" |
| | 尺寸 | 400 × 44px |
| **Quote2** (`h3IUg`) | 字体 | Inter, 36px, Medium 500 |
| | 颜色 | `$text-inverse` (`#FFFFFF`) |
| | 内容 | "luxury but one of the" |
| | 尺寸 | 400 × 44px |
| **Quote3** (`d8eX2s`) | 字体 | Inter, 36px, Medium 500 |
| | 颜色 | `$accent` (`#4A9FD8`) — 强调色醒目标识 |
| | 内容 | "necessities of life.\"" |
| | 尺寸 | 400 × 44px |
| **Quote Author** (`gfUMH`) | 字体 | Inter, 13px, Regular 400 |
| | 颜色 | `$text-muted` (`#888888`) |
| | 内容 | "— Henry Ward Beecher" |

### 1.3 底部页脚 (Panel Bottom)
- **位置:** x=60, y=825

| 元素 | 属性 | 值 |
|------|------|-----|
| **Footer** (`d1Bbbm`) | 字体 | Inter, 12px |
| | 颜色 | `$text-muted` (`#888888`) |
| | 内容 | "© 2026 LibraryOS. All rights reserved." |
| | 尺寸 | 218 × 15px |

---

## 右侧面板 (Right Panel)

**容器:** `XI89o` — 宽 880px (剩余空间)
**布局:** vertical, `alignItems: center`, `justifyContent: center`
**间距 (gap):** 32px

### 2.1 表单卡片 (Form Card)

**容器:** `illJq` — 宽 400px
**布局:** vertical, gap=24px
**填充:** `$bg-primary` (`#FFFFFF`)
**圆角:** `$card-radius` (16px)
**内边距:** 40px（四周）
**位置:** x=240(居中), y=141.5

| 区域 | 间距 (gap) | 属性 |
|------|-----------|------|
| **Form Header** | 6px | — |
| **Username Field** | 6px | `width: fill_container` |
| **Password Field** | 6px | `width: fill_container` |
| **Captcha Field** | 6px | `width: fill_container` |
| **Options Row** | — | `width: fill_container`, justifyContent: space_between |
| **Login Button** | — | `width: fill_container`, padding: [12,24] |
| **Register Row** | 4px | `width: fill_container`, justifyContent: center |

#### Form Header (`n4QU2`)
- **间距 (gap):** 6px
- **位置:** x=40, y=40

| 元素 | 属性 | 值 |
|------|------|-----|
| **Title** (`KQVqP`) | 字体 | Inter, 24px, SemiBold 600 |
| | 颜色 | `$text-primary` (`#1A1A1A`) |
| | 内容 | "Welcome back" |
| | 尺寸 | 171 × 29px |
| **Subtitle** (`qOurF`) | 字体 | Inter, 14px |
| | 颜色 | `$text-secondary` (`#666666`) |
| | 内容 | "Sign in to access the library system" |
| | 尺寸 | 235 × 17px |

#### Username Field (`AKU0K`)
- **位置:** x=40, y=116
- **布局:** vertical, gap=6px

| 元素 | 属性 | 值 |
|------|------|-----|
| **Label** (`zf7tR`) | 字体 | Inter, 13px, Medium 500 |
| | 颜色 | `$text-primary` (`#1A1A1A`) |
| | 内容 | "Username" |
| **Input** (`cXDQv`) | 尺寸 | 320 × 41px(含padding) |
| | 内边距 | [12, 16] |
| | 圆角 | `$input-radius` (12px) |
| | 填充 | `$bg-secondary` (`#F7F8FA`) |
| | 描边 | 1.5px solid `$border` (`#E5E7EB`) |
| **Placeholder** (`UV4gT`) | 字体 | Inter, 14px |
| | 颜色 | `$text-muted` (`#888888`) |
| | 内容 | "Enter your username" |

#### Password Field (`uj5yv`)
- **位置:** x=40, y=203
- 与 Username Field 结构一致

| 元素 | 属性 | 值 |
|------|------|-----|
| **Label** | 内容 | "Password" |
| **Input** | 尺寸 | 320 × 41px |
| **Placeholder** | 内容 | "••••••••" |

#### Captcha Field (`GFqgO`)
- **位置:** x=40, y=290
- **布局:** vertical, gap=6px
- **验证码输入行 (Captcha Row):** horizontal, gap=12px

| 元素 | 属性 | 值 |
|------|------|-----|
| **Label** | 内容 | "Verification Code" |
| **Captcha Input** | 宽度 | 160px |
| | 圆角 | `$input-radius` (12px) |
| | 填充 | `$bg-secondary` |
| **Captcha Image** (`NqSAE`) | 高度 | 45px |
| | 填充 | `$accent-light` (`#E8F4FD`) |
| | 圆角 | 10px |
| **Code Text** (`c2hdwm`) | 字体 | Geist Mono, 20px, Bold 700 |
| | 颜色 | `$accent` (`#4A9FD8`) |
| | 间距 (letterSpacing) | 6px |
| | 内容 | "A8K2" |

#### Options Row (`QoAtP`)
- **位置:** x=40, y=381
- **布局:** horizontal, justifyContent: space_between

| 元素 | 属性 | 值 |
|------|------|-----|
| **Remember Me** | 布局 | horizontal, gap=8px, alignItems: center |
| &nbsp;&nbsp;Checkbox (`AdQIH`) | 尺寸 | 18 × 18px, 圆角 4px, 填充 `$accent` |
| &nbsp;&nbsp;Check mark (`lWqHY`) | 内容 "✓", 颜色 `$text-inverse` |
| &nbsp;&nbsp;Label (`GLgB9`) | Inter, 13px, `$text-secondary`, "Remember me" |
| **Forgot Password** (`YjFHa`) | Inter, 13px, Medium 500, `$accent`, "Forgot password?" |

#### Login Button (`MEIAk`)
- **位置:** x=40, y=423
- **尺寸:** 320 × 42px
- **圆角:** 10px
- **填充:** `$accent` (`#4A9FD8`)

| 元素 | 属性 | 值 |
|------|------|-----|
| **Button Text** (`d6E7O`) | 字体 | Inter, 15px, SemiBold 600 |
| | 颜色 | `$text-inverse` (`#FFFFFF`) |
| | 内容 | "Sign In" |

#### Register Row (`N1lfh`)
- **位置:** x=40, y=489

| 元素 | 属性 | 值 |
|------|------|-----|
| **Question** (`EshG8`) | Inter, 13px, `$text-muted` | "Don't have an account?" |
| **Link** (`NO7H8`) | Inter, 13px, Medium 500, `$accent` | "Register here" |

### 2.2 Demo 账号提示 (Demo Hint)

**容器:** `es9h4` — 宽 737px, 高 40px
**布局:** horizontal, gap=16px, alignItems: center
**位置:** x=71.5, y=718.5

| 元素 | 属性 | 值 |
|------|------|-----|
| **Hint Label** (`TuwHn`) | Inter, 12px, `$text-muted` | "Demo accounts:" |
| **Admin Badge** (`d1GRYz`) | 圆角 999px, `$bg-primary`, 描边 `$border` | "Admin: admin / admin123" |
| **Librarian Badge** (`xIxAI`) | 同上 | "Librarian: librarian / admin123" |
| **Reader Badge** (`XPchg`) | 同上 | "Reader: reader01 / admin123" |

Badge 通用样式: padding [5,10], font Geist Mono 11px, color `$text-secondary`

---

## 组件层级关系

```
LoginPage (xBdgc)
├── LeftPanel (czleX) — 560×900
│   ├── PanelTop (lRvyG)
│   │   ├── Logo (IDs62)
│   │   └── Tagline (z9EP6u)
│   ├── QuoteArea (cefK0)
│   │   ├── Quote1 (xYXkB)
│   │   ├── Quote2 (h3IUg)
│   │   ├── Quote3 (d8eX2s)
│   │   └── QuoteAuthor (gfUMH)
│   └── PanelBottom (GTIM1)
│       └── Footer (d1Bbbm)
│
├── RightPanel (XI89o) — 880×900
│   ├── FormCard (illJq) — 400px
│   │   ├── FormHeader (n4QU2)
│   │   │   ├── Title (KQVqP)
│   │   │   └── Subtitle (qOurF)
│   │   ├── UsernameField (AKU0K)
│   │   ├── PasswordField (uj5yv)
│   │   ├── CaptchaField (GFqgO)
│   │   ├── OptionsRow (QoAtP)
│   │   ├── LoginButton (MEIAk)
│   │   └── RegisterRow (N1lfh)
│   └── DemoHint (es9h4)
```
