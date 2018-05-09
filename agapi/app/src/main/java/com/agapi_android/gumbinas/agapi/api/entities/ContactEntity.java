package com.agapi_android.gumbinas.agapi.api.entities;

import android.provider.BaseColumns;

public class ContactEntity implements BaseColumns {

    public static final String TABLE_NAME = "Contact";

    public static final String COLUMN_NAME_NAME = "name";
    public static final String COLUMN_NAME_PHONE = "phone";
    public static final String COLUMN_NAME_EMAIL = "email";
    public static final String COLUMN_NAME_PERSONAL_MESSAGE = "personal_message";
    public static final String COLUMN_NAME_IN_QUEUE = "in_queue";

    private ContactEntity() {}
}
