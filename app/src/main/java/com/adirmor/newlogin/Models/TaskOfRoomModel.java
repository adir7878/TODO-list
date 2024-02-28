package com.adirmor.newlogin.Models;

import com.google.firebase.Timestamp;

import java.util.UUID;

public class TaskOfRoomModel {
    private String description;
    private boolean isCompleted = false;
    private String id = String.valueOf (UUID.randomUUID ());
    private Timestamp creationTime = Timestamp.now ();

    public TaskOfRoomModel() {
    }

    public TaskOfRoomModel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }
}
