package rnfive.djs.cyclingcomputer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.garmin.fit.EventType;
import com.garmin.fit.Sport;

import java.io.File;
import java.util.Calendar;
import java.util.EnumMap;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import lombok.Getter;
import rnfive.djs.cyclingcomputer.define.Arrays;
import rnfive.djs.cyclingcomputer.define.Bearing;
import rnfive.djs.cyclingcomputer.define.DataFields;
import rnfive.djs.cyclingcomputer.define.Devices;
import rnfive.djs.cyclingcomputer.define.Dialogs;
import rnfive.djs.cyclingcomputer.define.EquipmentSensor;
import rnfive.djs.cyclingcomputer.define.FieldDef;
import rnfive.djs.cyclingcomputer.define.Files;
import rnfive.djs.cyclingcomputer.define.FitException;
import rnfive.djs.cyclingcomputer.define.FitFile;
import rnfive.djs.cyclingcomputer.define.Gears;
import rnfive.djs.cyclingcomputer.define.Permissions;
import rnfive.djs.cyclingcomputer.define.Preferences;
import rnfive.djs.cyclingcomputer.define.Strings;
import rnfive.djs.cyclingcomputer.define.Units;
import rnfive.djs.cyclingcomputer.define.enums.ConfirmResult;
import rnfive.djs.cyclingcomputer.define.enums.ConfirmType;
import rnfive.djs.cyclingcomputer.define.enums.SensorState;
import rnfive.djs.cyclingcomputer.define.listeners.ConfirmListener;
import rnfive.djs.cyclingcomputer.define.listeners.FragmentListener;
import rnfive.djs.cyclingcomputer.define.listeners.IDeviceStateChangeReceiver;
import rnfive.djs.cyclingcomputer.define.listeners.PreferenceListener;
import rnfive.djs.cyclingcomputer.define.listeners.RotateListener;
import rnfive.djs.cyclingcomputer.define.listeners.ToastListener;
import rnfive.djs.cyclingcomputer.exception.FitFileException;
import rnfive.djs.cyclingcomputer.service.Service_Recording;
import rnfive.djs.cyclingcomputer.strava.runnable.Runnable_StravaAuth;
import rnfive.djs.cyclingcomputer.strava.runnable.Runnable_StravaUpload;

import rn5.djs.stravalib.authentication.model.AuthenticationType;
import rn5.djs.stravalib.common.model.Token;

import static rnfive.djs.cyclingcomputer.define.StaticVariables.*;
import static rnfive.djs.cyclingcomputer.service.Service_Recording.START_RECORDING;
import static rnfive.djs.cyclingcomputer.service.Service_Recording.STOP_RECORDING;
import static rnfive.djs.cyclingcomputer.service.Service_Recording.ZERO_POWER;
import static rnfive.djs.cyclingcomputer.service.Service_Recording.data;
import static rnfive.djs.cyclingcomputer.service.Service_Recording.phoneSensors;
import static rnfive.djs.cyclingcomputer.service.Service_Recording.fitFile;
import static rnfive.djs.cyclingcomputer.service.Service_Recording.serviceRunning;
import static rnfive.djs.cyclingcomputer.utils.MenuUtil.menuItemSelector;

