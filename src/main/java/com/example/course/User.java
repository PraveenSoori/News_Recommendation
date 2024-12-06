package com.example.course;

import java.util.List;

public class User extends Member {
    private final List<String> interests;

    public User(String username, String password, String email, String fullName, int age, List<String> interests) {
        super(username, password, email, fullName, age);
        this.interests = interests;
    }

    @Override
    public boolean authenticate(String password) {
        return this.getPassword().equals(password);
    }

    public List<String> getInterests() {
        return interests;
    }
}

