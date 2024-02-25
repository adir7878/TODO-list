package com.adirmor.newlogin.Models;

import com.google.firebase.Timestamp;

import java.util.Random;
import java.util.UUID;

public class TaskModel {

    private String description;
    private boolean checked;
    private Timestamp selectedTimestamp;
    private final String id = String.valueOf (UUID.randomUUID ());
    private final int requestCode = new Random ().nextInt ();

    public TaskModel(){

    }
    public TaskModel(String text, Timestamp selectedTimestamp) {
        this.description = text;
        this.selectedTimestamp = selectedTimestamp;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    public Timestamp getSelectedTimestamp() {
        return selectedTimestamp;
    }
    public void setSelectedTimestamp(Timestamp selectedTimestamp) {
        this.selectedTimestamp = selectedTimestamp;
    }
    public String getId() {
        return id;
    }
    public int getRequestCode() {
        return requestCode;
    }
}
