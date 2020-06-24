package com.duzhaokun123.bilibilihd.ui.settings;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.duzhaokun123.bilibilihd.R;

public class SettingsDanmakuFragment extends PreferenceFragmentCompat {

    Preference test, danmakuClick;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_danamku, rootKey);

        test = findPreference("test");
        danmakuClick = findPreference("danmaku_click");

        if (test != null) {
            test.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(getContext(), DanmakuTestActivity.class);
                startActivity(intent);
                return true;
            });
        }

        if (danmakuClick != null) {
            danmakuClick.setOnPreferenceClickListener(preference -> {
                new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.danmaku_drawing)
                        .setMessage(R.string.danmaku_drawing_more)
                        .create()
                        .show();
                return true;
            });
        }
    }
}
