package com.shiweinan.gesturekeyboard;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

public class Preference extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        setPreferencesFromResource(R.xml.prefrence, s);
    }
}
