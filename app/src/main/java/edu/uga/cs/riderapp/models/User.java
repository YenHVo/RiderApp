package edu.uga.cs.riderapp.models;

import java.util.Date;

public class User {
    private String userId;
    private String email;
    private String name;
    private String role;
    private int points;
    private Date createdAt;

    public User() {
        this.createdAt = new Date();
        this.points = 0;
    }

    public User(String email, String name) {
        this.email = email;
        this.name = name;
        this.points = 0;
        this.role = null;
        this.createdAt = new Date();
    }

    public User(String userId, String email, String name, String role, int points, Date createdAt) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.role = role;
        this.points = points;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

}
