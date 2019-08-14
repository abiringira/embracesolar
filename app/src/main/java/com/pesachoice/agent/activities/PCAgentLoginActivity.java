package com.pesachoice.agent.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import org.springframework.util.StringUtils;
import com.pesachoice.billpay.activities.PCLoginActivity;
import com.pesachoice.billpay.activities.PCVerifyPhoneActivity;
import com.pesachoice.billpay.activities.helpers.DismissKeyboardListener;
import com.pesachoice.billpay.business.PCPesabusClient;
import com.pesachoice.billpay.business.PCPesachoiceConstant;
import com.pesachoice.billpay.model.PCSpecialUser;
import com.pesachoice.billpay.model.PCUser;

/**
 * Created by emmy on 12/03/2018.
 */

public class PCAgentLoginActivity extends PCLoginActivity {

    private PCPesabusClient.PCServiceType currentServiceType;
    private  final static String CLAZZ = PCAgentLoginActivity .class.getName();
    @Override
    protected  void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pcagent_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // NOTE: We need to call initialize view again because we are using a different content view
        // and so the previous references are lost when we change the content view.
        initializeView();
        selectedCountry = countryCodeMap.get(this.getUserCountryCode(this));
        if (!StringUtils.isEmpty(selectedCountry)) {
            countryPicker1.setText(selectedCountry.substring(selectedCountry.indexOf("+")));
        } else {
            showCountriesCode(this);
        }
        countryPicker1.setOnClickListener(this);
        PCUser user = new PCUser();
        user = getLogedInUser();
        String userName = user.getEmail();
        userNameText.setText(userName);
        configureLoginTypeSelector();
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            isFailedAuth = intent.getExtras().getString("PCCHECOUT");
        }
        // Listen for keyboard dismissal
        findViewById(R.id.login_container).setOnClickListener(new DismissKeyboardListener(this));
    }

    @Override
    protected void initializeView() {
        countryPicker1 = (TextView) findViewById(R.id.countryPicker1);
        loginWithPhoneNumberLayout = (LinearLayout) findViewById(R.id.loging_with_phone_number_layout);
        loginTypes = (Spinner) findViewById(R.id.login_types);
        passwordText = (EditText) findViewById(R.id.password);
        phoneEditText = (EditText) findViewById(R.id.phone);
        userNameText = (EditText) findViewById(R.id.email);
    }


    @Override
    public void showMainController(final PCSpecialUser user) {
        Log.d(CLAZZ, "Open Main Activity");
        if (user == null) {
            return;
        }
        if (isFailedAuth == null) {
            this.saveUserDetails(user, true);
            if (user.isRegistrationComplete() || user.isAgent()) {
                Intent intent = createMainTabActivityIntent(user);
                startActivity(intent);
            } else {
                //cache user details
                Intent intent = new Intent(this, PCAgentVerifyPhoneActivity.class);
                intent.putExtra(PCPesachoiceConstant.USER_INTENT_EXTRA, user);
                startActivity(intent);
                finish();
            }
        } else {
            Intent returnIntent = new Intent();
            this.saveUserDetails(user, true);
            returnIntent.putExtra(PCPesachoiceConstant.USER_INTENT_EXTRA, user);
            setResult(AppCompatActivity.RESULT_OK, returnIntent);
            finish();
        }
    }



    @Override
    protected Intent createMainTabActivityIntent(PCSpecialUser user) {
        Intent intent = new Intent(this, PCAgentMainActivity.class);
        intent.putExtra(PCPesachoiceConstant.USER_INTENT_EXTRA, user);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }


}
