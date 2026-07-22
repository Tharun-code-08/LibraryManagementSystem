package com.university.lms.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.university.lms.dto.response.ReportDTO;

/** Renders a {@link ReportDTO} as a single-sheet .xlsx workbook via Apache POI. */
public final class ExcelReportExporter implements ReportExporter {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");

    private final Path outputDirectory;

    public ExcelReportExporter(Path outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    @Override
    public String export(ReportDTO report, String fileBaseName) {
        try {
            Files.createDirectories(outputDirectory);
            try (XSSFWorkbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Report");

                Font titleFont = workbook.createFont();
                titleFont.setBold(true);
                titleFont.setFontHeightInPoints((short) 14);
                CellStyle titleStyle = workbook.createCellStyle();
                titleStyle.setFont(titleFont);

                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                CellStyle headerStyle = workbook.createCellStyle();
                headerStyle.setFont(headerFont);

                int rowIndex = 0;
                Row titleRow = sheet.createRow(rowIndex++);
                Cell titleCell = titleRow.createCell(0);
                titleCell.setCellValue(report.title());
                titleCell.setCellStyle(titleStyle);

                Row generatedRow = sheet.createRow(rowIndex++);
                generatedRow.createCell(0).setCellValue("Generated: " + report.generatedAt().format(TIMESTAMP_FORMAT));

                rowIndex++;
                Row headerRow = sheet.createRow(rowIndex++);
                List<String> headers = report.columnHeaders();
                for (int col = 0; col < headers.size(); col++) {
                    Cell cell = headerRow.createCell(col);
                    cell.setCellValue(headers.get(col));
                    cell.setCellStyle(headerStyle);
                }

                for (List<String> dataRow : report.rows()) {
                    Row row = sheet.createRow(rowIndex++);
                    for (int col = 0; col < dataRow.size(); col++) {
                        row.createCell(col).setCellValue(dataRow.get(col) == null ? "" : dataRow.get(col));
                    }
                }

                for (int col = 0; col < headers.size(); col++) {
                    sheet.autoSizeColumn(col);
                }

                Path outputPath = outputDirectory.resolve(fileBaseName + ".xlsx");
                try (FileOutputStream out = new FileOutputStream(outputPath.toFile())) {
                    workbook.write(out);
                }
                return outputPath.toString();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate report Excel file for " + fileBaseName, e);
        }
    }
}
