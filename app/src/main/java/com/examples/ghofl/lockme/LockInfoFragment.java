package com.examples.ghofl.lockme;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.icu.util.UniversalTimeScale;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.backendless.Backendless;
import com.backendless.Subscription;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.messaging.Message;
import com.backendless.messaging.MessageStatus;
import com.backendless.messaging.PublishOptions;
import com.backendless.messaging.SubscriptionOptions;
import com.backendless.persistence.DataQueryBuilder;
import com.skyfishjy.library.RippleBackground;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LockInfoFragment extends Fragment {

    private DataQueryBuilder queryBuilder;

    private ImageView _img_battery_status;
    private ImageView _img_connection_status;
    private ImageView _img_wifi_status;

    private ImageView _img_lock_status;
    private TextView _txv_lock_status;

    private ImageView _img_door_status;
    private TextView _txv_door_status;

    private String mLockSerialNumber;
    private Boolean mLockConnectionStatus;
    private Boolean mLockStatus;

    private Boolean Connection;

    RippleBackground mRippleBackground;

    public LockInfoFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLockConnectionStatus = false;
        queryBuilder = DataQueryBuilder.create();
        Connection = false;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_fragment_lock_info, container, false);

        //defining views
        _img_battery_status = rootView.findViewById(R.id.img_battery_status);
        _img_connection_status = rootView.findViewById(R.id.img_connection_status);
        _img_wifi_status = rootView.findViewById(R.id.img_wifi_status);

        _img_lock_status = rootView.findViewById(R.id.img_lock_status);
        _txv_lock_status = rootView.findViewById(R.id.txv_lock_status);

        _img_door_status = rootView.findViewById(R.id.img_door_status);
        _txv_door_status = rootView.findViewById(R.id.txv_door_status);

        mRippleBackground = rootView.findViewById(R.id.ripple_background);

        getStatusFromDirectConnection();

        //region event image connection status click
        _img_connection_status.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                ((LockActivity) getActivity()).LoadFragment(new ConnectLockToInternetFragment(),
                        getString(R.string.fragment_connect_lock_to_internet));
            }
        });
        //endregion event image connection status click

        //region event image lock status click
        _img_lock_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mRippleBackground.isRippleAnimationRunning()) {
                    mRippleBackground.startRippleAnimation();
                    if (Utilities.checkMobileDataOrWifiEnabled(getActivity().getBaseContext(), ConnectivityManager.TYPE_WIFI)) {
                        requestDirectToggle();
                    } else {
                        Utilities.setWifiEnabled(getActivity().getBaseContext(), true);
                        Log.e(getTag(), "Wifi is off.");
                        requestDirectToggle();
                    }
                }
            }
        });
        //endregion event image lock status click

        return rootView;
    }

    public void onStart() {
        super.onStart();
        updateImageResourceAndTexts();
    }

    private void updateImageResourceAndTexts() {
        if (getArguments() != null) {
            mLockSerialNumber = getArguments().getString(Utilities.TABLE_LOCK_COLUMN_SERIAL_NUMBER);

            readLocalStatus();
            getStatusFromDirectConnection();
        }
    }

    private void requestDirectToggle() {
        new Thread(new Task()).start();
        if (Connection) {
            RequestQueue MyRequestQueue = Volley.newRequestQueue(getActivity().getBaseContext());
            String url = getString(R.string.esp_http_address_toggle);
            StringRequest MyStringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener() {
                @Override
                public void onResponse(Object response) {
                    Log.e(this.getClass().getName(), response.toString());
                    //saveUpdatedStatusOfLockInLocal(response.toString());
                    //mRippleBackground.stopRippleAnimation();
                }
            }, new Response.ErrorListener() {
                public void onErrorResponse(VolleyError error) {
                    Log.e(getTag(), error.toString());
                }
            });
            MyStringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    4500,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            MyRequestQueue.add(MyStringRequest);
        } else
            requestOnlineToggle();
    }

    class Task implements Runnable {
        @Override
        public void run() {
            try {
                Thread.sleep(5000);
                getStatusFromDirectConnection();
            } catch (Exception e) {
                Log.e(getTag(), e.getMessage());
            }
        }
    }

    private void requestOnlineToggle() {
        PublishOptions mPublishOptions = new PublishOptions();
        mPublishOptions.setSubtopic(mLockSerialNumber);
        Backendless.Messaging.publish("toggle", getString(R.string.command_toggle),
                mPublishOptions, new AsyncCallback<MessageStatus>() {
                    public void handleResponse(MessageStatus messageStatus) {
                        Log.i(getTag(), messageStatus.toString());
//                        setAppSubscriber();
                    }

                    public void handleFault(BackendlessFault backendlessFault) {
                        Log.e(getTag(), backendlessFault.getMessage());
                        Utilities.showSnackBarMessage(getView(), backendlessFault.getMessage(), Snackbar.LENGTH_LONG).show();
                        mRippleBackground.stopRippleAnimation();
                    }
                });
    }

    private void setAppSubscriber() {
        SubscriptionOptions mSubscriptionOptions = new SubscriptionOptions();
        mSubscriptionOptions.setSubtopic(mLockSerialNumber);
        Backendless.Messaging.subscribe("response_toggle", new AsyncCallback<List<Message>>() {
                    public void handleResponse(List<Message> response) {
                        Message message = response.get(0);

                        Utilities.changeLockStatusInView(
                                Boolean.valueOf(message.getData().toString()),
                                _img_lock_status,
                                _txv_lock_status);

                        Utilities.changeDoorStatusInView(
                                Boolean.valueOf(message.getData().toString()),
                                _img_door_status,
                                _txv_door_status,
                                Boolean.valueOf(message.getData().toString()));

                        Log.i(getTag(), response.toString());

                        readServerStatus();
                    }

                    public void handleFault(BackendlessFault fault) {
                        Log.e(getTag(), fault.getMessage());
                        mRippleBackground.stopRippleAnimation();
                    }
                },
                mSubscriptionOptions, new AsyncCallback<Subscription>() {
                    public void handleResponse(Subscription response) {
                        Log.e(getTag(), response.toString());
                    }

                    public void handleFault(BackendlessFault fault) {
                        Log.e(getTag(), fault.getMessage());
                    }
                });
    }

    private void getStatusFromDirectConnection() {
        RequestQueue MyRequestQueue = Volley.newRequestQueue(getActivity().getBaseContext());
        String url = getString(R.string.esp_http_address_check);
        StringRequest MyStringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener() {
            @Override
            public void onResponse(Object response) {
                Log.e(getTag(), response.toString());
                saveUpdatedStatusOfLockInLocal(response.toString());
                mRippleBackground.stopRippleAnimation();
                Connection = true;
            }
        }, new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Log.e(getTag(), error.toString());
                Connection = false;
                readServerStatus();
            }
        });
        MyRequestQueue.add(MyStringRequest);
    }

    private void saveUpdatedStatusOfLockInLocal(String responseValue) {
        try {
            JSONObject mLockInfo = new JSONObject(responseValue);
            Lock mLock = new Lock();

            //region read status from lock
            if (mLockInfo != null)
                try {
                    mLockStatus = mLockInfo.getBoolean(Utilities.TABLE_LOCK_COLUMN_LOCK_STATUS);
                    Utilities.changeLockStatusInView(
                            mLockStatus,
                            _img_lock_status,
                            _txv_lock_status);
                    mLock.setLockStatus(mLockStatus);

                    Utilities.changeDoorStatusInView(
                            mLockInfo.getBoolean(Utilities.TABLE_LOCK_COLUMN_DOOR_STATUS),
                            _img_door_status,
                            _txv_door_status,
                            mLockStatus);
                    mLock.setDoorStatus(mLockInfo.getBoolean(Utilities.TABLE_LOCK_COLUMN_DOOR_STATUS));

                    mLockConnectionStatus = mLockInfo.getBoolean(Utilities.TABLE_LOCK_COLUMN_CONNECTION_STATUS);
                    Utilities.changeConnectionStatusInView(
                            mLockConnectionStatus,
                            _img_connection_status);
                    mLock.setConnectionStatus(mLockConnectionStatus);

                    Utilities.changeBatteryStatusInView(
                            mLockInfo.getInt(Utilities.TABLE_LOCK_COLUMN_BATTERY_STATUS),
                            _img_battery_status);
                    mLock.setBatteryStatus(mLockInfo.getInt(Utilities.TABLE_LOCK_COLUMN_BATTERY_STATUS));

                    Utilities.changeWifiStatusInView(
                            mLockInfo.getInt(Utilities.TABLE_LOCK_COLUMN_WIFI_STATUS),
                            _img_wifi_status);
                    mLock.setWifiStatus(mLockInfo.getInt(Utilities.TABLE_LOCK_COLUMN_WIFI_STATUS));

                    Utilities.setStatusInLocalForALock(getActivity().getBaseContext(), mLock);
                } catch (JSONException e) {
                    Log.e(getTag(), e.getMessage());
                }
            //endregion read status from lock

        } catch (JSONException e) {
            Log.e(getTag(), e.getMessage());
        }
    }

    private void readLocalStatus() {
        //region read status from local
        JSONObject mLockObjectJSONObject = Utilities.getLockFromLocalWithSerialNumber(getActivity(), mLockSerialNumber);

        if (mLockObjectJSONObject != null)
            try {
                mLockStatus = mLockObjectJSONObject.getBoolean(Utilities.TABLE_LOCK_COLUMN_LOCK_STATUS);
                Utilities.changeLockStatusInView(
                        mLockStatus,
                        _img_lock_status,
                        _txv_lock_status);

                Utilities.changeDoorStatusInView(
                        mLockObjectJSONObject.getBoolean(Utilities.TABLE_LOCK_COLUMN_DOOR_STATUS),
                        _img_door_status,
                        _txv_door_status,
                        mLockStatus);

                mLockConnectionStatus = mLockObjectJSONObject.getBoolean(Utilities.TABLE_LOCK_COLUMN_CONNECTION_STATUS);

                Utilities.changeConnectionStatusInView(
                        mLockConnectionStatus,
                        _img_connection_status);

                Utilities.changeBatteryStatusInView(
                        mLockObjectJSONObject.getInt(Utilities.TABLE_LOCK_COLUMN_BATTERY_STATUS),
                        _img_battery_status);

                Utilities.changeWifiStatusInView(
                        mLockObjectJSONObject.getInt(Utilities.TABLE_LOCK_COLUMN_WIFI_STATUS),
                        _img_wifi_status);
            } catch (JSONException e) {
                Log.e(getTag(), e.getMessage());
            }
        //endregion read status from local
    }

    private void readServerStatus() {
        //region read status from server
        StringBuilder mLockObjectStringBuilder = new StringBuilder();
        mLockObjectStringBuilder.append(Utilities.TABLE_LOCK_COLUMN_SERIAL_NUMBER);
        mLockObjectStringBuilder.append(".").append(Utilities.TABLE_LOCK_COLUMN_SERIAL_NUMBER).append("= \'").append(mLockSerialNumber).append("\'");
        queryBuilder.setWhereClause(String.valueOf(mLockObjectStringBuilder));
        Backendless.Data.of(Lock.class).find(queryBuilder, new AsyncCallback<List<Lock>>() {
            public void handleResponse(List<Lock> locks) {
                if (locks.size() != 0) {

                    mLockStatus = locks.get(0).getLockStatus();
                    Utilities.changeLockStatusInView(
                            mLockStatus,
                            _img_lock_status,
                            _txv_lock_status);

                    Utilities.changeDoorStatusInView(
                            locks.get(0).getDoorStatus(),
                            _img_door_status,
                            _txv_door_status,
                            mLockStatus);

                    mLockConnectionStatus = locks.get(0).getConnectionStatus();

                    Utilities.changeConnectionStatusInView(
                            mLockConnectionStatus,
                            _img_connection_status);

                    Utilities.changeBatteryStatusInView(
                            locks.get(0).getBatteryStatus(),
                            _img_battery_status);

                    Utilities.changeWifiStatusInView(
                            locks.get(0).getWifiStatus(),
                            _img_wifi_status);

                    Utilities.setStatusInLocalForALock(getActivity().getBaseContext(), locks.get(0));
                }

                mRippleBackground.stopRippleAnimation();
            }

            public void handleFault(BackendlessFault backendlessFault) {
                Log.e(getTag(), backendlessFault.getMessage());
                mRippleBackground.stopRippleAnimation();
            }
        });
        //endregion read status from server
    }
}

