// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.spring.photo.analyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PhotoApplication {
    public static void main(String[] args) {
        SpringApplication.run(PhotoApplication.class, args);
        System.out.println("Hello Amazon S3 and Amazon Rekognition");
    }
}
