package com.examples.ghofl.lockme;

/**
 * Created by PHD on 4/19/2018.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class Defaults {
    private Boolean internet_connectivity = Boolean.valueOf(false);
    public static final String APPLICATION_ID = "43F16378-A01D-6283-FFE2-EBA6CE6C6300";
    public static final String API_KEY = "A9F660C9-F062-D314-FF04-9E8DF5931E00";
    public static final String SERVER_URL = "https://api.backendless.com";
    public static final String TABLE_USERS = "users";
    public static final String TABLE_LOCK = "lock";
    public static final String TABLE_SERIAL_NUMBER = "serial_number";
    public static final String TABLE_USER_LOCK = "user_lock";
    public static final String TABLE_USERS_COLUMN_EMAIL = "email";
    public static final String TABLE_USERS_COLUMN_MOBILE_NUMBER = "mobile_number";
    public static final String TABLE_USERS_COLUMN_PASSWORD = "password";
    public static final String TABLE_LOCK_COLUMN_BATTERY_CHARGE = "battery_charge";
    public static final String TABLE_LOCK_COLUMN_CONFIGURATION_STATUS = "configuration_status";
    public static final String TABLE_LOCK_COLUMN_CONNECTION_STATUS = "connection_status";
    public static final String TABLE_LOCK_COLUMN_WIFI_NAME = "wifi_name";
    public static final String TABLE_LOCK_COLUMN_DOOR_STATUS = "door_status";
    public static final String TABLE_LOCK_COLUMN_LOCK_STATUS = "lock_status";
    public static final String TABLE_LOCK_COLUMN_SERIAL_NUMBER = "serial_number";
    public static final String TABLE_LOCK_COLUMN_WIFI_STRENGTH = "wifi_strength";
    public static final String TABLE_SERIAL_NUMBER_COLUMN_CONFIGURATION = "configuration";
    public static final String TABLE_SERIAL_NUMBER_COLUMN_DATE_TIME = "date_time";
    public static final String TABLE_SERIAL_NUMBER_COLUMN_GENERATION = "generation";
    public static final String TABLE_SERIAL_NUMBER_COLUMN_MAC_ADDRESS = "mac_address";
    public static final String TABLE_SERIAL_NUMBER_COLUMN_NUMBER = "number";
    public static final String TABLE_SERIAL_NUMBER_COLUMN_RESULT = "result";
    public static final String TABLE_USER_LOCK_COLUMN_ADMIN_STATUS = "admin_status";
    public static final String TABLE_USER_LOCK_COLUMN_LOCK = "lock";
    public static final String TABLE_USER_LOCK_COLUMN_NAME = "name";
    public static final String TABLE_USER_LOCK_COLUMN_USER = "user";
    public static final String WIFI_PREFIX_NAME = "Lock Wifi";

    public Defaults() {
    }

    public static boolean isNetworkAvailable(Context context) {
        return false;
    }

    public static Boolean getRealLockStatus(Map map) {
        try {
            JSONObject e = new JSONObject();
            if ((new JSONObject(map)).get("lock") instanceof JSONObject) {
                e = new JSONObject((new JSONObject(map)).get("lock").toString());
            } else if ((new JSONObject(map)).get("lock") instanceof JSONArray) {
                e = (new JSONArray((new JSONObject(map)).get("lock").toString())).getJSONObject(0);
            }

            return Boolean.valueOf(e.getBoolean("lock_status") && e.getBoolean("door_status"));
        } catch (JSONException var2) {
            var2.printStackTrace();
            return null;
        }
    }

    public static Boolean getLockStatus(Map map) {
        try {
            return Boolean.valueOf((new JSONObject(map)).getBoolean("lock_status"));
        } catch (JSONException var2) {
            Log.e("Defaults", var2.getMessage());
            return null;
        }
    }

    public static Boolean getDoorStatus(Map map) {
        try {
            return Boolean.valueOf((new JSONObject(map)).getBoolean("door_status"));
        } catch (JSONException var2) {
            var2.printStackTrace();
            return null;
        }
    }

    public static String getValueFromLockObject(Map map, String parameter) {
        try {
            JSONObject e = new JSONObject();
            if ((new JSONObject(map)).get("lock") instanceof JSONObject) {
                e = new JSONObject((new JSONObject(map)).get("lock").toString());
            } else if ((new JSONObject(map)).get("lock") instanceof JSONArray) {
                e = (new JSONArray((new JSONObject(map)).get("lock").toString())).getJSONObject(0);
            }

            return e.getString(parameter);
        } catch (JSONException var3) {
            Log.e("Defaults", var3.getMessage());
            return null;
        }
    }

    public static String getLockSerialNumber(Map map) {
        try {
            return (new JSONObject(getValueFromLockObject(map, "serial_number"))).get("result").toString();
        } catch (JSONException var2) {
            Log.e("Defaults", var2.getMessage());
            return null;
        }
    }

    public static String readSharedPreferenceObject(Context context, String object, String defaultvalue) {
        SharedPreferences mSharedPreferences = context.getSharedPreferences(context.getString(R.string.shared_preference_name), 0);
        return mSharedPreferences.getString(object, defaultvalue);
    }

    public static void setValueInSharedPreferenceObject(Context context, String object, String value) {
        SharedPreferences mSharedPreferences = context.getSharedPreferences(context.getString(R.string.shared_preference_name), 0);
        Editor mEditor = mSharedPreferences.edit();
        mEditor.putString(object, value);
        mEditor.apply();
        boolean a = false;
    }

    public static Boolean hasAdminLock(Context context) {
        return Integer.parseInt(readSharedPreferenceObject(context,
                context.getString(R.string.share_preference_parameter_number_of_admin_lock),
                "0")) > 0
                ? Boolean.valueOf(true)
                : Boolean.valueOf(false);
    }

    public static void syncUserLockInLocal() {
        StringBuilder mWhereClause = new StringBuilder();
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setRelationsDepth(Integer.valueOf(2));
        mWhereClause.append("user");
        mWhereClause.append(".objectid=\'").append(Backendless.UserService.CurrentUser().getObjectId()).append("\'");
        queryBuilder.setWhereClause(String.valueOf(mWhereClause));

        Backendless.Data.of("user_lock").find(queryBuilder, new AsyncCallback<List<Map>>() {

            @Override
            public void handleResponse(List<Map> maps) {
                if (maps.size() != 0) {
                    new JSONArray();
                    Iterator var3 = maps.iterator();

                    while (var3.hasNext()) {
                        Map map = (Map) var3.next();
                        JSONObject mUniqueLock = new JSONObject();

                        try {
                            mUniqueLock.put("serial_number", Defaults.getLockSerialNumber(map));
                            mUniqueLock.put("name", map.get("name"));
                            mUniqueLock.put("wifi_name", Defaults.getValueFromLockObject(map, "wifi_name"));
                            mUniqueLock.put("admin_status", map.get("admin_status"));
                            mUniqueLock.put("lock_status", map.get("lock").toString());
                            mUniqueLock.put("door_status", Defaults.getDoorStatus(map));
                        } catch (JSONException var7) {
                            Log.e(this.getClass().getName(), var7.getMessage());
                        }
                    }
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e(fault.getCode(), fault.getMessage());
            }
        });
    }

    public static void addUserLockInLocal(Context context, JSONObject userlock) {
        try {
            JSONArray e = new JSONArray(
                    readSharedPreferenceObject(context, "locks", "").equals("")
                            ? "[]"
                            : readSharedPreferenceObject(context, "locks", ""));
            e.put(userlock);
            setValueInSharedPreferenceObject(context, "locks", e.toString());
        } catch (JSONException var3) {
            Log.e("Defaults", var3.getMessage());
        }

    }

    public static JSONObject getLockFromLocalWithSerialNumber(Context context, String serialnumber) {
        try {
            JSONArray e = new JSONArray(
                    readSharedPreferenceObject(context, "locks", "").equals("")
                            ? "[]"
                            : readSharedPreferenceObject(context, "locks", ""));

            for (int i = 0; i < e.length(); ++i) {
                JSONObject mLockObject = new JSONObject(e.get(i).toString());
                if (mLockObject.get("serial_number").toString().equals(serialnumber)) {
                    return mLockObject;
                }
            }
        } catch (JSONException e) {
            Log.e("Defaults", e.getMessage());
        }

        return null;
    }

    public static ArrayList<String> getSavedLocalWifiNames(Context context) {
        ArrayList mListOfWifiNames = new ArrayList();

        try {
            JSONArray e = new JSONArray(
                    readSharedPreferenceObject(context, "locks", "").equals("")
                            ? "[]"
                            : readSharedPreferenceObject(context, "locks", ""));

            for (int i = 0; i < e.length(); ++i) {
                mListOfWifiNames.add((new JSONObject(e.get(i).toString())).get("wifi_name").toString());
            }

            return mListOfWifiNames;
        } catch (JSONException var4) {
            Log.e("Defaults", var4.getMessage());
            return new ArrayList();
        }
    }

    public static ArrayList<JSONObject> getLockFromLocal(Context context) {
        try {
            ArrayList e = new ArrayList();
            JSONArray mListOfMyLocks = new JSONArray(
                    readSharedPreferenceObject(context, "locks", "").equals("")
                            ? "[]"
                            : readSharedPreferenceObject(context, "locks", ""));

            for (int i = 0; i < mListOfMyLocks.length(); ++i) {
                e.add(new JSONObject(mListOfMyLocks.get(i).toString()));
            }

            return e;
        } catch (JSONException var4) {
            Log.e("Defaults", var4.getMessage());
            return null;
        }
    }

    public static Boolean checkInternet() {
        Runtime runtime = Runtime.getRuntime();

        try {
            Process e = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = e.waitFor();
            return Boolean.valueOf(exitValue == 0);
        } catch (IOException var3) {
            var3.printStackTrace();
        } catch (InterruptedException var4) {
            var4.printStackTrace();
        }

        return false;
    }

    public static void setMobileDataEnabled(Context context, boolean enabled) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            Method methodSet = Class.forName(tm.getClass().getName()).getDeclaredMethod("setDataEnabled", Boolean.TYPE);
            methodSet.invoke(tm, enabled);
        } catch (Exception e) {
            Log.e("Defaults", e.toString());
        }
    }

    public static void setWifiEnabled(Context context, boolean enabled) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(enabled);
    }

    public static Boolean checkMobileDataOrWifiEnabled(Context context, int choice) {
        NetworkInfo activeNetwork = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
            if (choice == ConnectivityManager.TYPE_MOBILE && activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                return true;
            else if (choice == ConnectivityManager.TYPE_WIFI && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                return true;
        }

        return false;
    }

    public static Boolean checkAirPlaneMode(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0) != 0;
    }
}

