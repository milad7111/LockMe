package com.examples.ghofl.lockme;


import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.AdapterView.OnItemClickListener;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public class LockListFragment extends Fragment {
    private LockListFragment.OnFragmentInteractionListener mListener;
    private ProgressBar _prg_load_lock_list;
    private ListView _lsv_lock_list;
    private ArrayList<String> mLockList;
    private ArrayList<String> mSerialNumbers;
    private Boolean mRealLockStatus;
    private ArrayAdapter<String> mArrayAdapter;
    FloatingActionButton fab;

    public LockListFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_fragment_lock_list, container, false);

        _prg_load_lock_list = rootView.findViewById(R.id.prg_load_lock_list);
        _lsv_lock_list = rootView.findViewById(R.id.lsv_lock_list);
        fab = rootView.findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    ((LockActivity) getActivity()).LoadFragment(new AddLockFragment(), getString(R.string.fragment_add_lock_fragment));
                }
            });
        }

        return rootView;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }

    }

    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof LockListFragment.OnFragmentInteractionListener) {
            mListener = (LockListFragment.OnFragmentInteractionListener) context;
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
        getAllSavedLock();
    }

    private void getAllSavedLock() {
        new Defaults();
        if (Defaults.checkInternet().booleanValue()) {
            ((LockActivity) getActivity()).queryBuilder = DataQueryBuilder.create();
            ((LockActivity) getActivity()).queryBuilder.setRelationsDepth(Integer.valueOf(2));
            StringBuilder mWhereClause = new StringBuilder();
            mWhereClause.append("user");
            mWhereClause.append(".objectId=\'").append(((LockActivity) getActivity()).mCurrentUser.getObjectId()).append("\'");
            ((LockActivity) getActivity()).queryBuilder.setWhereClause(String.valueOf(mWhereClause));
            Backendless.Data.of("user_lock").find(((LockActivity) getActivity()).queryBuilder, new AsyncCallback<List<Map>>() {
                public void handleResponse(List<Map> maps) {
                    if (maps.size() == 0) {
                        ((LockActivity) getActivity()).LoadFragment(new NoLockFragment(), getString(R.string.fragment_no_lock_fragment));
                    } else {
                        showServerLocks(maps);
                        fab.setVisibility(View.VISIBLE);
                    }

                }

                public void handleFault(BackendlessFault backendlessFault) {
                    Log.e(backendlessFault.getCode(), backendlessFault.getMessage());
                }
            });
        } else {
            showLocalLocks(Defaults.getLockFromLocal(getActivity().getBaseContext()));
            fab.setVisibility(View.VISIBLE);
        }

    }

    private void showServerLocks(List<Map> locklist) {
        _prg_load_lock_list.setVisibility(View.GONE);
        mLockList = new ArrayList();
        mSerialNumbers = new ArrayList();
        Iterator mLockListItterator = locklist.iterator();

        while (mLockListItterator.hasNext()) {
            Map row = (Map) mLockListItterator.next();
            mRealLockStatus = Defaults.getRealLockStatus(row);
            String mSerial = Defaults.getLockSerialNumber(row);
            mSerialNumbers.add(mSerial);
            mLockList.add(String.format("Lock Serial Number: %s\nLock Status: %s", mSerial,
                    mRealLockStatus ? getString(R.string.message_door_is_open) : getString(R.string.message_door_is_locked)));
        }

        mArrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, mLockList);
        _lsv_lock_list.setAdapter(null);
        _lsv_lock_list.setAdapter(mArrayAdapter);
        _lsv_lock_list.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Bundle mLockInfoFragmentBundle = new Bundle();
                mLockInfoFragmentBundle.putString("SerialNumber", (String) mSerialNumbers.get(i));
                LockInfoFragment mLockInfoFragment = new LockInfoFragment();
                mLockInfoFragment.setArguments(mLockInfoFragmentBundle);
                ((LockActivity) getActivity()).LoadFragment(mLockInfoFragment, getString(R.string.fragment_lock_info_fragment));
            }
        });
    }

    private void showLocalLocks(ArrayList<JSONObject> locklist) {
        _prg_load_lock_list.setVisibility(View.GONE);
        mLockList = new ArrayList();
        mSerialNumbers = new ArrayList();
        Iterator mLockListItterator = locklist.iterator();

        try {
            while (mLockListItterator.hasNext()) {
                JSONObject row = (JSONObject) mLockListItterator.next();
                mRealLockStatus = row.getBoolean("lock_status") && row.getBoolean("door_status");
                mSerialNumbers.add(row.getString("serial_number"));
                mLockList.add(String.format("Lock Serial Number: %s\nLock Status: %s", row.getString("serial_number"),
                        mRealLockStatus ? getString(R.string.message_door_is_open) : getString(R.string.message_door_is_locked)));
            }

            mArrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, mLockList);

            _lsv_lock_list.setAdapter(null);
            _lsv_lock_list.setAdapter(mArrayAdapter);
            _lsv_lock_list.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Bundle mLockInfoFragmentBundle = new Bundle();
                    mLockInfoFragmentBundle.putString("SerialNumber", mSerialNumbers.get(i));
                    LockInfoFragment mLockInfoFragment = new LockInfoFragment();
                    mLockInfoFragment.setArguments(mLockInfoFragmentBundle);
                    ((LockActivity) getActivity()).LoadFragment(mLockInfoFragment, getString(R.string.fragment_lock_info_fragment));
                }
            });
        } catch (JSONException var4) {
            Log.e(getTag(), var4.getMessage());
        }

    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri var1);
    }
}

