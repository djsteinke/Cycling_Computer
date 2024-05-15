package rnfive.htfu.cyclingcomputer.define;

import static rnfive.htfu.cyclingcomputer.MainActivity.filePathApp;
import static rnfive.htfu.cyclingcomputer.MainActivity.sDescription;
import static rnfive.htfu.cyclingcomputer.MainActivity.sName;
import static rnfive.htfu.cyclingcomputer.MainActivity.sport;
import static rnfive.htfu.cyclingcomputer.define.StaticVariables.bBCExists;
import static rnfive.htfu.cyclingcomputer.define.StaticVariables.bBPCadExists;
import static rnfive.htfu.cyclingcomputer.service.Service_Recording.data;

import android.icu.util.Calendar;
import android.util.Log;

import androidx.annotation.Nullable;

import com.garmin.fit.Activity;
import com.garmin.fit.ActivityMesg;
import com.garmin.fit.DateTime;
import com.garmin.fit.DeveloperDataIdMesg;
import com.garmin.fit.DeveloperField;
import com.garmin.fit.DeviceIndex;
import com.garmin.fit.DeviceInfoMesg;
import com.garmin.fit.Event;
import com.garmin.fit.EventMesg;
import com.garmin.fit.EventType;
import com.garmin.fit.FieldDescriptionMesg;
import com.garmin.fit.FileEncoder;
import com.garmin.fit.FileIdMesg;
import com.garmin.fit.Fit;
import com.garmin.fit.FitRuntimeException;
import com.garmin.fit.GarminProduct;
import com.garmin.fit.Intensity;
import com.garmin.fit.LapMesg;
import com.garmin.fit.LapTrigger;
import com.garmin.fit.Manufacturer;
import com.garmin.fit.Mesg;
import com.garmin.fit.RecordMesg;
import com.garmin.fit.SessionMesg;
import com.garmin.fit.SessionTrigger;
import com.garmin.fit.SubSport;
import com.garmin.fit.TimerTrigger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import rnfive.htfu.cyclingcomputer.BuildConfig;


