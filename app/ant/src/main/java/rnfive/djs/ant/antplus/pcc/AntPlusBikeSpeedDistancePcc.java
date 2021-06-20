package rnfive.djs.ant.antplus.pcc;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumSet;

import rnfive.djs.ant.antplus.pcc.defines.EventFlag;
import rnfive.djs.ant.antplus.pccbase.AntPlusBikeSpdCadCommonPcc;
import rnfive.djs.ant.antplus.pccbase.PccReleaseHandle;
import rnfive.djs.ant.utility.log.LogAnt;

public class AntPlusBikeSpeedDistancePcc extends AntPlusBikeSpdCadCommonPcc {
    private static final String TAG = AntPlusBikeSpeedDistancePcc.class.getSimpleName();
    private AntPlusBikeSpeedDistancePcc.CalculatedSpeedReceiver mCalculatedSpeedReceiver;
    private AntPlusBikeSpeedDistancePcc.CalculatedAccumulatedDistanceReceiver mCalculatedAccumulatedDistanceReceiver;
    private AntPlusBikeSpeedDistancePcc.IRawSpeedAndDistanceDataReceiver mRawSpeedAndDistanceDataReceiver;
    private AntPlusBikeSpeedDistancePcc.IMotionAndSpeedDataReceiver mMotionAndSpeedDataReceiver;

    public static PccReleaseHandle<AntPlusBikeSpeedDistancePcc> requestAccess(Activity userActivity, Context bindToContext, boolean skipPreferredSearch, int searchProximityThreshold, IPluginAccessResultReceiver<AntPlusBikeSpeedDistancePcc> resultReceiver, IDeviceStateChangeReceiver stateReceiver) {
        AntPlusBikeSpeedDistancePcc potentialRetObj = new AntPlusBikeSpeedDistancePcc();
        return requestAccessBSC_helper(false, userActivity, bindToContext, skipPreferredSearch, searchProximityThreshold, resultReceiver, stateReceiver, potentialRetObj);
    }

    public static PccReleaseHandle<AntPlusBikeSpeedDistancePcc> requestAccess(Activity userActivity, Context bindToContext, IPluginAccessResultReceiver<AntPlusBikeSpeedDistancePcc> resultReceiver, IDeviceStateChangeReceiver stateReceiver) {
        return requestAccess(userActivity, bindToContext, false, -1, resultReceiver, stateReceiver);
    }

    public static PccReleaseHandle<AntPlusBikeSpeedDistancePcc> requestAccess(Context bindToContext, int antDeviceNumber, int searchProximityThreshold, boolean isSpdCadCombinedSensor, IPluginAccessResultReceiver<AntPlusBikeSpeedDistancePcc> resultReceiver, IDeviceStateChangeReceiver stateReceiver) {
        AntPlusBikeSpeedDistancePcc potentialRetObj = new AntPlusBikeSpeedDistancePcc();
        return requestAccessBSC_helper(false, bindToContext, antDeviceNumber, searchProximityThreshold, isSpdCadCombinedSensor, resultReceiver, stateReceiver, potentialRetObj);
    }

    public static BikeSpdCadAsyncScanController<AntPlusBikeSpeedDistancePcc> requestAsyncScanController(Context bindToContext, int searchProximityThreshold, IBikeSpdCadAsyncScanResultReceiver scanResultReceiver) {
        AntPlusBikeSpeedDistancePcc potentialRetObj = new AntPlusBikeSpeedDistancePcc();
        return requestAccessBSC_Helper_AsyncScanController(false, bindToContext, searchProximityThreshold, potentialRetObj, scanResultReceiver);
    }

    private AntPlusBikeSpeedDistancePcc() {
        super(false);
    }

    protected Intent getServiceBindIntent() {
        Intent it = new Intent();
        it.setComponent(new ComponentName("com.dsi.ant.plugins.antplus", "com.dsi.ant.plugins.antplus.bikespdcad.CombinedBikeSpdCadService"));
        return it;
    }

    protected String getPluginPrintableName() {
        return "ANT+ Plugin: Bike Speed Distance";
    }

