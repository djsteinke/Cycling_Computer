package rnfive.htfu.ant.antplus.pccbase;

import android.os.Bundle;
import android.os.Message;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.concurrent.Semaphore;

import rnfive.htfu.ant.antplus.pcc.defines.BatteryStatus;
import rnfive.htfu.ant.antplus.pcc.defines.EventFlag;
import rnfive.htfu.ant.antplus.pcc.defines.RequestStatus;
import rnfive.htfu.ant.utility.log.LogAnt;

public abstract class AntPlusCommonPcc extends AntPluginPcc {
    private static final String TAG = AntPlusCommonPcc.class.getSimpleName();
    protected AntPlusCommonPcc.IRequestFinishedReceiver mRequestFinishedReceiver;
    protected Semaphore mCommandLock = new Semaphore(1);
    AntPlusCommonPcc.IManufacturerIdentificationReceiver mManufacturerIdentificationReceiver;
    AntPlusCommonPcc.IProductInformationReceiver mProductInformationReceiver;
    AntPlusCommonPcc.IBatteryStatusReceiver mBatteryStatusReceiver;
    AntPlusCommonPcc.IManufacturerSpecificDataReceiver mManufacturerSpecificDataReceiver;
    AntPlusCommonPcc.IRssiReceiver mRssiReceiver;

    public AntPlusCommonPcc() {
    }

    protected void handlePluginEvent(Message eventMsg) {
        Bundle b;
        long estTimestamp;
        EnumSet eventFlags;
        int rssi;
        int supplementaryRevision;
        switch(eventMsg.arg1) {
            case 100:
                if (this.mManufacturerIdentificationReceiver != null) {
                    b = eventMsg.getData();
                    estTimestamp = b.getLong("long_EstTimestamp");
                    eventFlags = EventFlag.getEventFlagsFromLong(b.getLong("long_EventFlags"));
                    rssi = b.getInt("int_hardwareRevision");
                    supplementaryRevision = b.getInt("int_manufacturerID");
                    int modelNumber = b.getInt("int_modelNumber");
                    this.mManufacturerIdentificationReceiver.onNewManufacturerIdentification(estTimestamp, eventFlags, rssi, supplementaryRevision, modelNumber);
                }
                break;
            case 101:
                if (this.mProductInformationReceiver != null) {
                    b = eventMsg.getData();
                    estTimestamp = b.getLong("long_EstTimestamp");
                    eventFlags = EventFlag.getEventFlagsFromLong(b.getLong("long_EventFlags"));
                    rssi = b.getInt("int_softwareRevision");
                    supplementaryRevision = b.getInt("int_supplementaryRevision", -2);
                    long serialNumber = b.getLong("long_serialNumber");
                    this.mProductInformationReceiver.onNewProductInformation(estTimestamp, eventFlags, rssi, supplementaryRevision, serialNumber);
                }
                break;
            case 102:
                if (this.mBatteryStatusReceiver != null) {
                    b = eventMsg.getData();
                    estTimestamp = b.getLong("long_EstTimestamp");
                    eventFlags = EventFlag.getEventFlagsFromLong(b.getLong("long_EventFlags"));
                    long cumulativeOperatingTime = b.getLong("long_cumulativeOperatingTime");
                    BigDecimal batteryVoltage = (BigDecimal)b.getSerializable("decimal_batteryVoltage");
                    BatteryStatus batteryStatus = BatteryStatus.getValueFromInt(b.getInt("int_batteryStatusCode"));
                    int cumulativeOperatingTimeResolution = b.getInt("int_cumulativeOperatingTimeResolution");
                    int numberOfBatteries = b.getInt("int_numberOfBatteries", -2);
                    int batteryIdentifier = b.getInt("int_batteryIdentifier", -2);
                    this.mBatteryStatusReceiver.onNewBatteryStatus(estTimestamp, eventFlags, cumulativeOperatingTime, batteryVoltage, batteryStatus, cumulativeOperatingTimeResolution, numberOfBatteries, batteryIdentifier);
                }
                break;
            case 103:
                if (this.mManufacturerSpecificDataReceiver != null) {
                    b = eventMsg.getData();
                    estTimestamp = b.getLong("long_EstTimestamp");
                    eventFlags = EventFlag.getEventFlagsFromLong(b.getLong("long_EventFlags"));
                    byte[] rawDataBytes = b.getByteArray("arrayByte_rawDataBytes");
                    this.mManufacturerSpecificDataReceiver.onNewManufacturerSpecificData(estTimestamp, eventFlags, rawDataBytes);
                }
                break;
            case 104:
            case 105:
            case 106:
            case 108:
            default:
                LogAnt.d(TAG, "Unrecognized event received: " + eventMsg.arg1);
                break;
            case 107:
                AntPlusCommonPcc.IRequestFinishedReceiver tempReceiver = this.mRequestFinishedReceiver;
                this.mRequestFinishedReceiver = null;
                this.mCommandLock.release();
                if (tempReceiver != null) {
                    Bundle bundle = eventMsg.getData();
                    int requestStatus = bundle.getInt("int_requestStatus");
                    tempReceiver.onNewRequestFinished(RequestStatus.getValueFromInt(requestStatus));
                }
                break;
            case 109:
                if (this.mRssiReceiver != null) {
                    b = eventMsg.getData();
                    estTimestamp = b.getLong("long_EstTimestamp");
                    eventFlags = EventFlag.getEventFlagsFromLong(b.getLong("long_EventFlags"));
                    rssi = b.getInt("int_rssi");
                    this.mRssiReceiver.onRssiData(estTimestamp, eventFlags, rssi);
                }
        }

    }

