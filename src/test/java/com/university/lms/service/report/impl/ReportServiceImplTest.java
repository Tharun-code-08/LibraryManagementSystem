package com.university.lms.service.report.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.university.lms.business.MembershipHolderResolver;
import com.university.lms.dto.request.ReportCriteriaDTO;
import com.university.lms.dto.response.BookDTO;
import com.university.lms.dto.response.FacultyDTO;
import com.university.lms.dto.response.PopularBookDTO;
import com.university.lms.dto.response.ReportDTO;
import com.university.lms.entity.Book;
import com.university.lms.entity.BookCopy;
import com.university.lms.entity.BookCopyCondition;
import com.university.lms.entity.BookCopyStatus;
import com.university.lms.entity.Branch;
import com.university.lms.entity.HolderType;
import com.university.lms.entity.Issue;
import com.university.lms.entity.Membership;
import com.university.lms.entity.MembershipType;
import com.university.lms.entity.Student;
import com.university.lms.entity.User;
import com.university.lms.model.Page;
import com.university.lms.repository.BookCopyRepository;
import com.university.lms.repository.FacultyRepository;
import com.university.lms.repository.IssueRepository;
import com.university.lms.repository.ReturnRepository;
import com.university.lms.repository.StudentRepository;
import com.university.lms.service.analytics.DashboardService;
import com.university.lms.service.catalog.BookService;
import com.university.lms.service.finance.FineService;
import com.university.lms.service.people.FacultyService;
import com.university.lms.service.people.StudentService;
import com.university.lms.service.report.ExportFormat;
import com.university.lms.service.report.ReportType;
import com.university.lms.util.ExcelReportExporter;
import com.university.lms.util.PdfReportExporter;
import com.university.lms.util.ReportFactory;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private BookService bookService;

    @Mock
    private StudentService studentService;

    @Mock
    private FacultyService facultyService;

    @Mock
    private FineService fineService;

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private ReturnRepository returnRepository;

    @Mock
    private BookCopyRepository bookCopyRepository;

    @Mock
    private DashboardService dashboardService;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private FacultyRepository facultyRepository;

    @TempDir
    private Path tempDir;

    private ReportServiceImpl reportService;
    private Issue issue;
    private BookCopy copy;

    @BeforeEach
    void setUp() {
        MembershipHolderResolver holderResolver = new MembershipHolderResolver(studentRepository, facultyRepository);
        ReportFactory reportFactory = new ReportFactory(
                new PdfReportExporter(tempDir.resolve("pdf")), new ExcelReportExporter(tempDir.resolve("excel")));
        reportService = new ReportServiceImpl(bookService, studentService, facultyService, fineService,
                issueRepository, returnRepository, bookCopyRepository, dashboardService, holderResolver, reportFactory);

        MembershipType type = new MembershipType("STUDENT_STANDARD", 3, 14, BigDecimal.valueOf(5), 1, 2);
        Membership membership = new Membership(type, HolderType.STUDENT, 1L, LocalDate.now(), LocalDate.now().plusDays(300));
        Book book = new Book("978-1", "Clean Code", null, null, null, null, null, null, BigDecimal.valueOf(50), null, null);
        Branch branch = new Branch("Main Library", "MAIN", null, null);
        copy = new BookCopy(book, branch, "BC100", null, null, null, BookCopyCondition.GOOD, null);
        User librarian = new User("librarian", "lib@library.local", "hash", null);
        issue = new Issue(copy, membership, librarian, LocalDateTime.now().minusDays(20), LocalDateTime.now().minusDays(6));

        User studentUser = new User("jdoe", "jdoe@university.edu", "hash", null);
        Student student = new Student(studentUser, "STU1001", "R1001", null);
        lenient().when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
    }

    @Test
    void generatesBooksReport() {
        BookDTO book = BookDTO.builder().id(1L).isbn("978-1").title("Clean Code")
                .authorNames(java.util.Set.of("Robert Martin")).publisherName("Prentice Hall")
                .categoryName("Software").totalCopies(3).availableCopies(2).build();
        when(bookService.search(org.mockito.ArgumentMatchers.any())).thenReturn(new Page<>(List.of(book), 0, 5000, 1));

        ReportDTO report = reportService.generate(ReportCriteriaDTO.builder().reportType(ReportType.BOOKS).build());

        assertEquals("Books Report", report.title());
        assertEquals(1, report.rows().size());
        assertTrue(report.rows().get(0).contains("Clean Code"));
    }

    @Test
    void generatesFacultyReportFilteredByDepartment() {
        FacultyDTO cs = FacultyDTO.builder().id(1L).username("adas").facultyId("F1")
                .department("CS").designation("Professor").build();
        FacultyDTO math = FacultyDTO.builder().id(2L).username("bsen").facultyId("F2")
                .department("Math").designation("Lecturer").build();
        when(facultyService.listAll()).thenReturn(List.of(cs, math));

        ReportDTO report = reportService.generate(ReportCriteriaDTO.builder()
                .reportType(ReportType.FACULTY).department("CS").build());

        assertEquals(1, report.rows().size());
        assertEquals("adas", report.rows().get(0).get(1));
    }

    @Test
    void generatesIssuesReportUsingDateRange() {
        when(issueRepository.findByIssueDateRange(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of(issue));

        ReportDTO report = reportService.generate(ReportCriteriaDTO.builder().reportType(ReportType.ISSUES).build());

        assertEquals(1, report.rows().size());
        assertEquals("Clean Code", report.rows().get(0).get(1));
        assertEquals("jdoe", report.rows().get(0).get(3));
    }

    @Test
    void generatesOverdueReportWithDaysOverdue() {
        when(issueRepository.findOverdueOpenIssues(org.mockito.ArgumentMatchers.any())).thenReturn(List.of(issue));

        ReportDTO report = reportService.generate(ReportCriteriaDTO.builder().reportType(ReportType.OVERDUE).build());

        assertEquals(1, report.rows().size());
        assertEquals("6", report.rows().get(0).get(5));
    }

    @Test
    void generatesInventoryAndLostBooksReports() {
        when(bookCopyRepository.findAll()).thenReturn(List.of(copy));
        when(bookCopyRepository.findByStatus(BookCopyStatus.LOST)).thenReturn(List.of());

        ReportDTO inventory = reportService.generate(ReportCriteriaDTO.builder().reportType(ReportType.INVENTORY).build());
        ReportDTO lost = reportService.generate(ReportCriteriaDTO.builder().reportType(ReportType.LOST_BOOKS).build());

        assertEquals(1, inventory.rows().size());
        assertEquals(0, lost.rows().size());
    }

    @Test
    void generatesPopularBooksReport() {
        when(dashboardService.getPopularBooks(50)).thenReturn(List.of(new PopularBookDTO("Clean Code", 12)));

        ReportDTO report = reportService.generate(ReportCriteriaDTO.builder().reportType(ReportType.POPULAR_BOOKS).build());

        assertEquals(1, report.rows().size());
        assertEquals("12", report.rows().get(0).get(1));
    }

    @Test
    void exportsReportToPdfAndExcel() {
        ReportDTO report = new ReportDTO("Test Report", List.of("Col A", "Col B"),
                List.of(List.of("v1", "v2")), LocalDateTime.now());

        String pdfPath = reportService.export(report, ExportFormat.PDF);
        String excelPath = reportService.export(report, ExportFormat.EXCEL);

        assertTrue(Files.exists(Path.of(pdfPath)));
        assertTrue(Files.exists(Path.of(excelPath)));
    }
}
