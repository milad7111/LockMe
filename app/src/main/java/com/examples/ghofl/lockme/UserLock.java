package com.examples.ghofl.lockme;

import android.content.Context;
import android.util.Log;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

/**
 * Created by family on 4/24/2018.
 */

public class UserLock {

    String objectId = null;
    String lock_plus_user = null;
    String lock_name = null;
    Boolean admin_status = false;

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getLockPlusUser() {
        return lock_plus_user;
    }

    public void setLockPlusUser(String lock_object_id, String user_object_id) {
        this.lock_plus_user = lock_object_id + user_object_id;
    }

    public String getLockName() {
        return lock_name;
    }

    public void setLockName(String lockName) {
        this.lock_name = lockName;
    }

    public Boolean getAdminStatus() {
        return admin_status;
    }

    public void setAdminStatus(Boolean adminStatus) {
        this.admin_status = adminStatus;
    }
}