    public void subscribeManufacturerIdentificationEvent(AntPlusCommonPcc.IManufacturerIdentificationReceiver ManufacturerIdentificationReceiver) {
        this.mManufacturerIdentificationReceiver = ManufacturerIdentificationReceiver;
        if (ManufacturerIdentificationReceiver != null) {
            this.subscribeToEvent(100);
        } else {
            this.unsubscribeFromEvent(100);
        }

    }

    public void subscribeProductInformationEvent(AntPlusCommonPcc.IProductInformationReceiver ProductInformationReceiver) {
        this.mProductInformationReceiver = ProductInformationReceiver;
        if (ProductInformationReceiver != null) {
            this.subscribeToEvent(101);
        } else {
            this.unsubscribeFromEvent(101);
        }

    }

    public void subscribeBatteryStatusEvent(AntPlusCommonPcc.IBatteryStatusReceiver BatteryStatusReceiver) {
        this.mBatteryStatusReceiver = BatteryStatusReceiver;
        if (BatteryStatusReceiver != null) {
            this.subscribeToEvent(102);
        } else {
            this.unsubscribeFromEvent(102);
        }

    }

    public boolean subscribeManufacturerSpecificDataEvent(AntPlusCommonPcc.IManufacturerSpecificDataReceiver ManufacturerSpecificDataReceiver) {
        if (this.reportedServiceVersion < 20206) {
            LogAnt.w(TAG, "subscribeManufacturerSpecificDataEvent requires ANT+ Plugins Service >20206, installed: " + this.reportedServiceVersion);
            return false;
        } else {
            this.mManufacturerSpecificDataReceiver = ManufacturerSpecificDataReceiver;
            if (ManufacturerSpecificDataReceiver != null) {
                return this.subscribeToEvent(103);
            } else {
                this.unsubscribeFromEvent(103);
                return true;
            }
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

    public boolean requestCommonDataPage(AntPlusCommonPcc.CommonDataPage commonDataPage, AntPlusCommonPcc.IRequestFinishedReceiver requestFinishedReceiver) {
        String cmdName = "requestCommonDataPage";
        int whatCmd = 106;
        Bundle params = new Bundle();
        params.putInt("int_requestedDataPage", commonDataPage.getIntValue());
        return this.sendRequestCommand(cmdName, whatCmd, params, requestFinishedReceiver, 20209);
    }

    public boolean requestManufacturerSpecificDataPage(int manufacturerSpecificDataPage, AntPlusCommonPcc.IRequestFinishedReceiver requestFinishedReceiver) {
        if (manufacturerSpecificDataPage >= 240 && manufacturerSpecificDataPage <= 255) {
            String cmdName = "requestManufacturerSpecificDataPage";
            int whatCmd = 106;
            Bundle params = new Bundle();
            params.putInt("int_requestedDataPage", manufacturerSpecificDataPage);
            return this.sendRequestCommand(cmdName, whatCmd, params, requestFinishedReceiver, 20209);
        } else {
            throw new IllegalArgumentException("The manufacturerSpecificDataPage must be within the range of 240 to 255");
        }
    }

    public boolean supportsRssi() {
        return this.supportsRssiEvent;
    }

    public boolean sendManufacturerSpecificDataPage(int manufacturerSpecificDataPage, byte[] manufacturerSpecificDataPagePayload, AntPlusCommonPcc.IRequestFinishedReceiver requestFinishedReceiver) {
        String cmdName = "sendManufacturerSpecificDataPage";
        int whatCmd = 111;
        Bundle params = new Bundle();
        params.putInt("int_ManufacturerSpecificPageNumber", manufacturerSpecificDataPage);
        params.putByteArray("arrayByte_ManufacturerSpecificPageData", manufacturerSpecificDataPagePayload);
        return this.sendRequestCommand(cmdName, whatCmd, params, requestFinishedReceiver, 30504);
    }

    protected boolean sendRequestCommand(String cmdName, int whatCmd, Bundle params, AntPlusCommonPcc.IRequestFinishedReceiver requestFinishedReceiver, Integer requiredServiceVersion) {
        if (requiredServiceVersion != null && this.reportedServiceVersion < requiredServiceVersion) {
            LogAnt.w(TAG, cmdName + " requires ANT+ Plugins Service >=" + requiredServiceVersion + ", installed: " + this.reportedServiceVersion);
            if (requestFinishedReceiver != null) {
                requestFinishedReceiver.onNewRequestFinished(RequestStatus.FAIL_PLUGINS_SERVICE_VERSION);
            }

            return false;
        } else if (!this.mCommandLock.tryAcquire()) {
            LogAnt.e(TAG, "Cmd " + cmdName + " failed to start because a local command is still processing.");
            return false;
        } else {
            this.mRequestFinishedReceiver = requestFinishedReceiver;
            Message cmdMsg = Message.obtain();
            cmdMsg.what = whatCmd;
            if (params != null) {
                cmdMsg.setData(params);
            }

            Message ret = this.sendPluginCommand(cmdMsg);
            if (ret == null) {
                LogAnt.e(TAG, "Cmd " + cmdName + " died in sendPluginCommand()");
                this.mCommandLock.release();
                return false;
            } else if (ret.arg1 == -3) {
                LogAnt.e(TAG, "Cmd " + cmdName + " failed with code " + ret.arg1);
                ret.recycle();
                this.mRequestFinishedReceiver = null;
                this.mCommandLock.release();
                if (requestFinishedReceiver != null) {
                    requestFinishedReceiver.onNewRequestFinished(RequestStatus.FAIL_BAD_PARAMS);
                }

                return false;
            } else if (ret.arg1 != 0) {
                LogAnt.e(TAG, "Cmd " + cmdName + " failed with code " + ret.arg1);
                ret.recycle();
                this.mRequestFinishedReceiver = null;
                this.mCommandLock.release();
                throw new RuntimeException(cmdName + " cmd failed internally");
            } else {
                ret.recycle();
                return true;
            }
        }
    }

    protected boolean sendRequestCommand(String cmdName, int whatCmd, AntPlusCommonPcc.IRequestFinishedReceiver requestFinishedReceiver) {
        return this.sendRequestCommand(cmdName, whatCmd, (Bundle)null, requestFinishedReceiver, (Integer)null);
    }

    protected boolean sendRequestCommand(String cmdName, int whatCmd, Bundle params, AntPlusCommonPcc.IRequestFinishedReceiver requestFinishedReceiver) {
        return this.sendRequestCommand(cmdName, whatCmd, params, requestFinishedReceiver, (Integer)null);
    }

    protected boolean sendRequestCommand(String cmdName, int whatCmd, AntPlusCommonPcc.IRequestFinishedReceiver requestFinishedReceiver, Integer requiredServiceVersion) {
        return this.sendRequestCommand(cmdName, whatCmd, (Bundle)null, requestFinishedReceiver, requiredServiceVersion);
    }

    public interface IRssiReceiver {
        void onRssiData(long var1, EnumSet<EventFlag> var3, int var4);
    }

    public interface IRequestFinishedReceiver {
        void onNewRequestFinished(RequestStatus var1);
    }

    public interface IManufacturerSpecificDataReceiver {
        void onNewManufacturerSpecificData(long var1, EnumSet<EventFlag> var3, byte[] var4);
    }

    public interface IBatteryStatusReceiver {
        void onNewBatteryStatus(long var1, EnumSet<EventFlag> var3, long var4, BigDecimal var6, BatteryStatus var7, int var8, int var9, int var10);
    }

    public interface IProductInformationReceiver {
        void onNewProductInformation(long var1, EnumSet<EventFlag> var3, int var4, int var5, long var6);
    }

    public interface IManufacturerIdentificationReceiver {
        void onNewManufacturerIdentification(long var1, EnumSet<EventFlag> var3, int var4, int var5, int var6);
    }

    public class IpcDefines {
        public static final String MSG_EVENT_ALLDATAEVENTS_PARAM_longESTTIMESTAMP = "long_EstTimestamp";
        public static final String MSG_EVENT_ALLDATAEVENTS_PARAM_longEVENTFLAGS = "long_EventFlags";
        public static final int MSG_EVENT_BASECOMMONPAGES_whatMANUFACTURERIDENTIFICATION = 100;
        public static final String MSG_EVENT_BASECOMMONPAGES_MANUFACTURERIDENTIFICATION_PARAM_intHARDWAREREVISION = "int_hardwareRevision";
        public static final String MSG_EVENT_BASECOMMONPAGES_MANUFACTURERIDENTIFICATION_PARAM_intMANUFACTURERID = "int_manufacturerID";
        public static final String MSG_EVENT_BASECOMMONPAGES_MANUFACTURERIDENTIFICATION_PARAM_intMODELNUMBER = "int_modelNumber";
        public static final int MSG_EVENT_BASECOMMONPAGES_whatPRODUCTINFORMATION = 101;
        public static final String MSG_EVENT_BASECOMMONPAGES_PRODUCTINFORMATION_PARAM_intMAINSOFTWAREREVISION = "int_softwareRevision";
        public static final String MSG_EVENT_BASECOMMONPAGES_PRODUCTINFORMATION_PARAM_intSUPPLEMENTALSOFTWAREREVISION = "int_supplementaryRevision";
        public static final String MSG_EVENT_BASECOMMONPAGES_PRODUCTINFORMATION_PARAM_longSERIALNUMBER = "long_serialNumber";
        public static final int MSG_EVENT_BASECOMMONPAGES_whatBATTERYSTATUS = 102;
        public static final String MSG_EVENT_BASECOMMONPAGES_BATTERYSTATUS_PARAM_longCUMULATIVEOPERATINGTIME = "long_cumulativeOperatingTime";
        public static final String MSG_EVENT_BASECOMMONPAGES_BATTERYSTATUS_PARAM_decimalBATTERYVOLTAGE = "decimal_batteryVoltage";
        public static final String MSG_EVENT_BASECOMMONPAGES_BATTERYSTATUS_PARAM_intBATTERYSTATUSCODE = "int_batteryStatusCode";
        public static final String MSG_EVENT_BASECOMMONPAGES_BATTERYSTATUS_PARAM_intCUMULATIVEOPERATINGTIMERESOLUTION = "int_cumulativeOperatingTimeResolution";
        public static final String MSG_EVENT_BASECOMMONPAGES_BATTERYSTATUS_PARAM_intNUMBEROFBATTERIES = "int_numberOfBatteries";
        public static final String MSG_EVENT_BASECOMMONPAGES_BATTERYSTATUS_PARAM_intBATTERYIDENTIFIER = "int_batteryIdentifier";
        public static final int MSG_EVENT_BASECOMMONPAGES_whatMANUFACTURERSPECIFICDATA = 103;
        public static final String MSG_EVENT_BASECOMMONPAGES_MANUFACTURERSPECIFICDATA_PARAM_arrayByteRAWDATABYTES = "arrayByte_rawDataBytes";
        public static final int MSG_CMD_BASECOMMONPAGES_whatREQUESTCOMMANDBURST = 104;
        public static final String MSG_CMD_BASECOMMONPAGES_REQUESTCOMMANDBURST_PARAM_intREQUESTEDCOMMANDID = "int_requestedCommandId";
        public static final String MSG_CMD_BASECOMMONPAGES_REQUESTCOMMANDBURST_PARAM_arrayByteCOMMANDDATA = "arrayByte_commandData";
        public static final int MSG_CMD_BASECOMMONPAGES_whatREQUESTCOMMANDSTATUS = 105;
        public static final int MSG_CMD_BASECOMMONPAGES_whatREQUESTDATAPAGE = 106;
        public static final String MSG_CMD_BASECOMMONPAGES_REQUESTDATAPAGE_PARAM_intREQUESTEDDATAPAGE = "int_requestedDataPage";
        public static final int MSG_EVENT_BASECOMMONPAGES_whatREQUESTFINISHED = 107;
        public static final String MSG_EVENT_BASECOMMONPAGES_REQUESTFINISHED_PARAM_intREQUESTSTATUS = "int_requestStatus";
        public static final int MSG_EVENT_BASECOMMONPAGES_whatGENERICCOMMAND = 108;
        public static final String MSG_EVENT_BASECOMMONPAGES_GENERICCOMMAND_PARAM_intSERIALNUMBER = "int_serialNumber";
        public static final String MSG_EVENT_BASECOMMONPAGES_GENERICCOMMAND_PARAM_intMANUFACTURERID = "int_manufacturerID";
        public static final String MSG_EVENT_BASECOMMONPAGES_GENERICCOMMAND_PARAM_intSEQUENCENUMBER = "int_sequenceNumber";
        public static final String MSG_EVENT_BASECOMMONPAGES_GENERICCOMMAND_PARAM_intCOMMANDNUMBER = "int_commandNumber";
        public static final int MSG_EVENT_BASECOMMONPAGES_whatRSSI = 109;
        public static final String MSG_EVENT_BASECOMMONPAGES_RSSI_PARAM_intRSSI = "int_rssi";
        public static final int MSG_CMD_BASECOMMONPAGES_whatSENDMANUFACTUERSPECIFICDATAPAGE = 111;
        public static final String MSG_CMD_BASECOMMONPAGES_SENDMANUFACTUERSPECIFICDATAPAGE_PARAM_intPAGENUMBER = "int_ManufacturerSpecificPageNumber";
        public static final String MSG_CMD_BASECOMMONPAGES_SENDMANUFACTUERSPECIFICDATAPAGE_PARAM_arrayBytePAGEDATA = "arrayByte_ManufacturerSpecificPageData";

        public IpcDefines() {
        }
    }

    public static enum CommonDataPage {
        MANUFACTURER_IDENTIFICATION(80),
        PRODUCT_INFORMATION(81),
        BATTERY_STATUS(82),
        COMMAND_STATUS(71),
        UNRECOGNIZED(-1);

        private int intValue;

        private CommonDataPage(int intValue) {
            this.intValue = intValue;
        }

        public int getIntValue() {
            return this.intValue;
        }

        public static AntPlusCommonPcc.CommonDataPage getValueFromInt(int intValue) {
            AntPlusCommonPcc.CommonDataPage[] var1 = values();
            int var2 = var1.length;

            for(int var3 = 0; var3 < var2; ++var3) {
                AntPlusCommonPcc.CommonDataPage source = var1[var3];
                if (source.getIntValue() == intValue) {
                    return source;
                }
            }

            AntPlusCommonPcc.CommonDataPage unrecognized = UNRECOGNIZED;
            unrecognized.intValue = intValue;
            return unrecognized;
        }
    }
}
