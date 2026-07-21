package com.university.lms.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.university.lms.dto.request.BookImportRowDTO;

/**
 * Parses the book bulk-import CSV format: a header row followed by data rows with columns
 * {@code isbn,title,authors,publisher,category,edition,language,cost} (authors is a single
 * field with multiple names separated by {@code ;}). No external CSV library is pulled in for
 * a format this simple; quoted fields containing commas are supported.
 */
public final class CsvImportUtil {

    private static final Pattern CSV_SPLIT = Pattern.compile(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

    private CsvImportUtil() {
    }

    public static List<BookImportRowDTO> parseBookRows(Reader reader) {
        List<BookImportRowDTO> rows = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            String line = bufferedReader.readLine(); // header, skipped
            int rowNumber = 1;
            while ((line = bufferedReader.readLine()) != null) {
                rowNumber++;
                if (line.isBlank()) {
                    continue;
                }
                rows.add(parseRow(rowNumber, line));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read CSV import file", e);
        }
        return rows;
    }

    private static BookImportRowDTO parseRow(int rowNumber, String line) {
        String[] fields = CSV_SPLIT.split(line, -1);
        String isbn = field(fields, 0);
        String title = field(fields, 1);
        String authors = field(fields, 2);
        String publisher = field(fields, 3);
        String category = field(fields, 4);
        String edition = field(fields, 5);
        String language = field(fields, 6);
        String costRaw = field(fields, 7);
        BigDecimal cost = costRaw == null || costRaw.isBlank() ? BigDecimal.ZERO : new BigDecimal(costRaw.trim());
        return new BookImportRowDTO(rowNumber, isbn, title, authors, publisher, category, edition, language, cost);
    }

    private static String field(String[] fields, int index) {
        if (index >= fields.length) {
            return null;
        }
        String value = fields[index].trim();
        if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
            value = value.substring(1, value.length() - 1);
        }
        return value;
    }
}
