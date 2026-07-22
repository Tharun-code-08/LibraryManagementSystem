package com.university.lms.service.report.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.university.lms.business.MembershipHolderResolver;
import com.university.lms.dto.request.BookSearchCriteria;
import com.university.lms.dto.request.FineSearchCriteria;
import com.university.lms.dto.request.ReportCriteriaDTO;
import com.university.lms.dto.request.StudentSearchCriteria;
import com.university.lms.dto.response.BookDTO;
import com.university.lms.dto.response.FacultyDTO;
import com.university.lms.dto.response.FineDTO;
import com.university.lms.dto.response.PopularBookDTO;
import com.university.lms.dto.response.ReportDTO;
import com.university.lms.dto.response.StudentDTO;
import com.university.lms.entity.BookCopy;
import com.university.lms.entity.BookCopyStatus;
import com.university.lms.entity.Issue;
import com.university.lms.entity.Return;
import com.university.lms.repository.BookCopyRepository;
import com.university.lms.repository.IssueRepository;
import com.university.lms.repository.ReturnRepository;
import com.university.lms.service.analytics.DashboardService;
import com.university.lms.service.catalog.BookService;
import com.university.lms.service.finance.FineService;
import com.university.lms.service.people.FacultyService;
import com.university.lms.service.people.StudentService;
import com.university.lms.service.report.ReportService;
import com.university.lms.util.ExportFormat;
import com.university.lms.util.ReportFactory;

public final class ReportServiceImpl implements ReportService {

    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final int MAX_ROWS = 5000;

    private final BookService bookService;
    private final StudentService studentService;
    private final FacultyService facultyService;
    private final FineService fineService;
    private final IssueRepository issueRepository;
    private final ReturnRepository returnRepository;
    private final BookCopyRepository bookCopyRepository;
    private final DashboardService dashboardService;
    private final MembershipHolderResolver membershipHolderResolver;
    private final ReportFactory reportFactory;

    public ReportServiceImpl(BookService bookService, StudentService studentService, FacultyService facultyService,
                              FineService fineService, IssueRepository issueRepository, ReturnRepository returnRepository,
                              BookCopyRepository bookCopyRepository, DashboardService dashboardService,
                              MembershipHolderResolver membershipHolderResolver, ReportFactory reportFactory) {
        this.bookService = bookService;
        this.studentService = studentService;
        this.facultyService = facultyService;
        this.fineService = fineService;
        this.issueRepository = issueRepository;
        this.returnRepository = returnRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.dashboardService = dashboardService;
        this.membershipHolderResolver = membershipHolderResolver;
        this.reportFactory = reportFactory;
    }

    @Override
    public ReportDTO generate(ReportCriteriaDTO criteria) {
        return switch (criteria.getReportType()) {
            case BOOKS -> booksReport(criteria);
            case STUDENTS -> studentsReport(criteria);
            case FACULTY -> facultyReport(criteria);
            case FINES -> finesReport(criteria);
            case ISSUES -> issuesReport(criteria);
            case RETURNS -> returnsReport(criteria);
            case OVERDUE -> overdueReport();
            case INVENTORY -> inventoryReport();
            case LOST_BOOKS -> lostBooksReport();
            case POPULAR_BOOKS -> popularBooksReport();
        };
    }

    @Override
    public String export(ReportDTO report, ExportFormat format) {
        String fileBaseName = report.title().toLowerCase().replaceAll("[^a-z0-9]+", "-")
                + "-" + report.generatedAt().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        return reportFactory.exporterFor(format).export(report, fileBaseName);
    }

    private ReportDTO booksReport(ReportCriteriaDTO criteria) {
        BookSearchCriteria searchCriteria = BookSearchCriteria.builder().pageSize(MAX_ROWS).build();
        List<BookDTO> books = bookService.search(searchCriteria).getContent();
        List<List<String>> rows = new ArrayList<>();
        for (BookDTO book : books) {
            rows.add(List.of(
                    nullSafe(book.getIsbn()),
                    nullSafe(book.getTitle()),
                    String.join(", ", book.getAuthorNames()),
                    nullSafe(book.getPublisherName()),
                    nullSafe(book.getCategoryName()),
                    String.valueOf(book.getTotalCopies()),
                    String.valueOf(book.getAvailableCopies())));
        }
        return new ReportDTO("Books Report",
                List.of("ISBN", "Title", "Authors", "Publisher", "Category", "Total Copies", "Available Copies"),
                rows, LocalDateTime.now());
    }

