package com.adirmor.newlogin.Models;

import com.adirmor.newlogin.Utils.FirebaseUtils;

import java.util.ArrayList;
import java.util.List;

public class UserModel {

    private String Email;
    private String Password;
    private String Username;
    private String id = FirebaseUtils.getCurrentUserId ();
    private boolean isBlockedFromRooms = false;


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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isBlockedFromRooms() {
        return isBlockedFromRooms;
    }

    public void setBlockedFromRooms(boolean blockedFromRooms) {
        isBlockedFromRooms = blockedFromRooms;
    }
}
