# 管理后台 - Admin Borrow/Return (流通管理)

> **设计风格:** Soft Bento · Carbon Frost
> **页面尺寸:** 1440 × 900 px
> **背景色:** `#F7F8FA` (`$bg-secondary`)

---

## 布局结构

```
┌─────────┬───────────────────────────────────────────────────────────────┐
│ Sidebar │  Main Content                                                 │
│ (240px) │                                                               │
│         │  Circulation                                                  │
│ 📊 Dash │  [Borrow]  [Return]  ─────────────────────────────────        │
│ 📖 Brw  │                                                               │
│ 📚 Books│  ┌───────── Borrow Panel (640px) ──────┐  ┌─── Return ───────┐│
│ 👥 Rdr  │  │ Step 1: Enter Reader Card            │  │ Scan barcode     ││
│ 📈 Stat │  │ [🔍 Scan card...]   [Lookup]         │  │ [🔍 Scan...][Find]││
│ 💰 Fine │  │ ┌─────────────────────────────────┐  │  │                  ││
│ ⚙️ Set  │  │ │ [W] Wang Xiaoming                │  │  │ ┌──────────────┐││
│         │  │ │ RD20260001 · Student · 2/5     │  │  │ │ 📖 Gatsby     │││
│ [SA]    │  │ └─────────────────────────────────┘  │  │ │ Due Jul 31   │││
│ System  │  │ Step 2: Scan Book Barcodes           │  │ │ Borrowed by  │││
│ Admin   │  │ [🔍 Scan barcode...]  [+]            │  │ │ Wang...      │││
│         │  │ ┌─────────────────────────────────┐  │  │ └──────────────┘││
│         │  │ │ Barcode        Title       Status│  │  │ ⚠ Overdue 5d   ││
│         │  │ │ 97807432...    Gatsby     OK     │  │  │ ¥2.50 fine     ││
│         │  │ │ 97875442...    1984       OK     │  │  │                ││
│         │  │ └─────────────────────────────────┘  │  │ [Confirm Return]││
│         │  │ [===== Confirm Borrow (2 books) ====]│  └────────────────┘│
│         │  └──────────────────────────────────────┘                     │
└─────────┴───────────────────────────────────────────────────────────────┘
```

---

## 1. 侧边栏 (Sidebar)

同 `07-admin-books.md`，此时 **Borrow/Return** 为激活态（蓝色高亮 `$accent`）。

---

## 2. Header

| 元素 | 属性 |
|------|------|
| **Title** (`e7ruDp`) | Inter 24px Bold 700, "Circulation" |

### Tab Bar

| Tab | 状态 | 文字 | 样式 |
|-----|------|------|------|
| **Borrow** | 激活 | Inter 14px SemiBold 600, `$accent` | 底部指示线 60×2px `$accent` |
| **Return** | 默认 | Inter 14px, `$text-secondary` | 无指示线 |

---

## 3. Borrow Panel (左侧, 640px)

**容器:** `s5k68` — 640px, vertical, padding=24, gap=20, `$bg-primary`, 圆角 `$card-radius`, 描边 1px

### 3.1 Section Header

| 元素 | 属性 |
|------|------|
| **Title** (`EO5e2`) | Inter 16px SemiBold 600, "Borrow" |

### 3.2 Step 1: Reader Lookup

| 元素 | 属性 |
|------|------|
| **Section Label** (`m4vkt`) | Inter 13px SemiBold 600, "Step 1: Enter Reader Card" |

#### Reader Input Row
| 元素 | 属性 |
|------|------|
| **Input** (`ESVaL`) | 240px, `$input-radius`, `$bg-secondary`, 描边 1.5px, 🔍 + "Scan or enter card no..." |
| **Lookup Button** (`ubajw`) | padding [10,20], 圆角 10px, 填充 `$accent`, Inter 13px SemiBold 600, "Lookup" |

#### Reader Info Card (after lookup)
| 元素 | 属性 |
|------|------|
| **Container** (`c89es`) | 368px, horizontal, padding=14, gap=16, `$bg-secondary`, 圆角 10px |
| **Avatar** (`s5L310`) | 40×40px, 圆角 999px, `$accent-light`, "W" |
| **Name** (`D2PYS`) | Inter 14px SemiBold 600, "Wang Xiaoming" |
| **Detail** (`d5Zqm`) | Inter 12px, `$text-muted`, "RD20260001 - Student - 2/5 borrowed" |

### 3.3 Step 2: Scan Books

| 元素 | 属性 |
|------|------|
| **Section Label** (`wfe0l`) | Inter 13px SemiBold 600, "Step 2: Scan Book Barcodes" |

#### Scan Input Row
| 元素 | 属性 |
|------|------|
| **Input** (`h5IQO5`) | 280px, `$input-radius`, `$bg-secondary`, 🔍 + "Scan barcode..." |
| **Add Button** (`i7uQxd`) | padding [10,20], 圆角 10px, 填充 `$accent`, "+" |

