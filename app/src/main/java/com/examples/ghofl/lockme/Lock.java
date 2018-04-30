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

public class Lock {

    private String objectId;
    private String serial_number;
    private Boolean lock_status;
    private Boolean door_status;
    private Boolean connection_status;
    private Integer battery_status;
    private Integer wifi_status;
    private Date created;
    private Date updated;
    private List<UserLock> related_users;


    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getSerialNumber() {
        return serial_number;
    }

    public void setSerialNumber(String serialNumber) {
        this.serial_number = serialNumber;
    }

    public Boolean getLockStatus() {
        return lock_status;
    }

    public void setLockStatus(Boolean lockStatus) {
        this.lock_status = lockStatus;
    }

    public Boolean getDoorStatus() {
        return door_status;
    }

    public void setDoorStatus(Boolean doorStatus) {
        this.door_status = doorStatus;
    }

    public Boolean getConnectionStatus() {
        return connection_status;
    }

    public void setConnectionStatus(Boolean connectionStatus) {
        this.connection_status = connectionStatus;
    }

    public Integer getBatteryStatus() {
        return battery_status;
    }

    public void setBatteryStatus(Integer batteryStatus) {
        this.battery_status = batteryStatus;
    }

    public Integer getWifiStatus() {
        return wifi_status;
    }

    public void setWifiStatus(Integer wifiStatus) {
        this.wifi_status = wifiStatus;
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
}