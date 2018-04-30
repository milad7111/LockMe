package com.examples.ghofl.lockme;


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
        //endregion event image lock status click

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

    private void updateImageResourceAndTexts() {
        if (getArguments() != null) {
            mLockSerialNumber = getArguments().getString(Utilities.TABLE_LOCK_COLUMN_SERIAL_NUMBER);

            //region read status from local
            JSONObject mLockObjectJSONObject = Utilities.getLockFromLocalWithSerialNumber(getActivity(), mLockSerialNumber);

            if (mLockObjectJSONObject != null)
                try {
                    Utilities.changeLockStatusInView(
                            mLockObjectJSONObject.getBoolean(Utilities.TABLE_LOCK_COLUMN_LOCK_STATUS),
                            _img_lock_status,
                            _txv_lock_status);

                    Utilities.changeDoorStatusInView(
                            mLockObjectJSONObject.getBoolean(Utilities.TABLE_LOCK_COLUMN_DOOR_STATUS),
                            _img_door_status,
                            _txv_door_status);

                    Utilities.changeConnectionStatusInView(
                            mLockObjectJSONObject.getBoolean(Utilities.TABLE_LOCK_COLUMN_CONNECTION_STATUS),
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

            //region read status from server
            StringBuilder mLockObjectStringBuilder = new StringBuilder();
            mLockObjectStringBuilder.append(Utilities.TABLE_LOCK_COLUMN_SERIAL_NUMBER);
            mLockObjectStringBuilder.append(".").append(Utilities.TABLE_LOCK_COLUMN_SERIAL_NUMBER).append("= \'").append(mLockSerialNumber).append("\'");
            ((LockActivity) getActivity()).queryBuilder.setWhereClause(String.valueOf(mLockObjectStringBuilder));
            Backendless.Data.of(Lock.class).find(((LockActivity) getActivity()).queryBuilder, new AsyncCallback<List<Lock>>() {
                public void handleResponse(List<Lock> locks) {
                    if (locks.size() != 0) {
                        Utilities.changeLockStatusInView(
                                locks.get(0).getLockStatus(),
                                _img_lock_status,
                                _txv_lock_status);

                        Utilities.changeDoorStatusInView(
                                locks.get(0).getDoorStatus(),
                                _img_door_status,
                                _txv_door_status);

                        Utilities.changeConnectionStatusInView(
                                locks.get(0).getConnectionStatus(),
                                _img_connection_status);

                        Utilities.changeBatteryStatusInView(
                                locks.get(0).getBatteryStatus(),
                                _img_battery_status);

                        Utilities.changeWifiStatusInView(
                                locks.get(0).getWifiStatus(),
                                _img_wifi_status);

                        Utilities.setStatusInLocalForALock(getActivity().getBaseContext(), locks.get(0));
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

