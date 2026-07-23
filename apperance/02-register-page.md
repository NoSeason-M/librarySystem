# 注册页 - Register Page

> **设计风格:** Soft Bento · Carbon Frost
> **页面尺寸:** 1440 × 900 px
> **背景色:** `#F7F8FA` (`$bg-secondary`)

---

## 布局结构

```
┌──────────────────────────────────────────────────────────┐
│  Left Panel (560px)              Right Panel (880px)     │
│  ┌──────────────────┐           ┌─────────────────────┐  │
│  │ Logo + Tagline    │           │                     │  │
│  │                   │           │   Form Card         │  │
│  │ Benefits List     │           │   ┌─────────────┐   │  │
│  │ 🔍 Search Books   │           │   │ Create an    │   │  │
│  │ 📱 Manage Online  │           │   │ Account      │   │  │
│  │ ⏰ Reminders      │           │   │ First/Last   │   │  │
│  │ 📊 Analytics      │           │   │ Email        │   │  │
│  │                   │           │   │ Phone        │   │  │
│  │                   │           │   │ Type/Passwd  │   │  │
│  │  Footer           │           │   │ Confirm Pass │   │  │
│  └──────────────────┘           │   │ Terms        │   │  │
│                                  │   │ Register Btn │   │  │
│                                  │   │ Sign In link │   │  │
│                                  │   └─────────────┘   │  │
│                                  └─────────────────────┘  │
└──────────────────────────────────────────────────────────┘
```

---

## 左侧面板 (Left Panel)

**容器:** `b4UMM` — 宽 560px, 高 `fill_container`(900px)
**布局:** vertical, `justifyContent: space_between`
**内边距:** 60px（四周）
**填充:** `$bg-inverse` (`#0A0A0A`)

### 1.1 顶部区域
- **位置:** x=60, y=60, gap=8px

| 元素 | 字体 | 颜色 | 内容 |
|------|------|------|------|
| **Logo** (`lDekS`) | Inter 24px Bold 700 | `$text-inverse` | "📚 LibraryOS" |
| **Tagline** (`fCj2W`) | Inter 15px | `$text-muted` | "Next-generation library management system" |

### 1.2 功能优势列表 (Benefits)
- **位置:** x=60, y=363, **间距 (gap):** 20px

每个功能项 (`horizontal`, gap=14px, alignItems=center):

| 图标 (28px) | 标题 (Inter 15px SemiBold) | 描述 (Inter 12px) |
|-------------|--------------------------|-------------------|
| 🔍 `$accent` | "Search Thousands of Books" | "Access our extensive collection" |
| 📱 `$accent` | "Manage Borrowings Online" | "Renew, reserve, track all in one place" |
| ⏰ `$accent` | "Get Smart Reminders" | "Never miss a due date again" |
| 📊 `$accent` | "View Reading Analytics" | "Track your reading journey" |

Title 颜色: `$text-inverse` / Desc 颜色: `$text-muted`

### 1.3 底部页脚
- **位置:** x=60, y=825
- **Footer:** Inter 12px, `$text-muted`, "© 2026 LibraryOS. All rights reserved."

---

## 右侧面板 (Right Panel)

**容器:** `Tc9Pe` — 宽 880px
**布局:** vertical, alignItems=center, justifyContent=center

### 2.1 表单卡片 (Form Card)

**容器:** `Yg4z4`
- **尺寸:** 440px 宽
- **内边距:** 36px
- **间距 (gap):** 18px
- **填充:** `$bg-primary` (`#FFFFFF`)
- **圆角:** `$card-radius` (16px)
- **位置:** x=220, y=134

| 区域 | 间距 (gap) | 布局 |
|------|-----------|------|
| **Form Header** | 4px | vertical |
| **Name Row** | 12px | **horizontal** (两列并排) |
| **Email Field** | 6px | vertical |
| **Phone Field** | 6px | vertical |
| **Type Row** | 12px | **horizontal** (两列并排) |
| **Confirm Password** | 6px | vertical |
| **Terms Row** | 8px | horizontal |
| **Register Button** | — | — |
| **Login Row** | 4px | horizontal, justifyContent=center |

