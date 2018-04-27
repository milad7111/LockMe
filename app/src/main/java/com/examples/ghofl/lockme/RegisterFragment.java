package com.examples.ghofl.lockme;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

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
                if (!_edt_register_password.getText().toString().equals(_edt_repeat_password.getText().toString()))
                    Utilities.showSnackBarMessage(getView(), getString(R.string.toast_password_not_match), Snackbar.LENGTH_LONG);
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

                            Utilities.showSnackBarMessage(getView(), getString(R.string.toast_user_registered), Snackbar.LENGTH_LONG);
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
        });

        return rootView;
    }
}

