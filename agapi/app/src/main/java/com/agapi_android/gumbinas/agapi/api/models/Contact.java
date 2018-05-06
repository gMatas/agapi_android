package com.agapi_android.gumbinas.agapi.api.models;

import android.annotation.SuppressLint;

import java.util.Locale;

public class Contact {
    public long id;
    public String name;
    public String phone;
    public String email;
    public String personalMessage;

    public Contact() {
        id = -1;
        name = "";
        phone = "";
        email = "";
        personalMessage = "";
    }

    @SuppressLint("DefaultLocale")
    @Override
    public String toString() {
        return String.format("id=%d\n name=%s\n phone=%s\n email=%s\n personal_message=%s",
                id, name, phone, email, personalMessage);
    }
}
