package com.examples.ghofl.lockme;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;

import java.util.HashMap;

public class LockActivity extends AppCompatActivity {
    public BackendlessUser mCurrentUser;

    public LockActivity() {
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_lock);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mCurrentUser = Backendless.UserService.CurrentUser();
        checkExistSavedLock();
    }

    public void LoadFragment(Fragment fragment, String tag) {
        FragmentManager mFragmentManager = getFragmentManager();
        FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.replace(R.id.frml_lock_fragments, fragment, tag);
        mFragmentTransaction.addToBackStack(tag);
        mFragmentTransaction.commit();
    }

    private void checkExistSavedLock() {
        if (Utilities.getLockFromLocal(getBaseContext()).size() > 0)
            LoadFragment(new LockListFragment(), getString(R.string.fragment_lock_list_fragment));
        else {
            try {
                java.util.HashMap[] mUserLocks = (HashMap[]) Backendless.UserService.CurrentUser().getProperty("related_locks");
                if (mUserLocks.length != 0)
                    LoadFragment(new LockListFragment(), getString(R.string.fragment_lock_list_fragment));
                else
                    LoadFragment(new NoLockFragment(), getString(R.string.fragment_no_lock_fragment));
            } catch (Exception e) {
                Log.e(this.getClass().getName(), e.getMessage());
                LoadFragment(new NoLockFragment(), getString(R.string.fragment_no_lock_fragment));
            }
        }
    }
}

