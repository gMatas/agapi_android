package com.agapi_android.gumbinas.agapi.api.models;

import com.agapi_android.gumbinas.agapi.api.enumerators.HealthIssueCategory;

import java.util.LinkedList;
import java.util.List;

public class HealthIssue {
    public int id;
    public HealthIssueCategory category;
    public List details;
}
