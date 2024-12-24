package com.example.item.tracker.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class CustomException extends Exception{
    private String code;
    private String message;
    private String subCode;
    private String details;
    public CustomException(){
        super();
    }
    public CustomException(String message){
        super(message);
    }
    public CustomException(String message, Throwable throwable){
        super(message, throwable);
        this.message = message;
    }
    public CustomException(String code, String message, String subCode, String details, Throwable throwable){
        super(message, throwable);
        this.code = code;
        this.message = message;
        this.subCode = subCode;
        this.details = details;
    }
}
