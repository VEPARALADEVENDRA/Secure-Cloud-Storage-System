# How to Run the Secure Cloud Storage Application

This guide provides step-by-step instructions to set up and run the Secure Cloud Storage application.

**Current Date:** May 30, 2025

## Phase 1: Prerequisites & Setup

1.  **Install Java Development Kit (JDK)**:
    *   Ensure you have JDK version 11 or later installed.
    *   Download from [Oracle JDK](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html) or [Eclipse Temurin (AdoptOpenJDK)](https://adoptium.net/).
    *   Verify installation by opening PowerShell and typing:
        ```powershell
        java -version
        ```

2.  **Install Apache Maven**:
    *   Download Maven from the [Apache Maven website](https://maven.apache.org/download.cgi).
    *   Follow their installation instructions to add Maven's `bin` directory to your system's PATH environment variable.
    *   Verify installation by opening PowerShell and typing:
        ```powershell
        mvn -version
        ```

3.  **Set up MySQL Server**:
    *   Install [MySQL Community Server](https://dev.mysql.com/downloads/mysql/) if you haven't already.
    *   Start the MySQL server.
    *   Connect to your MySQL server (e.g., using MySQL Workbench or the `mysql` command-line client).
    *   Create the database:
        ```sql
        CREATE DATABASE IF NOT EXISTS cloud_db;
        ```
    *   Ensure the user `root` (or a dedicated user you configure) has the necessary permissions on `cloud_db`.

4.  **Configure Application Properties**:
    *   Navigate to your project directory in PowerShell:
        ```powershell
        cd C:\Users\g.siva.kumar\Desktop\Cloud\secure-cloud-storage-system
        ```
    *   Open the file `src\main\resources\application.properties` in a text editor.
    *   Modify the following line to match your MySQL root password (or the password of the user you configured for `cloud_db`):
        ```properties
        spring.datasource.password=your_actual_mysql_password
        ```
    *   The other properties like `spring.datasource.url`, `spring.datasource.username`, and `spring.jpa.hibernate.ddl-auto=update` should be suitable for initial setup. The `ddl-auto=update` will instruct Hibernate to create/update the necessary tables (`user`, `file_metadata`, `cloud_provider`) in the `cloud_db` database when the application starts.

5.  **Verify/Create Required Directories**:
    *   The application expects the following directories. If they don't exist, create them using PowerShell or File Explorer:
        ```powershell
        mkdir C:\opt\uploads -ErrorAction SilentlyContinue
        mkdir C:\cloud -ErrorAction SilentlyContinue
        mkdir C:\cloud\aws -ErrorAction SilentlyContinue
        mkdir C:\cloud\azure -ErrorAction SilentlyContinue
        mkdir C:\cloud\gcp -ErrorAction SilentlyContinue
        mkdir C:\cloud\ibm -ErrorAction SilentlyContinue
        ```
    *   Ensure the user running the Java application has write permissions to these directories.

## Phase 2: Build and Run the Application

1.  **Open PowerShell**.
2.  **Navigate to the Project's Root Directory**:
    ```powershell
    cd C:\Users\g.siva.kumar\Desktop\Cloud\secure-cloud-storage-system
    ```
3.  **Clean and Build the Project using Maven** (Optional but recommended for a clean build):
    ```powershell
    mvn clean install
    ```
    *   This command compiles your code, runs any tests (if present), and packages the application.

4.  **Run the Spring Boot Application**:
    ```powershell
    mvn spring-boot:run
    ```
    *   Maven will download dependencies (if it's the first time or they've changed) and then start the embedded web server (Tomcat by default).
    *   Look for output in the console indicating that the application has started, typically ending with lines like `Tomcat started on port(s): 8080 (http)` and `Started SecureCloudStorageApplication in ... seconds`.

## Phase 3: Access and Test the Application

1.  **Access the Upload Page**:
    *   Open your web browser (Chrome, Firefox, Edge, etc.).
    *   Navigate to: `http://localhost:8080/upload` (or `http://localhost:8080/` as per `PageController.java`)

2.  **Test Functionality**:
    *   **Frontend Validations**: Try submitting the form with invalid data (e.g., bad email format, short phone number, no file selected, no cloud providers checked).
    *   **File Upload**:
        *   Fill in all fields correctly.
        *   Select a small file.
        *   Choose one or more cloud providers.
        *   Click "Upload File".
        *   Check for a success message.
        *   Verify that encrypted file chunks appear in the respective `C:\cloud\[provider_name]` directories.
        *   Check your `cloud_db` database:
            *   A new record in the `user` table (if the email was new).
            *   A new record in the `file_metadata` table.
            *   The `cloud_provider` table should have been populated on first startup by `FileService` (AWS, Azure, GCP, IBM Cloud).
    *   **Large File Upload**: Test with a file larger than 10MB to ensure chunking works.
    *   **Conflict Management**: Try uploading the same file simultaneously (you might need to be quick or simulate it by adding a breakpoint during debugging) to see if the lock mechanism prevents it.

3.  **Access API Documentation (Swagger UI)**:
    *   In your browser, navigate to: `http://localhost:8080/swagger-ui/index.html`
    *   Here you can explore and test the backend API endpoints directly:
        *   `POST /api/files/upload`
        *   `GET /api/files/user/{userId}`
        *   `GET /api/files/download/{fileId}`

4.  **Test File Download**:
    *   First, use Swagger UI or a tool like Postman to call `GET /api/files/user/{userId}` (find a `userId` from your database after an upload).
    *   From the response, get a `fileId`.
    *   Then call `GET /api/files/download/{fileId}?userId={requestingUserId}` (use the same `userId` for both for now, as current access control is owner-based).
    *   A zip file containing the decrypted original file should be downloaded. Verify its contents.

## Stopping the Application

*   In the PowerShell window where the application is running (where you executed `mvn spring-boot:run`), press `Ctrl + C`.
*   Confirm if prompted to terminate the batch job.

---
This guide should help you get the application up and running. If you encounter any errors, check the PowerShell console output for detailed messages from Spring Boot and Maven.
