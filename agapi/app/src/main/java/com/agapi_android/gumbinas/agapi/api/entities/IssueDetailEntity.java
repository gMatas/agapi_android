package com.agapi_android.gumbinas.agapi.api.entities;

import android.provider.BaseColumns;

public class IssueDetailEntity implements BaseColumns {

    public static final String TABLE_NAME = "IssueDetail";

    public static final String COLUMN_NAME_DETAIL_TEXT = "detail_text";
    public static final String COLUMN_NAME_FK_ISSUEDETAIL_HEALTHISSUE =
            "fk__issue_detail__health_issue";

    private IssueDetailEntity() {}
}
