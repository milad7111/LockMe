package com.examples.ghofl.lockme;


import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;

public class ConnectLockToInternetFragment extends Fragment {
    private OnFragmentInteractionListener mListener;

    private ListView _lsv_internet_network;
    private ArrayList<String> mWifiNetworks;
    private ArrayAdapter<String> mArrayAdapter;

    public ConnectLockToInternetFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_fragment_connect_lock_to_internet, container, false);

        _lsv_internet_network = rootView.findViewById(R.id.lsv_internet_network);

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
        getListOfAvailableNetworks();
    }

    private void getListOfAvailableNetworks() {
        RequestQueue MyRequestQueue = Volley.newRequestQueue(getActivity());
        String url = getString(R.string.esp_http_address) + getString(R.string.esp_get_networks);
        StringRequest MyStringRequest = new StringRequest(0, url, new Listener() {
            @Override
            public void onResponse(Object response) {
                Log.i(getTag(), response.toString());

                mWifiNetworks = new ArrayList<>();
                mArrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, mWifiNetworks);
                _lsv_internet_network.setAdapter(null);
                _lsv_internet_network.setAdapter(mArrayAdapter);
                _lsv_internet_network.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    }
                });
            }
        }, new ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Log.e(getTag(), error.toString());
            }
        });
        MyRequestQueue.add(MyStringRequest);
    }

    private void getSomeData(final String tag) {
        RequestQueue MyRequestQueue = Volley.newRequestQueue(getActivity());
        String url = getString(R.string.esp_http_address) + "check";
        StringRequest MyStringRequest = new StringRequest(0, url, new Listener() {
            public void onResponse(Object response) {
            }
        }, new ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Log.e(tag, error.getMessage());
            }
        });

        for (int i = 0; i < 100; ++i) {
            MyRequestQueue.add(MyStringRequest);
        }

    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri var1);
    }
}

