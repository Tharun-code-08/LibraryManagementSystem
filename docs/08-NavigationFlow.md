# Navigation Flow

```mermaid
flowchart TD
    Start([App Launch]) --> Splash[Splash / Loading]
    Splash --> Login[Login Screen]
    Login -->|Forgot Password| ForgotPwd[Forgot Password Flow]
    ForgotPwd --> Login
    Login -->|Auth Success| RoleGate{Role?}

    RoleGate -->|Guest| GuestSearch[Public Catalog Search]
    RoleGate -->|Student/Faculty| StudentShell[Student/Faculty Shell]
    RoleGate -->|Librarian| LibrarianShell[Librarian Shell]
    RoleGate -->|Admin| AdminShell[Admin Shell]

    StudentShell --> SDash[My Dashboard]
    StudentShell --> SSearch[Search Catalog]
    StudentShell --> SReserve[My Reservations]
    StudentShell --> SFines[My Fines]
    StudentShell --> SProfile[Profile / Password]

    LibrarianShell --> Dashboard[Dashboard]
    LibrarianShell --> Catalog[Catalog Mgmt]
    LibrarianShell --> People[Students/Faculty Mgmt]
    LibrarianShell --> Circulation[Issue / Return / Reserve]
    LibrarianShell --> Finance[Fines / Payments]
    LibrarianShell --> Inventory[Inventory / Audit]
    LibrarianShell --> Suppliers[Suppliers / Purchase Orders]
    LibrarianShell --> Reports[Reports]

    Catalog --> BookForm[Add/Edit Book]
    Catalog --> BulkImport[Bulk Import]
    People --> StudentForm[Add/Edit Student]
    People --> FacultyForm[Add/Edit Faculty]
    Circulation --> IssueScreen[Issue Book]
    Circulation --> ReturnScreen[Return Book]
    Circulation --> ReserveScreen[Reservation Queue]
    Finance --> FineScreen[Fine Management]
    FineScreen --> PaymentScreen[Collect Payment]

    AdminShell --> Dashboard
    AdminShell --> UserMgmt[User & Role Management]
    AdminShell --> AuditLogs[Audit Logs]
    AdminShell --> Settings[Settings / Theme / Backup]

    Dashboard -->|Quick Action| IssueScreen
    Dashboard -->|Quick Action| BookForm
    Dashboard -->|Quick Action| StudentForm

    LibrarianShell -->|Logout| Login
    AdminShell -->|Logout| Login
    StudentShell -->|Logout| Login

    LibrarianShell -->|Session Timeout| Login
    AdminShell -->|Session Timeout| Login
    StudentShell -->|Session Timeout| Login
```

## Notes
- The Sidebar persists across all authenticated shells; the Top Nav hosts global search,
  notifications, theme toggle, and profile menu at all times.
- Session timeout is a global interceptor: any idle session past the configured threshold routes
  back to Login regardless of current screen, after a confirmation snackbar countdown.
- Guests only ever reach the public catalog search — every other route requires authentication and
  a `RoleGuard` check at the controller level (defense in depth beyond hiding menu items).
