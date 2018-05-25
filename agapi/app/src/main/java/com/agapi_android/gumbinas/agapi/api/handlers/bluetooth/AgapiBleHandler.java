package com.agapi_android.gumbinas.agapi.api.handlers.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;

import java.util.Map;

public class AgapiBleHandler {

    private AgapiBleScanner _agapiBleScanner;

    public AgapiBleHandler(Activity activity) {
        _agapiBleScanner = new AgapiBleScanner(activity);
    }

    public Map<String, BluetoothDevice> getScanResults() {
        return _agapiBleScanner.getScanResults();
    }

    public void startScan() {
        _agapiBleScanner.startScan();
    }

    public void stopScan() {
        _agapiBleScanner.stopScan();
    }
}
