package com.pesachoice.agent.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pesachoice.agent.activities.PCAgentAccountDetailsActivity;
import com.pesachoice.agent.activities.PCAgentMainActivity;
import com.pesachoice.agent.activities.R;
import com.pesachoice.billpay.activities.PCMainTabActivity;
import com.pesachoice.billpay.business.PCPesabusClient;
import com.pesachoice.billpay.business.service.PCControllerFactory;
import com.pesachoice.billpay.business.service.PCUserActivityController;
import com.pesachoice.billpay.fragments.PCActivityDetailFragment;
import com.pesachoice.billpay.model.PCActivityRequest;
import com.pesachoice.billpay.model.PCAgent;
import com.pesachoice.billpay.model.PCAgentWalletTransaction;
import com.pesachoice.billpay.model.PCCurrency;
import com.pesachoice.billpay.model.PCData;
import com.pesachoice.billpay.model.PCProductServiceInfo;
import com.pesachoice.billpay.model.PCServiceInfo;
import com.pesachoice.billpay.model.PCSpecialUser;
import com.pesachoice.billpay.model.PCTransactionStatus;
import com.pesachoice.billpay.model.PCUser;
import org.springframework.util.StringUtils;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Queue;

/**
 * Created by emmy on 07/05/2018.
 */

public class PCAgentActivitiesFragment extends PCAgentSupportRepeatTransFragment {
    private PCAgentActivityAdapter adapter;
    private static final String clazz = PCAgentActivitiesFragment.class.getName();
    private TextView emptyView;
    private View rootView = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.pc_fragment_user_activities, container, false);
        repeatTransFragment = this;
        this.onDataLoadStart(rootView);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    private void makeRequest() {
        final PCActivityRequest activityRequest = new PCActivityRequest();
        final PCSpecialUser userData = ((PCAgentMainActivity) this.getActivity()).getAppUser();
        //PCCountryProfile countryProfile = ((PCMainTabActivity) this.getActivity()).getCountryProfile();
        //final PCOperatorRequest operatorRequest = ((PCMainTabActivity) this.getActivity()).getPcOperatorRequest(countryProfile.getCountry());
        // final PCRequest userRequest = new PCRequest();
        if (!userData.isRegistrationComplete()) {
            PCAgent agentUser = new PCAgent();
            agentUser.setEmail(userData.getEmail());
            agentUser.setTokenId(userData.getTokenId());
            activityRequest.setAgent(agentUser);
        } else {
            final PCUser user = ((PCAgentMainActivity) this.getActivity()).getAppUser();
            activityRequest.setUser(user);
            activityRequest.setDetailed(true);
        }
        PCUserActivityController userActivitiesController = (PCUserActivityController) PCControllerFactory.constructControllerManyType(PCControllerFactory.PCControllerType.USER_MANY_ITEM_CONTROLLER, (PCMainTabActivity) this.getActivity());
        userActivitiesController.setActivity((PCAgentMainActivity) this.getActivity());
        userActivitiesController.setServiceType(PCPesabusClient.PCServiceType.USER_ACTIVITIES);
        userActivitiesController.execute(activityRequest);
    }
    public void setAgentAdapter(Queue<PCAgentWalletTransaction> data) {

        if (data != null) {
            Log.d("COUNT", "" + data.size());
            List<PCAgentWalletTransaction> ary = new ArrayList<>(data);
            Log.d(clazz, (ary.get(0) != null ? ary.get(0).getCreatedTime() : "") + "");
            initializeAgentAdapter(ary);
        }
    }



    public void setEmptyView() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        emptyView = new TextView(getContext());
        emptyView.setLayoutParams(
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));


        emptyView.setText("You have no transaction history. Make your first transaction Today!");
        emptyView.setGravity(Gravity.CENTER_HORIZONTAL);
        emptyView.setPadding(20, 60, 20, 20);
        emptyView.setTextSize(18);
        emptyView.setVisibility(View.GONE);
        emptyView.setBackgroundColor(getResources().getColor(R.color.sectionBackground));
        emptyView.setTextColor(getResources().getColor(R.color.colorPrimary));

        ((ViewGroup) mainFragmentLayout.getParent()).addView(emptyView);
        mainFragmentLayout.setEmptyView(emptyView);
    }


    public void initializeAgentAdapter(List<PCAgentWalletTransaction> ary) {
        adapter = new PCAgentActivityAdapter(ary, (PCAgentMainActivity) this.getActivity());
        mainFragmentLayout.setAdapter(adapter);
        if (emptyView != null && ary != null && ary.size() > 0) {
            emptyView.setVisibility(View.GONE);
        }
    }


    @Override
    public void onDataLoadStart(View view) {
        super.onDataLoadStart(view);
        makeRequest();
    }
    @Override
    public void onCollectionDataLoadFinished(Collection<? extends PCData> pcData) {
        super.onCollectionDataLoadFinished(pcData);
        this.setAgentAdapter((Queue<PCAgentWalletTransaction>) pcData);
    }

    public class PCAgentActivityAdapter extends BaseAdapter {


        private List<PCAgentWalletTransaction> activities = new ArrayList<>();
        private PCAgentMainActivity context = new PCAgentMainActivity();
        private LayoutInflater inflater = null;

        public PCAgentActivityAdapter(List<PCAgentWalletTransaction> activities, PCAgentMainActivity context) {
            super();
            this.activities = activities;
            this.context = context;
            this.inflater = (LayoutInflater) context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void updateAdapter(PCAgentWalletTransaction newTrans) {
            activities.add(0, newTrans);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return activities.size();
        }

        @Override
        public Object getItem(int position) {
            return activities.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            try {
                ListItemHolder listItemHolder;

                if (convertView == null) {
                    //inflate the layout
                    convertView = inflater.inflate(com.pesachoice.billpay.activities.R.layout.pc_activity_list_item, parent, false);

                    //setup the viewholder
                    listItemHolder = new ListItemHolder();
                    listItemHolder.receiver_name = (TextView) convertView.findViewById(com.pesachoice.billpay.activities.R.id.receiver_name);
                    listItemHolder.transaction_type = (TextView) convertView.findViewById(com.pesachoice.billpay.activities.R.id.transaction_type);
                    listItemHolder.transaction_amount = (TextView) convertView.findViewById(com.pesachoice.billpay.activities.R.id.transaction_amount);
                    listItemHolder.transaction_date = (TextView) convertView.findViewById(com.pesachoice.billpay.activities.R.id.transaction_date);
                    listItemHolder.transaction_status = (TextView) convertView.findViewById(com.pesachoice.billpay.activities.R.id.transaction_status);
                    listItemHolder.repeat_transcation = (ImageView) convertView.findViewById(com.pesachoice.billpay.activities.R.id.repeat_transcation);
                    listItemHolder.itemView = convertView;
                    //cache the view holder
                    convertView.setTag(listItemHolder);
                } else {
                    listItemHolder = (ListItemHolder) convertView.getTag();
                }
                Log.e("Testingg", "prebefore");
                final PCAgentWalletTransaction activity = (PCAgentWalletTransaction) getItem(position);
                Log.e("Testingg", "before");
                if (activity != null && context != null) {
                    Log.e("Testingg", "Middlebefore");
                    String serviceType = activity.getServiceType();
                    if (serviceType != null) {
                        Log.e("Testingg", "postbefore");
                        String itemType = context.getTranscationType("agentServiceInfo");
                        final PCProductServiceInfo serviceInfo = activity.getAgentServiceInfo();

                        //TODO:need to handle case where service info is null
                        //this will help to show the user pending transcation
                        if (serviceInfo != null && serviceInfo.getTransactionStatus() == PCTransactionStatus.IN_PROGRESS) {
                            listItemHolder.transaction_status.setText(". IN PROGRESS");
                            //this change the textColor to red because the transaction is not yet successfully completed
                            listItemHolder.transaction_status.setTextColor(Color.parseColor("#FF0000"));
                            listItemHolder.repeat_transcation.setVisibility(View.GONE);
                        } else {
                            listItemHolder.transaction_status.setTextColor(getResources().getColor(com.pesachoice.billpay.activities.R.color.colorSuccess));
                            listItemHolder.transaction_status.setText(". SUCCESS");

                        }

                        PCSpecialUser receiver = activity.getReceiver();
                        PCSpecialUser sender = activity.getSender();

                        if (receiver != null) {
                            String fullName = StringUtils.replace(receiver.getFullName(), "null", "");
                            if (fullName != null && !StringUtils.isEmpty(fullName.trim())) {
                                listItemHolder.receiver_name.setText(StringUtils.capitalize(fullName));
                            } else {
                                listItemHolder.receiver_name.setText(receiver.getPhoneNumber());
                            }
                        }
                        listItemHolder.transaction_type.setText(itemType);
                        DecimalFormat formatter = new DecimalFormat("###,###,###.##");

                        if (getActivity() instanceof PCAgentMainActivity) {
                            PCAgentMainActivity pcMainTabActivity = (PCAgentMainActivity) getActivity();
                            pcCountryProfile = pcMainTabActivity.getCountryProfile();
                            if (pcCountryProfile.getCountry().equalsIgnoreCase("Rwanda")) {
                                List<PCCurrency> curr = pcCountryProfile.getCurrencies();
                                for (PCCurrency listCurr : curr) {

                                    if (listCurr.getCountry().equalsIgnoreCase("Rwanda")) {
                                        PCCurrency senderCurr = listCurr;
                                        Log.e("Contriessss;", listCurr.getCountry());
                                        Log.e("ExchangeRate;", listCurr.getExchangeRate());
                                        //Set the current Exchange Rate in CountryProfile
                                        String total = "";
                                        if (activity.getAgentServiceInfo().getTotalAmount() != 0) {
                                            total = (activity.getAgentServiceInfo().getTotalAmount()) + " " + senderCurr.getCurrencySymbol();
                                        } else {
                                            PCSpecialUser user = context.getAppUser();
                                            senderCurr = new PCCurrency();
                                            String currencyCode = "USD";
                                            String country = "US";
                                            if (user != null && user.getBaseCurrency() != null) {
                                                senderCurr = user.getBaseCurrency();
                                                if (!StringUtils.isEmpty(senderCurr.getCountry())) {
                                                    country = senderCurr.getCountry();
                                                }
                                                if (!StringUtils.isEmpty(senderCurr.getCurrencySymbol())) {
                                                    currencyCode = senderCurr.getCurrencySymbol();
                                                }
                                            }
                                            senderCurr.setCurrencySymbol(currencyCode);
                                            senderCurr.setCountrySymbol(country);
                                            total = (activity.getAgentServiceInfo().getTotalAmount()) + " " + senderCurr.getCurrencySymbol();
                                        }
                                        Log.e("country :", "finishhhLoadind");
                                        listItemHolder.repeat_transcation.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                //setup also repeat transaction listener
                                                repeatTransFragment.setTransaction(activity);
                                                repeatTransFragment.agentMainActivity = context;
                                                prepareCheckout();
                                                initiateCheckout();
                                            }
                                        });

                                        listItemHolder.transaction_amount.setText(total);
                                        Timestamp time = activity.getCreatedTime();
                                        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
                                        String transactionDate = dateFormat.format(time != null ? time : new Date());
                                        listItemHolder.transaction_date.setText(transactionDate);
                                        convertView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                PCAgentActivityDetailFragment frag = new PCAgentActivityDetailFragment();
                                                frag.setTransaction(activity);
                                                //TODO: this need to be fixed it's broken
                                                frag.setPcServiceInfo(serviceInfo);
                                                FragmentManager fm = ((PCMainTabActivity) context).getSupportFragmentManager();//getFragmentManager();
                                                fm.beginTransaction()
                                                        .replace(com.pesachoice.billpay.activities.R.id.list_activities, frag)
                                                        .addToBackStack(null)
                                                        .commit();
                                            }
                                        });
                                        break;
                                    }
                                }


                            }
                        }

                    } else if ((!StringUtils.isEmpty(serviceType))) {
                        Log.e("Testingg", "postbefore");
                        String itemType = context.getTranscationType(serviceType);

                        final PCServiceInfo serviceInfo = activity.getServiceInfo();
                        //TODO:need to handle case where service info is null
                        //this will help to show the user pending transcation
                        if (serviceInfo != null && serviceInfo.getTransactionStatus() == PCTransactionStatus.IN_PROGRESS) {
                            listItemHolder.transaction_status.setText(". IN PROGRESS");
                            listItemHolder.transaction_status.setTextColor(Color.parseColor("#FF0000"));
                            listItemHolder.repeat_transcation.setVisibility(View.GONE);
                        } else {
                            listItemHolder.transaction_status.setTextColor(Color.parseColor("#35535A"));
                            listItemHolder.transaction_status.setText(". SUCCESS");

                        }

                        PCSpecialUser receiver = activity.getReceiver();
                        PCSpecialUser sender = activity.getSender();

                        if (receiver != null) {
                            String fullName = StringUtils.replace(receiver.getFullName(), "null", "");
                            if (fullName != null && !StringUtils.isEmpty(fullName.trim())) {
                                listItemHolder.receiver_name.setText(StringUtils.capitalize(fullName));
                            } else {
                                listItemHolder.receiver_name.setText(receiver.getPhoneNumber());
                            }
                        }
                        listItemHolder.transaction_type.setText(itemType);

                        DecimalFormat formatter = new DecimalFormat("###,###,###.##");
                        PCCurrency currency = activity.getBaseCurrency();
                        String total = "";
                        if (currency != null && currency.getTotalAmount() != 0) {
                            total = formatter.format(currency.getTotalAmount()) + " " + currency.getCurrencySymbol();
                        } else {
                            PCSpecialUser user = context.getAppUser();
                            currency = new PCCurrency();
                            String currencyCode = "USD";
                            String country = "US";
                            if (user != null && user.getBaseCurrency() != null) {
                                currency = user.getBaseCurrency();
                                if (!StringUtils.isEmpty(currency.getCountry())) {
                                    country = currency.getCountry();
                                }
                                if (!StringUtils.isEmpty(currency.getCurrencySymbol())) {
                                    currencyCode = currency.getCurrencySymbol();
                                }
                            }
                            currency.setCurrencySymbol(currencyCode);
                            currency.setCountrySymbol(country);
                            total = formatter.format(serviceInfo.getTotalAmount()) + " " + currency.getCurrencySymbol();
                        }

                        listItemHolder.repeat_transcation.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //setup also repeat transaction listener
                                repeatTransFragment.setTransaction(activity);
                                repeatTransFragment.agentMainActivity = context;
                                prepareCheckout();
                                initiateCheckout();
                            }
                        });

                        listItemHolder.transaction_amount.setText(total);

                        Timestamp time = activity.getCreatedTime();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
                        String transactionDate = dateFormat.format(time != null ? time : new Date());
                        listItemHolder.transaction_date.setText(transactionDate);
                        convertView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                PCActivityDetailFragment frag = new PCActivityDetailFragment();
                                frag.setTransaction(activity);
                                //TODO: this need to be fixed it's broken
                                frag.setPcServiceInfo(serviceInfo);
                                FragmentManager fm = ((PCMainTabActivity) context).getSupportFragmentManager();//getFragmentManager();
                                fm.beginTransaction()
                                        .replace(com.pesachoice.billpay.activities.R.id.list_activities, frag)
                                        .addToBackStack(null)
                                        .commit();
                            }
                        });
                    }

                }
            } catch (Exception e) {
                String errorMessage = "Error while listing user transactions";
                presentError(e, errorMessage);
            }

            return convertView;


        }
    }

    static class ListItemHolder {
        View itemView;
        TextView receiver_name;
        TextView transaction_type;
        TextView transaction_amount;
        TextView transaction_date;
        TextView transaction_status;
        ImageView repeat_transcation;

    }

}
