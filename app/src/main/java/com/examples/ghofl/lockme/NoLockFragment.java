package com.examples.ghofl.lockme;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
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

    private OnFragmentInteractionListener mListener;
    private Button _btn_add_first_lock;
    private TextView _txv_no_lock;

    public NoLockFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.layout_fragment_no_lock, container, false);

        _btn_add_first_lock = rootView.findViewById(R.id.btn_add_first_lock);
        _txv_no_lock = rootView.findViewById(R.id.txv_no_lock);

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

        setContentOfTextViewDependsOnInternetConnection();

        _btn_add_first_lock.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                ((LockActivity) getActivity()).LoadFragment(new AddLockFragment(), getString(R.string.fragment_add_lock_fragment));
            }
        });
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri var1);
    }

    private void setContentOfTextViewDependsOnInternetConnection() {
        RequestQueue MyRequestQueue = Volley.newRequestQueue(getActivity().getBaseContext());
        String url = getString(R.string.dummy_http_url);
        StringRequest MyStringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener() {
            @Override
            public void onResponse(Object response) {
                Log.e(getTag(), response.toString() + getString(R.string.log_connected_to_internet));
                _txv_no_lock.setText(R.string.there_is_no_lock);
            }
        }, new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                _txv_no_lock.setText(getString(R.string.connect_to_internet));
                Log.e(getTag(), error.getMessage());
            }
        });
        MyRequestQueue.add(MyStringRequest);
    }
}

