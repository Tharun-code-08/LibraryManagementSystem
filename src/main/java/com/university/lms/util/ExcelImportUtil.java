package com.university.lms.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.university.lms.dto.request.StudentImportRowDTO;

/**
 * Parses the student bulk-registration Excel format: a header row followed by data rows with
 * columns {@code username, email, studentId, rollNumber, department, year, semester, phone}.
 */
public final class ExcelImportUtil {

    private ExcelImportUtil() {
    }

    public static List<StudentImportRowDTO> parseStudentRows(InputStream inputStream) {
        List<StudentImportRowDTO> rows = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isBlankRow(row)) {
                    continue;
                }
                rows.add(new StudentImportRowDTO(
                        rowIndex + 1,
                        stringValue(row.getCell(0)),
                        stringValue(row.getCell(1)),
                        stringValue(row.getCell(2)),
                        stringValue(row.getCell(3)),
                        stringValue(row.getCell(4)),
                        intValue(row.getCell(5)),
                        intValue(row.getCell(6)),
                        stringValue(row.getCell(7))));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read Excel import file", e);
        }
        return rows;
    }

    private static boolean isBlankRow(Row row) {
        return stringValue(row.getCell(0)) == null && stringValue(row.getCell(1)) == null;
    }

    private static String stringValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((long) cell.getNumericCellValue());
        }
        String value = cell.toString().trim();
        return value.isEmpty() ? null : value;
    }

    private static Integer intValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) cell.getNumericCellValue();
        }
        String value = cell.toString().trim();
        return value.isEmpty() ? null : Integer.parseInt(value);
    }
}
