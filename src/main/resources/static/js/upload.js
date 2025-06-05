document.addEventListener("DOMContentLoaded", function() {
    const form = document.getElementById("uploadForm");
    const fileInput = document.getElementById("fileInput");
    const fileNameDisplay = document.getElementById("fileNameDisplay");
    const fileSizeDisplay = document.getElementById("fileSizeDisplay");
    const emailInput = document.getElementById("email");
    const phoneInput = document.getElementById("phone");
    const submitButton = document.getElementById("submitButton");
    const messageDiv = document.createElement("div"); // Create a div for messages
    form.parentNode.insertBefore(messageDiv, form); // Insert message div before the form

    // New elements for file listing and download
    const userEmailForFilesInput = document.getElementById("userEmailForFiles");
    const fetchFilesButton = document.getElementById("fetchFilesButton");
    const fileListContainer = document.getElementById("fileListContainer");
    const fileListMessage = document.getElementById("fileListMessage");

    fileInput.addEventListener("change", function() {
        const file = fileInput.files[0];
        if (file) {
            fileNameDisplay.textContent = `Selected File: ${file.name}`;
            const fileSizeMB = (file.size / (1024 * 1024)).toFixed(2);
            fileSizeDisplay.textContent = `Size: ${fileSizeMB} MB`;

            if (file.size > 500 * 1024 * 1024) {
                displayMessage(`Error: File size (${fileSizeMB} MB) exceeds the 500MB limit.`, "error");
                submitButton.disabled = true;
            } else {
                clearMessage();
                submitButton.disabled = false;
            }
        } else {
            fileNameDisplay.textContent = "";
            fileSizeDisplay.textContent = "";
            clearMessage();
        }
    });

    form.addEventListener("submit", async function(event) {
        event.preventDefault(); // Prevent default form submission
        messageDiv.innerHTML = ''; // Clear previous messages

        // Basic Validations (though some are handled by HTML5 `required` and `pattern`)
        const emailPattern = /^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$/;
        if (!emailPattern.test(emailInput.value)) {
            displayMessage("Please enter a valid email address.", "error");
            return;
        }

        if (!phoneInput.value.match(/^\d{10}$/)) {
            displayMessage("Phone number must be 10 digits.", "error");
            return;
        }

        if (fileInput.files.length === 0) {
            displayMessage("Please select a file to upload.", "error");
            return;
        }

        const file = fileInput.files[0];
        if (file.size > 500 * 1024 * 1024) {
            displayMessage(`File size (${(file.size / (1024 * 1024)).toFixed(2)} MB) exceeds the 500MB limit.`, "error");
            return;
        }

        const selectedProviders = form.querySelectorAll('input[name="cloudProviders"]:checked');
        if (selectedProviders.length === 0) {
            displayMessage("Please select at least one cloud provider.", "error");
            return;
        }

        submitButton.disabled = true;
        submitButton.textContent = "Uploading...";
        displayMessage("Starting upload...", "info");

        const formData = new FormData(form);

        try {
            const response = await fetch(form.action, {
                method: "POST",
                body: formData,
            });

            const resultText = await response.text();

            if (response.ok) {
                displayMessage(resultText, "success"); // Display success message from server
                form.reset(); // Reset form fields
                fileNameDisplay.textContent = "";
                fileSizeDisplay.textContent = "";
            } else {
                displayMessage(`Upload failed: ${resultText}`, "error");
            }
        } catch (error) {
            console.error("Upload error:", error);
            displayMessage(`An error occurred during upload: ${error.message}`, "error");
        }

        submitButton.disabled = false;
        submitButton.textContent = "Upload File";
    });

    // --- File Listing and Download Logic ---
    fetchFilesButton.addEventListener("click", async function() {
        const userEmail = userEmailForFilesInput.value.trim();
        if (!userEmail) {
            displayFileListMessage("Please enter your email address.", "error");
            return;
        }

        displayFileListMessage("Fetching files...", "info");
        fileListContainer.innerHTML = ""; // Clear previous list

        try {
            // 1. Get User ID by email
            const userResponse = await fetch(`/api/users/email/${encodeURIComponent(userEmail)}`);
            if (!userResponse.ok) {
                if (userResponse.status === 404) {
                    displayFileListMessage("No user found with this email.", "error");
                } else {
                    displayFileListMessage(`Error fetching user: ${userResponse.statusText}`, "error");
                }
                return;
            }
            const userData = await userResponse.json();
            const userId = userData.id;

            if (!userId) {
                displayFileListMessage("Could not retrieve user ID.", "error");
                return;
            }

            // 2. Get files for that User ID
            const filesResponse = await fetch(`/api/files/user/${userId}`);
            if (!filesResponse.ok) {
                displayFileListMessage(`Error fetching files: ${filesResponse.statusText}`, "error");
                return;
            }

            const files = await filesResponse.json();

            if (files.length === 0) {
                displayFileListMessage("No files found for this user.", "info");
            } else {
                renderFileList(files, userId); // Pass userId for download links
                clearFileListMessage();
            }

        } catch (error) {
            console.error("Error fetching files:", error);
            displayFileListMessage(`An error occurred: ${error.message}`, "error");
        }
    });

    function renderFileList(files, userId) {
        const ul = document.createElement("ul");
        ul.className = "file-list";
        files.forEach(file => {
            const li = document.createElement("li");
            li.innerHTML = `
                <span>${file.originalFilename} (Size: ${(file.fileSize / (1024*1024)).toFixed(2)} MB, Uploaded: ${new Date(file.timestamp).toLocaleDateString()})</span>
                <button class="download-button" data-fileid="${file.id}" data-userid="${userId}" data-filename="${file.originalFilename}">Download</button>
            `;
            ul.appendChild(li);
        });
        fileListContainer.appendChild(ul);

        // Add event listeners to new download buttons
        document.querySelectorAll(".download-button").forEach(button => {
            button.addEventListener("click", handleDownload);
        });
    }

    async function handleDownload(event) {
        const button = event.target;
        const fileId = button.dataset.fileid;
        const userId = button.dataset.userid; // User ID of the file owner (for the download request)
        const originalFilename = button.dataset.filename;

        button.disabled = true;
        button.textContent = "Downloading...";
        displayFileListMessage(`Preparing download for ${originalFilename}...`, "info");

        try {
            // We need the user ID of the person *requesting* the download.
            // For simplicity, we are using the file owner's ID which was fetched earlier.
            // In a real app with authentication, this would be the logged-in user's ID.
            const requestingUserId = userId; // Or get current logged-in user's ID

            const response = await fetch(`/api/files/download/${fileId}?userId=${requestingUserId}`);

            if (response.ok) {
                const blob = await response.blob();
                const downloadUrl = window.URL.createObjectURL(blob);
                const a = document.createElement("a");
                a.href = downloadUrl;
                // The backend should set Content-Disposition, but we can use the stored filename as a fallback
                a.download = originalFilename + ".zip"; // Server sends it as a zip
                document.body.appendChild(a);
                a.click();
                a.remove();
                window.URL.revokeObjectURL(downloadUrl);
                displayFileListMessage(`Successfully downloaded ${originalFilename}.`, "success");
            } else {
                const errorText = await response.text();
                displayFileListMessage(`Download failed for ${originalFilename}: ${response.status} ${errorText || response.statusText}`, "error");
            }
        } catch (error) {
            console.error("Download error:", error);
            displayFileListMessage(`Error downloading ${originalFilename}: ${error.message}`, "error");
        }
        button.disabled = false;
        button.textContent = "Download";
    }

    function displayFileListMessage(message, type) {
        fileListMessage.textContent = message;
        fileListMessage.className = `message ${type}`;
        fileListMessage.style.display = "block";
    }

    function clearFileListMessage() {
        fileListMessage.textContent = "";
        fileListMessage.style.display = "none";
    }

    function displayMessage(message, type) {
        messageDiv.innerHTML = `<div class="message ${type}">${message}</div>`;
    }

    function clearMessage() {
        messageDiv.innerHTML = "";
    }
});