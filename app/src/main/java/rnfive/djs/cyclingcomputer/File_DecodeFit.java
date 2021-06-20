package rnfive.djs.cyclingcomputer;

import com.garmin.fit.ActivityMesg;
import com.garmin.fit.ActivityMesgListener;
import com.garmin.fit.Decode;
import com.garmin.fit.DeveloperField;
import com.garmin.fit.FitRuntimeException;
import com.garmin.fit.LapMesg;
import com.garmin.fit.LapMesgListener;
import com.garmin.fit.MesgBroadcaster;
import com.garmin.fit.RecordMesg;
import com.garmin.fit.RecordMesgListener;
import com.garmin.fit.SessionMesg;
import com.garmin.fit.SessionMesgListener;
import com.garmin.fit.Sport;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import static rnfive.djs.cyclingcomputer.MainActivity.filePathApp;
import static rnfive.djs.cyclingcomputer.MainActivity.filePathSummary;

public class File_DecodeFit {
    private static SessionMesg sessionMesg;
    private static ArrayList<LapMesg> lapMesgArrayList;
    private static ActivityMesg activityMesg;
    private static ArrayList<RecordMesg> recordMesgArrayList;
    private static String activityName;
    private static String activityDesc;
    private static File dir;

    File_DecodeFit() {
        sessionMesg = new SessionMesg();
        lapMesgArrayList = new ArrayList<>();
        activityMesg = new ActivityMesg();
        recordMesgArrayList = new ArrayList<>();
        activityName = "";
        activityDesc = "";
    }

    public void decode(String var0) {
        Decode var1 = new Decode();
        MesgBroadcaster var2 = new MesgBroadcaster(var1);
        File_DecodeFit.activityMesgListener activityMesgListener  = new File_DecodeFit.activityMesgListener();
        File_DecodeFit.sessionMesgListener sessionMesgListener = new File_DecodeFit.sessionMesgListener();
        File_DecodeFit.lapMesgListener lapMesgListener = new File_DecodeFit.lapMesgListener();
        File_DecodeFit.recordMesgListener recordMesgListener = new File_DecodeFit.recordMesgListener();

        FileInputStream var4;

        if (var0.length()>7 && var0.substring(var0.length()-7,var0.length()).equals("summary"))
            dir = filePathSummary;
        else
            dir = filePathApp;
        File newFile = new File(dir, var0);
        if (newFile.exists()) {
            try {
                var4 = new FileInputStream(newFile);
            } catch (IOException var24) {
                throw new RuntimeException("Error opening file " + var0 + " [1]");
            }

            try {
                if (!var1.checkFileIntegrity(var4)) {
                    throw new RuntimeException("FIT file integrity failed.");
                }
            } catch (RuntimeException var22) {
                System.err.print("Exception Checking File Integrity: ");
                System.err.println(var22.getMessage());
                System.err.println("Trying to continue...");
            } finally {
                try {
                    var4.close();
                } catch (IOException var18) {
                    throw new RuntimeException(var18);
                }
            }

            try {
                var4 = new FileInputStream(new File(dir, var0));
            } catch (IOException var21) {
                throw new RuntimeException("Error opening file " + var0 + " [2]");
            }

            var2.addListener(activityMesgListener);
            var2.addListener(sessionMesgListener);
            var2.addListener(lapMesgListener);
            var2.addListener(recordMesgListener);

            try {
                var1.read(var4, var2, var2);
            } catch (FitRuntimeException var25) {
                if (!var1.getInvalidFileDataSize()) {
                    System.err.print("Exception decoding file: ");
                    System.err.println(var25.getMessage());

                    try {
                        var4.close();
                        return;
                    } catch (IOException var19) {
                        throw new RuntimeException(var19);
                    }
                }

                var1.nextFile();
                var1.read(var4, var2, var2);
            }

            try {
                var4.close();
            } catch (IOException var20) {
                throw new RuntimeException(var20);
            }

            System.out.println("Decoded FIT file " + var0 + ".");
        } else {
            System.out.println("Decoded FIT file " + var0 + ". File does not exist.");
        }
    }

    SessionMesg getSessionMesg() {
        return sessionMesg;
    }

    ActivityMesg getActivityMesg() {
        return activityMesg;
    }

    ArrayList<LapMesg> getLapMesgArrayList() {
        return lapMesgArrayList;
    }

    ArrayList<RecordMesg> getRecordMesgArrayList() {
        return recordMesgArrayList;
    }

    String getActivityName() {
        return activityName;
    }

    String getActivityDesc() {
        return activityDesc;
    }

    private static class sessionMesgListener implements SessionMesgListener {
        private sessionMesgListener() {
        }

