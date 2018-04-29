package com.examples.ghofl.lockme;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.AdapterView.OnItemClickListener;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public class AddLockFragment extends Fragment {
    private OnFragmentInteractionListener mListener;
    private ProgressBar _prg_wifi_lock_list;
    private ListView _lsv_lock_wifi;
    private static WifiManager mWifiManager;
    private static WifiReceiver mWifiReceiver;
    private static List<ScanResult> mWifiLockList;
    private ArrayList<String> mWifiLockArrayList = new ArrayList();
    private ArrayAdapter mWifiLockArrayAdapter;
    private Builder builder;
    private AlertDialog dialogView;
    private EditText _edt_lock_password;
    private EditText _edt_lock_name;
    private Boolean wantToCloseDialog;
    private WifiConfiguration wifiConfig;
    private String mLockWifiName;
    private String mLockName;
    private String mLockSerialNumber;

    public AddLockFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mWifiReceiver = new WifiReceiver();
        wifiConfig = new WifiConfiguration();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_fragment_add_lock, container, false);

        _prg_wifi_lock_list = rootView.findViewById(R.id.prg_wifi_lock_list);
        _lsv_lock_wifi = rootView.findViewById(R.id.lsv_lock_wifi);

        return rootView;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
        fillAdapterLockWifi();
    }

    private void fillAdapterLockWifi() {
        if (!mWifiManager.isWifiEnabled()) {
            if (mWifiManager.setWifiEnabled(true)) {
                getActivity().registerReceiver(mWifiReceiver, new IntentFilter("android.net.wifi.SCAN_RESULTS"));
                mWifiManager.startScan();
            } else {
                Log.i(getTag(), getString(R.string.permission_wifi_needed));
            }
        } else if (mWifiManager.isWifiEnabled()) {
            getActivity().registerReceiver(mWifiReceiver, new IntentFilter("android.net.wifi.SCAN_RESULTS"));
            mWifiManager.startScan();
        }

        mWifiLockArrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, mWifiLockArrayList);
        _lsv_lock_wifi.setAdapter(mWifiLockArrayAdapter);
        _lsv_lock_wifi.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, final int position, long id) {
                builder = (new Builder(getActivity())).setView(getActivity().getLayoutInflater().inflate(
                        R.layout.layout_dialog_wifi_password, null)).setNegativeButton(R.string.dialog_button_discard,
                        new OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialogView.dismiss();
                            }
                        }).setPositiveButton(R.string.dialog_button_confirm, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).setTitle(getString(R.string.dialog_enter_password) + mWifiLockArrayList.get(position));
                dialogView = builder.create();
                dialogView.show();

                _edt_lock_password = dialogView.findViewById(R.id.edt_lock_password);
                _edt_lock_name = dialogView.findViewById(R.id.edt_lock_name);

                dialogView.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new android.view.View.OnClickListener() {
                    public void onClick(View v) {
                        wantToCloseDialog = false;
                        if (!_edt_lock_password.getText().toString().isEmpty()) {
                            wifiConfig = new WifiConfiguration();
                            wifiConfig.SSID = String.format("\"%s\"", mWifiLockArrayList.get(position));
                            wifiConfig.preSharedKey = String.format("\"%s\"", _edt_lock_password.getText().toString());
                            wifiConfig.allowedKeyManagement.set(1);
                            int netId = mWifiManager.addNetwork(wifiConfig);
                            if (netId == -1) {
                                Log.i(getTag(), getString(R.string.error_wifi_initialization));
                                _edt_lock_password.setText(null);
                            } else {
                                List list = mWifiManager.getConfiguredNetworks();
                                Iterator mWifiManagerConfiguredNetworks = list.iterator();

                                while (mWifiManagerConfiguredNetworks.hasNext()) {
                                    WifiConfiguration i = (WifiConfiguration) mWifiManagerConfiguredNetworks.next();
                                    if (i.SSID != null && i.SSID.equals(String.format("\"%s\"",
                                            mWifiLockArrayList.get(position)))) {
                                        mWifiManager.disableNetwork(mWifiManager.getConnectionInfo().getNetworkId());
                                        if (mWifiManager.enableNetwork(i.networkId, true)) {
                                            mWifiManager.saveConfiguration();
                                            int mWaitTime = 500;

                                            while (!mWifiManager.getConnectionInfo().getSSID()
                                                    .equals(wifiConfig.SSID)) {
                                                try {
                                                    Thread.sleep((long) mWaitTime);
                                                    mWaitTime += 500;
                                                    if (mWaitTime == 10000)
                                                        break;

                                                } catch (Exception e) {
                                                    Log.e(getTag(), e.getMessage());
                                                }
                                            }

                                            mLockName = _edt_lock_name.getText().toString();
                                            mLockSerialNumber = _edt_lock_password.getText().toString();
                                            mLockWifiName = (mWifiLockArrayList.get(position)).toString();
                                            wantToCloseDialog = true;
                                        }
                                        break;
                                    }
                                }
                            }
                        }

                        if (wantToCloseDialog) {
                            checkConnectionToESP8266();
                            dialogView.dismiss();
                        }

                    }
                });
            }
        });
    }

    private class WifiReceiver extends BroadcastReceiver {
        private WifiReceiver() {
        }

        public void onReceive(Context c, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals("android.net.wifi.SCAN_RESULTS")) {
                mWifiLockList = mWifiManager.getScanResults();
                mWifiLockArrayList.clear();
                ArrayList mSavedWifiNames = Utilities.getSavedLocalWifiNames(getActivity().getBaseContext());

                for (int i = 0; i < mWifiLockList.size(); ++i) {
                    if ((mWifiLockList.get(i)).SSID.contains(getString(R.string.WIFI_PREFIX_NAME)) &&
                            !mSavedWifiNames.contains((mWifiLockList.get(i)).SSID)) {
                        mWifiLockArrayList.add((mWifiLockList.get(i)).SSID);
                    }
                    mWifiLockArrayList.add((mWifiLockList.get(i)).SSID);
                }

                _prg_wifi_lock_list.setVisibility(View.GONE);
                mWifiLockArrayAdapter.notifyDataSetChanged();
            }
        }
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri var1);

    }

    private void saveLockToLocal(Boolean saved_on_server) {
        JSONObject mUniqueLock = new JSONObject();

        try {
            mUniqueLock.put(Utilities.TABLE_LOCK_COLUMN_SERIAL_NUMBER, mLockSerialNumber);
            mUniqueLock.put(Utilities.TABLE_USER_LOCK_COLUMN_LOCK_NAME, mLockName);
            mUniqueLock.put(Utilities.TABLE_LOCK_COLUMN_LOCK_SSID, mLockWifiName);
            mUniqueLock.put(Utilities.TABLE_USER_LOCK_COLUMN_ADMIN_STATUS, true);
            mUniqueLock.put(Utilities.TABLE_LOCK_COLUMN_LOCK_STATUS, true);
            mUniqueLock.put(Utilities.TABLE_LOCK_COLUMN_DOOR_STATUS, true);
            mUniqueLock.put(Utilities.TABLE_USER_LOCK_COLUMN_SAVE_STATUS, saved_on_server);
            mUniqueLock.put(Utilities.TABLE_LOCK_COLUMN_CONNECTION_STATUS, false);
            mUniqueLock.put(Utilities.TABLE_LOCK_COLUMN_BATTERY_STATUS, 0);
            mUniqueLock.put(Utilities.TABLE_LOCK_COLUMN_WIFI_STATUS, 0);

            Utilities.addUserLockInLocal(getActivity(), mUniqueLock);
            Utilities.setValueInSharedPreferenceObject(getActivity().getBaseContext(),
                    getString(R.string.share_preference_parameter_number_of_admin_lock),
                    String.valueOf(
                            Integer.parseInt(Utilities.readSharedPreferenceObject(getActivity().getBaseContext(),
                                    getString(R.string.share_preference_parameter_number_of_admin_lock), String.valueOf(0))) + 1));

            goToLockInfo();

        } catch (JSONException var3) {
            Log.e(getTag(), var3.getMessage());
        }
    }

    public void checkConnectionToESP8266() {
        _prg_wifi_lock_list.setVisibility(View.VISIBLE);

        RequestQueue MyRequestQueue = Volley.newRequestQueue(getActivity().getBaseContext());
        String url = getString(R.string.esp_http_address_check);
        StringRequest MyStringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener() {
            @Override
            public void onResponse(Object response) {
                Log.i(getTag(), response.toString());

                saveLockToLocal(false);

                StringBuilder mUniqueLock = new StringBuilder();
                mUniqueLock.append(Utilities.TABLE_LOCK_COLUMN_SERIAL_NUMBER);
                mUniqueLock.append("=\'").append(mLockSerialNumber).append("\'");
                ((LockActivity) getActivity()).queryBuilder.setWhereClause(String.valueOf(mUniqueLock));
                Backendless.Data.of(Utilities.TABLE_LOCK).find(((LockActivity) getActivity()).queryBuilder, new AsyncCallback<List<Map>>() {
                    public void handleResponse(List<Map> maps) {
                        if (maps.size() != 0) {
                            HashMap mUserLock = new HashMap();

                            mUserLock.put(Utilities.TABLE_USER_LOCK_COLUMN_ADMIN_STATUS, true);
                            mUserLock.put(Utilities.TABLE_USER_LOCK_COLUMN_LOCK_NAME, mLockName);
                            mUserLock.put(Utilities.TABLE_USER_LOCK_COLUMN_LOCK, maps.get(0));
                            mUserLock.put(Utilities.TABLE_USER_LOCK_COLUMN_USER, Backendless.UserService.CurrentUser());

                            Backendless.Data.of(Utilities.TABLE_USER_LOCK).save(mUserLock, new AsyncCallback<Map>() {
                                public void handleResponse(Map response) {
                                    Utilities.setSaveStatusTrueInLocalForALock(getActivity().getBaseContext(), mLockSerialNumber);
                                }

                                public void handleFault(BackendlessFault backendlessFault) {
                                    Log.e(getTag(), backendlessFault.getMessage());
                                }
                            });
                        }

                    }

                    public void handleFault(BackendlessFault backendlessFault) {
                        Log.e(getTag(), backendlessFault.getMessage());
                    }
                });
            }
        }, new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Log.e(getTag(), error.getMessage());
                _prg_wifi_lock_list.setVisibility(View.GONE);

                Utilities.showSnackBarMessage(getView(), getString(R.string.lock_wifi_strength_is_low), Snackbar.LENGTH_INDEFINITE);
            }
        });

        MyStringRequest.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        MyRequestQueue.add(MyStringRequest);
    }

    private void goToLockInfo() {
        Bundle mLockInfoFragmentBundle = new Bundle();
        mLockInfoFragmentBundle.putString(Utilities.TABLE_LOCK_COLUMN_SERIAL_NUMBER, mLockSerialNumber);
        LockInfoFragment mLockInfoFragment = new LockInfoFragment();
        mLockInfoFragment.setArguments(mLockInfoFragmentBundle);
        ((LockActivity) getActivity()).LoadFragment(mLockInfoFragment, getString(R.string.fragment_lock_info_fragment));
    }
}
