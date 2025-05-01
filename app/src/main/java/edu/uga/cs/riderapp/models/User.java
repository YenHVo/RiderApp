package edu.uga.cs.riderapp.models;

import java.util.Date;

/**
 * Represents a user in the RiderApp system.
 * Stores information such as user ID, email, display name, points, and registration date.
 */
public class User {
    private String userId;
    private String email;
    private String name;
    private long points;
    private Date createdAt;

    /**
     * Default constructor required for Firebase deserialization.
     * Initializes points to 0 and sets the current date as createdAt.
     */
    public User() {
        this.points = 0;
        this.createdAt = new Date();
    }

    /**
     * Full constructor for restoring a user with all attributes.
     *
     * @param userId    Unique user ID (UID from Firebase).
     * @param email     User's email.
     * @param name      Display name.
     * @param points    Accumulated ride points.
     * @param createdAt Date when the account was created.
     */
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
