package rnfive.djs.cyclingcomputer.define.runnables;

import android.util.Log;

import java.util.concurrent.Executors;

import rnfive.djs.cyclingcomputer.define.Arrays;

import static rnfive.djs.cyclingcomputer.define.PhoneSensors.absPressure;
import static rnfive.djs.cyclingcomputer.define.StaticVariables.bMoving;
import static rnfive.djs.cyclingcomputer.define.StaticVariables.bStarted;
import static rnfive.djs.cyclingcomputer.define.StaticVariables.darkSkyResponse;
import static rnfive.djs.cyclingcomputer.define.StaticVariables.lastUpdateValuesMS;
import static rnfive.djs.cyclingcomputer.define.runnables.Runnable_GetWeatherInfo.intervalMS;
import static rnfive.djs.cyclingcomputer.define.runnables.Runnable_GetWeatherInfo.lastRequestMS;
import static rnfive.djs.cyclingcomputer.service.Service_Recording.data;
import static rnfive.djs.cyclingcomputer.service.Service_Recording.phoneSensors;

public class Runnable_UpdateValues implements Runnable{

    private static final String TAG = Runnable_UpdateValues.class.getSimpleName();
    private static final float pressureZero = 1013.25f;

    public Runnable_UpdateValues() {}

    @Override
    public void run() {

        long lMsCurr = System.currentTimeMillis();
        int iMsDiff = (int) (lastUpdateValuesMS>0 ? (lMsCurr-lastUpdateValuesMS) : 0);

        data.addMS(iMsDiff);

        data.updateSpeed();
        data.updateCadence();
        data.updateHeartRate();
        data.updateDistance();
        data.updatePower();

        updateAltitude();
        if (bMoving) {
            updateGrade();
        }

        // Log.d(TAG, "LastMS[" + lastRequestMS + "] Lat[" + data.getLatitude() + "] Lon[" + data.getLongitude() + "]");
        if (lastRequestMS < (lMsCurr-intervalMS) && data.getLatitude() != 0 && data.getLongitude() != 0) {
            Executors.newSingleThreadExecutor().execute(new Runnable_GetWeatherInfo());
            lastRequestMS = lMsCurr;
        }

        lastUpdateValuesMS = lMsCurr;
    }

    private static void updateAltitude() {
        if (phoneSensors.getPressure() > 0) {
            if (!bStarted) {
                absPressure = ((darkSkyResponse != null ? darkSkyResponse.getPressure() : pressureZero));
            }
            double altitudePrev = data.getAltitude();
            double altTest = 0.0d;
            if (!bStarted||bMoving) {
                //data.setAltitude((1 - StrictMath.pow(phoneSensors.getPressure() / absPressure, 1 / 5.25588)) / 0.0000225577);
                altTest = (1 - StrictMath.pow(phoneSensors.getPressureCurr() / absPressure, 1 / 5.25588)) / 0.0000225577;
                data.setAltitude(altTest);
            }

            if (bStarted && bMoving)
                data.updateAscent(altitudePrev, data.getAltitude());
            Log.d(TAG, "Altitude[" + data.getAltitude() + " / " + altTest + "] pressure[" + phoneSensors.getPressure() + " / " + phoneSensors.getPressureCurr() + " ]");
        }
    }

    private static void updateGrade() {
        float distanceP2P = (float) (data.getDistanceTot() - data.getDistancePrev());
        Arrays.updateArray(data.getGradeArray()[0], (float) data.getAltitude());
        float d = (data.getGradeArray()[1][0] != null ? data.getGradeArray()[1][0] : 0.0f);
        Arrays.updateArray(data.getGradeArray()[1], d + distanceP2P);
        int iD = 1;
        float fDistanctTmp = 0.0f;
        int gradeArrayLength = data.getGradeArray()[1].length;
        while (fDistanctTmp < 35 && iD < gradeArrayLength) {
            float d1 = (data.getGradeArray()[1][0] != null ? data.getGradeArray()[1][0] : 0.0f);
            float d2 = (data.getGradeArray()[1][iD] != null ? data.getGradeArray()[1][iD++] : 0.0f);
            fDistanctTmp = d1 - d2;
        }
        iD--;
        float alt = data.getGradeArray()[0][0]-data.getGradeArray()[0][iD];
        float fGradeTmp = 0.0f;
        if (fDistanctTmp != 0)
            fGradeTmp = alt/fDistanctTmp*100.0f;
        if (fGradeTmp < 50)
            data.setGrade(fGradeTmp);

        data.setDistancePrev(data.getDistanceTot());
    }
}