#### Form Header
| 元素 | 属性 | 值 |
|------|------|-----|
| **Title** (`qtLTk`) | 字体 | Inter, 24px, SemiBold 600 |
| | 颜色 | `$text-primary` |
| | 内容 | "Create an Account" |
| **Subtitle** (`weAaO`) | 字体 | Inter, 14px |
| | 颜色 | `$text-secondary` |
| | 内容 | "Join our library community" |

#### Name Row (两列并排)
**容器:** `qqDkO`, 宽 368px, gap=12px, horizontal

| 列 | Label | Placeholder |
|----|-------|-------------|
| **First Name** (`N64Aq`) | "First Name" | "John" |
| **Last Name** (`W4aDn`) | "Last Name" | "Doe" |

输入框通用样式:
- 内边距: [10, 14]
- 圆角: `$input-radius` (12px)
- 填充: `$bg-secondary` (`#F7F8FA`)
- 描边: 1.5px solid `$border` (`#E5E7EB`)
- Label: Inter 13px Medium 500, `$text-primary`
- Placeholder: Inter 13px, `$text-muted`

#### Email Field
| 元素 | 内容 |
|------|------|
| **Label** | "Email" |
| **Placeholder** | "you@example.com" |

#### Phone Field
| 元素 | 内容 |
|------|------|
| **Label** | "Phone Number" |
| **Placeholder** | "+86 138 0000 0000" |

#### Type Row (并排)
**容器:** `j4zyr`, horizontal, gap=12px

| 列 | 类型 | 内容 |
|----|------|------|
| **Reader Type** (`vdU1P`) | 下拉框 | 选中值: "Student", 箭头: "▼" `$text-muted` |
| **Password** (`naKEk`) | 密码输入 | Placeholder: "••••••••" |

下拉框特殊样式: horizontal, justifyContent=space_between, alignItems=center

#### Confirm Password
| 元素 | 内容 |
|------|------|
| **Label** | "Confirm Password" |
| **Placeholder** | "••••••••" |

#### Terms Row
**容器:** `XlHAX`, horizontal, gap=8px, alignItems=center

| 元素 | 属性 |
|------|------|
| **Checkbox** (`Eg2A4`) | 18×18px, 圆角 4px, 填充 `$accent` |
| **Check** (`j79I0`) | "✓" `$text-inverse` |
| **Terms Text** (`fzITw`) | Inter 12px, `$text-secondary`, "I agree to the Terms of Service and Privacy Policy" |

#### Register Button
- 宽 368px, 高 42px
- 圆角: 10px
- 填充: `$accent` (`#4A9FD8`)
- Text: "Create Account" / Inter 15px SemiBold 600 / `$text-inverse`

#### Login Row
| 元素 | 内容 |
|------|------|
| **Question** | Inter 13px, `$text-muted`, "Already have an account?" |
| **Link** | Inter 13px Medium 500, `$accent`, "Sign in" |

---

## 组件层级关系

```
RegisterPage (vDHnP)
├── LeftPanel (b4UMM) — 560×900
│   ├── PanelTop (q6WlFM)
│   │   ├── Logo (lDekS)
│   │   └── Tagline (fCj2W)
│   ├── Benefits (yVZoX)
│   │   ├── SearchThousands... (ajto2)
│   │   ├── ManageOnline (RFrnK)
│   │   ├── GetSmartReminders (tTfwm)
│   │   └── ViewReadingAnalytics (EM7NB)
│   └── PanelBottom (b10kb)
│       └── Footer (nyJzC)
│
└── RightPanel (Tc9Pe) — 880×900
    ├── FormCard (Yg4z4) — 440px
    │   ├── FormHeader (SqlND)
    │   ├── NameRow (qqDkO)
    │   │   ├── FirstName (N64Aq)
    │   │   └── LastName (W4aDn)
    │   ├── EmailField (m2n57r)
    │   ├── PhoneField (ctybu)
    │   ├── TypeRow (j4zyr)
    │   │   ├── ReaderType (vdU1P) [dropdown]
    │   │   └── Password (naKEk)
    │   ├── ConfirmPassword (cCImO)
    │   ├── TermsRow (XlHAX)
    │   ├── RegisterButton (VQ91L)
    │   └── LoginRow (RQMk8)
```
