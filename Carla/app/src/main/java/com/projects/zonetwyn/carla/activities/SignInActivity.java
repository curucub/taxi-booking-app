package com.projects.zonetwyn.carla.activities;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.projects.zonetwyn.carla.R;
import com.projects.zonetwyn.carla.fragments.SignInPhoneFragment;
import com.projects.zonetwyn.carla.utils.Event;
import com.projects.zonetwyn.carla.utils.EventBus;

import io.reactivex.functions.Consumer;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SignInActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView toolbarTitle;

    private int navigationIndex;

    private String phone;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Init Calligraphy
        /*CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Montserrat-Medium.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());*/

        setContentView(R.layout.activity_sign_in);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(Color.WHITE);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        }

        toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setTextColor(Color.BLACK);
        toolbarTitle.setText(getResources().getString(R.string.sign_in_title));

        navigationIndex = 0;

        setupFragment();
        subscribeToBus();
    }

    private void subscribeToBus() {
        EventBus.subscribe(EventBus.SUBJECT_SIGN_IN, this, new Consumer<Object>() {
            @Override
            public void accept(Object o) {
                if (o instanceof Event) {
                    Event event = (Event) o;
                    if (event.getData() != null && event.getSubject() != 0) {
                        switch (event.getSubject()) {
                            case Event.SUBJECT_SIGN_IN_GO_TO_CODE:
                                navigationIndex = 1;
                                phone = (String) event.getData();
                                break;
                        }
                        setupFragment();
                    }
                }
            }
        });
    }

    private void setupFragment() {
        Fragment fragment = null;
        switch (navigationIndex) {
            case 0:
                fragment = new SignInPhoneFragment();
                break;
        }

        try {
            getSupportFragmentManager().popBackStack();
        } catch (Exception e) { }

        if (fragment != null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content, fragment);
            fragmentTransaction.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.unregister(this);
    }
}
