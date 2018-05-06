package com.agapi_android.gumbinas.agapi;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.agapi_android.gumbinas.agapi.api.controllers.AgapiController;
import com.agapi_android.gumbinas.agapi.api.handlers.AgapiDBHandler;


public class MainActivity extends AppCompatActivity {

    private static final int RESULT_CODE_REFRESH_AGAPI = 0;

    private AgapiController _agapi;

    private FrameLayout _myContentLayout;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_contacts:
                    inflateMyContentLayout(R.layout.contacts);
                    return true;

                case R.id.navigation_my_profile:
                    inflateMyContentLayout(R.layout.my_profile);
                    return true;

                case R.id.navigation_settings:
                    inflateMyContentLayout(R.layout.settings);

                    _agapi.reset();
                    setContentView(R.layout.title_layout);
                    configureTitleLayout();

                    return true;
            }
            return false;
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case RESULT_CODE_REFRESH_AGAPI:
                _agapi.loadUserProfile();
                _agapi.loadUserHealthInformation();

                // Check if the user profile exists and is loaded
                if (_agapi.isUserProfileLoaded()) {
                    // Load main layout
                    setContentView(R.layout.activity_main);

                    // Configure title layout components
                    configureMainLayout();

                    // Load contacts layout inside the main layout
                    inflateMyContentLayout(R.layout.contacts);
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get AGAPI api main controller
        _agapi = new AgapiController(this);

        // Check if the user profile is loaded and it exists, and inflate appropriate layout
        if (_agapi.isUserProfileLoaded()) {
            // Load main layout
            setContentView(R.layout.activity_main);

            // Configure title layout components
            configureMainLayout();

            // Load contacts layout inside the main layout
            inflateMyContentLayout(R.layout.contacts);

        } else {
            // Load title layout for signing-up
            setContentView(R.layout.title_layout);

            // Configure title layout components
            configureTitleLayout();
        }
    }

    private void configureMainLayout() {
        _myContentLayout = findViewById(R.id.my_content_layout);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private void configureTitleLayout() {
        // Set up create user text view
        TextView createProfileText = findViewById(R.id.title_text_create_profile_button);
        createProfileText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileCreateFormActivity.class);
                startActivityForResult(intent, RESULT_CODE_REFRESH_AGAPI);
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

    private void myProfile() {

    }

    private void inflateMyContentLayout(int resource) {
        // Remove all views in the frame layout to make place for new layout
        _myContentLayout.removeAllViewsInLayout();

        // Inflate the given layout inside the frame layout
        LayoutInflater layoutInflater = getLayoutInflater();
        layoutInflater.inflate(resource, _myContentLayout);
    }

}
