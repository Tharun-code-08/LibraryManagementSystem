package com.university.lms.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import javafx.concurrent.Task;

/**
 * Runs service/repository calls off the JavaFX Application Thread and marshals the result (or
 * failure) back onto it. Every screen that calls into the service layer must go through this
 * utility instead of invoking services directly from an FXML event handler, keeping the UI
 * responsive under database latency.
 */
public final class AsyncExecutor {

    private final ExecutorService workerPool = Executors.newCachedThreadPool(runnable -> {
        Thread thread = new Thread(runnable, "lms-async-worker");
        thread.setDaemon(true);
        return thread;
    });

    public <T> void run(Callable<T> work, Consumer<T> onSuccess, Consumer<Throwable> onFailure) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return work.call();
            }
        };
        task.setOnSucceeded(event -> onSuccess.accept(task.getValue()));
        task.setOnFailed(event -> onFailure.accept(task.getException()));
        workerPool.submit(task);
    }

    public void shutdown() {
        workerPool.shutdown();
    }
}
