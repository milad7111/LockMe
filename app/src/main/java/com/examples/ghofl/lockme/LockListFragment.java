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

public class LockListFragment extends Fragment {
    private LockListFragment.OnFragmentInteractionListener mListener;
    private RecyclerView _rsv_lock_list_new;
    private LockListAdapter mLockListAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<JSONObject> mUserLockJsonObjectList;

    FloatingActionButton fab;

    public LockListFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_fragment_lock_list, container, false);

        _rsv_lock_list_new = rootView.findViewById(R.id.rsv_lock_list_new);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        _rsv_lock_list_new.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity().getBaseContext());
        _rsv_lock_list_new.setLayoutManager(mLayoutManager);

        mUserLockJsonObjectList = new ArrayList<>();
        mLockListAdapter = new LockListAdapter(getActivity().getBaseContext(), mUserLockJsonObjectList, this);
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
        if (mListener != null)
            mListener.onFragmentInteraction(uri);
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof LockListFragment.OnFragmentInteractionListener)
            mListener = (LockListFragment.OnFragmentInteractionListener) context;
        else
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
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
        ((LockActivity) getActivity()).queryBuilder.setRelationsDepth(2);
        StringBuilder mWhereClause = new StringBuilder();
        mWhereClause.append(Utilities.TABLE_USER_LOCK_COLUMN_USER);
        mWhereClause.append(".objectId=\'").append(((LockActivity) getActivity()).mCurrentUser.getObjectId()).append("\'");
        ((LockActivity) getActivity()).queryBuilder.setWhereClause(String.valueOf(mWhereClause));

        Backendless.Data.of(Utilities.TABLE_USER_LOCK).find(((LockActivity) getActivity()).queryBuilder, new AsyncCallback<List<Map>>() {
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

    private void showServerLocks(List<Map> lock_list) {
        Iterator mLockListIterator = lock_list.iterator();

        while (mLockListIterator.hasNext()) {
            Map row = (Map) mLockListIterator.next();
            JSONObject mLockLockObject = new JSONObject();
            try {
                mLockLockObject.put(Utilities.TABLE_LOCK_COLUMN_CONNECTION_STATUS, row.get(Utilities.TABLE_LOCK_COLUMN_CONNECTION_STATUS));
                mLockLockObject.put(Utilities.TABLE_LOCK_COLUMN_LOCK_STATUS, row.get(Utilities.TABLE_LOCK_COLUMN_LOCK_STATUS));
                mLockLockObject.put(Utilities.TABLE_USER_LOCK_COLUMN_LOCK_NAME, row.get(Utilities.TABLE_USER_LOCK_COLUMN_LOCK_NAME));
                mLockLockObject.put(Utilities.TABLE_USER_LOCK_COLUMN_ADMIN_STATUS, row.get(Utilities.TABLE_USER_LOCK_COLUMN_ADMIN_STATUS));
                mLockLockObject.put(Utilities.TABLE_LOCK_COLUMN_SERIAL_NUMBER, row.get(Utilities.TABLE_LOCK_COLUMN_SERIAL_NUMBER));
                mUserLockJsonObjectList.add(mLockLockObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private void showLocalLocks(ArrayList<JSONObject> lock_list) {
        Iterator mLockListIterator = lock_list.iterator();

        while (mLockListIterator.hasNext()) {
            JSONObject row = (JSONObject) mLockListIterator.next();
            JSONObject mLockLockObject = new JSONObject();
            try {
                mLockLockObject.put(Utilities.TABLE_LOCK_COLUMN_CONNECTION_STATUS, row.get(Utilities.TABLE_LOCK_COLUMN_CONNECTION_STATUS));
                mLockLockObject.put(Utilities.TABLE_LOCK_COLUMN_LOCK_STATUS, row.get(Utilities.TABLE_LOCK_COLUMN_LOCK_STATUS));
                mLockLockObject.put(Utilities.TABLE_USER_LOCK_COLUMN_LOCK_NAME, row.get(Utilities.TABLE_USER_LOCK_COLUMN_LOCK_NAME));
                mLockLockObject.put(Utilities.TABLE_USER_LOCK_COLUMN_ADMIN_STATUS, row.get(Utilities.TABLE_USER_LOCK_COLUMN_ADMIN_STATUS));
                mLockLockObject.put(Utilities.TABLE_LOCK_COLUMN_SERIAL_NUMBER, row.get(Utilities.TABLE_LOCK_COLUMN_SERIAL_NUMBER));
                mUserLockJsonObjectList.add(mLockLockObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri var1);
    }
}

