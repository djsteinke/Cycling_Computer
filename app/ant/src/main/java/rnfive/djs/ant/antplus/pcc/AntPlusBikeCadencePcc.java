package rnfive.djs.ant.antplus.pcc;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;

import java.math.BigDecimal;
import java.util.EnumSet;

import rnfive.djs.ant.antplus.pcc.defines.EventFlag;
import rnfive.djs.ant.antplus.pccbase.AntPlusBikeSpdCadCommonPcc;
import rnfive.djs.ant.antplus.pccbase.PccReleaseHandle;
import rnfive.djs.ant.utility.log.LogAnt;

public final class AntPlusBikeCadencePcc extends AntPlusBikeSpdCadCommonPcc {
    private static final String TAG = AntPlusBikeCadencePcc.class.getSimpleName();
    private AntPlusBikeCadencePcc.ICalculatedCadenceReceiver mCalculatedCadenceReceiver;
    private AntPlusBikeCadencePcc.IRawCadenceDataReceiver mRawCadenceDataReceiver;
    private AntPlusBikeCadencePcc.IMotionAndCadenceDataReceiver mMotionAndCadenceDataReceiver;

    private static PccReleaseHandle<AntPlusBikeCadencePcc> requestAccess(Activity userActivity, Context bindToContext, boolean skipPreferredSearch, int searchProximityThreshold, IPluginAccessResultReceiver<AntPlusBikeCadencePcc> resultReceiver, IDeviceStateChangeReceiver stateReceiver) {
        AntPlusBikeCadencePcc potentialRetObj = new AntPlusBikeCadencePcc();
        return requestAccessBSC_helper(true, userActivity, bindToContext, skipPreferredSearch, searchProximityThreshold, resultReceiver, stateReceiver, potentialRetObj);
    }

    public static PccReleaseHandle<AntPlusBikeCadencePcc> requestAccess(Activity userActivity, Context bindToContext, IPluginAccessResultReceiver<AntPlusBikeCadencePcc> resultReceiver, IDeviceStateChangeReceiver stateReceiver) {
        return requestAccess(userActivity, bindToContext, false, -1, resultReceiver, stateReceiver);
    }

    public static PccReleaseHandle<AntPlusBikeCadencePcc> requestAccess(Context bindToContext, int antDeviceNumber, int searchProximityThreshold, boolean isSpdCadCombinedSensor, IPluginAccessResultReceiver<AntPlusBikeCadencePcc> resultReceiver, IDeviceStateChangeReceiver stateReceiver) {
        AntPlusBikeCadencePcc potentialRetObj = new AntPlusBikeCadencePcc();
        return requestAccessBSC_helper(true, bindToContext, antDeviceNumber, searchProximityThreshold, isSpdCadCombinedSensor, resultReceiver, stateReceiver, potentialRetObj);
    }

    public static BikeSpdCadAsyncScanController<AntPlusBikeCadencePcc> requestAsyncScanController(Context bindToContext, int searchProximityThreshold, IBikeSpdCadAsyncScanResultReceiver scanResultReceiver) {
        AntPlusBikeCadencePcc potentialRetObj = new AntPlusBikeCadencePcc();
        return requestAccessBSC_Helper_AsyncScanController(true, bindToContext, searchProximityThreshold, potentialRetObj, scanResultReceiver);
    }

    private AntPlusBikeCadencePcc() {
        super(true);
    }

    protected Intent getServiceBindIntent() {
        Intent it = new Intent();
        it.setComponent(new ComponentName("com.dsi.ant.plugins.antplus", "com.dsi.ant.plugins.antplus.bikespdcad.CombinedBikeSpdCadService"));
        return it;
    }

    protected String getPluginPrintableName() {
        return "ANT+ Plugin: Bike Cadence";
    }

