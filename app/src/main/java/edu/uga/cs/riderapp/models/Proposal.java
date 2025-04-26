package edu.uga.cs.riderapp.models;

import java.util.Date;

public class Proposal {
    private String proposalId;
    private String type;
    private String startLocation;
    private String endLocation;
    private String driverId;
    private String riderId;
    private String car;
    private String status;
    private Date createdAt;
    private int availableSeats;
    private boolean confirmedByDriver;
    private boolean confirmedByRider;
    private String driverName;
    private String riderName;

    public Proposal() {
        this.createdAt = new Date();
        this.status = "pending";
        this.confirmedByDriver = false;
        this.confirmedByRider = false;
    }

    // For Drivers
    public Proposal(String type, String startLocation, String endLocation,
                    String driverId, String car, int availableSeats) {
        this.type = type;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.driverId = driverId;
        this.car = car;
        this.availableSeats = availableSeats;
        this.status = "pending";
        this.createdAt = new Date();
        this.confirmedByDriver = false;
        this.confirmedByRider = false;
    }

    // For Riders
    public Proposal(String type, String startLocation, String endLocation,
                    String riderId) {
        this.type = type;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.riderId = riderId;
        this.car = null;
        this.status = "pending";
        this.createdAt = new Date();
        this.confirmedByDriver = false;
        this.confirmedByRider = false;
    }


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

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getRiderId() {
        return riderId;
    }

    public void setRiderId(String riderId) {
        this.riderId = riderId;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getRiderName() {
        return riderName;
    }

    public void setRiderName(String riderName) {
        this.riderName = riderName;
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

    public boolean isConfirmedByDriver() {
        return confirmedByDriver;
    }

    public void setConfirmedByDriver(boolean confirmedByDriver) {
        this.confirmedByDriver = confirmedByDriver;
    }

    public boolean isConfirmedByRider() {
        return confirmedByRider;
    }

    public void setConfirmedByRider(boolean confirmedByRider) {
        this.confirmedByRider = confirmedByRider;
    }
}
