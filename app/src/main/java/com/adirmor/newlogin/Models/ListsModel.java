package com.adirmor.newlogin.Models;

import com.google.firebase.Timestamp;
import com.google.firebase.ktx.Firebase;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ListsModel {

    private String id = String.valueOf (UUID.randomUUID ());
    private String name;
    private Timestamp time;

    private List<TaskOfListModel> list = new ArrayList<> ();

    public ListsModel() {
    }

    public ListsModel(String name,Timestamp time) {
        this.name = name;
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public List<TaskOfListModel> getList() {
        return list;
    }

    public void setList(List<TaskOfListModel> list) {
        this.list = list;
    }
}