    private ReportDTO studentsReport(ReportCriteriaDTO criteria) {
        StudentSearchCriteria searchCriteria = StudentSearchCriteria.builder()
                .department(criteria.getDepartment())
                .year(criteria.getYear())
                .pageSize(MAX_ROWS)
                .build();
        List<StudentDTO> students = studentService.search(searchCriteria).getContent();
        List<List<String>> rows = new ArrayList<>();
        for (StudentDTO student : students) {
            rows.add(List.of(
                    nullSafe(student.getStudentId()),
                    nullSafe(student.getUsername()),
                    nullSafe(student.getDepartment()),
                    student.getYear() == null ? "" : String.valueOf(student.getYear()),
                    nullSafe(student.getStatus()),
                    nullSafe(student.getMembershipStatus())));
        }
        return new ReportDTO("Students Report",
                List.of("Student ID", "Name", "Department", "Year", "Status", "Membership Status"),
                rows, LocalDateTime.now());
    }

    private ReportDTO facultyReport(ReportCriteriaDTO criteria) {
        List<FacultyDTO> facultyList = facultyService.listAll();
        List<List<String>> rows = new ArrayList<>();
        for (FacultyDTO faculty : facultyList) {
            if (criteria.getDepartment() != null && !criteria.getDepartment().isBlank()
                    && !criteria.getDepartment().equalsIgnoreCase(faculty.getDepartment())) {
                continue;
            }
            rows.add(List.of(
                    nullSafe(faculty.getFacultyId()),
                    nullSafe(faculty.getUsername()),
                    nullSafe(faculty.getDepartment()),
                    nullSafe(faculty.getDesignation()),
                    nullSafe(faculty.getPhone()),
                    nullSafe(faculty.getMembershipStatus())));
        }
        return new ReportDTO("Faculty Report",
                List.of("Faculty ID", "Name", "Department", "Designation", "Phone", "Membership Status"),
                rows, LocalDateTime.now());
    }

    private ReportDTO finesReport(ReportCriteriaDTO criteria) {
        FineSearchCriteria searchCriteria = FineSearchCriteria.builder().pageSize(MAX_ROWS).build();
        List<FineDTO> fines = fineService.search(searchCriteria).getContent();
        LocalDateTime from = criteria.getFromDate() == null ? null : criteria.getFromDate().atStartOfDay();
        LocalDateTime to = criteria.getToDate() == null ? null : criteria.getToDate().atTime(23, 59, 59);
        List<List<String>> rows = new ArrayList<>();
        for (FineDTO fine : fines) {
            if (from != null && fine.createdAt().isBefore(from)) {
                continue;
            }
            if (to != null && fine.createdAt().isAfter(to)) {
                continue;
            }
            rows.add(List.of(
                    String.valueOf(fine.id()),
                    nullSafe(fine.bookTitle()),
                    nullSafe(fine.memberName()),
                    nullSafe(fine.reason()),
                    fine.amount().toPlainString(),
                    fine.paidAmount().toPlainString(),
                    fine.remainingAmount().toPlainString(),
                    nullSafe(fine.status()),
                    fine.createdAt().format(DATETIME_FORMAT)));
        }
        return new ReportDTO("Fines Report",
                List.of("Fine ID", "Book", "Member", "Reason", "Amount", "Paid", "Remaining", "Status", "Created At"),
                rows, LocalDateTime.now());
    }

    private ReportDTO issuesReport(ReportCriteriaDTO criteria) {
        LocalDateTime from = criteria.getFromDate() == null
                ? LocalDateTime.now().minusYears(10) : criteria.getFromDate().atStartOfDay();
        LocalDateTime to = criteria.getToDate() == null
                ? LocalDateTime.now() : criteria.getToDate().atTime(23, 59, 59);
        List<Issue> issues = issueRepository.findByIssueDateRange(from, to);
        List<List<String>> rows = new ArrayList<>();
        for (Issue issue : issues) {
            rows.add(List.of(
                    String.valueOf(issue.getId()),
                    nullSafe(issue.getBookCopy().getBook().getTitle()),
                    nullSafe(issue.getBookCopy().getBarcode()),
                    memberName(issue),
                    issue.getIssueDate().format(DATETIME_FORMAT),
                    issue.getDueDate().format(DATETIME_FORMAT),
                    issue.getStatus().name()));
        }
        return new ReportDTO("Issues Report",
                List.of("Issue ID", "Book", "Copy Barcode", "Member", "Issue Date", "Due Date", "Status"),
                rows, LocalDateTime.now());
    }

