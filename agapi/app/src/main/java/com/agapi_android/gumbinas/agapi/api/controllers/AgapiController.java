package com.agapi_android.gumbinas.agapi.api.controllers;

import android.content.Context;
import android.util.Log;

import com.agapi_android.gumbinas.agapi.MainActivity;
import com.agapi_android.gumbinas.agapi.api.handlers.AgapiDBHandler;
import com.agapi_android.gumbinas.agapi.api.models.HealthIssue;
import com.agapi_android.gumbinas.agapi.api.models.Profile;

import java.util.List;


public class AgapiController {

    private Profile _userProfile;
    private HealthInformation _userHealthInformation;
    private AddressBook _userAddressBook;
    private AgapiDBHandler _adbHandler;

    public AgapiController(Context context) {
        _adbHandler = new AgapiDBHandler(context);
//        _adbHandler.dropAll();
        _userProfile = _adbHandler.getUserProfile();
        _userHealthInformation = new HealthInformation(_adbHandler);
    }

    /**
     * Check if user profile is created and it exits in the database.
     * @return true if user profile exists in the database, else if it's not.
     */
    public boolean isUserProfileLoaded() {
        return _userProfile != null;
    }

    public Profile getUserProfile() {
        return _userProfile;
    }

    public void saveUserProfile(Profile profile) {
        long profileId = _adbHandler.addUserProfile(profile);
        if (profileId >= 0) {
            _userProfile = profile;
            _userProfile.id = profileId;
            Log.d("AGAPI_DEBUG", "saveUserProfile: profile_id=" + profileId);
        } else {
            Log.e("AGAPI_DEBUG", "saveUserProfile: failed to put into database.");
        }
    }

    public void saveUserHealthInformation(List<HealthIssue> healthIssues) {
        _userHealthInformation.saveHealthIssues(healthIssues);
    }

    public void loadUserProfile() {
        _userProfile = _adbHandler.getUserProfile();

    }

    public void loadUserHealthInformation() {

    }

    public void reset() {
        _adbHandler.dropAll();
        _userProfile = null;
        _userHealthInformation = new HealthInformation(_adbHandler);
        Log.d("AGAPI_DEBUG", "Database dropped.");
    }
}
