package rnfive.djs.ant.antplus.pcc;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import rnfive.djs.ant.antplus.pcc.defines.EventFlag;
import rnfive.djs.ant.antplus.pccbase.AntPlusLegacyCommonPcc;
import rnfive.djs.ant.antplus.pccbase.AsyncScanController;
import rnfive.djs.ant.antplus.pccbase.PccReleaseHandle;
import rnfive.djs.ant.antplus.pccbase.AsyncScanController.IAsyncScanResultReceiver;
import rnfive.djs.ant.internal.compatibility.LegacyHeartRateCompat;
import rnfive.djs.ant.utility.log.LogAnt;
import java.math.BigDecimal;
import java.util.EnumSet;

public class AntPlusHeartRatePcc extends AntPlusLegacyCommonPcc {
    private static final String TAG = AntPlusHeartRatePcc.class.getSimpleName();
    private AntPlusHeartRatePcc.IHeartRateDataReceiver mHeartRateDataReceiver;
    private AntPlusHeartRatePcc.IPage4AddtDataReceiver mPage4AddtDataReceiver;
    private AntPlusHeartRatePcc.ICalculatedRrIntervalReceiver mCalculatedRrIntervalReceiver;
    private LegacyHeartRateCompat mCompat;

    protected int getRequiredServiceVersionForBind() {
        return 0;
    }

    public static PccReleaseHandle<AntPlusHeartRatePcc> requestAccess(Activity userActivity, Context bindToContext, boolean skipPreferredSearch, int searchProximityThreshold, IPluginAccessResultReceiver<AntPlusHeartRatePcc> resultReceiver, IDeviceStateChangeReceiver stateReceiver) {
        AntPlusHeartRatePcc potentialRetObj = new AntPlusHeartRatePcc();
        return requestAccess_Helper_SearchActivity(userActivity, bindToContext, skipPreferredSearch, searchProximityThreshold, potentialRetObj, resultReceiver, stateReceiver);
    }

    public static PccReleaseHandle<AntPlusHeartRatePcc> requestAccess(Activity userActivity, Context bindToContext, IPluginAccessResultReceiver<AntPlusHeartRatePcc> resultReceiver, IDeviceStateChangeReceiver stateReceiver) {
        return requestAccess(userActivity, bindToContext, false, -1, resultReceiver, stateReceiver);
    }

    public static PccReleaseHandle<AntPlusHeartRatePcc> requestAccess(Context bindToContext, int antDeviceNumber, int searchProximityThreshold, IPluginAccessResultReceiver<AntPlusHeartRatePcc> resultReceiver, IDeviceStateChangeReceiver stateReceiver) {
        AntPlusHeartRatePcc potentialRetObj = new AntPlusHeartRatePcc();
        return requestAccess_Helper_AsyncSearchByDevNumber(bindToContext, antDeviceNumber, searchProximityThreshold, potentialRetObj, resultReceiver, stateReceiver);
    }

    public static AsyncScanController<AntPlusHeartRatePcc> requestAsyncScanController(Context bindToContext, int searchProximityThreshold, IAsyncScanResultReceiver scanResultReceiver) {
        AntPlusHeartRatePcc potentialRetObj = new AntPlusHeartRatePcc();
        return requestAccess_Helper_AsyncScanController(bindToContext, searchProximityThreshold, potentialRetObj, scanResultReceiver);
    }

    private AntPlusHeartRatePcc() {
    }

    protected Intent getServiceBindIntent() {
        Intent it = new Intent();
        it.setComponent(new ComponentName("rn5.djs.ant.antplus", "rn5.djs.ant.antplus.heartrate.HeartRateService"));
        return it;
    }

    protected String getPluginPrintableName() {
        return "ANT+ Plugin: Heart Rate";
    }

