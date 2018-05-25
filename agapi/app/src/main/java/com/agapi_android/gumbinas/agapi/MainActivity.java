package com.agapi_android.gumbinas.agapi;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.agapi_android.gumbinas.agapi.api.controllers.AgapiController;
import com.agapi_android.gumbinas.agapi.api.models.Contact;
import com.agapi_android.gumbinas.agapi.api.models.HealthIssue;
import com.agapi_android.gumbinas.agapi.api.models.Profile;
import com.agapi_android.gumbinas.agapi.util.BluetoothUtils;
import com.agapi_android.gumbinas.agapi.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_REFRESH_AGAPI_TO_MAIN = 0;
    private static final int REQUEST_CODE_REFRESH_AGAPI_TO_PROFILE = 1;
    private static final int REQUEST_CODE_REFRESH_AGAPI_TO_CONTACTS = 2;
    private static final int REQUEST_ENABLE_BT = 4;

    private AgapiController _agapi;

//    private BleScanner _bleScanner;

    private BottomNavigationView _navigation;
    private FrameLayout _myContentLayout;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_contacts:
                    inflateMyContentLayout(R.layout.contacts);
                    setContactsPreview();
                    return true;

                case R.id.navigation_my_profile:
                    inflateMyContentLayout(R.layout.form_template);
                    setProfilePreview();
                    return true;

                case R.id.navigation_settings:
                    inflateMyContentLayout(R.layout.settings);
                    setSettingsPreview();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_REFRESH_AGAPI_TO_MAIN:
                _agapi.loadUserProfile();
                _agapi.getUserHealthInformation().load();

                // Check if the user profile exists and is loaded
                if (_agapi.isLoaded()) {
                    // Load main layout
                    setContentView(R.layout.activity_main);
                    // Configure title layout components
                    configureMainLayout();
                    // Load contacts layout inside the main layout
                    inflateMyContentLayout(R.layout.contacts);
                    _navigation.setSelectedItemId(R.id.navigation_contacts);
                }
                break;
            case REQUEST_CODE_REFRESH_AGAPI_TO_PROFILE:
                _agapi.loadUserProfile();
                _agapi.getUserHealthInformation().load();
                // Check if the user profile exists and is loaded
                if (_agapi.isLoaded()) {
                    _navigation.setSelectedItemId(R.id.navigation_my_profile);
                }
                break;
            case REQUEST_CODE_REFRESH_AGAPI_TO_CONTACTS:
                _agapi.getUserAddressBook().load();
                _navigation.setSelectedItemId(R.id.navigation_contacts);
                break;
            case REQUEST_ENABLE_BT:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get AGAPI api main controller
        _agapi = new AgapiController(this);

        // Set up Bluetooth
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        assert bluetoothManager != null;
        _mBluetoothAdapter = bluetoothManager.getAdapter();
        _mScanning = false;
        _isConnected = false;

        AgapiMessenger messenger = new AgapiMessenger();
        Thread t1 = new Thread(messenger);
        t1.start();
