package com.agapi_android.gumbinas.agapi.api.models;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.Locale;

public class Contact implements Comparable<Contact> {
    public long id;
    public String name;
    public String phone;
    public String email;
    public String personalMessage;
    public boolean inQueue;

    public Contact() {
        id = -1;
        name = "";
        phone = "";
        email = "";
        personalMessage = "";
        inQueue = false;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public String toString() {
        return String.format("id=%d\n name=%s\n phone=%s\n email=%s\n personal_message=%s\n in_queue=%b",
                id, name, phone, email, personalMessage, inQueue);
    }

    @Override
    public int compareTo(@NonNull Contact o) {
        return this.name.compareTo(o.name);
    }
}
