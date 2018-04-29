package com.examples.ghofl.lockme;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

/**
 * Created by family on 4/24/2018.
 */

public class UserLockClass {

    String objectId = null;
    String lockPlusUser = null;
    String lockName = null;
    Boolean adminStatus = false;
    LockClass lock = new LockClass();
    BackendlessUser user = Backendless.UserService.CurrentUser();

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getLockPlusUser() {
        return lockPlusUser;
    }

    public void setLockPlusUser() {
        this.lockPlusUser = lock.getObjectId() + user.getObjectId();
    }

    public String getLockName() {
        return lockName;
    }

    public void setLockName(String lockName) {
        this.lockName = lockName;
    }

    public Boolean getAdminStatus() {
        return adminStatus;
    }

    public void setAdminStatus(Boolean adminStatus) {
        this.adminStatus = adminStatus;
    }

    public LockClass getLock() {
        return lock;
    }

    public void setLock(LockClass lock) {
        this.lock = lock;
    }

    public BackendlessUser getUser() {
        return user;
    }

    public void setUser(BackendlessUser user) {
        this.user = user;
    }

    public void saveNewUserLock() {
        Backendless.Data.save(this, new AsyncCallback<UserLockClass>() {

            @Override
            public void handleResponse(UserLockClass response) {

            }

            @Override
            public void handleFault(BackendlessFault fault) {

            }
        });
    }


}