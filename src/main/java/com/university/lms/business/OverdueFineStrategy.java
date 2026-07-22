package com.university.lms.business;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.university.lms.entity.Issue;
import com.university.lms.entity.MembershipType;
import com.university.lms.entity.ReturnCondition;

/**
 * Combines an overdue-days fine (per the borrower's membership type, minus its grace period)
 * with a condition-based fine: the full book cost if lost, half if damaged, nothing if good.
 */
public final class OverdueFineStrategy implements FineCalculationStrategy {

    private static final BigDecimal DAMAGE_FRACTION = BigDecimal.valueOf(0.5);

    @Override
    public BigDecimal calculate(Issue issue, LocalDateTime returnDate, ReturnCondition condition) {
        BigDecimal overdueFine = calculateOverdueFine(issue, returnDate);
        BigDecimal conditionFine = calculateConditionFine(issue, condition);
        return overdueFine.add(conditionFine);
    }

    private BigDecimal calculateOverdueFine(Issue issue, LocalDateTime returnDate) {
        MembershipType membershipType = issue.getMembership().getMembershipType();
        long daysLate = ChronoUnit.DAYS.between(issue.getDueDate().toLocalDate(), returnDate.toLocalDate())
                - membershipType.getGracePeriodDays();
        if (daysLate <= 0) {
            return BigDecimal.ZERO;
        }
        return membershipType.getFinePerDay().multiply(BigDecimal.valueOf(daysLate));
    }

    private BigDecimal calculateConditionFine(Issue issue, ReturnCondition condition) {
        BigDecimal bookCost = issue.getBookCopy().getBook().getCost();
        return switch (condition) {
            case LOST -> bookCost;
            case DAMAGED -> bookCost.multiply(DAMAGE_FRACTION);
            case GOOD -> BigDecimal.ZERO;
        };
    }
}
