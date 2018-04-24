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
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.backendless.Backendless;
import com.backendless.Subscription;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.messaging.Message;
import com.backendless.messaging.MessageStatus;
import com.backendless.messaging.PublishOptions;
import com.backendless.messaging.SubscriptionOptions;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public class LockInfoFragment extends Fragment {
    private OnFragmentInteractionListener mListener;
    private ImageView _img_connect_lock_to_internet;
    private ImageView _img_lock_status;
    private ImageView _img_door_status;
    private Button _btn_change_lock_status;
    private ProgressBar _prg_change_lock_status;
    private ProgressBar _prg_load_lock_door_status;
    RelativeLayout _lnrl_lock_door_status;
    private String mLockSerialNumber;

    public LockInfoFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_fragment_lock_info, container, false);

        _img_connect_lock_to_internet = rootView.findViewById(R.id.img_connect_lock_to_internet);
        _img_lock_status = rootView.findViewById(R.id.img_lock_status);
        _img_door_status = rootView.findViewById(R.id.img_door_status);
        _btn_change_lock_status = rootView.findViewById(R.id.btn_change_lock_status);
        _prg_change_lock_status = rootView.findViewById(R.id.prg_change_lock_status);
        _prg_load_lock_door_status = rootView.findViewById(R.id.prg_load_lock_door_status);
        _lnrl_lock_door_status = rootView.findViewById(R.id.lnrl_lock_door_status);

        if (getArguments() != null) {
            mLockSerialNumber = getArguments().getString("SerialNumber");
            StringBuilder mLockObject = new StringBuilder();
            mLockObject.append("serial_number");
            mLockObject.append(".result = \'").append(mLockSerialNumber).append("\'");
            ((LockActivity) getActivity()).queryBuilder.setWhereClause(String.valueOf(mLockObject));
            Backendless.Data.of("lock").find(((LockActivity) getActivity()).queryBuilder,
                    new AsyncCallback<List<Map>>() {
                public void handleResponse(List<Map> maps) {
                    if (maps.size() != 0) {
                        if (Defaults.getLockStatus(maps.get(0))) {
                            _img_lock_status.setImageResource(R.drawable.img_close_lock);
                        } else {
                            _img_lock_status.setImageResource(R.drawable.img_open_lock);
                        }

                        if (Defaults.getDoorStatus(maps.get(0))) {
                            _img_door_status.setImageResource(R.drawable.img_close_door);
                        } else {
                            _img_door_status.setImageResource(R.drawable.img_open_door);
                        }

                        _prg_load_lock_door_status.setVisibility(View.GONE);
                        _lnrl_lock_door_status.setVisibility(View.VISIBLE);
                    }
                }

            public void handleFault(BackendlessFault backendlessFault) {
                Log.e(getTag(), backendlessFault.getMessage());

                JSONObject mLockObject = Defaults.getLockFromLocalWithSerialNumber(getActivity(), mLockSerialNumber);

                if (mLockObject != null) {
                    try {
                        if (mLockObject.getBoolean("lock_status")) {
                            _img_lock_status.setImageResource(R.drawable.img_close_lock);
                        } else {
                            _img_lock_status.setImageResource(R.drawable.img_open_lock);
                        }

                        if (mLockObject.getBoolean("door_status")) {
                            _img_door_status.setImageResource(R.drawable.img_close_door);
                        } else {
                            _img_door_status.setImageResource(R.drawable.img_open_door);
                        }

                        _prg_load_lock_door_status.setVisibility(View.GONE);
                        _lnrl_lock_door_status.setVisibility(View.VISIBLE);
                    } catch (JSONException e) {
                        Log.e(getTag(), e.getMessage());
                    }
                }
            }
            });
        }

        _btn_change_lock_status.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                PublishOptions mPublishOptions = new PublishOptions();
                mPublishOptions.setSubtopic("change_lock_status.1200.user_id");
                Backendless.Messaging.publish("channel200", "open", mPublishOptions, new AsyncCallback<MessageStatus>() {
                    public void handleResponse(MessageStatus messageStatus) {
                        Log.i(getTag(), messageStatus.toString());
                    }

                public void handleFault(BackendlessFault backendlessFault) {
                    Log.e(getTag(), backendlessFault.getMessage());

                    final Snackbar mbackendlessFaultSnackBar = Snackbar.make(getView(),
                            backendlessFault.getMessage(), Snackbar.LENGTH_INDEFINITE);

                    mbackendlessFaultSnackBar.setAction(R.string.dialog_button_confirm, new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mbackendlessFaultSnackBar.dismiss();
                        }
                    }).show();
                }
                });
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
        _img_connect_lock_to_internet.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                ((LockActivity) getActivity()).LoadFragment(new ConnectLockToInternetFragment(),
                        getString(R.string.fragment_connect_lock_to_internet));
            }
        });
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri var1);
    }
}