    protected void handlePluginEvent(Message eventMsg) {
        Bundle b;
        long estTimestamp;
        EnumSet<EventFlag> eventFlags;
        BigDecimal calculatedRrInterval;
        int computedHeartRate;
        switch(eventMsg.arg1) {
            case 201:
                if (mHeartRateDataReceiver != null) {
                    b = eventMsg.getData();
                    estTimestamp = b.getLong("long_EstTimestamp");
                    eventFlags = EventFlag.getEventFlagsFromLong(b.getLong("long_EventFlags"));
                    computedHeartRate = b.getInt("int_computedHeartRate");
                    long heartBeatCounter = b.getLong("long_heartBeatCounter");
                    BigDecimal heartBeatEventTime = (BigDecimal)b.getSerializable("decimal_timestampOfLastEvent");
                    AntPlusHeartRatePcc.DataState dataState;
                    if (b.containsKey("int_dataState")) {
                        dataState = AntPlusHeartRatePcc.DataState.getValueFromInt(b.getInt("int_dataState"));
                    } else {
                        dataState = AntPlusHeartRatePcc.DataState.LIVE_DATA;
                    }

                    mHeartRateDataReceiver.onNewHeartRateData(estTimestamp, eventFlags, computedHeartRate, heartBeatCounter, heartBeatEventTime, dataState);
                }
                break;
            case 202:
                if (mCompat != null) {
                    b = eventMsg.getData();
                    estTimestamp = b.getLong("long_EstTimestamp");
                    eventFlags = EventFlag.getEventFlagsFromLong(b.getLong("long_EventFlags"));
                    calculatedRrInterval = (BigDecimal)b.getSerializable("decimal_timestampOfLastEvent");
                    mCompat.onNewHeartRateDataTimestamp(estTimestamp, eventFlags, calculatedRrInterval);
                }
                break;
            case 203:
                if (mPage4AddtDataReceiver != null) {
                    b = eventMsg.getData();
                    estTimestamp = b.getLong("long_EstTimestamp");
                    eventFlags = EventFlag.getEventFlagsFromLong(b.getLong("long_EventFlags"));
                    computedHeartRate = b.getInt("int_manufacturerSpecificByte");
                    BigDecimal previousHeartBeatEventTime = (BigDecimal)b.getSerializable("decimal_timestampOfPreviousToLastHeartBeatEvent");
                    mPage4AddtDataReceiver.onNewPage4AddtData(estTimestamp, eventFlags, computedHeartRate, previousHeartBeatEventTime);
                }
                break;
            case 204:
            case 205:
            case 206:
            default:
                super.handlePluginEvent(eventMsg);
                break;
            case 207:
                if (mCalculatedRrIntervalReceiver != null) {
                    b = eventMsg.getData();
                    estTimestamp = b.getLong("long_EstTimestamp");
                    eventFlags = EventFlag.getEventFlagsFromLong(b.getLong("long_EventFlags"));
                    calculatedRrInterval = (BigDecimal)b.getSerializable("decimal_calculatedRrInterval");
                    AntPlusHeartRatePcc.RrFlag rrFlag = AntPlusHeartRatePcc.RrFlag.getValueFromInt(b.getInt("int_rrFlag"));
                    mCalculatedRrIntervalReceiver.onNewCalculatedRrInterval(estTimestamp, eventFlags, calculatedRrInterval, rrFlag);
                }
        }

    }

    public void subscribeHeartRateDataEvent(AntPlusHeartRatePcc.IHeartRateDataReceiver HeartRateDataReceiver) {
        if (reportedServiceVersion < 20208) {
            if (HeartRateDataReceiver != null) {
                mCompat = new LegacyHeartRateCompat(HeartRateDataReceiver);
                subscribeToEvent(202);
            } else {
                mCompat = null;
                unsubscribeFromEvent(202);
            }

            HeartRateDataReceiver = mCompat;
        }

        mHeartRateDataReceiver = HeartRateDataReceiver;
        if (HeartRateDataReceiver != null) {
            subscribeToEvent(201);
        } else {
            unsubscribeFromEvent(201);
        }

    }

    public void subscribePage4AddtDataEvent(AntPlusHeartRatePcc.IPage4AddtDataReceiver Page4AddtDataReceiver) {
        mPage4AddtDataReceiver = Page4AddtDataReceiver;
        if (Page4AddtDataReceiver != null) {
            subscribeToEvent(203);
        } else {
            unsubscribeFromEvent(203);
        }

    }

    public boolean subscribeCalculatedRrIntervalEvent(AntPlusHeartRatePcc.ICalculatedRrIntervalReceiver CalculatedRrIntervalReceiver) {
        if (reportedServiceVersion < 20208) {
            LogAnt.w(TAG, "subscribeCalculatedRrIntervalEvent requires ANT+ Plugins Service >20208, installed: " + reportedServiceVersion);
            return false;
        } else {
            mCalculatedRrIntervalReceiver = CalculatedRrIntervalReceiver;
            if (CalculatedRrIntervalReceiver != null) {
                return subscribeToEvent(207);
            } else {
                unsubscribeFromEvent(207);
                return true;
            }
        }
    }

