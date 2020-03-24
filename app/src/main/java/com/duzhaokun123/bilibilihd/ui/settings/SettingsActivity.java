package com.duzhaokun123.bilibilihd.ui.settings;

import androidx.fragment.app.Fragment;

import android.widget.FrameLayout;

import com.duzhaokun123.bilibilihd.R;
import com.duzhaokun123.bilibilihd.databinding.ActivitySettingsBinding;
import com.duzhaokun123.bilibilihd.ui.widget.BaseActivity;

public class SettingsActivity extends BaseActivity<ActivitySettingsBinding> {

    @Override
    protected int initConfig() {
        return 0;
    }

    @Override
    public int initLayout() {
        return R.layout.activity_settings;
    }

    @Override
    public void initView() {
        Fragment mFragmentSettingFirst = new SettingsMainFragment();

        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .add(R.id.fl_settings_first, mFragmentSettingFirst, "main")
                .commitAllowingStateLoss();
    }

    @Override
    public void initData() {

    }

    public FrameLayout getFlSettingSecond() {
        return baseBind.flSettingsSecond;
    }

    @Override
    public void onBackPressed() {
        setTitle(R.string.settings);
        super.onBackPressed();
    }
}
