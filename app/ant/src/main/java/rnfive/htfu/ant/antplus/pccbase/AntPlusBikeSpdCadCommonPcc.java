package rnfive.htfu.ant.antplus.pccbase;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;

import java.math.BigDecimal;
import java.util.EnumSet;

import rnfive.htfu.ant.antplus.pcc.defines.BatteryStatus;
import rnfive.htfu.ant.antplus.pcc.defines.DeviceType;
import rnfive.htfu.ant.antplus.pcc.defines.EventFlag;
import rnfive.htfu.ant.antplus.pcc.defines.RequestAccessResult;
import rnfive.htfu.ant.antplus.pccbase.AsyncScanController.AsyncScanResultDeviceInfo;
import rnfive.htfu.ant.internal.pluginsipc.AntPluginDeviceDbProvider.DeviceDbDeviceInfo;
import rnfive.htfu.ant.utility.log.LogAnt;
import rnfive.htfu.ant.utility.parcel.ParcelPacker;
import rnfive.htfu.ant.utility.parcel.ParcelUnpacker;

public abstract class AntPlusBikeSpdCadCommonPcc extends AntPlusLegacyCommonPcc {
    private static final String TAG = AntPlusBikeSpdCadCommonPcc.class.getSimpleName();
    boolean isInstanceCadencePcc;
    private Boolean mIsSpeedAndCadenceCombinedSensor = null;
    private AntPlusBikeSpdCadCommonPcc.IBatteryStatusReceiver mBatteryStatusReceiver;

    protected int getRequiredServiceVersionForBind() {
        return 10800;
    }

    protected AntPlusBikeSpdCadCommonPcc(boolean isInstanceCadencePcc) {
        this.isInstanceCadencePcc = isInstanceCadencePcc;
    }

    protected static <T extends AntPlusBikeSpdCadCommonPcc> PccReleaseHandle<T> requestAccessBSC_helper(boolean isCadence, Activity userActivity, Context bindToContext, boolean skipPreferredSearch, int searchProximityThreshold, IPluginAccessResultReceiver<T> resultReceiver, IDeviceStateChangeReceiver stateReceiver, T potentialRetObj) {
        Bundle b = new Bundle();
        b.putInt("int_RequestAccessMode", 1);
        b.putBoolean("b_ForceManualSelect", skipPreferredSearch);
        b.putInt("int_ProximityBin", searchProximityThreshold);
        b.putBoolean("bool_IsCadencePcc", isCadence);
        return requestAccess_Helper_Main(bindToContext, b, potentialRetObj, new AntPlusBikeSpdCadCommonPcc.RequestAccessResultHandlerUIBikeSC(userActivity), resultReceiver, stateReceiver);
    }

    protected static <T extends AntPlusBikeSpdCadCommonPcc> PccReleaseHandle<T> requestAccessBSC_helper(boolean isCadence, Context bindToContext, int antDeviceNumber, int searchProximityThreshold, boolean isSpdCadCombinedSensor, IPluginAccessResultReceiver<T> resultReceiver, IDeviceStateChangeReceiver stateReceiver, T potentialRetObj) {
        Bundle b = new Bundle();
        b.putInt("int_RequestAccessMode", 3);
        b.putInt("int_AntDeviceID", antDeviceNumber);
        b.putInt("int_ProximityBin", searchProximityThreshold);
        b.putBoolean("bool_IsSpdCadCombinedSensor", isSpdCadCombinedSensor);
        b.putBoolean("bool_IsCadencePcc", isCadence);
        return requestAccess_Helper_Main(bindToContext, b, potentialRetObj, new AntPlusBikeSpdCadCommonPcc.RequestAccessResultHandlerAsyncSearchBikeSC(potentialRetObj), resultReceiver, stateReceiver);
    }

