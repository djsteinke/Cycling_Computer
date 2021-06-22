package rnfive.djs.cyclingcomputer.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.GeomagneticField;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.garmin.fit.Sport;

import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import lombok.Getter;
import rnfive.djs.cyclingcomputer.FitFile;
import rnfive.djs.cyclingcomputer.R;
import rnfive.djs.cyclingcomputer.antplus.AntPlus_BC;
import rnfive.djs.cyclingcomputer.antplus.AntPlus_BP;
import rnfive.djs.cyclingcomputer.antplus.AntPlus_BS;
import rnfive.djs.cyclingcomputer.antplus.AntPlus_HR;
import rnfive.djs.cyclingcomputer.define.Data;
import rnfive.djs.cyclingcomputer.define.EquipmentSensor;
import rnfive.djs.cyclingcomputer.define.Files;
import rnfive.djs.cyclingcomputer.define.PhoneSensors;
import rnfive.djs.cyclingcomputer.define.enums.SensorState;
import rnfive.djs.cyclingcomputer.define.listeners.IDeviceStateChangeReceiver;
import rnfive.djs.cyclingcomputer.define.runnables.Runnable_UpdateValues;
import rnfive.djs.cyclingcomputer.MainActivity;
import rnfive.djs.cyclingcomputer.define.StaticVariables;
import rnfive.djs.cyclingcomputer.define.Strings;

import static androidx.core.app.NotificationCompat.FLAG_ONLY_ALERT_ONCE;
import static rnfive.djs.cyclingcomputer.MainActivity.iAntHRId;

@Getter
public class Service_Recording extends Service implements LocationListener, IDeviceStateChangeReceiver {

    public static final String TAG = Service_Recording.class.getSimpleName();

    public static final String START_SERVICE = "rn5.djs.cyclingcomputer.RecordingService.START_SERVICE";
    public static final String STOP_SERVICE = "rn5.djs.cyclingcomputer.RecordingService.STOP_SERVICE";
    public static final String START_RECORDING = "rn5.djs.cyclingcomputer.RecordingService.START_RECORDING";
    public static final String STOP_RECORDING = "rn5.djs.cyclingcomputer.RecordingService.STOP_RECORDING";
    public static final String ZERO_POWER = "rn5.djs.cyclingcomputer.RecordingService.ZERO_POWER";

    public static final String CHANNEL_ID = "RECORDING_SERVICE_CHANNEL";
    public static Data data;
    public static FitFile fitFile;

    public static boolean bServiceStarted;
    private static PowerManager.WakeLock wakeLock;

    private static boolean bRecording;
    public static final PhoneSensors phoneSensors = new PhoneSensors();

    private String notificationMsg;

    // Location
    private LocationManager locationManager;
    public static Location location;
    private static boolean bGpsRunning;
    public static boolean serviceRunning;

    private AntPlus_BP bpAnt;
    private AntPlus_HR hrAnt;
    private AntPlus_BS bsAnt;
    private AntPlus_BC bcAnt;

    private boolean notificationRunning;
    private boolean updateValuesRunning;
    private boolean antPlusRunning;
    private boolean recordRunning;

    private final Handler notificationHandler = new Handler();
    private final Handler updateValuesHandler = new Handler();
    private final Handler antPlusHandler = new Handler();
    private final Handler recordHandler = new Handler();