    private ReportDTO returnsReport(ReportCriteriaDTO criteria) {
        LocalDateTime from = criteria.getFromDate() == null
                ? LocalDateTime.now().minusYears(10) : criteria.getFromDate().atStartOfDay();
        LocalDateTime to = criteria.getToDate() == null
                ? LocalDateTime.now() : criteria.getToDate().atTime(23, 59, 59);
        List<Return> returns = returnRepository.findByReturnDateRange(from, to);
        List<List<String>> rows = new ArrayList<>();
        for (Return returnRecord : returns) {
            rows.add(List.of(
                    String.valueOf(returnRecord.getId()),
                    nullSafe(returnRecord.getIssue().getBookCopy().getBook().getTitle()),
                    memberName(returnRecord.getIssue()),
                    returnRecord.getReturnDate().format(DATETIME_FORMAT),
                    returnRecord.getConditionOnReturn().name(),
                    nullSafe(returnRecord.getNotes())));
        }
        return new ReportDTO("Returns Report",
                List.of("Return ID", "Book", "Member", "Return Date", "Condition", "Notes"),
                rows, LocalDateTime.now());
    }

    private ReportDTO overdueReport() {
        LocalDateTime now = LocalDateTime.now();
        List<Issue> overdueIssues = issueRepository.findOverdueOpenIssues(now);
        List<List<String>> rows = new ArrayList<>();
        for (Issue issue : overdueIssues) {
            long daysOverdue = Duration.between(issue.getDueDate(), now).toDays();
            rows.add(List.of(
                    String.valueOf(issue.getId()),
                    nullSafe(issue.getBookCopy().getBook().getTitle()),
                    memberName(issue),
                    issue.getIssueDate().format(DATETIME_FORMAT),
                    issue.getDueDate().format(DATETIME_FORMAT),
                    String.valueOf(Math.max(daysOverdue, 0))));
        }
        return new ReportDTO("Overdue Report",
                List.of("Issue ID", "Book", "Member", "Issue Date", "Due Date", "Days Overdue"),
                rows, LocalDateTime.now());
    }

    private ReportDTO inventoryReport() {
        List<BookCopy> copies = bookCopyRepository.findAll();
        List<List<String>> rows = new ArrayList<>();
        for (BookCopy copy : copies) {
            rows.add(copyRow(copy));
        }
        return new ReportDTO("Inventory Report",
                List.of("Barcode", "Book", "Branch", "Location", "Condition", "Status"),
                rows, LocalDateTime.now());
    }

    private ReportDTO lostBooksReport() {
        List<BookCopy> copies = bookCopyRepository.findByStatus(BookCopyStatus.LOST);
        List<List<String>> rows = new ArrayList<>();
        for (BookCopy copy : copies) {
            rows.add(copyRow(copy));
        }
        return new ReportDTO("Lost Books Report",
                List.of("Barcode", "Book", "Branch", "Location", "Condition", "Status"),
                rows, LocalDateTime.now());
    }

    private ReportDTO popularBooksReport() {
        List<PopularBookDTO> popularBooks = dashboardService.getPopularBooks(50);
        List<List<String>> rows = new ArrayList<>();
        for (PopularBookDTO popularBook : popularBooks) {
            rows.add(List.of(nullSafe(popularBook.bookTitle()), String.valueOf(popularBook.issueCount())));
        }
        return new ReportDTO("Popular Books Report", List.of("Book Title", "Times Issued"), rows, LocalDateTime.now());
    }

    private List<String> copyRow(BookCopy copy) {
        String location = String.join("/",
                nullSafe(copy.getShelf()), nullSafe(copy.getRack()), nullSafe(copy.getRowLabel()));
        return List.of(
                nullSafe(copy.getBarcode()),
                nullSafe(copy.getBook().getTitle()),
                nullSafe(copy.getBranch().getName()),
                location,
                copy.getCondition().name(),
                copy.getStatus().name());
    }

    private String memberName(Issue issue) {
        return membershipHolderResolver.resolveDisplayName(
                issue.getMembership().getHolderType(), issue.getMembership().getHolderId());
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
