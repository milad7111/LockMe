package com.examples.ghofl.lockme;

/**
 * Created by PHD on 4/19/2018.
 */

import android.annotation.SuppressLint;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utilities {
    public static final String APPLICATION_ID = "8178E63C-1936-5C74-FFC6-B90735872B00";
    public static final String SECRET_KEY = "BA7F64EC-A219-CA3F-FFB7-B81B1BBC5300";
    public static final String SERVER_URL = "http://api.backendless.com";

    public static final String TABLE_USERS_COLUMN_EMAIL = "email";
    public static final String TABLE_USERS_COLUMN_MOBILE_NUMBER = "mobile_number";
    public static final String TABLE_USERS_COLUMN_PASSWORD = "password";
    public static final String TABLE_USERS_COLUMN_RELATED_LOCKS = "related_locks";

    public static final String TABLE_LOCK = "Lock";
    public static final String TABLE_LOCK_COLUMN_RELATED_USERS = "related_users";
    public static final String TABLE_LOCK_COLUMN_BATTERY_STATUS = "battery_status";
    public static final String TABLE_LOCK_COLUMN_CONNECTION_STATUS = "connection_status";
    public static final String TABLE_LOCK_COLUMN_LOCK_SSID = "lock_ssid";
    public static final String TABLE_LOCK_COLUMN_DOOR_STATUS = "door_status";
    public static final String TABLE_LOCK_COLUMN_LOCK_STATUS = "lock_status";
    public static final String TABLE_LOCK_COLUMN_SERIAL_NUMBER = "serial_number";
    public static final String TABLE_LOCK_COLUMN_WIFI_STATUS = "wifi_status";

    public static final String TABLE_SERIAL_NUMBER = "SerialNumber";
    public static final String TABLE_SERIAL_NUMBER_COLUMN_CONFIGURATION = "configuration";
    public static final String TABLE_SERIAL_NUMBER_COLUMN_GENERATION = "generation";
    public static final String TABLE_SERIAL_NUMBER_COLUMN_MAC_ADDRESS = "mac_address";
    public static final String TABLE_SERIAL_NUMBER_COLUMN_NUMBER = "number";
    public static final String TABLE_SERIAL_NUMBER_COLUMN_SERIAL_NUMBER = "serial_number";

    public static final String TABLE_USER_LOCK = "UserLock";
    public static final String TABLE_USER_LOCK_COLUMN_SAVE_STATUS = "save_status";
    public static final String TABLE_USER_LOCK_COLUMN_ADMIN_STATUS = "admin_status";
    public static final String TABLE_USER_LOCK_COLUMN_LOCK_PLUS_USER = "lock_plus_user";
    public static final String TABLE_USER_LOCK_COLUMN_LOCK_NAME = "lock_name";

    public static final String SSID_FOR_CONNECT_ESP8266_TO_INTERNET = "needed_ssid";
    public static final String PASSWORD_FOR_CONNECT_ESP8266_TO_INTERNET = "needed_password";

    public Utilities() {
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

        } catch (Exception e) {
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
        } catch (Exception e) {
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
        } catch (Exception e) {
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
        } catch (Exception e) {
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

        } catch (Exception e) {
            Log.e("Utilities", e.getMessage());

            return new ArrayList<>();
        }
    }

    public static Snackbar showSnackBarMessage(View view, String message, int duration) {
        final Snackbar mSnackBar = Snackbar.make(view, message, duration);
        mSnackBar.setAction(R.string.dialog_button_confirm, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSnackBar.dismiss();
            }
        });

        return mSnackBar;
    }

    private static ArrayList<JSONObject> checkExistenceUnsavedLocalLocksInServer(ArrayList<JSONObject> local_locks) {
        ArrayList<JSONObject> mUnsavedLocks = new ArrayList<>();

        try {
            for (int i = 0; i < local_locks.size(); i++)
                if (!local_locks.get(i).getBoolean(TABLE_USER_LOCK_COLUMN_SAVE_STATUS))
                    mUnsavedLocks.add(local_locks.get(i));

            return mUnsavedLocks;
        } catch (Exception e) {
            Log.e("Utilities", e.getMessage());
        }

        return new ArrayList<>();
    }

    public static void saveMultiUserLocksInServer(final Context context, final ArrayList<JSONObject> user_locks) {

        try {
            for (int i = 0; i < user_locks.size(); i++) {

                final List<UserLock> mUserLockList = new ArrayList<>();
                final UserLock mUserLockObject = new UserLock();

                //because just admin locks may be exists in local but not in server
                mUserLockObject.setAdminStatus(true);
                mUserLockObject.setLockName(user_locks.get(i).getString(TABLE_USER_LOCK_COLUMN_LOCK_NAME));

                final String mSerialNumber = user_locks.get(i).getString(TABLE_LOCK_COLUMN_SERIAL_NUMBER);

                DataQueryBuilder queryBuilder = DataQueryBuilder.create();
                StringBuilder mWhereClause = new StringBuilder();
                mWhereClause
                        .append(Utilities.TABLE_LOCK_COLUMN_SERIAL_NUMBER)
                        .append(".")
                        .append(Utilities.TABLE_SERIAL_NUMBER_COLUMN_SERIAL_NUMBER)
                        .append("=\'")
                        .append(mSerialNumber)
                        .append("\'");

                queryBuilder.setWhereClause(String.valueOf(mWhereClause));
                Backendless.Data.of(Utilities.TABLE_LOCK).find(queryBuilder, new AsyncCallback<List<Map>>() {
                    public void handleResponse(final List<Map> maps) {
                        if (maps.size() != 0) {
                            mUserLockObject.setLockPlusUser(maps.get(0).get("objectId").toString(), Backendless.UserService.CurrentUser().getObjectId());

                            Backendless.Data.of(UserLock.class).save(mUserLockObject, new AsyncCallback<UserLock>() {
                                @Override
                                public void handleResponse(UserLock response) {
                                    mUserLockList.add(response);

                                    Backendless.Data.of(TABLE_LOCK).addRelation(maps.get(0),
                                            TABLE_LOCK_COLUMN_RELATED_USERS, mUserLockList,
                                            new AsyncCallback<Integer>() {
                                                @Override
                                                public void handleResponse(Integer response) {
                                                    Log.i("Utilities", response.toString());

                                                    Backendless.Data.of(BackendlessUser.class).addRelation(Backendless.UserService.CurrentUser(),
                                                            TABLE_USERS_COLUMN_RELATED_LOCKS, mUserLockList,
                                                            new AsyncCallback<Integer>() {
                                                                @Override
                                                                public void handleResponse(Integer response) {
                                                                    Log.i("Utilities", response.toString());

                                                                    setSaveStatusTrueInLocalForALock(context, mSerialNumber);
                                                                }

                                                                @Override
                                                                public void handleFault(BackendlessFault fault) {
                                                                    Log.i("Utilities", fault.getMessage());
                                                                }
                                                            });
                                                }

                                                @Override
                                                public void handleFault(BackendlessFault fault) {
                                                    Log.i("Utilities", fault.getMessage());
                                                }
                                            });
                                }

                                @Override
                                public void handleFault(BackendlessFault fault) {
                                    Log.i("Utilities", fault.getMessage());
                                }
                            });
                        } else {
                            Log.e("Utilities", "There is no Lock with serial number : " + mSerialNumber);
                        }
                    }

                    public void handleFault(BackendlessFault backendlessFault) {
                        Log.e(backendlessFault.getCode(), backendlessFault.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setStatusInLocalForALock(Context context, Lock lock) {
        try {
            JSONArray mLocalSavedUserLocks = new JSONArray(
                    readSharedPreferenceObject(context, "locks", "").equals("")
                            ? "[]"
                            : readSharedPreferenceObject(context, "locks", ""));

            for (int i = 0; i < mLocalSavedUserLocks.length(); i++) {
                mLocalSavedUserLocks.put(i, (new JSONObject(mLocalSavedUserLocks.get(i).toString())).put(
                        TABLE_LOCK_COLUMN_CONNECTION_STATUS, lock.getConnectionStatus()));

                mLocalSavedUserLocks.put(i, (new JSONObject(mLocalSavedUserLocks.get(i).toString())).put(
                        TABLE_LOCK_COLUMN_WIFI_STATUS, lock.getWifiStatus()));

                mLocalSavedUserLocks.put(i, (new JSONObject(mLocalSavedUserLocks.get(i).toString())).put(
                        TABLE_LOCK_COLUMN_LOCK_STATUS, lock.getLockStatus()));

                mLocalSavedUserLocks.put(i, (new JSONObject(mLocalSavedUserLocks.get(i).toString())).put(
                        TABLE_LOCK_COLUMN_DOOR_STATUS, lock.getDoorStatus()));

                mLocalSavedUserLocks.put(i, (new JSONObject(mLocalSavedUserLocks.get(i).toString())).put(
                        TABLE_LOCK_COLUMN_BATTERY_STATUS, lock.getBatteryStatus()));
            }

            setValueInSharedPreferenceObject(context, "locks", mLocalSavedUserLocks.toString());
        } catch (Exception e) {
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
                    mLocalSavedUserLocks.put(i, (new JSONObject(mLocalSavedUserLocks.get(i).toString())).put(TABLE_USER_LOCK_COLUMN_SAVE_STATUS, true));
                    break;
                }

            setValueInSharedPreferenceObject(context, "locks", mLocalSavedUserLocks.toString());
        } catch (Exception e) {
            Log.e("Utilities", e.getMessage());
        }
    }

    public static String createWhereClauseWithListOfObjectId(String related_column, String search_column, HashMap[] objects) {
        StringBuilder mWhereClause = new StringBuilder();

        for (int i = 0; i < objects.length; i++) {
            mWhereClause.append(related_column);
            mWhereClause.append(".");
            mWhereClause.append(search_column);
            mWhereClause.append("=\'");
            mWhereClause.append(objects[i].get(search_column));
            mWhereClause.append("\'");

            if (i + 1 != objects.length)
                mWhereClause.append(" or ");
        }

        return String.valueOf(mWhereClause);
    }

    @SuppressLint("ResourceType")
    public static void changeConnectionStatusInView(Boolean status, ImageView image_view) {
        if (status)
            image_view.setImageResource(R.drawable.ic_cloud_done);
        else
            image_view.setImageResource(R.drawable.ic_cloud_off_black_24dp);
    }

    @SuppressLint("ResourceType")
    public static void changeWifiStatusInView(int status, ImageView image_view) {
        switch (status) {
            case 1: {
                image_view.setImageResource(R.drawable.ic_signal_cellular_0_bar_black_24dp);
                break;
            }
            case 2: {
                image_view.setImageResource(R.drawable.ic_signal_cellular_2_bar_black_24dp);
                break;
            }
            case 3: {
                image_view.setImageResource(R.drawable.ic_signal_cellular_3_bar_black_24dp);
                break;

            }
            case 4: {
                image_view.setImageResource(R.drawable.ic_signal_cellular_4_bar_black_24dp);
                break;
            }
            default:
                break;
        }
    }

    @SuppressLint("ResourceType")
    public static void changeBatteryStatusInView(int status, ImageView image_view) {
        switch (status) {
            case 1: {
                image_view.setImageResource(R.drawable.ic_battery_20_black_24dp);
                break;
            }
            case 2: {
                image_view.setImageResource(R.drawable.ic_battery_50_black_24dp);
                break;
            }
            case 3: {
                image_view.setImageResource(R.drawable.ic_battery_80_black_24dp);
                break;
            }
            case 4: {
                image_view.setImageResource(R.drawable.ic_battery_charging_full_black_24dp);
                break;
            }
            default:
                break;
        }
    }

    @SuppressLint("ResourceType")
    public static void changeDoorStatusInView(Boolean status, ImageView image_view, TextView text_view, Boolean lock_status) {
        if (status) {
            image_view.setImageResource(R.drawable.ic_security_green_24dp);
            text_view.setText(R.string.door_is_close);
        } else {
            image_view.setImageResource(R.drawable.ic_security_black_24dp);
            text_view.setText(R.string.door_is_open_not_secure);

            if (lock_status) {
                image_view.setImageResource(R.drawable.ic_security_red_24dp);
                text_view.setText(R.string.door_is_open_attention_required);
            }
        }
    }

    public static void changeLockStatusInView(Boolean status, ImageView image_view, TextView text_view) {
        if (status) {
            image_view.setImageResource(R.drawable.ic_close_lock_48dp);
            if (text_view != null)
                text_view.setText(R.string.lock_is_secure);
        } else {
            image_view.setImageResource(R.drawable.ic_open_lock_48dp);
            if (text_view != null)
                text_view.setText(R.string.lock_is_not_secure);
        }
    }

    public static String checkConnectToAnyLockWifi(Context context) {
        WifiManager mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return mWifiManager.getConnectionInfo().getSSID().toString();
    }

    public static void sleepSomeTime(int time) {
        try {
            Thread.sleep(time);
        } catch (Exception e) {
            Log.e("Utilities", e.getMessage());
        }
    }
}