    private final Runnable recordRunnable = new Runnable() {
        @Override
        public void run() {
            if (!StaticVariables.bPaused)
                fitFile.recordMesg();
            getRecordHandler().postDelayed(this,(MainActivity.sport== Sport.RUNNING?2000:1000));
        }
    };
    private final Runnable antPlusRunnable = new Runnable() {
        @Override
        public void run() {
            connectAntPlus();
            getAntPlusHandler().postDelayed(this,2000);
        }
    };
    private final Runnable notificationRunnable = new Runnable() {
        @Override
        public void run() {
            setNotificationMessage();
            getNotificationHandler().postDelayed(this,60000);
        }
    };
    private final Runnable updateValuesRunnable = new Runnable() {
        @Override
        public void run() {
            Executors.newSingleThreadExecutor().execute(new Runnable_UpdateValues());
            getUpdateValuesHandler().postDelayed(this,1000);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        setNotificationMessage();
        data = new Data();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Log.d(TAG, "onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String action = null;
        if (intent != null)
            action = intent.getAction();
        if (action == null)
            action = "NONE";

        switch (action) {
            case START_SERVICE:
                bServiceStarted = true;
                serviceRunning = true;
                startUpdateValues();
                startNotification();
                startAntPlus();
                startGps();
                break;
            case START_RECORDING:
                startRecording();
                notificationMsg = "Recording";
                setNotificationMessage();
                bRecording = true;
                break;
            case STOP_RECORDING:
                bRecording = false;
                stopRecording();
                notificationMsg = null;
                break;
            case ZERO_POWER:
                if (bpAnt != null)
                    bpAnt.zero();
                break;
            case STOP_SERVICE:
                stopUpdateValues();
                stopAntPlus();
                stopGps();
                stopRecording();
                stopNotification();
                bServiceStarted = false;
                serviceRunning = false;
                stopSelf();
                break;
            default:
                break;
        }

        return START_STICKY;
    }

    void setNotificationMessage() {
        Intent notificationIntent = new Intent(this, Service_Recording.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        float d = (data != null ? (float) data.getDescentTot() : 0);
        String unit = (StaticVariables.bMetric?" km":" mi");
        String text = (bRecording ? Strings.getDistanceString(d) + unit : null);

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.bigText(text); //detail mode is the "expanded" notification
        bigText.setBigContentTitle(notificationMsg);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(notificationMsg)
                .setContentText(text)
                .setSmallIcon(R.drawable.app_icon_splash_new)
                .setContentIntent(pendingIntent)
                .setStyle(bigText)
                .build();
        notification.flags = FLAG_ONLY_ALERT_ONCE;

        startForeground(1, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Recording Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            for (NotificationChannel n : manager.getNotificationChannels()) {
                Log.d(TAG, "Notification: " + n.getId());
            }
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void startGps() {
        boolean bGpsEnabled = MainActivity.isGPSEnabled(locationManager);
        if (bGpsEnabled && MainActivity.bGpsGranted) {
            try {
                if (!bGpsRunning)
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
                bGpsRunning = true;
                Log.d(TAG, "startGps()");
            } catch (SecurityException e) {
                Log.e(TAG,"GPS Error. " + e.getMessage());
            }
        }
    }

    private void stopGps() {
        if (!StaticVariables.bStarted) {
            if (bGpsRunning)
                locationManager.removeUpdates(this);
            bGpsRunning = false;
        }
        Log.d(TAG,"stopGps()");
    }

    private boolean connectHr() {
        if (!hrAnt.isSearching()) {
            if (iAntHRId > 1 && !hrAnt.isConnected())
                return true;
            else return iAntHRId == 1 && !hrAnt.isSupported();
        }
        return false;
    }

    void connectAntPlus() {
        long lRunTime = System.currentTimeMillis();
        if (hrAnt == null)
            hrAnt = new AntPlus_HR(iAntHRId, this, this);
        if (lRunTime - hrAnt.getConnectTime() >= MainActivity.iAntInterval) {
            if (connectHr())
                hrAnt.connect();
        }
        if (hrAnt.isSupported()) {
            if (MainActivity.iAntBPId > 0) {
                if (bpAnt == null) {
                    bpAnt = new AntPlus_BP(MainActivity.iAntBPId, this)
                            .withDeviceStateChangeReceiver(this);
                }
                if (lRunTime - bpAnt.getConnectTime() >= MainActivity.iAntInterval) {
                    if (!bpAnt.isConnected() && !bpAnt.isSearching())
                        bpAnt.connect();
                }
            }
            if (MainActivity.iAntBCId > 0) {
                if (bcAnt == null)
                    bcAnt = new AntPlus_BC(MainActivity.iAntBCId, this, false);
                if (lRunTime - bcAnt.getConnectTime() >= MainActivity.iAntInterval) {
                    if (!bcAnt.isConnected() && !bcAnt.isSearching())
                        bcAnt.connect();
                }
            }
            if (MainActivity.iAntBSId > 0) {
                if (bsAnt == null)
                    bsAnt = new AntPlus_BS(MainActivity.iAntBSId, this, false);
                if (lRunTime - bsAnt.getConnectTime() >= MainActivity.iAntInterval) {
                    if (!bsAnt.isConnected() && !bsAnt.isSearching())
                        bsAnt.connect();
                }
            }
            if (MainActivity.iAntBSCId > 0) {
                if (bcAnt == null)
                    bcAnt = new AntPlus_BC(MainActivity.iAntBSCId, this, true);
                if (lRunTime - bcAnt.getConnectTime() >= MainActivity.iAntInterval) {
                    if (!bcAnt.isConnected() && !bcAnt.isSearching()) {
                        bcAnt.connect();
                        if (StaticVariables.bDebug)
                            MainActivity.toastListener.onToast("Cadence connecting...");
                    }
                }
            }
        }
    }

    private void disconnectAntPlus() {
        Files.logMesg("D","disconnectAntPlus", null);
        if (MainActivity.iAntBPId > 0 && bpAnt != null) {
            bpAnt.disconnect();
            bpAnt = null;
        }
        if (iAntHRId > 0 && hrAnt != null) {
            hrAnt.disconnect();
            hrAnt = null;
        }
        if (MainActivity.iAntBSId > 0 && bsAnt != null) {
            bsAnt.disconnect();
            bsAnt = null;
        }
        if (MainActivity.iAntBCId > 0 && bcAnt != null) {
            bcAnt.disconnect();
            bcAnt = null;
        }
        if (MainActivity.iAntBSCId > 0) {
            if (bcAnt != null)
                bcAnt.disconnect();
            if (bsAnt != null)
                bsAnt.disconnect();
            bcAnt = null;
            bsAnt = null;
        }
    }

    private void startUpdateValues() {
        if (!updateValuesRunning)
            updateValuesHandler.post(updateValuesRunnable);
        updateValuesRunning = true;
        Log.d(TAG, "startRecording()");
    }

    private void stopUpdateValues() {
        updateValuesHandler.removeCallbacks(updateValuesRunnable);
        updateValuesRunning = false;
        Log.d(TAG, "startRecording()");
    }

    private void startNotification() {
        if (!notificationRunning)
            notificationHandler.post(notificationRunnable);
        notificationRunning = true;
        Log.d(TAG, "startRecording()");
    }

    private void stopNotification() {
        notificationHandler.removeCallbacks(notificationRunnable);
        notificationRunning = false;
        Log.d(TAG, "startRecording()");
    }

    private void startRecording() {
        if (!bRecording)
            recordHandler.post(recordRunnable);
        bRecording = true;
        Log.d(TAG, "startRecording()");
    }

    private void stopRecording() {
        recordHandler.removeCallbacks(recordRunnable);
        bRecording = false;
        Log.d(TAG, "startRecording()");
    }

    private void startAntPlus() {
        if (!antPlusRunning)
            antPlusHandler.postDelayed(antPlusRunnable,5000);
        antPlusRunning = true;
        Log.d(TAG, "startAntPlus()");
    }

    private void stopAntPlus() {
        if (!StaticVariables.bStarted) {
            disconnectAntPlus();
            antPlusHandler.removeCallbacks(antPlusRunnable);
            antPlusRunning = false;
        }
        Log.d(TAG, "stopAntPlus()");
    }

    @Override
    public void onDeviceStateChange(int id, String sensorId, SensorState sensorState) {
        int type = EquipmentSensor.getType(id);

        Log.d("onDeviceStateChange","MainActivity [" + sensorState + "]");
        switch (type) {
            case EquipmentSensor.POWER:
                if (EquipmentSensor.getBand(id) == EquipmentSensor.BLE) {
                    MainActivity.bBpBleExists = sensorState == SensorState.CONNECTED;
                } else {
                    MainActivity.bBpAntExists = sensorState == SensorState.CONNECTED;
                }
                if (!MainActivity.bBpBleExists && !MainActivity.bBpAntExists)
                    StaticVariables.bBPCadExists = false;
                boolean bpExists = StaticVariables.bBPExists;
                StaticVariables.bBPExists = MainActivity.bBpAntExists || MainActivity.bBpBleExists;
                if (bpExists != StaticVariables.bBPExists)
                    MainActivity.toastListener.onPowerConnect();
                break;
            case EquipmentSensor.HEARTRATE:
                if (hrAnt != null) {
                    if (hrAnt.isConnected()) {
                        StaticVariables.bHRExists = true;
                        break;
                    }
                }
                StaticVariables.bHRExists = false;
                data.setHr(-1);
                break;
        }
        MainActivity.toastListener.onToast(EquipmentSensor.getSensorName(id) + " " + sensorState.getName());
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        startGps();
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        stopGps();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
    
    @Override
    public void onLocationChanged(Location location) {

        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            this.location = location;
            //b_gps_has_accuracy = false;
            StaticVariables.geomagneticField = new GeomagneticField(
                    (float) location.getLatitude(),
                    (float) location.getLongitude(),
                    (float) location.getAltitude(),
                    System.currentTimeMillis());
            data.setGpsAccuracy(0);
            if (location.hasAccuracy()) {
                data.setGpsAccuracy((int) location.getAccuracy());
                // TODO b_gps_has_accuracy = location.getAccuracy() <= f_gps_accuracy_min;
            }

            data.setLatitude(location.getLatitude());
            data.setLongitude(location.getLongitude());
            if (phoneSensors.getSensorPressure() == null) {
                data.setAltitudeValue(location.getAltitude());
                // TODO - add gps altitude delta
            }

            // TODO updateWeather();
            // TODO strava description();

            double distanceP2P = 0;
            if (data.getLocationPrev() != null)
                distanceP2P = location.distanceTo(data.getLocationPrev());
            data.setLocationPrev(location);
            data.updateGPSDistance(distanceP2P);

            float speed = 0.0f;
            if (location.hasSpeed()) {
                speed = location.getSpeed();
                //Log.d(TAG, "Speed[" + speed + "]");
                speed = (speed > StaticVariables.speedMin ? speed : 0.0f);
            }
            float gpsSpeed = (speed + data.getSpeedGpsPrev())/2.0f;
            data.setSpeedGps(gpsSpeed);
            data.setSpeedGpsPrev(speed);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopAntPlus();
        stopUpdateValues();
        stopNotification();
        stopRecording();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
