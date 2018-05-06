package com.agapi_android.gumbinas.agapi.api.models;

import android.annotation.SuppressLint;

import com.agapi_android.gumbinas.agapi.api.controllers.HealthInformation;

import java.util.Date;


public class Profile {
    public long id;
    public String firstName;
    public String lastName;
    public String birthDate;
    public String phone;
    public String email;
    public String streetAddress;
    public String city;
    public String country;
    public Contact emergencyContact;

    public Profile() {
        id = -1;
        firstName = "";
        lastName = "";
        birthDate = "";
        phone = "";
        email = "";
        streetAddress = "";
        city = "";
        country = "";
        emergencyContact = null;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public String toString() {
        return String.format("id=%d\n firstName=%s\n lastName=%s\n birthDate=%s\n " +
                        "phone=%s\n email=%s\n streetAddress=%s\n city=%s\n country=%s\n emergencyContact=%d",
                id, firstName, lastName, birthDate, phone, email, streetAddress, city, country,
                emergencyContact != null ? emergencyContact.id : null);
    }
}
