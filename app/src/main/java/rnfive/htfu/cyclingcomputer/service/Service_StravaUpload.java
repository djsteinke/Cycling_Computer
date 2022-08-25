package rnfive.htfu.cyclingcomputer.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;

import com.garmin.fit.Sport;

import androidx.annotation.Nullable;
import rnfive.htfu.cyclingcomputer.MainActivity;
import rnfive.htfu.cyclingcomputer.define.listeners.StravaUploadResponseListener;
import rnfive.htfu.cyclingcomputer.strava.runnable.Runnable_StravaUpload;

import static rnfive.htfu.cyclingcomputer.MainActivity.sDescription;
import static rnfive.htfu.cyclingcomputer.MainActivity.sName;
import static rnfive.htfu.cyclingcomputer.MainActivity.sport;
import static rnfive.htfu.cyclingcomputer.service.Service_Recording.fitFile;

public class Service_StravaUpload extends IntentService implements StravaUploadResponseListener {

    public static final String TAG = Service_StravaUpload.class.getSimpleName();

    public static final String START_UPLOAD = "rnfive.htfu.cyclingcomputer.StravaUpload.START_UPLOAD";

    public Service_StravaUpload() {
        super("Service_StravaUpload");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String action = null;
        if (intent != null)
            action = intent.getAction();
        if (action == null)
            action = "NONE";

        switch (action) {
            case START_UPLOAD:
                upload();
                break;
            default:
                break;
        }
    }

    private void upload() {
        Runnable_StravaUpload runnable = new Runnable_StravaUpload(fitFile.getFitFileName(), sName, sDescription);
        runnable.withListener(this);
        if (sport == Sport.RUNNING)
            runnable.setRunActivity(true);
        runnable.run();
    }

    @Override
    public void onStravaResponse(String val) {
        // TODO Post notification of successful upload
        stopSelf();
    }
}
