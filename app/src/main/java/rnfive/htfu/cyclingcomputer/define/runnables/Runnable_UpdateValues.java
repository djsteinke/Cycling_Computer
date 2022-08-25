package rnfive.htfu.cyclingcomputer.define.runnables;

import android.util.Log;

import java.util.concurrent.Executors;

import static rnfive.htfu.cyclingcomputer.define.PhoneSensors.absPressure;
import static rnfive.htfu.cyclingcomputer.define.StaticVariables.bStarted;
import static rnfive.htfu.cyclingcomputer.define.StaticVariables.darkSkyResponse;
import static rnfive.htfu.cyclingcomputer.define.StaticVariables.lastUpdateValuesMS;
import static rnfive.htfu.cyclingcomputer.define.runnables.Runnable_GetWeatherInfo.intervalMS;
import static rnfive.htfu.cyclingcomputer.define.runnables.Runnable_GetWeatherInfo.lastRequestMS;
import static rnfive.htfu.cyclingcomputer.service.Service_Recording.data;
import static rnfive.htfu.cyclingcomputer.service.Service_Recording.phoneSensors;

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
            data.updateAltitude();
        }
    }
}
