package com.example.course;

public class User {
    private String username;
    private String fullName;
    private String email;
    private int age;
    private String interests;

    public User(String username, String fullName, String email, int age, String interests) {
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.age = age;
        this.interests = interests;
    }

    // Getters and setters
    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public int getAge() {
        return age;
    }

    public String getInterests() {
        return interests;
    }
}

