package com.examples.ghofl.lockme;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

public class LoginFragment extends Fragment {

    private TextInputEditText _edt_mail;
    private TextInputEditText _edt_login_password;
    private CheckBox _chbx_remember;
    private Button _btn_login;
    private Button _btn_skip_login;
    private ProgressBar _prg_login;
    private String mEmail;
    private String mPassword;
    private Snackbar mSnackbar;

    public LoginFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_fragment_login, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        _edt_mail = view.findViewById(R.id.edt_mail);
        _edt_login_password = view.findViewById(R.id.edt_login_password);
        _chbx_remember = view.findViewById(R.id.chbx_remember);
        _btn_login = view.findViewById(R.id.btn_login);
        _btn_skip_login = view.findViewById(R.id.btn_skip_login);
        _prg_login = view.findViewById(R.id.prg_login);

        readMailAndPasswordFromSharedPreference();
        initializeLoginViews();

        _btn_login.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                _prg_login.setVisibility(View.VISIBLE);
                checkInternetConnection();
            }
        });

        _btn_skip_login.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (Utilities.hasAdminLock(getActivity())) {
                    try {
                        mSnackbar.dismiss();
                    } catch (Exception e) {
                        Log.e(getTag(), e.getMessage());
                    }

                    Backendless.UserService.logout(new AsyncCallback<Void>() {
                        @Override
                        public void handleResponse(Void response) {
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            Log.e(getTag(), fault.getMessage());
                        }
                    });

                    ((MainActivity) getActivity()).comeFromLogin();
                } else
                    _btn_skip_login.setVisibility(View.INVISIBLE);
            }
        });
    }

    public void onStart() {
        super.onStart();
        readMailAndPasswordFromSharedPreference();
        initializeLoginViews();
        setVisibilityOfSkipButtonDependsOnInternetConnection();
    }

    public void onResume() {
        super.onResume();

        setVisibilityOfSkipButtonDependsOnInternetConnection();
        if (!Utilities.checkMobileDataOrWifiEnabled(getActivity().getBaseContext(), ConnectivityManager.TYPE_WIFI) &&
                !Utilities.checkMobileDataOrWifiEnabled(getActivity().getBaseContext(), ConnectivityManager.TYPE_MOBILE))
            requestConnectToNetworkOrDataMobile();
    }

    private void setVisibilityOfSkipButtonDependsOnInternetConnection() {
        if (Utilities.hasAdminLock(getActivity()))
            _btn_skip_login.setVisibility(View.VISIBLE);
        else
            _btn_skip_login.setVisibility(View.INVISIBLE);

        if (!Utilities.checkMobileDataOrWifiEnabled(getActivity().getBaseContext(), ConnectivityManager.TYPE_WIFI)) {
            Utilities.setWifiEnabled(getActivity().getBaseContext(), true);
            setVisibilityOfSkipButtonDependsOnInternetConnection();
            Log.e(getTag(), getString(R.string.message_wifi_is_off));
        }
    }

    private void readMailAndPasswordFromSharedPreference() {
        mEmail = Utilities.readSharedPreferenceObject(getActivity(), Utilities.TABLE_USERS_COLUMN_EMAIL, getString(R.string.empty_phrase));
        mPassword = Utilities.readSharedPreferenceObject(getActivity(), Utilities.TABLE_USERS_COLUMN_PASSWORD, getString(R.string.empty_phrase));
    }

    private void requestConnectToNetworkOrDataMobile() {
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle(getString(R.string.dialog_connect_network));
        alertDialog.setMessage(getString(R.string.dialog_choose_how_to_connect_internet));
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.dialog_button_data),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Utilities.setMobileDataEnabled(getActivity(), true);
                        setVisibilityOfSkipButtonDependsOnInternetConnection();
                        dialog.dismiss();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.dialog_button_wifi_network),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Utilities.setWifiEnabled(getActivity().getBaseContext(), true);
                        Utilities.setWifiEnabled(getActivity(), true);
                        setVisibilityOfSkipButtonDependsOnInternetConnection();
                        dialog.dismiss();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dialog_button_discard),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        setVisibilityOfSkipButtonDependsOnInternetConnection();
                        try {
                            mSnackbar = Utilities.showSnackBarMessage(
                                    getView(),
                                    getString(R.string.snackbar_message_offline),
                                    Snackbar.LENGTH_INDEFINITE);
                            mSnackbar.show();
                        } catch (Exception e) {
                            Log.e(getTag(), e.getMessage());
                        }
                        dialog.dismiss();
                    }
                });

        alertDialog.show();
    }

    private void initializeLoginViews() {
        if (!mEmail.equals(getString(R.string.empty_phrase))) {
            _edt_mail.setText(mEmail);
            if (!mPassword.equals(getString(R.string.empty_phrase))) {
                _edt_login_password.setText(mPassword);
                _chbx_remember.setChecked(true);
            }
        }
    }

    private void checkInternetConnection() {
        if (Utilities.checkMobileDataOrWifiEnabled(getActivity().getBaseContext(), ConnectivityManager.TYPE_WIFI)) {
            RequestQueue MyRequestQueue = Volley.newRequestQueue(getActivity().getBaseContext());
            String url = getString(R.string.backendless_base_http_url);
            StringRequest MyStringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener() {
                @Override
                public void onResponse(Object response) {
                    Log.e(getTag(), response.toString());

                    Backendless.UserService.login(
                            _edt_mail.getText().toString(),
                            _edt_login_password.getText().toString(), new AsyncCallback() {

                                @Override
                                public void handleResponse(Object response) {
                                    _prg_login.setVisibility(View.INVISIBLE);

                                    try {
                                        Utilities.syncDataBetweenLocalAndServer(getActivity().getBaseContext());
                                        Utilities.setValueInSharedPreferenceObject(getActivity(), Utilities.TABLE_USERS_COLUMN_EMAIL,
                                                _edt_mail.getText().toString());

                                        if (_chbx_remember.isChecked())
                                            Utilities.setValueInSharedPreferenceObject(getActivity(), Utilities.TABLE_USERS_COLUMN_PASSWORD, _edt_login_password.getText().toString());
                                        else
                                            Utilities.setValueInSharedPreferenceObject(getActivity(), Utilities.TABLE_USERS_COLUMN_PASSWORD, getString(R.string.empty_phrase));

                                        try {
                                            mSnackbar.dismiss();
                                        } catch (Exception e) {
                                            Log.e(getTag(), e.getMessage());
                                        }
                                        ((MainActivity) getActivity()).comeFromLogin();
                                    } catch (Exception e) {
                                        Log.e(getTag(), e.getMessage());
                                    }
                                }

                                @Override
                                public void handleFault(BackendlessFault fault) {
                                    Log.e(getTag(), fault.getMessage());

                                    try {
                                        _prg_login.setVisibility(View.INVISIBLE);
                                        if (fault.getCode().equals("3003") || fault.getCode().equals("IllegalArgumentException")) {

                                            //region Hide keyboard
                                            View view = getActivity().getCurrentFocus();
                                            if (view != null) {
                                                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                            }
                                            //endregion Hide keyboard

                                            Utilities.showSnackBarMessage(getView(), fault.getMessage(), Snackbar.LENGTH_INDEFINITE).show();

                                        } else
                                            requestConnectToNetworkOrDataMobile();
                                    } catch (Exception e) {
                                        Log.e(getTag(), e.getMessage());
                                    }
                                }
                            });
                }
            }, new Response.ErrorListener() {
                public void onErrorResponse(VolleyError error) {
                    Log.e(getTag(), error.toString());
                    mSnackbar = Utilities.showSnackBarMessage(
                            getView(),
                            getString(R.string.snackbar_message_offline),
                            Snackbar.LENGTH_INDEFINITE);
                    mSnackbar.show();
                    _prg_login.setVisibility(View.INVISIBLE);
                }
            });
            MyRequestQueue.add(MyStringRequest);
        } else {
            try {
                Utilities.setWifiEnabled(getActivity().getBaseContext(), true);
                Log.e(getTag(), getString(R.string.message_wifi_is_off));
                Utilities.showSnackBarMessage(
                        getView(),
                        getString(R.string.message_wifi_off_wait),
                        Snackbar.LENGTH_LONG).show();
                _prg_login.setVisibility(View.INVISIBLE);
            } catch (Exception e) {
                Log.e(getTag(), e.getMessage());
            }
        }
    }
}

