package com.agapi_android.gumbinas.agapi.api.entities;

import android.provider.BaseColumns;

public class HealthIssueEntity implements BaseColumns {

    public static final String TABLE_NAME = "HealthIssue";

    public static final String COLUMN_NAME_CATEGORY = "category";
    public static final String COLUMN_NAME_DESCRIPTION = "description";

    private HealthIssueEntity() {}
}
