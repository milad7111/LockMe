package com.examples.ghofl.lockme;

import com.backendless.Backendless;
import com.backendless.exceptions.BackendlessFault;

/**
 * Created by family on 4/24/2018.
 */

public class LockClass {

    String mLockID;
    String mSerialNumber;
    Boolean mLockStatus;
    Boolean mDoorStatus;
    Boolean mConnectionStatus;
    Integer mBatteryStatus;
    Integer mWifiStatus;
    Integer mUserID;

    public String getLockID() {
        return mLockID;
    }

    public void setLockID(String mLockID) {
        this.mLockID = mLockID;
    }

    public String getSerialNumber() {
        return mSerialNumber;
    }

    public void setSerialNumber(String mSerialNumber) {
        this.mSerialNumber = mSerialNumber;
    }

    public Boolean getLockStatus() {
        return mLockStatus;
    }

    public void setLockStatus(Boolean mLockStatus) {
        this.mLockStatus = mLockStatus;
    }

    public Boolean getDoorStatus() {
        return mDoorStatus;
    }

    public void setDoorStatus(Boolean mDoorStatus) {
        this.mDoorStatus = mDoorStatus;
    }

    public Boolean getConnectionStatus() {
        return mConnectionStatus;
    }

    public void setConnectionStatus(Boolean mConnectionStatus) {
        this.mConnectionStatus = mConnectionStatus;
    }

    public Integer getBatteryStatus() {
        //Between 1-4
        return mBatteryStatus;
    }

    public void setBatteryStatus(Integer mBatteryStatus) {
        this.mBatteryStatus = mBatteryStatus;
    }

    public Integer getmWifiStatus() {
        //Between 0-4
        return mWifiStatus;
    }

    public void setWifiStatus(Integer mWifiStatus) {
        this.mWifiStatus = mWifiStatus;
    }

    public Integer getUserID() {
        return mUserID;
    }

    public void setUserID(Integer mUserID) {
        this.mUserID = mUserID;
    }
}