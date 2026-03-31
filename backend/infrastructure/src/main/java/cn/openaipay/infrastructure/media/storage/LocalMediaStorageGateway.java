package cn.openaipay.infrastructure.media.storage;

import cn.openaipay.application.media.port.MediaLoadedObject;
import cn.openaipay.application.media.port.MediaStoragePort;
import cn.openaipay.application.media.port.MediaStoredObject;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 本地媒体存储网关
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Component
public class LocalMediaStorageGateway implements MediaStoragePort {

    /** 日期信息 */
    private static final DateTimeFormatter DATE_PATH_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd", Locale.ROOT);

    /** basepath信息 */
    private final Path basePath;
    /** 最大信息 */
    private final long maxImageBytes;
    /** 最大信息 */
    private final int compressMaxWidth;
    /** jpegquality信息 */
    private final float jpegQuality;

    public LocalMediaStorageGateway(@Value("${aipay.media.storage-base-path:./media-storage}") String storageBasePath,
                                    @Value("${aipay.media.max-image-bytes:10485760}") long maxImageBytes,
                                    @Value("${aipay.media.compress-max-width:1280}") int compressMaxWidth,
                                    @Value("${aipay.media.jpeg-quality:0.82}") float jpegQuality) {
        this.basePath = resolveBasePath(storageBasePath);
        this.maxImageBytes = Math.max(1024L, maxImageBytes);
        this.compressMaxWidth = Math.max(320, compressMaxWidth);
        this.jpegQuality = Math.min(1.0f, Math.max(0.1f, jpegQuality));
    }

    /**
     * 处理业务数据。
     */
    @Override
    public MediaStoredObject storeImage(Long ownerUserId, String originalName, String mimeType, byte[] content) {
        if (content == null || content.length == 0) {
            throw new IllegalArgumentException("image content must not be empty");
        }
        if (content.length > maxImageBytes) {
            throw new IllegalArgumentException("image size exceeds max limit");
        }

        BufferedImage sourceImage = readImage(content);
        String format = resolveFormat(mimeType, originalName);
        BufferedImage compressedImage = compressImage(sourceImage, format);

        byte[] storedBytes = writeImage(compressedImage, format);
        String extension = resolveExtension(format);
        String datePath = DATE_PATH_FORMATTER.format(LocalDate.now());
        String fileName = ownerUserId + "_" + System.currentTimeMillis()
                + "_" + String.format(Locale.ROOT, "%03d", ThreadLocalRandom.current().nextInt(0, 1000))
                + "." + extension;

        Path relativePath = Paths.get(datePath, fileName);
        Path absolutePath = basePath.resolve(relativePath).normalize();
        ensureSafePath(absolutePath);
        try {
            Files.createDirectories(absolutePath.getParent());
            Files.write(absolutePath, storedBytes);
        } catch (IOException ex) {
            throw new IllegalStateException("store image failed: " + ex.getMessage(), ex);
        }

        String sha256 = sha256(storedBytes);
        String normalizedMimeType = resolveMimeType(format, mimeType);
        return new MediaStoredObject(
                relativePath.toString().replace('\\', '/'),
                null,
                normalizedMimeType,
                content.length,
                (long) storedBytes.length,
                compressedImage.getWidth(),
                compressedImage.getHeight(),
                sha256
        );
    }

    /**
     * 加载业务数据。
     */
    @Override
    public MediaLoadedObject loadFile(String storagePath, String fallbackMimeType) {
        if (storagePath == null || storagePath.isBlank()) {
            throw new IllegalArgumentException("storagePath must not be blank");
        }
        Path absolutePath = basePath.resolve(storagePath).normalize();
        ensureSafePath(absolutePath);
        try {
            byte[] content = Files.readAllBytes(absolutePath);
            String mimeType = Files.probeContentType(absolutePath);
            if (mimeType == null || mimeType.isBlank()) {
                mimeType = fallbackMimeType;
            }
            if (mimeType == null || mimeType.isBlank()) {
                mimeType = "application/octet-stream";
            }
            return new MediaLoadedObject(mimeType, content);
        } catch (IOException ex) {
            throw new IllegalStateException("load image failed: " + ex.getMessage(), ex);
        }
    }

