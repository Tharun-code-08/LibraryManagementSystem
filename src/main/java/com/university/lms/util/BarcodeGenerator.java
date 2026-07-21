package com.university.lms.util;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;

/** Renders Code128 barcode PNGs (used for book-copy labels) under a configured output directory. */
public final class BarcodeGenerator {

    private static final int WIDTH = 300;
    private static final int HEIGHT = 100;

    private final Path outputDirectory;

    public BarcodeGenerator(Path outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public String generate(String value, String fileNameWithoutExtension) {
        try {
            Files.createDirectories(outputDirectory);
            BitMatrix matrix = new Code128Writer().encode(value, BarcodeFormat.CODE_128, WIDTH, HEIGHT);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
            Path outputPath = outputDirectory.resolve(fileNameWithoutExtension + ".png");
            ImageIO.write(image, "PNG", outputPath.toFile());
            return outputPath.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate barcode for value: " + value, e);
        }
    }
}
