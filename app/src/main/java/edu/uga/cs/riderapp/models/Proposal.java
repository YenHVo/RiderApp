package edu.uga.cs.riderapp.models;

import java.util.Date;

public class Proposal {
    private String proposalId;
    private String type;
    private String startLocation;
    private String endLocation;
    private User driver;
    private User rider;
    private String car;
    private String status;
    private Date createdAt;
    private int availableSeats;

    public Proposal() {
        this.createdAt = new Date();
        this.status = "pending";
    }

    // For Drivers
    public Proposal(String type, String startLocation, String endLocation,
                    User user, String car, int availableSeats) {
        this.type = type;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.car = car;
        this.status = "pending";
        this.createdAt = new Date();
        this.availableSeats = availableSeats;
        this.driver = user;
        this.rider = null;
    }

    // For Riders
    public Proposal(String type, String startLocation, String endLocation,
                    User user) {
        this.type = type;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.rider = user;
        this.driver = null;
        this.car = null;
        this.status = "pending";
        this.createdAt = new Date();
    }

    // Getters and Setters
    public String getProposalId() {
        return proposalId;
    }

    public void setProposalId(String proposalId) {
        this.proposalId = proposalId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        if ("offer".equals(type) || "request".equals(type)) {
            this.type = type;
        } else {
            throw new IllegalArgumentException("Proposal type must be either 'offer' or 'request'");
        }
    }

    public String getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(String startLocation) {
        this.startLocation = startLocation;
    }

    public String getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(String endLocation) {
        this.endLocation = endLocation;
    }

    public User getDriver() {
        return driver;
    }

    public void setDriver(User driver) {
        this.driver = driver;
    }

    public User getRider() {
        return rider;
    }

    public void setRider(User rider) {
        this.rider = rider;
    }

    public String getCar() {
        return car;
    }

    public void setCar(String car) {
        this.car = car;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

}
