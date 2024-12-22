package com.example.item.tracker.api;

import com.example.item.tracker.component.DatabaseService;
import com.example.item.tracker.model.CustomException;
import com.example.item.tracker.model.ErrorMessage;
import com.example.item.tracker.model.WorkItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.StreamSupport;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("api/items")
public class MainController {
    private final DatabaseService dbService;

    @Autowired
    MainController(DatabaseService dbService) {
        this.dbService = dbService;
    }

    @GetMapping("")
    public ResponseEntity<?> getItems(@RequestParam(required = false) String archived) {
        Iterable<WorkItem> result;
        try {
            if (archived != null && archived.trim().equalsIgnoreCase("false")) {
                result = dbService.getItemsDataSQLReport(0);
            } else if (archived != null && archived.trim().equalsIgnoreCase("true")) {
                result = dbService.getItemsDataSQLReport(1);
            } else {
                result = dbService.getItemsDataSQLReport(-1);
            }
        } catch (CustomException e) {
            return ResponseEntity.badRequest().body(new ErrorMessage(e.getCode(), e.getMessage(), e.getSubCode(), e.getDetails()));
        }
        return ResponseEntity.ok().body(StreamSupport.stream(result.spliterator(), false)
                .toList());
    }

    // Notice the : character which is used for custom methods. More information can
    // be found here:
    // https://cloud.google.com/apis/design/custom_methods
    @PutMapping("{id}:archive")
    public ResponseEntity<?> modUser(@PathVariable String id) {
        try {
            dbService.flipItemArchive(id);
        } catch (CustomException e) {
            return ResponseEntity.badRequest().body(new ErrorMessage(e.getCode(), e.getMessage(), e.getSubCode(), e.getDetails()));
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("")
    public ResponseEntity<?> addItem(@RequestBody Map<String, String> payload) {
        String name = payload.get("name");
        String guide = payload.get("guide");
        String description = payload.get("description");
        String status = payload.get("status");

        WorkItem item = new WorkItem();
        String workId = UUID.randomUUID().toString();
        String date = LocalDateTime.now().toString();
        item.setId(workId);
        item.setGuide(guide);
        item.setDescription(description);
        item.setName(name);
        item.setDate(date);
        item.setStatus(status);
        Iterable<WorkItem> result;
        try {
            dbService.injectNewSubmission(item);
            result = dbService.getItemsDataSQLReport(0);
        } catch (CustomException e) {
            return ResponseEntity.badRequest().body(new ErrorMessage(e.getCode(), e.getMessage(), e.getSubCode(), e.getDetails()));
        }
        return ResponseEntity.ok(StreamSupport.stream(result.spliterator(), false)
                .toList());
    }
}