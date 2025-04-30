package edu.uga.cs.riderapp.models;


import java.util.Date;

public class User {
    private String userId;
    private String email;
    private String name;

    private long points;
    private Date createdAt;

    public User() {

        this.points = 0;
        this.createdAt = new Date();
    }

    public User(String email, String name) {
        this.email = email;
        this.name = name;

        this.points = 0;
        this.createdAt = new Date();
    }

    public User(String userId, String email, String name, long points, Date createdAt) {
        this.userId = userId;
        this.email = email;
        this.name = name;

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

    public long getPoints() {
        return points;
    }

    public void setPoints(long points) {
        this.points = points;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
