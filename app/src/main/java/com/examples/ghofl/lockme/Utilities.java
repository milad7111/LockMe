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
import android.support.design.widget.Snackbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utilities {
    public static final String APPLICATION_ID = "43F16378-A01D-6283-FFE2-EBA6CE6C6300";
    public static final String SECRET_KEY = "A9F660C9-F062-D314-FF04-9E8DF5931E00";
    public static final String SERVER_URL = "https://api.backendless.com";

    public static final String TABLE_USERS = "users";
    public static final String TABLE_USERS_COLUMN_EMAIL = "email";
    public static final String TABLE_USERS_COLUMN_MOBILE_NUMBER = "mobile_number";
    public static final String TABLE_USERS_COLUMN_PASSWORD = "password";

    public static final String TABLE_LOCK = "lock";
    public static final String TABLE_LOCK_COLUMN_BATTERY_STATUS = "battery_charge";
    public static final String TABLE_LOCK_COLUMN_CONNECTION_STATUS = "connection_status";
    public static final String TABLE_LOCK_COLUMN_LOCK_SSID = "lock_ssid";
    public static final String TABLE_LOCK_COLUMN_DOOR_STATUS = "door_status";
    public static final String TABLE_LOCK_COLUMN_LOCK_STATUS = "lock_status";
    public static final String TABLE_LOCK_COLUMN_SERIAL_NUMBER = "serial_number";
    public static final String TABLE_LOCK_COLUMN_WIFI_STATUS = "wifi_status";
    public static final String TABLE_LOCK_COLUMN_MEAN_POWER_CONS = "mean_power_cons";

    public static final String TABLE_SERIAL_NUMBER = "serial_number";
    public static final String TABLE_SERIAL_NUMBER_COLUMN_CONFIGURATION = "configuration";
    public static final String TABLE_SERIAL_NUMBER_COLUMN_DATE_TIME = "date_time";
    public static final String TABLE_SERIAL_NUMBER_COLUMN_GENERATION = "generation";
    public static final String TABLE_SERIAL_NUMBER_COLUMN_MAC_ADDRESS = "mac_address";
    public static final String TABLE_SERIAL_NUMBER_COLUMN_NUMBER = "number";
    public static final String TABLE_SERIAL_NUMBER_COLUMN_SERIAL_NUMBER = "serial_number";

    public static final String TABLE_USER_LOCK = "user_lock";
    public static final String TABLE_USER_LOCK_COLUMN_SAVE_STATUS = "save_status";
    public static final String TABLE_USER_LOCK_COLUMN_ADMIN_STATUS = "admin_status";
    public static final String TABLE_USER_LOCK_COLUMN_LOCK = "lock";
    public static final String TABLE_USER_LOCK_COLUMN_LOCK_NAME = "lock_name";
    public static final String TABLE_USER_LOCK_COLUMN_USER = "user";

    public static final String SSID_FOR_CONNECT_ESP8266_TO_INTERNET = "needed_ssid";
    public static final String PASSWORD_FOR_CONNECT_ESP8266_TO_INTERNET = "needed_password";


    public Utilities() {
    }

    public static Boolean getRealLockStatus(Map map) {
        try {
            JSONObject mLockObject = new JSONObject();
            if ((new JSONObject(map)).get(TABLE_USER_LOCK_COLUMN_LOCK) instanceof JSONObject)
                mLockObject = new JSONObject((new JSONObject(map)).get(TABLE_USER_LOCK_COLUMN_LOCK).toString());
            else if ((new JSONObject(map)).get(TABLE_USER_LOCK_COLUMN_LOCK) instanceof JSONArray)
                mLockObject = (new JSONArray((new JSONObject(map)).get(TABLE_USER_LOCK_COLUMN_LOCK).toString())).getJSONObject(0);

            return mLockObject.getBoolean(TABLE_LOCK_COLUMN_LOCK_STATUS) && mLockObject.getBoolean(TABLE_LOCK_COLUMN_DOOR_STATUS);
        } catch (JSONException e) {
            Log.e("Utilities", e.getMessage());
            return null;
        }
    }

    public static Boolean getLockStatus(Map map) {
        try {
            return (new JSONObject(map)).getBoolean(TABLE_LOCK_COLUMN_LOCK_STATUS);
        } catch (JSONException e) {
            Log.e("Utilities", e.getMessage());
            return null;
        }
    }

    public static Boolean getDoorStatus(Map map) {
        try {
            return (new JSONObject(map)).getBoolean(TABLE_LOCK_COLUMN_DOOR_STATUS);
        } catch (JSONException e) {
            Log.e("Utilities", e.getMessage());
            return null;
        }
    }

    public static Boolean getConnectionStatus(Map map) {
        try {
            return (new JSONObject(map)).getBoolean(TABLE_LOCK_COLUMN_CONNECTION_STATUS);
        } catch (JSONException e) {
            Log.e("Utilities", e.getMessage());
            return null;
        }
    }

    public static int getBatteryStatus(Map map) {
        try {
            return ((new JSONObject(map)).getInt(TABLE_LOCK_COLUMN_BATTERY_STATUS));
        } catch (JSONException e) {
            Log.e("Utilities", e.getMessage());
            return -1;
        }
    }

    public static int getWifiStatus(Map map) {
        try {
            return ((new JSONObject(map)).getInt(TABLE_LOCK_COLUMN_WIFI_STATUS));
        } catch (JSONException e) {
            Log.e("Utilities", e.getMessage());
            return -1;
        }
    }

    public static String getValueFromLockObject(Map map, String parameter) {
        try {
            JSONObject mLockObject = new JSONObject();
            if ((new JSONObject(map)).get(TABLE_USER_LOCK_COLUMN_LOCK) instanceof JSONObject) {
                mLockObject = new JSONObject((new JSONObject(map)).get(TABLE_USER_LOCK_COLUMN_LOCK).toString());
            } else if ((new JSONObject(map)).get(TABLE_USER_LOCK_COLUMN_LOCK) instanceof JSONArray) {
                mLockObject = (new JSONArray((new JSONObject(map)).get(TABLE_USER_LOCK_COLUMN_LOCK).toString())).getJSONObject(0);
            }

            return mLockObject.getString(parameter);
        } catch (JSONException e) {
            Log.e("Utilities", e.getMessage());
            return null;
        }
    }

    public static String getLockSerialNumber(Map map) {
        try {
            return (new JSONObject(getValueFromLockObject(map, TABLE_LOCK_COLUMN_SERIAL_NUMBER))).get(TABLE_SERIAL_NUMBER_COLUMN_SERIAL_NUMBER).toString();
        } catch (JSONException e) {
            Log.e("Utilities", e.getMessage());
            return null;
        }
    }

    public static String readSharedPreferenceObject(Context context, String object, String default_value) {
        SharedPreferences mSharedPreferences = context.getSharedPreferences(context.getString(R.string.shared_preference_name), 0);
        return mSharedPreferences.getString(object, default_value);
    }

    public static void setValueInSharedPreferenceObject(Context context, String object, String value) {
        SharedPreferences mSharedPreferences = context.getSharedPreferences(context.getString(R.string.shared_preference_name), 0);
        Editor mEditor = mSharedPreferences.edit();
        mEditor.putString(object, value);
        mEditor.apply();
    }

    public static Boolean hasAdminLock(Context context) {
        return Integer.parseInt(readSharedPreferenceObject(context,
                context.getString(R.string.share_preference_parameter_number_of_admin_lock),
                "0")) > 0
                ? true
                : false;
    }

    public static void syncDataBetweenLocalAndServer(Context context) {

        ArrayList<JSONObject> mNotSavedLocalUserLocksArrayList = checkExistenceUnsavedLocalLocksInServer(getLockFromLocal(context));

        if (mNotSavedLocalUserLocksArrayList.size() != 0)
            saveMultiUserLocksInServer(context, mNotSavedLocalUserLocksArrayList);
    }

    public static void addUserLockInLocal(Context context, JSONObject userlock) {
        try {
            JSONArray mUserLocks = new JSONArray(
                    readSharedPreferenceObject(context, "locks", "").equals("")
                            ? "[]"
                            : readSharedPreferenceObject(context, "locks", ""));
            mUserLocks.put(userlock);
            setValueInSharedPreferenceObject(context, "locks", mUserLocks.toString());

        } catch (JSONException e) {
            Log.e("Utilities", e.getMessage());
        }
    }

    public static JSONObject getLockFromLocalWithSerialNumber(Context context, String serialnumber) {
        try {
            JSONArray mLocalSavedUserLocks = new JSONArray(
                    readSharedPreferenceObject(context, "locks", "").equals("")
                            ? "[]"
                            : readSharedPreferenceObject(context, "locks", ""));

            for (int i = 0; i < mLocalSavedUserLocks.length(); ++i) {
                JSONObject mLockObject = new JSONObject(mLocalSavedUserLocks.get(i).toString());
                if (mLockObject.get("serial_number").toString().equals(serialnumber)) {
                    return mLockObject;
                }
            }
        } catch (JSONException e) {
            Log.e("Utilities", e.getMessage());
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

            for (int i = 0; i < e.length(); ++i)
                mListOfWifiNames.add((new JSONObject(e.get(i).toString())).get(TABLE_LOCK_COLUMN_LOCK_SSID).toString());

            return mListOfWifiNames;
        } catch (JSONException e) {
            Log.e("Utilities", e.getMessage());
            return new ArrayList();
        }
    }

    public static ArrayList<JSONObject> getLockFromLocal(Context context) {
        try {
            ArrayList mLockList = new ArrayList();
            JSONArray mListOfMyLocks = new JSONArray(
                    readSharedPreferenceObject(context, "locks", "").equals("")
                            ? "[]"
                            : readSharedPreferenceObject(context, "locks", ""));

            for (int i = 0; i < mListOfMyLocks.length(); ++i) {
                mLockList.add(new JSONObject(mListOfMyLocks.get(i).toString()));
            }

            return mLockList;
        } catch (JSONException e) {
            Log.e("Utilities", e.getMessage());
            return null;
        }
    }

    public static void setMobileDataEnabled(Context context, boolean enabled) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            Method methodSet = Class.forName(tm.getClass().getName()).getDeclaredMethod("setDataEnabled", Boolean.TYPE);
            methodSet.invoke(tm, enabled);
        } catch (Exception e) {
            Log.e("Utilities", e.toString());
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

    public static ArrayList<String> parseESPAvailableNetworksResponse(String response) {
        try {
            JSONObject mResponse = new JSONObject(response);

            JSONArray mResponseNetworksSSIDJSONArray = mResponse.getJSONArray("networksSSID");
            ArrayList<String> mNetworksSSIDArrayList = new ArrayList<>();
            for (int i = 0; i < mResponseNetworksSSIDJSONArray.length(); i++)
                mNetworksSSIDArrayList.add(mResponseNetworksSSIDJSONArray.get(i).toString());

            return mNetworksSSIDArrayList;

        } catch (JSONException e) {
            Log.e("Utilities", e.getMessage());

            return new ArrayList<>();
        }
    }

    public static void showSnackBarMessage(View view, String message, int duration) {
        final Snackbar mSnackBar = Snackbar.make(view, message, duration);
        mSnackBar.setAction(R.string.dialog_button_confirm, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSnackBar.dismiss();
            }
        }).show();
    }

    private static ArrayList<JSONObject> checkExistenceUnsavedLocalLocksInServer(ArrayList<JSONObject> local_locks) {
        ArrayList<JSONObject> mUnsavedLocks = new ArrayList<>();

        try {
            for (int i = 0; i < local_locks.size(); i++)
                if (!local_locks.get(i).getBoolean(TABLE_USER_LOCK_COLUMN_SAVE_STATUS))
                    mUnsavedLocks.add(local_locks.get(i));

            return mUnsavedLocks;
        } catch (JSONException e) {
            Log.e("Utilities", e.getMessage());
        }

        return new ArrayList<>();
    }

    public static void saveMultiUserLocksInServer(final Context context, ArrayList<JSONObject> user_locks) {
        List<Map> mUserLockMapList = new ArrayList<>();

        try {
            for (int i = 0; i < user_locks.size(); i++) {
                Map<String, Object> mUserLockMapObject = new HashMap<>();

                mUserLockMapObject.put(TABLE_USER_LOCK_COLUMN_LOCK_NAME, user_locks.get(i).getString(TABLE_USER_LOCK_COLUMN_LOCK_NAME));
                //because just admin locks may be exists in local but not in server
                mUserLockMapObject.put(TABLE_USER_LOCK_COLUMN_ADMIN_STATUS, true);
                mUserLockMapObject.put(TABLE_USER_LOCK_COLUMN_USER, Backendless.UserService.CurrentUser());

                LockClass mLockObject = new LockClass();
                mLockObject.setSerialNumber(user_locks.get(i).getString(TABLE_LOCK_COLUMN_SERIAL_NUMBER));
                //Write a service on server for insert in userlock, get serial number and return lock to add as relation in user lock

                mUserLockMapObject.put(TABLE_USER_LOCK_COLUMN_LOCK, mLockObject);

                mUserLockMapList.add(mUserLockMapObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Backendless.Data.of(TABLE_USER_LOCK).create(mUserLockMapList, new AsyncCallback<List<String>>() {

            @Override
            public void handleResponse(List<String> response) {
                Log.e("Utilities", response.toString());

                //I assume all user locks saved on server in right way
                setSaveStatusTrueInLocalForAllLocks(context);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e("Utilities", fault.getMessage());
            }
        });
    }

    private static void setSaveStatusTrueInLocalForAllLocks(Context context) {
        try {
            JSONArray mLocalSavedUserLocks = new JSONArray(
                    readSharedPreferenceObject(context, "locks", "").equals("")
                            ? "[]"
                            : readSharedPreferenceObject(context, "locks", ""));

            for (int i = 0; i < mLocalSavedUserLocks.length(); i++)
                mLocalSavedUserLocks.put(i, (new JSONObject(mLocalSavedUserLocks.get(i).toString())).put(TABLE_USER_LOCK_COLUMN_ADMIN_STATUS, true));

            setValueInSharedPreferenceObject(context, "locks", mLocalSavedUserLocks.toString());
        } catch (JSONException e) {
            Log.e("Utilities", e.getMessage());
        }
    }

    public static void setSaveStatusTrueInLocalForALock(Context context, String serial_number) {
        try {
            JSONArray mLocalSavedUserLocks = new JSONArray(
                    readSharedPreferenceObject(context, "locks", "").equals("")
                            ? "[]"
                            : readSharedPreferenceObject(context, "locks", ""));

            for (int i = 0; i < mLocalSavedUserLocks.length(); i++)
                if (new JSONObject(mLocalSavedUserLocks.get(i).toString()).getString(TABLE_LOCK_COLUMN_SERIAL_NUMBER).equals(serial_number)) {
                    mLocalSavedUserLocks.put(i, (new JSONObject(mLocalSavedUserLocks.get(i).toString())).put(TABLE_USER_LOCK_COLUMN_ADMIN_STATUS, true));
                    break;
                }

            setValueInSharedPreferenceObject(context, "locks", mLocalSavedUserLocks.toString());
        } catch (JSONException e) {
            Log.e("Utilities", e.getMessage());
        }
    }
}

