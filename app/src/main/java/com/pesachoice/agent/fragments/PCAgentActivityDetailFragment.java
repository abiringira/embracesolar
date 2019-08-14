package com.pesachoice.agent.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.pesachoice.agent.activities.PCAgentMainActivity;
import com.pesachoice.billpay.fragments.PCActivityDetailFragment;
import com.pesachoice.billpay.model.PCAgentWalletTransaction;
import com.pesachoice.billpay.model.PCCurrency;
import com.pesachoice.billpay.model.PCElectricityServiceInfo;
import com.pesachoice.billpay.model.PCGenericError;
import com.pesachoice.billpay.model.PCOperatorRequest;
import com.pesachoice.billpay.model.PCProductServiceInfo;
import com.pesachoice.billpay.model.PCServiceInfo;
import com.pesachoice.billpay.model.PCSpecialUser;
import com.pesachoice.billpay.model.PCTicketServiceInfo;
import com.pesachoice.billpay.model.PCTransaction;
import com.pesachoice.billpay.model.PCTransactionStatus;
import com.pesachoice.billpay.utils.PCTicketsUtils;

import org.springframework.util.StringUtils;

import java.text.DecimalFormat;

/**
 * Created by emmy on 07/05/2018.
 */

public class PCAgentActivityDetailFragment extends PCAgentSupportRepeatTransFragment {
    private View rootView;
    private TextView receiverName;
    private TextView transactionStatusTitle;
    private TextView accountNumber;
    private TextView accountNumberTitle;
    private TextView billAmount;
    private TextView transactionFee;
    private TextView totalActivityAmount;
    private TextView localAmout;
    private TextView localAmountTitle;
    private TextView paymentCard;
    private ImageView imageViewQR;
    private TextView transactionStatus;
    private TextView transactionType;
    private TextView transactionTypeTitle;
    private TextView eventNbr;
    private TextView voucherNumberText;
    private TextView voucherNumber;
    private PCActivityDetailFragment activityDetailFragment = null;
    private static final String CLAZZZ = PCActivityDetailFragment.class.getName();
    private PCProductServiceInfo serviceInfo;
    private String amountText;
    private String feesText;
    private String totalAmountText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this pcOnPhoneContactLoad

        rootView = inflater.inflate(com.pesachoice.billpay.activities.R.layout.pc_activity_detail_fragment, container, false);
        agentMainActivity = (PCAgentMainActivity) getActivity();
        repeatTransFragment = this;
        //this is introduce to better handle scanning of events QR
        if (getTransaction().getServiceInfo() instanceof PCTicketServiceInfo) {
            rootView = inflater.inflate(com.pesachoice.billpay.activities.R.layout.pc_activity_ticket_detail_fragment, container, false);
            setupTicketLayout();
        } else {
            setupLayout();
            //do checkout
            doCheckOut();
        }