//        _bleScanner = new BleScanner();
//        _agapi.enableBleHandler(this);

        // Check if the user profile is loaded and it exists, and inflate appropriate layout
        if (_agapi.isLoaded()) {
            // Load main layout
            setContentView(R.layout.activity_main);

            // Configure title layout components
            configureMainLayout();

            // Load contacts layout inside the main layout
            _navigation.setSelectedItemId(R.id.navigation_contacts);

        } else {
            // Load title layout for signing-up
            setContentView(R.layout.title_layout);

            // Configure title layout components
            configureTitleLayout();
        }
    }

    private void configureMainLayout() {
        _myContentLayout = findViewById(R.id.my_content_layout);
        _navigation = findViewById(R.id.navigation);
        _navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private void configureTitleLayout() {
        setTitle(R.string.app_name);
        // Set up create user text view
        TextView createProfileText = findViewById(R.id.title_text_create_profile_button);
        createProfileText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileCreateFormActivity.class);
                startActivityForResult(intent, REQUEST_CODE_REFRESH_AGAPI_TO_MAIN);
            }
        });

        TextView aboutButton = findViewById(R.id.title_text_help_button);
        aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _agapi.reset();
            }
        });
    }

    private void setProfilePreview() {
        setTitle(R.string.title_my_profile);

        ViewGroup contentLayout = findViewById(R.id.form_content_layout);

        LayoutInflater inflater = getLayoutInflater();
        inflater.inflate(R.layout.my_profile, contentLayout);

        Profile userProfile = _agapi.getUserProfile();
        TextView textView;
        textView = findViewById(R.id.form_profile_text_name);
        textView.setText(userProfile.firstName);
        textView = findViewById(R.id.form_profile_text_surname);
        textView.setText(userProfile.lastName);
        textView = findViewById(R.id.form_profile_text_birthdate);
        textView.setText(userProfile.birthDate);
        textView = findViewById(R.id.form_profile_text_phone);
        textView.setText(userProfile.phone);
        textView = findViewById(R.id.form_profile_text_email);
        textView.setText(userProfile.email);
        textView = findViewById(R.id.form_profile_text_street_address);
        textView.setText(userProfile.streetAddress);
        textView = findViewById(R.id.form_profile_text_city);
        textView.setText(userProfile.city);
        textView = findViewById(R.id.form_profile_text_country);
        textView.setText(userProfile.country);

        List<HealthIssue> healthIssues = _agapi.getUserHealthInformation().getHealthIssues();
        ViewGroup healthInfoTitleLayout = findViewById(R.id.my_profile_health_information_title_layout);
        ViewGroup healthInfoContentLayout = findViewById(R.id.my_profile_health_information_content_layout);
        healthInfoContentLayout.removeAllViews();

        if (!healthIssues.isEmpty()) {
            LinearLayoutCompat.LayoutParams healthIssueLayoutParams = new LinearLayoutCompat.LayoutParams(
                    LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                    LinearLayoutCompat.LayoutParams.WRAP_CONTENT);
            healthIssueLayoutParams.bottomMargin =
                    (int) getResources().getDimension(R.dimen.activity_vertical_margin_big);

            for (HealthIssue healthIssue : healthIssues) {
                LinearLayoutCompat healthIssueLayout = new LinearLayoutCompat(this);
                healthIssueLayout.setOrientation(LinearLayoutCompat.VERTICAL);
                healthIssueLayout.setLayoutParams(healthIssueLayoutParams);
                healthInfoContentLayout.addView(healthIssueLayout);

                TextView healthIssueTitle = new TextView(this);
                healthIssueTitle.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
                healthIssueTitle.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
                healthIssueTitle.setPadding(0, 0, 0,
                        (int) getResources().getDimension(R.dimen.activity_vertical_margin_half));
                healthIssueTitle.setText(healthIssue.category.getDescription());
                healthIssueTitle.setTextColor(getResources().getColor(R.color.colorAccent));
                healthIssueLayout.addView(healthIssueTitle);

                TextView healthIssueDescription = new TextView(this);
                healthIssueTitle.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
                healthIssueTitle.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
                healthIssueDescription.setHint(R.string.form_profile_preview_text_no_description);
                healthIssueDescription.setText(healthIssue.description);
                healthIssueDescription.setTextSize(18);
                healthIssueDescription.setTextColor(getResources().getColor(R.color.colorTextBlack));
                healthIssueLayout.addView(healthIssueDescription);

                healthInfoTitleLayout.setVisibility(View.VISIBLE);
            }
        } else {
            healthInfoTitleLayout.setVisibility(View.GONE);
        }

        Button editProfileButton = findViewById(R.id.form_button_right);
        editProfileButton.setText(R.string.form_profile_preview_button_right);
        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileCreateFormActivity.class);
                startActivityForResult(intent, REQUEST_CODE_REFRESH_AGAPI_TO_PROFILE);
            }
        });

        Button deleteProfileButton = findViewById(R.id.form_button_left);
        deleteProfileButton.setText(R.string.form_profile_preview_button_left);
        deleteProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Dėmesio!")
                        .setMessage("Ar tikrai norite ištrinti visą savo profilio informaciją, " +
                                "bei viską kas su ja susiję?")
                        .setPositiveButton("Taip", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                _agapi.clear();
                                setContentView(R.layout.title_layout);
                                configureTitleLayout();
                            }
                        }).setNegativeButton("Ne", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).show();
            }
        });
    }

    private void setContactsPreview() {
        setTitle(R.string.title_contacts);

        FloatingActionButton addNewContactButton = findViewById(R.id.contacts_button_add_new);
        addNewContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ContactCreateFormActivity.class);
                startActivityForResult(intent, REQUEST_CODE_REFRESH_AGAPI_TO_CONTACTS);
            }
        });

        ViewGroup contactsContentLayout = findViewById(R.id.contacts_content_layout);

        LinearLayoutCompat.LayoutParams queueCircleLayoutParams = new LinearLayoutCompat.LayoutParams(
                (int) getResources().getDimension(R.dimen.contact_queue_circle_diameter),
                (int) getResources().getDimension(R.dimen.contact_queue_circle_diameter));
        queueCircleLayoutParams.setMargins(
                (int) getResources().getDimension(R.dimen.activity_horizontal_margin_half),
                (int) getResources().getDimension(R.dimen.activity_horizontal_margin_half),
                (int) getResources().getDimension(R.dimen.activity_horizontal_margin_half),
                (int) getResources().getDimension(R.dimen.activity_horizontal_margin_half));

        LinearLayoutCompat.LayoutParams cardTextLayoutParams = new LinearLayoutCompat.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardTextLayoutParams.gravity = Gravity.CENTER;

        LinearLayoutCompat.LayoutParams contactCardLayoutParams = new LinearLayoutCompat.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        contactCardLayoutParams.topMargin = (int) getResources().getDimension(R.dimen.activity_vertical_margin_half);
        contactCardLayoutParams.leftMargin = (int) getResources().getDimension(R.dimen.activity_horizontal_margin_half);
        contactCardLayoutParams.rightMargin = (int) getResources().getDimension(R.dimen.activity_horizontal_margin_half);

        TextView noContactsTextView = findViewById(R.id.contacts_text_no_entries);
        if(_agapi.getUserAddressBook().isEmpty()) {
            noContactsTextView.setVisibility(View.VISIBLE);
        } else {
            noContactsTextView.setVisibility(View.GONE);

            List<Contact> userContacts = _agapi.getUserAddressBook().getContacts();
            Collections.sort(userContacts);
            for (final Contact contact : userContacts) {

                CardView contactCard = new CardView(this);
                contactCard.setLayoutParams(contactCardLayoutParams);
                contactsContentLayout.addView(contactCard);

                LinearLayoutCompat cardContentLayout = new LinearLayoutCompat(this);
                cardContentLayout.setOrientation(LinearLayoutCompat.HORIZONTAL);
                contactCard.addView(cardContentLayout);

                View queueCircle = new View(this);
                queueCircle.setLayoutParams(queueCircleLayoutParams);
                queueCircle.setBackground(getDrawable(R.drawable.circle_24dp));
                ColorStateList queueCircleColorStateList;
                if (contact.inQueue)
                    queueCircleColorStateList = getResources().getColorStateList(R.color.color_state_list_in_queue);
                else
                    queueCircleColorStateList = getResources().getColorStateList(R.color.color_state_list_not_in_queue);
                queueCircle.setBackgroundTintList(queueCircleColorStateList);
                cardContentLayout.addView(queueCircle);

                LinearLayoutCompat cardTextLayout = new LinearLayoutCompat(this);
                cardTextLayout.setOrientation(LinearLayoutCompat.VERTICAL);
                cardTextLayout.setLayoutParams(cardTextLayoutParams);
                cardContentLayout.addView(cardTextLayout);

                TextView cardNameTextView = new TextView(this);
                cardNameTextView.setPaddingRelative(
                        (int) getResources().getDimension(R.dimen.activity_horizontal_margin_half),
                        0, 0,
                        (int) getResources().getDimension(R.dimen.activity_vertical_margin_half));
                cardNameTextView.setTextSize(18);
                cardNameTextView.setTypeface(null, Typeface.BOLD);
                cardNameTextView.setTextColor(getResources().getColor(R.color.colorTextBlack));
                cardNameTextView.setText(contact.name);
                cardTextLayout.addView(cardNameTextView);

                TextView cardPhoneTextView = new TextView(this);
                cardPhoneTextView.setPaddingRelative(
                        (int) getResources().getDimension(R.dimen.activity_horizontal_margin_half),
                        0, 0, 0);
                cardPhoneTextView.setTextSize(16);
                cardPhoneTextView.setText(contact.phone);
                cardTextLayout.addView(cardPhoneTextView);

                contactCard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ScrollView contactPreviewContentLayout = new ScrollView(MainActivity.this);
                        contactPreviewContentLayout.setPadding(
                                (int) getResources().getDimension(R.dimen.activity_horizontal_margin_half),
                                (int) getResources().getDimension(R.dimen.activity_vertical_margin_half),
                                (int) getResources().getDimension(R.dimen.activity_horizontal_margin_half),
                                (int) getResources().getDimension(R.dimen.activity_vertical_margin_half));
                        LayoutInflater inflater = getLayoutInflater();
                        inflater.inflate(R.layout.form_contact_preview, contactPreviewContentLayout);

                        CheckBox inQueuePreviewCheckBox = contactPreviewContentLayout.findViewById(R.id.form_contact_text_in_queue);
                        TextView namePreviewTextView = contactPreviewContentLayout.findViewById(R.id.form_contact_text_name);
                        TextView phonePreviewTextView = contactPreviewContentLayout.findViewById(R.id.form_contact_text_phone);
                        TextView emailPreviewTextView = contactPreviewContentLayout.findViewById(R.id.form_contact_text_email);
                        TextView personalMessagePreviewTextView = contactPreviewContentLayout.findViewById(R.id.form_contact_text_personal_message);

                        inQueuePreviewCheckBox.setChecked(contact.inQueue);
                        inQueuePreviewCheckBox.setText(contact.inQueue ?
                                R.string.form_contact_text_in_queue : R.string.form_contact_text_not_in_queue);
                        namePreviewTextView.setText(contact.name);
                        phonePreviewTextView.setText(contact.phone);
                        emailPreviewTextView.setText(contact.email);
                        personalMessagePreviewTextView.setText(contact.personalMessage);

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("").setView(contactPreviewContentLayout)
                                .setPositiveButton("Redaguoti", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(MainActivity.this, ContactCreateFormActivity.class);
                                        intent.setAction(String.valueOf(contact.id));
                                        startActivityForResult(intent, REQUEST_CODE_REFRESH_AGAPI_TO_CONTACTS);
                                    }
                                })
                                .setNegativeButton("Grįžti", null)
                                .setNeutralButton("Ištrinti", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        AlertDialog.Builder deleteConfirmDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                                        deleteConfirmDialogBuilder.setTitle("Dėmesio!")
                                                .setMessage("Ar tikrai norite ištrinti šį kontaktą?")
                                                .setPositiveButton("Taip", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        _agapi.getUserAddressBook().deleteContact(contact.id);
                                                        _navigation.setSelectedItemId(R.id.navigation_contacts);
                                                    }
                                                }).setNegativeButton("Ne", null).show();
                                    }
                                }).show();
                    }
                });
            }
        }
    }

    private void setSettingsPreview() {
        setTitle(R.string.title_settings);

        final LinearLayoutCompat settingsContentLayout = findViewById(R.id.settings_content_layout);

        FloatingActionButton addNewContactButton = findViewById(R.id.settings_button_scan);
        addNewContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDevicesContentLayout(settingsContentLayout);
                startScan();
            }
        });
    }

    private void inflateMyContentLayout(int resource) {
        // Remove all views in the frame layout to make place for new layout
        _myContentLayout.removeAllViewsInLayout();

        // Inflate the given layout inside the frame layout
        LayoutInflater layoutInflater = getLayoutInflater();
        layoutInflater.inflate(resource, _myContentLayout);
    }

    private static int spToPx(float sp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
                context.getResources().getDisplayMetrics());
    }

