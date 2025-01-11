// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.spring.photo.analyzer;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionAsyncClient;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsResponse;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.Label;
import software.amazon.awssdk.services.rekognition.model.RekognitionException;

@Component
public class AnalyzePhotos {
    public ArrayList<WorkItem> DetectLabels(byte[] bytes, String key) {
        RekognitionAsyncClient rekAsyncClient = RekognitionAsyncClient.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .region(Region.US_EAST_2)
                .build();
        try {
            final AtomicReference<ArrayList<WorkItem>> reference = new AtomicReference<>();
            SdkBytes sourceBytes = SdkBytes.fromByteArray(bytes);

            // Create an Image object for the source image.
            Image souImage = Image.builder()
                    .bytes(sourceBytes)
                    .build();

            DetectLabelsRequest detectLabelsRequest = DetectLabelsRequest.builder()
                    .image(souImage)
                    .maxLabels(10)
                    .build();

            System.out.println("detecting labels async ...");
            CompletableFuture<DetectLabelsResponse> futureGet = rekAsyncClient.detectLabels(detectLabelsRequest);
            futureGet.whenComplete((resp, err) -> {
                try {
                    if (resp != null) {
                        List<Label> labels = resp.labels();
                        System.out.println("Detected labels for the given photo");
                        ArrayList<WorkItem> list = new ArrayList<>();
                        WorkItem item;
                        for (Label label : labels) {
                            item = new WorkItem();
                            item.setKey(key); // identifies the photo.
                            item.setConfidence(label.confidence().toString());
                            item.setName(label.name());
                            list.add(item);
                        }
                        reference.set(list);

                    } else {
                        err.printStackTrace();
                    }

                } finally {
                    // Only close the client when you are completely done with it.
                    rekAsyncClient.close();
                }
            });
            futureGet.join();

            // Use the AtomicReference object to return the ArrayList<WorkItem> collection.
            return reference.get();

        } catch (RekognitionException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
        return null;
    }
}
