package rnfive.djs.cyclingcomputer.define;

import android.icu.util.Calendar;
import android.util.Log;

import com.garmin.fit.Activity;
import com.garmin.fit.ActivityMesg;
import com.garmin.fit.ActivityMesgListener;
import com.garmin.fit.DateTime;
import com.garmin.fit.Decode;
import com.garmin.fit.DeveloperDataIdMesg;
import com.garmin.fit.DeveloperField;
import com.garmin.fit.Event;
import com.garmin.fit.EventMesg;
import com.garmin.fit.EventType;
import com.garmin.fit.FieldDescriptionMesg;
import com.garmin.fit.FileCreatorMesg;
import com.garmin.fit.FileEncoder;
import com.garmin.fit.FileIdMesg;
import com.garmin.fit.Fit;
import com.garmin.fit.FitRuntimeException;
import com.garmin.fit.GarminProduct;
import com.garmin.fit.Intensity;
import com.garmin.fit.LapMesg;
import com.garmin.fit.LapMesgListener;
import com.garmin.fit.LapTrigger;
import com.garmin.fit.MesgBroadcaster;
import com.garmin.fit.RecordMesg;
import com.garmin.fit.RecordMesgListener;
import com.garmin.fit.SessionMesg;
import com.garmin.fit.SessionMesgListener;
import com.garmin.fit.SessionTrigger;
import com.garmin.fit.SubSport;
import com.garmin.fit.TimerTrigger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.Nullable;
import rnfive.djs.cyclingcomputer.BuildConfig;
import rnfive.djs.cyclingcomputer.exception.FitFileException;


import static rnfive.djs.cyclingcomputer.MainActivity.filePathApp;
import static rnfive.djs.cyclingcomputer.MainActivity.filePathSummary;
import static rnfive.djs.cyclingcomputer.MainActivity.sDescription;
import static rnfive.djs.cyclingcomputer.MainActivity.sName;
import static rnfive.djs.cyclingcomputer.MainActivity.sport;
import static rnfive.djs.cyclingcomputer.MainActivity.toastListener;
import static rnfive.djs.cyclingcomputer.define.StaticVariables.bBCExists;
import static rnfive.djs.cyclingcomputer.define.StaticVariables.bBPCadExists;
import static rnfive.djs.cyclingcomputer.define.StaticVariables.bMoving;
import static rnfive.djs.cyclingcomputer.define.StaticVariables.bPaused;
import static rnfive.djs.cyclingcomputer.service.Service_Recording.data;
import static rnfive.djs.cyclingcomputer.service.Service_Recording.fitFile;

public class FitFile {
    private static final String TAG = "FitFile";
    private FileEncoder var0;
    private Calendar startDate;
    private String fitFileName;
    private File fitFileDir;
    private boolean bActivity;
    private static final ThreadLocal<SimpleDateFormat> fitSdf = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss",Locale.US));
    private boolean bOpen;

    public FitFile() {}

    public void create(@Nullable String name) {
        startDate = Calendar.getInstance();

        if (name == null) {
            fitFileName = Objects.requireNonNull(fitSdf.get()).format(startDate.getTime()) + ".fit";
            fitFileDir = filePathApp;
            bActivity = true;
        } else {
            fitFileName = name + ".summary";
            fitFileDir = filePathSummary;
        }

        try {
            var0 = new FileEncoder(new File(fitFileDir, fitFileName), Fit.ProtocolVersion.V2_0);
            bOpen = true;
        } catch (FitRuntimeException var11) {
            Log.e(TAG,"Error opening file " + fitFileName);
        }
        if (bActivity) {
            fileIdMesg();
            fileCreatorMesg();
            eventMesg(EventType.START, false);
        }
    }

    public boolean isOpen() {
        return bOpen;
    }

