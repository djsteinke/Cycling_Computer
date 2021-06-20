package rnfive.djs.cyclingcomputer;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
/*
import rnfive.djs.ant.antplus.pcc.MultiDeviceSearch;
import rnfive.djs.ant.antplus.pcc.defines.DeviceType;
import rnfive.djs.ant.antplus.pcc.defines.RequestAccessResult;
import rnfive.djs.ant.antplus.pccbase.MultiDeviceSearch.MultiDeviceSearchResult;
*
 */
import com.dsi.ant.plugins.antplus.pcc.MultiDeviceSearch;
import com.dsi.ant.plugins.antplus.pcc.defines.DeviceType;
import com.dsi.ant.plugins.antplus.pcc.defines.RequestAccessResult;
import com.dsi.ant.plugins.antplus.pccbase.MultiDeviceSearch.MultiDeviceSearchResult;

import java.util.ArrayList;
import java.util.EnumSet;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import rnfive.djs.cyclingcomputer.R;
import rnfive.djs.cyclingcomputer.antplus.AntPlus_Util;
import rnfive.djs.cyclingcomputer.define.Dialogs;
import rnfive.djs.cyclingcomputer.define.enums.ConfirmResult;
import rnfive.djs.cyclingcomputer.define.enums.ConfirmType;
import rnfive.djs.cyclingcomputer.define.listeners.ConfirmListener;

public class Activity_AntSensorSearch extends AppCompatActivity implements ConfirmListener {

    private final static String TAG = Activity_AntSensorSearch.class.getSimpleName();
    SharedPreferences mainPrefs;
    SharedPreferences.Editor editor;
    /**
     * Relates a MultiDeviceSearchResult with an RSSI value
     */
    static class AntSearchResult
    {
        DeviceType mDeviceType;
        int mDeviceId;
        boolean bSaved = false;
        boolean bConnected = false;

        AntSearchResult() {}
    }

    private Context mContext;
    private final ArrayList<AntSearchResult> mDeviceArray = new ArrayList<>();
    private ArrayAdapter_AntSearchResult mDeviceAdapter;
    private Activity mActivity;
    private static AntSearchResult oldDevice;
    private static AntSearchResult newDevice;
    private static ConfirmListener antConfirmListener;
    private ProgressBar searchProgress;
    private ImageButton searchButton;

