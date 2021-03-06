package mci.uni.stuttgart.bilget;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;

import mci.uni.stuttgart.bilget.network.JSONLoader;

/**
 * Created by Hanwen on 1/20/2015.
 */
public class SettingsActivity extends PreferenceActivity {

    private static final String TAG = "SettingActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .add(android.R.id.content, new SettingsFragment()) //TODO
                .commit();

        // Add a button to the header list.
        if (hasHeaders()) {
            Button button = new Button(this);
            button.setText("Settings");
            setListFooter(button);
        }
    }


    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        private ListPreference frequencyPref;
        private EditTextPreference thresholdPref;
        private EditTextPreference linkPref;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            PreferenceManager.setDefaultValues(getActivity(), R.xml.settings, false);
            addPreferencesFromResource(R.xml.settings);

            //set default value
            frequencyPref = (ListPreference) findPreference("prefFrequency");
            frequencyPref.setDefaultValue(1);
            frequencyPref.setTitle(getResources().getString(R.string.pref_frequency_summary) + " : " + frequencyPref.getEntry());

            thresholdPref = (EditTextPreference) findPreference("prefThreshold");
            thresholdPref.setTitle(getResources().getString(R.string.pref_frequency_summary) + " : " + thresholdPref.getText());

            linkPref = (EditTextPreference)findPreference("prefLink");
            createButton();

        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals("prefThreshold")) {
                Log.i(TAG,"preference threshold!");
                // Set summary to be the user-description for the selected value
                thresholdPref.setTitle(getResources().getString(R.string.pref_frequency_summary) + " : " + thresholdPref.getText());
                //TODO callback in service
            }
            if( key.equals("prefFrequency")) {
                Log.i(TAG,"preference frequency!");
                frequencyPref.setTitle(getResources().getString(R.string.pref_frequency_summary) + " : " + frequencyPref.getEntry());
                //TODO callback in service
            }
        }

        private void createButton(){
            Preference button = findPreference("update_button");

            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    try {
                        URL sourceUrl = new URL(linkPref.getText());
                        JSONLoader.getInstance(null).download(sourceUrl, false, getActivity());
                    } catch (MalformedURLException e) {
                        Toast.makeText(getActivity(), R.string.url_not_avaliable, Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                    return true;
                }
            });
        }

        //register preference change listener.
        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        //unregister preference change listener.
        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }
    }
}
