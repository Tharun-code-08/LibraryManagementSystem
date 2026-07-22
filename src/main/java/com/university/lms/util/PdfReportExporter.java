package com.university.lms.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import com.university.lms.dto.response.ReportDTO;

/** Renders a {@link ReportDTO} as a paginated landscape-A4 PDF table via PDFBox. */
public final class PdfReportExporter implements ReportExporter {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");
    private static final float MARGIN = 40f;
    private static final float LINE_HEIGHT = 16f;
    private static final float TITLE_SIZE = 16f;
    private static final float HEADER_SIZE = 10f;
    private static final float BODY_SIZE = 9f;

    private final Path outputDirectory;

    public PdfReportExporter(Path outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    @Override
    public String export(ReportDTO report, String fileBaseName) {
        try {
            Files.createDirectories(outputDirectory);
            try (PDDocument document = new PDDocument()) {
                PDType1Font titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                PDType1Font headerFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                PDType1Font bodyFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

                PDRectangle pageSize = new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());
                float contentWidth = pageSize.getWidth() - (2 * MARGIN);
                int columnCount = Math.max(1, report.columnHeaders().size());
                float columnWidth = contentWidth / columnCount;

                PageCursor cursor = new PageCursor(document, pageSize);
                cursor.newPage();
                cursor.writeTitle(titleFont, report);
                cursor.writeHeaderRow(headerFont, report.columnHeaders(), columnWidth);

                for (List<String> row : report.rows()) {
                    cursor.ensureRoom(headerFont, report.columnHeaders(), columnWidth);
                    cursor.writeDataRow(bodyFont, row, columnWidth);
                }
                cursor.close();

                Path outputPath = outputDirectory.resolve(fileBaseName + ".pdf");
                document.save(outputPath.toFile());
                return outputPath.toString();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate report PDF for " + fileBaseName, e);
        }
    }

    /** Tracks the current page/content-stream and vertical cursor across page breaks. */
    private final class PageCursor {
        private final PDDocument document;
        private final PDRectangle pageSize;
        private PDPageContentStream stream;
        private float y;

        PageCursor(PDDocument document, PDRectangle pageSize) {
            this.document = document;
            this.pageSize = pageSize;
        }

        void newPage() throws IOException {
            if (stream != null) {
                stream.close();
            }
            PDPage page = new PDPage(pageSize);
            document.addPage(page);
            stream = new PDPageContentStream(document, page);
            y = pageSize.getHeight() - MARGIN;
        }

        void writeTitle(PDType1Font font, ReportDTO report) throws IOException {
            writeText(font, TITLE_SIZE, MARGIN, y, report.title());
            y -= (LINE_HEIGHT + 4);
            writeText(new PDType1Font(Standard14Fonts.FontName.HELVETICA), BODY_SIZE, MARGIN, y,
                    "Generated: " + report.generatedAt().format(TIMESTAMP_FORMAT));
            y -= (LINE_HEIGHT + 6);
        }

        void writeHeaderRow(PDType1Font font, List<String> headers, float columnWidth) throws IOException {
            float x = MARGIN;
            for (String header : headers) {
                writeText(font, HEADER_SIZE, x, y, truncate(font, header, HEADER_SIZE, columnWidth));
                x += columnWidth;
            }
            y -= LINE_HEIGHT;
        }

        void ensureRoom(PDType1Font headerFont, List<String> headers, float columnWidth) throws IOException {
            if (y - LINE_HEIGHT < MARGIN) {
                newPage();
                writeHeaderRow(headerFont, headers, columnWidth);
            }
        }

        void writeDataRow(PDType1Font font, List<String> row, float columnWidth) throws IOException {
            float x = MARGIN;
            for (String cell : row) {
                writeText(font, BODY_SIZE, x, y, truncate(font, cell == null ? "" : cell, BODY_SIZE, columnWidth));
                x += columnWidth;
            }
            y -= LINE_HEIGHT;
        }

        void writeText(PDType1Font font, float size, float x, float yPos, String text) throws IOException {
            stream.beginText();
            stream.setFont(font, size);
            stream.newLineAtOffset(x, yPos);
            stream.showText(text);
            stream.endText();
        }

        void close() throws IOException {
            if (stream != null) {
                stream.close();
            }
        }
    }

    private String truncate(PDType1Font font, String text, float fontSize, float maxWidth) {
        try {
            float availableWidth = maxWidth - 4;
            if (font.getStringWidth(text) / 1000 * fontSize <= availableWidth) {
                return text;
            }
            String ellipsis = "...";
            StringBuilder builder = new StringBuilder();
            for (char c : text.toCharArray()) {
                String candidate = builder.toString() + c + ellipsis;
                if (font.getStringWidth(candidate) / 1000 * fontSize > availableWidth) {
                    break;
                }
                builder.append(c);
            }
            return builder + ellipsis;
        } catch (IOException e) {
            return text;
        }
    }
}
