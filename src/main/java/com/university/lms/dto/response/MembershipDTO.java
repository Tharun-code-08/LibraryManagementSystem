package com.university.lms.dto.response;

import java.time.LocalDate;

public record MembershipDTO(Long id, String holderType, Long holderId, String membershipTypeName,
                             LocalDate startDate, LocalDate expiryDate, String status) {
}
