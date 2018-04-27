package com.examples.ghofl.lockme;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.os.Bundle;
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
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

public class LoginFragment extends Fragment {
    private EditText _edt_mail;
    private EditText _edt_login_password;
    private CheckBox _chbx_remember;
    private Button _btn_login;
    private Button _btn_skip_login;
    private ProgressBar _prg_login;
    private String mEmail;
    private String mPassword;

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
        initializeLoginViews();
        setVisibilityOfSkipButtonDependsOnInternetConnection();

        _btn_login.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                _prg_login.setVisibility(View.VISIBLE);
                Backendless.UserService.login(
                        _edt_mail.getText().toString(),
                        _edt_login_password.getText().toString(), new AsyncCallback() {

                            @Override
                            public void handleResponse(Object response) {
                                _prg_login.setVisibility(View.INVISIBLE);

                                Utilities.syncDataBetweenLocalAndServer(getActivity().getBaseContext());

                                Utilities.setValueInSharedPreferenceObject(getActivity(), getString(R.string.share_preference_parameter_mail),
                                        _edt_mail.getText().toString());

                                if (_chbx_remember.isChecked())
                                    Utilities.setValueInSharedPreferenceObject(getActivity(), getString(R.string.share_preference_parameter_password), _edt_login_password.getText().toString());
                                else
                                    Utilities.setValueInSharedPreferenceObject(getActivity(), getString(R.string.share_preference_parameter_password), getString(R.string.empty_phrase));

                                ((MainActivity) getActivity()).comeFromLogin();
                            }

                            @Override
                            public void handleFault(BackendlessFault fault) {
                                _prg_login.setVisibility(View.INVISIBLE);
                                Log.e(getTag(), fault.getMessage());

                                if (fault.getCode().equals("3003") || fault.getCode().equals("IllegalArgumentException")) {

                                    //region Hide keyboard
                                    View view = getActivity().getCurrentFocus();
                                    if (view != null) {
                                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                    }
                                    //endregion Hide keyboard

                                    Utilities.showSnackBarMessage(getView(), fault.getMessage(), Snackbar.LENGTH_INDEFINITE);

                                } else
                                    requestConnectToNetworkOrDataMobile();
                            }
                        });
            }
        });

        _btn_skip_login.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (Utilities.hasAdminLock(getActivity()))
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
        initializeLoginViews();
    }

    public void onResume() {
        super.onResume();

        if (!Utilities.checkMobileDataOrWifiEnabled(getActivity().getBaseContext(), ConnectivityManager.TYPE_WIFI) &&
                !Utilities.checkMobileDataOrWifiEnabled(getActivity().getBaseContext(), ConnectivityManager.TYPE_MOBILE))
            requestConnectToNetworkOrDataMobile();
        else
            setVisibilityOfSkipButtonDependsOnInternetConnection();
    }

    private void setVisibilityOfSkipButtonDependsOnInternetConnection() {
        RequestQueue MyRequestQueue = Volley.newRequestQueue(getActivity().getBaseContext());
        String url = getString(R.string.backendless_base_http_url);
        StringRequest MyStringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener() {
            @Override
            public void onResponse(Object response) {
                Log.e(getTag(), response.toString());
                Log.e(getTag(), getString(R.string.log_connected_to_internet));
            }
        }, new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                if (Utilities.hasAdminLock(getActivity()))
                    _btn_skip_login.setVisibility(View.VISIBLE);
                else
                    _btn_skip_login.setVisibility(View.INVISIBLE);

                Log.e(getTag(), error.toString());
            }
        });
        MyRequestQueue.add(MyStringRequest);
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
                        Utilities.setWifiEnabled(getActivity(), true);
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

    private void initializeLoginViews() {
        if (!mEmail.equals(getString(R.string.empty_phrase))) {
            _edt_mail.setText(mEmail);
            if (!mPassword.equals(getString(R.string.empty_phrase))) {
                _edt_login_password.setText(mPassword);
                _chbx_remember.setChecked(true);
            }
        }
    }
}

