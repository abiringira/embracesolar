package com.pesachoice.agent.activities;

import android.content.Intent;
import android.util.Log;

import com.pesachoice.billpay.activities.PCAsyncListener;
import com.pesachoice.billpay.activities.PCMainTabActivity;
import com.pesachoice.billpay.activities.PCVerifyPhoneActivity;
import com.pesachoice.billpay.business.PCPesabusClient;
import com.pesachoice.billpay.business.PCPesachoiceConstant;
import com.pesachoice.billpay.model.PCData;
import com.pesachoice.billpay.model.PCGenericError;
import com.pesachoice.billpay.model.PCSpecialUser;
import com.pesachoice.billpay.utils.PCGeneralUtils;

/**
 * Created by emmy on 21/03/2018.
 */

public class PCAgentVerifyPhoneActivity extends PCVerifyPhoneActivity {

    private final static String CLAZZZ = PCVerifyPhoneActivity.class.getName();
    @Override
    public void onTaskCompleted(PCData data) {
        super.onTaskCompleted(data);
        if (currentServiceType == PCPesabusClient.PCServiceType.VERIFY_PHONE) {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }

            this.presentVerificationDialog(data);
        } else if (this.currentServiceType == PCPesabusClient.PCServiceType.COMPLETE_REGISTRATION) {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            if (data != null && data instanceof PCSpecialUser) {
                if (data.getErrorMessage() != null && !"".equals(data.getErrorMessage())) {
                    Log.e(CLAZZZ, "An error occured: [" + data.getErrorMessage() + "]");
                    PCGenericError error = new PCGenericError();
                    error.setMessage(data.getErrorMessage());
                    this.presentError(error, this.getResources().getString(com.pesachoice.billpay.activities.R.string.verify_phone_error_title));
                } else {
                    PCSpecialUser userResponse = (PCSpecialUser) data;
					/*
					 * start pcUser tracking feature
					 */
                    String userTrakingKey = PCGeneralUtils.createTrackingUserKey(userResponse, pingResults.getApiKey());
                    this.saveUserTrackingKey(userTrakingKey);
                    this.saveUserDetails(userResponse, true);
                    Intent intent = new Intent(this, PCAgentMainActivity.class);
                    intent.putExtra(PCPesachoiceConstant.USER_INTENT_EXTRA, userResponse);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        }
    }


}
