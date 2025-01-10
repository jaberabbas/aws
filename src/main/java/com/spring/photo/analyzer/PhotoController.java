// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.spring.photo.analyzer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Controller
public class PhotoController {

    // Change to your Bucket Name
    private final String bucketName = "myphotobucketjab";

    private final S3Service s3Service;
    private final AnalyzePhotos analyzePhotos;

    private final WriteExcel excel;

    private final SendMessages sendMessage;

    @Autowired
    PhotoController(S3Service s3Service, AnalyzePhotos analyzePhotos, WriteExcel excel, SendMessages sendMessage) {
        this.s3Service = s3Service;
        this.analyzePhotos = analyzePhotos;
        this.excel = excel;
        this.sendMessage = sendMessage;
    }

    @GetMapping("/")
    public String root() {
        return "index";
    }

    @GetMapping("/process")
    public String process() {
        return "process";
    }

    @GetMapping("/photo")
    public String photo() {
        return "upload";
    }

    // Generates a report that analyzes photos in a given bucket.
    @RequestMapping(value = "/report", method = RequestMethod.POST)
    @ResponseBody
    String report(HttpServletRequest request, HttpServletResponse response) {
        // Get a list of key names in the given bucket.
        String email = request.getParameter("email");
        ArrayList<String> myKeys = (ArrayList<String>) s3Service.ListBucketObjects(bucketName);
        ArrayList<List<WorkItem>> myList = new ArrayList<>();
        for (String myKey : myKeys) {
            byte[] keyData = s3Service.getObjectBytes(bucketName, myKey);
            ArrayList<WorkItem> item = analyzePhotos.DetectLabels(keyData, myKey);
            myList.add(item);
        }

        // Now we have a list of WorkItems describing the photos in the S3 bucket.
        InputStream excelData = excel.exportExcel(myList);
        try {
            // Email the report.
            sendMessage.sendReport(excelData, email);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "The photos have been analyzed and the report is sent";
    }

    // Upload an image to an Amazon S3 bucket.
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public ModelAndView singleFileUpload(@RequestParam("file") MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            String name = file.getOriginalFilename();

            // Put the file into the bucket.
            s3Service.putObject(bytes, bucketName, name);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ModelAndView(new RedirectView("photo"));
    }

    @RequestMapping(value = "/getimages", method = RequestMethod.GET)
    @ResponseBody
    String getImages(HttpServletRequest request, HttpServletResponse response) {
        return s3Service.ListAllObjects(bucketName);
    }

    // Downloads the given image from the Amazon S3 bucket.
    @RequestMapping(value = "/downloadphoto", method = RequestMethod.GET)
    void buildDynamicReportDownload(HttpServletRequest request, HttpServletResponse response) {
        try {
            String photoKey = request.getParameter("photoKey");
            byte[] photoBytes = s3Service.getObjectBytes(bucketName, photoKey);
            InputStream is = new ByteArrayInputStream(photoBytes);

            // Define the required information here.
            response.setContentType("image/png");
            response.setHeader("Content-disposition", "attachment; filename=" + photoKey);
            org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
            response.flushBuffer();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
