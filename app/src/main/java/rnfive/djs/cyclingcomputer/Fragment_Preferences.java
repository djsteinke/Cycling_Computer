package rnfive.djs.cyclingcomputer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import rnfive.djs.cyclingcomputer.strava.runnable.Runnable_StravaAuth;
import rn5.djs.stravalib.authentication.model.AuthenticationType;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static rnfive.djs.cyclingcomputer.MainActivity.token;
import static rnfive.djs.cyclingcomputer.MainActivity.bInternetGranted;
import static rnfive.djs.cyclingcomputer.MainActivity.iAntBSCId;
import static rnfive.djs.cyclingcomputer.MainActivity.iAntBSId;
import static rnfive.djs.cyclingcomputer.MainActivity.preferenceListener;
import static rnfive.djs.cyclingcomputer.MainActivity.preferences;
import static rnfive.djs.cyclingcomputer.MainActivity.toastListener;
import static rnfive.djs.cyclingcomputer.define.StaticVariables.*;

public class Fragment_Preferences extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = Fragment_Preferences.class.getSimpleName();
    private Context context;

    private Fragment_Preferences() {}
    public Fragment_Preferences(Context context) {
        this.context = context;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);

        Preference rideDataField = findPreference("KEY_BIKE_DATA_FIELDS");
        if (rideDataField != null)
            rideDataField.setOnPreferenceClickListener(onPreferenceClickListener("KEY_BIKE_DATA_FIELDS"));

        Preference runDataField = findPreference("KEY_RUN_DATA_FIELDS");
        if (runDataField != null)
            runDataField.setOnPreferenceClickListener(onPreferenceClickListener("KEY_RUN_DATA_FIELDS"));

        Preference stravaLoginField = findPreference("KEY_STRAVA_LOGIN");
        if (stravaLoginField != null)
            stravaLoginField.setOnPreferenceClickListener(onPreferenceClickListener("KEY_STRAVA_LOGIN"));

        Preference antSearchField = findPreference("KEY_ANT_SEARCH");
        if (antSearchField != null)
            antSearchField.setOnPreferenceClickListener(onPreferenceClickListener("KEY_ANT_SEARCH"));

        PreferenceScreen preferenceScreen = (PreferenceScreen) findPreference("preferenceScreen");
        if (iAntBSCId == 0 && iAntBSId == 0 && preferenceScreen != null) {
            PreferenceCategory bikeCategory = (PreferenceCategory) findPreference("bikeCategory");
            preferenceScreen.removePreference(bikeCategory);
        }

            /*
            PreferenceCategory athleteCategory = (PreferenceCategory) findPreference("athleteCategory");
            preferenceScreen.removePreference(athleteCategory);
            */

        PreferenceCategory dataFields = (PreferenceCategory) findPreference("dataFieldCategory");
        if (dataFields != null)
            dataFields.removePreference(runDataField);

        PreferenceCategory appSettingsCategory = (PreferenceCategory) findPreference("appSettingsCategory");
        CheckBoxPreference debugPref = (CheckBoxPreference) findPreference("KEY_DEBUG");
        if (appSettingsCategory != null)
            appSettingsCategory.removePreference(debugPref);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "onSharedPreferenceChanged() key[" + key + "]");
        Preference pref = findPreference(key);
        if (key.equals("KEY_STRAVA_LOGIN")) {
            setSummary(key);
        }
        if (pref instanceof EditTextPreference) {
            EditTextPreference etp = (EditTextPreference) pref;
            pref.setSummary(etp.getText());
            switch(key) {
                case "KEY_HR_MAX":
                    iAthleteHrMax = Integer.parseInt(etp.getText());
                    break;
                case "KEY_FTP":
                    iAthleteFtp = Integer.parseInt(etp.getText());
                    break;
                case "KEY_GRADE_OFFSET":
                    dGradeOffset = Double.parseDouble(etp.getText());
                    preferences.setDGradeOffset(dGradeOffset);
                    break;
            }
        }
        if (pref instanceof CheckBoxPreference) {
            CheckBoxPreference cbp = (CheckBoxPreference) pref;
            switch (key) {
                case "KEY_DEBUG":
                    bDebug = cbp.isChecked();
                    break;
                case "KEY_METRIC":
                    bMetric = cbp.isChecked();
                    break;
                case "KEY_INVERT_COLOR":
                    bInvert = cbp.isChecked();
                    break;
                case "KEY_KEEP_AWAKE":
                    bKeepAwake = cbp.isChecked();
                    preferences.setBKeepAwakeMode(bKeepAwake);
                    break;
                case "KEY_DISTANCE_DEFAULT":
                    bAntDistance = cbp.isChecked();
                    break;
                case "KEY_SPEED_DEFAULT":
                    bAntSpeed = cbp.isChecked();
                    break;
                case "KEY_HR_ZONE_COLOR":
                    bHrZoneColors = cbp.isChecked();
                    break;
                case "KEY_POWER_ZONE_COLOR":
                    bPowerZoneColors = cbp.isChecked();
                    break;
                        /*
                    case "ALT_GRAPH_DISPLAY_KEY":
                        MainActivity.b_display_alt_graph = cbp.isChecked();
                        break;
                    case "HR_GRAPH_DISPLAY_KEY":
                        MainActivity.b_display_hr_graph = cbp.isChecked();
                        break;
                    case "BOLD_KEY":
                        MainActivity.b_bold = cbp.isChecked();
                        break;
                    case "METRIC_KEY":
                        EditTextPreference editTextPref = (EditTextPreference) findPreference("WEIGHT_KEY");
                        double weight = Double.parseDouble(editTextPref.getText());
                        if (cbp.isChecked())
                            weight *= LB_TO_KG;
                        else
                            weight /= LB_TO_KG;
                        editTextPref.setText(getStringFromInt((int) (weight*10), i_precision_one));
                        MainActivity.b_metric = cbp.isChecked();
                        break;
                */
            }
        }

        preferenceListener.updatePreference(null);
    }

    private Preference.OnPreferenceClickListener onPreferenceClickListener(final String key) {
        return new Preference.OnPreferenceClickListener() {
            Intent newIntent;
            Bundle args;

            @Override
            public boolean onPreferenceClick(Preference preference) {
                switch (key) {
                    case "KEY_BIKE_DATA_FIELDS" :
                        newIntent = new Intent(context, Activity_DataFieldPrefs.class);
                        args = new Bundle();
                        args.putSerializable(Activity_DataFieldPrefs.DATA_FIELD_TYPE_KEY, "Bike");
                        newIntent.putExtra(Activity_DataFieldPrefs.DATA_FIELD_KEY, args);
                        newIntent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(newIntent);
                        break;
                    case "KEY_RUN_DATA_FIELDS" :
                        newIntent = new Intent(context, Activity_DataFieldPrefs.class);
                        args = new Bundle();
                        args.putSerializable(Activity_DataFieldPrefs.DATA_FIELD_TYPE_KEY, "Run");
                        newIntent.putExtra(Activity_DataFieldPrefs.DATA_FIELD_KEY, args);
                        newIntent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(newIntent);
                        break;
                    case "KEY_STRAVA_LOGIN" :
                        if (bInternetGranted) {
                            if (token == null) {
                                Uri uri = Uri.parse("https://www.strava.com/oauth/mobile/authorize")
                                        .buildUpon()
                                        .appendQueryParameter("client_id", "24797")
                                        .appendQueryParameter("redirect_uri", Settings.stravaRedirectUri)
                                        .appendQueryParameter("response_type", "code")
                                        .appendQueryParameter("approval_prompt", "auto")
                                        .appendQueryParameter("scope", "activity:write,activity:read_all")
                                        .build();
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                startActivity(intent);
                            } else {
                                ExecutorService executor = Executors.newSingleThreadExecutor();
                                executor.execute(new Runnable_StravaAuth(context, AuthenticationType.DEAUTHORIZE, null));
                            }
                        } else {
                            toastListener.onToast("Internet permission required for Strava");
                        }
                        break;
                    case "KEY_ANT_SEARCH" :
                        newIntent = new Intent(context, Activity_AntSensorSearch.class);
                        newIntent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(newIntent);
                        break;
                    default :
                        break;
                }
                return false;
            }
        };
    }

    private void setSummary(String key) {
        Preference summaryPref = findPreference(key);
        if (summaryPref != null) {
            switch (key) {
                case "KEY_STRAVA_LOGIN":
                    String val = (token != null ? token.getUsername() : null);
                    SpannableString newSS = new SpannableString(val != null ? "*Logged in as " + val : "");
                    newSS.setSpan(new StyleSpan(Typeface.ITALIC), 0, newSS.length(), 0);
                    summaryPref.setSummary(newSS);
                    break;
                case "KEY_WHEEL":
                case "KEY_FTP":
                case "KEY_GRADE_OFFSET":
                case "KEY_HR_MAX":
                    EditTextPreference etp = (EditTextPreference) summaryPref;
                    summaryPref.setSummary(etp.getText());
                    break;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
        setSummary("KEY_STRAVA_LOGIN");
        setSummary("KEY_WHEEL");
        setSummary("KEY_HR_MAX");
        setSummary("KEY_FTP");
        setSummary("KEY_GRADE_OFFSET");
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        preferenceListener.updatePreference(null);
    }
}
