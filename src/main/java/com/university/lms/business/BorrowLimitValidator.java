package com.university.lms.business;

import com.university.lms.entity.MembershipType;
import com.university.lms.exception.BorrowLimitExceededException;

/** Enforces a membership type's maximum concurrent-borrow rule. */
public final class BorrowLimitValidator {

    public void validate(MembershipType membershipType, long currentOpenIssueCount) {
        if (currentOpenIssueCount >= membershipType.getMaxBorrowLimit()) {
            throw new BorrowLimitExceededException(membershipType.getMaxBorrowLimit());
        }
    }
}
