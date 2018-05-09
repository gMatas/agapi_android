package com.agapi_android.gumbinas.agapi;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutCompat;
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

import com.agapi_android.gumbinas.agapi.api.controllers.AgapiController;
import com.agapi_android.gumbinas.agapi.api.models.Contact;
import com.agapi_android.gumbinas.agapi.api.models.HealthIssue;
import com.agapi_android.gumbinas.agapi.api.models.Profile;

import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_REFRESH_AGAPI_TO_MAIN = 0;
    private static final int REQUEST_CODE_REFRESH_AGAPI_TO_PROFILE = 1;
    private static final int REQUEST_CODE_REFRESH_AGAPI_TO_CONTACTS = 2;

    private AgapiController _agapi;

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
//                    _agapi.reset();
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
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get AGAPI api main controller
        _agapi = new AgapiController(this);

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
                                })
                                .show();
                    }
                });
            }
        }
    }

    private void setSettingsPreview() {
        setTitle(R.string.title_settings);
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
}