    public void openTmp(String name) throws FitException {
        toastListener.onToast("Loading saved activity.");
        startDate = Calendar.getInstance();
        fitFileName = Objects.requireNonNull(fitSdf.get()).format(startDate.getTime()) + ".fit";
        fitFileDir = filePathApp;
        try {
            var0 = new FileEncoder(new File(fitFileDir,fitFileName), Fit.ProtocolVersion.V2_0);
            bOpen = true;
        } catch (FitRuntimeException var11) {
            Log.e(TAG, "Error opening file " + fitFileName);
            throw new FitException("FitRuntimeException - Error opening file " + fitFileName);
        }
        boolean loaded;
        String msg = "";
        try {
            decode(name, var0);
        } catch (FileNotFoundException e) {
            throw new FitException("Decode failed.  File not found " + name);
        } catch (IOException e) {
            throw new FitException("IOException: " + e.getMessage());
        }
        File origFile = new File(filePathApp,name);
        if (origFile.exists() && origFile.delete())
            toastListener.onToast("Saved activity loaded.");
    }

    private void fileIdMesg() {
        FileIdMesg var1 = new FileIdMesg();
        var1.setTimeCreated(new DateTime(startDate.getTime()));
        var1.setManufacturer(1);
        var1.setType(com.garmin.fit.File.ACTIVITY);
        var1.setSerialNumber(0L);
        var1.setProduct(GarminProduct.ANDROID_ANTPLUS_PLUGIN);
        var0.write(var1);
    }

    private void fileCreatorMesg() {
        FileCreatorMesg var2 = new FileCreatorMesg();
        var2.setSoftwareVersion(BuildConfig.VERSION_CODE);
        var0.write(var2);
    }

    public void recordMesg() {
        RecordMesg var6 = new RecordMesg();
        var6.setAltitude((float) data.getAltitudeValue());
        var6.setDistance((float) data.getDistanceTot());
        if (bBCExists || bBPCadExists)
            var6.setCadence((short) data.getCadence());
        if (data.getSmoothL() > 0 || data.getSmoothR() > 0)
            var6.setCombinedPedalSmoothness((float) (data.getSmoothL() + data.getSmoothR()) / ((data.getSmoothL() > 0 ? 1 : 0) + (data.getSmoothR() > 0 ? 1 : 0)));
        //var6.setDeviceIndex(??? Short);
        var6.setGpsAccuracy((short) data.getGpsAccuracy());
        var6.setGrade(data.getGrade());
        var6.setHeartRate((short) data.getHr());
        if (data.getSmoothL() != -1)
            var6.setLeftPedalSmoothness((float) data.getSmoothL());
        if (data.getSmoothR() != -1)
            var6.setRightPedalSmoothness((float) data.getSmoothR());
        if (data.getBalanceR() != -1)
            var6.setLeftRightBalance((short) (data.getBalanceR() + 128));
        if (data.getTorqueL() != -1)
            var6.setLeftTorqueEffectiveness((float) data.getTorqueL());
        if (data.getTorqueR() != -1)
            var6.setRightTorqueEffectiveness((float) data.getTorqueR());
        var6.setPositionLat(Bearing.semicircleFromDegrees(data.getLatitude()));
        var6.setPositionLong(Bearing.semicircleFromDegrees(data.getLongitude()));
        var6.setPower(data.getPower());
        var6.setSpeed(data.getSpeed());
        //var6.setTemperature(C Byte);
        var6.setTimestamp(new DateTime(Calendar.getInstance().getTime()));
        try {
            var0.write(var6);
        } catch (FitRuntimeException e) {
            Log.e(TAG, "FitRuntimeException" + e.getMessage());
        }
    }

    public void eventMesg(EventType eventType, boolean bPause) {
        EventMesg newEventMesg = new EventMesg();
        newEventMesg.setTimestamp(new DateTime((bPause?Calendar.getInstance().getTime():startDate.getTime())));
        newEventMesg.setTimerTrigger(TimerTrigger.MANUAL);
        newEventMesg.setEvent(Event.TIMER);
        newEventMesg.setEventType(eventType);
        newEventMesg.setEventGroup((short)0);
        var0.write(newEventMesg);
    }

