package tn.esprit.spring.smartcity.report;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class PhotoUploadService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private Path uploadPath;

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp");
    private static final Set<String> ALLOWED_VIDEO_TYPES = Set.of(
            "video/mp4", "video/webm", "video/quicktime");
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024;
    private static final long MAX_VIDEO_SIZE = 30 * 1024 * 1024;

    @PostConstruct
    public void init() {
        uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    public String uploadPhoto(MultipartFile file) {
        if (file.getContentType() == null || !ALLOWED_IMAGE_TYPES.contains(file.getContentType())) {
            throw new RuntimeException("Invalid image type. Allowed: JPEG, PNG, GIF, WebP");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new RuntimeException("Image size exceeds 10MB limit");
        }
        return storeFile(file);
    }

    public String uploadVideo(MultipartFile file) {
        if (file.getContentType() == null || !ALLOWED_VIDEO_TYPES.contains(file.getContentType())) {
            throw new RuntimeException("Invalid video type. Allowed: MP4, WebM, QuickTime");
        }
        if (file.getSize() > MAX_VIDEO_SIZE) {
            throw new RuntimeException("Video size exceeds 30MB limit");
        }
        return storeFile(file);
    }

    private String storeFile(MultipartFile file) {
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path destination = uploadPath.resolve(filename);
        try {
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            return filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    public Path getFilePath(String filename) {
        return uploadPath.resolve(filename).normalize();
    }
}
