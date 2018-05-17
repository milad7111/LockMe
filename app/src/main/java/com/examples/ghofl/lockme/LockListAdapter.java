package com.examples.ghofl.lockme;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

public class LockListAdapter extends RecyclerView.Adapter<LockListAdapter.ViewHolder> {

    private List<JSONObject> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context mContext;
    private Fragment mFragment;

    LockListAdapter(Context context, List<JSONObject> data, Fragment fragment) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.mContext = context;
        mFragment = fragment;
    }

    @Override
    public LockListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_lock_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LockListAdapter.ViewHolder holder, int position) {
        boolean connection_status = false;
        boolean lock_status = false;
        String lock_name = "my_lock";
        Boolean admin_status = true;
        try {
            connection_status = mData.get(position).getBoolean(Utilities.TABLE_LOCK_COLUMN_CONNECTION_STATUS);
            lock_status = mData.get(position).getBoolean(Utilities.TABLE_LOCK_COLUMN_LOCK_STATUS);
            admin_status = mData.get(position).getBoolean(Utilities.TABLE_USER_LOCK_COLUMN_ADMIN_STATUS);
            lock_name = mData.get(position).getString(Utilities.TABLE_USER_LOCK_COLUMN_LOCK_NAME);
        } catch (Exception e) {
            Log.e("LockListAdapter", e.getMessage());
        }

        Utilities.changeLockStatusInView(
                lock_status,
                holder._img_lock_status_list,
                null);

        Utilities.changeConnectionStatusInView(
                connection_status,
                holder._img_connection_status_list);

        holder._txv_lock_name.setText(lock_name);
        holder._txv_is_admin.setText((admin_status) ? "you are admin" : "member");
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView _img_connection_status_list;
        TextView _txv_lock_name;
        TextView _txv_is_admin;
        ImageView _img_lock_status_list;

        ViewHolder(View itemView) {
            super(itemView);

            _img_connection_status_list = itemView.findViewById(R.id.img_connection_status_list);
            _txv_lock_name = itemView.findViewById(R.id.txv_lock_name);
            _txv_is_admin = itemView.findViewById(R.id.txv_is_admin);
            _img_lock_status_list = itemView.findViewById(R.id.img_lock_status_list);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkInternetConnection(getAdapterPosition());
                }
            });
        }

        @Override
        public void onClick(View view) {
        }
    }

    // convenience method for getting data at click position
    JSONObject getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    private void checkInternetConnection(final int position) {
        if (Utilities.checkMobileDataOrWifiEnabled(mContext, ConnectivityManager.TYPE_WIFI)) {
            RequestQueue MyRequestQueue = Volley.newRequestQueue(mContext);
            String url = mContext.getString(R.string.backendless_base_http_url);
            StringRequest MyStringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener() {
                @Override
                public void onResponse(Object response) {
                    Log.e(this.getClass().getName(), response.toString());

                    try {
                        Bundle mLockInfoFragmentBundle = new Bundle();
                        mLockInfoFragmentBundle.putString(Utilities.TABLE_LOCK_COLUMN_SERIAL_NUMBER
                                , mData.get(position).getString(Utilities.TABLE_LOCK_COLUMN_SERIAL_NUMBER));
                        LockInfoFragment mLockInfoFragment = new LockInfoFragment();
                        mLockInfoFragment.setArguments(mLockInfoFragmentBundle);

                        FragmentManager mFragmentManager = mFragment.getFragmentManager();
                        FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();
                        mFragmentTransaction.replace(R.id.frml_lock_fragments, mLockInfoFragment,
                                mContext.getString(R.string.fragment_lock_info_fragment));

                        mFragmentTransaction.addToBackStack(mContext.getString(R.string.fragment_lock_info_fragment));
                        mFragmentTransaction.commit();

                    } catch (Exception e) {
                        Log.e(this.getClass().getName(), e.getMessage());
                    }
                }
            }, new Response.ErrorListener() {
                public void onErrorResponse(VolleyError error) {
                    Log.e(this.getClass().getName(), error.toString());

                    try {
                        if (Utilities.getLockFromLocalWithSerialNumber(mContext,
                                mData.get(position).getString(Utilities.TABLE_LOCK_COLUMN_SERIAL_NUMBER))
                                .getBoolean(Utilities.TABLE_USER_LOCK_COLUMN_ADMIN_STATUS))
                            checkDirectConnection(position);
                        else
                            Utilities.showSnackBarMessage(mFragment.getView(), mContext.getString(R.string.message_not_admin_for_lock),
                                    Snackbar.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Log.e(this.getClass().getName(), e.getMessage());
                    }
                }
            });
            MyRequestQueue.add(MyStringRequest);
        } else {
            try {
                Utilities.setWifiEnabled(mContext, true);
                Log.e(this.getClass().getName(), mContext.getString(R.string.message_wifi_is_off));
                Utilities.sleepSomeTime(3000);
                checkInternetConnection(position);
            } catch (Exception e) {
                Log.e(this.getClass().getName(), e.getMessage());
            }
        }
    }

    private void checkDirectConnection(final int position) {
        String mConnectedWifiName = Utilities.checkConnectToAnyLockWifi(mContext).replace("\"", "");
        String mWifiNameOfSelectedLock = null;
        try {
            mWifiNameOfSelectedLock = Utilities.getLockFromLocalWithSerialNumber(
                    mContext,
                    mData.get(position)
                            .getString(Utilities.TABLE_LOCK_COLUMN_SERIAL_NUMBER))
                    .getString(Utilities.TABLE_LOCK_COLUMN_LOCK_SSID);

            if (!mConnectedWifiName.equals(mWifiNameOfSelectedLock)) {
                WifiConfiguration wifiConfig = new WifiConfiguration();
                WifiManager mWifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                wifiConfig.SSID = String.format("\"%s\"",
                        Utilities.getLockFromLocalWithSerialNumber(mContext, mData.get(position).getString(Utilities.TABLE_LOCK_COLUMN_SERIAL_NUMBER))
                                .getString(Utilities.TABLE_LOCK_COLUMN_LOCK_SSID));

                wifiConfig.preSharedKey = String.format("\"%s\"", mData.get(position).getString(Utilities.TABLE_LOCK_COLUMN_SERIAL_NUMBER));
                wifiConfig.allowedKeyManagement.set(1);
                int netId = mWifiManager.addNetwork(wifiConfig);
                if (netId == -1) {
                    Log.i(getClass().getName(), mContext.getString(R.string.error_wifi_initialization));
                } else {
                    List<WifiConfiguration> list = mWifiManager.getConfiguredNetworks();
                    Iterator<WifiConfiguration> mWifiManagerConfiguredNetworks = list.iterator();

                    while (mWifiManagerConfiguredNetworks.hasNext()) {
                        WifiConfiguration i = mWifiManagerConfiguredNetworks.next();
                        if (i.SSID != null && i.SSID.equals(String.format("\"%s\"",
                                Utilities.getLockFromLocalWithSerialNumber(mContext, mData.get(position).getString(Utilities.TABLE_LOCK_COLUMN_SERIAL_NUMBER))
                                        .getString(Utilities.TABLE_LOCK_COLUMN_LOCK_SSID)))) {
                            if (mWifiManager.enableNetwork(i.networkId, true)) {
                                mWifiManager.saveConfiguration();
                            }
                            break;
                        }
                    }
                }

                Utilities.sleepSomeTime(3000);
            }
        } catch (Exception e) {
            Log.e(this.getClass().getName(), e.getMessage());
        }

        RequestQueue MyRequestQueue = Volley.newRequestQueue(mContext);
        String url = mContext.getString(R.string.esp_http_address_check);
        StringRequest MyStringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener() {
            @Override
            public void onResponse(Object response) {
                Log.e(this.getClass().getName(), response.toString());

                try {
                    Bundle mLockInfoFragmentBundle = new Bundle();
                    mLockInfoFragmentBundle.putString(Utilities.TABLE_LOCK_COLUMN_SERIAL_NUMBER
                            , mData.get(position).getString(Utilities.TABLE_LOCK_COLUMN_SERIAL_NUMBER));
                    LockInfoFragment mLockInfoFragment = new LockInfoFragment();
                    mLockInfoFragment.setArguments(mLockInfoFragmentBundle);

                    FragmentManager mFragmentManager = mFragment.getFragmentManager();
                    FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();
                    mFragmentTransaction.replace(R.id.frml_lock_fragments, mLockInfoFragment,
                            mContext.getString(R.string.fragment_lock_info_fragment));

                    mFragmentTransaction.addToBackStack(mContext.getString(R.string.fragment_lock_info_fragment));
                    mFragmentTransaction.commit();
                } catch (Exception e) {
                    Log.e(this.getClass().getName(), e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Log.e(this.getClass().getName(), error.toString());

                try {
                    final Snackbar mSnackBar = Snackbar.make(mFragment.getView(), "You are not connected neither to lock nor to internet.", Snackbar.LENGTH_INDEFINITE);
                    mSnackBar.setAction(R.string.dialog_button_try_again, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            checkInternetConnection(position);
                            mSnackBar.dismiss();
                        }
                    }).show();
                } catch (Exception e) {
                    Log.e(this.getClass().getName(), e.getMessage());
                }
            }
        });
        MyRequestQueue.add(MyStringRequest);
    }
}
