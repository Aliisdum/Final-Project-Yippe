package az.developia.flight_booking_name.controller;

import az.developia.flight_booking_name.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/files")
@AllArgsConstructor
@Tag(name = "File Management", description = "File download APIs")
public class FileController {

    private FileStorageService fileStorageService;

    @GetMapping("/download/{fileName:.+}")
    @Operation(summary = "Download file", description = "Download file by filename")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        Resource resource = fileStorageService.loadFileAsResource(fileName);
        String contentType = "application/octet-stream";

        try {
            Path filePath = Paths.get("uploads").resolve(fileName).normalize();
            String detectedType = Files.probeContentType(filePath);
            if (detectedType != null) {
                contentType = detectedType;
            }
        } catch (IOException ignored) {
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
