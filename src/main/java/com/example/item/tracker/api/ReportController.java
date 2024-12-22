// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.example.item.tracker.api;

import com.example.item.tracker.component.DatabaseService;
import com.example.item.tracker.component.SendMessages;
import com.example.item.tracker.component.WriteExcel;
import com.example.item.tracker.model.CustomException;
import com.example.item.tracker.model.ErrorMessage;
import com.example.item.tracker.model.WorkItem;
import com.google.gson.Gson;
import jxl.write.WriteException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Slf4j
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("api/items:report")
public class ReportController {

    private final DatabaseService dbService;
    private final WriteExcel writeExcel;
    private final SendMessages sm;

    @Autowired()
    ReportController(DatabaseService dbService, WriteExcel writeExcel, SendMessages sm) {
        this.dbService = dbService;
        this.writeExcel = writeExcel;
        this.sm = sm;
    }

    @PostMapping("")
    public ResponseEntity<?> sendReport(@RequestBody Map<String, String> body) {
        List<WorkItem> list = null;
        try {
            list = dbService.getItemsDataSQLReport(-1);
            InputStream is = writeExcel.write(list);
            sm.sendReport(is, body.get("email"));
        } catch (CustomException e) {
            return ResponseEntity.badRequest().body(new ErrorMessage(e.getCode(), e.getMessage(), e.getSubCode(), e.getDetails()));
        }
        Gson gson = new Gson();
        return ResponseEntity.ok("ok");

        //return gson.toJson("error");
    }
}