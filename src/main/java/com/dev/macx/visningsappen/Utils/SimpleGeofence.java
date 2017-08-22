package com.dev.macx.visningsappen.Utils;

import com.google.android.gms.location.Geofence;

/**
 * This class is used for..
 *
 * @author Midhun.
 */

public class SimpleGeofence {

    // Instance variables
    private String mId;

    private String status;

    private String geofenceAddress;


    private final double mLatitude;
    private final double mLongitude;
    private final float mRadius;
    private long mExpirationDuration;
    private int mTransitionType;
    /**
     * @param geofenceId The Geofence's request ID.
     * @param latitude Latitude of the Geofence's center in degrees.
     * @param longitude Longitude of the Geofence's center in degrees.
     * @param radius Radius of the geofence circle in meters.
     * @param expiration Geofence expiration duration.
     * @param transition Type of Geofence transition.
     * @param status
     */
    public SimpleGeofence(String geofenceId, double latitude, double longitude, float radius,
                          long expiration, int transition, String status, String geofenceAddress) {
        // Set the instance fields from the constructor.
        this.mId = geofenceId;
        this.mLatitude = latitude;
        this.mLongitude = longitude;
        this.mRadius = radius;
        this.mExpirationDuration = expiration;
        this.mTransitionType = transition;
        this.status=status;
        this.geofenceAddress = geofenceAddress;
    }

    // Instance field getters.
    public String getId() {
        return mId;
    }
    public void setId(String mId) {
        this.mId = mId;
    }

    public double getLatitude() {
        return mLatitude;
    }
    public double getLongitude() {
        return mLongitude;
    }
    public float getRadius() {
        return mRadius;
    }
    public long getExpirationDuration() {
        return mExpirationDuration;
    }
    public int getTransitionType() {
        return mTransitionType;
    }
    public String getGeofenceAddress() {
        return geofenceAddress;
    }

    /**
     * Creates a Location Services Geofence object from a SimpleGeofence.
     * @return A Geofence object.
     */
    public Geofence toGeofence() {
        // Build a new Geofence object.
        return new Geofence.Builder()
                .setRequestId(mId)
                .setTransitionTypes(mTransitionType)
                .setCircularRegion(mLatitude, mLongitude, mRadius)
                .setExpirationDuration(mExpirationDuration)
                .build();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}