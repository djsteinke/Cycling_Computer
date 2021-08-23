package rnfive.djs.cyclingcomputer;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import rnfive.djs.cyclingcomputer.service.Service_Recording;

public class App extends Application {
    private Context context;
    @Override
    public void onCreate() {

        super.onCreate();

        Thread.setDefaultUncaughtExceptionHandler(
                this::handleUncaughtException);
    }

    void handleUncaughtException (Thread thread, Throwable e) {
        Intent serviceIntent = new Intent(context, Service_Recording.class);
        serviceIntent.setAction(Service_Recording.CRASH);
        ContextCompat.startForegroundService(context, serviceIntent);
        AppCompatActivity activity = (AppCompatActivity) context;
        activity.finish();
    }

    void setContext(Context context) {
        this.context = context;
    }
}
