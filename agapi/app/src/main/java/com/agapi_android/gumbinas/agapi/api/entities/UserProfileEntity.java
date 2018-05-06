package com.agapi_android.gumbinas.agapi.api.entities;

import android.provider.BaseColumns;

public class UserProfileEntity implements BaseColumns {

    public static final String TABLE_NAME = "UserProfile";

    public static final String COLUMN_NAME_FIRST_NAME = "first_name";
    public static final String COLUMN_NAME_LAST_NAME = "last_name";
    public static final String COLUMN_NAME_STREET_ADDRESS = "street_address";
    public static final String COLUMN_NAME_BIRTH_DATE = "birth_date";
    public static final String COLUMN_NAME_PHONE = "phone";
    public static final String COLUMN_NAME_EMAIL = "email";
    public static final String COLUMN_NAME_CITY = "city";
    public static final String COLUMN_NAME_COUNTRY = "country";
    public static final String COLUMN_NAME_EMERGENCY_CONTACT_ID = "emergency_contact_id";

    private UserProfileEntity() {}
}
