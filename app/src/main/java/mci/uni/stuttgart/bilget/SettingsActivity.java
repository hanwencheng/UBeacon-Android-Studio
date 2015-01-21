package mci.uni.stuttgart.bilget;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;

import java.util.List;

/**
 * Created by Hanwen on 1/20/2015.
 */
public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        // Add a button to the header list.
        if (hasHeaders()) {
            Button button = new Button(this);
            button.setText("Settings");
            setListFooter(button);
        }
    }

    public static class SettingsFragment extends PreferenceFragment{
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            PreferenceManager.setDefaultValues(getActivity(), R.xml.settings, false);
            addPreferencesFromResource(R.xml.settings);
        }
    }

//    /**
//     * Populate the activity with the top-level headers.
//     */
//    @Override
//    public void onBuildHeaders(List<Header> target) {
//        loadHeadersFromResource(R.xml.preference_headers, target);
//    }
//
//    /**
//     * This fragment shows the preferences for the first header.
//     */
//    public static class Prefs1Fragment extends PreferenceFragment {
//        @Override
//        public void onCreate(Bundle savedInstanceState) {
//            super.onCreate(savedInstanceState);
//
//            // Make sure default values are applied.  In a real app, you would
//            // want this in a shared function that is used to retrieve the
//            // SharedPreferences wherever they are needed.
//            PreferenceManager.setDefaultValues(getActivity(),
//                    R.xml.advanced_preferences, false);
//
//            // Load the preferences from an XML resource
//            addPreferencesFromResource(R.xml.fragmented_preferences);
//        }
//    }
//
//    /**
//     * This fragment contains a second-level set of preference that you
//     * can get to by tapping an item in the first preferences fragment.
//     */
//    public static class Prefs1FragmentInner extends PreferenceFragment {
//        @Override
//        public void onCreate(Bundle savedInstanceState) {
//            super.onCreate(savedInstanceState);
//
//            // Can retrieve arguments from preference XML.
//            Log.i("args", "Arguments: " + getArguments());
//
//            // Load the preferences from an XML resource
//            addPreferencesFromResource(R.xml.fragmented_preferences_inner);
//        }
//    }
//
//    /**
//     * This fragment shows the preferences for the second header.
//     */
//    public static class Prefs2Fragment extends PreferenceFragment {
//        @Override
//        public void onCreate(Bundle savedInstanceState) {
//            super.onCreate(savedInstanceState);
//
//            // Can retrieve arguments from headers XML.
//            Log.i("args", "Arguments: " + getArguments());
//
//            // Load the preferences from an XML resource
//            addPreferencesFromResource(R.xml.preference_dependencies);
//        }
//    }
}
