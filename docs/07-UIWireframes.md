# UI Wireframes

Text/ASCII wireframes for the primary screens. Visual language: rounded 12px cards, soft elevation
shadows, left navigation drawer, top app bar with global search, Material-inspired color system
(see `09-ColorPalette.md`).

## 1. Login Screen
```
┌───────────────────────────────────────────────────────────────┐
│                         (gradient background)                  │
│                    ┌───────────────────────────┐               │
│                    │   [Logo]  LibraryOS        │               │
│                    │                            │               │
│                    │  Username / Email          │               │
│                    │  [_______________________] │               │
│                    │  Password                  │               │
│                    │  [_______________________] │  <- glass card│
│                    │  Role: (Student|Faculty|   │     w/ blur   │
│                    │         Librarian|Admin)   │               │
│                    │  [x] Remember Me   Forgot? │               │
│                    │                            │               │
│                    │  [        LOGIN        ]   │               │
│                    └───────────────────────────┘               │
└───────────────────────────────────────────────────────────────┘
```

## 2. Main Shell (post-login)
```
┌──────────┬──────────────────────────────────────────────────────┐
│  LOGO    │  [☰]  Global Search [__________]  🔔  🌙  👤 Profile ▾ │
│──────────┼──────────────────────────────────────────────────────┤
│ ▣ Dashboard   │                                                    │
│ 📚 Catalog    │              (routed content area)                │
│ 👥 People     │                                                    │
│ 🔄 Circulation│                                                    │
│ 💰 Fines      │                                                    │
│ 📦 Inventory  │                                                    │
│ 🚚 Suppliers  │                                                    │
│ 📊 Reports    │                                                    │
│ 🛠 Settings   │                                                    │
│ 🔐 Admin      │                                                    │
└──────────┴──────────────────────────────────────────────────────┘
```

## 3. Dashboard
```
┌─────────────────────────────────────────────────────────────────┐
│ [Total Books]  [Issued]  [Available]  [Overdue]  [Reservations]  │  <- stat cards
│  12,480         3,204     9,120        184         96            │
├───────────────────────────────┬───────────────────────────────────┤
│  Monthly Issues vs Returns     │  Book Categories (Pie)            │
│  (Line Chart)                  │                                    │
├───────────────────────────────┼───────────────────────────────────┤
│  Popular Books (Bar Chart)     │  Recent Activity (feed)           │
│                                │  • J.Doe issued "Clean Code"      │
│                                │  • Fine ₹20 waived for #A1123     │
├───────────────────────────────┴───────────────────────────────────┤
│ Quick Actions: [+ Issue Book] [+ Add Book] [+ Register Student]   │
└─────────────────────────────────────────────────────────────────┘
```

## 4. Book Catalog (List + Filters)
```
┌─────────────────────────────────────────────────────────────────┐
│ Books                                   [+ Add Book] [Import] [⤓]│
│ [Search title/ISBN/author] [Category ▾][Status ▾][Advanced Filter]│
├─────────────────────────────────────────────────────────────────┤
│ Cover │ Title            │ Author  │ Category │ Copies │ Status  │
│ [img] │ Clean Code        │ Martin  │ CS       │ 5/8    │ ● Avail │
│ [img] │ Design Patterns   │ GoF     │ CS       │ 0/3    │ ● Out   │
│ ...                                                              │
├─────────────────────────────────────────────────────────────────┤
│  ◀ 1 2 3 … 42 ▶                          Rows per page: [25 ▾]   │
└─────────────────────────────────────────────────────────────────┘
```

## 5. Issue Book Screen
```
┌─────────────────────────────────────────────────────────────────┐
│  Issue Book                                                       │
│  Student/Faculty: [Scan/Search ID_______] → shows photo + limits │
│  Book Copy:       [Scan Barcode________]  → shows title/status   │
│  Due Date: [auto-computed, editable]     Borrow Limit: 3/5 used  │
│  [  Confirm Issue  ]                                              │
├─────────────────────────────────────────────────────────────────┤
│  Recent Issues (this session)                                    │
└─────────────────────────────────────────────────────────────────┘
```

## 6. Return & Fine Screen
```
┌─────────────────────────────────────────────────────────────────┐
│  Return Book                                                      │
│  Scan Copy Barcode: [______________]                              │
│  Issued to: Jane Doe (STU1023)   Due: 2026-07-10  (11 days late) │
│  Condition on return: (Good|Damaged|Lost)                         │
│  Computed Fine: ₹55.00   [Waive] [Collect Payment]                │
│  [  Confirm Return  ]                                              │
└─────────────────────────────────────────────────────────────────┘
```

## 7. Reports Screen
```
┌─────────────────────────────────────────────────────────────────┐
│  Reports                                                          │
│  Report Type: [Overdue ▾]  Range: [Jul 2026 ▾]  Dept: [All ▾]     │
│  [ Generate ]  [Export PDF] [Export Excel] [Print]                │
├─────────────────────────────────────────────────────────────────┤
│  (preview table / chart rendered here)                            │
└─────────────────────────────────────────────────────────────────┘
```

Every list screen shares the same composition: filter bar → data table (sortable/paginated) →
bulk action bar; every form screen shares: sectioned card layout → inline validation → sticky
save/cancel footer.
