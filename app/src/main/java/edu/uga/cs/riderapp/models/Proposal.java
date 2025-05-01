package edu.uga.cs.riderapp.models;

import java.util.Date;

/**
 * Represents a ride proposal in the application, which can be either an offer from a driver
 * or a request from a rider. Contains details like locations, time, participants, vehicle, and statuses.
 */
public class Proposal {
    private String proposalId;
    private String type;
    private String startLocation;
    private String endLocation;
    private String driverId;
    private String riderId;
    private String car;
    private long dateTime;
    private Date createdAt;
    private int availableSeats;
    private boolean confirmedByDriver;
    private boolean confirmedByRider;
    private String driverName;
    private String riderName;
    private String riderStatus;
    private String driverStatus;

    /**
     * Default constructor initializing defaults like creation time and status flags.
     */
    public Proposal() {
        this.createdAt = new Date();
        this.riderStatus = "pending";
        this.driverStatus = "pending";
        this.confirmedByDriver = false;
        this.confirmedByRider = false;
    }

    /**
     * Constructor for offers and requests.
     *
     * @param type           Type of the proposal ("offer" or "request").
     * @param startLocation  Starting location of the ride.
     * @param endLocation    Destination location of the ride.
     * @param driverId       Firebase UID of the driver.
     * @param riderId        Firebase UID of the rider.
     * @param car            Car model used by the driver (if applicable).
     * @param availableSeats Number of available seats in the car.
     * @param dateTime       Scheduled time of the ride (in millis).
     */
    public Proposal(String type, String startLocation, String endLocation,
                    String driverId, String riderId,String car, int availableSeats, long dateTime) {
        this.type = type;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.driverId = driverId;
        this.riderId = riderId;
        this.car = car;
        this.availableSeats = availableSeats;
        this.createdAt = new Date();
        this.dateTime = dateTime;
        this.confirmedByDriver = false;
        this.confirmedByRider = false;
        this.riderStatus = "pending";
        this.driverStatus = "pending";
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

    public String getRiderStatus() {
        return riderStatus;
    }

    public String getDriverStatus() {
        return driverStatus;
    }

    public void setRiderStatus(String riderStatus) {
        this.riderStatus = riderStatus;
    }

    public void setDriverStatus(String driverStatus) {
        this.driverStatus = driverStatus;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public Boolean getConfirmedByDriver() {
        return confirmedByDriver;
    }

    public void setConfirmedByDriver(Boolean confirmedByDriver) {
        this.confirmedByDriver = confirmedByDriver;
    }

    public Boolean getConfirmedByRider() {
        return confirmedByRider;
    }

    public void setConfirmedByRider(Boolean confirmedByRider) {
        this.confirmedByRider = confirmedByRider;
    }
}
