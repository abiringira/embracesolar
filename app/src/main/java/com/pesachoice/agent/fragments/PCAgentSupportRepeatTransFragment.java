package com.pesachoice.agent.fragments;

import android.app.ProgressDialog;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.pesachoice.agent.activities.PCAgentMainActivity;
import com.pesachoice.billpay.fragments.PCAgentCheckoutFragment;
import com.pesachoice.billpay.fragments.PCSupportRepeatTransFragment;
import com.pesachoice.billpay.fragments.PCTransactionFragmentFactory;
import com.pesachoice.billpay.model.PCActivity;
import com.pesachoice.billpay.model.PCAgentWalletTransaction;
import com.pesachoice.billpay.model.PCCurrency;
import com.pesachoice.billpay.model.PCOperatorRequest;
import com.pesachoice.billpay.model.PCProductServiceInfo;

import org.springframework.util.StringUtils;

import java.util.List;
/**
 * Created by emmy on 07/05/2018.
 */
public class PCAgentSupportRepeatTransFragment extends PCSupportRepeatTransFragment {
    protected PCAgentMainActivity agentMainActivity;
    protected PCAgentSupportRepeatTransFragment repeatTransFragment;
    private PCAgentWalletTransaction transaction = new PCAgentWalletTransaction();
    private PCProductServiceInfo serviceInfo;
    private PCAgentCheckoutFragment frag = null;


    @Override
    public PCAgentWalletTransaction getTransaction() {
        return transaction;
    }
    public void setTransaction(PCAgentWalletTransaction transaction) {
        this.transaction = transaction;
    }

    @Override
    protected void checkOut() {
        FragmentManager fm = agentMainActivity.getSupportFragmentManager();
        if (agentMainActivity instanceof PCAgentMainActivity) {
            PCOperatorRequest op = agentMainActivity.getPcOperatorRequest("Rwanda");
            if (op != null && op.getCompanyId().equalsIgnoreCase("embracesolar")) {
                frag.amRepeatingTrans = true;
                fm.beginTransaction()
                        .replace(com.pesachoice.billpay.activities.R.id.grid_container, frag, Integer.toString(com.pesachoice.billpay.activities.R.layout.pc_fragment_agent_checkout))
                        .addToBackStack(null)
                        .commit();
                //to scoll back to tab one
            } else {
                frag.amRepeatingTrans = true;
                fm.beginTransaction()
                        .replace(com.pesachoice.billpay.activities.R.id.grid_container_country, frag, Integer.toString(com.pesachoice.billpay.activities.R.layout.pc_fragment_checkout))
                        .addToBackStack(null)
                        .commit();
            }
        }
        agentMainActivity.movePrevious(1);
        if (repeatTransFragment instanceof PCAgentActivityDetailFragment) {
            //close the open fragment i.e. activity opened to go back to the list of activities
            agentMainActivity.getSupportFragmentManager().beginTransaction().remove(this).commit();
        }
    }

    @Override
    public boolean initiateCheckout() {
        boolean repeatResult = false;
        //String country = transaction.getServiceInfo().getReceiverCountry();
        String country = "";
        if (agentMainActivity != null) {
            /**
             * use current country profile if we got the profile of the same transaction
             */
            final PCOperatorRequest operatorRequest = agentMainActivity.getPcOperatorRequest(agentMainActivity.getCountryProfile().getCountry());
            if (operatorRequest.getCompanyId().equalsIgnoreCase("embracesolar")) {
                country = transaction.getAgentServiceInfo().getReceiverCountry();
            } else {
                country = transaction.getAgentServiceInfo().getReceiverCountry();
            }
           /* if (PCPesabusClient.PCServiceType.CALLING_REFIL == serviceType || (agentMainActivity.getCountryProfile() != null && country.equalsIgnoreCase(agentMainActivity.getCountryProfile().getCountry()))) {
                checkOut();
                repeatResult = true;
            } */
            if (!StringUtils.isEmpty(country)) {
                progressDialog = ProgressDialog.show(agentMainActivity, "Repeat Transaction", "loading...", true);
                //open checkout to repeat the same transaction
                agentMainActivity.makeCallToGetCountryProfile(country, null, repeatTransFragment);
                checkOut();
                repeatResult = true;
            }
        }
        return repeatResult;
    }