        public void onMesg(SessionMesg var1) {
            sessionMesg = var1;
            if (activityName.equals("")) {
                activityName = "Activity";
                if (sessionMesg.getSport() != null) {
                    if (sessionMesg.getSport().equals(Sport.CYCLING))
                        activityName = "Ride";
                    else if (sessionMesg.getSport().equals(Sport.RUNNING))
                        activityName = "Run";
                }
            }
        }
    }
    private static class activityMesgListener implements ActivityMesgListener {
        private activityMesgListener() {
        }

        public void onMesg(ActivityMesg var1) {
            activityMesg = var1;
            for (DeveloperField val:var1.getDeveloperFields()) {
                switch (val.getName()) {
                    case "activity_name" :
                        activityName = val.getStringValue();
                        break;
                    case "activity_desc" :
                        activityDesc = val.getStringValue();
                        break;
                }
            }
        }
    }

    private static class lapMesgListener implements LapMesgListener {
        private lapMesgListener() {
        }

        public void onMesg(LapMesg var1) {
            lapMesgArrayList.add(var1);
        }
    }

    private static class recordMesgListener implements RecordMesgListener {
        private recordMesgListener() {
        }

        public void onMesg(RecordMesg var1) {
            if (var1.getHeartRate() == null)
                var1.setHeartRate((short)0);
            if (var1.getAltitude() == null)
                var1.setAltitude(0f);
            if (var1.getCadence() == null)
                var1.setCadence((short)0);
            if (var1.getPositionLat() == null)
                var1.setPositionLat(0);
            if (var1.getPositionLong() == null)
                var1.setPositionLong(0);
            if (var1.getPower() == null)
                var1.setPower(0);
            if (var1.getRightPedalSmoothness() == null)
                var1.setRightPedalSmoothness(0f);
            if (var1.getLeftPedalSmoothness() == null)
                var1.setLeftPedalSmoothness(0f);
            if (var1.getLeftRightBalance() == null)
                var1.setLeftRightBalance((short)128);
            if (var1.getRightTorqueEffectiveness() == null)
                var1.setRightTorqueEffectiveness(0f);
            if (var1.getLeftTorqueEffectiveness() == null)
                var1.setLeftTorqueEffectiveness(0f);
            if (var1.getSpeed() == null)
                var1.setSpeed(0f);
            if (var1.getDistance() == null)
                var1.setDistance(0f);

            //var6.setCombinedPedalSmoothness(per F);
            //var6.setDeviceIndex(??? Short);
            //var6.setGpsAccuracy(Short.parseShort(s_act_gps_accuracy));
            //var6.setGrade(% Float);

            recordMesgArrayList.add(var1);
        }
    }

    /*
    private static class Listener implements SessionMesgListener, LapMesgListener, RecordMesgListener {
        private Listener() {
        }


        public void onMesg(LapMesg var1) {
            lapMesgArrayList.add(var1);
        }

        public void onMesg(ActivityMesg var1) {
            activityMesg = var1;
        }

        public void onMesg(RecordMesg var1) {
            recordMesgArrayList.add(var1);
        }

        private void printDeveloperData(Mesg var1) {
            Iterator var2 = var1.getDeveloperFields().iterator();

            while(true) {
                DeveloperField var3;
                do {
                    if (!var2.hasNext()) {
                        return;
                    }

                    var3 = (DeveloperField)var2.next();
                } while(var3.getNumValues() < 1);

                if (var3.isDefined()) {
                    System.out.print("   " + var3.getName());
                    if (var3.getUnits() != null) {
                        System.out.print(" [" + var3.getUnits() + "]");
                    }

                    System.out.print(": ");
                } else {
                    System.out.print("   Undefined Field: ");
                }

                System.out.print(var3.getValue(0));

                for(int var4 = 1; var4 < var3.getNumValues(); ++var4) {
                    System.out.print("," + var3.getValue(var4));
                }

                System.out.println();
            }
        }

        public void onDescription(DeveloperFieldDescription var1) {
            System.out.println("New Developer Field Description");
            System.out.println("   App Id: " + var1.getApplicationId());
            System.out.println("   App Version: " + var1.getApplicationVersion());
            System.out.println("   Field Num: " + var1.getFieldDefinitionNumber());
        }

        private void printValues(Mesg var1, int var2) {
            Iterable var3 = var1.getOverrideField((short)var2);
            Field var4 = Factory.createField(var1.getNum(), var2);
            boolean var5 = false;
            if (var4 != null) {
                Iterator var6 = var3.iterator();

                while(var6.hasNext()) {
                    FieldBase var7 = (FieldBase)var6.next();
                    if (!var5) {
                        System.out.println("   " + var4.getName() + ":");
                        var5 = true;
                    }

                    if (var7 instanceof Field) {
                        System.out.println("      native: " + var7.getValue());
                    } else {
                        System.out.println("      override: " + var7.getValue());
                    }
                }

            }
        }
    }
        */
}
