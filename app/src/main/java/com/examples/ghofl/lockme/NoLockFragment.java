package com.examples.ghofl.lockme;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;

public class NoLockFragment extends Fragment {

    private NoLockFragment.OnFragmentInteractionListener mListener;
    private Button _btn_add_first_lock;

    public NoLockFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.layout_fragment_no_lock, container, false);

        _btn_add_first_lock = rootView.findViewById(R.id.btn_add_first_lock);

        return rootView;
    }

    public void onButtonPressed(Uri uri) {
        if (this.mListener != null) {
            this.mListener.onFragmentInteraction(uri);
        }
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof NoLockFragment.OnFragmentInteractionListener)
            this.mListener = (NoLockFragment.OnFragmentInteractionListener) context;
        else
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
    }

    public void onDetach() {
        super.onDetach();
        this.mListener = null;
    }

    public void onStart() {
        super.onStart();
        this._btn_add_first_lock.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
//                ((LockActivity)NoLockFragment.this.getActivity()).LoadFragment(new AddLockFragment(), NoLockFragment.this.getString(2131427404));
            }
        });
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri var1);
    }
}