@Getter
public class MainActivity extends AppCompatActivity
        implements ToastListener, ConfirmListener, PreferenceListener, FragmentListener, RotateListener,
        IDeviceStateChangeReceiver {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final EnumMap<Sport, Integer> hmName = new EnumMap<>(Sport.class);
    static {
        hmName.put(Sport.CYCLING, R.string.ride);
        hmName.put(Sport.RUNNING,R.string.run);
        hmName.put(Sport.WALKING,R.string.walk);
    }

    private static final int MY_PERMISSIONS_REQUEST = 1;

    public static Preferences preferences;
    public static Devices devices = new Devices();

    private static final FieldDef fieldDef = new FieldDef();


    // ANDROID CONTEXT Objects
    //private ConstraintLayout mainLayout;

    private TextView tvDebug;

    private ImageButton ibStart;
    private ImageButton ibLap;
    private ImageButton ibEnd;

    // ACTIVITY
    private static boolean bRunningTmp;

    // CALCULATION
    private static int iDisplayCnt;

    // ACTIVITY
    public static String sName;
    public static String sDescription;
    public static Sport sport;

    // ANT+
    public static final int iAntInterval = 5000;
    public static int iAntBPId;
    public static int iAntHRId;
    public static int iAntBCId;
    public static int iAntBSId;
    public static int iAntBSCId;

    //private Ble_BP bpBle;
    //private Ble_HR hrBle;

    private static boolean bWeatherExists;

    public static boolean bBpAntExists;
    public static boolean bBpBleExists;

    public static boolean bTrainer;
    public static boolean bCommute;
    public static boolean bPrivate;

    private Handler hDisplay;
    private final Runnable rDisplay = new Runnable() {
        @Override
        public void run() {
            updateDisplay();
            getHDisplay().postDelayed(this,1000);
        }
    };

    public static LocationManager locationManager;
    public static DisplayMetrics displayMetrics;
    public static ToastListener toastListener;
    public static PreferenceListener preferenceListener;
    public static FragmentListener fragmentListener;
    public static RotateListener rotateListener;
    private static ConfirmListener confirmListener;

    public static boolean bAntSupportMsgDisplayed;
    public static boolean bAntSupported;
    public static boolean bVertical = true;

    public static int white;
    public static int black;
    public static int gray;
    public static int accent;

    public static int width;
    public static int fragWidth;
    private int buttonVisibility;

    public static boolean bWriteGranted;
    public static boolean bGpsGranted;
    public static boolean bInternetGranted;

    //public final static File filePathDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    public static File filePathApp;
    public static File filePathProfile;
    public static File filePathDevice;
    public static File filePathSummary;
    public static File filePathLog;

    public static final int reqCode = 1001;
    public static final String stravaCode = "STRAVA_CODE";
    public static Token token;

    private static final int start_button = R.id.start_button;
    private static final int lap_button = R.id.lap_button;
    private static final int end_button = R.id.end_button;
    private static final int zero_power_item = R.id.zero_power_item;
    private static final int rotate_item = R.id.rotate_item;

    private Activity activity;
    private static boolean bFragmentReady;
    private Fragment_DataFields fragmentDataFields;
    public static FragmentManager fm;
    protected App app;

    public static int[] activityDataFields = new int[fieldDef.getSize()];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        createDir();

        Files.logMesg("D","onCreate", null);

        app = (App) getApplication();
        app.setContext(this);

        activity = this;
        tvDebug = findViewById(R.id.main_debug);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        toastListener = this;
        confirmListener = this;
        preferenceListener = this;
        fragmentListener = this;
        rotateListener = this;
        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        phoneSensors.setSensorManager((SensorManager) getSystemService(SENSOR_SERVICE));

        white = getColor(R.color.white);
        black = getColor(R.color.black);
        gray = getColor(R.color.gray);
        accent = getColor(R.color.colorAccent);

        sport = Sport.CYCLING;
        Integer nameInt = hmName.get(sport);
        if (nameInt != null)
            sName = getString(nameInt);
        sDescription = "";

        width = displayMetrics.widthPixels;

        //setDataFieldList();

        fm = getSupportFragmentManager();
        startFragment();

        setButtonVisibility(1);
        getPreferences();
        devices.load();

        bTrainer = false;
        bCommute = false;
        bPrivate = false;

        String fitFileNameTmp = getFitFileName();
        if (fitFileNameTmp != null) {
            Log.d("onCreate","loading fit file[" + fitFileNameTmp + "]");
            Files.logMesg("DEBUG","onCreate", "FitFile exists. Load existing file.");
            try {
                fitFile = new FitFile();
                fitFile.openTmp(fitFileNameTmp);
                bStarted = true;
                bPaused = true;
                ibStart.setActivated(false);
                setButtonVisibility(2);
                setOnClick(true);
            } catch (FitException | FitFileException e) {
                setFitFileName(null);
                toastListener.onToast(e.getMessage());
            }
        }

        token = Token.fromFile(filePathProfile);
        if (token != null)
            onToast("Logged in as " + token.getUsername());
    }

    private void createDir() {
        filePathApp = getExternalFilesDir("Cycling Computer");
        filePathProfile = new File(filePathApp,"Profile");
        filePathDevice = new File(filePathProfile, "Device");
        filePathSummary = new File(filePathApp,"Summary");
        filePathLog = new File(filePathApp,"Log");

        boolean dirCreated = (filePathApp.exists() || filePathApp.mkdir());
        dirCreated = (dirCreated && (filePathApp.canWrite() || filePathApp.setWritable(true,true)));
        Log.d("onCreate","filePathApp status[" + dirCreated + "]");

        dirCreated = (filePathProfile.exists() || filePathProfile.mkdir());
        dirCreated = (dirCreated && (filePathProfile.canWrite() || filePathProfile.setWritable(true,true)));
        Log.d("onCreate","filePathProfile status[" + dirCreated + "]");

        dirCreated = (filePathDevice.exists() || filePathDevice.mkdir());
        dirCreated = (dirCreated && (filePathDevice.canWrite() || filePathDevice.setWritable(true,true)));
        Log.d("onCreate","filePathDevice status[" + dirCreated + "]");

        dirCreated = (filePathSummary.exists() || filePathSummary.mkdir());
        dirCreated = (dirCreated && (filePathSummary.canWrite() || filePathSummary.setWritable(true,true)));
        Log.d("onCreate","filePathSummary status[" + dirCreated + "]");

        dirCreated = (filePathLog.exists() || filePathLog.mkdir());
        dirCreated = (dirCreated && (filePathSummary.canWrite() || filePathLog.setWritable(true,true)));
        Log.d("onCreate","filePathLog status[" + dirCreated + "]");

    }


    private void startFragment() {
        ibStart = findViewById(R.id.start_button);
        ibLap = findViewById(R.id.lap_button);
        ibEnd = findViewById(R.id.end_button);
        bFragmentReady = false;
        FrameLayout fragLayout = findViewById(R.id.fragment_view);
        ViewTreeObserver vto = fragLayout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                fragLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int layoutWidth  = fragLayout.getMeasuredWidth();
                int layoutHeight = fragLayout.getMeasuredHeight();
                if (bVertical)
                    fragWidth = width;
                else {
                    int tmpWidth = (Math.max(layoutWidth, layoutHeight));
                    fragWidth = (tmpWidth * 95 / 100);
                }
                Log.e("setFrag","fragWidth[" + layoutWidth + "] fragHeight[" + layoutHeight + "]" );
                new AsyncTask_FragmentDataFields().execute();
            }
        });
    }

    void updateDisplay() {
        bWeatherExists = darkSkyResponse != null;

        if (bFragmentReady && fragmentDataFields.getView() != null && data != null) {
            SpannableString ssValue;
            String sValue;
            int i=0;
            final String dash = "-";
            float fValue;
            boolean isSS;
            boolean skip;
            boolean bPower;
            boolean bCadence;
            boolean bSpeed;
            for (int dataField : activityDataFields) {
                isSS = false;
                skip = false;
                bPower = false;
                bCadence = false;
                bSpeed = false;
                sValue = "-";
                ssValue = new SpannableString(sValue);
                switch (dataField) {
                    case DataFields.TIME:
                        if (data.getMsTot()/1000 >= 3600) {
                            sValue = Strings.getTimeString(data.getMsTot(),1);
                        } else {
                            isSS = true;
                            ssValue = getSSValue(Strings.getTimeString(data.getMsTot(), 1), 3, 0.8f);
                        }
                        break;
                    case DataFields.TIME_LAP:
                        if (data.getMsTot()/1000 >= 3600) {
                            sValue = Strings.getTimeString(data.getMsLap(),1);
                        } else {
                            isSS = true;
                            ssValue = getSSValue(Strings.getTimeString(data.getMsLap(),1),3,0.8f);
                        }
                        break;
                    case DataFields.SPEED:
                        isSS = true;
                        bSpeed = true;
                        if (data.getSpeed() != -1.0f)
                            ssValue = getSSValue(Strings.getSpeedString(data.getSpeed()), 2, 0.7f);
                        break;
                    case DataFields.SPEED_AVG:
                        isSS = true;
                        if (data.getSpeedAvg() != -1.0f)
                            ssValue = getSSValue(Strings.getSpeedString(data.getSpeedAvg()), 2, 0.7f);
                        break;
                    case DataFields.SPEED_LAP:
                        isSS = true;
                        if (data.getSpeedAvgLap() != -1.0f)
                            ssValue = getSSValue(Strings.getSpeedString(data.getSpeedAvgLap()), 2, 0.7f);
                        break;
                    case DataFields.PACE:
                        isSS = true;
                        if (data.getSpeed() != -1.0f)
                            ssValue = getSSValue(Strings.getPaceString(data.getSpeed()), 3, 0.8f);
                        break;
                    case DataFields.PACE_AVG:
                        isSS = true;
                        if (data.getSpeedAvg() != -1.0f)
                            ssValue = getSSValue(Strings.getPaceString(data.getSpeedAvg()), 3, 0.8f);
                        break;
                    case DataFields.PACE_LAP:
                        isSS = true;
                        if (data.getSpeedAvgLap() != -1.0f)
                            ssValue = getSSValue(Strings.getPaceString(data.getSpeedAvgLap()), 3, 0.8f);
                        break;
                    case DataFields.HR:
                        if (bHRExists && data.getHr() != -1)
                            sValue = String.valueOf(data.getHr());
                        break;
                    case DataFields.HR_AVG:
                        if (data.getHrAvg() != -1)
                            sValue = String.valueOf(data.getHrAvg());
                        break;
                    case DataFields.HR_LAP:
                        if (data.getHrAvgLap() != -1)
                            sValue = String.valueOf(data.getHrAvgLap());
                        break;
                    case DataFields.CADENCE:
                        bCadence = true;
                        if ((bBCExists || bBPCadExists) && data.getCadence() != -1)
                            sValue = String.valueOf(data.getCadence());
                        break;
                    case DataFields.CADENCE_AVG:
                        if (data.getCadenceAvg() != -1)
                            sValue = String.valueOf(data.getCadenceAvg());
                        break;
                    case DataFields.CADENCE_LAP:
                        if (data.getCadenceAvgLap() != -1)
                            sValue = String.valueOf(data.getCadenceAvgLap());
                        break;
                    case DataFields.POWER:
                        if (bBPExists && data.getPower() != -1)
                            sValue = String.valueOf(data.getPower());
                        bPower = true;
                        break;
                    case DataFields.POWER_3S:
                        if (bBPExists && data.getPower() != -1) {
                            sValue = String.valueOf(Math.round(Arrays.getAvg(data.getPower3sArray())));
                        }
                        bPower = true;
                        break;
                    case DataFields.POWER_10S:
                        if (bBPExists && data.getPower() != -1) {
                            sValue = String.valueOf(Math.round(Arrays.getAvg(data.getPower10sArray())));
                        }
                        bPower = true;
                        break;
                    case DataFields.POWER_30S:
                        if (bBPExists && data.getPower() != -1) {
                            sValue = String.valueOf(Math.round(Arrays.getAvg(data.getPower30sArray())));
                        }
                        bPower = true;
                        break;
                    case DataFields.POWER_AVG:
                        if (data.getPowerAvg() != -1) {
                            sValue = String.valueOf(data.getPowerAvg());
                        }
                        break;
                    case DataFields.POWER_LAP:
                        if (data.getPowerAvgLap() != -1) {
                            sValue = String.valueOf(data.getPowerAvgLap());
                        }
                        break;
                    case DataFields.TORQUE:
                        if (bBpAntExists) {
                            if (data.getTorqueL() > 0)
                                sValue = data.getTorqueL() + (data.getTorqueR() > 0 ? "-" : "");
                            if (data.getTorqueR() > 0)
                                sValue = (sValue.equals(dash) ? String.valueOf(data.getTorqueR()) : sValue + data.getTorqueR());
                            if (sValue.equals(dash) && (data.getTorqueL() == 0 || data.getTorqueR() == 0))
                                sValue = "0";
                        }
                        break;
                    case DataFields.BALANCE:
                        if (bBPExists && data.getBalanceR() != -1) {
                            sValue = 100 - data.getBalanceR() + "-" + data.getBalanceR();
                        }
                        break;
                    case DataFields.SMOOTHNESS:
                        if (bBpAntExists) {
                            if (data.getSmoothL() > 0)
                                sValue = data.getSmoothL() + (data.getSmoothR() > 0 ? "-" : "");
                            if (data.getSmoothR() > 0)
                                sValue = (sValue.equals(dash) ? String.valueOf(data.getSmoothR()) : sValue + data.getSmoothR());
                            if (sValue.equals(dash) && (data.getSmoothL() == 0 || data.getSmoothR() == 0))
                                sValue = "0";
                        }
                        break;
                    case DataFields.BEARING:
                        skip = (iDisplayCnt%3 != 0);
                        isSS = true;
                        ssValue = getSSValue(Bearing.bearingToDirection(data.getBearing()),0,0.9f);
                        break;
                    case DataFields.GPS_ACCURACY:
                        if (isGPSEnabled(locationManager)) {
                            if (bMetric) {
                                sValue = String.valueOf(data.getGpsAccuracy());
                            } else {
                                sValue = String.valueOf((int) (data.getGpsAccuracy() * Units.FC_M_FT));
                            }
                        }
                        break;
                    case DataFields.ASCENT:
                        sValue = Strings.getAltitudeString((float) data.getAscentTot());
                        break;
                    case DataFields.ASCENT_LAP:
                        sValue = Strings.getAltitudeString((float) data.getAscentLap());
                        break;
                    case DataFields.DESCENT:
                        sValue = Strings.getAltitudeString((float) data.getDescentTot());
                        break;
                    case DataFields.DESCENT_LAP:
                        sValue = Strings.getAltitudeString((float) data.getDescentLap());
                        break;
                    case DataFields.GRADE:
                        isSS = true;
                        double rad = data.getAngle() * Math.PI / 180.0d;
                        double grade = Math.round(StrictMath.tan(rad) * 200.0d)/2.0d;
                        ssValue = getSSValue(Strings.getNumericString(grade,1),2,0.7f);
                        break;
                    case DataFields.TEMPERATURE:
                        if (bWeatherExists)
                            sValue = Strings.getTemperatureString((float) darkSkyResponse.getTemperature());
                        break;
                    case DataFields.WIND:
                        skip = (iDisplayCnt%3 != 0);
                        if (!skip) {
                            if (bWeatherExists) {
                                fragmentDataFields.setImageValue(i, (float) ((darkSkyResponse.getWindBearing() - 180) - data.getBearing()));
                                fValue = Units.getSpeed(darkSkyResponse.getWindSpeed());
                                if (fValue >=10)
                                    sValue = String.valueOf(Math.round(fValue));
                                else {
                                    isSS = true;
                                    ssValue = getSSValue(Strings.getSpeedString(darkSkyResponse.getWindSpeed()),2,0.7f);
                                }
                            } else
                                fragmentDataFields.setImageValue(i,0);
                        }
                        break;
                    case DataFields.ALTITUDE:
                        sValue = Strings.getAltitudeString((float) data.getAltitudeValue());
                        break;
                    case DataFields.TIME_OF_DAY:
                        isSS = true;
                        ssValue = getSSValue(Strings.getTimeOfDayString(),1,0.7f);
                        break;
                    case DataFields.DISTANCE:
                        isSS = true;
                        ssValue = getSSValue(Strings.getDistanceString((float) data.getDistanceTot()),(Units.getDistance((float) data.getDistanceTot())>=10?2:3),0.8f);
                        break;
                    case DataFields.DISTANCE_LAP:
                        isSS = true;
                        ssValue = getSSValue(Strings.getDistanceString((float) data.getDistanceLap()),(Units.getDistance((float) data.getDistanceLap())>=10?2:3),0.8f);
                        break;
                    case DataFields.GEAR_RATIO:
                        if (bBCExists && bBSExists) {
                            if (bMoving)
                                Gears.determineGearing();
                            isSS = true;
                            ssValue = getSSValue(Gears.sRatios[data.getIGear()],0,0.9f);
                        }
                        break;
                    case DataFields.ANGLE:
                        isSS = false;
                        sValue = Strings.getNumericString(data.getAngle(), 1);
                        break;
                    case DataFields.TEST:
                    case DataFields.NONE:
                    default :
                        break;
                }

                int iBatStatus = -1;
                if (bPower) {
                    iBatStatus = bpAntBattery;
                }
                if (bCadence) {
                    iBatStatus = bcAntBattery;
                }
                if (bSpeed) {
                    iBatStatus = bsAntBattery;
                }
                if (iBatStatus > 0) {
                    fragmentDataFields.setCornerIcon(i,iBatStatus);
                }
                if (skip)
                    i++;
                else {
                    if (isSS)
                        fragmentDataFields.setSSValue(i++, ssValue);
                    else
                        fragmentDataFields.setValue(i++, sValue);
                }
            }
        }
        iDisplayCnt++;
    }

    private SpannableString getSSValue(String val, int pos, float proportion) {
        SpannableString ssString = new SpannableString(val);
        ssString.setSpan(new RelativeSizeSpan(proportion), (pos==0?0:ssString.length() - pos), ssString.length(), 0);
        return ssString;
    }

    private void setFitFileName(@Nullable String name) {
        SharedPreferences mainPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = mainPrefs.edit();
        editor.putString("FIT_FILE_NAME", name);
        editor.apply();
        Files.logMesg("D","getFitFileName", name);
    }

    private String getFitFileName() {
        SharedPreferences mainPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String ret = mainPrefs.getString("FIT_FILE_NAME",null);
        Files.logMesg("D","getFitFileName", ret);
        return ret;
    }

    private void getPreferences() {
        preferences = Preferences.load();
        if (preferences == null) {
            Log.e(TAG, "preferences is null.");
        }
        iAthleteHrMax = preferences.getIHrMax();
        iAthleteFtp = preferences.getIFtp();
        bHrZoneColors = preferences.isBHrZoneColor();
        bPowerZoneColors = preferences.isBPowerZoneColor();
        bDebug = preferences.isBDebugMode();
        bInvert = preferences.isBInvertColorMode();
        bMetric = preferences.isBMetricMode();
        bKeepAwake = preferences.isBKeepAwakeMode();
        bAntSpeed = preferences.isBUseSensorSpeed();
        bAntDistance = preferences.isBUseSensorDistance();
        iWheelSize = preferences.getIHrMax();
        activityDataFields = preferences.getBikeDataFields();
        dGradeOffset = preferences.getDGradeOffset();

        SharedPreferences mainPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        iAntHRId = mainPrefs.getInt("ANT_PLUS_ID_HR",0);
        iAntBPId = mainPrefs.getInt("ANT_PLUS_ID_BP",58707);
        iAntBCId = mainPrefs.getInt("ANT_PLUS_ID_BC",0);
        iAntBSCId = mainPrefs.getInt("ANT_PLUS_ID_BSC",0);
        iAntBSId = mainPrefs.getInt("ANT_PLUS_ID_BS",0);

        if (bDebug)
            tvDebug.setVisibility(View.VISIBLE);
        else
            tvDebug.setVisibility(View.GONE);
        preferences.save();
    }

    private void setButtonVisibility(int val) {
        Files.logMesg("D","setButtonVisibility", "Val[" + val + "]");
        ConstraintLayout start = findViewById(R.id.start_button_layout);
        ConstraintLayout lap = findViewById(R.id.lap_button_layout);
        ConstraintLayout end = findViewById(R.id.end_button_layout);

        buttonVisibility = val;
        switch (val) {
            case 1:
                start.setVisibility(View.VISIBLE);
                lap.setVisibility(View.GONE);
                end.setVisibility(View.GONE);
                break;
            case 2:
                start.setVisibility(View.VISIBLE);
                lap.setVisibility(View.VISIBLE);
                end.setVisibility(View.VISIBLE);
                break;
            default :
                break;
        }
    }

    private View.OnClickListener onClick() {
        return this::click;
    }

    private void click(View v) {
        switch (v.getId()) {
            case start_button:
                if (bStarted) {
                    pause();
                } else
                    start();
                break;
            case lap_button:
                lap();
                break;
            case end_button:
                bRunningTmp = !bPaused;
                if (bRunningTmp)
                    pause();
                Dialogs.Confirm(activity, confirmListener, ConfirmType.SAVE,getString(R.string.save_alert),null,getString(R.string.save),getString(R.string.discard),getString(R.string.cancel));
                break;
            default:
                break;
        }
    }

    private void start() {
        Files.logMesg("D","start", null);
        bStarted = true;
        bPaused = false;
        ibStart.setActivated(true);
        setButtonVisibility(2);
        fitFile = new FitFile();
        fitFile.create(null);
        // Reset Variables
        // data = new Data();
        sendToService(START_RECORDING);
        setFitFileName(fitFile.getFitFileName());
        startService();
        setKeepAwake();
    }

    public void sendToService(String action) {
        Intent serviceIntent = new Intent(this, Service_Recording.class);
        serviceIntent.setAction(action);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    private void startService() {
        Intent serviceIntent = new Intent(this, Service_Recording.class);
        serviceIntent.setAction(Service_Recording.START_SERVICE);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    private void stopService() {
        if (serviceRunning) {
            Intent serviceIntent = new Intent(this, Service_Recording.class);
            serviceIntent.setAction(Service_Recording.STOP_SERVICE);
            ContextCompat.startForegroundService(this, serviceIntent);
        }
    }

    private void end(boolean save) {
        Files.logMesg("D","end", "Save["+save+"]");
        bStarted = false;
        bPaused = false;
        ibStart.setActivated(false);
        setButtonVisibility(1);
        sendToService(STOP_RECORDING);
        if (fitFile != null) {
            if (save) {
                fitFile.close();
                FitFile summary = new FitFile();
                summary.create(fitFile.getFitFileName());
                summary.close();
                if (token != null) {
                    Runnable_StravaUpload runnable = new Runnable_StravaUpload(fitFile.getFitFileName(), sName, sDescription);
                    if (sport == Sport.RUNNING)
                        runnable.setRunActivity(true);
                    Executors.newSingleThreadExecutor().execute(runnable);
                }
            } else {
                fitFile.delete();
            }
        }
        //fitFile = null;
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void pause() {
        Files.logMesg("D","pause", null);
        bPaused = !bPaused;
        ibStart.setActivated(!bPaused);
        if (fitFile != null) {
            fitFile.eventMesg((bPaused?EventType.STOP:EventType.START), true);
        }
    }

    private void lap() {
        Files.logMesg("D","lap", null);
        if (fitFile != null) {
            fitFile.lapMesg();
        }
        resetLapVariables();
    }

    private void resetLapVariables() {
        Files.logMesg("D","resetLapVariables", null);
        data.resetLapVariables();
    }

    private void setKeepAwake() {
        Log.d(TAG,"setKeepAwake() bStarted[" + bStarted + "] bKeepAwake[" + bKeepAwake + "]");
        //if(bStarted && bKeepAwake)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void setOnClick(boolean set) {
        if (set) {
            ibStart.setOnClickListener(onClick());
            ibLap.setOnClickListener(onClick());
            ibEnd.setOnClickListener(onClick());
        } else {
            ibStart.setOnClickListener(null);
            ibLap.setOnClickListener(null);
            ibEnd.setOnClickListener(null);
        }
        Log.d("ACTION","setOnClick[" + set + "].");
        Files.logMesg("D","setOnClick", "[" + set + "]");
    }

    /*
    private void connectBle() {
        if (devices.getDevices() != null && !devices.getDevices().isEmpty()) {
            for (Device device : devices.getDevices()) {
                if (device.getSensorIdList() != null && !device.getSensorIdList().isEmpty()) {
                    for (int i : device.getSensorIdList()) {
                        int type = EquipmentSensor.getType(i);
                        switch (type) {
                            case EquipmentSensor.POWER:
                                bpBle = new Ble_BP()
                                        .forSensor(device)
                                        .withContext(this)
                                        .withDeviceStateChangeReceiver(this);
                                bpBle.create();

                                bpBle.subscribeInstantaneousPower((var1, var2) -> {
                                    if (!bBpAntExists)
                                        data.setPower(var2.intValue());
                                });
                                bpBle.subscribeInstantaneousPedalPowerBalance((timestamp, balance) -> data.setBalanceR(balance.intValue()));
                                bpBle.subscribeCalculatedCadence((var1, var2) -> {
                                    if (!bBpAntExists) {
                                        bBPCadExists = true;
                                        data.setCadBP(var2.intValue());
                                    }
                                });
                                bpBle.subscribeCalculatedBalance((timestamp, balance, isRightPedalBalance) -> data.setBalanceR(balance.intValue()));
                                bpBle.start();
                                break;
                            case EquipmentSensor.HEARTRATE:
                                hrBle = Ble_HR.Builder.newInstance()
                                        .setContext(this)
                                        .setDevice(device)
                                        .setReceiver(this)
                                        .build();

                                hrBle.create();
                                hrBle.subscribeHeartRateMeasurement((timestamp, hr) -> data.setHr(hr.intValue()));
                                hrBle.subscribeRRInterval((timestamp, rrInterval) -> data.setIRRInterval(rrInterval.intValue()));
                                hrBle.start();
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        }
    }

    private void startBle() {
        if (bpBle != null)
            bpBle.start();
        if (hrBle != null)
            hrBle.start();
    }

    private void stopBle() {
        if (!bStarted) {
            if (bpBle != null)
                bpBle.stop();
            if (hrBle != null)
                hrBle.stop();
        }
    }

    private void destroyBle() {
        if (bpBle != null)
            bpBle.destroy();
        if (hrBle != null)
            hrBle.destroy();
    }

     */

    private void startDisplay() {
        iDisplayCnt = 0;
        hDisplay = new Handler();
        hDisplay.post(rDisplay);
        Log.d("ACTION", "Display started.");
        Files.logMesg("D","startDisplay", null);
    }

    private void stopDisplay() {
        hDisplay.removeCallbacks(rDisplay);
        hDisplay = null;
        Log.d("ACTION", "Display stopped.");
        Files.logMesg("D","stopDisplay", null);
    }

    public static boolean isGPSEnabled(LocationManager locationManager) {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void refreshToken() {
        if (token != null) {
            Calendar gmtDate = Calendar.getInstance(TimeZone.getTimeZone("gmt"));
            if (token.getExpirationDate() < gmtDate.getTimeInMillis()) {
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(new Runnable_StravaAuth(this, AuthenticationType.REFRESH_TOKEN, token.getRefreshToken()));
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        //http://localhost/?state=&code=6e30d771fa2afd6eeeef5352638bc3a8ae75cd92&scope=read,activity:write,activity:read_all
        if (requestCode == reqCode && resultCode == RESULT_OK && data != null) {
            String code = data.getStringExtra(stravaCode);
            // Use code to obtain accessToken
            toastListener.onToast(code);
        }
    }

    @Override
    public void updatePreference(@Nullable String key) {
        preferences.save();
        if (key == null) {
            getPreferences();
            fragmentDataFields.setDataFields();
        }
        Log.d("ACTION","updatePreference");
    }

    @Override
    public void onConfirm(ConfirmType confirmType, ConfirmResult confirmResult, @Nullable String[] strings) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\t");
        sb.append(confirmType);
        sb.append(":");
        sb.append(confirmResult);
        if (strings != null) {
            for (String val : strings) {
                sb.append("\n\t\t");
                sb.append(val);
            }
        }
        Files.logMesg("D","onConfirm", sb.toString());

        switch (confirmType) {
            case TEST:
                tvDebug.setText(confirmResult.toString());
                break;
            case SAVE:
                confirmResultSave(confirmResult, strings);
                break;
            case EXIT :
                confirmResultExit(confirmResult, strings);
                break;
            case PERMISSIONS:
                if (strings != null)
                    ActivityCompat.requestPermissions(activity,strings,MY_PERMISSIONS_REQUEST);
                break;
        }
    }

    private void confirmResultExit(ConfirmResult confirmResult, @Nullable String[] strings) {
        switch (confirmResult) {
            case POSITIVE:
                //TODO save for later
                setFitFileName(fitFile.getFitFileName());
                fitFile.closeTmp();
                finish();
                break;
            case NEGATIVE:
                end(false);
                setFitFileName(null);
                finish();
                break;
            case NEUTRAL:
                break;
        }
    }

    private void confirmResultSave(ConfirmResult confirmResult, @Nullable String[] strings) {
        switch (confirmResult) {
            case POSITIVE:
                if (strings != null) {
                    sName = strings[0];
                    sDescription = strings[1];
                }
                end(true);
                setFitFileName(null);
                break;
            case NEGATIVE:
                end(false);
                setFitFileName(null);
                break;
            case NEUTRAL:
                if (bRunningTmp)
                    pause();
                break;
        }
    }

    public void onFragmentReady(Fragment_DataFields fragment_dataFields) {
        bFragmentReady = true;
        fragmentDataFields = fragment_dataFields;
    }

    @Override
    public void onRotate() {
        bVertical = !bVertical;
        fragmentDataFields = null;
        if (bVertical) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            setContentView(R.layout.activity_main_activity);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            setContentView(R.layout.activity_main_activity_land);
        }
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        startFragment();
        setOnClick(true);
        setButtonVisibility(buttonVisibility);
    }

    @Override
    public void onToast(String msg) {
        if (!msg.isEmpty()) {
            runOnUiThread(() -> Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show());
            Files.logMesg("D","onToast", msg);
            Log.d("onToast",msg);
        }
    }

    @Override
    public void onDeviceStateChange(int id, String sensorId, SensorState sensorState) {
        int type = EquipmentSensor.getType(id);

        Log.d("onDeviceStateChange","MainActivity [" + sensorState + "]");
        if (type == EquipmentSensor.POWER) {
            bBpAntExists = sensorState == SensorState.CONNECTED;
            if (!bBpAntExists)
                bBPCadExists = false;
            if (bBpAntExists != bBPExists)
                invalidateOptionsMenu();
        }
        /*
        switch (type) {
            case EquipmentSensor.POWER:
                if (EquipmentSensor.getBand(id) == EquipmentSensor.BLE) {
                    bBpBleExists = sensorState == SensorState.CONNECTED;
                } else {
                    bBpAntExists = sensorState == SensorState.CONNECTED;
                }
                if (!bBpBleExists && !bBpAntExists)
                    bBPCadExists = false;
                boolean bpExists = bBPExists;
                bBPExists = bBpAntExists || bBpBleExists;
                if (bpExists != bBPExists)
                    invalidateOptionsMenu();
                break;
            case EquipmentSensor.HEARTRATE:
                if (hrBle != null && hrBle.isConnected()) {
                    bHRExists = true;
                    break;
                }
                bHRExists = false;
                data.setHr(-1);
                break;
        }
         */
        onToast(EquipmentSensor.getSensorName(id) + " " + sensorState.getName());
    }

    @Override
    public void onPowerConnect() {
        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //menu.setGroupVisible(R.id.power_menu,true);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = true;
        switch (item.getItemId()) {
            case zero_power_item:
                sendToService(ZERO_POWER);
                result = true;
                /*
                if (bBpAntExists) {
                    sendToService(ZERO_POWER);
                    result = true;
                } else
                    result = bpBle.zero();

                 */
                break;
            case rotate_item:
                onRotate();
                break;
            default :
                result = menuItemSelector(this, item, TAG);
                break;
        }
        return result || super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.zero_power_menu);
        item.setVisible(bBPExists);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (bStarted)
            Dialogs.Confirm(activity, confirmListener, ConfirmType.EXIT,getString(R.string.exit_alert),null,getString(R.string.save),getString(R.string.discard),getString(R.string.cancel));
        else
            finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Files.logMesg("D","onStart", null);
        Permissions.checkAppPermissions(activity,confirmListener);
        bAntSupportMsgDisplayed = false;
        if (!bStarted) {
            //startUpdateValues();
            phoneSensors.startUpdates();
            //startBle();
            //startGps();
            setOnClick(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Files.logMesg("D","onStop", null);
        if (!bStarted) {
            //stopUpdateValues();
            phoneSensors.stopUpdates();
            //stopBle();
            //stopGps();
            setOnClick(false);
            stopService();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Files.logMesg("D","onResume", null);
        startDisplay();
        setKeepAwake();
        refreshToken();
        startService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Files.logMesg("D","onPause", null);
        stopDisplay();
    }

    @Override
    protected void onDestroy() {
        Files.logMesg("D","onDestroy", null);
        Log.d("MainActivity","onDestroy()");
        bStarted = false;
        bWeatherExists = false;
        //stopUpdateValues();
        phoneSensors.stopUpdates();
        //stopGps();
        //destroyBle();
        if (fitFile != null && fitFile.isOpen()) {
            Log.d("MainActivity","onDestroy() close fitFile");
            setFitFileName(fitFile.getFitFileName());
            fitFile.closeTmp();
        }
        stopService();
        super.onDestroy();
    }
}
