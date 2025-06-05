package com.cloudstorage.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/upload") // Serves upload.html from templates at /upload URL
    public String uploadPage() {
        return "upload"; // This refers to upload.html in src/main/resources/templates
    }

    // Optional: If you want the upload page to be the root page as well
    @GetMapping("/")
    public String homePage() {
        return "upload";
    }
}
