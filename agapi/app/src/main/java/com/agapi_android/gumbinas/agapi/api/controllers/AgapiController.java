package com.agapi_android.gumbinas.agapi.api.controllers;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.agapi_android.gumbinas.agapi.api.handlers.bluetooth.AgapiBleHandler;
import com.agapi_android.gumbinas.agapi.api.handlers.AgapiDBHandler;
import com.agapi_android.gumbinas.agapi.api.models.Profile;

public class AgapiController {

    private Context _context;

    private Profile _userProfile;
    private HealthInformation _userHealthInformation;
    private AddressBook _userAddressBook;
    private AgapiDBHandler _adbHandler;
    private AgapiBleHandler _bleHandler;

    public AgapiController(Context context) {
        _context = context;
        _adbHandler = new AgapiDBHandler(_context);
        _userHealthInformation = new HealthInformation(_adbHandler);
        _userAddressBook = new AddressBook(_adbHandler);
        loadUserProfile();
    }

    /**
     * Check if user profile is created and it exits in the database.
     * @return true if user profile exists in the database, else if it's not.
     */
    public boolean isLoaded() {
        return _userProfile != null;
    }

    public Profile getUserProfile() {
        Profile profileCopy = new Profile();
        profileCopy.id = _userProfile.id;
        profileCopy.firstName = _userProfile.firstName;
        profileCopy.lastName = _userProfile.lastName;
        profileCopy.birthDate = _userProfile.birthDate;
        profileCopy.phone = _userProfile.phone;
        profileCopy.email = _userProfile.email;
        profileCopy.streetAddress = _userProfile.streetAddress;
        profileCopy.city = _userProfile.city;
        profileCopy.country = _userProfile.country;
        return _userProfile;
    }

    public HealthInformation getUserHealthInformation() {
        return _userHealthInformation;
    }

    public AddressBook getUserAddressBook() {
        return _userAddressBook;
    }

    public AgapiBleHandler getBleHandler() {
        return _bleHandler;
    }

    public void enableBleHandler(Activity activity) {
        _bleHandler = new AgapiBleHandler(activity);
    }

    public void disableBleHandler() {
        _bleHandler = null;
    }

    public void loadUserProfile() {
        _userProfile = _adbHandler.getUserProfile();
    }

    public void saveUserProfile(Profile profile) {
        if (isLoaded() && _userProfile.id == profile.id) {
            _adbHandler.updateUserProfile(profile);
            _userProfile = profile;

        } else {
            long profileId = _adbHandler.addUserProfile(profile);
            if (profileId >= 0) {
                _userProfile = profile;
                _userProfile.id = profileId;
                Log.d("AGAPI_DEBUG", "saveUserProfile: profile_id=" + profileId);
            } else {
                Log.e("AGAPI_DEBUG", "saveUserProfile: failed to put into database.");
            }
        }
    }

    public void clear() {
        _userHealthInformation.clear();
        _userAddressBook.clear();
        _adbHandler.removeUserProfile();
        _userProfile = null;
    }

    public void reset() {
        _adbHandler.dropAll();
        _userProfile = null;
        _userHealthInformation = new HealthInformation(_adbHandler);
        Log.d("AGAPI_DEBUG", "Database dropped.");
    }
}
