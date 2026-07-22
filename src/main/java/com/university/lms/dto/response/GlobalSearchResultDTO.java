package com.university.lms.dto.response;

/** A single hit in the cross-entity global search overlay. {@code entityType} is one of
 *  {@code BOOK}, {@code AUTHOR}, {@code STUDENT}, {@code FACULTY}. */
public record GlobalSearchResultDTO(String entityType, Long id, String title, String subtitle) {
}
