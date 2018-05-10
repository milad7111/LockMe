package com.examples.ghofl.lockme;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AddLockFragment extends Fragment {
    private OnFragmentInteractionListener mListener;
    private ProgressBar _prg_wifi_lock_list;
    private ListView _lsv_lock_wifi;
    private static WifiManager mWifiManager;
    private static WifiReceiver mWifiReceiver;
    private static List<ScanResult> mWifiLockList;
    private ArrayList<String> mWifiLockArrayList = new ArrayList<>();
    private ArrayAdapter<? extends String> mWifiLockArrayAdapter;
    private Builder builder;
    private AlertDialog dialogView;
    private EditText _edt_lock_password;
    private EditText _edt_lock_name;
    private Boolean wantToCloseDialog;
    private WifiConfiguration wifiConfig;
    private String mLockWifiName;
    private String mLockName;
    private String mLockSerialNumber;
    private Snackbar mSnackBar;

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
        checkWifi();
    }

    @Override
    public void onResume() {
        super.onResume();
        mLockName = "";
    }

    @Override
    public void onStop() {
        super.onStop();
        mSnackBar.dismiss();
        getActivity().getFragmentManager().beginTransaction().remove(this);
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

        mWifiLockArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, mWifiLockArrayList);
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

                _edt_lock_name.setText(mLockName);

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
                                List<WifiConfiguration> list = mWifiManager.getConfiguredNetworks();
                                Iterator<WifiConfiguration> mWifiManagerConfiguredNetworks = list.iterator();

                                while (mWifiManagerConfiguredNetworks.hasNext()) {
                                    WifiConfiguration i = mWifiManagerConfiguredNetworks.next();
                                    if (i.SSID != null && i.SSID.equals(String.format("\"%s\"",
                                            mWifiLockArrayList.get(position)))) {
//                                        mWifiManager.disableNetwork(mWifiManager.getConnectionInfo().getNetworkId());
                                        if (mWifiManager.enableNetwork(i.networkId, true)) {
                                            mWifiManager.saveConfiguration();

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
                ArrayList<String> mSavedWifiNames = Utilities.getSavedLocalWifiNames(getActivity().getBaseContext());

                for (int i = 0; i < mWifiLockList.size(); ++i) {
                    if ((mWifiLockList.get(i)).SSID.contains(getString(R.string.WIFI_PREFIX_NAME)) &&
                            !mSavedWifiNames.contains((mWifiLockList.get(i)).SSID)) {
                        mWifiLockArrayList.add((mWifiLockList.get(i)).SSID);
                    }
                }

                getActivity().unregisterReceiver(mWifiReceiver);
                tryAgain();

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

        } catch (JSONException e) {
            Log.e(getTag(), e.getMessage());
        }
    }

    public void checkConnectionToESP8266() {
        _prg_wifi_lock_list.setVisibility(View.VISIBLE);

        RequestQueue MyRequestQueueForCheck = Volley.newRequestQueue(getActivity());
        String url = getString(R.string.esp_http_address_check);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        StringRequest MyStringRequestForCheck = new StringRequest(Request.Method.GET, url, new Listener() {
            @Override
            public void onResponse(Object response) {
                Log.i(getTag(), response.toString());
                saveLockToLocal(false);
            }
        }, new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Log.e(getTag(), error.toString());
                _prg_wifi_lock_list.setVisibility(View.GONE);

                Utilities.showSnackBarMessage(getView(), getString(R.string.lock_wifi_strength_is_low), Snackbar.LENGTH_INDEFINITE).show();
            }
        });

        MyRequestQueueForCheck.add(MyStringRequestForCheck);
    }

    private void goToLockInfo() {
        Bundle mLockInfoFragmentBundle = new Bundle();
        mLockInfoFragmentBundle.putString(Utilities.TABLE_LOCK_COLUMN_SERIAL_NUMBER, mLockSerialNumber);
        LockInfoFragment mLockInfoFragment = new LockInfoFragment();
        mLockInfoFragment.setArguments(mLockInfoFragmentBundle);
        ((LockActivity) getActivity()).LoadFragment(mLockInfoFragment, getString(R.string.fragment_lock_info_fragment));
    }

    private void checkWifi() {
        if (Utilities.checkMobileDataOrWifiEnabled(getActivity().getBaseContext(), ConnectivityManager.TYPE_WIFI)) {
            fillAdapterLockWifi();
            _prg_wifi_lock_list.setVisibility(View.VISIBLE);
        } else {
            _prg_wifi_lock_list.setVisibility(View.GONE);
            mSnackBar = Utilities.showSnackBarMessage(getView(), "Turn on WIFI", Snackbar.LENGTH_INDEFINITE);
            mSnackBar.setAction(R.string.dialog_button_confirm, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Utilities.setWifiEnabled(getActivity().getBaseContext(), true);
                    mSnackBar.dismiss();
                    _prg_wifi_lock_list.setVisibility(View.VISIBLE);

                    try {
                        Thread.sleep(3000);
                        _prg_wifi_lock_list.setVisibility(View.GONE);
                    } catch (InterruptedException e) {
                        Log.e(getTag(), e.getMessage());
                    }

                    checkWifi();
                }
            });
            mSnackBar.show();
        }
    }

    private void tryAgain() {
        mSnackBar = Utilities.showSnackBarMessage(getView(), "Find more ...", Snackbar.LENGTH_INDEFINITE);
        mSnackBar.setAction("Try", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkWifi();
                mSnackBar.dismiss();
            }
        });
        mSnackBar.show();
    }
}
