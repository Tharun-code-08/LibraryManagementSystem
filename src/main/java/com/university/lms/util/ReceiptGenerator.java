package com.university.lms.util;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

/** Renders a one-page PDF payment receipt under a configured output directory. */
public final class ReceiptGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");
    private static final float MARGIN = 60f;
    private static final float LINE_HEIGHT = 20f;

    private final Path outputDirectory;

    public ReceiptGenerator(Path outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public String generate(String receiptNumber, String memberName, String bookTitle, BigDecimal amount,
                            String method, LocalDateTime paidAt) {
        try {
            Files.createDirectories(outputDirectory);
            try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage(PDRectangle.A5);
                document.addPage(page);

                PDType1Font titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                PDType1Font bodyFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                float y = page.getMediaBox().getHeight() - MARGIN;

                try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                    y = writeLine(stream, titleFont, 16, y, "Library Management System — Payment Receipt");
                    y -= LINE_HEIGHT / 2;
                    y = writeLine(stream, bodyFont, 11, y, "Receipt No: " + receiptNumber);
                    y = writeLine(stream, bodyFont, 11, y, "Date: " + paidAt.format(DATE_FORMAT));
                    y = writeLine(stream, bodyFont, 11, y, "Member: " + memberName);
                    y = writeLine(stream, bodyFont, 11, y, "Book: " + bookTitle);
                    y = writeLine(stream, bodyFont, 11, y, "Payment Method: " + method);
                    y = writeLine(stream, bodyFont, 11, y, "Amount Paid: " + amount);
                    y -= LINE_HEIGHT;
                    writeLine(stream, bodyFont, 9, y, "Thank you. Please retain this receipt for your records.");
                }

                Path outputPath = outputDirectory.resolve(receiptNumber + ".pdf");
                document.save(outputPath.toFile());
                return outputPath.toString();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate receipt PDF for " + receiptNumber, e);
        }
    }

    private float writeLine(PDPageContentStream stream, PDType1Font font, float fontSize, float y, String text)
            throws IOException {
        stream.beginText();
        stream.setFont(font, fontSize);
        stream.newLineAtOffset(MARGIN, y);
        stream.showText(text);
        stream.endText();
        return y - LINE_HEIGHT;
    }
}