    protected void handlePluginEvent(Message eventMsg) {
        Bundle b;
        long estTimestamp;
        EnumSet<EventFlag> eventFlags;
        BigDecimal timestampOfLastEvent;
        switch(eventMsg.arg1) {
            case 301:
                if (mCalculatedCadenceReceiver != null) {
                    b = eventMsg.getData();
                    estTimestamp = b.getLong("long_EstTimestamp");
                    eventFlags = EventFlag.getEventFlagsFromLong(b.getLong("long_EventFlags"));
                    timestampOfLastEvent = (BigDecimal)b.getSerializable("decimal_calculatedCadence");
                    mCalculatedCadenceReceiver.onNewCalculatedCadence(estTimestamp, eventFlags, timestampOfLastEvent);
                }
                break;
            case 302:
                if (mRawCadenceDataReceiver != null) {
                    b = eventMsg.getData();
                    estTimestamp = b.getLong("long_EstTimestamp");
                    eventFlags = EventFlag.getEventFlagsFromLong(b.getLong("long_EventFlags"));
                    timestampOfLastEvent = (BigDecimal)b.getSerializable("decimal_timestampOfLastEvent");
                    long cumulativeRevolutions = b.getLong("long_cumulativeRevolutions");
                    mRawCadenceDataReceiver.onNewRawCadenceData(estTimestamp, eventFlags, timestampOfLastEvent, cumulativeRevolutions);
                }
                break;
            case 303:
                if (mMotionAndCadenceDataReceiver != null) {
                    b = eventMsg.getData();
                    estTimestamp = b.getLong("long_EstTimestamp");
                    eventFlags = EventFlag.getEventFlagsFromLong(b.getLong("long_EventFlags"));
                    boolean isStopped = b.getBoolean("bool_isStopped");
                    mMotionAndCadenceDataReceiver.onNewMotionAndCadenceData(estTimestamp, eventFlags, isStopped);
                }
                break;
            default:
                super.handlePluginEvent(eventMsg);
        }

    }

    public void subscribeCalculatedCadenceEvent(AntPlusBikeCadencePcc.ICalculatedCadenceReceiver CalculatedCadenceReceiver) {
        mCalculatedCadenceReceiver = CalculatedCadenceReceiver;
        if (CalculatedCadenceReceiver != null) {
            subscribeToEvent(301);
        } else {
            unsubscribeFromEvent(301);
        }

    }

    public void subscribeRawCadenceDataEvent(AntPlusBikeCadencePcc.IRawCadenceDataReceiver RawCadenceDataReceiver) {
        mRawCadenceDataReceiver = RawCadenceDataReceiver;
        if (RawCadenceDataReceiver != null) {
            subscribeToEvent(302);
        } else {
            unsubscribeFromEvent(302);
        }

    }

    public boolean subscribeMotionAndCadenceDataEvent(AntPlusBikeCadencePcc.IMotionAndCadenceDataReceiver motionAndCadenceDataReceiver) {
        if (reportedServiceVersion < 20208) {
            LogAnt.w(TAG, "subscribeMotionAndCadenceDataEvent requires ANT+ Plugins Service >20208, installed: " + reportedServiceVersion);
            return false;
        } else {
            mMotionAndCadenceDataReceiver = motionAndCadenceDataReceiver;
            if (motionAndCadenceDataReceiver != null) {
                return subscribeToEvent(303);
            } else {
                unsubscribeFromEvent(303);
                return true;
            }
        }
    }

    public interface IMotionAndCadenceDataReceiver {
        void onNewMotionAndCadenceData(long var1, EnumSet<EventFlag> var3, boolean var4);
    }

    public interface IRawCadenceDataReceiver {
        void onNewRawCadenceData(long var1, EnumSet<EventFlag> var3, BigDecimal var4, long var5);
    }

    public interface ICalculatedCadenceReceiver {
        void onNewCalculatedCadence(long var1, EnumSet<EventFlag> var3, BigDecimal var4);
    }

    public static final class IpcDefines {
        public static final int MSG_EVENT_BIKECADENCE_whatCALCULATEDCADENCE = 301;
        public static final String MSG_EVENT_BIKECADENCE_CALCULATEDCADENCE_PARAM_decimalCALCULATEDCADENCE = "decimal_calculatedCadence";
        public static final int MSG_EVENT_BIKECADENCE_whatRAWCADENCEDATA = 302;
        public static final String MSG_EVENT_BIKECADENCE_RAWCADENCEDATA_PARAM_decimalTIMESTAMPOFLASTEVENT = "decimal_timestampOfLastEvent";
        public static final String MSG_EVENT_BIKECADENCE_RAWCADENCEDATA_PARAM_longCUMULATIVEREVOLUTIONS = "long_cumulativeRevolutions";
        public static final int MSG_EVENT_BIKECADENCE_whatMOTIONANDCADENCEDATA = 303;
        public static final String MSG_EVENT_BIKECADENCE_MOTIONANDCADENCEDATA_PARAM_boolISSTOPPED = "bool_isStopped";

        private IpcDefines() {
        }
    }
}
