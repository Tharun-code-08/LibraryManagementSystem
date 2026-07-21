package com.university.lms.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Copies user-selected files (student/faculty photos, book covers) into a managed asset directory. */
public final class FileStorageUtil {

    private final Path baseDirectory;

    public FileStorageUtil(Path baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    /** Copies {@code sourceFile} into the managed directory under {@code fileNameWithoutExtension}
     *  plus the source file's own extension, returning the stored path. */
    public String store(Path sourceFile, String fileNameWithoutExtension) {
        try {
            Files.createDirectories(baseDirectory);
            String sourceName = sourceFile.getFileName().toString();
            String extension = sourceName.contains(".") ? sourceName.substring(sourceName.lastIndexOf('.')) : "";
            Path target = baseDirectory.resolve(fileNameWithoutExtension + extension);
            Files.copy(sourceFile, target, StandardCopyOption.REPLACE_EXISTING);
            return target.toString();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store file: " + sourceFile, e);
        }
    }
}
