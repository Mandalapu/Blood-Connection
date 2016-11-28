package com.example.mamanoha.bloodconnection.DataObjects;

/**
 * Created by Manu on 11/28/2016.
 * "requestedBG":"AB+","emerLevel":5,"distance":9.780850143745342,"requestId":2,"requestorId":5
 */

public class AwaitingRequest
{
    private String requesterName;
    private String requestBloodGroup;
    private  int emergencyLevel;
    private String distance;
    private int requestId;
    private int requesterId;

    public AwaitingRequest(String requesterName, String requestBloodGroup, int emergencyLevel, String distance,
                           int requestId, int requesterId)
    {
        this.requesterName = requesterName;
        this.requestBloodGroup = requestBloodGroup;
        this.emergencyLevel = emergencyLevel;
        this.distance =distance;
        this.requestId = requestId;
        this.requesterId = requesterId;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public String getRequestBloodGroup() {
        return requestBloodGroup;
    }

    public void setRequestBloodGroup(String requestBloodGroup) {
        this.requestBloodGroup = requestBloodGroup;
    }

    public int getEmergencyLevel() {
        return emergencyLevel;
    }

    public void setEmergencyLevel(int emergencyLevel) {
        this.emergencyLevel = emergencyLevel;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(int requesterId) {
        this.requesterId = requesterId;
    }
}
