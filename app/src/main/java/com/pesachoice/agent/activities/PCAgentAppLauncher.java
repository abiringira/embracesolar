package com.pesachoice.agent.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.pesachoice.billpay.activities.PCAppLauncher;
import com.pesachoice.billpay.activities.PCSignupActivity;
import com.pesachoice.billpay.activities.PCVerifyPhoneActivity;
import com.pesachoice.billpay.business.PCPesachoiceConstant;

/**
 * Created by emmy on 12/03/2018.
 */

public class PCAgentAppLauncher extends PCAppLauncher {

    private final static String CLAZZ = PCAgentAppLauncher.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pcagent_signup_login);
    }

    @Override
    public void openLogin(View view) {
        Log.d(CLAZZ, "Open login Activity");
        Intent intent = new Intent(this, PCAgentLoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void openSignUp(View view) {
        Log.d(clazzz, "Open Sign Up Activity");
        Intent intent = new Intent(this, PCSignupActivity.class);
        startActivityForResult(intent, SIGNUP_ACTIVITY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGNUP_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Intent intent = new Intent(this, PCAgentVerifyPhoneActivity.class);
                intent.putExtra(PCPesachoiceConstant.USER_INTENT_EXTRA, data);
                startActivityForResult(intent, 3);
            }
        }
    }

}
