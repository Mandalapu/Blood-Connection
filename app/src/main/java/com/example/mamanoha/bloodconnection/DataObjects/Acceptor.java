package com.example.mamanoha.bloodconnection.DataObjects;

/**
 * Created by Manu on 11/27/2016.
 */
public class Acceptor {

    private String firstName;
    private int age;
    private int userId;
    private String bloodGroup;
    private String phoneNumber;
    private String distance;

    public Acceptor(String firstName, int age, int userId, String bloodGroup, String phoneNumber, String distance)
    {
        this.firstName = firstName;
        this.age = age;
        this.userId = userId;
        this.bloodGroup = bloodGroup;
        this.phoneNumber = phoneNumber;
        this.distance = distance;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }
}
