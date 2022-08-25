package rnfive.htfu.cyclingcomputer.define;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import rnfive.htfu.cyclingcomputer.define.enums.ConfirmResult;
import rnfive.htfu.cyclingcomputer.define.enums.ConfirmType;
import rnfive.htfu.cyclingcomputer.define.listeners.ConfirmListener;

import rnfive.htfu.cyclingcomputer.R;
import rnfive.htfu.cyclingcomputer.MainActivity;

public final class Permissions implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = Permissions.class.getSimpleName();
    private Permissions() {}

    public static void checkAppPermissions(Activity activity, ConfirmListener confirmListener) {
        MainActivity.bWriteGranted = true; //ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        MainActivity.bGpsGranted = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        MainActivity.bInternetGranted = ContextCompat.checkSelfPermission(activity, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED;

        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MainActivity.bGpsGranted = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
            Log.d(TAG, "Granted[" + MainActivity.bGpsGranted + "]");
            if (!MainActivity.bGpsGranted) {
                String[] permList = {Manifest.permission.ACCESS_BACKGROUND_LOCATION};
                ActivityCompat.requestPermissions(activity, permList, 1);
                Log.d(TAG, "Requested[]");
            }
        }
        */


        if (!MainActivity.bGpsGranted || !MainActivity.bInternetGranted || !MainActivity.bWriteGranted) {
            ArrayList<String> al_s_permissions = new ArrayList<>();
            if (!MainActivity.bGpsGranted) {
                //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                //    al_s_permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
                al_s_permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if (!MainActivity.bInternetGranted)
                al_s_permissions.add(Manifest.permission.INTERNET);

            Dialogs.vals = new String[al_s_permissions.size()];
            Dialogs.vals = al_s_permissions.toArray(Dialogs.vals);
            String desc = "";
            if (!MainActivity.bGpsGranted && ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                desc = "<b>" + activity.getString(R.string.permission_name_location) + "</b>" + "<br/>";
                desc += activity.getString(R.string.permission_location);
            }
            if (!MainActivity.bWriteGranted && ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                desc += (desc.isEmpty() ? "" : "<br/><br/>");
                desc += "<b>" + activity.getString(R.string.permission_name_write) + "</b>" + "<br/>";
                desc += activity.getString(R.string.permission_write);
            }
            if (!MainActivity.bInternetGranted && ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.INTERNET)) {
                desc += (desc.isEmpty() ? "" : "\n");
                desc += "Internet is required to upload activity and gather weather information.";
            }

            if (desc.isEmpty())
                confirmListener.onConfirm(ConfirmType.PERMISSIONS, ConfirmResult.POSITIVE, Dialogs.vals);
            else
                Dialogs.Confirm(activity,confirmListener,ConfirmType.PERMISSIONS,ConfirmType.PERMISSIONS.toString(),desc,"OK","","");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {

        int i = 0;
        for (String permission : permissions) {
            switch (permission) {
                case Manifest.permission.ACCESS_FINE_LOCATION:
                    // If request is cancelled, the result arrays are empty.
                    if (grantResults.length > 0
                            && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        if (StaticVariables.bDebug && !MainActivity.bGpsGranted)
                            MainActivity.toastListener.onToast("Location Permission Granted");
                        MainActivity.bGpsGranted = true;
                    } else {
                        MainActivity.bGpsGranted = false;
                    }
                    break;
                case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                    if ((grantResults.length > 0
                            && grantResults[i] == PackageManager.PERMISSION_GRANTED)) {
                        if (StaticVariables.bDebug && !MainActivity.bWriteGranted)
                            MainActivity.toastListener.onToast("Write Permission Granted");
                        MainActivity.bWriteGranted = true;
                    } else
                        MainActivity.bWriteGranted = false;
                    break;
                case Manifest.permission.INTERNET:
                    if (grantResults.length > 0
                            && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        if (StaticVariables.bDebug && !MainActivity.bInternetGranted)
                            MainActivity.toastListener.onToast("Internet Permission Granted");
                        MainActivity.bInternetGranted = true;
                    } else
                        MainActivity.bInternetGranted = false;
                    break;
                default:
                    break;
            }
            i++;
        }
    }
}
