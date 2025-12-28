package com.ecommerce.review.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

/**
 * Service for handling image upload, processing, and management for reviews.
 * In production, you might want to integrate with cloud storage services like AWS S3, Google Cloud Storage, or Azure Blob Storage.
 */
@Service
@RequiredArgsConstructor
public class ImageService {

    private static final Logger log = LoggerFactory.getLogger(ImageService.class);

    @Value("${review.images.upload-dir:/tmp/review-images}")
    private String uploadDirectory;
    
    @Value("${review.images.max-size:5242880}") // 5MB default
    private long maxFileSize;
    
    @Value("${review.images.max-count:5}")
    private int maxImageCount;
    
    @Value("${review.images.allowed-types:jpg,jpeg,png,gif,webp}")
    private String allowedTypes;
    
    @Value("${review.images.thumbnail-size:200}")
    private int thumbnailSize;
    
    @Value("${review.images.max-dimension:1920}")
    private int maxDimension;
    
    @Value("${server.base-url:http://localhost:8080}")
    private String baseUrl;
    
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );
    
    /**
     * Upload multiple images for a review.
     */
    public List<String> uploadImages(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return new ArrayList<>();
        }
        
        if (files.size() > maxImageCount) {
            throw new IllegalArgumentException(
                String.format("Maximum %d images allowed, but %d provided", maxImageCount, files.size()));
        }
        
        List<String> imageUrls = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                String imageUrl = uploadSingleImage(file);
                imageUrls.add(imageUrl);
            } catch (Exception e) {
                log.error("Failed to upload image: {}", file.getOriginalFilename(), e);
                // Clean up already uploaded images on failure
                deleteImages(imageUrls);
                throw new RuntimeException("Failed to upload images: " + e.getMessage(), e);
            }
        }
        
        log.info("Successfully uploaded {} images", imageUrls.size());
        return imageUrls;
    }
    
    /**
     * Upload a single image.
     */
    private String uploadSingleImage(MultipartFile file) throws IOException {
        // Validate file
        validateImage(file);
        
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = generateUniqueFilename(fileExtension);
        
        // Create subdirectory based on date
        String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        Path datePath = uploadPath.resolve(dateDir);
        if (!Files.exists(datePath)) {
            Files.createDirectories(datePath);
        }
        
        Path filePath = datePath.resolve(uniqueFilename);
        
        // Process and save image
        BufferedImage processedImage = processImage(file);
        saveImage(processedImage, filePath.toString(), fileExtension);
        
        // Generate thumbnail
        String thumbnailFilename = "thumb_" + uniqueFilename;
        Path thumbnailPath = datePath.resolve(thumbnailFilename);
        BufferedImage thumbnail = createThumbnail(processedImage);
        saveImage(thumbnail, thumbnailPath.toString(), fileExtension);
        
        // Return URL path
        String relativePath = dateDir + "/" + uniqueFilename;
        String imageUrl = baseUrl + "/api/reviews/images/" + relativePath;
        
        log.debug("Image uploaded successfully: {}", imageUrl);
        return imageUrl;
    }
    
    /**
     * Validate uploaded image file.
     */
    private void validateImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException(
                String.format("File size exceeds maximum allowed size of %d bytes", maxFileSize));
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                "Invalid file type. Allowed types: " + String.join(", ", ALLOWED_MIME_TYPES));
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid filename");
        }
        
        String extension = getFileExtension(filename).toLowerCase();
        List<String> allowedExtensions = Arrays.asList(allowedTypes.split(","));
        if (!allowedExtensions.contains(extension)) {
            throw new IllegalArgumentException(
                "Invalid file extension. Allowed extensions: " + allowedTypes);
        }
    }
    
    /**
     * Process image (resize if needed, optimize).
     */
    private BufferedImage processImage(MultipartFile file) throws IOException {
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        
        if (originalImage == null) {
            throw new IllegalArgumentException("Invalid image file");
        }
        
        // Check if resizing is needed
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        
        if (width <= maxDimension && height <= maxDimension) {
            return originalImage;
        }
        
        // Calculate new dimensions maintaining aspect ratio
        double aspectRatio = (double) width / height;
        int newWidth, newHeight;
        
        if (width > height) {
            newWidth = maxDimension;
            newHeight = (int) (maxDimension / aspectRatio);
        } else {
            newHeight = maxDimension;
            newWidth = (int) (maxDimension * aspectRatio);
        }
        
        return resizeImage(originalImage, newWidth, newHeight);
    }
    
    /**
     * Resize image to specified dimensions.
     */
    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();
        
        // Set rendering hints for better quality
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();
        
        return resizedImage;
    }
    
    /**
     * Create thumbnail image.
     */
    private BufferedImage createThumbnail(BufferedImage originalImage) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        
        // Calculate dimensions for square thumbnail
        int size = Math.min(width, height);
        int x = (width - size) / 2;
        int y = (height - size) / 2;
        
        // Crop to square
        BufferedImage croppedImage = originalImage.getSubimage(x, y, size, size);
        
        // Resize to thumbnail size
        return resizeImage(croppedImage, thumbnailSize, thumbnailSize);
    }
    
    /**
     * Save image to file system.
     */
    private void saveImage(BufferedImage image, String filePath, String format) throws IOException {
        File outputFile = new File(filePath);
        
        // Ensure parent directories exist
        outputFile.getParentFile().mkdirs();
        
        // Convert format for ImageIO
        String imageFormat = format.equals("jpg") ? "jpeg" : format;
        
        if (!ImageIO.write(image, imageFormat, outputFile)) {
            throw new IOException("Failed to write image in format: " + imageFormat);
        }
    }
    
    /**
     * Delete images by URLs.
     */
    public void deleteImages(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }
        
        for (String imageUrl : imageUrls) {
            try {
                deleteImage(imageUrl);
            } catch (Exception e) {
                log.error("Failed to delete image: {}", imageUrl, e);
            }
        }
    }
    
    /**
     * Delete a single image.
     */
    private void deleteImage(String imageUrl) {
        try {
            // Extract relative path from URL
            String relativePath = extractRelativePathFromUrl(imageUrl);
            if (relativePath == null) {
                log.warn("Could not extract path from URL: {}", imageUrl);
                return;
            }
            
            Path imagePath = Paths.get(uploadDirectory, relativePath);
            Path thumbnailPath = imagePath.getParent().resolve("thumb_" + imagePath.getFileName());
            
            // Delete main image
            if (Files.exists(imagePath)) {
                Files.delete(imagePath);
                log.debug("Deleted image: {}", imagePath);
            }
            
            // Delete thumbnail
            if (Files.exists(thumbnailPath)) {
                Files.delete(thumbnailPath);
                log.debug("Deleted thumbnail: {}", thumbnailPath);
            }
            
        } catch (Exception e) {
            log.error("Error deleting image: {}", imageUrl, e);
        }
    }
    
    /**
     * Get image file as byte array.
     */
    public byte[] getImage(String relativePath) throws IOException {
        Path imagePath = Paths.get(uploadDirectory, relativePath);
        
        if (!Files.exists(imagePath)) {
            throw new FileNotFoundException("Image not found: " + relativePath);
        }
        
        return Files.readAllBytes(imagePath);
    }
    
    /**
     * Get thumbnail image as byte array.
     */
    public byte[] getThumbnail(String relativePath) throws IOException {
        Path imagePath = Paths.get(uploadDirectory, relativePath);
        String filename = imagePath.getFileName().toString();
        Path thumbnailPath = imagePath.getParent().resolve("thumb_" + filename);
        
        if (!Files.exists(thumbnailPath)) {
            throw new FileNotFoundException("Thumbnail not found: " + relativePath);
        }
        
        return Files.readAllBytes(thumbnailPath);
    }
    
    /**
     * Generate unique filename.
     */
    private String generateUniqueFilename(String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("%s_%s.%s", timestamp, uuid, extension);
    }
    
    /**
     * Get file extension from filename.
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
    
    /**
     * Extract relative path from image URL.
     */
    private String extractRelativePathFromUrl(String imageUrl) {
        if (imageUrl == null) {
            return null;
        }
        
        String prefix = "/api/reviews/images/";
        int index = imageUrl.indexOf(prefix);
        if (index == -1) {
            return null;
        }
        
        return imageUrl.substring(index + prefix.length());
    }
    
    /**
     * Get image metadata.
     */
    public ImageMetadata getImageMetadata(String relativePath) throws IOException {
        Path imagePath = Paths.get(uploadDirectory, relativePath);
        
        if (!Files.exists(imagePath)) {
            throw new FileNotFoundException("Image not found: " + relativePath);
        }
        
        BufferedImage image = ImageIO.read(imagePath.toFile());
        if (image == null) {
            throw new IOException("Could not read image: " + relativePath);
        }
        
        return ImageMetadata.builder()
            .width(image.getWidth())
            .height(image.getHeight())
            .fileSize(Files.size(imagePath))
            .format(getFileExtension(imagePath.getFileName().toString()))
            .build();
    }
    
    /**
     * Clean up old images (for maintenance).
     */
    public void cleanupOldImages(int daysOld) {
        try {
            Path uploadPath = Paths.get(uploadDirectory);
            if (!Files.exists(uploadPath)) {
                return;
            }
            
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
            
            Files.walk(uploadPath)
                .filter(Files::isRegularFile)
                .filter(path -> {
                    try {
                        return Files.getLastModifiedTime(path).toInstant()
                            .isBefore(cutoffDate.atZone(java.time.ZoneId.systemDefault()).toInstant());
                    } catch (IOException e) {
                        return false;
                    }
                })
                .forEach(path -> {
                    try {
                        Files.delete(path);
                        log.debug("Deleted old image: {}", path);
                    } catch (IOException e) {
                        log.error("Failed to delete old image: {}", path, e);
                    }
                });
                
        } catch (IOException e) {
            log.error("Error during image cleanup", e);
        }
    }
    
    /**
     * Data class for image metadata.
     */
    public static class ImageMetadata {
        private int width;
        private int height;
        private long fileSize;
        private String format;
        
        public ImageMetadata() {}
        
        public ImageMetadata(int width, int height, long fileSize, String format) {
            this.width = width;
            this.height = height;
            this.fileSize = fileSize;
            this.format = format;
        }
        
        public static ImageMetadataBuilder builder() {
            return new ImageMetadataBuilder();
        }
        
        public int getWidth() { return width; }
        public void setWidth(int width) { this.width = width; }
        public int getHeight() { return height; }
        public void setHeight(int height) { this.height = height; }
        public long getFileSize() { return fileSize; }
        public void setFileSize(long fileSize) { this.fileSize = fileSize; }
        public String getFormat() { return format; }
        public void setFormat(String format) { this.format = format; }
        
        public static class ImageMetadataBuilder {
            private int width;
            private int height;
            private long fileSize;
            private String format;
            
            public ImageMetadataBuilder width(int width) {
                this.width = width;
                return this;
            }
            
            public ImageMetadataBuilder height(int height) {
                this.height = height;
                return this;
            }
            
            public ImageMetadataBuilder fileSize(long fileSize) {
                this.fileSize = fileSize;
                return this;
            }
            
            public ImageMetadataBuilder format(String format) {
                this.format = format;
                return this;
            }
            
            public ImageMetadata build() {
                return new ImageMetadata(width, height, fileSize, format);
            }
        }
    }
}