    protected void handlePluginEvent(Message eventMsg) {
        Bundle b;
        long estTimestamp;
        EnumSet<EventFlag> eventFlags;
        BigDecimal timestampOfLastEvent;
        switch(eventMsg.arg1) {
            case 201:
                if (mCalculatedSpeedReceiver != null) {
                    b = eventMsg.getData();
                    estTimestamp = b.getLong("long_EstTimestamp");
                    eventFlags = EventFlag.getEventFlagsFromLong(b.getLong("long_EventFlags"));
                    timestampOfLastEvent = (BigDecimal)b.getSerializable("decimal_calculatedSpeed");
                    mCalculatedSpeedReceiver.onNewCalculatedSpeedRaw(estTimestamp, eventFlags, timestampOfLastEvent);
                }
                break;
            case 202:
                if (mCalculatedAccumulatedDistanceReceiver != null) {
                    b = eventMsg.getData();
                    estTimestamp = b.getLong("long_EstTimestamp");
                    eventFlags = EventFlag.getEventFlagsFromLong(b.getLong("long_EventFlags"));
                    timestampOfLastEvent = (BigDecimal)b.getSerializable("decimal_calculatedAccumulatedDistance");
                    mCalculatedAccumulatedDistanceReceiver.onNewCalculatedAccumulatedDistanceRaw(estTimestamp, eventFlags, timestampOfLastEvent);
                }
                break;
            case 203:
                if (mRawSpeedAndDistanceDataReceiver != null) {
                    b = eventMsg.getData();
                    estTimestamp = b.getLong("long_EstTimestamp");
                    eventFlags = EventFlag.getEventFlagsFromLong(b.getLong("long_EventFlags"));
                    timestampOfLastEvent = (BigDecimal)b.getSerializable("decimal_timestampOfLastEvent");
                    long cumulativeRevolutions = b.getLong("long_cumulativeRevolutions");
                    mRawSpeedAndDistanceDataReceiver.onNewRawSpeedAndDistanceData(estTimestamp, eventFlags, timestampOfLastEvent, cumulativeRevolutions);
                }
                break;
            case 303:
                if (mMotionAndSpeedDataReceiver != null) {
                    b = eventMsg.getData();
                    estTimestamp = b.getLong("long_EstTimestamp");
                    eventFlags = EventFlag.getEventFlagsFromLong(b.getLong("long_EventFlags"));
                    boolean isStopped = b.getBoolean("bool_isStopped");
                    mMotionAndSpeedDataReceiver.onNewMotionAndSpeedData(estTimestamp, eventFlags, isStopped);
                }
                break;
            default:
                super.handlePluginEvent(eventMsg);
        }

    }

    public void subscribeCalculatedSpeedEvent(AntPlusBikeSpeedDistancePcc.CalculatedSpeedReceiver CalculatedSpeedReceiver) {
        mCalculatedSpeedReceiver = CalculatedSpeedReceiver;
        if (CalculatedSpeedReceiver != null) {
            subscribeToEvent(201);
        } else {
            unsubscribeFromEvent(201);
        }

    }

    public void subscribeCalculatedAccumulatedDistanceEvent(AntPlusBikeSpeedDistancePcc.CalculatedAccumulatedDistanceReceiver CalculatedAccumulatedDistanceReceiver) {
        mCalculatedAccumulatedDistanceReceiver = CalculatedAccumulatedDistanceReceiver;
        if (CalculatedAccumulatedDistanceReceiver != null) {
            subscribeToEvent(202);
        } else {
            unsubscribeFromEvent(202);
        }

    }

    public void subscribeRawSpeedAndDistanceDataEvent(AntPlusBikeSpeedDistancePcc.IRawSpeedAndDistanceDataReceiver RawSpeedAndDistanceDataReceiver) {
        mRawSpeedAndDistanceDataReceiver = RawSpeedAndDistanceDataReceiver;
        if (RawSpeedAndDistanceDataReceiver != null) {
            subscribeToEvent(203);
        } else {
            unsubscribeFromEvent(203);
        }

    }

    public boolean subscribeMotionAndSpeedDataEvent(AntPlusBikeSpeedDistancePcc.IMotionAndSpeedDataReceiver motionAndSpeedDataReceiver) {
        if (reportedServiceVersion < 20208) {
            LogAnt.w(TAG, "subscribeMotionAndSpeedDataEvent requires ANT+ Plugins Service >20208, installed: " + reportedServiceVersion);
            return false;
        } else {
            mMotionAndSpeedDataReceiver = motionAndSpeedDataReceiver;
            if (motionAndSpeedDataReceiver != null) {
                return subscribeToEvent(303);
            } else {
                unsubscribeFromEvent(303);
                return true;
            }
        }
    }

