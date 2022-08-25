package rnfive.htfu.cyclingcomputer.service;

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
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.garmin.fit.Sport;

import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import lombok.Getter;
import rnfive.htfu.cyclingcomputer.define.FitFile;
import rnfive.htfu.cyclingcomputer.R;
import rnfive.htfu.cyclingcomputer.antplus.AntPlus_BC;
import rnfive.htfu.cyclingcomputer.antplus.AntPlus_BP;
import rnfive.htfu.cyclingcomputer.antplus.AntPlus_BS;
import rnfive.htfu.cyclingcomputer.antplus.AntPlus_HR;
import rnfive.htfu.cyclingcomputer.define.Data;
import rnfive.htfu.cyclingcomputer.define.EquipmentSensor;
import rnfive.htfu.cyclingcomputer.define.Files;
import rnfive.htfu.cyclingcomputer.define.PhoneSensors;
import rnfive.htfu.cyclingcomputer.define.RecordingHandlerThread;
import rnfive.htfu.cyclingcomputer.define.enums.Action;
import rnfive.htfu.cyclingcomputer.define.enums.SensorState;
import rnfive.htfu.cyclingcomputer.define.listeners.IDeviceStateChangeReceiver;
import rnfive.htfu.cyclingcomputer.define.runnables.Runnable_UpdateValues;
import rnfive.htfu.cyclingcomputer.MainActivity;
import rnfive.htfu.cyclingcomputer.define.StaticVariables;
import rnfive.htfu.cyclingcomputer.define.Strings;

import static androidx.core.app.NotificationCompat.FLAG_ONLY_ALERT_ONCE;
import static rnfive.htfu.cyclingcomputer.MainActivity.iAntHRId;
import static rnfive.htfu.cyclingcomputer.define.enums.Action.START;
import static rnfive.htfu.cyclingcomputer.define.enums.Action.STOP;

@Getter
public class Service_Recording extends Service implements LocationListener, IDeviceStateChangeReceiver {

    public static final String TAG = Service_Recording.class.getSimpleName();

    public static final String START_SERVICE = "rnfive.htfu.cyclingcomputer.RecordingService.START_SERVICE";
    public static final String STOP_SERVICE = "rnfive.htfu.cyclingcomputer.RecordingService.STOP_SERVICE";
    public static final String START_RECORDING = "rnfive.htfu.cyclingcomputer.RecordingService.START_RECORDING";
    public static final String STOP_RECORDING = "rnfive.htfu.cyclingcomputer.RecordingService.STOP_RECORDING";
    public static final String ZERO_POWER = "rnfive.htfu.cyclingcomputer.RecordingService.ZERO_POWER";
    public static final String CRASH = "rnfive.htfu.cyclingcomputer.RecordingService.CRASH";

    public static final String CHANNEL_ID = "RECORDING_SERVICE_CHANNEL";
    public static Data data;
    public static FitFile fitFile;

    public static boolean bServiceStarted;
    private static PowerManager.WakeLock wakeLock;

    private static boolean bRecording;
    public static final PhoneSensors phoneSensors = new PhoneSensors();

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
    //private final Handler updateValuesHandler = new Handler();
    private Handler antPlusHandler;
    //private final Handler recordHandler = new Handler();

    //private final RecordingHandlerThread handlerThread = new RecordingHandlerThread("RecordingLooper");
    private final HandlerThread antThread = new HandlerThread("AntThread");
    private Handler handler;
    private int updateMaxTime;

    private final Runnable antPlusRunnable = new Runnable() {
        @Override
        public void run() {
            connectAntPlus();
            antPlusHandler.postDelayed(this,2000);
        }
    };
    private final Runnable notificationRunnable = new Runnable() {
        @Override
        public void run() {
            setNotificationMessage();
            handler.postDelayed(this,60000);
        }
    };
    private final Runnable updateValuesRunnable = new Runnable() {
        @Override
        public void run() {
            long t1 = System.currentTimeMillis();
            new Runnable_UpdateValues().run();
            if (StaticVariables.bStarted && !StaticVariables.bPaused) {
                fitFile.recordMesg();
            }
            //Executors.newSingleThreadExecutor().execute(new Runnable_UpdateValues());
            handler.postDelayed(this,1000);
            long t = System.currentTimeMillis() - t1;
            updateMaxTime = (int) Math.max(updateMaxTime, t);
            //Log.d(TAG, "updateValuesRunnable[" + t + "]");
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

        antThread.start();
        antPlusHandler = new Handler(antThread.getLooper());
        //handlerThread.start();
        //handler = new Handler(handlerThread.getLooper());
        handler = new Handler();
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
                service(START);
                break;
            case START_RECORDING:
            case STOP_RECORDING:
                setNotificationMessage();
                break;
            case ZERO_POWER:
                if (bpAnt != null)
                    bpAnt.zero();
                break;
            case STOP_SERVICE:
                service(STOP);
                break;
            case CRASH:
                if (fitFile != null && fitFile.isOpen())
                    fitFile.closeTmp();
                service(STOP);
                break;
            default:
                break;
        }

        return START_STICKY;
    }

