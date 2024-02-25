package com.adirmor.newlogin.Models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TaskOfListModel {
    private String description;
    private boolean isCompleted = false;
    private String id = String.valueOf (UUID.randomUUID ());

    public TaskOfListModel() {
    }

    public TaskOfListModel(String description) {
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

}
