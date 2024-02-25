package com.adirmor.newlogin.Models;

public class UserModel {

    private String Email;
    private String Password;
    private String Username;


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
}
