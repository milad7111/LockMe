package com.examples.ghofl.lockme;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.Tab;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.CheckBox;
import android.widget.EditText;

import com.backendless.Backendless;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private MainActivity.ViewPagerAdapter adapter;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    public MainActivity() {
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_main);
        Backendless.setUrl("https://api.backendless.com");
        Backendless.initApp(this.getApplicationContext(), "43F16378-A01D-6283-FFE2-EBA6CE6C6300", "A9F660C9-F062-D314-FF04-9E8DF5931E00");
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        viewPager = findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new MainActivity.ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new LoginFragment(), this.getString(R.string.fragment_login));
        adapter.addFragment(new RegisterFragment(), this.getString(R.string.fragment_register));
        viewPager.setAdapter(adapter);
    }

    public void switchTab(int index, String mail) {
        EditText _edt_mail = findViewById(R.id.edt_mail);
        _edt_mail.setText(mail);
        EditText _edt_login_password = findViewById(R.id.edt_login_password);
        _edt_login_password.setText(null);
        CheckBox _chbx_remember = findViewById(R.id.chbx_remember);
        _chbx_remember.setChecked(false);
        Defaults.setValueInSharedPreferenceObject(this, this.getString(R.string.share_preference_parameter_mail), mail);
        Defaults.setValueInSharedPreferenceObject(this, this.getString(R.string.share_preference_parameter_password), this.getString(R.string.empty_phrase));
        Tab tab = tabLayout.getTabAt(index);
        tab.select();
    }

    public void comeFromLogin() {
//        Intent mLockActivity = new Intent(this, LockActivity.class);
//        this.startActivity(mLockActivity);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList();
        private final List<String> mFragmentTitleList = new ArrayList();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
