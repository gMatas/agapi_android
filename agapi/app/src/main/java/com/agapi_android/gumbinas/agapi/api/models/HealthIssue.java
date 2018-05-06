package com.agapi_android.gumbinas.agapi.api.models;

import com.agapi_android.gumbinas.agapi.api.enumerators.HealthIssueCategory;

import java.util.LinkedList;
import java.util.List;

public class HealthIssue {
    public long id;
    public HealthIssueCategory category;
    public String description;

    public HealthIssue() {
        id = -1;
        category = null;
        description = "";
    }

    public HealthIssue(HealthIssueCategory category, String description) {
        this.category = category;
        this.description = description;
    }
}
