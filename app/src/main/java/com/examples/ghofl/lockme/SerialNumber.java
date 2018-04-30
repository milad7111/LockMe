package com.examples.ghofl.lockme;

import java.util.Date;
import java.util.List;

/**
 * Created by family on 4/24/2018.
 */

public class SerialNumber {

    private String objectId;
    private String serial_number;
    private String configuration;
    private String generation;
    private String mac_address;
    private Date created;
    private Date updated;

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

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public String getGeneration() {
        return generation;
    }

    public void setGeneration(String generation) {
        this.generation = generation;
    }

    public String getMacAddress() {
        return mac_address;
    }

    public void setMacAddress(String mac_address) {
        this.mac_address = mac_address;
    }
}