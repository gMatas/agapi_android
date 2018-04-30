package com.agapi_android.gumbinas.agapi.api.models;

import com.agapi_android.gumbinas.agapi.api.controllers.HealthInformation;

import java.util.Date;


public class Profile {
    public int id;
    public String firstName;
    public String lastName;
    public Date birthDate;
    public String streetAddress;
    public String city;
    public String country;
    public HealthInformation healthInfo;
    public Contact emergencyContact;
}
