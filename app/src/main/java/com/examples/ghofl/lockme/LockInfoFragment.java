package com.examples.ghofl.lockme;


import android.app.Fragment;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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

import org.json.JSONObject;

import java.util.List;

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
    private Boolean mMessageDeliver;
    private RippleBackground mRippleBackground;
    private Snackbar mSnacbar;

    private RequestQueue mRequestQueue;

    private Thread mThread;

    public LockInfoFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLockConnectionStatus = false;
        queryBuilder = DataQueryBuilder.create();
        Connection = false;
        mMessageDeliver = false;
        mRequestQueue = Volley.newRequestQueue(getActivity().getBaseContext());
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_fragment_lock_info, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //defining views
        _img_battery_status = view.findViewById(R.id.img_battery_status);
        _img_connection_status = view.findViewById(R.id.img_connection_status);
        _img_wifi_status = view.findViewById(R.id.img_wifi_status);

        _img_lock_status = view.findViewById(R.id.img_lock_status);
        _txv_lock_status = view.findViewById(R.id.txv_lock_status);

        _img_door_status = view.findViewById(R.id.img_door_status);
        _txv_door_status = view.findViewById(R.id.txv_door_status);

        mRippleBackground = view.findViewById(R.id.ripple_background);

        try {
            if (getArguments() != null && getArguments().getString("CheckStatus") != null)
            {
                saveUpdatedStatusOfLockInLocal(getArguments().getString("CheckStatus"));
                Connection = true;
            }
            else
                getStatusFromDirectConnection();
        } catch (Exception e) {
            Log.e(getTag(), e.getMessage());
        }

        //region event image connection status click
        _img_connection_status.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                try {
                    mRequestQueue.cancelAll(new RequestQueue.RequestFilter() {
                        @Override
                        public boolean apply(Request<?> request) {
                            return true;
                        }
                    });
                    ((LockActivity) getActivity()).LoadFragment(new ConnectLockToInternetFragment(),
                            getString(R.string.fragment_connect_lock_to_internet));
                } catch (Exception e) {
                    Log.e(getTag(), e.getMessage());
                }
            }
        });
        //endregion event image connection status click

        //region event image lock status click
        _img_lock_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (!mRippleBackground.isRippleAnimationRunning()) {
                        mRippleBackground.startRippleAnimation();
//                        if (Utilities.checkMobileDataOrWifiEnabled(getActivity().getBaseContext(), ConnectivityManager.TYPE_WIFI)) {
//                            requestDirectToggle();
//                        } else {
//                            Utilities.setWifiEnabled(getActivity().getBaseContext(), true);
//                            Log.e(getTag(), getString(R.string.message_wifi_is_off));
                            requestDirectToggle();
//                        }
                    }
                } catch (Exception e) {
                    Log.e(getTag(), e.getMessage());
                }
            }
        });
        //endregion event image lock status click
    }

    public void onStart() {
        super.onStart();
        updateImageResourceAndTexts();
    }

    private void updateImageResourceAndTexts() {
        if (getArguments() != null) {
            mLockSerialNumber = getArguments().getString(Utilities.TABLE_LOCK_COLUMN_SERIAL_NUMBER);
            readLocalStatus();
        }
    }

    private void requestDirectToggle() {
        stopThread();
        mThread = new Thread(new Task());
        mThread.start();

        if (Connection) {
            String url = getString(R.string.esp_http_address_toggle);
            StringRequest MyStringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener() {
                @Override
                public void onResponse(Object response) {
                    Log.e(getTag(), response.toString());
                    mMessageDeliver = true;
                    getStatusFromDirectConnection();
                    mRippleBackground.stopRippleAnimation();
                }
            }, new Response.ErrorListener() {
                public void onErrorResponse(VolleyError error) {
                    Log.e(getTag(), error.toString());
                    try {
                        Utilities.showSnackBarMessage(getView(), getString(R.string.snackbar_message_direct_order_failed), Snackbar.LENGTH_SHORT).show();
                        mRippleBackground.stopRippleAnimation();
                    } catch (Exception e) {
                        Log.e(getTag(), e.getMessage());
                    }
                }
            });

            MyStringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    4500,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            mRequestQueue.add(MyStringRequest);
        } else
            requestOnlineToggle();
    }

    private void requestOnlineToggle() {
        mMessageDeliver = false;
        PublishOptions mPublishOptions = new PublishOptions();
        mPublishOptions.setSubtopic(mLockSerialNumber);
        Backendless.Messaging.publish(getString(R.string.channel_toggle), getString(R.string.command_toggle),
                mPublishOptions, new AsyncCallback<MessageStatus>() {
                    public void handleResponse(MessageStatus messageStatus) {
                        Log.i(getTag(), messageStatus.toString());

                        if (messageStatus.getErrorMessage() == null) {
                            try {
                                mSnacbar = Utilities.showSnackBarMessage(
                                        getView(),
                                        getString(R.string.snackbar_message_command_sent),
                                        Snackbar.LENGTH_SHORT);
                                mSnacbar.show();
                            } catch (Exception e) {
                                Log.e(getTag(), e.getMessage());
                            }
                            setAppSubscriber();
                        }
                    }

                    public void handleFault(BackendlessFault backendlessFault) {
                        Log.e(getTag(), backendlessFault.getMessage());
                        Utilities.showSnackBarMessage(getView(), getString(R.string.log_cannot_connect), Snackbar.LENGTH_SHORT).show();
                        mRippleBackground.stopRippleAnimation();
                    }
                });
    }

    private void setAppSubscriber() {
        SubscriptionOptions mSubscriptionOptions = new SubscriptionOptions();
        mSubscriptionOptions.setSubtopic(mLockSerialNumber);
        Backendless.Messaging.subscribe(getString(R.string.channel_response_toggle), new AsyncCallback<List<Message>>() {
                    public void handleResponse(List<Message> response) {
                        Log.i(getTag(), response.toString());
                        mMessageDeliver = true;
                        readServerStatus();
                        mRippleBackground.stopRippleAnimation();
                    }

                    public void handleFault(BackendlessFault fault) {
                        Log.e(getTag(), fault.getMessage());
                        mMessageDeliver = false;
                    }
                },
                mSubscriptionOptions, new AsyncCallback<Subscription>() {
                    public void handleResponse(Subscription response) {
                        Log.e(getTag(), response.toString());
                        Subscription mSubscription = response;
                        mSubscription.cancelSubscription();
                    }

                    public void handleFault(BackendlessFault fault) {
                        Log.e(getTag(), fault.getMessage());
                        mRippleBackground.stopRippleAnimation();
                    }
                });
    }

    private void getStatusFromDirectConnection() {
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
                mRippleBackground.stopRippleAnimation();
                Connection = false;

//                try {
//                    getStatusFromDirectConnection();
//                } catch (Exception e) {
//                    Log.e(getTag(), e.getMessage());
//                }

                readServerStatus();
            }
        });
        mRequestQueue.add(MyStringRequest);
    }

    private void saveUpdatedStatusOfLockInLocal(String responseValue) {
        try {
            JSONObject mLockInfo = new JSONObject(responseValue);
            Lock mLock = new Lock();

            //region read status from lock
            if (mLockInfo != null) {
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

                Utilities.setStatusInLocalForALock(getActivity().getBaseContext(), mLock, mLockSerialNumber);
                //endregion read status from lock
            }
        } catch (Exception e) {
            Log.e(getTag(), e.getMessage());
        }
    }

    private void readLocalStatus() {
        try {
            //region read status from local
            JSONObject mLockObjectJSONObject = Utilities.getLockFromLocalWithSerialNumber(getActivity(), mLockSerialNumber);

            if (mLockObjectJSONObject != null) {
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
            }
        } catch (Exception e) {
            Log.e(getTag(), e.getMessage());
        }
        //endregion read status from local
    }

    private void readServerStatus() {
        //region read status from server
        StringBuilder mLockObjectStringBuilder = new StringBuilder();
        mLockObjectStringBuilder.append(Utilities.TABLE_LOCK_COLUMN_SERIAL_NUMBER);
        mLockObjectStringBuilder
                .append(".")
                .append(Utilities.TABLE_LOCK_COLUMN_SERIAL_NUMBER)
                .append("= \'")
                .append(mLockSerialNumber)
                .append("\'");

        queryBuilder.setWhereClause(String.valueOf(mLockObjectStringBuilder));
        Backendless.Data.of(Lock.class).find(queryBuilder, new AsyncCallback<List<Lock>>() {
            public void handleResponse(List<Lock> locks) {
                try {
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

                        Utilities.setStatusInLocalForALock(getActivity().getBaseContext(), locks.get(0), mLockSerialNumber);
                    }
                } catch (Exception e) {
                    Log.e(getTag(), e.getMessage());
                }
            }

            public void handleFault(BackendlessFault backendlessFault) {
                Log.e(getTag(), backendlessFault.getMessage());
            }
        });
        //endregion read status from server
    }

    class Task implements Runnable {
        @Override
        public void run() {
            try {
                Thread.sleep(5000);
                nothingReceived();
            } catch (Exception e) {
                Log.e(getTag(), e.getMessage());
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        stopThread();
    }

    private void stopThread() {
        try {
            mThread.stop();
        } catch (Exception e) {
            Log.e(getTag(), e.toString());
        }
    }

    private void nothingReceived() {
        try {
            if (!mMessageDeliver)
                Utilities.showSnackBarMessage(getView(), "Nothing received.", Snackbar.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(getTag(), e.getMessage());
        }
    }
}

