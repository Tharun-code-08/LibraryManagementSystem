package com.university.lms.business;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.university.lms.entity.Book;
import com.university.lms.entity.BookCopy;
import com.university.lms.entity.BookCopyCondition;
import com.university.lms.entity.HolderType;
import com.university.lms.entity.Issue;
import com.university.lms.entity.Membership;
import com.university.lms.entity.MembershipType;
import com.university.lms.entity.ReturnCondition;
import com.university.lms.entity.User;

class OverdueFineStrategyTest {

    private final OverdueFineStrategy strategy = new OverdueFineStrategy();

    private Issue buildIssue(LocalDateTime dueDate, BigDecimal bookCost, int gracePeriodDays) {
        MembershipType membershipType = new MembershipType("STUDENT_STANDARD", 3, 14, BigDecimal.valueOf(5), gracePeriodDays, 2);
        Membership membership = new Membership(membershipType, HolderType.STUDENT, 1L, LocalDate.now().minusDays(30), LocalDate.now().plusDays(300));
        Book book = new Book("978-1", "Test Book", null, null, null, null, null, null, bookCost, null, null);
        BookCopy copy = new BookCopy(book, null, "BC1", null, null, null, BookCopyCondition.GOOD, null);
        User librarian = new User("libuser", "lib@library.local", "hash", null);
        return new Issue(copy, membership, librarian, dueDate.minusDays(14), dueDate);
    }

    @Test
    void returnsZeroWhenReturnedOnTimeInGoodCondition() {
        Issue issue = buildIssue(LocalDateTime.now().plusDays(5), BigDecimal.valueOf(50), 1);
        BigDecimal fine = strategy.calculate(issue, LocalDateTime.now(), ReturnCondition.GOOD);
        assertEquals(BigDecimal.ZERO, fine);
    }

    @Test
    void chargesPerDayFineBeyondGracePeriod() {
        LocalDateTime dueDate = LocalDateTime.now().minusDays(10);
        Issue issue = buildIssue(dueDate, BigDecimal.valueOf(50), 1);
        // 10 days late - 1 day grace = 9 chargeable days * 5.00/day = 45.00
        BigDecimal fine = strategy.calculate(issue, LocalDateTime.now(), ReturnCondition.GOOD);
        assertEquals(new BigDecimal("45.00"), fine.setScale(2));
    }

    @Test
    void returnsZeroWhenLateDaysExactlyEqualGracePeriod() {
        // 3 days late, 3-day grace period -> daysLate == 0, no fine yet.
        LocalDateTime dueDate = LocalDateTime.now().minusDays(3);
        Issue issue = buildIssue(dueDate, BigDecimal.valueOf(50), 3);
        BigDecimal fine = strategy.calculate(issue, LocalDateTime.now(), ReturnCondition.GOOD);
        assertEquals(BigDecimal.ZERO, fine);
    }

    @Test
    void chargesOneDayFineWhenOneDayPastGracePeriod() {
        // 4 days late, 3-day grace period -> 1 chargeable day * 5.00/day = 5.00
        LocalDateTime dueDate = LocalDateTime.now().minusDays(4);
        Issue issue = buildIssue(dueDate, BigDecimal.valueOf(50), 3);
        BigDecimal fine = strategy.calculate(issue, LocalDateTime.now(), ReturnCondition.GOOD);
        assertEquals(new BigDecimal("5.00"), fine.setScale(2));
    }

    @Test
    void chargesFullBookCostWhenLost() {
        Issue issue = buildIssue(LocalDateTime.now().plusDays(5), BigDecimal.valueOf(50), 1);
        BigDecimal fine = strategy.calculate(issue, LocalDateTime.now(), ReturnCondition.LOST);
        assertEquals(0, BigDecimal.valueOf(50).compareTo(fine));
    }

    @Test
    void chargesHalfBookCostWhenDamaged() {
        Issue issue = buildIssue(LocalDateTime.now().plusDays(5), BigDecimal.valueOf(50), 1);
        BigDecimal fine = strategy.calculate(issue, LocalDateTime.now(), ReturnCondition.DAMAGED);
        assertEquals(0, BigDecimal.valueOf(25).compareTo(fine));
    }
}