    public void lapMesg() {
        LapMesg newLap = new LapMesg();
        newLap.setTimestamp(new DateTime(Calendar.getInstance().getTime()));
        newLap.setStartTime(new DateTime(startDate.getTime()));
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
        var0.write(newLap);
/*

  start_position_lat (3-1-SINT32): 39.5995534 deg (472441074)
  start_position_long (4-1-SINT32): -104.9204284 deg (-1251749468)
  end_position_lat (5-1-SINT32): 39.5995506 deg (472441041)
  end_position_long (6-1-SINT32): -104.9202795 deg (-1251747692)

 */
    }

    public void closeTmp() {
        eventMesg(EventType.STOP, true);
        try {
            var0.close();
            bOpen = false;
        } catch (FitRuntimeException var10) {
            Log.e(TAG,"Error closing encode " + fitFileName);
        }
    }

    public void close() {

        if (bActivity) {
            eventMesg(EventType.STOP_ALL, false);
            lapMesg();

            EventMesg var4 = new EventMesg();
            var4.setTimestamp(new DateTime(Calendar.getInstance().getTime()));
            var4.setData(1L);
            var4.setEvent(Event.SESSION);
            var4.setEventType(EventType.STOP_DISABLE_ALL);
            var4.setEventGroup((short) 1);
            var0.write(var4);
        }

        SessionMesg var5 = new SessionMesg();
        var5.setTimestamp(new DateTime(Calendar.getInstance().getTime()));
        var5.setStartTime(new DateTime(startDate.getTime()));
        var5.setTotalElapsedTime(data.getMsElapsed()/1000.0f);
        var5.setTotalMovingTime(data.getMsTotM()/1000.0f);
        var5.setTotalTimerTime(data.getMsTot()/1000.0f);
        var5.setTotalDistance((float) data.getDistanceTot());
        var5.setEvent(Event.LAP);
        var5.setNumLaps(1);
        var5.setFirstLapIndex(0);
        var5.setEventType(EventType.STOP);
        var5.setTrigger(SessionTrigger.ACTIVITY_END);
        var5.setSport(sport);
        var5.setSubSport(SubSport.GENERIC);
        var5.setTotalAscent((int) data.getAscentTot());
        var5.setTotalDescent((int) data.getDescentTot());
        var5.setMaxSpeed(data.getSpeedMax());
        var5.setAvgSpeed(data.getSpeedAvg());
        var5.setMaxPower(data.getPowerMax());
        var5.setAvgPower(data.getPowerAvg());
        var5.setNormalizedPower(0);
        var5.setAvgHeartRate((short) data.getHrAvg());
        var5.setMaxHeartRate((short) data.getHrMax());
        var5.setAvgCadence((short) data.getCadenceAvg());
        var5.setMaxCadence((short) data.getCadenceMax());
        var0.write(var5);

        byte[] varNew = {1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, -112, -23, 121, 98, -37};
        DeveloperDataIdMesg varDev = new DeveloperDataIdMesg();

        int l = varNew.length;
        for(int varTmp = 0; varTmp < l; ++varTmp) {
            varDev.setApplicationId(varTmp, varNew[varTmp]);
        }

        varDev.setDeveloperDataIndex((short)0);
        var0.write(varDev);

        FieldDescriptionMesg varName = new FieldDescriptionMesg();
        varName.setDeveloperDataIndex((short)0);
        varName.setFieldDefinitionNumber((short)0);
        varName.setFitBaseTypeId((short)7);
        varName.setFieldName(0, "activity_name");
        var0.write(varName);
        FieldDescriptionMesg varDesc = new FieldDescriptionMesg();
        varDesc.setDeveloperDataIndex((short)0);
        varDesc.setFieldDefinitionNumber((short)1);
        varDesc.setFitBaseTypeId((short)7);
        varDesc.setFieldName(0, "activity_desc");
        var0.write(varDesc);

        ActivityMesg actMesg = new ActivityMesg();
        DeveloperField devName = new DeveloperField(varName, varDev);
        DeveloperField devDesc = new DeveloperField(varDesc, varDev);
        actMesg.addDeveloperField(devName);
        actMesg.addDeveloperField(devDesc);
        devName.setValue(0,sName);
        devDesc.setValue(0,sDescription);
        actMesg.setTimestamp(new DateTime(Calendar.getInstance().getTime()));
        actMesg.setTotalTimerTime((float)data.getMsTot()/1000);
        actMesg.setNumSessions(1);
        actMesg.setType(Activity.MANUAL);
        actMesg.setEvent(Event.ACTIVITY);
        actMesg.setEventType(EventType.STOP);
        var0.write(actMesg);

        try {
            var0.close();
            bOpen = false;
        } catch (FitRuntimeException var10) {
            Log.e(TAG,"Error closing encode " + fitFileName);
        }
    }