    protected  void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ant_sensor_search);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        mainPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        mContext = this;
        mActivity = this;
        antConfirmListener = this;

        ListView mDeviceList = findViewById(R.id.ant_device_list);

        mDeviceAdapter = new ArrayAdapter_AntSearchResult(this, mDeviceArray);
        mDeviceList.setAdapter(mDeviceAdapter);

        setSavedDeviceList();

        mDeviceList.setOnItemClickListener((parent, view, position, id) -> {
            AntSearchResult device = mDeviceAdapter.getItem(position);
            if (device != null) {
                if (device.bSaved) {
                    MainActivity.toastListener.onToast(getString(R.string.forget_sensor));
                } else {
                    saveDevice(device);
                }
            }
        });

        mDeviceList.setOnItemLongClickListener((parent, view, position, id) -> {
            AntSearchResult device = mDeviceAdapter.getItem(position);
            if (device != null) {
                oldDevice = device;
                newDevice = null;
                Dialogs.Confirm(mActivity, antConfirmListener, ConfirmType.SENSOR, "Forget Device Id:" + device.mDeviceId + "?", null, getString(R.string.forget),null, getString(R.string.cancel));
            }
            mDeviceAdapter.notifyDataSetChanged();
            return false;
        });

        final EnumSet<DeviceType> devices = EnumSet.noneOf(DeviceType.class);
        devices.add(DeviceType.BIKE_POWER);
        devices.add(DeviceType.HEARTRATE);
        devices.add(DeviceType.BIKE_SPDCAD);
        devices.add(DeviceType.BIKE_CADENCE);
        devices.add(DeviceType.BIKE_SPD);

        searchProgress = findViewById(R.id.ant_search_progress);
        searchButton = findViewById(R.id.ant_search_button);
        searchButton.setOnClickListener(v -> {
            // start the multi-device search
            new MultiDeviceSearch(mContext, devices, mCallback, mRssiCallback);
            searchButton.setVisibility(View.GONE);
            searchProgress.setVisibility(View.VISIBLE);
        });

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
        Log.d(TAG, "onCreate()");
    }

    private void saveDevice(AntSearchResult device) {

        String key = getKeyFromType(device.mDeviceType);
        if (!key.equals("")) {
            boolean bUpdate = true;
            for (AntSearchResult res : mDeviceArray) {
                if (res.mDeviceType == device.mDeviceType && res.mDeviceId != device.mDeviceId) {
                    oldDevice = res;
                    newDevice = device;
                    bUpdate = false;
                    Dialogs.Confirm(mActivity, antConfirmListener, ConfirmType.SENSOR, "Replace Device Id: " + res.mDeviceId + "?", "New Device Id: " + device.mDeviceId, getString(R.string.replace), null, getString(R.string.cancel));
                }
            }
            if (bUpdate) {
                updateKey(key, device.mDeviceId);
                device.bSaved = true;
                mDeviceAdapter.notifyDataSetChanged();
                String name = device.mDeviceType.toString();
                MainActivity.toastListener.onToast(name.substring(0,name.length()-1) + " " + getString(R.string.saved));
            }
        }
    }

    private String getKeyFromType(DeviceType deviceType) {

        switch (deviceType) {
            case BIKE_SPDCAD:
                return "ANT_PLUS_ID_BSC";
            case BIKE_SPD:
                return "ANT_PLUS_ID_BS";
            case BIKE_CADENCE:
                return "ANT_PLUS_ID_BC";
            case BIKE_POWER:
                return "ANT_PLUS_ID_BP";
            case HEARTRATE:
                return "ANT_PLUS_ID_HR";
            default :
                return "";
        }
    }

    private void updateKey(String key, int val) {
        editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
        editor.putInt(key, val);
        editor.apply();
    }

    private void setSavedDeviceList() {
        int i_deviceId;
        AntSearchResult result;

        i_deviceId = mainPrefs.getInt("ANT_PLUS_ID_BSC",0);
        if (i_deviceId != 0) {
            result = new AntSearchResult();
            result.mDeviceType = DeviceType.BIKE_SPDCAD;
            result.mDeviceId = i_deviceId;
            result.bSaved = true;
            mDeviceAdapter.add(result);
        }

        i_deviceId = mainPrefs.getInt("ANT_PLUS_ID_BS",0);
        if (i_deviceId != 0) {
            result = new AntSearchResult();
            result.mDeviceType = DeviceType.BIKE_SPD;
            result.mDeviceId = i_deviceId;
            result.bSaved = true;
            mDeviceAdapter.add(result);
        }

        i_deviceId = mainPrefs.getInt("ANT_PLUS_ID_BC",0);
        if (i_deviceId != 0) {
            result = new AntSearchResult();
            result.mDeviceType = DeviceType.BIKE_CADENCE;
            result.mDeviceId = i_deviceId;
            result.bSaved = true;
            mDeviceAdapter.add(result);
        }

        i_deviceId = mainPrefs.getInt("ANT_PLUS_ID_HR",1);
        if (i_deviceId != 0) {
            result = new AntSearchResult();
            result.mDeviceType = DeviceType.HEARTRATE;
            result.mDeviceId = i_deviceId;
            result.bSaved = true;
            mDeviceAdapter.add(result);
        }

        i_deviceId = mainPrefs.getInt("ANT_PLUS_ID_BP",1);
        if (i_deviceId != 0) {
            result = new AntSearchResult();
            result.mDeviceType = DeviceType.BIKE_POWER;
            result.mDeviceId = i_deviceId;
            result.bSaved = true;
            mDeviceAdapter.add(result);
        }

        mDeviceAdapter.notifyDataSetChanged();
    }

    private void setDeviceSaved(int id) {
        for (AntSearchResult device : mDeviceArray) {
            if (device.mDeviceId == id) {
                device.bSaved = !device.bSaved;
                mDeviceAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

    private boolean listContainsDevice(AntSearchResult device) {
        for (AntSearchResult res : mDeviceArray) {
            if (res.mDeviceType == device.mDeviceType && res.mDeviceId == device.mDeviceId) {
                res.bConnected = true;
                return true;
            }
        }
        return false;
    }

    private final MultiDeviceSearch.SearchCallbacks mCallback = new MultiDeviceSearch.SearchCallbacks()
    {
        /**
         * Called when a device is found. Display found devices in connected and
         * found lists
         */
        public void onDeviceFound(final MultiDeviceSearchResult deviceFound)
        {

            final AntSearchResult result = new AntSearchResult();
            result.mDeviceType = deviceFound.getAntDeviceType();
            result.mDeviceId = deviceFound.getAntDeviceNumber();
            result.bSaved = false;
            result.bConnected = true;

            runOnUiThread(() -> {
                if (!listContainsDevice(result)) {
                    mDeviceAdapter.add(result);
                }
                mDeviceAdapter.notifyDataSetChanged();
            });
        }

        /**
         * The search has been stopped unexpectedly
         */
        public void onSearchStopped(RequestAccessResult reason)
        {
            MainActivity.toastListener.onToast(AntPlus_Util.getResultCode(reason,"Device Search"));

            runOnUiThread(() -> {
                if (searchProgress != null)
                    searchProgress.setVisibility(View.GONE);
                if (searchButton != null)
                    searchButton.setVisibility(View.VISIBLE);
            });
        }

        @Override
        public void onSearchStarted(MultiDeviceSearch.RssiSupport supportsRssi) {
            //TODO
            /*
            if(supportsRssi == MultiDeviceSearch.RssiSupport.UNAVAILABLE)
            {
                 Toast.makeText(mContext, "Rssi information not available.", Toast.LENGTH_SHORT).show();
            } else if(supportsRssi == MultiDeviceSearch.RssiSupport.UNKNOWN_OLDSERVICE)
            {
                Toast.makeText(mContext, "Rssi might be supported. Please upgrade the plugin service.", Toast.LENGTH_SHORT).show();
            }
            */
        }
    };

    /**
     * Callback for RSSI data of previously found devices
     */
    private final MultiDeviceSearch.RssiCallback mRssiCallback = new MultiDeviceSearch.RssiCallback()
    {
        /**
         * Receive an RSSI data update from a specific found device
         */
        @Override
        public void onRssiUpdate(final int resultId, final int rssi)
        {

        }
    };

    @Override
    public void onConfirm(ConfirmType confirmType, ConfirmResult result, @Nullable String[] strings) {
        if (confirmType == ConfirmType.SENSOR && result == ConfirmResult.POSITIVE) {
            int id = (newDevice != null?newDevice.mDeviceId:0);
            if (oldDevice != null) {
                updateKey(getKeyFromType(oldDevice.mDeviceType),id);
                if (oldDevice.bConnected)
                    oldDevice.bSaved = false;
                else
                    mDeviceAdapter.remove(oldDevice);
            }
            if (newDevice != null) {
                newDevice.bSaved = true;
            }
            mDeviceAdapter.notifyDataSetChanged();
        }
        oldDevice = null;
        newDevice = null;
    }
}

