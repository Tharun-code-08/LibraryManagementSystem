# Use Case Diagram

```mermaid
flowchart LR
    Admin((Admin))
    Librarian((Librarian))
    Student((Student))
    Faculty((Faculty))
    Guest((Guest))

    subgraph Auth["Authentication"]
        UC1[Login]
        UC2[Logout]
        UC3[Forgot Password]
        UC4[Change Password]
    end

    subgraph Catalog["Catalog Management"]
        UC5[Search Books]
        UC6[Add / Edit / Delete Book]
        UC7[Manage Authors / Publishers / Categories]
        UC8[Bulk Import Books]
        UC9[Generate QR / Barcode]
    end

    subgraph People["People Management"]
        UC10[Register Student / Faculty]
        UC11[Manage Membership]
    end

    subgraph Circulation["Circulation"]
        UC12[Issue Book]
        UC13[Return Book]
        UC14[Reserve Book]
        UC15[Pay / Waive Fine]
    end

    subgraph Ops["Operations & Admin"]
        UC16[Manage Inventory / Audit]
        UC17[Manage Suppliers / Purchase Orders]
        UC18[Generate Reports]
        UC19[Manage Users / Roles / Permissions]
        UC20[View Audit Logs]
        UC21[Configure Settings / Backup]
        UC22[View Dashboard / Analytics]
    end

    Guest --> UC5

    Student --> UC1
    Student --> UC2
    Student --> UC3
    Student --> UC4
    Student --> UC5
    Student --> UC14
    Student --> UC15

    Faculty --> UC1
    Faculty --> UC2
    Faculty --> UC5
    Faculty --> UC14

    Librarian --> UC1
    Librarian --> UC2
    Librarian --> UC4
    Librarian --> UC5
    Librarian --> UC6
    Librarian --> UC7
    Librarian --> UC8
    Librarian --> UC9
    Librarian --> UC10
    Librarian --> UC11
    Librarian --> UC12
    Librarian --> UC13
    Librarian --> UC14
    Librarian --> UC15
    Librarian --> UC16
    Librarian --> UC17
    Librarian --> UC18
    Librarian --> UC22

    Admin --> UC1
    Admin --> UC2
    Admin --> UC4
    Admin --> UC19
    Admin --> UC20
    Admin --> UC21
    Admin --> UC22
    Admin -.includes.-> Librarian
```

## Notes
- `Admin` inherits all `Librarian` use cases (generalization) plus system administration.
- `UC12 Issue Book` includes `Availability Check` and `Maximum Borrow Validation` as sub-flows.
- `UC13 Return Book` includes `Damage Check` and `Fine Calculation` as sub-flows.
- `Guest` is limited to read-only catalog search (no authentication required).
