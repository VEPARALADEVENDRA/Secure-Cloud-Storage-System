package com.cloudstorage.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "file_metadata")
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "encrypted_chunk_count", nullable = false)
    private int encryptedChunkCount;

    @Lob // For potentially long keys or if storing multiple keys/info
    @Column(name = "encryption_key", nullable = false, length = 1024) // Increased length
    private String encryptionKey;

    @Column(name = "access_rights", nullable = false)
    private String accessRights;

    @Column(name = "cloud_providers", nullable = false)
    private String cloudProviders; // Comma-separated names like "AWS,Azure"

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Lob
    @Column(name = "chunk_storage_paths", nullable = false, length = 4096) // To store comma-separated paths of chunks
    private String chunkStoragePaths;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public int getEncryptedChunkCount() {
        return encryptedChunkCount;
    }

    public void setEncryptedChunkCount(int encryptedChunkCount) {
        this.encryptedChunkCount = encryptedChunkCount;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public String getAccessRights() {
        return accessRights;
    }

    public void setAccessRights(String accessRights) {
        this.accessRights = accessRights;
    }

    public String getCloudProviders() {
        return cloudProviders;
    }

    public void setCloudProviders(String cloudProviders) {
        this.cloudProviders = cloudProviders;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getChunkStoragePaths() {
        return chunkStoragePaths;
    }

    public void setChunkStoragePaths(String chunkStoragePaths) {
        this.chunkStoragePaths = chunkStoragePaths;
    }
}