////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////

    private static final long SCAN_PERIOD = 10000;

    private ViewGroup _contentLayout;

    private BluetoothAdapter _mBluetoothAdapter;
    private boolean _mScanning;
    private Handler _mHandler;

    private BluetoothLeScanner _mBluetoothLeScanner;
    private Map<String, BluetoothDevice> _mScanResults;
    private BleScanCallback _mScanCallback;

    private boolean _isConnected;
    private BluetoothGatt _mGatt;

    private boolean _mEchoInitialized;

    private AgapiBtMessage _incomingMessage = new AgapiBtMessage();


    public void setDevicesContentLayout(ViewGroup contentLayout) {
        _contentLayout = contentLayout;
    }

    public void startScan() {
        if (!hasPermissions() || _mScanning) {
            return;
        }
        List<ScanFilter> filters = new ArrayList<>();
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED).build();

        _mScanResults = new HashMap<>();
        _mScanCallback = new BleScanCallback(_mScanResults);

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
        try {
            if (_mScanning && _mBluetoothAdapter != null && _mBluetoothAdapter.isEnabled() && _mBluetoothLeScanner != null) {
                _mBluetoothLeScanner.stopScan(_mScanCallback);
                scanComplete();
            }
            _mScanCallback = null;
            _mHandler = null;
            _mScanning = false;
        } catch (NullPointerException npe) {
            Log.e("AGAPI_DEBUG", "stopScan: Null pointer exception" + npe.getMessage());
        }
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
            startActivity(enableBtIntent);
            Log.d("AGAPI_DEBUG", "Requested user enables Bluetooth. Try starting the scan again.");
        }
    }

    private void connectDevice(BluetoothDevice device) {
        Log.d("AGAPI_DEBUG", "connectDevice: " + device.getAddress());
        BleGattCallback gattCallback = new BleGattCallback();
        _mGatt = device.connectGatt(MainActivity.this, true, gattCallback);
    }

    private void disconnectDevice() {
        Log.d("AGAPI_DEBUG", "Closing Gatt connection");
//            clearLogs();
        _isConnected = false;
        _mEchoInitialized = false;
        if (_mGatt != null) {
            _mGatt.disconnect();
            _mGatt.close();
        }
    }

    private void reloadDetectedDevices() {
        _contentLayout.removeAllViews();

        if (_mScanResults == null || _mScanResults.isEmpty())
            return;

        LinearLayoutCompat.LayoutParams deviceCardLayoutParams = new LinearLayoutCompat.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        deviceCardLayoutParams.topMargin = (int) getResources().getDimension(R.dimen.activity_vertical_margin_half);
        deviceCardLayoutParams.leftMargin = (int) getResources().getDimension(R.dimen.activity_horizontal_margin_half);
        deviceCardLayoutParams.rightMargin = (int) getResources().getDimension(R.dimen.activity_horizontal_margin_half);

        LinearLayoutCompat.LayoutParams circleViewLayoutParams = new LinearLayoutCompat.LayoutParams(
                (int) getResources().getDimension(R.dimen.contact_queue_circle_diameter),
                (int) getResources().getDimension(R.dimen.contact_queue_circle_diameter));
        circleViewLayoutParams.setMargins(
                (int) getResources().getDimension(R.dimen.activity_horizontal_margin_half),
                (int) getResources().getDimension(R.dimen.activity_horizontal_margin_half),
                (int) getResources().getDimension(R.dimen.activity_horizontal_margin_half),
                (int) getResources().getDimension(R.dimen.activity_horizontal_margin_half));

        for (final BluetoothDevice device : _mScanResults.values()) {
            CardView deviceCard = new CardView(MainActivity.this);
            deviceCard.setLayoutParams(deviceCardLayoutParams);
            _contentLayout.addView(deviceCard);

            LinearLayoutCompat cardContentLayout = new LinearLayoutCompat(MainActivity.this);
            cardContentLayout.setOrientation(LinearLayoutCompat.HORIZONTAL);
            deviceCard.addView(cardContentLayout);

            View connectionCircle = new View(MainActivity.this);
            connectionCircle.setLayoutParams(circleViewLayoutParams);
            connectionCircle.setBackground(getDrawable(R.drawable.circle_24dp));
            ColorStateList connectionCircleColorStateList;
            connectionCircleColorStateList = getResources().getColorStateList(R.color.color_state_list_not_in_queue);
            connectionCircle.setBackgroundTintList(connectionCircleColorStateList);
            cardContentLayout.addView(connectionCircle);

            LinearLayoutCompat cardTextContentLayout = new LinearLayoutCompat(MainActivity.this);
            cardTextContentLayout.setOrientation(LinearLayoutCompat.VERTICAL);
            cardContentLayout.addView(cardTextContentLayout);

            TextView cardNameTextView = new TextView(MainActivity.this);
            cardNameTextView.setPaddingRelative(
                    (int) getResources().getDimension(R.dimen.activity_horizontal_margin_half),
                    0, 0,
                    (int) getResources().getDimension(R.dimen.activity_vertical_margin_half));
            cardNameTextView.setTextSize(18);
            if (device.getName() != null && !device.getName().trim().isEmpty()) {
                cardNameTextView.setTypeface(null, Typeface.BOLD);
                cardNameTextView.setTextColor(getResources().getColor(R.color.colorTextBlack));
                cardNameTextView.setText(device.getName());
            } else {
                cardNameTextView.setText("Nėra pavadinimo");
            }
            cardTextContentLayout.addView(cardNameTextView);

            TextView cardAddressTextView = new TextView(MainActivity.this);
            cardAddressTextView.setPaddingRelative(
                    (int) getResources().getDimension(R.dimen.activity_horizontal_margin_half),
                    0, 0,
                    (int) getResources().getDimension(R.dimen.activity_vertical_margin_half));
            cardAddressTextView.setTextSize(14);
            cardAddressTextView.setText(device.getAddress());
            cardTextContentLayout.addView(cardAddressTextView);

            deviceCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopScan();
                    String positiveButtonText;
                    positiveButtonText = "PRISIJUNGTI";
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Įrenginio informacija")
                            .setMessage(String.format("%s\n%s", device.getName(), device.getAddress()))
                            .setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    connectDevice(device);
                                    dialog.cancel();
                                }
                            }).setNegativeButton("GRĮŽTI", null)
                            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    return;
                                }
                            })
                            .show();
                }
            });
        }
    }

    private void sendMessage(String message) {
        if (!_isConnected || !_mEchoInitialized) {
            return;
        }

        BluetoothGattCharacteristic characteristic = BluetoothUtils.findEchoCharacteristic(_mGatt);
        if (characteristic == null) {
            Log.e("AGAPI_DEBUG", "Unable to find echo characteristic.");
            disconnectDevice();
            return;
        }

        Log.d("AGAPI_DEBUG", "Sending message: " + message);

        byte[] messageBytes = StringUtils.bytesFromString(message);
        if (messageBytes.length == 0) {
            Log.e("AGAPI_DEBUG", "Unable to convert message to bytes");
            return;
        }

        characteristic.setValue(messageBytes);
        boolean success = _mGatt.writeCharacteristic(characteristic);
        if (success) {
            Log.d("AGAPI_DEBUG", "Wrote: " + StringUtils.byteArrayInHexFormat(messageBytes));
        } else {
            Log.e("AGAPI_DEBUG", "Failed to write data");
        }
    }

    public void initializeEcho() {
        _mEchoInitialized = true;
    }

    private class BleScanCallback extends ScanCallback {

        private Map<String, BluetoothDevice> mScanResults;

        BleScanCallback(Map<String, BluetoothDevice> scanResults) {
            this.mScanResults = scanResults;
        }

        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addScanResult(result);
                    reloadDetectedDevices();
                }
            });
        }

        @Override
        public void onBatchScanResults(final List<ScanResult> results) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (ScanResult result : results) {
                        addScanResult(result);
                    }
                    reloadDetectedDevices();
                }
            });
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

    private class BleGattCallback extends BluetoothGattCallback {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.d("AGAPI_DEBUG", "onConnectionStateChange newState: " + newState);

            if (status == BluetoothGatt.GATT_FAILURE) {
                Log.e("AGAPI_DEBUG", "Connection Gatt failure status " + status);
                disconnectDevice();
                return;
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                // handle anything not SUCCESS as failure
                Log.e("AGAPI_DEBUG", "Connection not GATT sucess status " + status);
                disconnectDevice();
                return;
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("AGAPI_DEBUG", "Connected to device " + gatt.getDevice().getAddress());
                _isConnected = true;
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("AGAPI_DEBUG", "Disconnected from device");
                disconnectDevice();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.d("AGAPI_DEBUG", "Device service discovery unsuccessful, status " + status);
                return;
            }

            List<BluetoothGattCharacteristic> matchingCharacteristics = BluetoothUtils.findCharacteristics(gatt);
            if (matchingCharacteristics.isEmpty()) {
                Log.e("AGAPI_DEBUG", "Unable to find characteristics.");
                return;
            }

            Log.d("AGAPI_DEBUG", "Initializing: setting write type and enabling notification");
            for (BluetoothGattCharacteristic characteristic : matchingCharacteristics) {
                characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                enableCharacteristicNotification(gatt, characteristic);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("AGAPI_DEBUG", "Characteristic written successfully");
            } else {
                Log.e("AGAPI_DEBUG", "Characteristic write unsuccessful, status: " + status);
                disconnectDevice();
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("AGAPI_DEBUG", "Characteristic read successfully");
                readCharacteristic(characteristic);
            } else {
                Log.e("AGAPI_DEBUG", "Characteristic read unsuccessful, status: " + status);
                // Trying to read from the Time Characteristic? It doesnt have the property or permissions
                // set to allow this. Normally this would be an error and you would want to:
                // disconnectGattServer();
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.d("AGAPI_DEBUG", "Characteristic changed, " + characteristic.getUuid().toString());
            readCharacteristic(characteristic);
        }

        private void enableCharacteristicNotification(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            boolean characteristicWriteSuccess = gatt.setCharacteristicNotification(characteristic, true);
            if (characteristicWriteSuccess) {
                Log.d("AGAPI_DEBUG", "Characteristic notification set successfully for " + characteristic.getUuid().toString());
                if (BluetoothUtils.isEchoCharacteristic(characteristic)) {
                    initializeEcho();
                }
            } else {
                Log.e("AGAPI_DEBUG",  "Characteristic notification set failure for " + characteristic.getUuid().toString());
            }
        }

        private void readCharacteristic(BluetoothGattCharacteristic characteristic) {
            byte[] messageBytes = characteristic.getValue();
            Log.d("AGAPI_DEBUG", "Read: " + StringUtils.byteArrayInHexFormat(messageBytes));
            String message = StringUtils.stringFromBytes(messageBytes);
            if (message == null) {
                Log.e("AGAPI_DEBUG", "Unable to convert bytes to string");
                return;
            }
            _incomingMessage.constructMessage(message);
            Log.d("AGAPI_DEBUG", "Received message: " + _incomingMessage.getMessage());
        }
    }

    private class AgapiMessenger implements Runnable {

        @Override
        public void run() {
            assert _incomingMessage != null;
            while (true) {

                if (!_incomingMessage.isComplete())
                    continue;

                Profile userProfile = _agapi.getUserProfile();
                List<HealthIssue> healthIssues = _agapi.getUserHealthInformation().getHealthIssues();
                List<Contact> emergencyContacts = _agapi.getUserAddressBook().getContacts();
                switch (_incomingMessage.getData().action) {
                    case "HELP":
                        for (Contact contact : emergencyContacts) {
                            if (contact.inQueue) {
                                String birthYear = userProfile.birthDate.split("/")[0].trim();
                                String healthIssuesString = "";
                                for (HealthIssue issue : healthIssues)
                                    healthIssuesString = healthIssuesString.concat(issue.description + ". ");
                                String message = String.format(
                                    "Patyriau griūtį šioje vietoje: K. Baršausko g. 59, Kaunas. " +
                                    "Aplinkos temperatūra yra 23 laipsniai pagal celcijų.\n" +
                                    "Aš esu %s %s, esu %s metų gimimo.\n" +
                                    "Papildoma informacija:\n" +
                                    "Gyvenu adresu %s, %s, %s" +
                                    "Pateikiu savo sveikatos info:\n%s",
                                        userProfile.firstName, userProfile.lastName, birthYear,
                                        userProfile.streetAddress, userProfile.city, userProfile.country,
                                        healthIssuesString);
                                sendSMS(contact.phone, message);
                            }
                        }
                        sendMessage("<AGAPI=HELP_OK>");
                        break;
                    case "HELP_ABORT":
                        for (Contact contact : emergencyContacts) {
                            if (contact.inQueue) {
                                String message = String.format(
                                        "Pagalbos nebereikia. Ačiū ir atsiprašau už sukeltus sunkumus.\n" +
                                        "%s %s", userProfile.firstName, userProfile.lastName);
                                sendSMS(contact.phone, message);
                                log("HELP ABORT SMS message was sent.");
                            }
                        }
                        sendMessage("<AGAPI=HELP_ABORT_OK>");
                        break;
                }
                _incomingMessage.clear();
            }
        }

        private void sendSMS(String phoneNo, String msg) {
            SmsManager smsManager = SmsManager.getDefault();
            try {
                if (msg.length() > 70) {
                    ArrayList<String> parts = smsManager.divideMessage(msg);
                    smsManager.sendMultipartTextMessage(phoneNo, null, parts, null, null);
                } else {
                    smsManager.sendTextMessage(phoneNo, null, msg, null, null);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Message Sent",
                                Toast.LENGTH_LONG).show();
                    }
                });
                Log.d("AGAPI_DEBUG", "sendSMS: message sent to phoneNo " + phoneNo);
            } catch (final Exception ex) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),ex.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
                ex.printStackTrace();
            }
        }
    }

    private static void log(String string) {
        Log.d("AGAPI_DEBUG", string);
    }


    private class AgapiBtMessage {
        private String _message;
        private Data _data;
        private boolean _isOpen;
        private boolean _isComplete;

        AgapiBtMessage() {
            _message = "";
            _isOpen = true;
        }

        public Data getData() {
            return _data;
        }

        public void setMessage(String message) {
            _message = message;
            buildData();
            _isOpen = false;
        }

        public String getMessage() {
            return "<" + _message + ">\n";
        }

        public void constructMessage(String message) {
            if (!_isOpen) {
                return;
            }

            _message += message;
            int opening = _message.indexOf('<');
            int closing = _message.indexOf('>', opening);

            if (opening < 0) {
                _message = "";
                return;
            }

            if (closing >= 0) {
                _message = _message.substring(opening + 1, closing);
                buildData();
                _isOpen = false;
            }
        }

        private void buildData() {
            _data = new Data(_message);
            _isComplete = true;
        }

        public boolean isComplete() {
            return _isComplete;
        }

        public void open() {
            _message = "";
            _isComplete = false;
            _isOpen = true;
        }

        public void clear() {
            _message = "";
            _data = null;
            _isComplete = false;
            _isOpen = true;
        }

        private class Data {

            public String action = "";
            public int temperature = 0;

            private Data(String dataString) {
                String[] dataArray = dataString.split("\\+");
                for (String part : dataArray) {
                    String[] dataPartContent = part.split("=");
                    switch (dataPartContent[0]) {
                        case "AGAPI":
                            action = dataPartContent[1];
                            break;
                        case "TEMPERATURE":
                            temperature = Integer.valueOf(dataPartContent[1]);
                            break;
                    }
                }
            }
        }
    }
}