    private BufferedImage readImage(byte[] content) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(content)) {
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null) {
                throw new IllegalArgumentException("unsupported image format");
            }
            return image;
        } catch (IOException ex) {
            throw new IllegalStateException("read image failed: " + ex.getMessage(), ex);
        }
    }

    private BufferedImage compressImage(BufferedImage sourceImage, String format) {
        int sourceWidth = sourceImage.getWidth();
        int sourceHeight = sourceImage.getHeight();
        if (sourceWidth <= compressMaxWidth) {
            return convertByFormat(sourceImage, format);
        }
        int targetWidth = compressMaxWidth;
        int targetHeight = Math.max(1, sourceHeight * targetWidth / sourceWidth);

        Image scaled = sourceImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        BufferedImage target = format.equals("jpg")
                ? new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB)
                : new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics = target.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (format.equals("jpg")) {
            graphics.setComposite(AlphaComposite.SrcOver);
            graphics.setBackground(java.awt.Color.WHITE);
            graphics.clearRect(0, 0, targetWidth, targetHeight);
        }
        graphics.drawImage(scaled, 0, 0, null);
        graphics.dispose();
        return target;
    }

    private BufferedImage convertByFormat(BufferedImage source, String format) {
        if (!"jpg".equals(format)) {
            return source;
        }
        BufferedImage converted = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = converted.createGraphics();
        graphics.setComposite(AlphaComposite.SrcOver);
        graphics.setBackground(java.awt.Color.WHITE);
        graphics.clearRect(0, 0, converted.getWidth(), converted.getHeight());
        graphics.drawImage(source, 0, 0, null);
        graphics.dispose();
        return converted;
    }

    private byte[] writeImage(BufferedImage image, String format) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            if ("jpg".equals(format)) {
                Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
                if (!writers.hasNext()) {
                    throw new IllegalStateException("jpg writer not found");
                }
                ImageWriter writer = writers.next();
                ImageWriteParam writeParam = writer.getDefaultWriteParam();
                if (writeParam.canWriteCompressed()) {
                    writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    writeParam.setCompressionQuality(jpegQuality);
                }
                try (MemoryCacheImageOutputStream imageOutput = new MemoryCacheImageOutputStream(outputStream)) {
                    writer.setOutput(imageOutput);
                    writer.write(null, new IIOImage(image, null, null), writeParam);
                    writer.dispose();
                }
            } else {
                ImageIO.write(image, format, outputStream);
            }
            return outputStream.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("write image failed: " + ex.getMessage(), ex);
        }
    }

    private String resolveFormat(String mimeType, String originalName) {
        String normalizedMimeType = mimeType == null ? "" : mimeType.toLowerCase(Locale.ROOT);
        if (normalizedMimeType.contains("png")) {
            return "png";
        }
        if (normalizedMimeType.contains("jpg") || normalizedMimeType.contains("jpeg")) {
            return "jpg";
        }

        String lowerName = originalName == null ? "" : originalName.toLowerCase(Locale.ROOT);
        if (lowerName.endsWith(".png")) {
            return "png";
        }
        if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
            return "jpg";
        }
        return "jpg";
    }

    private Path resolveBasePath(String storageBasePath) {
        String normalizedConfig = storageBasePath == null || storageBasePath.isBlank() ? "./media-storage" : storageBasePath.trim();
        Path configuredPath = Paths.get(normalizedConfig);
        if (configuredPath.isAbsolute()) {
            return configuredPath.toAbsolutePath().normalize();
        }

        Path userDir = Paths.get(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        if (isDefaultStorageBasePath(normalizedConfig)) {
            Path stablePath = resolveStableDefaultBasePath(userDir);
            if (stablePath != null) {
                return stablePath;
            }
        }
        return userDir.resolve(configuredPath).normalize();
    }

    private boolean isDefaultStorageBasePath(String storageBasePath) {
        String normalized = storageBasePath.replace('\\', '/');
        return "media-storage".equals(normalized) || "./media-storage".equals(normalized);
    }

    private Path resolveStableDefaultBasePath(Path userDir) {
        if (isAdapterWebDirectory(userDir)) {
            return userDir.resolve("media-storage").normalize();
        }

        Path adapterWebDir = userDir.resolve("backend").resolve("adapter-web");
        if (Files.isDirectory(adapterWebDir)) {
            return adapterWebDir.resolve("media-storage").normalize();
        }
        return null;
    }

    private boolean isAdapterWebDirectory(Path path) {
        Path fileName = path.getFileName();
        Path parent = path.getParent();
        return fileName != null
                && parent != null
                && "adapter-web".equals(fileName.toString())
                && parent.getFileName() != null
                && "backend".equals(parent.getFileName().toString());
    }

    private String resolveExtension(String format) {
        return "png".equals(format) ? "png" : "jpg";
    }

    private String resolveMimeType(String format, String fallbackMimeType) {
        if ("png".equals(format)) {
            return "image/png";
        }
        if ("jpg".equals(format)) {
            return "image/jpeg";
        }
        return fallbackMimeType == null || fallbackMimeType.isBlank() ? "application/octet-stream" : fallbackMimeType;
    }

    private void ensureSafePath(Path absolutePath) {
        if (!absolutePath.startsWith(basePath)) {
            throw new IllegalArgumentException("invalid storage path");
        }
    }

    private String sha256(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(content);
            StringBuilder builder = new StringBuilder(bytes.length * 2);
            for (byte value : bytes) {
                builder.append(String.format(Locale.ROOT, "%02x", value));
            }
            return builder.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("sha256 compute failed: " + ex.getMessage(), ex);
        }
    }
}
