package com.university.lms.util;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/** Renders QR-code PNGs (used for book catalog records) under a configured output directory. */
public final class QrCodeGenerator {

    private static final int SIZE = 300;

    private final Path outputDirectory;

    public QrCodeGenerator(Path outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public String generate(String value, String fileNameWithoutExtension) {
        try {
            Files.createDirectories(outputDirectory);
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            BitMatrix matrix = new QRCodeWriter().encode(value, BarcodeFormat.QR_CODE, SIZE, SIZE, hints);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
            Path outputPath = outputDirectory.resolve(fileNameWithoutExtension + ".png");
            ImageIO.write(image, "PNG", outputPath.toFile());
            return outputPath.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate QR code for value: " + value, e);
        }
    }
}
