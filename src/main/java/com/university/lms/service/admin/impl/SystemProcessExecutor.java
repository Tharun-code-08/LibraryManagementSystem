package com.university.lms.service.admin.impl;

import java.io.IOException;

import com.university.lms.service.admin.ProcessExecutor;

public final class SystemProcessExecutor implements ProcessExecutor {

    @Override
    public int run(ProcessBuilder processBuilder) throws IOException, InterruptedException {
        Process process = processBuilder.start();
        return process.waitFor();
    }
}
