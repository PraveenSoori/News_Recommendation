package com.example.course;

import java.util.List;

public abstract class Member {
    private String username;
    private String password;
    private String email;
    private String fullName; // Updated to match User
    private int age;

    public Member(String username, String password, String email, String fullName, int age) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName; // Updated
        this.age = age;
    }

    public String getFullName() { // Updated getter name
        return fullName;
    }

    public void setFullName(String fullName) { // Updated setter name
        this.fullName = fullName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public abstract boolean authenticate(String password);
}
