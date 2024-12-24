package com.example.item.tracker.api;

import com.example.item.tracker.component.DatabaseService;
import com.example.item.tracker.model.CustomException;
import com.example.item.tracker.model.ErrorCodes;
import com.example.item.tracker.model.ErrorMessage;
import com.example.item.tracker.model.WorkItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.stream.StreamSupport;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("api/items")
public class MainController {
    private static final Logger log = LoggerFactory.getLogger(MainController.class);
    private final DatabaseService dbService;

    @Autowired
    MainController(DatabaseService dbService) {
        this.dbService = dbService;
    }

    @GetMapping("")
    public ResponseEntity<?> getItems(@RequestParam(required = false) String archived) {
        Iterable<WorkItem> result;
        try {
            result = dbService.getItemsDataSQLReport(archived);
        } catch (CustomException e) {
            log.error("CustomException: " + e.getCode() + " " + e.getMessage()+ " " + e.getSubCode() + " " + e.getDetails() );
            return getErrorMessageResponseEntity(e);
        }
        return ResponseEntity.ok().body(StreamSupport.stream(result.spliterator(), false)
                .toList());
    }

    @PutMapping("{id}:archive")
    public ResponseEntity<?> modifyUser(@PathVariable int id) {
        WorkItem workItem;
        try {
            dbService.flipItemArchive(id);
            workItem = dbService.getItemDataSQLReport(id);
        } catch (CustomException e) {
            return getErrorMessageResponseEntity(e);
        }
        return ResponseEntity.ok(workItem);
    }

    @PostMapping("")
    public ResponseEntity<?> addItem(@RequestBody Map<String, String> payload) {
        WorkItem item = new WorkItem();
        String workId = UUID.randomUUID().toString();
        String date = LocalDateTime.now().toString();
        item.setId(workId);
        item.setGuide(payload.get("guide"));
        item.setDescription(payload.get("description"));
        item.setName(payload.get("name"));
        item.setDate(date);
        item.setStatus(payload.get("status"));
        WorkItem workItem = null;
        try {
            workItem = dbService.injectNewSubmission(item);
        } catch (CustomException e) {
            return getErrorMessageResponseEntity(e);
        }
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(workItem.getId()).toUri();

        return ResponseEntity.created(location).build();
    }

    private ResponseEntity<ErrorMessage> getErrorMessageResponseEntity(CustomException e) {
        log.error("Inside getErrorMessageResponseEntity: " + e.getCode() + " " + e.getMessage()+ " " + e.getSubCode() + " " + e.getDetails() );
        if (ErrorCodes.TEC001.getCode().equalsIgnoreCase(e.getCode()))
            return ResponseEntity.internalServerError().body(new ErrorMessage(e.getCode(), e.getMessage(), e.getSubCode(), e.getDetails()));
        else
            return ResponseEntity.badRequest().body(new ErrorMessage(e.getCode(), e.getMessage(), e.getSubCode(), e.getDetails()));
    }
}