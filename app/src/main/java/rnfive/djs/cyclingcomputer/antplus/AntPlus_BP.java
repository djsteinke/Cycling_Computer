package rnfive.djs.cyclingcomputer.antplus;

import android.content.Context;
import android.util.Log;

import rnfive.djs.cyclingcomputer.define.EquipmentSensor;
import rnfive.djs.cyclingcomputer.define.enums.SensorState;
import rnfive.djs.cyclingcomputer.define.listeners.IDeviceStateChangeReceiver;
import rnfive.djs.cyclingcomputer.MainActivity;
/*
import rnfive.djs.ant.antplus.pcc.AntPlusBikePowerPcc;
import rnfive.djs.ant.antplus.pcc.defines.BatteryStatus;
import rnfive.djs.ant.antplus.pcc.defines.DeviceState;
import rnfive.djs.ant.antplus.pcc.defines.RequestAccessResult;
import rnfive.djs.ant.antplus.pccbase.AntPluginPcc;
import rnfive.djs.ant.antplus.pccbase.AntPlusCommonPcc;
import rnfive.djs.ant.antplus.pccbase.PccReleaseHandle;
import static rnfive.djs.ant.antplus.pcc.defines.DeviceState.DEAD;

 */

import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc;
import com.dsi.ant.plugins.antplus.pcc.defines.BatteryStatus;
import com.dsi.ant.plugins.antplus.pcc.defines.DeviceState;
import com.dsi.ant.plugins.antplus.pcc.defines.RequestAccessResult;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc;
import com.dsi.ant.plugins.antplus.pccbase.AntPlusCommonPcc;
import com.dsi.ant.plugins.antplus.pccbase.PccReleaseHandle;
import static com.dsi.ant.plugins.antplus.pcc.defines.DeviceState.DEAD;

import static rnfive.djs.cyclingcomputer.define.StaticVariables.bBPCadExists;
import static rnfive.djs.cyclingcomputer.define.StaticVariables.bDebug;
import static rnfive.djs.cyclingcomputer.define.StaticVariables.bpAntBattery;
import static rnfive.djs.cyclingcomputer.service.Service_Recording.data;

public class AntPlus_BP {

    private static final String TAG = "AntPlus_BP";
    private AntPlusBikePowerPcc bpPcc = null;
    private PccReleaseHandle<AntPlusBikePowerPcc> bpReleaseHandle = null;
    private boolean connected;
    private final int id;
    private long lConnectTime;
    private boolean searching;
    private final Context context;
    private BatteryStatus deviceBatteryStatus = null;
    private IDeviceStateChangeReceiver iDeviceStateChangeReceiver;
    private static final int SENSOR_ID = EquipmentSensor.getSensorId(EquipmentSensor.ANT,EquipmentSensor.POWER);

    public AntPlus_BP(int id, Context context) {
        this.connected = false;
        this.id = id;
        this.context = context;
        this.lConnectTime = System.currentTimeMillis();
        this.searching = false;
        Log.d(TAG,"created");
    }
    public AntPlus_BP withDeviceStateChangeReceiver(IDeviceStateChangeReceiver deviceStateChangeReceiver) {
        this.iDeviceStateChangeReceiver = deviceStateChangeReceiver;
        return this;
    }

    public void connect() {
        Log.d(TAG,"connect");
        if (id != 0 && bpPcc == null) {
            this.searching = true;
            this.bpReleaseHandle = AntPlusBikePowerPcc.requestAccess(context, id, 0, mReceiverBP, mDSCReceiverBP);
        }
        this.lConnectTime = System.currentTimeMillis();
    }

    public void disconnect() {
        Log.d(TAG,"disconnect");
        if (bpReleaseHandle != null)
            this.bpReleaseHandle.close();
        this.bpPcc = null;
    }

    public boolean zero() {
        if (bpPcc != null)
            bpPcc.requestManualCalibration(requestFinishedReceiver);
        return true;
    }

    public boolean isConnected() {
        return this.connected;
    }
    public boolean isSearching() {
        return this.searching;
    }

    public long getConnectTime() {return this.lConnectTime;}

    public BatteryStatus getDeviceBatteryStatus() {
        return deviceBatteryStatus;
    }

