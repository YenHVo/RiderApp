package edu.uga.cs.riderapp.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RideHistory {

    private String driverId;
    private String riderId;
    private String driverName;
    private String riderName;
    private String startLocation;
    private String endLocation;
    private String role;
    private long dateTime;

    public RideHistory() {

    }


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

    public String getDate() {
        // Create a Date object from the timestamp
        Date date = new Date(dateTime);

        // Define the desired date format
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault());

        // Format the date and return it as a string
        return sdf.format(date);
    }
}
