package com.example.item.tracker.model;

import lombok.Data;

@Data
public class WorkItem {

    private String id;
    private String name;
    private String guide;
    private String date;
    private String description;
    private String status;
    private boolean archived;

}