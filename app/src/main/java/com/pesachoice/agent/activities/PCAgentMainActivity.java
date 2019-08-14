package com.pesachoice.agent.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.pesachoice.agent.fragments.PCAgentActivitiesFragment;
import com.pesachoice.billpay.activities.PCMainTabActivity;
import com.pesachoice.billpay.activities.PCVerifyPhoneActivity;
import com.pesachoice.billpay.business.PCPesabusClient;
import com.pesachoice.billpay.business.PCPesachoiceConstant;
import com.pesachoice.billpay.business.service.PCControllerFactory;
import com.pesachoice.billpay.business.service.PCUserController;
import com.pesachoice.billpay.fragments.PCMainTabsFragment;
import com.pesachoice.billpay.fragments.PCMoneyTransferTransactionFragment;
import com.pesachoice.billpay.fragments.PCProductsFragment;
import com.pesachoice.billpay.fragments.PCSupportCallingFragment;
import com.pesachoice.billpay.fragments.PCSupportRepeatTransFragment;
import com.pesachoice.billpay.fragments.PCUserAccountFragment;
import com.pesachoice.billpay.fragments.PCUserActivitiesFragment;
import com.pesachoice.billpay.fragments.PCViewPagerAdapter;
import com.pesachoice.billpay.fragments.PConRequiredDataLoaded;
import com.pesachoice.billpay.model.PCActivity;
import com.pesachoice.billpay.model.PCData;
import com.pesachoice.billpay.model.PCGenericError;
import com.pesachoice.billpay.model.PCMoneyTransferPaymentData;
import com.pesachoice.billpay.model.PCRequest;
import com.pesachoice.billpay.model.PCSpecialUser;
import com.pesachoice.billpay.model.PCUser;
import com.pesachoice.billpay.utils.PCPushNotificationSupport;
import com.pubnub.api.PubNub;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class PCAgentMainActivity extends PCMainTabActivity {
    private final static String CLAZZZ = PCAgentMainActivity.class.getName();
    public  PCViewPagerAdapter adapter;
    private PConRequiredDataLoaded transFrag;
    private PConRequiredDataLoaded supportedCountryAndProductFragment;
    private boolean gettingProfileForRepeatTranscation = false;
    private Bundle savedInstanceState;
    public PCUserAccountFragment pcUserAccountFragment;
    private PCSupportRepeatTransFragment repeatTransactionFragment = null;
    private List<PCUser> pcAllReceivers = null;
    private PCProductsFragment productFragment;

    @Override
    public void userAuntheticated () {
        if (appUser != null) {
            boolean autoLogedIn = this.getIntent().getExtras().getBoolean(PCPesachoiceConstant.USER_AUTO_LOGGED_IN, false);

            if (autoLogedIn) {
                try {
                    final PCRequest isAuthenticatedRequest = new PCRequest();
                    isAuthenticatedRequest.setUser(appUser);
                    isAuthenticatedRequest.setDetailed(true);
                    isAuthenticatedRequest.setSpecial(true);
                    this.currentServiceTypeAuth = PCPesabusClient.PCServiceType.IS_AUTHENTICATED;
                    PCUserController userController = (PCUserController)
                            PCControllerFactory.constructController(
                                    PCControllerFactory.PCControllerType.USER_CONTROLLER, this);
                    if (userController != null) {
                        userController.setActivity(this);
                        userController.setServiceType(PCPesabusClient.PCServiceType.IS_AUTHENTICATED);
                        userController.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, isAuthenticatedRequest);
                    }
                } catch (Throwable exc) {
                    Log.e(CLAZZZ, "Could not handle Authentication process because [" + exc.getMessage() + "]");
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    PCGenericError error = new PCGenericError();
                    if(exc instanceof PCGenericError) {
                        error = (PCGenericError)exc;
                    }
                    else
                        error.setMessage(exc.getMessage());
                    this.presentError(error, "Error While Authenticating User");
                }
            } else {
                generateCallingDetails();
            }
            //start notification module
            PubNub pubNub = PCPushNotificationSupport.initialize(this);
            pubNub.addListener(getPubNubListener());
        }
    }




    @Override
    public void setupViewPager(ViewPager viewPager) {
        adapter = new PCViewPagerAdapter(getSupportFragmentManager());;
        supportedCountryAndProductFragment = new PCProductsFragment();
        //PCMoneyTransferPaymentData transactionData = new PCMoneyTransferPaymentData();
        //transactionData.setPaymentProcessingType(PCActivity.PCPaymentProcessingType.MONEY_TRANSFER);
        //((PCMoneyTransferTransactionFragment) supportedCountryAndProductFragment).setTransactionData(transactionData);
        //((PCMoneyTransferTransactionFragment) supportedCountryAndProductFragment).setServiceType(PCPesabusClient.PCServiceType.MONEY_TRANSFER);
        if (savedInstanceState == null) {
            adapter.addFragment((PCMainTabsFragment) supportedCountryAndProductFragment, "Services");
            adapter.addFragment(new PCAgentActivitiesFragment(), "Activity");
            pcUserAccountFragment = new PCUserAccountFragment();
            adapter.addFragment(pcUserAccountFragment, "Account");
            appUser = getLogedInUser();
            if (appUser != null && appUser.isCustomerRep()) {
                adapter.addFragment(new PCSupportCallingFragment(), "Support");
            }

        } else {
            Integer count = savedInstanceState.getInt("tabsCount");
            String[] titles = savedInstanceState.getStringArray("titles");
            for (int i = 0; i < count; i++) {
                adapter.addFragment(getFragment(i), titles[i]);
            }
        }
        viewPager.setAdapter(adapter);
    }

    private Fragment getFragment(int position) {
        return savedInstanceState == null ? adapter.getItem(position) : getSupportFragmentManager().findFragmentByTag(getFragmentTag(position));
    }

    private String getFragmentTag(int position) {
        return "android:switcher:" + R.id.viewpager + ":" + position;
    }


    @Override
    public void straightLogout() {
        Intent intent = new Intent(this, PCAgentAppLauncher.class);
        PCSpecialUser user = getLogedInUser();
        user.setTokenId("");
        this.saveUserDetails(user, false);
        startActivity(intent);
        finish();
    }

    @Override
    protected void showHelpActivity() {
        Intent intent = new Intent(this, PCAgentAccountDetailsActivity.class);
        intent.putExtra(PCPesabusClient.OPTION, PCPesabusClient.OPTION_HELP);
        startActivity(intent);
    }


    @Override
    public void onTaskCompleted(Queue<? extends PCData> data) {
        try {
            PCAgentActivitiesFragment activitiesFragment = (PCAgentActivitiesFragment) adapter.getItem(1);
            if (data != null && data.size() > 0) {
                Object objTest = data.peek();
                if (currentServiceType == PCPesabusClient.PCServiceType.GET_ALL_RECEIVERS && objTest instanceof PCUser) {
                    if (data != null) {
                        Log.d("Receivers count ", "" + data.size());
                        pcAllReceivers = new ArrayList<>((Queue<PCUser>) data);

                    }
                } else {
                    activitiesFragment.onCollectionDataLoadFinished(data);

                }
            } else if (data != null && data.size() == 0) {
                activitiesFragment.setEmptyView();
            }
        } catch (Throwable error) {
            if (error.getMessage() != null && error.getMessage().length() > 0) {
                Log.d(CLAZZZ, error.getMessage());
            }
        }
    }

}