    private void service(Action action) {
        updateValues(action);
        antPlus(action);
        gps(action);
        notification(action);
        bServiceStarted = (action == START);
        serviceRunning = (action == START);
        if (action == STOP) {
            //handlerThread.quit();
            MainActivity.toastListener.onToast("Max MS[" + updateMaxTime + "]");
            stopSelf();
        }
    }

    void setNotificationMessage() {
        Intent notificationIntent = new Intent(this, Service_Recording.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        float d = (data != null ? (float) data.getDistanceTot() : 0);
        String text = null;
        String title = null;
        if (StaticVariables.bStarted) {
            text = Strings.getDistanceString(d) + (StaticVariables.bMetric?" km":" mi");
            text += "\n" + Strings.getSpeedString(data.getSpeedAvg()) + (StaticVariables.bMetric?" kph":" mph");
            title = (StaticVariables.bPaused ? "Paused" : "Recording");
        }

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.bigText(text); //detail mode is the "expanded" notification
        bigText.setBigContentTitle(title);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.app_icon_splash_new)
                .setContentIntent(pendingIntent)
                .setStyle(bigText)
                .build();
        notification.flags = FLAG_ONLY_ALERT_ONCE;

        startForeground(1, notification);
    }

    private void createNotificationChannel() {
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

    private void gps(Action action) {
        switch (action) {
            case START:
                boolean bGpsEnabled = MainActivity.isGPSEnabled(locationManager);
                if (bGpsEnabled && MainActivity.bGpsGranted) {
                    try {
                        if (!bGpsRunning)
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
                        bGpsRunning = true;
                    } catch (SecurityException e) {
                        Log.e(TAG,"GPS Error. " + e.getMessage());
                    }
                }
                break;
            case STOP:
                if (!StaticVariables.bStarted) {
                    if (bGpsRunning)
                        locationManager.removeUpdates(this);
                    bGpsRunning = false;
                }
                break;
            default:
                break;
        }
        Log.d(TAG, "gps(" + action + ")");
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
        if (bpAnt != null) {
            bpAnt.disconnect();
            bpAnt = null;
        }
        if (hrAnt != null) {
            hrAnt.disconnect();
            hrAnt = null;
        }
        if (bsAnt != null) {
            bsAnt.disconnect();
            bsAnt = null;
        }
        if (bcAnt != null) {
            bcAnt.disconnect();
            bcAnt = null;
        }
    }

    private void updateValues(Action action) {
        if (action == START) {
            if (!updateValuesRunning)
                handler.post(updateValuesRunnable);
            updateValuesRunning = true;
        } else {
            handler.removeCallbacks(updateValuesRunnable);
            updateValuesRunning = false;
        }
        Log.d(TAG, "updateValues(" + action + ")");
    }

    private void notification(Action action) {
        if (action == START) {
            if (!notificationRunning)
                handler.post(notificationRunnable);
            notificationRunning = true;
        } else {
            handler.removeCallbacks(notificationRunnable);
            notificationRunning = false;
        }
        Log.d(TAG, "notification(" + action + ")");
    }

    private void antPlus(Action action) {
        switch (action) {
            case START:
                if (!antPlusRunning)
                    antPlusHandler.postDelayed(antPlusRunnable,5000);
                antPlusRunning = true;
                break;
            case STOP:
                if (!StaticVariables.bStarted) {
                    disconnectAntPlus();
                    antPlusHandler.removeCallbacks(antPlusRunnable);
                    antPlusRunning = false;
                }
                break;
            default:
                break;
        }
        Log.d(TAG, "antPlus(" + action + ")");
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
        gps(START);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        gps(STOP);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
    
    @Override
    public void onLocationChanged(Location location) {
        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
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

            float speed = 0.0f;
            if (location.hasSpeed()) {
                speed = location.getSpeed();
                //Log.d(TAG, "Speed[" + speed + "]");
                speed = (speed > StaticVariables.speedMin ? speed : 0.0f);
            }
            float gpsSpeed = (speed + data.getSpeedGpsPrev())/2.0f;
            data.setSpeedGps(gpsSpeed);
            data.setSpeedGpsPrev(speed);

            if (phoneSensors.getSensorPressure() == null) {
                data.setAltitudeValue(location.getAltitude());
                // TODO - add gps altitude delta
            }

            // TODO updateWeather();
            // TODO strava description();

            if (gpsSpeed > 0) {
                Service_Recording.location = location;
                data.setLatitude(location.getLatitude());
                data.setLongitude(location.getLongitude());
                double distanceP2P = 0;
                if (data.getLocationPrev() != null)
                    distanceP2P = location.distanceTo(data.getLocationPrev());
                data.setLocationPrev(location);
                data.updateGPSDistance(distanceP2P);
            }
        }
    }

    @Override
    public void onDestroy() {
        service(STOP);
        antThread.quit();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
