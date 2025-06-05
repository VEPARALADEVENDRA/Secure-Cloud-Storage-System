package com.cloudstorage.controller;

import com.cloudstorage.model.FileMetadata;
import com.cloudstorage.service.FileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/files") // Changed base path for clarity
@Api(tags = "File Management")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    @ApiOperation(value = "Upload a file", notes = "Uploads a file with metadata, encrypts it, and distributes chunks to simulated cloud providers.")
    public ResponseEntity<String> uploadFile(
            @ApiParam(value = "Full name of the uploader", required = true) @RequestParam("fullName") String fullName,
            @ApiParam(value = "Email of the uploader", required = true) @RequestParam("email") String email,
            @ApiParam(value = "Phone number of the uploader (10 digits)", required = true) @RequestParam("phone") String phone,
            @ApiParam(value = "Purpose of storing the file", required = true) @RequestParam("purpose") String purpose,
            @ApiParam(value = "Access rights for the file (Read/Write)", required = true) @RequestParam("accessRights") String accessRights,
            @ApiParam(value = "The file to upload", required = true) @RequestParam("file") MultipartFile file,
            @ApiParam(value = "Selected cloud providers (e.g., AWS, Azure, GCP)", required = true) @RequestParam("cloudProviders") List<String> cloudProviders) {

        String validationMessage = fileService.validateFields(fullName, email, phone, file);
        if (!validationMessage.isEmpty()) {
            return ResponseEntity.badRequest().body(validationMessage);
        }

        try {
            FileMetadata metadata = fileService.processFileUpload(fullName, email, phone, purpose, accessRights, file, cloudProviders);
            return ResponseEntity.status(HttpStatus.CREATED).body("File uploaded successfully. File ID: " + metadata.getId());
        } catch (Exception e) {
            // Log the exception e.g., e.printStackTrace(); or use a logger
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    @ApiOperation(value = "Get files for a user", notes = "Retrieves a list of files uploaded by a specific user.")
    public ResponseEntity<List<FileMetadata>> getUserFiles(
            @ApiParam(value = "ID of the user", required = true) @PathVariable Long userId) {
        try {
            List<FileMetadata> files = fileService.getUserFiles(userId);
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/download/{fileId}")
    @ApiOperation(value = "Download a file", notes = "Downloads a specific file by its ID, decrypting and reassembling it.")
    public ResponseEntity<byte[]> downloadFile(
            @ApiParam(value = "ID of the file to download", required = true) @PathVariable Long fileId) {
        try {
            byte[] fileData = fileService.downloadFile(fileId);
            FileMetadata fileMetadata = fileService.getUserFiles(null).stream().filter(f -> f.getId().equals(fileId)).findFirst().orElse(null); // This is inefficient, get metadata properly
            String fileName = (fileMetadata != null) ? fileMetadata.getOriginalFilename() : "downloaded_file";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", fileName + ".zip"); // Sending as zip

            return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}