    protected static <T extends AntPlusBikeSpdCadCommonPcc> AntPlusBikeSpdCadCommonPcc.BikeSpdCadAsyncScanController<T> requestAccessBSC_Helper_AsyncScanController(boolean isCadence, Context bindingContext, int searchProximityThreshold, T retPccObject, AntPlusBikeSpdCadCommonPcc.IBikeSpdCadAsyncScanResultReceiver scanResultReceiver) {
        AntPlusBikeSpdCadCommonPcc.BikeSpdCadAsyncScanController<T> controller = new AntPlusBikeSpdCadCommonPcc.BikeSpdCadAsyncScanController(scanResultReceiver, retPccObject);
        Bundle b = new Bundle();
        b.putBoolean("bool_IsCadencePcc", isCadence);
        requestAsyncScan_Helper_SubMain(bindingContext, searchProximityThreshold, b, retPccObject, controller);
        return controller;
    }

    protected void setIsSpeedAndCadence(boolean isSpeedAndCadence) {
        if (this.mIsSpeedAndCadenceCombinedSensor != null) {
            throw new IllegalStateException("Can't reinitialize isSpeedAndCadence");
        } else {
            this.mIsSpeedAndCadenceCombinedSensor = isSpeedAndCadence;
        }
    }

    public boolean isSpeedAndCadenceCombinedSensor() {
        return this.mIsSpeedAndCadenceCombinedSensor;
    }

    protected Message createCmdMsg(int cmdCode, Bundle msgData) {
        if (msgData == null) {
            msgData = new Bundle();
        }

        msgData.putBoolean("bool_IsCadencePcc", this.isInstanceCadencePcc);
        return super.createCmdMsg(cmdCode, msgData);
    }

    protected void handlePluginEvent(Message eventMsg) {
        switch(eventMsg.arg1) {
            case 207:
                if (this.mBatteryStatusReceiver != null) {
                    Bundle b = eventMsg.getData();
                    long estTimestamp = b.getLong("long_EstTimestamp");
                    EnumSet<EventFlag> eventFlags = EventFlag.getEventFlagsFromLong(b.getLong("long_EventFlags"));
                    BigDecimal batteryVoltage = (BigDecimal)b.getSerializable("decimal_batteryVoltage");
                    BatteryStatus batteryStatus = BatteryStatus.getValueFromInt(b.getInt("int_batteryStatus"));
                    this.mBatteryStatusReceiver.onNewBatteryStatus(estTimestamp, eventFlags, batteryVoltage, batteryStatus);
                }
                break;
            default:
                super.handlePluginEvent(eventMsg);
        }

    }

    public boolean subscribeBatteryStatusEvent(AntPlusBikeSpdCadCommonPcc.IBatteryStatusReceiver batteryStatusReceiver) {
        if (this.reportedServiceVersion < 20208) {
            LogAnt.w(TAG, "subscribeBatteryStatusEvent requires ANT+ Plugins Service >20208, installed: " + this.reportedServiceVersion);
            return false;
        } else {
            this.mBatteryStatusReceiver = batteryStatusReceiver;
            if (batteryStatusReceiver != null) {
                return this.subscribeToEvent(207);
            } else {
                this.unsubscribeFromEvent(207);
                return true;
            }
        }
    }

    private static class RequestAccessResultHandlerUIBikeSC<T extends AntPlusBikeSpdCadCommonPcc> extends RequestAccessResultHandler_UI<T> {
        public RequestAccessResultHandlerUIBikeSC(Activity foregroundActivity) {
            super(foregroundActivity);
        }

        public boolean handleRequestAccessResult(Message msg) {
            if (msg.what == 0) {
                (this.retPccObject).setIsSpeedAndCadence(msg.getData().getBoolean("bool_IsSpdCadCombinedSensor"));
            }

            return super.handleRequestAccessResult(msg);
        }
    }

    private static class RequestAccessResultHandlerAsyncSearchBikeSC<T extends AntPlusBikeSpdCadCommonPcc> extends RequestAccessResultHandler_AsyncSearchByDevNumber<T> {
        AntPlusBikeSpdCadCommonPcc pccObj;

        public RequestAccessResultHandlerAsyncSearchBikeSC(AntPlusBikeSpdCadCommonPcc pccRetObj) {
            this.pccObj = pccRetObj;
        }

        public boolean handleRequestAccessResult(Message msg) {
            if (msg.what == 0) {
                this.pccObj.setIsSpeedAndCadence(msg.getData().getBoolean("bool_IsSpdCadCombinedSensor"));
            }

            return super.handleRequestAccessResult(msg);
        }
    }

