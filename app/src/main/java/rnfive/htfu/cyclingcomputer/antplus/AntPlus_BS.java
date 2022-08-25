package rnfive.htfu.cyclingcomputer.antplus;

import android.content.Context;
import android.util.Log;

import java.math.BigDecimal;
import java.util.EnumSet;

import lombok.Getter;
import lombok.Setter;
import rnfive.htfu.cyclingcomputer.define.Arrays;
import rnfive.htfu.cyclingcomputer.MainActivity;
/*
import rnfive.htfu.ant.antplus.pcc.AntPlusBikeSpeedDistancePcc;
import rnfive.htfu.ant.antplus.pcc.defines.BatteryStatus;
import rnfive.htfu.ant.antplus.pcc.defines.DeviceState;
import rnfive.htfu.ant.antplus.pcc.defines.EventFlag;
import rnfive.htfu.ant.antplus.pcc.defines.RequestAccessResult;
import rnfive.htfu.ant.antplus.pccbase.AntPluginPcc;
import rnfive.htfu.ant.antplus.pccbase.PccReleaseHandle;
import static rnfive.htfu.ant.antplus.pcc.defines.DeviceState.DEAD;

 */
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikeSpeedDistancePcc;
import com.dsi.ant.plugins.antplus.pcc.defines.BatteryStatus;
import com.dsi.ant.plugins.antplus.pcc.defines.DeviceState;
import com.dsi.ant.plugins.antplus.pcc.defines.EventFlag;
import com.dsi.ant.plugins.antplus.pcc.defines.RequestAccessResult;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc;
import com.dsi.ant.plugins.antplus.pccbase.PccReleaseHandle;
import static com.dsi.ant.plugins.antplus.pcc.defines.DeviceState.DEAD;

import static rnfive.htfu.cyclingcomputer.antplus.AntPlus_Util.getResultCode;
import static rnfive.htfu.cyclingcomputer.define.StaticVariables.bDebug;
import static rnfive.htfu.cyclingcomputer.define.StaticVariables.bsAntBattery;
import static rnfive.htfu.cyclingcomputer.define.StaticVariables.iWheelSize;
import static rnfive.htfu.cyclingcomputer.define.StaticVariables.speedMin;
import static rnfive.htfu.cyclingcomputer.service.Service_Recording.data;
import static rnfive.htfu.cyclingcomputer.define.StaticVariables.bBSExists;

@Getter
@Setter
public class AntPlus_BS {
    private static final String TAG = "AntPlus_BC";
    private static final String SENSOR = "Speed Device";
    private AntPlusBikeSpeedDistancePcc pcc = null;
    private PccReleaseHandle<AntPlusBikeSpeedDistancePcc> mReleaseHandle = null;
    private boolean connected;
    private boolean searching;
    private boolean combined;
    private int id;
    private Context context;
    private long connectTime;
    private float distance;
    private float lastDistance;
    private BatteryStatus deviceBatteryStatus = null;
    private double dLastEventTimestamp = 0;

    public AntPlus_BS(int id, Context context, boolean combined) {
        this.id = id;
        this.connected = false;
        this.context = context;
        this.searching = false;
        this.combined = combined;
        this.connectTime = System.currentTimeMillis();
        this.distance = 0f;
        this.lastDistance = 0f;
        Log.d(TAG,"created");
    }

    public void connect() {
        if (id != 0 && pcc == null) {
            Log.d(TAG,(id == 1?"hardare check":"connect"));
            this.searching = true;
            this.mReleaseHandle = AntPlusBikeSpeedDistancePcc.requestAccess(context,id,0,combined,mReceiver,mDSCReceiver);
        }
        this.connectTime = System.currentTimeMillis();
    }

    public void disconnect() {
        Log.d(TAG,"disconnect");
        if (mReleaseHandle != null)
            this.mReleaseHandle.close();
        this.pcc = null;
    }

    private void subscribeToEvents() {
        if (pcc != null) {
            pcc.subscribeCalculatedSpeedEvent(new AntPlusBikeSpeedDistancePcc.CalculatedSpeedReceiver(
                                                      BigDecimal.valueOf((float) iWheelSize / 1000f)) {
                @Override
                public void onNewCalculatedSpeed(long estTimestamp,
                                                 EnumSet<EventFlag> eventFlags,
                                                 final BigDecimal calculatedSpeed) {
                    float speed = calculatedSpeed.floatValue();
                    speed = (speed < speedMin ? 0f : speed);
                    float senSpeed = (speed + data.getSpeedSenPrev())/2f;
                    data.setSpeedSen(senSpeed);
                    data.setSpeedSenPrev(speed);
                }
            }
            );

            pcc.subscribeCalculatedAccumulatedDistanceEvent(new AntPlusBikeSpeedDistancePcc.CalculatedAccumulatedDistanceReceiver(
                    BigDecimal.valueOf((float) iWheelSize / 1000f)) {
                @Override
                public void onNewCalculatedAccumulatedDistance(long estTimestamp, EnumSet<EventFlag> eventFlags, BigDecimal calculatedDistance) {
                    distance = calculatedDistance.floatValue();
                    data.updateSenDistance(distance - lastDistance);
                    lastDistance = distance;
                }
            });

            pcc.subscribeRawSpeedAndDistanceDataEvent((estTimestamp, eventFlags, timestampOfLastEvent, cumulativeRevolutions) -> {
                double dTimestampofLastEvent = timestampOfLastEvent.doubleValue();
                if (dLastEventTimestamp != 0) {
                    Arrays.updateArray(data.getRotTArray(), (dTimestampofLastEvent- dLastEventTimestamp));
                }
                dLastEventTimestamp = dTimestampofLastEvent;
            });

            pcc.subscribeBatteryStatusEvent((l, enumSet, bigDecimal, batteryStatus) -> {
                    deviceBatteryStatus = batteryStatus;
                    bsAntBattery = batteryStatus.getIntValue();
                }
            );
        }
    }

    private AntPluginPcc.IPluginAccessResultReceiver<AntPlusBikeSpeedDistancePcc> mReceiver =
            new AntPluginPcc.IPluginAccessResultReceiver<AntPlusBikeSpeedDistancePcc>() {
                //Handle the result, connecting to events on success or reporting failure to user.
                @Override
                public void onResultReceived(AntPlusBikeSpeedDistancePcc result, RequestAccessResult resultCode,
                                             DeviceState initialDeviceState) {

                    searching = false;
                    boolean show = false;
                    bBSExists = false;
                    switch (resultCode) {
                        case SUCCESS:
                            pcc = result;
                            connected = true;
                            subscribeToEvents();
                            show = true;
                            bBSExists = true;
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
                        MainActivity.toastListener.onToast(getResultCode(resultCode,SENSOR));
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
                bBSExists = false;
                data.setSpeedSen(-1);
                MainActivity.toastListener.onToast(SENSOR + " Disconnected");
            }
        }
    };
}