    public void delete() {
        try {
            var0.close();
            bOpen = false;
        } catch (FitRuntimeException var10) {
            Log.e(TAG,"Error closing encode " + fitFileName);
        }

        File file = new File(fitFileDir,fitFileName);
        if (file.exists() && file.delete())
            Log.d(TAG,"File Deleted. " + fitFileName);
    }

    public String getFitFileName() {
        return fitFileName;
    }

    /**
     * @param var0 - File name
     * @param fileEncoder - FileEncoder
     * @throws FitFileException -- Throws Fit FitFileException
     */
    private void decode(String var0, FileEncoder fileEncoder) throws FileNotFoundException, IOException, FitException {
        Decode decode = new Decode();
        //decode.skipHeader();        // Use on streams with no header and footer (stream contains FIT defn and data messages only)
        //decode.incompleteStream();  // This suppresses exceptions with unexpected eof (also incorrect crc)
        MesgBroadcaster mesgBroadcaster = new MesgBroadcaster(decode);
        Listener listener = new Listener(fileEncoder);
        File newFile = new File(filePathApp, var0);
        FileInputStream in;

        in = new FileInputStream(newFile);
        if (!decode.checkFileIntegrity(in)) {
            throw new FitException("FIT file integrity failed.");
        }
        in.close();

        in = new FileInputStream(newFile);

        mesgBroadcaster.addListener((SessionMesgListener)listener);
        mesgBroadcaster.addListener((ActivityMesgListener)listener);
        mesgBroadcaster.addListener((LapMesgListener)listener);
        mesgBroadcaster.addListener((RecordMesgListener)listener);

        try {
            decode.read(in, mesgBroadcaster, mesgBroadcaster);
        } catch (FitRuntimeException e) {
            // If a file with 0 data size in it's header  has been encountered,
            // attempt to keep processing the file
            if (decode.getInvalidFileDataSize()) {
                decode.nextFile();
                decode.read(in, mesgBroadcaster, mesgBroadcaster);
            } else {
                Log.e(TAG, "Exception decoding file: " + e.getMessage());
                in.close();
                return;
            }
        }
        in.close();
    }

    private static final class Listener implements SessionMesgListener, ActivityMesgListener, LapMesgListener, RecordMesgListener {
        private final FileEncoder fileEncoder;
        Listener(FileEncoder fileEncoder) {
            this.fileEncoder = fileEncoder;
        }
        public void onMesg(SessionMesg sessionMesg) {
            fileEncoder.write(sessionMesg);
        }
        public void onMesg(ActivityMesg activityMesg) {
            fileEncoder.write(activityMesg);
        }
        public void onMesg(LapMesg lapMesg) {
            fileEncoder.write(lapMesg);
        }
        public void onMesg(RecordMesg recordMesg) {
            fileEncoder.write(recordMesg);
        }
    }

}
