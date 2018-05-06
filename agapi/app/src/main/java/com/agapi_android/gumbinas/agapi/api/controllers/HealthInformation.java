package com.agapi_android.gumbinas.agapi.api.controllers;

import android.content.Context;
import android.util.Log;

import com.agapi_android.gumbinas.agapi.api.handlers.AgapiDBHandler;
import com.agapi_android.gumbinas.agapi.api.models.HealthIssue;

import java.util.List;

public class HealthInformation {
    private List<HealthIssue> _healthIssues;
    private AgapiDBHandler _adbHandler;

    public HealthInformation(AgapiDBHandler adbHandler) {
        _adbHandler = adbHandler;
    }

    public void saveHealthIssues(List<HealthIssue> healthIssues) {
        for (HealthIssue healthIssue : healthIssues) {
            healthIssue.id = _adbHandler.addHealthIssue(healthIssue);
        }
        _healthIssues = healthIssues;
        Log.d("AGAPI_DEBUG", "saveHealthIssues: SAVED.");
    }
}
