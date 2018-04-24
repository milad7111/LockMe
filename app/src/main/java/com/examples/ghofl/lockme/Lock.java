package com.examples.ghofl.lockme;

import android.util.Log;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.messaging.MessageStatus;
import com.backendless.messaging.PublishOptions;

/**
 * Created by family on 4/24/2018.
 */

public class Lock {

    Integer mLockID;
    String mSerialNumber;
    Integer mLockStatus;
    Boolean mDoorStatus;
    Boolean mIsConnected;
    Integer mBatteryCharge;
    Integer mSignalStrength;
    Integer mUserID;

    public Integer getmLockID() {
        return mLockID;
    }

    public void setmLockID(Integer mLockID) {
        this.mLockID = mLockID;
    }

    public String getmSerialNumber() {
        return mSerialNumber;
    }

    public void setmSerialNumber(String mSerialNumber) {
        this.mSerialNumber = mSerialNumber;
    }

    public Integer getmLockStatus() {
        return mLockStatus;
    }

    public void setmLockStatus(Integer mLockStatus) {
        this.mLockStatus = mLockStatus;
    }

    public Boolean getmDoorStatus() {
        return mDoorStatus;
    }

    public void setmDoorStatus(Boolean mDoorStatus) {
        this.mDoorStatus = mDoorStatus;
    }

    public Boolean getmIsConnected() {
        return mIsConnected;
    }

    public void setmIsConnected(Boolean mIsConnected) {
        this.mIsConnected = mIsConnected;
    }

    public Integer getmBatteryCharge() {
        //it should be between 1-4
        if(0<mBatteryCharge&& mBatteryCharge<5)
        return mBatteryCharge;
        //means that data is wrong inserted
        else return -1;
    }

    public void setmBatteryCharge(Integer mBatteryCharge) {
        this.mBatteryCharge = mBatteryCharge;
    }

    public Integer getmSignalStrength() {
        if(0<mSignalStrength&& mSignalStrength<5)
            return mSignalStrength;
        else return -1;
    }

    public void setmSignalStrength(Integer mSignalStrength) {
        this.mSignalStrength = mSignalStrength;
    }

    public Integer getmUserID() {
        return mUserID;
    }

    public void setmUserID(Integer mUserID) {
        this.mUserID = mUserID;
    }


}