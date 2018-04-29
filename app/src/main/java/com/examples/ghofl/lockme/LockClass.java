package com.examples.ghofl.lockme;

import android.hardware.usb.UsbRequest;

import com.backendless.Backendless;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by family on 4/24/2018.
 */

public class LockClass {

    private String objectId;
    private String serialNumber;
    private Boolean lockStatus;
    private Boolean doorStatus;
    private Boolean connectionStatus;
    private Integer batteryStatus;
    private Integer wifiStatus;
    private Integer user;
    private Date created;
    private Date updated;
    private List<UserLockClass> userLocks;


    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public Boolean getLockStatus() {
        return lockStatus;
    }

    public void setLockStatus(Boolean lockStatus) {
        this.lockStatus = lockStatus;
    }

    public Boolean getDoorStatus() {
        return doorStatus;
    }

    public void setDoorStatus(Boolean doorStatus) {
        this.doorStatus = doorStatus;
    }

    public Boolean getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(Boolean connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    public Integer getBatteryStatus() {
        //Between 1-4
        return batteryStatus;
    }

    public void setBatteryStatus(Integer batteryStatus) {
        this.batteryStatus = batteryStatus;
    }

    public Integer getWifiStatus() {
        //Between 0-4
        return wifiStatus;
    }

    public void setWifiStatus(Integer wifiStatus) {
        this.wifiStatus = wifiStatus;
    }

    public Integer getUser() {
        return user;
    }

    public void setUser(Integer user) {
        this.user = user;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public void addUserLock(UserLockClass userlock) {
        if (userLocks == null)
            userLocks = new ArrayList<UserLockClass>();

        userLocks.add(userlock);
    }

    public List<UserLockClass> getUserLocks() {
        return userLocks;
    }

    public void setUserLocks(List<UserLockClass> userLocks) {
        this.userLocks = userLocks;
    }
}