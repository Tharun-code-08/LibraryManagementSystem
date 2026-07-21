package com.university.lms.exception;

import java.util.function.Consumer;

import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application-wide safety net. Installed as the {@link Thread.UncaughtExceptionHandler} for
 * the JavaFX Application Thread (and any {@code AsyncExecutor} worker threads) so that no
 * unhandled exception can crash the UI. {@link BusinessException}s are shown to the user with
 * their own message; anything else is logged in full and surfaced as a generic friendly error.
 */
public final class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final Consumer<String> userFacingErrorPresenter;

    public GlobalExceptionHandler(Consumer<String> userFacingErrorPresenter) {
        this.userFacingErrorPresenter = userFacingErrorPresenter;
    }

    public void install() {
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        String friendlyMessage = throwable instanceof BusinessException
                ? throwable.getMessage()
                : "Something went wrong. Please try again, or contact support if the problem persists.";

        log.error("Uncaught exception on thread '{}'", thread.getName(), throwable);
        Platform.runLater(() -> userFacingErrorPresenter.accept(friendlyMessage));
    }
}