    public static class MultiDeviceSearchSpdCadResult extends MultiDeviceSearch.MultiDeviceSearchResult {
        private static final int IPC_VERSION = 1;
        protected final DeviceDbDeviceInfo mCadenceInfo;
        public static final Parcelable.Creator<MultiDeviceSearchSpdCadResult> CREATOR = new Parcelable.Creator<MultiDeviceSearchSpdCadResult>() {
            public AntPlusBikeSpdCadCommonPcc.MultiDeviceSearchSpdCadResult[] newArray(int size) {
                return new AntPlusBikeSpdCadCommonPcc.MultiDeviceSearchSpdCadResult[size];
            }

            public AntPlusBikeSpdCadCommonPcc.MultiDeviceSearchSpdCadResult createFromParcel(Parcel source) {
                return new AntPlusBikeSpdCadCommonPcc.MultiDeviceSearchSpdCadResult(source);
            }
        };

        public MultiDeviceSearchSpdCadResult(int resultID, DeviceDbDeviceInfo spdInfo, DeviceDbDeviceInfo cadInfo, boolean alreadyConnected) {
            super(resultID, DeviceType.BIKE_SPDCAD, spdInfo, alreadyConnected);
            this.mCadenceInfo = cadInfo;
        }

        public boolean isPreferredDevice() {
            return this.isPreferredForSpeed() | this.isPreferredForCadence();
        }

        public boolean isPreferredForSpeed() {
            return this.mInfo.isPreferredDevice;
        }

        public boolean isPreferredForCadence() {
            return this.mCadenceInfo.isPreferredDevice;
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            ParcelPacker packer = new ParcelPacker(dest);
            dest.writeInt(1);
            ParcelPacker innerpacker = new ParcelPacker(dest);
            dest.writeParcelable(this.mCadenceInfo, flags);
            innerpacker.finish();
            packer.finish();
        }

        protected MultiDeviceSearchSpdCadResult(Parcel source) {
            super(source);
            ParcelUnpacker unpacker = new ParcelUnpacker(source);
            int sourceIpcVersion = source.readInt();
            ParcelUnpacker innerunpack = new ParcelUnpacker(source);
            this.mCadenceInfo = source.readParcelable(DeviceDbDeviceInfo.class.getClassLoader());
            innerunpack.finish();
            if (sourceIpcVersion > 1) {
                LogAnt.i(AntPlusBikeSpdCadCommonPcc.TAG, "Decoding " + AntPlusBikeSpdCadCommonPcc.MultiDeviceSearchSpdCadResult.class.getSimpleName() + " version " + sourceIpcVersion + " using version " + 1 + " decoder");
            }

            unpacker.finish();
        }
    }

    public static class BikeSpdCadAsyncScanController<T extends AntPlusBikeSpdCadCommonPcc> extends AsyncScanController<T> {
        AntPlusBikeSpdCadCommonPcc.IBikeSpdCadAsyncScanResultReceiver bikeResultReceiver;

        BikeSpdCadAsyncScanController(AntPlusBikeSpdCadCommonPcc.IBikeSpdCadAsyncScanResultReceiver bikeResultReceiver, T pccObject) {
            super(pccObject);
            this.bikeResultReceiver = bikeResultReceiver;
        }

        protected void sendFailureToReceiver(RequestAccessResult requestAccessResult) {
            this.bikeResultReceiver.onSearchStopped(requestAccessResult);
        }

        protected void sendResultToReceiver(Bundle result) {
            AsyncScanResultDeviceInfo newResult = result.getParcelable("parcelable_AsyncScanResultDeviceInfo");
            boolean isSpdAndCadComboSensor = result.getBoolean("bool_IsCombinedSensor");
            this.bikeResultReceiver.onSearchResult(new AntPlusBikeSpdCadCommonPcc.BikeSpdCadAsyncScanResultDeviceInfo(newResult, isSpdAndCadComboSensor));
        }

