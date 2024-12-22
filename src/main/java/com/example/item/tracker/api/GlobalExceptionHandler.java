package com.example.item.tracker.api;

import com.example.item.tracker.model.CustomException;
import com.example.item.tracker.model.ErrorCodes;
import com.example.item.tracker.model.ErrorMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

@org.springframework.web.bind.annotation.ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessage> handleValidationExceptions(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        StringBuilder sbKeyValueErrors = new StringBuilder();
        errors.forEach((fieldName, errorMessage) -> sbKeyValueErrors.append(fieldName).append(" : ").append(errorMessage));
        ErrorMessage errorMessage = new ErrorMessage(ErrorCodes.FUNC001.getCode(), ErrorCodes.FUNC001.getDesc(), "", sbKeyValueErrors.toString());
        return ResponseEntity.badRequest().body(errorMessage);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorMessage> handleSQLIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException e) {
        ErrorMessage errorMessage = new ErrorMessage(ErrorCodes.FUNC001.getCode(), ErrorCodes.FUNC001.getDesc(), String.valueOf(e.getErrorCode()), e.getMessage());
        return ResponseEntity.badRequest().body(errorMessage);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorMessage> handleCustomException(CustomException e) {
        ErrorMessage errorMessage = new ErrorMessage(e.getCode(), e.getMessage(), e.getSubCode(), e.getDetails());
        return ResponseEntity.badRequest().body(errorMessage);
    }


}