#### Scanned Book List
| 元素 | 属性 |
|------|------|
| **Container** (`gcGti`) | `fill_container`, vertical, gap=2, 圆角 10px, 描边 1px |

**表头:**
| Barcode (140px) | Title (220px) | Status (80px) |
|-----------------|---------------|---------------|
| Geist Mono 11px Bold, `$text-muted` | Inter 11px Bold | Inter 11px Bold |

**数据行 (2条):**
| Barcode (Geist Mono 11px) | Title (Inter 12px) | Status (Inter 11px, `$success`) | 删除(x) |
|---------------------------|--------------------|---------------------------------|---------|
| 9780743273565-001 | The Great Gatsby | OK | x (red) |
| 9787544291163-002 | 1984 | OK | x (red) |

### 3.4 Confirm Button

| 元素 | 属性 |
|------|------|
| **Container** (`aLuVs`) | `fill_container`, padding [12,28], 圆角 10px, 填充 `$accent` |
| **Label** (`Cirrc`) | Inter 14px SemiBold 600, `$text-inverse`, "Confirm Borrow (2 books)" |

---

## 4. Return Panel (右侧, fill_container)

**容器:** `wnVtC` — `fill_container`, vertical, padding=24, gap=20, `$bg-primary`, 圆角 `$card-radius`, 描边 1px

### 4.1 Section Header
| **Title** (`lL6Gr`) | Inter 16px SemiBold 600, "Return" |

### 4.2 Scan Input Row
| **Scan Input** (`dic2U`) | 🔍 + "Scan book barcode to return...", `fill_container` |
| **Find Button** (`TQpjC`) | padding [10,20], 圆角 10px, `$accent`, "Find" |

### 4.3 Return Book Info
| **Container** (`i2o00S`) | padding 16, gap=12, `$bg-secondary`, 圆角 10px |
| **Cover Thumb** (`AptLU`) | 40×56px, 圆角 6px, `$accent-light`, 📖 |
| **Title** (`z92cQA`) | Inter 14px SemiBold 600, "The Great Gatsby" |
| **Detail** (`ZHxm4`) | Inter 12px, `$text-muted`, "Borrowed: Jul 1, 2026 - Due: Jul 31, 2026" |
| **Reader** (`zbJmq`) | Inter 12px, `$text-secondary`, "Borrowed by: Wang Xiaoming" |

### 4.4 Overdue Warning (条件显示)
| **Container** (`MAEh5`) | padding [12,16], gap=10, 填充 `rgba(248,113,113,0.08)`, 描边 1px `$danger`, 圆角 10px |
| **Icon** | ⚠️ `$danger` |
| **Title** (`IcmSd`) | Inter 13px SemiBold 600, `$danger`, "Overdue by 5 days" |
| **Fine** (`b52Tcy`) | Inter 12px, `$danger`, "Fine amount: 2.50 yuan" |

### 4.5 Confirm Return Button
| **Container** (`acx7N`) | padding [12,28], 圆角 10px, 填充 `$success` (绿色) |
| **Label** (`TPrpw`) | Inter 14px SemiBold 600, `$text-inverse`, "Confirm Return" |

---

## 组件层级关系

```
AdminBorrowReturn (oUGxp) — 1440×900
├── Sidebar (X8o6AD) — 240×900, Borrow/Return 激活态
└── MainContent (c2Eoq3)
    ├── Header (Mlqgp) — "Circulation"
    ├── TabBar (h0zKKH) — [Borrow] [Return]
    ├── TabDivider (F3GlB)
    └── ContentRow (K09RDX) — horizontal, gap=20
        ├── BorrowPanel (s5k68) — 640px
        │   ├── SectionHeader (Xdgce)
        │   ├── Step1 (DadiA)
        │   │   ├── ReaderRow (abNFi) — input + Lookup button
        │   │   └── ReaderInfoCard (c89es)
        │   ├── Step2 (DI1b1)
        │   │   └── ScanRow (JD1nm) — input + Add button
        │   ├── BookList (gcGti) — 2 book rows
        │   └── ConfirmBorrow (aLuVs)
        └── ReturnPanel (wnVtC) — fill_container
            ├── SectionHeader (Q5Fdta)
            ├── ScanRow (d12q5) — input + Find button
            ├── ReturnBookInfo (i2o00S)
            ├── OverdueWarning (MAEh5) — conditional
            └── ConfirmReturn (acx7N) — green
```

---

## 交互说明

| 操作 | 交互 |
|------|------|
| Tab 切换 | 点击 Borrow / Return 切换面板（当前显示 Borrow 激活态） |
| 读者查找 | 输入读者证号 → 点 Lookup → 显示读者信息卡片 |
| 扫码添加 | 输入条码 → 点 "+" → 添加到图书列表 |
| 移除图书 | 点击列表中的 x (红色) → 移除条目 |
| 确认借书 | 点 "Confirm Borrow" → 提交借阅 |
| 还书查询 | 输入条码 → 点 Find → 显示借阅信息和逾期警告 |
| 确认还书 | 点 "Confirm Return" → 完成还书（绿色按钮） |
