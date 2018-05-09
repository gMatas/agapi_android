package com.agapi_android.gumbinas.agapi.api.controllers;

import android.support.annotation.NonNull;
import android.util.Log;

import com.agapi_android.gumbinas.agapi.api.handlers.AgapiDBHandler;
import com.agapi_android.gumbinas.agapi.api.models.HealthIssue;

import java.util.ArrayList;
import java.util.List;

public class HealthInformation {

    private List<HealthIssue> _healthIssues;
    private AgapiDBHandler _adbHandler;

    HealthInformation(AgapiDBHandler adbHandler) {
        _adbHandler = adbHandler;
        load();
    }

    public boolean isEmpty() {
        return _healthIssues.isEmpty();
    }

    public List<HealthIssue> getHealthIssues() {
        List<HealthIssue> healthIssuesCopy = new ArrayList<>();
        if (_healthIssues == null) {
            return healthIssuesCopy;
        }
        for (HealthIssue issue : _healthIssues) {
            HealthIssue issueCopy = new HealthIssue();
            issueCopy.id = issue.id;
            issueCopy.category = issue.category;
            issueCopy.description = issue.description;
            healthIssuesCopy.add(issueCopy);
        }
        return healthIssuesCopy;
    }

    public void load() {
        _healthIssues = _adbHandler.getAllHealthIssues();
    }

    public void saveHealthIssues(@NonNull List<HealthIssue> healthIssues) {
        // If no issues present in the database
        if (_healthIssues.isEmpty()) {
            for (HealthIssue healthIssue : healthIssues) {
                healthIssue.id = _adbHandler.addHealthIssue(healthIssue);
            }
            if (!healthIssues.isEmpty())
                _healthIssues = healthIssues;
            Log.d("AGAPI_DEBUG", "saveHealthIssues: ADDED ALL.");
            return;
        }

        // If provided list is empty remove all database contained health issues
        if (healthIssues.isEmpty()) {
            for (HealthIssue healthIssue : _healthIssues) {
                _adbHandler.removeHealthIssue(healthIssue.id);
            }
            _healthIssues.clear();
            Log.d("AGAPI_DEBUG", "saveHealthIssues: REMOVED ALL.");
            return;
        }

        List<HealthIssue> updateList = new ArrayList<>();
        List<HealthIssue> addList = new ArrayList<>();
        for (HealthIssue newIssue : healthIssues) {
            boolean updated = false;
            for (HealthIssue oldIssue : _healthIssues) {
                if (newIssue.id == oldIssue.id || newIssue.category.name().equals(oldIssue.category.name())) {
                    newIssue.id = oldIssue.id;
                    _adbHandler.updateHealthIssue(newIssue);
                    updateList.add(newIssue);
                    updated = true;
                    break;
                }
            }
            if (!updated) {
                newIssue.id = _adbHandler.addHealthIssue(newIssue);
                addList.add(newIssue);
            }
        }
        for (HealthIssue oldIssue : _healthIssues) {
            boolean needDelete = true;
            for (HealthIssue newIssue : healthIssues) {
                if (oldIssue.id == newIssue.id || oldIssue.category.name().equals(newIssue.category.name())) {
                    needDelete = false;
                }
            }
            if (needDelete) {
                _adbHandler.removeHealthIssue(oldIssue.id);
            }
        }
        _healthIssues.clear();
        _healthIssues.addAll(addList);
        _healthIssues.addAll(updateList);
    }

    public void clear() {
        for (HealthIssue issue : _healthIssues)
            _adbHandler.removeHealthIssue(issue.id);
        _healthIssues.clear();
    }
}
