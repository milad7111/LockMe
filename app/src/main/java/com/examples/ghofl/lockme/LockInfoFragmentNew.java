package com.examples.ghofl.lockme;


import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class LockInfoFragmentNew extends Fragment {

    private OnFragmentInteractionListener mListener;

    private ImageView _img_battery_status;
    private ImageView _img_connection_status;
    private ImageView _img_wifi_status;

    private ImageView _img_lock_status_new;
    private TextView _txv_lock_status;

    private ImageView _img_door_status_new;
    private TextView _txv_door_status;


    private String mLockSerialNumber;

    public LockInfoFragmentNew() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("ResourceType")
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_fragment_lock_info_new, container, false);

        //defining views
        _img_battery_status = rootView.findViewById(R.id.img_battery_status);
        _img_connection_status = rootView.findViewById(R.id.img_connection_status);
        _img_wifi_status = rootView.findViewById(R.id.img_wifi_status);

        _img_lock_status_new = rootView.findViewById(R.id.img_lock_status_new);
        _txv_lock_status = rootView.findViewById(R.id.txv_lock_status);

        _img_door_status_new = rootView.findViewById(R.id.img_door_status_new);
        _txv_door_status = rootView.findViewById(R.id.txv_door_status);

        //setting initial values.
        JSONObject mLockObject = Defaults.getLockFromLocalWithSerialNumber(getActivity(), mLockSerialNumber);

        if (mLockObject != null) {
            try {
                if (mLockObject.getBoolean("lock_status")) {
                    _img_lock_status_new.setImageResource(R.drawable.img_close_lock);
                    _img_lock_status_new.setColorFilter(Integer.parseInt(getResources().getString(R.color.colorPrimary)));
                } else {
                    _img_lock_status_new.setImageResource(R.drawable.img_open_lock);
                    _img_lock_status_new.setColorFilter(Integer.parseInt(getResources().getString(R.color.colorAccent)));
                }

                if (mLockObject.getBoolean("door_status")) {
                    _img_door_status_new.setImageResource(R.drawable.ic_security_black_24dp);
                    _img_door_status_new.setColorFilter(Integer.parseInt(getResources().getString(R.color.colorPrimary)));
                    _txv_door_status.setText(getResources().getString(R.string.door_is_close));
                } else {
                    _img_door_status_new.setImageResource(R.drawable.ic_security_black_24dp);
                    _img_door_status_new.setColorFilter(Integer.parseInt(getResources().getString(R.color.colorAccent)));
                    _txv_door_status.setText(getResources().getString(R.string.door_is_open_non_secure));
                }
                //TO DO: add connection status to mLockObject.
                if(true) {
                    _img_connection_status.setImageResource(R.drawable.ic_cloud_done);
                    _img_connection_status.setColorFilter(Integer.parseInt(getResources().getString(R.color.colorPrimary)));
                }
                else
                    {
                        _img_connection_status.setImageResource(R.drawable.ic_cloud_off_black_24dp);
                        _img_connection_status.setColorFilter(Integer.parseInt(getResources().getString(R.color.colorAccent)));
                }
                //TODO: set battery status icon
                Integer battrey_status = 1;
                switch (battrey_status){
                    case 1:{
                        _img_battery_status.setImageResource(R.drawable.ic_battery_20_black_24dp);
                        _img_battery_status.setColorFilter(Integer.parseInt(getResources().getString(R.color.colorAccent)));
                        break;
                    }
                    case 2:{
                        _img_battery_status.setImageResource(R.drawable.ic_battery_50_black_24dp);
                        _img_battery_status.setColorFilter(Integer.parseInt(getResources().getString(R.color.colorAccent)));
                        break;
                    }
                    case 3:{
                        _img_battery_status.setImageResource(R.drawable.ic_battery_80_black_24dp);
                        _img_battery_status.setColorFilter(Integer.parseInt(getResources().getString(R.color.colorPrimary)));
                        break;

                    }
                    case 4:{
                        _img_battery_status.setImageResource(R.drawable.ic_battery_charging_full_black_24dp);
                        _img_battery_status.setColorFilter(Integer.parseInt(getResources().getString(R.color.colorPrimary)));
                        break;
                    }
                    default:
                }

                //TODO: set wifi status icon
                Integer wifi_status = 1;
                switch (battrey_status){
                    case 1:{
                        _img_wifi_status.setImageResource(R.drawable.ic_signal_cellular_0_bar_black_24dp);
                        _img_wifi_status.setColorFilter(Integer.parseInt(getResources().getString(R.color.colorAccent)));
                        break;
                    }
                    case 2:{
                        _img_wifi_status.setImageResource(R.drawable.ic_signal_cellular_2_bar_black_24dp);
                        _img_wifi_status.setColorFilter(Integer.parseInt(getResources().getString(R.color.colorAccent)));
                        break;
                    }
                    case 3:{
                        _img_wifi_status.setImageResource(R.drawable.ic_signal_cellular_3_bar_black_24dp);
                        _img_wifi_status.setColorFilter(Integer.parseInt(getResources().getString(R.color.colorPrimary)));
                        break;

                    }
                    case 4:{
                        _img_wifi_status.setImageResource(R.drawable.ic_signal_cellular_4_bar_black_24dp);
                        _img_wifi_status.setColorFilter(Integer.parseInt(getResources().getString(R.color.colorPrimary)));
                        break;
                    }
                    default:
                }

            } catch (JSONException e) {
                Log.e(getTag(), e.getMessage());
            }
        }

        //handle lock open or close command, later its need animation.
        _img_lock_status_new.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: define backendless API and set images again.
            }
        });

        return rootView;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }

    }

    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void onStart() {
        super.onStart();


    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri var1);
    }
}