    public interface ICalculatedRrIntervalReceiver {
        void onNewCalculatedRrInterval(long var1, EnumSet<EventFlag> var3, BigDecimal var4, AntPlusHeartRatePcc.RrFlag var5);
    }

    public enum RrFlag {
        DATA_SOURCE_PAGE_4(1),
        DATA_SOURCE_CACHED(2),
        DATA_SOURCE_AVERAGED(3),
        HEART_RATE_ZERO_DETECTED(4),
        UNRECOGNIZED(-1);

        private final int intValue;

        RrFlag(int intValue) {
            this.intValue = intValue;
        }

        public int getIntValue() {
            return intValue;
        }

        public static AntPlusHeartRatePcc.RrFlag getValueFromInt(int intValue) {
            AntPlusHeartRatePcc.RrFlag[] var1 = values();

            for (RrFlag source : var1) {
                if (source.getIntValue() == intValue) {
                    return source;
                }
            }

            return UNRECOGNIZED;
        }
    }

    public interface IPage4AddtDataReceiver {
        void onNewPage4AddtData(long var1, EnumSet<EventFlag> var3, int var4, BigDecimal var5);
    }

    public interface IHeartRateDataReceiver {
        void onNewHeartRateData(long var1, EnumSet<EventFlag> var3, int var4, long var5, BigDecimal var7, AntPlusHeartRatePcc.DataState var8);
    }

    public enum DataState {
        LIVE_DATA(1),
        INITIAL_VALUE(2),
        ZERO_DETECTED(3),
        UNRECOGNIZED(-1);

        private final int intValue;

         DataState(int intValue) {
            this.intValue = intValue;
        }

        public int getIntValue() {
            return intValue;
        }

        public static AntPlusHeartRatePcc.DataState getValueFromInt(int intValue) {
            AntPlusHeartRatePcc.DataState[] var1 = values();

            for (DataState source : var1) {
                if (source.getIntValue() == intValue) {
                    return source;
                }
            }

            return UNRECOGNIZED;
        }
    }

    public static final class IpcDefines {
        public static final String PATH_ANTPLUS_HEARTRATEPLUGIN_PKG = "rn5.djs.ant.antplus";
        public static final String PATH_ANTPLUS_HEARTRATEPLUGIN_SERVICE = "rn5.djs.ant.antplus.heartrate.HeartRateService";
        public static final int MSG_EVENT_HEARTRATE_whatHEARTRATEDATA = 201;
        public static final String MSG_EVENT_HEARTRATE_HEARTRATEDATA_PARAM_intCOMPUTEDHEARTRATE = "int_computedHeartRate";
        public static final String MSG_EVENT_HEARTRATE_HEARTRATEDATA_PARAM_longHEARTBEATCOUNTER = "long_heartBeatCounter";
        public static final String MSG_EVENT_HEARTRATE_HEARTRATEDATA_PARAM_decimalHEARTBEATEVENTTIME = "decimal_timestampOfLastEvent";
        public static final String MSG_EVENT_HEARTRATE_HEARTRATEDATA_PARAM_intDATASTATE = "int_dataState";
        /** @deprecated */
        @Deprecated
        public static final int DEPRECATED_MSG_EVENT_HEARTRATE_whatHEARTBEATEVENTTIME = 202;
        /** @deprecated */
        @Deprecated
        public static final String DEPRECATED_MSG_EVENT_HEARTRATE_HEARTBEATEVENTTIME_PARAM_decimalHEARTBEATEVENTTIME = "decimal_timestampOfLastEvent";
        public static final int MSG_EVENT_HEARTRATE_whatPAGE4ADDTDATA = 203;
        public static final String MSG_EVENT_HEARTRATE_PAGE4ADDTDATA_PARAM_intMANUFACTURERSPECIFICBYTE = "int_manufacturerSpecificByte";
        public static final String MSG_EVENT_HEARTRATE_PAGE4ADDTDATA_PARAM_decimalPREVIOUSHEARTBEATEVENTTIME = "decimal_timestampOfPreviousToLastHeartBeatEvent";
        public static final int MSG_EVENT_HEARTRATE_whatCALCULATEDRRINTERVAL = 207;
        public static final String MSG_EVENT_HEARTRATE_RRINTERVAL_PARAM_decimalCALCULATEDRRINTERVAL = "decimal_calculatedRrInterval";
        public static final String MSG_EVENT_HEARTRATE_RRINTERVAL_PARAM_intRRFLAG = "int_rrFlag";

        private IpcDefines() {
        }
    }
}
