package com.examples.ghofl.lockme;


import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ConnectLockToInternetFragment extends Fragment {

    private ListView _lsv_internet_network;
    private ArrayList<String> mWifiNetworks;
    private ArrayAdapter<String> mArrayAdapter;
    private Boolean wantToCloseDialog;

    private EditText _edt_lock_password;
    private EditText _edt_lock_name;

    private AlertDialog.Builder builder;
    private AlertDialog dialogView;
    private String mSSIDForConnectESP8266ToInternet;

    public ConnectLockToInternetFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_fragment_connect_lock_to_internet, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        _lsv_internet_network = view.findViewById(R.id.lsv_internet_network);
    }

    public void onStart() {
        super.onStart();
        getListOfAvailableNetworks();
    }

    private void getListOfAvailableNetworks() {
        RequestQueue MyRequestQueue = Volley.newRequestQueue(getActivity());
        String url = getString(R.string.esp_http_address_networks);
        StringRequest MyStringRequest = new StringRequest(Request.Method.GET, url, new Listener() {
            @Override
            public void onResponse(Object response) {
                try {
                    _lsv_internet_network.setAdapter(null);
                    mWifiNetworks = Utilities.parseESPAvailableNetworksResponse(response.toString());
                    mArrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, mWifiNetworks);
                    _lsv_internet_network.setAdapter(mArrayAdapter);
                    _lsv_internet_network.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            mSSIDForConnectESP8266ToInternet = mWifiNetworks.get(i);
                            builder = (new AlertDialog.Builder(getActivity())).setView(getActivity().getLayoutInflater().inflate(
                                    R.layout.layout_dialog_wifi_password, null)).setNegativeButton(R.string.dialog_button_discard,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            dialogView.dismiss();
                                        }
                                    }).setPositiveButton(R.string.dialog_button_confirm, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                }
                            }).setTitle(getString(R.string.dialog_enter_password) + mWifiNetworks.get(i));
                            dialogView = builder.create();
                            dialogView.show();

                            _edt_lock_password = dialogView.findViewById(R.id.edt_lock_password);
                            _edt_lock_name = dialogView.findViewById(R.id.edt_lock_name);
                            _edt_lock_name.setVisibility(View.GONE);

                            dialogView.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new android.view.View.OnClickListener() {
                                public void onClick(View v) {
                                    wantToCloseDialog = false;
                                    if (!_edt_lock_password.getText().toString().isEmpty()) {
                                        connectToInternet(mSSIDForConnectESP8266ToInternet, _edt_lock_password.getText().toString());
                                        wantToCloseDialog = true;
                                    }

                                    if (wantToCloseDialog)
                                        dialogView.dismiss();
                                }
                            });
                        }
                    });
                } catch (Exception e) {
                    Log.e(getTag(), e.getMessage());
                }
            }
        }, new ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Log.e(getTag(), error.toString());
            }
        });

        MyStringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        MyRequestQueue.add(MyStringRequest);
    }

    private void connectToInternet(final String ssid, final String password) {
        RequestQueue MyRequestQueue = Volley.newRequestQueue(getActivity());
        String url = getString(R.string.esp_http_address_connect);
        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, new Listener() {
            public void onResponse(Object response) {
                try{
                    Utilities.showSnackBarMessage(getView(), response.toString(), Snackbar.LENGTH_INDEFINITE).show();
                } catch (Exception e){
                    Log.e(getTag(), e.getMessage());
                }
            }
        }, new ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Log.e(getTag(), error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put(Utilities.SSID_FOR_CONNECT_ESP8266_TO_INTERNET, ssid);
                params.put(Utilities.PASSWORD_FOR_CONNECT_ESP8266_TO_INTERNET, password);
                return params;
            }
        };

        MyStringRequest.setRetryPolicy(new DefaultRetryPolicy(
                11000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        MyRequestQueue.add(MyStringRequest);
    }
}

