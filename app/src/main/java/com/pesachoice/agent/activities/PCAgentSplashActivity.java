package com.pesachoice.agent.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.pesachoice.billpay.activities.PCBaseActivity;
import com.pesachoice.billpay.activities.PCLoginActivity;
import com.pesachoice.billpay.activities.PCVerifyPhoneActivity;
import com.pesachoice.billpay.business.PCPesabusClient;
import com.pesachoice.billpay.business.PCPesachoiceConstant;
import com.pesachoice.billpay.business.service.PCControllerFactory;
import com.pesachoice.billpay.business.service.PCPingServerController;
import com.pesachoice.billpay.fragments.PCUpdateAppDialogFragment;
import com.pesachoice.billpay.model.PCData;
import com.pesachoice.billpay.model.PCGenericError;
import com.pesachoice.billpay.model.PCPingResults;
import com.pesachoice.billpay.model.PCRequest;
import com.pesachoice.billpay.model.PCUser;
import com.pesachoice.billpay.utils.PCPreferenceHelper;

import org.springframework.util.StringUtils;

import java.util.Queue;

/**
 * Created by emmy on 12/03/2018.
 */

public class PCAgentSplashActivity extends PCBaseActivity {
    private ImageView imageView;
    private ProgressDialog progressDialog;
    private final static String CLAZZZ = PCLoginActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pcagent_splash_activity);
        try {
            if (!isNetworkAvailable()) {
                presentError(new PCGenericError(), "Error while connecting to the internet");
                return;
            }

            //make service call to ping the server
            PCPingResults pingResults = new PCPingResults();
            currentServiceType = PCPesabusClient.PCServiceType.PING;
            PCPingServerController pingServerController = (PCPingServerController)
                    PCControllerFactory.constructController(
                            PCControllerFactory.PCControllerType.PING_SERVER, this);
            if (pingServerController != null) {
                pingServerController.setActivity(this);
                pingServerController.setServiceType(PCPesabusClient.PCServiceType.PING);
                pingServerController.execute(pingResults);
            }
        } catch (Throwable exc) {
            Log.e(CLAZZZ, "Could not handle Pinging pesabus process because [" + exc.getMessage() + "]");
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            PCGenericError error = new PCGenericError();
            if (exc instanceof PCGenericError) {
                error = (PCGenericError) exc;

            } else {
                error.setMessage(exc.getMessage());
            }
        }

    }

    public void startTheApp() {
        if (!PCPreferenceHelper.getBooleanFromPrefs(this, PCPesachoiceConstant.SHARED_PREF_USER_LOGGED_IN, false)) {
            Intent intent = new Intent(this, PCAgentAppLauncher.class);
            startActivity(intent);
            finish();
        } else if (!PCPreferenceHelper.getBooleanFromPrefs(this, PCPesachoiceConstant.SHARED_PREF_USER_VERIFIED_PHONE, false)) {
            PCUser user = getLogedInUser();
            Intent intent = new Intent(this, PCAgentVerifyPhoneActivity.class);
            intent.putExtra(PCPesachoiceConstant.USER_INTENT_EXTRA, user);
            startActivity(intent);
            finish();
        } else {
            PCUser user = getLogedInUser();
            onAuthUserAction(user);
            //TODO if token expired
        }
    }

    private int getCurrentAppVersionCode() {
        int versionCode = -1;
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionCode = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    @Override
    public void onTaskCompleted(Queue<? extends PCData> data) {

    }

    public void onAuthUserAction(PCUser user) {

        try {
            // Get input fields
            String email = user == null ? "" : user.getEmail();
            String tokenId = user == null ? "" : user.getTokenId();
            final PCRequest isAuthenticatedRequest = new PCRequest();
            this.validateUserNameToken(email, tokenId);
            this.showMainController(user);
        } catch (PCGenericError error) {
            this.presentError(error, this.getResources().getString(com.pesachoice.billpay.activities.R.string.login_error_title));
        }
    }

    @Override
    public void presentError(PCGenericError error, String title) {
        String message = error.getMessage();
        if (isFinishing() || message == null) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setTitle(title);

        builder.setPositiveButton(com.pesachoice.billpay.activities.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //finish the activity
                finish();
                Log.d(CLAZZZ, "Clicked on OK on Alert box");
            }
        });

        Dialog alert = builder.create();
        alert.show();
    }

    void validateUserNameToken(String email, String tokenNumber) throws PCGenericError {
        PCGenericError error;
        if (StringUtils.isEmpty(email)) {
            error = new PCGenericError();
            error.setMessage(this.getResources().getString(com.pesachoice.billpay.activities.R.string.login_missing_email));
            error.setField("email");
            throw error;
        }

        if (StringUtils.isEmpty(tokenNumber)) {
            error = new PCGenericError();
            error.setMessage(this.getResources().getString(com.pesachoice.billpay.activities.R.string.token_number_missing));
            error.setField("Token Number");
            throw error;
        }
    }

    @Override
    public void onTaskStarted() {
        imageView = (ImageView) findViewById(R.id.splash_image);
        if (this.currentServiceType == PCPesabusClient.PCServiceType.PING) {
            imageView.setVisibility(View.VISIBLE);
            progressDialog = ProgressDialog.show(this, "", "");
            progressDialog.getWindow().setBackgroundDrawable(null);
        } else {
            // progressDialog = ProgressDialog.show(this, "Authenticating user", "Please wait while Authenticating process is going on", true);
            imageView.setVisibility(View.VISIBLE);
            progressDialog = ProgressDialog.show(this, "", "");
            progressDialog.getWindow().setBackgroundDrawable(null);
        }
    }

    @Override
    public void onTaskCompleted(PCData data) {
        if (data != null && data instanceof PCUser && this.currentServiceType != PCPesabusClient.PCServiceType.PING) {
            final PCUser user = (PCUser) data;
            if (user.getErrorMessage() != null && !"".equals(user.getErrorMessage())) { // service call failed
                Log.e(CLAZZZ, "An error occured: [" + user.getErrorMessage() + "]");
                PCGenericError error = new PCGenericError();
                error.setMessage(user.getErrorMessage());
                this.presentError(error, this.getResources().getString(com.pesachoice.billpay.activities.R.string.login_error_title));
            } else { // service call succeeded
                this.showMainController(user);
            }
        } else if (data != null && this.currentServiceType == PCPesabusClient.PCServiceType.PING && data instanceof PCPingResults) {
            // ping the server result
            pingResults = (PCPingResults) data;
            if (pingResults.getErrorMessage() != null && !"".equals(pingResults.getErrorMessage()) && null == pingResults.getApiKey()) { // service call failed
                Log.e(CLAZZZ, "An error occured: [" + pingResults.getErrorMessage() + "]");
                PCGenericError error = new PCGenericError();
                error.setMessage(pingResults.getErrorMessage());
                this.presentError(error, "Error connecting to the server");
            } else {
                //check if user need to update the PESACHOICE
                String minVersion = pingResults.getMinVersion();
                int currentAppVersion = getCurrentAppVersionCode();
                if (!StringUtils.isEmpty(minVersion)) {
                    /*
                     * Check if current version is required to update
                     */
                    minVersion = minVersion.trim();
                    if (Integer.parseInt(minVersion) < currentAppVersion) {

                        PCUpdateAppDialogFragment dialogFragment = new PCUpdateAppDialogFragment();
                        dialogFragment.setAppVersionCode(minVersion);
                        FragmentManager fm = getSupportFragmentManager();
                        dialogFragment.show(fm, "update app dialog");
                    } else {
                        startTheApp();
                    }
                } else {
                    startTheApp();
                }

                //start by verifying if the user need to enter login details or if they can login automatically

            }
        } else {
            // Some error occurred and response data is missing
            Log.e(CLAZZZ, "Application level error");
            /*
             * TODO: missing implementation on what the user should see in this case
             */
            startTheApp();
        }
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    void showMainController(PCUser pcUser) {
        Intent intent = new Intent(this, PCAgentMainActivity.class);
        intent.putExtra(PCPesachoiceConstant.USER_INTENT_EXTRA, pcUser);
        intent.putExtra(PCPesachoiceConstant.USER_AUTO_LOGGED_IN, true);
        startActivity(intent);
        finish();
    }
}
