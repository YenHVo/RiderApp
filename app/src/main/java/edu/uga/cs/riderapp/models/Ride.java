package edu.uga.cs.riderapp.models;

import com.google.firebase.database.DataSnapshot;

/**
 * Represents a ride model in the application, shared between a driver and rider.
 * Contains details such as users involved, route, timing, and points.
 */
public class Ride {
    private String driverId;
    private String riderId;
    private String startLocation;
    private String endLocation;
    private Long dateTime;
    private Long points;
    private String status;
    private String proposalId;
    private String driverEmail;
    private String riderEmail;

    /**
     * Default constructor.
     */
    public Ride() {
    }

    /**
     * Constructs a Ride object with all fields.
     *
     * @param driverId      Firebase UID of the driver
     * @param riderId       Firebase UID of the rider
     * @param startLocation Ride start location
     * @param endLocation   Ride end location
     * @param dateTime      Scheduled ride time in epoch milliseconds
     * @param points        Points awarded (positive for driver, negative for rider)
     * @param status        Optional status (e.g., "completed", "active")
     * @param proposalId    Associated proposal ID
     * @param driverEmail   Driver's email (for history/logging)
     * @param riderEmail    Rider's email (for history/logging)
     */
    public Ride(String driverId, String riderId, String startLocation, String endLocation, Long dateTime, Long points, String status, String proposalId, String driverEmail, String riderEmail) {
        this.driverId = driverId;
        this.riderId = riderId;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.dateTime = dateTime;
        this.points = points;
        this.status = status;
        this.proposalId = proposalId;
        this.driverEmail = driverEmail;
        this.riderEmail = riderEmail;
    }

    /**
     * Copy constructor for creating a duplicate Ride object.
     *
     * @param other The Ride object to copy from.
     */
    public Ride(Ride other) {
        if (other == null) return;

        this.driverId = other.driverId;
        this.riderId = other.riderId;
        this.startLocation = other.startLocation;
        this.endLocation = other.endLocation;
        this.dateTime = other.dateTime;
        this.points = other.points;
        this.status = other.status;
        this.proposalId = other.proposalId;
        this.driverEmail = other.driverEmail;
        this.riderEmail = other.riderEmail;
    }

    // Getters and Setters
    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }

    public String getRiderId() { return riderId; }
    public void setRiderId(String riderId) { this.riderId = riderId; }

    public String getStartLocation() { return startLocation; }
    public void setStartLocation(String startLocation) { this.startLocation = startLocation; }

    public String getEndLocation() { return endLocation; }
    public void setEndLocation(String endLocation) { this.endLocation = endLocation; }

    public Long getDateTime() { return dateTime; }
    public void setDateTime(Long dateTime) { this.dateTime = dateTime; }

    public Long getPoints() { return points; }
    public void setPoints(Long points) { this.points = points; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getProposalId() { return proposalId; }
    public void setProposalId(String proposalId) { this.proposalId = proposalId; }

    public String getDriverEmail() { return driverEmail; }
    public void setDriverEmail(String driverEmail) { this.driverEmail = driverEmail; }

    public String getRiderEmail() { return riderEmail; }
    public void setRiderEmail(String riderEmail) { this.riderEmail = riderEmail; }

}