    private void subscribeToEvents() {
        if (bpPcc != null) {
            bpPcc.subscribeCalculatedPowerEvent((estTimestamp, eventFlags, dataSource, calculatedPower) -> data.setPower(calculatedPower.intValue()));

            bpPcc.subscribeInstantaneousCadenceEvent((estTimestamp, eventFlags, dataSource, instantaneousCadence) -> {
                if (instantaneousCadence >= 0) {
                    bBPCadExists = true;
                    data.setCadBP(instantaneousCadence);
                }
            });

            bpPcc.subscribePedalPowerBalanceEvent((estTimestamp, eventFlags, rightPedalIndicator, pedalPowerPercentage) -> {
                if (String.valueOf(rightPedalIndicator).equals("true")) {
                    data.setBalanceR(pedalPowerPercentage);
                } else {
                    data.setBalanceR(100 - pedalPowerPercentage);
                }
            });

            bpPcc.subscribeTorqueEffectivenessEvent((estTimestamp, eventFlags, powerOnlyUpdateEventCount, leftTorqueEffectiveness, rightTorqueEffectiveness) -> {
                data.setTorqueL(leftTorqueEffectiveness.intValue());
                data.setTorqueR(rightTorqueEffectiveness.intValue());
            });

            bpPcc.subscribePedalSmoothnessEvent((estTimestamp, eventFlags, powerOnlyUpdateEventCount, separatePedalSmoothnessSupport, leftOrCombinedPedalSmoothness, rightPedalSmoothness) -> {
                data.setSmoothL(leftOrCombinedPedalSmoothness.intValue());
                data.setSmoothR(rightPedalSmoothness.intValue());
            });

            bpPcc.subscribeBatteryStatusEvent(
                    (estTimestamp, eventFlags, cumulativeOperatingTime, batteryVoltage, batteryStatus, cumulativeOperatingTimeResolution, numberOfBatteries, batteryIdentifier) -> {
                        deviceBatteryStatus = batteryStatus;
                        bpAntBattery = batteryStatus.getIntValue();
                        //bcBattery = (float) ((batteryVoltage.doubleValue() - 3.0) / 1.2);
                    });

            bpPcc.subscribeCalibrationMessageEvent((estTimestamp, eventFlags, calibrationMessage) -> {
                String msg = "";

                switch (calibrationMessage.calibrationId) {
                    case GENERAL_CALIBRATION_FAIL:
                        msg = "Calibration Failed.";
                        break;
                    case GENERAL_CALIBRATION_SUCCESS:
                        String tmp = "";
                        if (calibrationMessage.calibrationData != null)
                            tmp = "DATA: " + calibrationMessage.calibrationData.toString();
                        msg = "Calibration Successful" + (tmp.equals("")?"":" (" + tmp + ")");
                        break;
                    case CUSTOM_CALIBRATION_RESPONSE:
                    case CUSTOM_CALIBRATION_UPDATE_SUCCESS:
                        StringBuilder bytes = new StringBuilder();
                        for (byte manufacturerByte : calibrationMessage.manufacturerSpecificData) {
                            String val = "[" + manufacturerByte + "]";
                            bytes.append(val);
                        }

                        msg = "CUSTOM: " + bytes.toString();
                        break;

                    case CTF_ZERO_OFFSET:
                        msg = "CTF ZERO: " + calibrationMessage.ctfOffset.toString();
                        break;
                    case UNRECOGNIZED:
                        msg = "Failed: UNRECOGNIZED. PluginLib Upgrade Required?";
                    default:
                        break;
                }

                if (!msg.equals(""))
                    MainActivity.toastListener.onToast(msg);
            });

        }
    }

    private final AntPluginPcc.IPluginAccessResultReceiver<AntPlusBikePowerPcc> mReceiverBP =
            new AntPluginPcc.IPluginAccessResultReceiver<AntPlusBikePowerPcc>() {
                @Override
                public void onResultReceived(AntPlusBikePowerPcc result,
                                             RequestAccessResult resultCode, DeviceState initialDeviceState) {

                    searching = false;
                    boolean show = false;
                    MainActivity.bBpAntExists = false;
                    switch (resultCode) {
                        case SUCCESS:
                            bpPcc = result;
                            connected = true;
                            subscribeToEvents();
                            show = true;
                            MainActivity.bAntSupported = true;
                            MainActivity.bBpAntExists = true;
                            iDeviceStateChangeReceiver.onDeviceStateChange(SENSOR_ID, String.valueOf(id), (connected ? SensorState.CONNECTED : SensorState.DISCONNECTED));
                            break;
                        case ADAPTER_NOT_DETECTED:
                            if (!MainActivity.bAntSupportMsgDisplayed)
                                show = true;
                            MainActivity.bAntSupportMsgDisplayed = true;
                            MainActivity.bAntSupported = false;
                            connected = false;
                            break;
                        case BAD_PARAMS:
                            break;
                        default :
                            MainActivity.bAntSupported = true;
                            break;
                    }

                    if (bDebug || show)
                        MainActivity.toastListener.onToast(AntPlus_Util.getResultCode(resultCode,"Power Meter"));
                }
            };

    private final AntPluginPcc.IDeviceStateChangeReceiver mDSCReceiverBP = new AntPluginPcc.IDeviceStateChangeReceiver()
    {
        @Override
        public void onDeviceStateChange(final DeviceState newDeviceState)
        {
            if (newDeviceState == DEAD) {
                bpPcc = null;
                connected = false;
                MainActivity.bBpAntExists = false;
                bBPCadExists = false;
                data.resetPower();
                iDeviceStateChangeReceiver.onDeviceStateChange(SENSOR_ID, String.valueOf(id), (connected ? SensorState.CONNECTED : SensorState.DISCONNECTED));
                //toastListener.onToast("Power Meter Disconnected");
            }
        }
    };

    private final AntPlusCommonPcc.IRequestFinishedReceiver requestFinishedReceiver =
            requestStatus -> {
                String msg;
                switch(requestStatus)
                {
                    case SUCCESS:
                        msg = "Zero Offset Requested...";
                        break;
                    case FAIL_PLUGINS_SERVICE_VERSION:
                        msg = "Plugin Service Upgrade Required?";
                        break;
                    default:
                        msg = "Zero Offset Request Failed. Please try again.";
                        msg += "\n" + requestStatus.toString();
                        break;
                }
                MainActivity.toastListener.onToast(msg);
            };
}
