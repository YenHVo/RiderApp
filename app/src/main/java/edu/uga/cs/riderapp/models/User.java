package edu.uga.cs.riderapp.models;


import java.util.Date;

public class User {
    private String userId;
    private String email;
    private String name;
    //private boolean isRider;
    //private boolean isDriver;
    private int points;
    private Date createdAt;

    public User() {
        //this.isRider = true;
        //this.isDriver = true;
        this.points = 0;
        this.createdAt = new Date();
    }

    public User(String email, String name) {
        this.email = email;
        this.name = name;
        //this.isRider = true;
        //this.isDriver = true;
        this.points = 0;
        this.createdAt = new Date();
    }

    public User(String userId, String email, String name, int points, Date createdAt) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        //this.isRider = isRider;
        //this.isDriver = isDriver;
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

    /*
    public boolean isRider() {
        return isRider;
    }

    public void setRider(boolean rider) {
        isRider = rider;
    }

    public boolean isDriver() {
        return isDriver;
    }

    public void setDriver(boolean driver) {
        isDriver = driver;
    }*/

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
