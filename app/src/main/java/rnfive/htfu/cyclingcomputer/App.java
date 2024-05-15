package rnfive.htfu.cyclingcomputer;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.jetbrains.annotations.Nullable;

import rnfive.htfu.cyclingcomputer.service.Service_Recording;

public class App extends Application {

    private static Application sApplication;

    @Override
    public void onCreate() {

        super.onCreate();
        sApplication = this;

        Thread.setDefaultUncaughtExceptionHandler(
                this::handleUncaughtException);
    }

    private void handleUncaughtException(Thread thread, Throwable e) {
        Context context = getContext();
        Intent serviceIntent = new Intent(context, Service_Recording.class);
        serviceIntent.setAction(Service_Recording.CRASH);
        ContextCompat.startForegroundService(context, serviceIntent);
        AppCompatActivity activity = (AppCompatActivity) context;
        activity.finish();
    }

    private static Application getApplication() {
        return sApplication;
    }

    public static Context getContext() {
        return getApplication().getApplicationContext();
    }
}