    public interface IMotionAndSpeedDataReceiver {
        void onNewMotionAndSpeedData(long var1, EnumSet<EventFlag> var3, boolean var4);
    }

    public interface IRawSpeedAndDistanceDataReceiver {
        void onNewRawSpeedAndDistanceData(long var1, EnumSet<EventFlag> var3, BigDecimal var4, long var5);
    }

    public abstract static class CalculatedAccumulatedDistanceReceiver {
        BigDecimal wheelCircumference;
        BigDecimal initialDistanceZeroVal = null;

        public CalculatedAccumulatedDistanceReceiver(BigDecimal wheelCircumference) {
            this.wheelCircumference = wheelCircumference;
        }

        public abstract void onNewCalculatedAccumulatedDistance(long var1, EnumSet<EventFlag> var3, BigDecimal var4);

        void onNewCalculatedAccumulatedDistanceRaw(long estTimestamp, EnumSet<EventFlag> eventFlags, BigDecimal calculatedAccumulatedDistanceRaw) {
            if (initialDistanceZeroVal == null) {
                initialDistanceZeroVal = calculatedAccumulatedDistanceRaw.multiply(wheelCircumference).setScale(10, RoundingMode.HALF_UP);
            }

            onNewCalculatedAccumulatedDistance(estTimestamp, eventFlags, calculatedAccumulatedDistanceRaw.multiply(wheelCircumference).setScale(10, RoundingMode.HALF_UP).subtract(initialDistanceZeroVal));
        }
    }

    public abstract static class CalculatedSpeedReceiver {
        BigDecimal wheelCircumference;

        public CalculatedSpeedReceiver(BigDecimal wheelCircumference) {
            this.wheelCircumference = wheelCircumference;
        }

        public abstract void onNewCalculatedSpeed(long var1, EnumSet<EventFlag> var3, BigDecimal var4);

        void onNewCalculatedSpeedRaw(long estTimestamp, EnumSet<EventFlag> eventFlags, BigDecimal calculatedSpeedRaw) {
            onNewCalculatedSpeed(estTimestamp, eventFlags, calculatedSpeedRaw.multiply(wheelCircumference).setScale(10, RoundingMode.HALF_UP));
        }
    }

    public static final class IpcDefines {
        public static final int MSG_EVENT_BIKESPEEDDISTANCE_whatCALCULATEDSPEED = 201;
        public static final String MSG_EVENT_BIKESPEEDDISTANCE_CALCULATEDSPEED_PARAM_decimalCALCULATEDSPEED = "decimal_calculatedSpeed";
        public static final int MSG_EVENT_BIKESPEEDDISTANCE_whatCALCULATEDACCUMULATEDDISTANCE = 202;
        public static final String MSG_EVENT_BIKESPEEDDISTANCE_CALCULATEDACCUMULATEDDISTANCE_PARAM_decimalCALCULATEDACCUMULATEDDISTANCE = "decimal_calculatedAccumulatedDistance";
        public static final int MSG_EVENT_BIKESPEEDDISTANCE_whatRAWSPEEDANDDISTANCEDATA = 203;
        public static final String MSG_EVENT_BIKESPEEDDISTANCE_RAWSPEEDANDDISTANCEDATA_PARAM_decimalTIMESTAMPOFLASTEVENT = "decimal_timestampOfLastEvent";
        public static final String MSG_EVENT_BIKESPEEDDISTANCE_RAWSPEEDANDDISTANCEDATA_PARAM_longCUMULATIVEREVOLUTIONS = "long_cumulativeRevolutions";
        public static final int MSG_EVENT_BIKESPEEDDISTANCE_whatMOTIONANDSPEEDDATA = 303;
        public static final String MSG_EVENT_BIKESPEEDDISTANCE_MOTIONANDSPEEDDATA_PARAM_boolISSTOPPED = "bool_isStopped";

        private IpcDefines() {
        }
    }
}
