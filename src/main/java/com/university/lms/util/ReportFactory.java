package com.university.lms.util;

/** Selects the concrete {@link ReportExporter} for a requested {@link ExportFormat}. */
public final class ReportFactory {

    private final PdfReportExporter pdfReportExporter;
    private final ExcelReportExporter excelReportExporter;

    public ReportFactory(PdfReportExporter pdfReportExporter, ExcelReportExporter excelReportExporter) {
        this.pdfReportExporter = pdfReportExporter;
        this.excelReportExporter = excelReportExporter;
    }

    public ReportExporter exporterFor(ExportFormat format) {
        return switch (format) {
            case PDF -> pdfReportExporter;
            case EXCEL -> excelReportExporter;
        };
    }
}
