package com.example.course;

public class Admin extends Member {
    public Admin(String username, String password, String email, String fullname, int age) {
        super(username, password, email, fullname, age);
    }

    @Override
    public boolean authenticate(String password) {
        return this.getPassword().equals(password);
    }
}

