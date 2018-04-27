package com.examples.ghofl.lockme;


import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.messaging.MessageStatus;
import com.backendless.messaging.PublishOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class LockInfoFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private ImageView _img_battery_status;
    private ImageView _img_connection_status;
    private ImageView _img_wifi_status;

    private ImageView _img_lock_status;
    private TextView _txv_lock_status;

    private ImageView _img_door_status;
    private TextView _txv_door_status;

    private String mLockSerialNumber;

    public LockInfoFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        updateImageResourceAndTexts();

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
                PublishOptions mPublishOptions = new PublishOptions();
                mPublishOptions.setSubtopic("change_lock_status.1200.user_id");
                Backendless.Messaging.publish("channel200", getString(R.string.direct_command_open),
                        mPublishOptions, new AsyncCallback<MessageStatus>() {
                    public void handleResponse(MessageStatus messageStatus) {
                        Log.i(getTag(), messageStatus.toString());
                    }

                    public void handleFault(BackendlessFault backendlessFault) {
                        Log.e(getTag(), backendlessFault.getMessage());

                        Utilities.showSnackBarMessage(getView(), backendlessFault.getMessage(), Snackbar.LENGTH_INDEFINITE);
                    }
                });
            }
        });
        //region event image lock status click

        return rootView;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null)
            mListener.onFragmentInteraction(uri);
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener)
            mListener = (OnFragmentInteractionListener) context;
        else
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
    }

    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void onStart() {
        super.onStart();

        updateImageResourceAndTexts();
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri var1);
    }

    @SuppressLint("ResourceType")
    private void changeDoorStatusInView(Boolean status) {
        if (status) {
            _img_door_status.setImageResource(R.drawable.ic_security_black_24dp);
            _img_lock_status.setColorFilter(Integer.parseInt(getResources().getString(android.R.color.holo_green_dark)));
            _txv_door_status.setText(R.string.door_is_close);
        } else {
            _img_door_status.setImageResource(R.drawable.ic_security_black_24dp);
            _txv_door_status.setText(R.string.door_is_open_not_secure);
        }
    }

    private void changeLockStatusInView(Boolean status) {
        if (status) {
            _img_lock_status.setImageResource(R.drawable.ic_close_lock_48dp);
            _txv_lock_status.setText(R.string.lock_is_secure);
        } else {
            _img_lock_status.setImageResource(R.drawable.ic_open_lock_48dp);
            _txv_lock_status.setText(R.string.lock_is_not_secure);
        }
    }

    @SuppressLint("ResourceType")
    private void changeConnectionStatusInView(Boolean status) {
        if (status) {
            _img_connection_status.setImageResource(R.drawable.ic_cloud_done);
            _img_connection_status.setColorFilter(Integer.parseInt(getResources().getString(android.R.color.holo_blue_light)));
        } else {
            _img_connection_status.setImageResource(R.drawable.ic_cloud_off_black_24dp);
        }
    }

    @SuppressLint("ResourceType")
    private void changeBatteryStatusInView(int status) {
        switch (status) {
            case 1: {
                _img_battery_status.setImageResource(R.drawable.ic_battery_20_black_24dp);
                _img_battery_status.setColorFilter(Integer.parseInt(getResources().getString(android.R.color.holo_red_dark)));
                break;
            }
            case 2: {
                _img_battery_status.setImageResource(R.drawable.ic_battery_50_black_24dp);
                _img_battery_status.setColorFilter(Integer.parseInt(getResources().getString(android.R.color.holo_red_light)));
                break;
            }
            case 3: {
                _img_battery_status.setImageResource(R.drawable.ic_battery_80_black_24dp);
                _img_battery_status.setColorFilter(Integer.parseInt(getResources().getString(android.R.color.holo_green_light)));
                break;

            }
            case 4: {
                _img_battery_status.setImageResource(R.drawable.ic_battery_charging_full_black_24dp);
                _img_battery_status.setColorFilter(Integer.parseInt(getResources().getString(android.R.color.holo_green_dark)));
                break;
            }
            default:
                break;
        }
    }

    @SuppressLint("ResourceType")
    private void changeWifiStatusInView(int status) {
        switch (status) {
            case 1: {
                _img_wifi_status.setImageResource(R.drawable.ic_signal_cellular_0_bar_black_24dp);
                _img_wifi_status.setColorFilter(Integer.parseInt(getResources().getString(android.R.color.holo_red_dark)));
                break;
            }
            case 2: {
                _img_wifi_status.setImageResource(R.drawable.ic_signal_cellular_2_bar_black_24dp);
                _img_wifi_status.setColorFilter(Integer.parseInt(getResources().getString(android.R.color.holo_blue_bright)));
                break;
            }
            case 3: {
                _img_wifi_status.setImageResource(R.drawable.ic_signal_cellular_3_bar_black_24dp);
                _img_wifi_status.setColorFilter(Integer.parseInt(getResources().getString(android.R.color.holo_blue_light)));
                break;

            }
            case 4: {
                _img_wifi_status.setImageResource(R.drawable.ic_signal_cellular_4_bar_black_24dp);
                _img_wifi_status.setColorFilter(Integer.parseInt(getResources().getString(android.R.color.holo_blue_dark)));
                break;
            }
            default:
                break;
        }
    }

    private void updateImageResourceAndTexts() {
        if (getArguments() != null) {
            mLockSerialNumber = getArguments().getString("SerialNumber");

            //region read status from local
            JSONObject mLockObjectJSONObject = Utilities.getLockFromLocalWithSerialNumber(getActivity(), mLockSerialNumber);

            if (mLockObjectJSONObject != null)
                try {
                    changeLockStatusInView(mLockObjectJSONObject.getBoolean(Utilities.TABLE_LOCK_COLUMN_LOCK_STATUS));
                    changeDoorStatusInView(mLockObjectJSONObject.getBoolean(Utilities.TABLE_LOCK_COLUMN_DOOR_STATUS));
                    changeConnectionStatusInView(mLockObjectJSONObject.getBoolean(Utilities.TABLE_LOCK_COLUMN_CONNECTION_STATUS));
                    changeBatteryStatusInView(mLockObjectJSONObject.getInt(Utilities.TABLE_LOCK_COLUMN_BATTERY_STATUS));
                    changeWifiStatusInView(mLockObjectJSONObject.getInt(Utilities.TABLE_LOCK_COLUMN_WIFI_STATUS));
                } catch (JSONException e) {
                    Log.e(getTag(), e.getMessage());
                }
            //endregion read status from local

            //region read status from server
            StringBuilder mLockObjectStringBuilder = new StringBuilder();
            mLockObjectStringBuilder.append(Utilities.TABLE_LOCK_COLUMN_SERIAL_NUMBER);
            mLockObjectStringBuilder.append(".").append(Utilities.TABLE_LOCK_COLUMN_SERIAL_NUMBER).append("= \'").append(mLockSerialNumber).append("\'");
            ((LockActivity) getActivity()).queryBuilder.setWhereClause(String.valueOf(mLockObjectStringBuilder));
            Backendless.Data.of(Utilities.TABLE_LOCK).find(((LockActivity) getActivity()).queryBuilder, new AsyncCallback<List<Map>>() {
                public void handleResponse(List<Map> maps) {
                    if (maps.size() != 0) {
                        changeLockStatusInView(Utilities.getLockStatus(maps.get(0)));
                        changeDoorStatusInView(Utilities.getDoorStatus(maps.get(0)));
                        changeConnectionStatusInView(Utilities.getConnectionStatus(maps.get(0)));
                        changeBatteryStatusInView(Utilities.getBatteryStatus(maps.get(0)));
                        changeWifiStatusInView(Utilities.getWifiStatus(maps.get(0)));
                    }
                }

                public void handleFault(BackendlessFault backendlessFault) {
                    Log.e(getTag(), backendlessFault.getMessage());
                }
            });
            //endregion read status from server
        }
    }
}

