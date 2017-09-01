package com.hotmail.maximglukhov.naturalnumbersgrid;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.SeekBarPreference;

import java.util.Map;

/**
 * <p>Handles all user's preferences.</p>
 */
public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        Preference preference = findPreference(key);
        if (preference != null) {
            // Sync column value to summary.
            if (key.equals(getString(
                    R.string.preference_columns_key))) {
                int columns = ((SeekBarPreference) preference).getValue();
                preference.setSummary(Integer.toString(columns));
            }
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Add preference layout.
        addPreferencesFromResource(R.xml.preferences);

        // Load all preference summaries.
        PreferenceManager preferenceManager = getPreferenceManager();
        Preference preference;
        Map<String, ?> map = preferenceManager.getSharedPreferences().getAll();
        for (String key : map.keySet()) {
            preference = preferenceManager.findPreference(key);
            if (preference != null) {
                String summary = null;
                // Resolve summary for column preference.
                if (key.equals(getString(R.string.preference_columns_key))) {
                    summary = String.valueOf(((SeekBarPreference) preference)
                            .getValue());
                }

                // Check if we managed to resolve any summaries and apply if we do.
                if (summary != null) {
                    preference.setSummary(summary);
                }
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }
}
