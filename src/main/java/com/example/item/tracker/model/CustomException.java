package com.example.item.tracker.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class CustomException extends Throwable {
    private String code;
    private String message;
    private String subCode;
    private String details;
}
