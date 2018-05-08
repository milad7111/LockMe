package com.examples.ghofl.lockme;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

public class RegisterFragment extends Fragment {
    EditText _edt_register_mail;
    EditText _edt_register_mobile_number;
    EditText _edt_register_password;
    EditText _edt_repeat_password;
    Button _btn_register;
    ProgressBar _prg_register;

    public RegisterFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_fragment_register, container, false);
        _edt_register_mail = rootView.findViewById(R.id.edt_register_mail);
        _edt_register_mobile_number = rootView.findViewById(R.id.edt_register_mobile_number);
        _edt_register_password = rootView.findViewById(R.id.edt_register_password);
        _edt_repeat_password = rootView.findViewById(R.id.edt_repeat_password);
        _btn_register = rootView.findViewById(R.id.btn_register);
        _prg_register = rootView.findViewById(R.id.prg_resgister);

        _btn_register.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                checkInternetConnection();
            }
        });

        return rootView;
    }

    private void checkInternetConnection() {
        RequestQueue MyRequestQueue = Volley.newRequestQueue(getActivity().getBaseContext());
        String url = getString(R.string.backendless_base_http_url);
        StringRequest MyStringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener() {
            @Override
            public void onResponse(Object response) {
                Log.e(getTag(), response.toString());

                if (!_edt_register_password.getText().toString().equals(_edt_repeat_password.getText().toString()))
                    Utilities.showSnackBarMessage(getView(), getString(R.string.toast_password_not_match), Snackbar.LENGTH_LONG).show();
                else {
                    BackendlessUser user = new BackendlessUser();
                    user.setEmail(_edt_register_mail.getText().toString());
                    user.setPassword(_edt_register_password.getText().toString());

                    String mMobileNumber = _edt_register_mobile_number.getText().toString();
                    if (!mMobileNumber.isEmpty())
                        user.setProperty(Utilities.TABLE_USERS_COLUMN_MOBILE_NUMBER, mMobileNumber);

                    _prg_register.setVisibility(View.VISIBLE);
                    Backendless.UserService.register(user, new AsyncCallback() {

                        @Override
                        public void handleResponse(Object response) {
                            _prg_register.setVisibility(View.INVISIBLE);

                            Utilities.showSnackBarMessage(getView(), getString(R.string.toast_user_registered), Snackbar.LENGTH_LONG).show();
                            ((MainActivity) getActivity()).switchTab(0, _edt_register_mail.getText().toString());

                            _edt_register_mail.setText(null);
                            _edt_register_mobile_number.setText(null);
                            _edt_register_password.setText(null);
                            _edt_repeat_password.setText(null);
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            _prg_register.setVisibility(View.INVISIBLE);
                            Log.e(getTag(), fault.getMessage());
                        }
                    });
                }
            }
        }, new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Log.e(this.getClass().getName(), error.toString());

                requestConnectToNetworkOrDataMobile();
            }
        });
        MyRequestQueue.add(MyStringRequest);
    }

    private void requestConnectToNetworkOrDataMobile() {
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle(getString(R.string.dialog_connect_network));
        alertDialog.setMessage(getString(R.string.dialog_choose_how_to_connect_internet));
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.dialog_button_data),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Utilities.setMobileDataEnabled(getActivity(), true);
                        dialog.dismiss();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.dialog_button_wifi_network),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Utilities.setWifiEnabled(getActivity(), true);
                        dialog.dismiss();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dialog_button_discard),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        alertDialog.show();
    }
}