        protected void handleReqAccSuccess(Message msg, T retPccObject, IPluginAccessResultReceiver<T> resultReceiver) {
            if (msg.what == 0) {
                retPccObject.setIsSpeedAndCadence(msg.getData().getBoolean("bool_IsSpdCadCombinedSensor"));
            }

            super.handleReqAccSuccess(msg, retPccObject, resultReceiver);
        }

        public PccReleaseHandle<T> requestDeviceAccess(AntPlusBikeSpdCadCommonPcc.BikeSpdCadAsyncScanResultDeviceInfo deviceToConnectTo, IPluginAccessResultReceiver<T> resultReceiver, IDeviceStateChangeReceiver stateReceiver) {
            Bundle bikeParams = new Bundle();
            bikeParams.putBoolean("bool_IsSpdCadCombinedSensor", deviceToConnectTo.isSpdAndCadComboSensor);
            return this.requestDeviceAccess(deviceToConnectTo.resultInfo, bikeParams, resultReceiver, stateReceiver);
        }

        public PccReleaseHandle<T> requestDeviceAccess(AsyncScanResultDeviceInfo deviceToConnectTo, IPluginAccessResultReceiver<T> resultReceiver, IDeviceStateChangeReceiver stateReceiver) {
            LogAnt.w(AntPlusBikeSpdCadCommonPcc.TAG, "WARNING! Using old bike connect method, possibly connecting to wrong device or not connecting.");
            LogAnt.d(AntPlusBikeSpdCadCommonPcc.TAG, "WARNING! Using old bike connect method, app should use new connect method which takes BikeSpdCadAsyncScanResultDeviceInfo object");
            return super.requestDeviceAccess(deviceToConnectTo, resultReceiver, stateReceiver);
        }
    }

    public interface IBikeSpdCadAsyncScanResultReceiver {
        void onSearchStopped(RequestAccessResult var1);

        void onSearchResult(AntPlusBikeSpdCadCommonPcc.BikeSpdCadAsyncScanResultDeviceInfo var1);
    }

    public static class BikeSpdCadAsyncScanResultDeviceInfo {
        public static final String MSG_BIKESPDCAD_ASYNCSCANRESULTINFO_boolISCOMBINEDSENSOR = "bool_IsCombinedSensor";
        public final boolean isSpdAndCadComboSensor;
        public final AsyncScanResultDeviceInfo resultInfo;

        public BikeSpdCadAsyncScanResultDeviceInfo(AsyncScanResultDeviceInfo resultInfo, boolean isSpdAndCadComboSensor) {
            this.resultInfo = resultInfo;
            this.isSpdAndCadComboSensor = isSpdAndCadComboSensor;
        }
    }

    public interface IBatteryStatusReceiver {
        void onNewBatteryStatus(long var1, EnumSet<EventFlag> var3, BigDecimal var4, BatteryStatus var5);
    }

    public class IpcDefines {
        public static final String PATH_ANTPLUS_BIKECOMBOSPDCADPLUGIN_PKG = "com.dsi.ant.plugins.antplus";
        public static final String PATH_ANTPLUS_BIKECOMBOSPDCADPLUGIN_SERVICE = "com.dsi.ant.plugins.antplus.bikespdcad.CombinedBikeSpdCadService";
        public static final String MSG_BIKESPDCAD_REQ_PARAM_boolISCADENCEPCC = "bool_IsCadencePcc";
        public static final String MSG_BIKESPDCAD_REQACC_ASYNCSEARCHBYANTDEVID_PARAM_boolISSPDCADCOMBINEDSENSOR = "bool_IsSpdCadCombinedSensor";
        public static final String MSG_BIKESPDCAD_REQACC_RESULT_boolISSPDCADCOMBINEDSENSOR = "bool_IsSpdCadCombinedSensor";
        public static final int MSG_EVENT_BIKESPDCAD_whatBATTERYSTATUS = 207;
        public static final String MSG_EVENT_BIKESPDCAD_BATTERYSTATUS_PARAM_decimalBATTERYVOLTAGE = "decimal_batteryVoltage";
        public static final String MSG_EVENT_BIKESPDCAD_BATTERYSTATUS_PARAM_intBATTERYSTATUS = "int_batteryStatus";

        public IpcDefines() {
        }
    }
}
