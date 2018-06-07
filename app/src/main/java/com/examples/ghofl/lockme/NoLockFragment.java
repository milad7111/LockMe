package com.examples.ghofl.lockme;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class NoLockFragment extends Fragment {

    private Button _btn_add_first_lock;
    private TextView _txv_no_lock;

    public NoLockFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_fragment_no_lock, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        _btn_add_first_lock = view.findViewById(R.id.btn_add_first_lock);
        _txv_no_lock = view.findViewById(R.id.txv_no_lock);
    }

    public void onStart() {
        super.onStart();

        setContentOfTextViewDependsOnInternetConnection();

        _btn_add_first_lock.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                ((LockActivity) getActivity()).LoadFragment(new AddLockFragment(), getString(R.string.fragment_add_lock_fragment));
            }
        });
    }

    private void setContentOfTextViewDependsOnInternetConnection() {
        RequestQueue MyRequestQueue = Volley.newRequestQueue(getActivity().getBaseContext());
        String url = getString(R.string.backendless_base_http_url);
        StringRequest MyStringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener() {
            @Override
            public void onResponse(Object response) {
                Log.e(getTag(), response.toString() + getString(R.string.log_connected_to_internet));
                _txv_no_lock.setText(R.string.there_is_no_lock);
            }
        }, new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                _txv_no_lock.setText(getString(R.string.connect_to_internet));
                Log.e(getTag(), error.toString());
            }
        });
        MyRequestQueue.add(MyStringRequest);
    }
}

