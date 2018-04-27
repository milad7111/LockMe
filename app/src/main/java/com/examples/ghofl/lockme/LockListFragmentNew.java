package com.examples.ghofl.lockme;


import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class LockListFragmentNew extends Fragment {
    private LockListFragmentNew.OnFragmentInteractionListener mListener;
    private ProgressBar _prg_load_lock_list;
    private ListView _lsv_lock_list;
    private ArrayList<String> mLockList;
    private ArrayList<String> mSerialNumbers;
    private Boolean mRealLockStatus;
    private ArrayAdapter<String> mArrayAdapter;
    private RecyclerView _rsv_lock_list_new;
    private LockListAdapter mLockListAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<JSONObject> mUserLockJsonObjectList;

    FloatingActionButton fab;

    public LockListFragmentNew() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_fragment_lock_list_new, container, false);

//        _prg_load_lock_list = rootView.findViewById(R.id.prg_load_lock_list);
        _rsv_lock_list_new = rootView.findViewById(R.id.rsv_lock_list_new);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        _rsv_lock_list_new.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity().getBaseContext());
        _rsv_lock_list_new.setLayoutManager(mLayoutManager);

        //TODO: set input.
        mUserLockJsonObjectList =   new ArrayList<>();
        mLockListAdapter = new LockListAdapter(getActivity().getBaseContext(),mUserLockJsonObjectList);
        _rsv_lock_list_new.setAdapter(mLockListAdapter);

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
        if (context instanceof LockListFragmentNew.OnFragmentInteractionListener) {
            mListener = (LockListFragmentNew.OnFragmentInteractionListener) context;
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
        showLocalLocks(Utilities.getLockFromLocal(getActivity().getBaseContext()));
        fab.setVisibility(View.VISIBLE);

        ((LockActivity) getActivity()).queryBuilder = DataQueryBuilder.create();
        ((LockActivity) getActivity()).queryBuilder.setRelationsDepth(Integer.valueOf(2));
        StringBuilder mWhereClause = new StringBuilder();
        mWhereClause.append("user");
        mWhereClause.append(".objectId=\'").append(((LockActivity) getActivity()).mCurrentUser.getObjectId()).append("\'");
        ((LockActivity) getActivity()).queryBuilder.setWhereClause(String.valueOf(mWhereClause));
        Backendless.Data.of("user_lock").find(((LockActivity) getActivity()).queryBuilder, new AsyncCallback<List<Map>>() {
            public void handleResponse(List<Map> maps) {
                if (maps.size() == 0)
                    showLocalLocks(Utilities.getLockFromLocal(getActivity().getBaseContext()));
                else {
                    showServerLocks(maps);
                    fab.setVisibility(View.VISIBLE);
                }

            }

            public void handleFault(BackendlessFault backendlessFault) {
                Log.e(backendlessFault.getCode(), backendlessFault.getMessage());
            }
        });
    }

    private void showServerLocks(List<Map> locklist) {
        _prg_load_lock_list.setVisibility(View.GONE);
        mLockList = new ArrayList();
        mSerialNumbers = new ArrayList();
        Iterator mLockListItterator = locklist.iterator();

        while (mLockListItterator.hasNext()) {
            Map row = (Map) mLockListItterator.next();
            mRealLockStatus = Utilities.getRealLockStatus(row);
            String mSerial = Utilities.getLockSerialNumber(row);
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
                mRealLockStatus = row.getBoolean(Utilities.TABLE_LOCK_COLUMN_LOCK_STATUS) && row.getBoolean(Utilities.TABLE_LOCK_COLUMN_DOOR_STATUS);
                mSerialNumbers.add(row.getString(Utilities.TABLE_LOCK_COLUMN_SERIAL_NUMBER));
                mLockList.add(String.format("Lock Serial Number: %s\nLock Status: %s", row.getString(Utilities.TABLE_LOCK_COLUMN_SERIAL_NUMBER),
                        mRealLockStatus ? getString(R.string.message_door_is_open) : getString(R.string.message_door_is_locked)));
            }

            mArrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, mLockList);

            _lsv_lock_list.setAdapter(null);
            _lsv_lock_list.setAdapter(mArrayAdapter);
            _lsv_lock_list.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Bundle mLockInfoFragmentBundle = new Bundle();
                    mLockInfoFragmentBundle.putString(Utilities.TABLE_LOCK_COLUMN_SERIAL_NUMBER, mSerialNumbers.get(i));
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

