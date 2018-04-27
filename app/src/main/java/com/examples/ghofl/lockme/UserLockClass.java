package com.examples.ghofl.lockme;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

import java.util.concurrent.locks.Lock;

/**
 * Created by family on 4/24/2018.
 */

public class UserLockClass {

    String mUserLockID;
    String mLockName;
    Boolean mAdminStatus;
    LockClass mLock;
    BackendlessUser mUser;

    public String getUserLockID() {
        return mUserLockID;
    }

    public void mUserLockID(String mUserLockID) {
        this.mUserLockID = mUserLockID;
    }

    public String getLockName() {
        return mLockName;
    }

    public void setLockName(String mLockName) {
        this.mLockName = mLockName;
    }

    public Boolean getAdminStatus() {
        return mAdminStatus;
    }

    public void setAdminStatus(Boolean mAdminStatus) {
        this.mAdminStatus = mAdminStatus;
    }

    public LockClass getLock() {
        return mLock;
    }

    public void setLock(LockClass mLock) {
        this.mLock = mLock;
    }

    public BackendlessUser getUser() {
        return mUser;
    }

    public void setUser(BackendlessUser mUser) {
        this.mUser = mUser;
    }

    public void saveNewUserLock()
    {
        Backendless.Persistence.save(this, new AsyncCallback<UserLockClass>() {

            @Override
            public void handleResponse(UserLockClass response) {

            }

            @Override
            public void handleFault(BackendlessFault fault) {

            }
        });
    }


}