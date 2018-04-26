package com.examples.ghofl.lockme;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.Subscription;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.messaging.Message;
import com.backendless.messaging.SubscriptionOptions;
import com.backendless.persistence.DataQueryBuilder;

import java.util.List;
import java.util.Map;

public class LockActivity extends AppCompatActivity {
    public DataQueryBuilder queryBuilder;
    public BackendlessUser mCurrentUser;
    public List<Map> mResponseListMap;

    public LockActivity() {
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_lock);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mCurrentUser = Backendless.UserService.CurrentUser();
        caheckExistSavedLock();
    }

    public void LoadFragment(Fragment fragment, String tag) {
        FragmentManager mFragmentManager = getFragmentManager();
        FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.replace(R.id.frml_lock_fragments, fragment, tag);
        mFragmentTransaction.addToBackStack(tag);
        mFragmentTransaction.commit();
    }

    private void caheckExistSavedLock() {
        queryBuilder = DataQueryBuilder.create();
        queryBuilder.setRelationsDepth(2);
        StringBuilder mWhereClause = new StringBuilder();
        mWhereClause.append("user");
        mWhereClause.append(".objectId=\'").append(mCurrentUser != null ? mCurrentUser.getObjectId() : null).append("\'");
        queryBuilder.setWhereClause(String.valueOf(mWhereClause));
        Backendless.Data.of("user_lock").find(queryBuilder, new AsyncCallback<List<Map>>() {
            public void handleResponse(List<Map> maps) {
                if (maps.size() == 0) {
                    if (Defaults.hasAdminLock(getBaseContext()))
                        LoadFragment(new LockListFragment(), getString(R.string.fragment_lock_list_fragment));
                    else
                        LoadFragment(new NoLockFragment(), getString(R.string.fragment_no_lock_fragment));
                } else {
                    mResponseListMap = maps;
                    LoadFragment(new LockListFragment(), getString(R.string.fragment_lock_list_fragment));
                }
            }

            public void handleFault(BackendlessFault backendlessFault) {
                Log.e(backendlessFault.getCode(), backendlessFault.getMessage());
                if (Defaults.hasAdminLock(getBaseContext()))
                    LoadFragment(new LockListFragment(), getString(R.string.fragment_lock_list_fragment));
                else
                    LoadFragment(new NoLockFragment(), getString(R.string.fragment_no_lock_fragment));
            }
        });
    }

    public void checkConnectionToESP8266(final String tag, final String serialnumber) {
        RequestQueue MyRequestQueue = Volley.newRequestQueue(this);
        String url = getString(R.string.esp_http_address);
        StringRequest MyStringRequest = new StringRequest(Request.Method.GET, url, new Listener() {
            @Override
            public void onResponse(Object response) {
                Log.i(tag, response.toString());
                Bundle mLockInfoFragmentBundle = new Bundle();
                mLockInfoFragmentBundle.putString("SerialNumber", serialnumber);
                LockInfoFragment mLockInfoFragment = new LockInfoFragment();
                mLockInfoFragment.setArguments(mLockInfoFragmentBundle);
                LoadFragment(mLockInfoFragment, getString(R.string.fragment_lock_info_fragment));
            }
        }, new ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Log.e(tag, error.getMessage());
            }
        });
        MyRequestQueue.add(MyStringRequest);
    }

    private void setAppSubscriber() {
        SubscriptionOptions mSubscriptionOptions = new SubscriptionOptions();
        mSubscriptionOptions.setSubtopic("change_lock_status.1300.user_id");
        Backendless.Messaging.subscribe("channel200", new AsyncCallback<List<Message>>() {
            public void handleResponse(List<Message> response) {
                Message message = response.get(0);
//                if (message.getData().equals("done"))
//                    _img_lock_status.setImageResource(R.drawable.img_open_door);
            }

            public void handleFault(BackendlessFault fault) {
                Log.e(this.getClass().getName(), fault.getMessage());
            }
        }, mSubscriptionOptions, new AsyncCallback<Subscription>() {
            public void handleResponse(Subscription response) {
                Log.e(this.getClass().getName(), response.toString());
            }

            public void handleFault(BackendlessFault fault) {
                Log.e(this.getClass().getName(), fault.getMessage());
            }
        });
    }
}

