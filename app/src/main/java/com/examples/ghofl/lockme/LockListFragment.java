package com.examples.ghofl.lockme;


import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class LockListFragment extends Fragment {
    private LockListFragment.OnFragmentInteractionListener mListener;
    private RecyclerView _rsv_lock_list;
    private LockListAdapter mLockListAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<JSONObject> mUserLockJsonObjectList;
    private HashMap[] mUserLocks;
    private Snackbar mSyncingSnackBar;

    private FloatingActionButton fab;

    public LockListFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_fragment_lock_list, container, false);

        _rsv_lock_list = rootView.findViewById(R.id.rsv_lock_list);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        _rsv_lock_list.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity().getBaseContext());
        _rsv_lock_list.setLayoutManager(mLayoutManager);

        mUserLockJsonObjectList = new ArrayList<>();
        mLockListAdapter = new LockListAdapter(getActivity().getBaseContext(), mUserLockJsonObjectList, this);
        _rsv_lock_list.setAdapter(mLockListAdapter);

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
        checkInternetConnection();
        getAllSavedLock();
    }

    private void getAllSavedLock() {
        mUserLockJsonObjectList.clear();
        showLocalLocks(Utilities.getLockFromLocal(getActivity().getBaseContext()));
        fab.setVisibility(View.VISIBLE);

        try {
            mUserLocks = (HashMap[]) Backendless.UserService.CurrentUser().getProperty("related_locks");
            if (mUserLocks.length != 0) {
                ((LockActivity) getActivity()).queryBuilder = DataQueryBuilder.create();
                ((LockActivity) getActivity()).queryBuilder.setWhereClause(Utilities.createWhereClauseWithListOfObjectId(
                        Utilities.TABLE_LOCK_COLUMN_RELATED_USERS,
                        "objectId",
                        mUserLocks));
                Backendless.Data.of(Lock.class).find(((LockActivity) getActivity()).queryBuilder, new AsyncCallback<List<Lock>>() {
                    @Override
                    public void handleResponse(List<Lock> response) {
                        try {
                            mSyncingSnackBar.dismiss();
                        } catch (Exception e) {
                            Log.e(getTag(), e.getMessage());
                        }

                        showServerLocks(response);
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Log.e(getTag(), fault.getMessage());
                    }
                });
            } else
                Log.e(getTag(), "mUserLocks size is zero.");
        } catch (Exception e) {
            Log.e(getTag(), e.getMessage());
        }
    }

    private void showServerLocks(List<Lock> lock_list) {
        mUserLockJsonObjectList.clear();

        for (int i = 0; i < lock_list.size(); i++) {
            JSONObject mLockObject = new JSONObject();
            try {
                mLockObject.put(Utilities.TABLE_LOCK_COLUMN_CONNECTION_STATUS, lock_list.get(i).getConnectionStatus());
                mLockObject.put(Utilities.TABLE_LOCK_COLUMN_LOCK_STATUS, lock_list.get(i).getLockStatus());
                //TODO ATTENTION: Two forward lines are true : if order of mUserLocks members and Retrieve Locks be the same
                mLockObject.put(Utilities.TABLE_USER_LOCK_COLUMN_LOCK_NAME, lock_list.get(i).getLockName(i));
                mLockObject.put(Utilities.TABLE_USER_LOCK_COLUMN_ADMIN_STATUS, lock_list.get(i).getAdminStatus(i));
                mLockObject.put(Utilities.TABLE_LOCK_COLUMN_SERIAL_NUMBER, lock_list.get(0).getSerialNumber().getSerialNumber());
                mUserLockJsonObjectList.add(mLockObject);

                Utilities.setStatusInLocalForALock(getActivity().getBaseContext(), lock_list.get(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        mLockListAdapter.notifyDataSetChanged();
    }

    private void showLocalLocks(ArrayList<JSONObject> lock_list) {
        Iterator mLockListIterator = lock_list.iterator();

        while (mLockListIterator.hasNext()) {
            JSONObject row = (JSONObject) mLockListIterator.next();
            JSONObject mLockObject = new JSONObject();
            try {
                mLockObject.put(Utilities.TABLE_LOCK_COLUMN_CONNECTION_STATUS, row.get(Utilities.TABLE_LOCK_COLUMN_CONNECTION_STATUS));
                mLockObject.put(Utilities.TABLE_LOCK_COLUMN_LOCK_STATUS, row.get(Utilities.TABLE_LOCK_COLUMN_LOCK_STATUS));
                mLockObject.put(Utilities.TABLE_USER_LOCK_COLUMN_LOCK_NAME, row.get(Utilities.TABLE_USER_LOCK_COLUMN_LOCK_NAME));
                mLockObject.put(Utilities.TABLE_USER_LOCK_COLUMN_ADMIN_STATUS, row.get(Utilities.TABLE_USER_LOCK_COLUMN_ADMIN_STATUS));
                mLockObject.put(Utilities.TABLE_LOCK_COLUMN_SERIAL_NUMBER, row.get(Utilities.TABLE_LOCK_COLUMN_SERIAL_NUMBER));
                mUserLockJsonObjectList.add(mLockObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri var1);
    }

    private void checkInternetConnection() {
        RequestQueue MyRequestQueue = Volley.newRequestQueue(getActivity().getBaseContext());
        String url = getString(R.string.backendless_base_http_url);
        StringRequest MyStringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener() {
            @Override
            public void onResponse(Object response) {
                Log.e(getTag(), response.toString());

                mSyncingSnackBar = Utilities.showSnackBarMessage(getView(), "Syncing ...", Snackbar.LENGTH_INDEFINITE);
                mSyncingSnackBar.show();

                if (Backendless.UserService.CurrentUser() == null)
                    getActivity().finish();
                else
                    getAllSavedLock();
            }
        }, new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Log.e(getTag(), error.toString());
                final Snackbar mSnackBar = Snackbar.make(getView(), "You are offline ...", Snackbar.LENGTH_INDEFINITE);
                mSnackBar.setAction(R.string.dialog_button_try_again, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        checkInternetConnection();
                        mSnackBar.dismiss();
                    }
                }).show();
            }
        });
        MyRequestQueue.add(MyStringRequest);
    }
}