        return rootView;
    }

    private void doCheckOut() {
        if (pcServiceInfo != null && pcServiceInfo.getTransactionStatus() == PCTransactionStatus.IN_PROGRESS) {
            repeatTranscation.setVisibility(View.GONE);
        } else {
            repeatTranscation.setVisibility(View.VISIBLE);
            //prepare rootview layout
            prepareCheckout();
            repeatTranscation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    initiateCheckout();
                }
            });
        }
    }

    private void setupTicketLayout() {
        receiverName = (TextView) rootView.findViewById(com.pesachoice.billpay.activities.R.id.transaction_customer_name);
        TextView event_name = (TextView) rootView.findViewById(com.pesachoice.billpay.activities.R.id.event_name);
        transactionStatus = (TextView) rootView.findViewById(com.pesachoice.billpay.activities.R.id.transaction_status);
        imageViewQR = (ImageView) rootView.findViewById(com.pesachoice.billpay.activities.R.id.imageViewQR);
        accountNumber = (TextView) rootView.findViewById(com.pesachoice.billpay.activities.R.id.account_number);
        billAmount = (TextView) rootView.findViewById(com.pesachoice.billpay.activities.R.id.bill_amount);
        transactionFee = (TextView) rootView.findViewById(com.pesachoice.billpay.activities.R.id.transaction_fee);
        totalActivityAmount = (TextView) rootView.findViewById(com.pesachoice.billpay.activities.R.id.total_activity_amount);
        localAmout = (TextView) rootView.findViewById(com.pesachoice.billpay.activities.R.id.local_amount);
        accountNumberTitle = (TextView) rootView.findViewById(com.pesachoice.billpay.activities.R.id.account_number_title);
        transactionStatusTitle = (TextView) rootView.findViewById(com.pesachoice.billpay.activities.R.id.transaction_status_title);
        repeatTranscation = (Button) rootView.findViewById(com.pesachoice.billpay.activities.R.id.repeat_transcation);
        eventNbr = (TextView) rootView.findViewById(com.pesachoice.billpay.activities.R.id.event_nbr);
        localAmountTitle = (TextView) rootView.findViewById(com.pesachoice.billpay.activities.R.id.local_amount_title);
        voucherNumber = (TextView) rootView.findViewById(com.pesachoice.billpay.activities.R.id.voucher_number);
        voucherNumberText = (TextView) rootView.findViewById(com.pesachoice.billpay.activities.R.id.voucher_number_text);
        event_name.setText(((PCTicketServiceInfo) getTransaction().getServiceInfo()).getName());

        /**
         * we comment this below setting image code for tests purpose as we changed the getTicketQRInformation into boolean
         */
        imageViewQR.setImageBitmap(PCTicketsUtils.getTicketQRInformation(getTransaction()));

        if (eventNbr != null) {
            eventNbr.setText("Ticket #: " + getTransaction().getTransactionId());
        }
        //set customer name
        populateReceiverName();
        //set payment details
        populatePaymentDetails(this.getTransaction());
    }


    /**
     * used to set transaction details text in corresponding textviews
     */
    private void populateView() {
        try {
            String serviceType = "";
            PCAgentWalletTransaction pcTransaction = this.getTransaction();
            populate_transaction_status();
            populateReceiverName();
            Log.e("wALLETTTT", "bEFORE vieee tEST");
            populatePaymentDetails(pcTransaction);
            Log.e("wALLETTTT", "bafter Viewww PCTransaction tEST");
            final PCOperatorRequest operatorRequest = agentMainActivity.getPcOperatorRequest(agentMainActivity.getCountryProfile().getCountry());
            if (operatorRequest.getCompanyId().equalsIgnoreCase("embracesolar")) {
                serviceType = agentMainActivity.getTranscationType("agentServiceInfo");
            } else {
                serviceType = agentMainActivity.getTranscationType(pcTransaction.getServiceType());
            }

            if (serviceType != null) {

                transactionType.setText(serviceType);
                //show voucher code if service type is electricity
                showVoucherIfTransIsElectricity(pcTransaction, serviceType);

            } else {
                this.transactionType.setVisibility(View.GONE);
                this.transactionTypeTitle.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            String errorMessage = "Error while showing transaction details";
            Log.e(CLAZZZ, errorMessage + "[" + e.getMessage() + "]");
            PCGenericError error = new PCGenericError();
            error.setMessage(errorMessage);
            this.presentError(error, "Error", CLAZZZ);
        }
    }

    private void showVoucherIfTransIsElectricity(PCTransaction pcTransaction, String serviceType) {
        if ("ELECTRICITY".equalsIgnoreCase(serviceType) && pcTransaction.getServiceInfo() != null) {
            String voucherSerialCode = ((PCElectricityServiceInfo) pcTransaction.getServiceInfo()).getMeterSerialCode();

            if (!StringUtils.isEmpty(voucherSerialCode)) {
                voucherNumber.setVisibility(View.VISIBLE);
                voucherNumberText.setVisibility(View.VISIBLE);
                voucherNumber.setText(voucherSerialCode);
            }
        }
    }

    private void populatePaymentDetails(PCAgentWalletTransaction pcTransaction) {
        DecimalFormat formatter = new DecimalFormat("###,###,###.##");
        Double transactionAmount = 0.0;
        Double fee = 0.0;
        Double totalTransactionAmount = 0.0;
        Log.e("wALLETTTT", "bEFORE PCTransaction tEST");
        if (pcTransaction != null) {
            /*if (pcTransaction instanceof PCAgentWalletTransaction) {
                PCAgentWalletTransaction agentWalletTransaction = (PCAgentWalletTransaction) pcTransaction;
                serviceInfo = agentWalletTransaction.getAgentServiceInfo();
                //serviceType = PCPesabusClient.PCServiceType.AGENT_TRANSACTION;
                Log.e("wALLETTTT","aFTER tEST");
            } else {
                serviceInfo = pcTransaction.getServiceInfo();

            }*/
            Log.e("Middle t", "Middle tEST");
            PCSpecialUser sender = pcTransaction.getSender();
            final PCOperatorRequest pcOperatorRequest = agentMainActivity.getPcOperatorRequest(agentMainActivity.getCountryProfile().getCountry());
            if (pcOperatorRequest.getCompanyId().equalsIgnoreCase("embracesolar")) {
                serviceInfo = pcTransaction.getAgentServiceInfo();
                if (serviceInfo != null) {
                    transactionAmount = agentMainActivity.isCountryUS() ? serviceInfo.getUsd() : serviceInfo.getAmount();
                    fee = serviceInfo.getFees();
                    totalTransactionAmount = agentMainActivity.isCountryUS() ? serviceInfo.getTotalUsd() : serviceInfo.getTotalAmount();
                    String currencySymbol = "";
                    PCCurrency currency = pcTransaction.getBaseCurrency();
                    if (currency == null) {
                        currency = sender.getBaseCurrency();
                    }
                    if (currency != null) {
                        currencySymbol = currency.getCurrencySymbol();

                        if (totalTransactionAmount == null || totalTransactionAmount == 0) {
                            totalTransactionAmount = currency.getTotalAmount();
                        }

                        if (transactionAmount == null || transactionAmount == 0) {
                            transactionAmount = currency.getAmount();
                        }

                        if ((fee == null || fee == 0) && (totalTransactionAmount >= transactionAmount)) {

                            fee = totalTransactionAmount - transactionAmount;
                        }
                    } else {
                        currencySymbol = "RWF";
                    }

                    if ("Rwf".equalsIgnoreCase(currencySymbol)) {
                        amountText = currencySymbol + formatter.format(transactionAmount);
                        feesText = currencySymbol + formatter.format(fee);
                        totalAmountText = currencySymbol + formatter.format(totalTransactionAmount);
                    }
                    billAmount.setText(amountText);
                    transactionFee.setText(feesText);
                    totalActivityAmount.setText(totalAmountText);
                }

            } else {
                serviceInfo = pcTransaction.getAgentServiceInfo();
                if (serviceInfo != null) {
                    transactionAmount = agentMainActivity.isCountryUS() ? serviceInfo.getUsd() : serviceInfo.getAmount();
                    fee = serviceInfo.getFees();
                    totalTransactionAmount = agentMainActivity.isCountryUS() ? serviceInfo.getTotalUsd() : serviceInfo.getTotalAmount();
                    String currencySymbol = "";
                    PCCurrency currency = pcTransaction.getBaseCurrency();
                    if (currency == null) {
                        currency = sender.getBaseCurrency();
                    }
                    if (currency != null) {
                        currencySymbol = currency.getCurrencySymbol();

                        if (totalTransactionAmount == null || totalTransactionAmount == 0) {
                            totalTransactionAmount = currency.getTotalAmount();
                        }

                        if (transactionAmount == null || transactionAmount == 0) {
                            transactionAmount = currency.getAmount();
                        }

                        if ((fee == null || fee == 0) && (totalTransactionAmount >= transactionAmount)) {

                            fee = totalTransactionAmount - transactionAmount;
                        }
                    } else {
                        currencySymbol = "$";
                    }

                    if ("$".equalsIgnoreCase(currencySymbol)) {
                        amountText = currencySymbol + formatter.format(transactionAmount);
                        feesText = currencySymbol + formatter.format(fee);
                        totalAmountText = currencySymbol + formatter.format(totalTransactionAmount);
                    }
                    billAmount.setText(amountText);
                    transactionFee.setText(feesText);
                    totalActivityAmount.setText(totalAmountText);
                }

            }

            Log.e("wALLETTTT", "After tEST");


            String receiverCountry = serviceInfo.getReceiverCountry();
            Double localCurrencyVal = serviceInfo.getTotalLocalCurrency();

            if (!StringUtils.isEmpty(receiverCountry) && localCurrencyVal > 0) {
                String receiverCurrencySymbol = PCServiceInfo.getCurrencySymbol(receiverCountry);
                String local = formatter.format(localCurrencyVal) + " " + receiverCurrencySymbol;
                localAmout.setText(local);
            } else {
                localAmout.setVisibility(View.GONE);
                localAmountTitle.setVisibility(View.GONE);
            }

            if (StringUtils.isEmpty(serviceInfo.getAccountNumber())) {
                this.accountNumber.setVisibility(View.GONE);
                this.accountNumberTitle.setVisibility(View.GONE);
            } else {
                this.accountNumber.setText(serviceInfo.getAccountNumber());
            }
        }
    }


    /**
     * Populate receiver name method changed from void to boolean for tests purpose.
     *
     * @return the boolean
     */
    public boolean populateReceiverName() {
        boolean validation = false;
        PCAgentWalletTransaction pcTransaction = this.getTransaction();
        String receiverName = "";
        if (pcTransaction != null) {
            PCSpecialUser pcReceiver = pcTransaction.getReceiver();

            if (pcReceiver != null) {

                if (pcReceiver.getFullName() != null) {
                    receiverName = StringUtils.capitalize(StringUtils.replace(pcReceiver.getFullName(), "null", ""));

                    if (StringUtils.isEmpty(receiverName.trim())) {
                        receiverName = pcReceiver.getPhoneNumber();
                    }
                }

                this.receiverName.setText(receiverName);
                validation = true;
            }
        }

        return validation;
    }


    private void populate_transaction_status() {
        if (pcServiceInfo != null && pcServiceInfo.getTransactionStatus() == PCTransactionStatus.IN_PROGRESS) {
            transactionStatus.setText("In Progress");
            transactionStatus.setTextColor(Color.parseColor("#FF0000"));
        } else {
            transactionStatus.setText("Completed");
        }
    }

    /**
     * used to prepare the layout
     */
    private void setupLayout() {
        receiverName = (TextView) rootView.findViewById(com.pesachoice.billpay.activities.R.id.receivername);
        transactionStatus = (TextView) rootView.findViewById(com.pesachoice.billpay.activities.R.id.transaction_status);
        accountNumber = (TextView) rootView.findViewById(com.pesachoice.billpay.activities.R.id.account_number);
        accountNumberTitle = (TextView) rootView.findViewById(com.pesachoice.billpay.activities.R.id.account_number_title);
        transactionStatusTitle = (TextView) rootView.findViewById(com.pesachoice.billpay.activities.R.id.transaction_status_title);
        billAmount = (TextView) rootView.findViewById(com.pesachoice.billpay.activities.R.id.bill_amount);
        transactionFee = (TextView) rootView.findViewById(com.pesachoice.billpay.activities.R.id.transaction_fee);
        totalActivityAmount = (TextView) rootView.findViewById(com.pesachoice.billpay.activities.R.id.total_activity_amount);
        localAmout = (TextView) rootView.findViewById(com.pesachoice.billpay.activities.R.id.local_amount);
        localAmountTitle = (TextView) rootView.findViewById(com.pesachoice.billpay.activities.R.id.local_amount_title);
        transactionType = (TextView) rootView.findViewById(com.pesachoice.billpay.activities.R.id.transaction_type);
        transactionTypeTitle = (TextView) rootView.findViewById(com.pesachoice.billpay.activities.R.id.transaction_type_tile);
        repeatTranscation = (Button) rootView.findViewById(com.pesachoice.billpay.activities.R.id.repeat_transcation);

        populateView();
    }


}
