package com.examples.ghofl.lockme;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.backendless.Backendless;
import com.backendless.Subscription;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.messaging.Message;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

public class LoginFragment extends Fragment {
    private EditText _edt_mail;
    private EditText _edt_login_password;
    private CheckBox _chbx_remember;
    private Button _btn_login;
    private Button _btn_skip_login;
    private ProgressBar _prg_login;
    private String mail;
    private String password;

    public LoginFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_fragment_login, container, false);

        _edt_mail = rootView.findViewById(R.id.edt_mail);
        _edt_login_password = rootView.findViewById(R.id.edt_login_password);
        _chbx_remember = rootView.findViewById(R.id.chbx_remember);
        _btn_login = rootView.findViewById(R.id.btn_login);
        _btn_skip_login = rootView.findViewById(R.id.btn_skip_login);
        _prg_login = rootView.findViewById(R.id.prg_login);

        readMailAndPasswordFromSharedPreference();

        if (!mail.equals(getString(R.string.empty_phrase))) {
            _edt_mail.setText(mail);
            if (!password.equals(getString(R.string.empty_phrase))) {
                _edt_login_password.setText(password);
                _chbx_remember.setChecked(true);
            }
        }

        _btn_login.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                _prg_login.setVisibility(View.VISIBLE);
                Backendless.UserService.login(
                        _edt_mail.getText().toString(),
                        _edt_login_password.getText().toString(), new AsyncCallback() {

                            @Override
                            public void handleResponse(Object response) {
                                _prg_login.setVisibility(View.INVISIBLE);
                                Defaults.setValueInSharedPreferenceObject(getActivity(), getString(R.string.share_preference_parameter_mail),
                                        _edt_mail.getText().toString());

                                if (_chbx_remember.isChecked()) {
                                    Defaults.setValueInSharedPreferenceObject(getActivity(), getString(R.string.share_preference_parameter_password), _edt_login_password.getText().toString());
                                } else
                                    Defaults.setValueInSharedPreferenceObject(getActivity(), getString(R.string.share_preference_parameter_password),
                                            getString(R.string.empty_phrase));


                                ((MainActivity) getActivity()).comeFromLogin();
                            }

                            @Override
                            public void handleFault(BackendlessFault fault) {
                                _prg_login.setVisibility(View.INVISIBLE);
                                Log.e(getTag(), fault.getMessage());

                                if (fault.getCode().equals("3003") || fault.getCode().equals("IllegalArgumentException")) {

                                    View view = getActivity().getCurrentFocus();
                                    if (view != null) {
                                        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                    }

                                    final Snackbar mBackendlessErrorSnackBar = Snackbar.make(getView(),
                                            fault.getMessage(), Snackbar.LENGTH_INDEFINITE);
                                    mBackendlessErrorSnackBar.setAction(R.string.dialog_button_confirm, new OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            mBackendlessErrorSnackBar.dismiss();
                                        }
                                    }).show();
                                } else
                                    requestConnectToNetworkOrDataMobile();
                            }
                        });
            }
        });

        _btn_skip_login.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (Defaults.hasAdminLock(getActivity()))
                    ((MainActivity) getActivity()).comeFromLogin();
                else
                    _btn_skip_login.setVisibility(View.INVISIBLE);
            }
        });

        return rootView;
    }

    public void onStart() {
        super.onStart();
        readMailAndPasswordFromSharedPreference();
        if (!mail.equals(getString(R.string.empty_phrase))) {
            _edt_mail.setText(mail);
            if (!password.equals(getString(R.string.empty_phrase))) {
                _edt_login_password.setText(password);
                _chbx_remember.setChecked(true);
            }
        }
    }

    public void onResume() {
        super.onResume();

        if (!Defaults.checkMobileDataOrWifiEnabled(getActivity().getBaseContext(), ConnectivityManager.TYPE_WIFI) &&
                !Defaults.checkMobileDataOrWifiEnabled(getActivity().getBaseContext(), ConnectivityManager.TYPE_MOBILE))
            requestConnectToNetworkOrDataMobile();
        else
            setVisibilityOfSkipButtonDependsOnInternetConnection();
    }

    private void setVisibilityOfSkipButtonDependsOnInternetConnection() {
        RequestQueue MyRequestQueue = Volley.newRequestQueue(getActivity().getBaseContext());
        String url = getString(R.string.dummy_http_url);
        StringRequest MyStringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener() {
            @Override
            public void onResponse(Object response) {
                Log.e(getTag(), response.toString() + getString(R.string.log_connected_to_internet));
            }
        }, new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                if (Defaults.hasAdminLock(getActivity()))
                    _btn_skip_login.setVisibility(View.VISIBLE);
                else
                    _btn_skip_login.setVisibility(View.INVISIBLE);
                Log.e(getTag(), error.getMessage());
            }
        });
        MyRequestQueue.add(MyStringRequest);
    }

    private void readMailAndPasswordFromSharedPreference() {
        mail = Defaults.readSharedPreferenceObject(getActivity(), getString(R.string.share_preference_parameter_mail), getString(R.string.empty_phrase));
        password = Defaults.readSharedPreferenceObject(getActivity(), getString(R.string.share_preference_parameter_password), getString(R.string.empty_phrase));
    }

    private void requestConnectToNetworkOrDataMobile() {
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle(getString(R.string.dialog_connect_network));
        alertDialog.setMessage(getString(R.string.dialog_choose_how_to_connect_internet));
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.dialog_button_data),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Defaults.setMobileDataEnabled(getActivity(), true);
                        setVisibilityOfSkipButtonDependsOnInternetConnection();
                        dialog.dismiss();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.dialog_button_wifi_network),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Defaults.setWifiEnabled(getActivity(), true);
                        setVisibilityOfSkipButtonDependsOnInternetConnection();
                        dialog.dismiss();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dialog_button_discard),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        setVisibilityOfSkipButtonDependsOnInternetConnection();
                        dialog.dismiss();
                    }
                });

        alertDialog.show();
    }
}

