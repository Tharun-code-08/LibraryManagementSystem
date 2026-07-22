package com.university.lms.dto.request;

import java.math.BigDecimal;

/** One row of a bulk book-catalog CSV import. Author/category/publisher names are resolved
 *  (find-or-create) by the service rather than requiring pre-existing IDs. */
public record BookImportRowDTO(int rowNumber, String isbn, String title, String authorNames,
                                String publisherName, String categoryName, String edition,
                                String language, BigDecimal cost) {
}
