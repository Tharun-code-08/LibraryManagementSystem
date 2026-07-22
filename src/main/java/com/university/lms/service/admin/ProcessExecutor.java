package com.university.lms.service.admin;

import java.io.IOException;

/** Thin, swappable seam over external process execution — lets {@code BackupServiceImpl} be
 *  unit-tested without actually invoking the {@code mysqldump}/{@code mysql} CLI tools. */
public interface ProcessExecutor {

    /** Starts the given process builder and blocks until it exits. @return the process exit code. */
    int run(ProcessBuilder processBuilder) throws IOException, InterruptedException;
}
