package com.example.mamanoha.bloodconnection.DataObjects;

import org.json.JSONObject;

import java.util.Date;

/**
 * Created by Manu on 11/26/2016.
 */

/**
 * {"requests":[{"requestId":3,"requestedBloodType":"AB+","status":"Pending","emergencyLevel":5,"timestamp":1477590317881},
 * {"requestId":2,"requestedBloodType":"AB+","status":"Donors Found","emergencyLevel":5,"timestamp":1477588899254}],
 * "userId":5,"error":""}
 */
public class UserRequests {

    private String requestDate;
    private String bloodGroup;
    private String status;
    private int emergencyLevel;
    private int requestId;

    public UserRequests(int requestId, String requestedBloodType, String status, int emergencyLevel, String requestDate)
    {
        this.requestId = requestId;
        this.bloodGroup = requestedBloodType;
        this.status = status;
        this.requestDate = requestDate;
        this.emergencyLevel = emergencyLevel;
    }

    public int getEmergencyLevel()
    {
        return this.emergencyLevel;
    }

    public void setEmergencyLevel(int emergencyLevel)
    {
        this.emergencyLevel = emergencyLevel;
    }

    public int getRequestId()
    {
        return this.requestId;
    }

    public void setRequestId(int requestId)
    {
        this.requestId = requestId;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getBloodGroup()
    {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup)
    {
        this.bloodGroup = bloodGroup;
    }

    public String getRequestDate()
    {
        return requestDate;
    }

    public void setRequestDate(String requestDate)
    {
        this.requestDate = requestDate;
    }

}
