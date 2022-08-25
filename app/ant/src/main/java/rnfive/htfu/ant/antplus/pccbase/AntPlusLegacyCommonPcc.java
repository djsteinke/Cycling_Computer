package rnfive.htfu.ant.antplus.pccbase;

import android.os.Bundle;
import android.os.Message;

import java.util.EnumSet;

import rnfive.htfu.ant.antplus.pcc.defines.EventFlag;
import rnfive.htfu.ant.utility.log.LogAnt;

public abstract class AntPlusLegacyCommonPcc extends AntPluginPcc {
    private static final String TAG = AntPlusLegacyCommonPcc.class.getSimpleName();
    AntPlusLegacyCommonPcc.ICumulativeOperatingTimeReceiver mCumulativeOperatingTimeReceiver;
    AntPlusLegacyCommonPcc.IManufacturerAndSerialReceiver mManufacturerAndSerialReceiver;
    AntPlusLegacyCommonPcc.IVersionAndModelReceiver mVersionAndModelReceiver;
    AntPlusCommonPcc.IRssiReceiver mRssiReceiver;

    public AntPlusLegacyCommonPcc() {
    }

    protected void handlePluginEvent(Message eventMsg) {
        Bundle b;
        long estTimestamp;
        EnumSet<EventFlag> eventFlags;
        int hardwareVersion;
        int softwareVersion;
        switch(eventMsg.arg1) {
            case 109:
                if (this.mRssiReceiver != null) {
                    b = eventMsg.getData();
                    estTimestamp = b.getLong("long_EstTimestamp");
                    eventFlags = EventFlag.getEventFlagsFromLong(b.getLong("long_EventFlags"));
                    hardwareVersion = b.getInt("int_rssi");
                    this.mRssiReceiver.onRssiData(estTimestamp, eventFlags, hardwareVersion);
                }
                break;
            case 204:
                if (this.mCumulativeOperatingTimeReceiver != null) {
                    b = eventMsg.getData();
                    estTimestamp = b.getLong("long_EstTimestamp");
                    eventFlags = EventFlag.getEventFlagsFromLong(b.getLong("long_EventFlags"));
                    long cumulativeOperatingTime = b.getLong("long_cumulativeOperatingTime");
                    this.mCumulativeOperatingTimeReceiver.onNewCumulativeOperatingTime(estTimestamp, eventFlags, cumulativeOperatingTime);
                }
                break;
            case 205:
                if (this.mManufacturerAndSerialReceiver != null) {
                    b = eventMsg.getData();
                    estTimestamp = b.getLong("long_EstTimestamp");
                    eventFlags = EventFlag.getEventFlagsFromLong(b.getLong("long_EventFlags"));
                    hardwareVersion = b.getInt("int_manufacturerID");
                    softwareVersion = b.getInt("int_serialNumber");
                    this.mManufacturerAndSerialReceiver.onNewManufacturerAndSerial(estTimestamp, eventFlags, hardwareVersion, softwareVersion);
                }
                break;
            case 206:
                if (this.mVersionAndModelReceiver != null) {
                    b = eventMsg.getData();
                    estTimestamp = b.getLong("long_EstTimestamp");
                    eventFlags = EventFlag.getEventFlagsFromLong(b.getLong("long_EventFlags"));
                    hardwareVersion = b.getInt("int_hardwareVersion");
                    softwareVersion = b.getInt("int_softwareVersion");
                    int modelNumber = b.getInt("int_modelNumber");
                    this.mVersionAndModelReceiver.onNewVersionAndModel(estTimestamp, eventFlags, hardwareVersion, softwareVersion, modelNumber);
                }
                break;
            default:
                LogAnt.d(TAG, "Unrecognized event received: " + eventMsg.arg1);
        }

    }

    public void subscribeCumulativeOperatingTimeEvent(AntPlusLegacyCommonPcc.ICumulativeOperatingTimeReceiver CumulativeOperatingTimeReceiver) {
        this.mCumulativeOperatingTimeReceiver = CumulativeOperatingTimeReceiver;
        if (CumulativeOperatingTimeReceiver != null) {
            this.subscribeToEvent(204);
        } else {
            this.unsubscribeFromEvent(204);
        }

    }

    public void subscribeManufacturerAndSerialEvent(AntPlusLegacyCommonPcc.IManufacturerAndSerialReceiver ManufacturerAndSerialReceiver) {
        this.mManufacturerAndSerialReceiver = ManufacturerAndSerialReceiver;
        if (ManufacturerAndSerialReceiver != null) {
            this.subscribeToEvent(205);
        } else {
            this.unsubscribeFromEvent(205);
        }

    }

    public void subscribeVersionAndModelEvent(AntPlusLegacyCommonPcc.IVersionAndModelReceiver VersionAndModelReceiver) {
        this.mVersionAndModelReceiver = VersionAndModelReceiver;
        if (VersionAndModelReceiver != null) {
            this.subscribeToEvent(206);
        } else {
            this.unsubscribeFromEvent(206);
        }

    }

    public boolean subscribeRssiEvent(AntPlusCommonPcc.IRssiReceiver RssiReceiver) {
        if (this.reportedServiceVersion < 30203) {
            LogAnt.w(TAG, "subscribeManufacturerSpecificDataEvent requires ANT+ Plugins Service >30203, installed: " + this.reportedServiceVersion);
            return false;
        } else {
            this.mRssiReceiver = RssiReceiver;
            if (RssiReceiver != null) {
                return this.subscribeToEvent(109);
            } else {
                this.unsubscribeFromEvent(109);
                return true;
            }
        }
    }

    public boolean supportsRssi() {
        return this.supportsRssiEvent;
    }

    public interface IVersionAndModelReceiver {
        void onNewVersionAndModel(long var1, EnumSet<EventFlag> var3, int var4, int var5, int var6);
    }

    public interface IManufacturerAndSerialReceiver {
        void onNewManufacturerAndSerial(long var1, EnumSet<EventFlag> var3, int var4, int var5);
    }

    public interface ICumulativeOperatingTimeReceiver {
        void onNewCumulativeOperatingTime(long var1, EnumSet<EventFlag> var3, long var4);
    }

    public static class IpcDefines {
        public static final int MSG_EVENT_LEGACYCOMMON_whatCUMULATIVEOPERATINGTIME = 204;
        public static final String MSG_EVENT_LEGACYCOMMON_CUMULATIVEOPERATINGTIME_PARAM_longCUMULATIVEOPERATINGTIME = "long_cumulativeOperatingTime";
        public static final int MSG_EVENT_LEGACYCOMMON_whatMANUFACTURERANDSERIAL = 205;
        public static final String MSG_EVENT_LEGACYCOMMON_MANUFACTURERANDSERIAL_PARAM_intMANUFACTURERID = "int_manufacturerID";
        public static final String MSG_EVENT_LEGACYCOMMON_MANUFACTURERANDSERIAL_PARAM_intSERIALNUMBER = "int_serialNumber";
        public static final int MSG_EVENT_LEGACYCOMMON_whatVERSIONANDMODEL = 206;
        public static final String MSG_EVENT_LEGACYCOMMON_VERSIONANDMODEL_PARAM_intHARDWAREVERSION = "int_hardwareVersion";
        public static final String MSG_EVENT_LEGACYCOMMON_VERSIONANDMODEL_PARAM_intSOFTWAREVERSION = "int_softwareVersion";
        public static final String MSG_EVENT_LEGACYCOMMON_VERSIONANDMODEL_PARAM_intMODELNUMBER = "int_modelNumber";

        public IpcDefines() {
        }
    }
}
