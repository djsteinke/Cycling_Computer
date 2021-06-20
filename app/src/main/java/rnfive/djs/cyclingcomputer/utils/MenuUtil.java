package rnfive.djs.cyclingcomputer.utils;

import android.content.Intent;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import rnfive.djs.cyclingcomputer.LogReader;
import rnfive.djs.cyclingcomputer.MainActivity;
import rnfive.djs.cyclingcomputer.R;
import rnfive.djs.cyclingcomputer.Settings;

public final class MenuUtil {

    private static final int record_item = R.id.record_item;
    private static final int settings_item = R.id.settings_item;
    private static final int log_item = R.id.log_item;
    private static final int bluetooth_item = R.id.bluetooth_item;
    private static final int bluetooth_search = R.id.bluetooth_search;
    private static final int hrv = R.id.hrv;

    private MenuUtil() {
    }

    public static boolean menuItemSelector(AppCompatActivity context, MenuItem item, String sourceActivity) {
        boolean b_finish = false;
        switch (item.getItemId()) {
            case record_item:
                context.startActivity(new Intent(context, MainActivity.class));
                b_finish = true;
                break;
            case settings_item:
                context.startActivity(new Intent(context, Settings.class));
                b_finish = true;
                break;
            case log_item:
                context.startActivity(new Intent(context, LogReader.class));
                b_finish = true;
                break;
                /*
            case bluetooth_item:
                context.startActivity(new Intent(context, Activity_Bluetooth.class));
                b_finish = true;
                break;
            case bluetooth_search:
                context.startActivity(new Intent(context, Activity_BleSearch.class));
                b_finish = true;
                break;
            case hrv:
                context.startActivity(new Intent(context, Activity_HRV.class));
                b_finish = true;
                break;

                 */
            default:
                break;
        }

        switch (sourceActivity) {
            case "MainActivity":
                b_finish = false;
                break;
        }

        if (b_finish)
            context.finish();

        return true;
    }
}
