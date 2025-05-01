package edu.uga.cs.riderapp.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Represents a completed ride history record between a driver and a rider.
 * Stores key metadata such as participants, route, role, date/time, and status.
 */
public class RideHistory {

    private String driverId;
    private String riderId;
    private String driverName;
    private String riderName;
    private String startLocation;
    private String endLocation;
    private String role;
    private long dateTime;
    private String status;

    /**
     * Default constructor.
     */
    public RideHistory() {
    }

    // Getters and Setters
    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }

    public String getRiderId() { return riderId; }
    public void setRiderId(String riderId) { this.riderId = riderId; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    public String getRiderName() { return riderName; }
    public void setRiderName(String riderName) { this.riderName = riderName; }

    public String getStartLocation() { return startLocation; }
    public void setStartLocation(String startLocation) { this.startLocation = startLocation; }

    public String getEndLocation() { return endLocation; }
    public void setEndLocation(String endLocation) { this.endLocation = endLocation; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public long getDateTime() { return dateTime; }
    public void setDateTime(long dateTime) { this.dateTime = dateTime; }

    public String getStatus() { return status; }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Converts the stored timestamp to a formatted date string.
     *
     * @return a readable date-time string in "MM/dd/yyyy HH:mm" format
     */
    public String getDate() {
        // Create a Date object from the timestamp
        Date date = new Date(dateTime);

        // Define the desired date format
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault());

        // Format the date and return it as a string
        return sdf.format(date);
    }
}
