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

    private MainActivity.ViewPagerAdapter mAdapter;
    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    public MainActivity() {
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_main);
        Backendless.setUrl(Utilities.SERVER_URL);
        Backendless.initApp(this.getApplicationContext(), Utilities.APPLICATION_ID, Utilities.SECRET_KEY);
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mViewPager = findViewById(R.id.viewpager);
        setupViewPager(mViewPager);
        mTabLayout = findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        mAdapter = new MainActivity.ViewPagerAdapter(getSupportFragmentManager());
        mAdapter.addFragment(new LoginFragment(), this.getString(R.string.fragment_login));
        mAdapter.addFragment(new RegisterFragment(), this.getString(R.string.fragment_register));
        viewPager.setAdapter(mAdapter);
    }

    public void switchTab(int index, String mail) {
        EditText _edt_mail = findViewById(R.id.edt_mail);
        EditText _edt_login_password = findViewById(R.id.edt_login_password);
        CheckBox _chbx_remember = findViewById(R.id.chbx_remember);

        _edt_mail.setText(mail);
        _edt_login_password.setText(null);
        _chbx_remember.setChecked(false);

        Utilities.setValueInSharedPreferenceObject(this, Utilities.TABLE_USERS_COLUMN_EMAIL, mail);
        Utilities.setValueInSharedPreferenceObject(this, Utilities.TABLE_USERS_COLUMN_PASSWORD, getString(R.string.empty_phrase));

        Tab mTab = mTabLayout.getTabAt(index);
        mTab.select();
    }

    public void comeFromLogin() {
        startActivity(new Intent(this, LockActivity.class));
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
