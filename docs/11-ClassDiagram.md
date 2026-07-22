# Class Diagram (Core Circulation Slice)

Representative slice showing the pattern to be replicated across all modules (Catalog, People,
Finance, Inventory, Procurement, Reports, Admin).

```mermaid
classDiagram
    class BookController {
        -BookService bookService
        +onAddBook()
        +onSearch(criteria)
        +onDelete(id)
    }

    class IssueController {
        -IssueService issueService
        -StudentService studentService
        +onScanCopy(barcode)
        +onScanMember(memberId)
        +onConfirmIssue()
    }

    class BookService {
        <<interface>>
        +create(BookCreateDTO) BookDTO
        +update(id, BookUpdateDTO) BookDTO
        +delete(id)
        +search(SearchCriteria) Page~BookDTO~
    }

    class BookServiceImpl {
        -BookRepository bookRepository
        -BookValidator validator
        -AuditLogService auditLogService
        +create(BookCreateDTO) BookDTO
        +search(SearchCriteria) Page~BookDTO~
    }

    class IssueService {
        <<interface>>
        +issueBook(IssueRequestDTO) IssueResultDTO
        +returnBook(ReturnRequestDTO) ReturnResultDTO
    }

    class IssueServiceImpl {
        -IssueRepository issueRepository
        -BookCopyRepository bookCopyRepository
        -BorrowLimitValidator borrowLimitValidator
        -FineCalculationStrategy fineStrategy
        -NotificationService notificationService
        -AuditLogService auditLogService
        +issueBook(IssueRequestDTO) IssueResultDTO
        +returnBook(ReturnRequestDTO) ReturnResultDTO
    }

    class BorrowLimitValidator {
        +validate(Membership, int currentBorrowed) void
    }

    class FineCalculationStrategy {
        <<interface>>
        +calculate(Issue, Return) BigDecimal
    }

    class OverdueFineStrategy {
        +calculate(Issue, Return) BigDecimal
    }

    class BookRepository {
        <<interface>>
        +findById(id) Optional~Book~
        +search(SearchCriteria) Page~Book~
        +save(Book) Book
    }

    class HibernateBookRepository {
        -EntityManager entityManager
        +findById(id) Optional~Book~
        +search(SearchCriteria) Page~Book~
    }

    class IssueRepository {
        <<interface>>
        +save(Issue) Issue
        +findOpenByCopy(copyId) Optional~Issue~
    }

    class Book
    class BookCopy
    class Issue
    class Membership

    BookController --> BookService
    IssueController --> IssueService
    BookService <|.. BookServiceImpl
    IssueService <|.. IssueServiceImpl
    BookServiceImpl --> BookRepository
    BookRepository <|.. HibernateBookRepository
    IssueServiceImpl --> IssueRepository
    IssueServiceImpl --> BorrowLimitValidator
    IssueServiceImpl --> FineCalculationStrategy
    FineCalculationStrategy <|.. OverdueFineStrategy
    IssueRepository --> Issue
    HibernateBookRepository --> Book
    Issue --> BookCopy
    Issue --> Membership
    BookCopy --> Book
```

## Design Notes
- Every `*Service` is an interface with exactly one primary implementation registered in
  `AppContext`; unit tests substitute mock repositories, never mock the service under test.
- `IssueServiceImpl` depends only on repository and business-layer **interfaces** — never on
  Hibernate types directly — preserving the Clean Architecture boundary.
- `DTO`s (`IssueRequestDTO`, `IssueResultDTO`, `BookDTO`) are immutable, constructed via `Builder`
  (e.g. `IssueRequestDTO.builder().memberId(..).copyBarcode(..).build()`).
- This exact controller → service(interface) → serviceImpl → repository(interface) → hibernateImpl
  shape is replicated for every one of the 30 modules listed in the SRS.
