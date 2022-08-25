package rnfive.htfu.cyclingcomputer.antplus;

import android.content.Context;
import android.util.Log;

import lombok.Getter;
import lombok.Setter;
import rnfive.htfu.cyclingcomputer.define.Arrays;
import rnfive.htfu.cyclingcomputer.MainActivity;
/*
import rnfive.htfu.ant.antplus.pcc.AntPlusBikeCadencePcc;
import rnfive.htfu.ant.antplus.pcc.defines.BatteryStatus;
import rnfive.htfu.ant.antplus.pcc.defines.DeviceState;
import rnfive.htfu.ant.antplus.pcc.defines.RequestAccessResult;
import rnfive.htfu.ant.antplus.pccbase.AntPluginPcc;
import rnfive.htfu.ant.antplus.pccbase.PccReleaseHandle;
import static rnfive.htfu.ant.antplus.pcc.defines.DeviceState.DEAD;
*/
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikeCadencePcc;
import com.dsi.ant.plugins.antplus.pcc.defines.BatteryStatus;
import com.dsi.ant.plugins.antplus.pcc.defines.DeviceState;
import com.dsi.ant.plugins.antplus.pcc.defines.RequestAccessResult;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc;
import com.dsi.ant.plugins.antplus.pccbase.PccReleaseHandle;
import static com.dsi.ant.plugins.antplus.pcc.defines.DeviceState.DEAD;

import static rnfive.htfu.cyclingcomputer.define.StaticVariables.bBCExists;
import static rnfive.htfu.cyclingcomputer.define.StaticVariables.bDebug;
import static rnfive.htfu.cyclingcomputer.define.StaticVariables.bcAntBattery;
import static rnfive.htfu.cyclingcomputer.service.Service_Recording.data;

@Getter
@Setter
public class AntPlus_BC {

    private static final String TAG = "AntPlus_BC";
    private static final String SENSOR = "Candence Device";
    private AntPlusBikeCadencePcc pcc = null;
    private PccReleaseHandle<AntPlusBikeCadencePcc> mReleaseHandle = null;
    private boolean connected;
    private boolean searching;
    private final boolean combined;
    private final int id;
    private final Context context;
    private long connectTime;
    private AntPlus_BS bsAnt = null;
    private BatteryStatus deviceBatteryStatus = null;
    private double dLastEventTimestamp = 0;

    public AntPlus_BC(int id, Context context, boolean combined) {
        this.id = id;
        this.connected = false;
        this.context = context;
        this.searching = false;
        this.combined = combined;
        this.connectTime = System.currentTimeMillis();
        Log.d(TAG,"created");
    }

    public void connect() {
        if (id != 0 && pcc == null) {
            Log.d(TAG,(id == 1?"hardare check":"connect"));
            this.searching = true;
            this.mReleaseHandle = AntPlusBikeCadencePcc.requestAccess(context,id,0,combined,mReceiver,mDSCReceiver);
        }
        this.connectTime = System.currentTimeMillis();
    }

    public void disconnect() {
        Log.d(TAG,"disconnect");
        if (mReleaseHandle != null)
            this.mReleaseHandle.close();
        this.pcc = null;
        if (bsAnt != null)
            bsAnt.disconnect();
    }

    private void subscribeToEvents() {
        if (pcc != null) {
            pcc.subscribeCalculatedCadenceEvent((estTimestamp, eventFlags, calculatedCadence) -> {
                data.setCadBC(calculatedCadence.intValue());
                Arrays.updateArray(data.getCadBCArray(), calculatedCadence.intValue());
            });
            if (pcc.isSpeedAndCadenceCombinedSensor()) {
                bsAnt = new AntPlus_BS(this.id, this.context, true);
                bsAnt.connect();
            }

            pcc.subscribeBatteryStatusEvent((l, enumSet, bigDecimal, batteryStatus) -> {
                deviceBatteryStatus = batteryStatus;
                bcAntBattery = batteryStatus.getIntValue();
                MainActivity.toastListener.onToast("CADENCE BAT: " + batteryStatus.toString());
            });

            pcc.subscribeRawCadenceDataEvent((estTimestamp, eventFlags, timestampOfLastEvent, cumulativeRevolutions) -> {
                double dTimestampofLastEvent = timestampOfLastEvent.doubleValue();
                if (dLastEventTimestamp != 0) {
                    Arrays.updateArray(data.getCadTArray(), (dTimestampofLastEvent- dLastEventTimestamp));
                }
                dLastEventTimestamp = dTimestampofLastEvent;
            });
        }
    }

    private AntPluginPcc.IPluginAccessResultReceiver<AntPlusBikeCadencePcc> mReceiver =
            new AntPluginPcc.IPluginAccessResultReceiver<AntPlusBikeCadencePcc>() {
                //Handle the result, connecting to events on success or reporting failure to user.
                @Override
                public void onResultReceived(AntPlusBikeCadencePcc result, RequestAccessResult resultCode,
                                             DeviceState initialDeviceState) {

                    searching = false;
                    boolean show = false;
                    bBCExists = false;
                    switch (resultCode) {
                        case SUCCESS:
                            pcc = result;
                            connected = true;
                            subscribeToEvents();
                            show = true;
                            bBCExists = true;
                            break;
                        case ADAPTER_NOT_DETECTED:
                            if (!MainActivity.bAntSupportMsgDisplayed)
                                show = true;
                            MainActivity.bAntSupportMsgDisplayed = true;
                            connected = false;
                            break;
                        case BAD_PARAMS:
                            break;
                        default :
                            if (id == 1)
                                connected = true;
                            break;
                    }

                    if (bDebug || show)
                        MainActivity.toastListener.onToast(AntPlus_Util.getResultCode(resultCode,SENSOR));
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
                bBCExists = false;
                data.setCadBC(-1);
                MainActivity.toastListener.onToast(SENSOR + " Disconnected");
            }
        }
    };
}
