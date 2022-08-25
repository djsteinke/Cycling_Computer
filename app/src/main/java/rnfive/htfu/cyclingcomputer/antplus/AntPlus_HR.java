package rnfive.htfu.cyclingcomputer.antplus;

import android.content.Context;
import android.util.Log;

/*
import rnfive.htfu.ant.antplus.pcc.AntPlusHeartRatePcc;
import rnfive.htfu.ant.antplus.pcc.defines.DeviceState;
import rnfive.htfu.ant.antplus.pcc.defines.RequestAccessResult;
import rnfive.htfu.ant.antplus.pccbase.AntPluginPcc;
import rnfive.htfu.ant.antplus.pccbase.PccReleaseHandle;
import static rnfive.htfu.ant.antplus.pcc.defines.DeviceState.DEAD;

 */
import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc;
import com.dsi.ant.plugins.antplus.pcc.defines.DeviceState;
import com.dsi.ant.plugins.antplus.pcc.defines.RequestAccessResult;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc;
import com.dsi.ant.plugins.antplus.pccbase.PccReleaseHandle;
import static com.dsi.ant.plugins.antplus.pcc.defines.DeviceState.DEAD;

import lombok.Getter;
import lombok.Setter;
import rnfive.htfu.cyclingcomputer.define.EquipmentSensor;
import rnfive.htfu.cyclingcomputer.define.enums.SensorState;
import rnfive.htfu.cyclingcomputer.define.listeners.IDeviceStateChangeReceiver;
import rnfive.htfu.cyclingcomputer.MainActivity;

import static rnfive.htfu.cyclingcomputer.define.StaticVariables.bDebug;
import static rnfive.htfu.cyclingcomputer.service.Service_Recording.data;

@Getter
@Setter
public class AntPlus_HR {

    private static final String TAG = "AntPlus_HR";
    private AntPlusHeartRatePcc pcc = null;
    private PccReleaseHandle<AntPlusHeartRatePcc> hrReleaseHandle = null;
    private boolean connected;
    private boolean searching;
    private boolean supported;
    private int id;
    private Context context;
    private long lConnectTime;
    private int rrInterval = 0;
    private IDeviceStateChangeReceiver deviceStateChangeReceiver;

    public AntPlus_HR(int id, Context context, IDeviceStateChangeReceiver deviceStateChangeReceiver) {
        this.id = id;
        this.connected = false;
        this.context = context;
        this.searching = false;
        this.supported = false;
        this.lConnectTime = System.currentTimeMillis();
        this.deviceStateChangeReceiver = deviceStateChangeReceiver;
        Log.d(TAG,"created");
    }

    public void connect() {
        if (id != 0 && pcc == null) {
            Log.d(TAG,(id == 1?"hardare check":"connect"));
            this.searching = true;
            this.hrReleaseHandle = AntPlusHeartRatePcc.requestAccess(context, id, 0, mReceiver, mDSCReceiver);
        }
        this.lConnectTime = System.currentTimeMillis();
    }

    public void disconnect() {
        Log.d(TAG,"disconnect");
        if (hrReleaseHandle != null)
            this.hrReleaseHandle.close();
        this.pcc = null;
    }

    public long getConnectTime() {return this.lConnectTime;}

    private void subscribeToEvents() {
        if (pcc != null) {
            pcc.subscribeHeartRateDataEvent((l, enumSet, computedHeartRate, heartBeatCount, hearBeatEventTime, dataState) -> data.setHr(computedHeartRate));

            pcc.subscribeCalculatedRrIntervalEvent((l, enumSet, bigDecimal, rrFlag) -> rrInterval = bigDecimal.intValue());
        }
    }

    private AntPluginPcc.IPluginAccessResultReceiver<AntPlusHeartRatePcc> mReceiver =
            new AntPluginPcc.IPluginAccessResultReceiver<AntPlusHeartRatePcc>() {
                //Handle the result, connecting to events on success or reporting failure to user.
                @Override
                public void onResultReceived(AntPlusHeartRatePcc result, RequestAccessResult resultCode,
                                             DeviceState initialDeviceState) {

                    searching = false;
                    boolean show = false;
                    //bHRExists = false;
                    switch (resultCode) {
                        case SUCCESS:
                            pcc = result;
                            connected = true;
                            subscribeToEvents();
                            show = true;
                            supported = true;
                            deviceStateChangeReceiver.onDeviceStateChange(EquipmentSensor.HEARTRATE, String.valueOf(id), SensorState.CONNECTED);
                            //bHRExists = true;
                            break;
                        case ADAPTER_NOT_DETECTED:
                            if (!MainActivity.bAntSupportMsgDisplayed)
                                show = true;
                            MainActivity.bAntSupportMsgDisplayed = true;
                            supported = false;
                            connected = false;
                            break;
                        case BAD_PARAMS:
                            break;
                        default :
                            if (id == 1)
                                connected = true;
                            supported = true;
                            break;
                    }

                    if (bDebug || show)
                        MainActivity.toastListener.onToast(AntPlus_Util.getResultCode(resultCode,"Heart Rate Monitor"));
                }
            };

    private AntPluginPcc.IDeviceStateChangeReceiver mDSCReceiver = new AntPluginPcc.IDeviceStateChangeReceiver()
    {
        @Override
        public void onDeviceStateChange(final DeviceState newDeviceState)
        {
            if (newDeviceState == DEAD) {
                pcc = null;
                connected = false;
                deviceStateChangeReceiver.onDeviceStateChange(EquipmentSensor.HEARTRATE, String.valueOf(id), SensorState.DISCONNECTED);
                //bHRExists = false;
                //iHr = -1;
                MainActivity.toastListener.onToast("Heart Rate Monitor Disconnected");
            }
        }
    };
}
