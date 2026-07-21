package com.university.lms.business;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.university.lms.entity.Issue;
import com.university.lms.entity.ReturnCondition;

/** Strategy for computing the fine owed when a book is returned. */
public interface FineCalculationStrategy {

    BigDecimal calculate(Issue issue, LocalDateTime returnDate, ReturnCondition condition);
}
