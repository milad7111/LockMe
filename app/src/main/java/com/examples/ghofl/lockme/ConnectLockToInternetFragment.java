package com.examples.ghofl.lockme;


import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class ConnectLockToInternetFragment extends Fragment {
    private OnFragmentInteractionListener mListener;

    public ConnectLockToInternetFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_fragment_connect_lock_to_internet, container, false);
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
        getListOfAvailableNetworks(getTag());
    }

    private void getListOfAvailableNetworks(final String tag) {
        RequestQueue MyRequestQueue = Volley.newRequestQueue(getActivity());
        String url = getString(R.string.esp_http_address) + getString(R.string.esp_connect);
        StringRequest MyStringRequest = new StringRequest(0, url, new Listener() {
            @Override
            public void onResponse(Object response) {
                Toast.makeText(getActivity(), response.toString(), Toast.LENGTH_SHORT).show();
            }
        }, new ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Log.e(tag, error.getMessage());
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

