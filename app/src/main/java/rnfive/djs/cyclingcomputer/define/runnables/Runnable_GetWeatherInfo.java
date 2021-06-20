package rnfive.djs.cyclingcomputer.define.runnables;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import rnfive.djs.cyclingcomputer.define.DarkSkyResponse;

import static rnfive.djs.cyclingcomputer.define.StaticVariables.DARK_SKY_KEY;
import static rnfive.djs.cyclingcomputer.define.StaticVariables.darkSkyResponse;
import static rnfive.djs.cyclingcomputer.define.StaticVariables.getClassFromJson;
import static rnfive.djs.cyclingcomputer.service.Service_Recording.data;

public class Runnable_GetWeatherInfo implements Runnable {

    private static final String TAG = Runnable_GetWeatherInfo.class.getSimpleName();
    public static boolean running = false;
    public static long lastRequestMS = 0;
    public static final long intervalMS = 600000;

    public Runnable_GetWeatherInfo() {}

    @Override
    public void run() {
        Log.d(TAG, "run()");
        if (!running)
            command();
    }

    private static void command() {
        running = true;
        try {
            URL url = new URL("https://api.darksky.net/forecast/" + DARK_SKY_KEY + "/" +
                    data.getLatitude() + "," + data.getLongitude() +
                    "?exclude=minutely,hourly,daily,alerts,flags&units=si");

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            String response = readStream(in);
            Log.d(TAG, response);
            if (!response.isEmpty())
                darkSkyResponse = getClassFromJson(response, DarkSkyResponse.class);
            urlConnection.disconnect();
        } catch (MalformedURLException e) {
            Log.e(TAG, "command() MalformedURLException:" + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "command() HttpURLConnection:" + e.getMessage());
            lastRequestMS = 0;
        }
        running = false;
    }

    private static String readStream(InputStream is) {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            int i = is.read();
            while(i != -1) {
                bo.write(i);
                i = is.read();
            }
            return bo.toString();
        } catch (IOException e) {
            return "";
        }
    }
}