    @Override
    protected void prepareCheckout() {
        try {
            agentMainActivity = (PCAgentMainActivity) getActivity();
            if (agentMainActivity != null && transaction != null) {
                final PCOperatorRequest operatorRequest = agentMainActivity.getPcOperatorRequest(agentMainActivity.getCountryProfile().getCountry());
                if (operatorRequest.getCompanyId().equalsIgnoreCase("embracesolar")) {
                    frag = new PCAgentCheckoutFragment();
                    serviceType = PCTransactionFragmentFactory.getServiceType("agentServiceInfo");
                    transaction.setSender(agentMainActivity.getAppUser());
                    transaction.setActionType(transaction.getActionType());
                    frag.setServiceType(serviceType);
                    pcBillPaymentData = PCTransactionFragmentFactory.getBillPaymentData("agentServiceInfo");
                    pcBillPaymentData.setPaymentProcessingType(PCActivity.PCPaymentProcessingType.getProcessingType("agentServiceInfo"));
                    pcBillPaymentData.setServiceInfo(transaction.getAgentServiceInfo());
                    serviceInfo = (PCProductServiceInfo) pcBillPaymentData.getServiceInfo();
                    receiver = transaction.getReceiver();
                    if (serviceInfo != null) {
                        List<PCCurrency> currency = agentMainActivity.getCountryProfile().getCurrencies();
                        for (PCCurrency listCurr : currency) {
                            if (listCurr.getCountry().equalsIgnoreCase("Rwanda")) {
                                serviceInfo.setAmount(listCurr.getAmount());
                                serviceInfo.setTotalAmount(listCurr.getTotalAmount());

                                if (receiver != null)
                                    receiver.setCountry(serviceInfo.getReceiverCountry());

                                break;
                            }
                        }
                    }

                } /*else {
                    frag = new PCCheckoutFragment();
                    serviceType = PCTransactionFragmentFactory.getServiceType(transaction.getServiceType());
                    transaction.setSender(agentMainActivity.getAppUser());
                    transaction.setActionType(transaction.getActionType());
                    frag.setServiceType(serviceType);
                    pcBillPaymentData = PCTransactionFragmentFactory.getBillPaymentData(transaction.getServiceType());
                    pcBillPaymentData.setPaymentProcessingType(PCActivity.PCPaymentProcessingType.getProcessingType(transaction.getServiceType()));
                    pcBillPaymentData.setServiceInfo(transaction.getServiceInfo());
                    serviceInfo = (PCProductServiceInfo) pcBillPaymentData.retrieveServiceInfo();
                    receiver = transaction.getReceiver();
                    if (transaction.getBaseCurrency() != null && serviceInfo != null) {
                        PCCurrency currency = transaction.getBaseCurrency();
                        if (currency.getTotalAmount() > 0 || currency.getAmount() > 0) {
                            serviceInfo.setAmount(currency.getAmount());
                            serviceInfo.setTotalAmount(currency.getTotalAmount());
                        }
                        if (receiver != null)
                            receiver.setCountry(serviceInfo.getReceiverCountry());
                    }

                }*/

                if (agentMainActivity.getApiKey() != null) {
                    transaction.setId(agentMainActivity.getApiKey().getId());
                }
                //TODO: need to fix this bug and also as
                // k Odilon to modify the serviceType String
                // NOTE: We need to set the amount and totalAmount when a transaction was done especially in a different currency.
                pcBillPaymentData.setReceiver(receiver);
                pcBillPaymentData.setSender(((PCAgentMainActivity) this.getActivity()).getAppUser());
                pcBillPaymentData.setOperator(this.getTransaction().getOperator());
                frag.setTransactionData(pcBillPaymentData);
                this.pcServiceInfo = serviceInfo;
                frag.setServiceInfo(this.pcServiceInfo);
            }
        } catch (Exception e) {
            String errorMessage = "Error while trying to repeat a transaction";
            presentError(e, errorMessage);
            e.printStackTrace();
        }
    }
}
