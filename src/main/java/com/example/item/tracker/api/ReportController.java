package com.example.item.tracker.api;

import com.example.item.tracker.component.DatabaseService;
import com.example.item.tracker.component.SendMessages;
import com.example.item.tracker.component.WriteExcel;
import com.example.item.tracker.model.CustomException;
import com.example.item.tracker.model.ErrorCodes;
import com.example.item.tracker.model.ErrorMessage;
import com.example.item.tracker.model.WorkItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.List;

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
    public ResponseEntity<?> sendReport(@RequestParam(required = true) String email) {
        List<WorkItem> list = null;
        try {
            list = dbService.getItemsDataSQLReport("all");
            InputStream is = writeExcel.write(list);
            sm.sendReport(is, email);
        } catch (CustomException e) {
            if (ErrorCodes.TEC001.getCode().equalsIgnoreCase(e.getCode()))
                return ResponseEntity.internalServerError().body(new ErrorMessage(e.getCode(), e.getMessage(), e.getSubCode(), e.getDetails()));
            else
                return ResponseEntity.badRequest().body(new ErrorMessage(e.getCode(), e.getMessage(), e.getSubCode(), e.getDetails()));
        }
        return ResponseEntity.ok("ok");
    }
}