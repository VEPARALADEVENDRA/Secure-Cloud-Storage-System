package com.cloudstorage.service;

import com.cloudstorage.model.FileMetadata;
import com.cloudstorage.model.User;
import com.cloudstorage.repository.FileMetadataRepository;
import com.cloudstorage.repository.UserRepository;
import com.cloudstorage.util.AESUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class FileService {

    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${cloud.aws.path}")
    private String awsPath;

    @Value("${cloud.azure.path}")
    private String azurePath;

    @Value("${cloud.gcp.path}")
    private String gcpPath;

    private static final int CHUNK_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String LOCK_SUFFIX = ".lock";

    public String validateFields(String fullName, String email, String phone, MultipartFile file) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "Full name is required.";
        }
        if (email == null || !email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            return "Invalid email format.";
        }
        if (phone == null || !phone.matches("^[0-9]{10}$")) {
            return "Phone number must be 10 digits.";
        }
        if (file.isEmpty()) {
            return "File is required.";
        }
        if (file.getSize() > 500 * 1024 * 1024) { // 500MB
            return "File size cannot exceed 500MB.";
        }
        return "";
    }

    public FileMetadata processFileUpload(String fullName, String email, String phone, String purpose, String accessRights, MultipartFile file, List<String> cloudProviderNames) throws Exception {
        // Find or create user
        User user = userRepository.findByEmail(email);
        if (user == null) {
            user = new User();
            user.setName(fullName);
            user.setEmail(email);
            user.setPhone(phone);
            user = userRepository.save(user);
        }

        // Conflict detection
        if (detectWriteConflicts(user.getId(), file.getOriginalFilename())) {
            throw new IOException("Upload in progress by another user or conflict detected.");
        }

        Path lockFilePath = Paths.get(uploadDir, file.getOriginalFilename() + LOCK_SUFFIX);
        try {
            Files.createFile(lockFilePath); // Create lock file

            byte[] fileBytes = file.getBytes();
            int chunkCount = (int) Math.ceil((double) fileBytes.length / CHUNK_SIZE);
            String encryptionKey = AESUtils.generateKey();
            List<String> chunkLocations = new ArrayList<>();
            List<String> actualCloudProviders = new ArrayList<>();

            // Ensure cloud directories exist
            Files.createDirectories(Paths.get(awsPath));
            Files.createDirectories(Paths.get(azurePath));
            Files.createDirectories(Paths.get(gcpPath));

            String[] availableCloudPaths = {awsPath, azurePath, gcpPath}; // Add more if needed
            int cloudProviderIndex = 0;

            for (int i = 0; i < chunkCount; i++) {
                int start = i * CHUNK_SIZE;
                int length = Math.min(fileBytes.length - start, CHUNK_SIZE);
                byte[] chunk = Arrays.copyOfRange(fileBytes, start, start + length);
                byte[] encryptedChunk = AESUtils.encryptChunk(chunk, encryptionKey);

                // Round-robin distribution for simplicity
                String selectedCloudPath = availableCloudPaths[cloudProviderIndex % availableCloudPaths.length];
                String chunkFileName = UUID.randomUUID().toString() + "_chunk_" + i;
                Path chunkPath = Paths.get(selectedCloudPath, chunkFileName);
                Files.write(chunkPath, encryptedChunk);
                chunkLocations.add(chunkPath.toString());
                if(!actualCloudProviders.contains(getCloudProviderNameFromPath(selectedCloudPath))){
                    actualCloudProviders.add(getCloudProviderNameFromPath(selectedCloudPath));
                }
                cloudProviderIndex++;
            }

            return generateMetadata(file.getOriginalFilename(), user.getId(), chunkCount, chunkLocations, encryptionKey, accessRights, String.join(",", actualCloudProviders));
        } finally {
            Files.deleteIfExists(lockFilePath); // Delete lock file
        }
    }
    private String getCloudProviderNameFromPath(String path) {
        if (path.equals(awsPath)) return "AWS";
        if (path.equals(azurePath)) return "Azure";
        if (path.equals(gcpPath)) return "GCP";
        return "Unknown";
    }

    public List<FileMetadata> getUserFiles(Long userId) {
        return fileMetadataRepository.findByUserId(userId);
    }

    public byte[] downloadFile(Long fileId) throws Exception {
        FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new IOException("File not found"));

        // TODO: Implement access rights verification here if needed

        List<String> chunkLocations = Arrays.asList(metadata.getChunkStoragePaths().split(",")); // Assuming paths are stored comma-separated
        String encryptionKey = metadata.getEncryptionKey();
        List<byte[]> decryptedChunks = new ArrayList<>();

        for (String chunkLocation : chunkLocations) {
            byte[] encryptedChunk = Files.readAllBytes(Paths.get(chunkLocation));
            decryptedChunks.add(AESUtils.decryptChunk(encryptedChunk, encryptionKey));
        }

        // Combine chunks
        int totalSize = 0;
        for (byte[] chunk : decryptedChunks) {
            totalSize += chunk.length;
        }
        byte[] combinedFile = new byte[totalSize];
        int currentPosition = 0;
        for (byte[] chunk : decryptedChunks) {
            System.arraycopy(chunk, 0, combinedFile, currentPosition, chunk.length);
            currentPosition += chunk.length;
        }

        // Zip the file (optional, but good for sending single file downloads)
        Path tempZipFile = Files.createTempFile("download", ".zip");
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempZipFile.toFile()))) {
            ZipEntry zipEntry = new ZipEntry(metadata.getOriginalFilename());
            zos.putNextEntry(zipEntry);
            zos.write(combinedFile);
            zos.closeEntry();
        }
        byte[] zipBytes = Files.readAllBytes(tempZipFile);
        Files.delete(tempZipFile);
        return zipBytes;
    }


    private FileMetadata generateMetadata(String originalFilename, Long userId, int chunkCount, List<String> chunkLocations, String encryptionKey, String accessRights, String cloudProviders) {
        FileMetadata metadata = new FileMetadata();
        metadata.setUserId(userId);
        metadata.setOriginalFilename(originalFilename);
        metadata.setEncryptedChunkCount(chunkCount);
        metadata.setEncryptionKey(encryptionKey); // Key is already Base64 encoded by AESUtils
        metadata.setAccessRights(accessRights);
        metadata.setCloudProviders(cloudProviders);
        metadata.setTimestamp(LocalDateTime.now());
        metadata.setChunkStoragePaths(String.join(",", chunkLocations)); // Store chunk paths
        return fileMetadataRepository.save(metadata);
    }

    public boolean detectWriteConflicts(Long userId, String fileName) {
        Path lockFilePath = Paths.get(uploadDir, fileName + LOCK_SUFFIX);
        return Files.exists(lockFilePath);
        // More sophisticated conflict detection could involve checking active uploads for the same user/file
        // or using a distributed lock manager if scaling further.
    }
}