package com.university.lms.service.report;

import com.university.lms.dto.request.ReportCriteriaDTO;
import com.university.lms.dto.response.ReportDTO;
import com.university.lms.util.ExportFormat;

public interface ReportService {

    ReportDTO generate(ReportCriteriaDTO criteria);

    /** Exports an already-generated report and returns the absolute path of the written file. */
    String export(ReportDTO report, ExportFormat format);
}
