package com.university.lms.business;

import com.university.lms.entity.HolderType;

/** A resolved borrower identity, independent of whether they are a student or faculty member. */
public record HolderRef(HolderType holderType, Long holderId, String displayName) {
}
