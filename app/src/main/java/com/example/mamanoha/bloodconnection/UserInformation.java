package com.example.mamanoha.bloodconnection;

/**
 * Created by Manu on 11/7/2016.
 * This class is responsible to store the user information like his blood group, current token value and userid, username.
 * This fields are required to make API calls to the web services and some of the returned data can also be stored in case
 * if it  is required.
 */
//Just to amke sure that no class extends this.
public final class UserInformation
{
    private static int userId;
    private static String usernName;
    private static String bloodGroup;
    private static String token;
    private static String notificationsToken;
    private static String healthStatus;

    //Just in case.
    private UserInformation()
    {
    }

    public static int getUserId()
    {
        return userId;
    }

    public static void setUserId(int userId)
    {
        UserInformation.userId = userId;
    }

    public static String getUsernName()
    {
        return usernName;
    }

    public static void setUsernName(String usernName)
    {
        UserInformation.usernName = usernName;
    }

    public static String getBloodGroup()
    {
        return bloodGroup;
    }

    public static void setBloodGroup(String bloodGroup)
    {
        UserInformation.bloodGroup = bloodGroup;
    }

    public static String getToken()
    {
        return token;
    }

    public static void setToken(String token)
    {
        UserInformation.token = token;
    }

    public static String getNotificationsToken()
    {
        return notificationsToken;
    }

    public static void setNotificationsToken(String notificationsToken)
    {
        UserInformation.notificationsToken = notificationsToken;
    }

    public static String getHealthStatus()
    {
        return healthStatus;
    }

    public static void setHealthStatus(String healthStatus)
    {
        UserInformation.healthStatus = healthStatus;
    }
}