public class FitEncode {
    private static final String TAG = "FitEncode";
    private final List<Mesg> messages = new ArrayList<>();
    private final DateTime startTime = new DateTime(Calendar.getInstance().getTime());
    private static final ThreadLocal<SimpleDateFormat> fitSdf = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US));
    private DateTime lapStartTime = new DateTime(Calendar.getInstance().getTime());
    private int laps;
    private String fileName;
    private File fileDir;
    private boolean bActivity;

    public FitEncode() {}

    public String getFileName() {
        return fileName;
    }

    public void create(@Nullable String name) {
        if (name == null) {
            fileName = Objects.requireNonNull(fitSdf.get()).format(startTime.getDate()) + ".fit";
            fileDir = filePathApp;
            bActivity = true;
        }

        if (bActivity) {
            start();
        }
    }

    public void start() {
        eventMsg(EventType.START);
    }

    public void recordMsg() {
        RecordMesg recordMesg = new RecordMesg();
        recordMesg.setTimestamp(new DateTime(Calendar.getInstance().getTime()));
        recordMesg.setAltitude((float) data.getAltitudeValue());
        recordMesg.setDistance((float) data.getDistanceTot());
        if (bBCExists || bBPCadExists)
            recordMesg.setCadence((short) data.getCadence());
        if (data.getSmoothL() > 0 || data.getSmoothR() > 0)
            recordMesg.setCombinedPedalSmoothness((float) (data.getSmoothL() + data.getSmoothR()) / ((data.getSmoothL() > 0 ? 1 : 0) + (data.getSmoothR() > 0 ? 1 : 0)));
        //var6.setDeviceIndex(??? Short);
        recordMesg.setGpsAccuracy((short) data.getGpsAccuracy());
        recordMesg.setGrade((float)data.getGrade());
        recordMesg.setHeartRate((short) data.getHr());
        if (data.getSmoothL() != -1)
            recordMesg.setLeftPedalSmoothness((float) data.getSmoothL());
        if (data.getSmoothR() != -1)
            recordMesg.setRightPedalSmoothness((float) data.getSmoothR());
        if (data.getBalanceR() != -1)
            recordMesg.setLeftRightBalance((short) (data.getBalanceR() + 128));
        if (data.getTorqueL() != -1)
            recordMesg.setLeftTorqueEffectiveness((float) data.getTorqueL());
        if (data.getTorqueR() != -1)
            recordMesg.setRightTorqueEffectiveness((float) data.getTorqueR());
        recordMesg.setPositionLat(Bearing.semicircleFromDegrees(data.getLatitude()));
        recordMesg.setPositionLong(Bearing.semicircleFromDegrees(data.getLongitude()));
        recordMesg.setPower(data.getPower());
        recordMesg.setSpeed(data.getSpeed());
        //var6.setTemperature(C Byte);
        messages.add(recordMesg);
    }

    public void eventMsg(EventType eventType) {
        EventMesg newEventMsg = new EventMesg();
        newEventMsg.setTimestamp(new DateTime(Calendar.getInstance().getTime()));
        newEventMsg.setTimerTrigger(TimerTrigger.MANUAL);
        newEventMsg.setEvent(Event.TIMER);
        newEventMsg.setEventType(eventType);
        //newEventMsg.setEventGroup((short)0);
        messages.add(newEventMsg);
    }

    public void lapMsg() {
        LapMesg newLap = new LapMesg();
        newLap.setTimestamp(new DateTime(Calendar.getInstance().getTime()));
        newLap.setStartTime(lapStartTime);
        newLap.setTotalElapsedTime(data.getMsElapsedLap()/1000.0f);
        newLap.setTotalMovingTime(data.getMsLapM()/1000.0f);
        newLap.setTotalTimerTime(data.getMsTotM()/1000.0f);
        newLap.setTotalDistance((float) data.getDistanceLap());
        newLap.setEvent(Event.LAP);
        newLap.setEventType(EventType.STOP);
        newLap.setIntensity(Intensity.ACTIVE);
        newLap.setLapTrigger(LapTrigger.SESSION_END);
        newLap.setSport(sport);
        newLap.setTotalAscent((int) data.getAscentLap());
        newLap.setTotalDescent((int) data.getDescentLap());
        newLap.setMaxSpeed(data.getSpeedMaxLap());
        newLap.setAvgSpeed(data.getSpeedAvgLap());
        newLap.setMaxPower(data.getPowerMaxLap());
        newLap.setAvgPower(data.getPowerAvgLap());
        newLap.setAvgHeartRate((short) data.getHrAvgLap());
        newLap.setMaxHeartRate((short) data.getHrMaxLap());
        newLap.setAvgCadence((short) data.getCadenceAvgLap());
        newLap.setMaxCadence((short) data.getCadenceMaxLap());
        messages.add(newLap);

        lapStartTime = new DateTime(Calendar.getInstance().getTime());
        laps ++;
    }

    public void close() {
        if (bActivity) {
            eventMsg(EventType.STOP_ALL);
            lapMsg();
        }

        SessionMesg sessionMsg = new SessionMesg();
        sessionMsg.setTimestamp(new DateTime(Calendar.getInstance().getTime()));
        sessionMsg.setStartTime(startTime);
        sessionMsg.setTotalElapsedTime(data.getMsElapsed()/1000.0f);
        sessionMsg.setTotalMovingTime(data.getMsTotM()/1000.0f);
        sessionMsg.setTotalTimerTime(data.getMsTot()/1000.0f);
        sessionMsg.setTotalDistance((float) data.getDistanceTot());
        sessionMsg.setEvent(Event.LAP);
        sessionMsg.setNumLaps(1);
        sessionMsg.setFirstLapIndex(0);
        sessionMsg.setEventType(EventType.STOP);
        sessionMsg.setTrigger(SessionTrigger.ACTIVITY_END);
        sessionMsg.setSport(sport);
        sessionMsg.setSubSport(SubSport.GENERIC);
        sessionMsg.setTotalAscent((int) data.getAscentTot());
        sessionMsg.setTotalDescent((int) data.getDescentTot());
        sessionMsg.setMaxSpeed(data.getSpeedMax());
        sessionMsg.setAvgSpeed(data.getSpeedAvg());
        sessionMsg.setMaxPower(data.getPowerMax());
        sessionMsg.setAvgPower(data.getPowerAvg());
        //sessionMsg.setNormalizedPower(0);
        sessionMsg.setAvgHeartRate((short) data.getHrAvg());
        sessionMsg.setMaxHeartRate((short) data.getHrMax());
        sessionMsg.setAvgCadence((short) data.getCadenceAvg());
        sessionMsg.setMaxCadence((short) data.getCadenceMax());
        messages.add(sessionMsg);

        byte[] varNew = {1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, -112, -23, 121, 98, -37};
        DeveloperDataIdMesg varDev = new DeveloperDataIdMesg();

        int l = varNew.length;
        for(int varTmp = 0; varTmp < l; ++varTmp) {
            varDev.setApplicationId(varTmp, varNew[varTmp]);
        }

        varDev.setDeveloperDataIndex((short)0);
        messages.add(varDev);

        FieldDescriptionMesg varName = new FieldDescriptionMesg();
        varName.setDeveloperDataIndex((short)0);
        varName.setFieldDefinitionNumber((short)0);
        varName.setFitBaseTypeId((short)7);
        varName.setFieldName(0, "activity_name");
        messages.add(varName);
        FieldDescriptionMesg varDesc = new FieldDescriptionMesg();
        varDesc.setDeveloperDataIndex((short)0);
        varDesc.setFieldDefinitionNumber((short)1);
        varDesc.setFitBaseTypeId((short)7);
        varDesc.setFieldName(0, "activity_desc");
        messages.add(varDesc);

        ActivityMesg actMsg = new ActivityMesg();
        DeveloperField devName = new DeveloperField(varName, varDev);
        DeveloperField devDesc = new DeveloperField(varDesc, varDev);
        devName.setValue(0,sName);
        devDesc.setValue(0,sDescription);
        actMsg.addDeveloperField(devName);
        actMsg.addDeveloperField(devDesc);
        actMsg.setTimestamp(new DateTime(Calendar.getInstance().getTime()));
        actMsg.setTotalTimerTime((float)data.getMsTot()/1000);
        actMsg.setNumSessions(1);
        actMsg.setType(Activity.MANUAL);
        actMsg.setEvent(Event.ACTIVITY);
        actMsg.setEventType(EventType.STOP);
        messages.add(actMsg);

        save();
    }

    public void save() {
        // The combination of file type, manufacturer id, product id, and serial number should be unique.
        // When available, a non-random serial number should be used.

        FileIdMesg fileIdMsg = new FileIdMesg();
        fileIdMsg.setTimeCreated(startTime);
        fileIdMsg.setManufacturer(1);
        fileIdMsg.setType(com.garmin.fit.File.ACTIVITY);
        fileIdMsg.setSerialNumber(0L);
        fileIdMsg.setProduct(GarminProduct.ANDROID_ANTPLUS_PLUGIN);

        // A Device Info message is a BEST PRACTICE for FIT ACTIVITY files
        DeviceInfoMesg deviceInfoMesg = new DeviceInfoMesg();
        deviceInfoMesg.setDeviceIndex(DeviceIndex.CREATOR);
        deviceInfoMesg.setManufacturer(Manufacturer.DEVELOPMENT);
        deviceInfoMesg.setProduct((int) 0);
        deviceInfoMesg.setProductName("Cycling Computer");
        deviceInfoMesg.setSerialNumber((long) 0L);
        deviceInfoMesg.setSoftwareVersion((float) BuildConfig.VERSION_CODE);
        deviceInfoMesg.setTimestamp(startTime);

        // Create the output stream
        FileEncoder encode;

        try {
            encode = new FileEncoder(new java.io.File(fileDir, fileName), Fit.ProtocolVersion.V2_0);
        } catch (FitRuntimeException e) {
            Log.e(TAG,"Error opening encode " + fileName);
            return;
        }

        encode.write(fileIdMsg);
        encode.write(deviceInfoMesg);

        for (Mesg message : messages) {
            encode.write(message);
        }

        // Close the output stream
        try {
            encode.close();
        } catch (FitRuntimeException e) {
            Log.e(TAG, "Error closing encode.");
            return;
        }
        Log.d(TAG, "Encoded FIT Activity file " + fileName);
    }

}
