package com.pesachoice.agent.activities;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.springframework.util.StringUtils;

import com.pesachoice.billpay.activities.PCSignupActivity;
import com.pesachoice.billpay.activities.helpers.DismissKeyboardListener;

/**
 * Created by emmy on 12/03/2018.
 */

public class PCAgentSignupActivity extends PCSignupActivity {
    private static final String CLAZZZ =PCAgentSignupActivity.class.getName();
    private PCAgentSignupActivity activity;
    private EditText phoneText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pcagent_signup);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        phoneText = (EditText) findViewById(R.id.phone);
        countryPicker1 = (TextView) findViewById(R.id.countryPicker1);
        //set the country code by default based on the user's sim card or wifi they're connected
        selectedCountry = null;
        activity = this;
        String countryCodeValue = this.getUserCountryCode(this);
        selectedCountry = countryCodeValue != null ? countryCodeMap.get(countryCodeValue) : null;
        if (!StringUtils.isEmpty(selectedCountry)) {
            countryPicker1.setText(selectedCountry.substring(selectedCountry.indexOf("+")));
        } else {
            showCountriesCode(activity);
        }

        countryPicker1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCountriesCode(activity);
            }
        });

        EditText phoneText = (EditText) findViewById(R.id.phone);
        phoneText.setOnFocusChangeListener(this);

        EditText passwordText = (EditText) findViewById(R.id.password);
        passwordText.setOnFocusChangeListener(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //set privacy and tos links
        TextView tosAndPrivacyText = (TextView) findViewById(R.id.read_tos_and_privacy);
        tosAndPrivacyText.setMovementMethod(LinkMovementMethod.getInstance());
        // Listen for keyboard dismissal
        findViewById(R.id.signup_container).setOnClickListener(new DismissKeyboardListener(this));

        /*
         * TODO: incomplete
         */
        if (savedInstanceState == null) {
            //TODO: Missing implementation
        }
    }


}
