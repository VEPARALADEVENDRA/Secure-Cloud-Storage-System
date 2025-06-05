# Secure Cloud Storage System

## Overview
The Secure Cloud Storage System is a cloud data storage solution built using Java Spring Boot for the backend, MySQL for the database, and HTML/CSS/JavaScript for the frontend. This system simulates a Cloud-of-Clouds architecture, allowing users to securely upload, store, and retrieve files with AES encryption and metadata management.

## Features
- **File Upload**: Users can upload files with associated metadata.
- **AES Encryption**: Files are encrypted in chunks using AES-256 before being stored.
- **Cloud Provider Simulation**: Files are distributed across simulated cloud providers (AWS, Azure, GCP).
- **Metadata Storage**: Metadata for each file is stored in a MySQL database.
- **File Conflict Prevention**: Mechanisms are in place to prevent simultaneous uploads of the same file.
- **File Retrieval**: Users can download their files, which are decrypted upon retrieval.
- **User-Friendly Frontend**: The frontend is built with Thymeleaf and includes validation for user inputs.

## Project Structure
```
secure-cloud-storage-system
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── cloudstorage
│   │   │           ├── SecureCloudStorageApplication.java
│   │   │           ├── controller
│   │   │           │   └── FileController.java
│   │   │           ├── service
│   │   │           │   └── FileService.java
│   │   │           ├── model
│   │   │           │   ├── User.java
│   │   │           │   ├── FileMetadata.java
│   │   │           │   └── CloudProvider.java
│   │   │           ├── repository
│   │   │           │   ├── UserRepository.java
│   │   │           │   ├── FileMetadataRepository.java
│   │   │           │   └── CloudProviderRepository.java
│   │   │           ├── config
│   │   │           │   └── SwaggerConfig.java
│   │   │           └── util
│   │   │               └── AESUtils.java
│   │   └── resources
│   │       ├── templates
│   │       │   └── upload.html
│   │       ├── static
│   │       │   ├── css
│   │       │   │   └── style.css
│   │       │   └── js
│   │       │       └── upload.js
│   │       └── application.properties
├── pom.xml
└── README.md
```

## Setup Instructions
1. **Clone the Repository**: 
   ```
   git clone <repository-url>
   cd secure-cloud-storage-system
   ```

2. **Database Setup**:
   - Create a MySQL database named `cloud_db`.
   - Update the `application.properties` file with your MySQL credentials.

3. **Build the Project**:
   - Ensure you have Maven installed.
   - Run the following command to build the project:
   ```
   mvn clean install
   ```

4. **Run the Application**:
   - Start the Spring Boot application:
   ```
   mvn spring-boot:run
   ```

5. **Access the Application**:
   - Open your web browser and navigate to `http://localhost:8080/upload` to access the file upload page.

## Usage
- Fill out the form with your details, select a file to upload, and choose the cloud providers for storage.
- The application will validate your inputs and handle the file upload process, including encryption and metadata storage.
- You can view your uploaded files and download them as needed.

## Technologies Used
- **Backend**: Java, Spring Boot
- **Database**: MySQL
- **Frontend**: HTML, CSS, JavaScript, Thymeleaf
- **Encryption**: AES-256

## Contributing
Contributions are welcome! Please submit a pull request or open an issue for any enhancements or bug fixes.

## License
This project is licensed under the MIT License. See the LICENSE file for details.