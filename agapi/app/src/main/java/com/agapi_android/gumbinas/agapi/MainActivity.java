package com.agapi_android.gumbinas.agapi;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;


public class MainActivity extends AppCompatActivity {

    private FrameLayout myContentLayout;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_my_profile:
                    inflateMyContentLayout(R.layout.my_profile);
                    return true;
                case R.id.navigation_contacts:
                    inflateMyContentLayout(R.layout.contacts);
                    return true;
                case R.id.navigation_settings:
                    inflateMyContentLayout(R.layout.settings);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myContentLayout = findViewById(R.id.my_content_layout);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // Inflate default layout
        inflateMyContentLayout(R.layout.contacts);
    }

    private void inflateMyContentLayout(int resource) {
        // Remove all views in the frame layout to make place for new layout
        myContentLayout.removeAllViewsInLayout();

        // Inflate the given layout inside the frame layout
        LayoutInflater layoutInflater = getLayoutInflater();
        layoutInflater.inflate(resource, myContentLayout);
    }

}
