package com.example.item.tracker.model;


import lombok.Getter;

@Getter
public enum ErrorCodes {
    FUNC001("FUNC001", "functional error in entity api"),
    TEC001("TEC001", "technical error in entity api");
    private final String code;
    private final String desc;
    ErrorCodes(String code, String desc){
        this.code = code;
        this.desc = desc;
    }

}
