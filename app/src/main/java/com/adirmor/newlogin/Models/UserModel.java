package com.adirmor.newlogin.Models;

import java.util.ArrayList;
import java.util.List;

public class UserModel {

    private String Email;
    private String Password;
    private String Username;
    private List<String> roomIDs = new ArrayList<> ();


    public UserModel() {
    }

    public UserModel(String email, String password, String username) {
        Email = email;
        Password = password;
        Username = username;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public List<String> getRoomIDs() {
        return roomIDs;
    }

    public void setRoomIDs(List<String> roomIDs) {
        this.roomIDs = roomIDs;
    }
}
