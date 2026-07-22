# Sequence Diagrams

## 1. Login
```mermaid
sequenceDiagram
    actor U as User
    participant LC as LoginController
    participant AS as AuthService
    participant PE as PasswordEncoder
    participant UR as UserRepository
    participant SM as SessionManager
    participant AL as AuditLogService

    U->>LC: enter credentials + submit
    LC->>AS: authenticate(username, password)
    AS->>UR: findByUsername(username)
    UR-->>AS: User(passwordHash, status)
    AS->>PE: matches(password, hash)
    PE-->>AS: true
    AS->>SM: createSession(user)
    SM-->>AS: SessionToken
    AS->>AL: log(LOGIN_SUCCESS, user)
    AS-->>LC: AuthResult(user, session)
    LC->>LC: navigate to role-based shell
```

## 2. Issue Book
```mermaid
sequenceDiagram
    actor L as Librarian
    participant IC as IssueController
    participant IS as IssueService
    participant BLV as BorrowLimitValidator
    participant BCR as BookCopyRepository
    participant IR as IssueRepository
    participant AL as AuditLogService
    participant NS as NotificationService

    L->>IC: scan member ID + copy barcode, confirm
    IC->>IS: issueBook(IssueRequestDTO)
    IS->>BCR: findByBarcode(barcode)
    BCR-->>IS: BookCopy(status=AVAILABLE)
    IS->>BLV: validate(membership, currentBorrowedCount)
    BLV-->>IS: OK
    IS->>BCR: markIssued(copy)
    IS->>IR: save(new Issue)
    IR-->>IS: Issue(id, dueDate)
    IS->>AL: log(BOOK_ISSUED, issue)
    IS->>NS: sendIssueReceipt(member, issue)
    IS-->>IC: IssueResultDTO
    IC->>IC: show success snackbar + print receipt option
```

## 3. Return Book with Fine
```mermaid
sequenceDiagram
    actor L as Librarian
    participant RC as ReturnController
    participant RS as ReturnService
    participant IR as IssueRepository
    participant FCS as FineCalculationStrategy
    participant FR as FineRepository
    participant AL as AuditLogService
    participant NS as NotificationService

    L->>RC: scan copy barcode, set condition, confirm
    RC->>RS: returnBook(ReturnRequestDTO)
    RS->>IR: findOpenByCopy(copyId)
    IR-->>RS: Issue(dueDate, memberId)
    RS->>FCS: calculate(issue, returnDate, condition)
    FCS-->>RS: fineAmount
    alt fineAmount > 0
        RS->>FR: save(new Fine)
    end
    RS->>IR: markReturned(issue)
    RS->>AL: log(BOOK_RETURNED, issue)
    RS->>NS: sendReturnReceipt(member, issue, fine)
    RS-->>RC: ReturnResultDTO(fineAmount)
    RC->>RC: show fine dialog if fineAmount > 0
```

## 4. Reservation Fulfillment (on Return)
```mermaid
sequenceDiagram
    participant RS as ReturnService
    participant RQ as ReservationQueueManager
    participant ResR as ReservationRepository
    participant NS as NotificationService

    RS->>RQ: onCopyAvailable(bookId)
    RQ->>ResR: findNextWaiting(bookId)
    ResR-->>RQ: Reservation(memberId)
    RQ->>ResR: markReady(reservation, expiresAt)
    RQ->>NS: notifyReservationReady(member, book)
```

## 5. Overdue Fine Sweep (Scheduled Job)
```mermaid
sequenceDiagram
    participant SCH as ScheduledExecutor
    participant FS as FineService
    participant IR as IssueRepository
    participant FCS as FineCalculationStrategy
    participant NS as NotificationService

    SCH->>FS: runNightlyOverdueSweep()
    FS->>IR: findOverdueOpenIssues(today)
    IR-->>FS: List<Issue>
    loop each overdue issue
        FS->>FCS: calculate(issue, today)
        FS->>FS: upsertFine(issue, amount)
        FS->>NS: sendOverdueReminder(member, issue)
    end
```

## 6. Bulk Student Import
```mermaid
sequenceDiagram
    actor L as Librarian
    participant BIC as BulkImportController
    participant SS as StudentService
    participant CSV as CsvImportUtil
    participant SV as StudentValidator
    participant SR as StudentRepository

    L->>BIC: upload CSV/Excel file
    BIC->>SS: importStudents(file)
    SS->>CSV: parse(file)
    CSV-->>SS: List<StudentImportRowDTO>
    loop each row
        SS->>SV: validate(row)
        alt valid
            SS->>SR: save(student)
        else invalid
            SS->>SS: collect into rejectedRows
        end
    end
    SS-->>BIC: ImportResult(successCount, rejectedRows)
    BIC->>BIC: show summary + downloadable rejected-rows report
```
