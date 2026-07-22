package com.university.lms.util;

import com.university.lms.dto.response.ReportDTO;

/** Renders a generic {@link ReportDTO} to a file under a configured output directory. */
public interface ReportExporter {

    /** @return the absolute path of the written file. */
    String export(ReportDTO report, String fileBaseName);
}
