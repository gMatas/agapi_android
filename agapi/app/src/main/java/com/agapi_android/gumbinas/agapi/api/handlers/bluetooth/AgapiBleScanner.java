package com.agapi_android.gumbinas.agapi.api.handlers.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class AgapiBleScanner extends ScanCallback {

    private static final long SCAN_PERIOD = 10000;

    private Activity _activity;
    private Context _context;

    private BluetoothAdapter _mBluetoothAdapter;
    private boolean _mScanning;
    private Handler _mHandler;

    private BluetoothLeScanner _mBluetoothLeScanner;
    private Map<String, BluetoothDevice> _mScanResults;
    private AgapiBleScanner.BleScanCallback _mScanCallback;

    AgapiBleScanner(Activity activity) {
        _activity = activity;
        _context = activity.getApplicationContext();
        BluetoothManager bluetoothManager = (BluetoothManager) _context.getSystemService(Context.BLUETOOTH_SERVICE);
        assert bluetoothManager != null;
        _mBluetoothAdapter = bluetoothManager.getAdapter();
        _mScanning = false;
    }

    public Map<String, BluetoothDevice> getScanResults() {
        return _mScanResults == null ? new HashMap<String, BluetoothDevice>() : _mScanResults;
    }

    public void startScan() {
        if (!hasPermissions() || _mScanning) {
            return;
        }
        List<ScanFilter> filters = new ArrayList<>();
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED).build();

        _mScanResults = new HashMap<>();
        _mScanCallback = new AgapiBleScanner.BleScanCallback(_mScanResults);

        _mBluetoothLeScanner = _mBluetoothAdapter.getBluetoothLeScanner();
        _mBluetoothLeScanner.startScan(filters, settings, _mScanCallback);
        _mScanning = true;

        _mHandler = new Handler();
        _mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScan();
            }
        }, SCAN_PERIOD);
    }

    public void stopScan() {
        Log.d("AGAPI_DEBUG", "stopScan: ");
        if (_mScanning && _mBluetoothAdapter != null && _mBluetoothAdapter.isEnabled() && _mBluetoothLeScanner != null) {
            _mBluetoothLeScanner.stopScan(_mScanCallback);
            scanComplete();
        }
        _mScanCallback = null;
        _mHandler = null;
        _mScanning = false;
    }

    private void scanComplete() {
        if (_mScanResults.isEmpty()) {
            return;
        }

    }

    private boolean hasPermissions() {
        if (_mBluetoothAdapter == null || !_mBluetoothAdapter.isEnabled()) {
            requestBluetoothEnable();
            return false;
        }
        return true;
    }

    private void requestBluetoothEnable() {
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (_mBluetoothAdapter == null || !_mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            _activity.startActivity(enableBtIntent);
            Log.d("AGAPI_DEBUG", "Requested user enables Bluetooth. Try starting the scan again.");
        }
    }

    private class BleScanCallback extends ScanCallback {

        private Map<String, BluetoothDevice> mScanResults;

        BleScanCallback(Map<String, BluetoothDevice> scanResults) {
            this.mScanResults = scanResults;

        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            addScanResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) {
                addScanResult(result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.d("AGAPI_DEBUG", "BLE Scan Failed with code " + errorCode);
        }

        private void addScanResult(ScanResult result) {
            BluetoothDevice device = result.getDevice();
            String deviceAddress = device.getAddress();
            this.mScanResults.put(deviceAddress, device);
        }
    }
}
