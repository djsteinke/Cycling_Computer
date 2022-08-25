package rnfive.htfu.ant.antplus.pcc;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;

import java.math.BigDecimal;
import java.util.GregorianCalendar;
import java.util.concurrent.Semaphore;

import rnfive.htfu.ant.antplus.pcc.defines.BatteryStatus;
import rnfive.htfu.ant.antplus.pccbase.AntPluginPcc;
import rnfive.htfu.ant.antplus.pccbase.PccReleaseHandle;
import rnfive.htfu.ant.internal.compatibility.LegacyGeocacheCompat.GeocacheDeviceDataCompat_v1;
import rnfive.htfu.ant.utility.log.LogAnt;

public class AntPlusGeocachePcc extends AntPluginPcc {
    private static final String TAG = AntPlusGeocachePcc.class.getSimpleName();
    private AntPlusGeocachePcc.IAvailableDeviceListReceiver mAvailableDeviceListReceiver;
    private AntPlusGeocachePcc.ISimpleProgressUpdateReceiver mSimpleProgressUpdateReceiver;
    private AntPlusGeocachePcc.IProgrammingFinishedReceiver mProgrammingFinishedReceiver;
    private AntPlusGeocachePcc.IDataDownloadFinishedReceiver mDataDownloadFinishedReceiver;
    private AntPlusGeocachePcc.IAuthTokenRequestFinishedReceiver mAuthTokenRequestFinishedReceiver;
    private final Semaphore mCommandLock = new Semaphore(1);

    protected int getRequiredServiceVersionForBind() {
        return 0;
    }

    public static PccReleaseHandle<AntPlusGeocachePcc> requestListAndRequestAccess(Context bindToContext, IPluginAccessResultReceiver<AntPlusGeocachePcc> resultReceiver, IDeviceStateChangeReceiver stateReceiver, AntPlusGeocachePcc.IAvailableDeviceListReceiver availableDeviceListRecevier) {
        Bundle b = new Bundle();
        b.putInt("int_RequestAccessMode", 300);
        AntPlusGeocachePcc possibleRetObj = new AntPlusGeocachePcc();
        possibleRetObj.mAvailableDeviceListReceiver = availableDeviceListRecevier;
        return requestAccess_Helper_Main(bindToContext, b, possibleRetObj, new RequestAccessResultHandler(), resultReceiver, stateReceiver);
    }

    private AntPlusGeocachePcc() {
    }

    protected Intent getServiceBindIntent() {
        Intent it = new Intent();
        it.setComponent(new ComponentName("com.dsi.ant.plugins.antplus", "com.dsi.ant.plugins.antplus.geocache.GeocacheService"));
        return it;
    }

    protected String getPluginPrintableName() {
        return "ANT+ Plugin: Geocache";
    }

    protected void handlePluginEvent(Message eventMsg) {
        Bundle b;
        int statusCode;
        AntPlusGeocachePcc.GeocacheRequestStatus geocacheRequestStatus;
        switch(eventMsg.arg1) {
            case 201:
                if (this.mAvailableDeviceListReceiver != null) {
                    b = eventMsg.getData();
                    int[] deviceIDs = b.getIntArray("arrayInt_deviceIDs");
                    String[] deviceIdentifierStrings = b.getStringArray("arrayString_deviceIdentifierStrings");
                    AntPlusGeocachePcc.DeviceChangingCode changeCode = AntPlusGeocachePcc.DeviceChangingCode.getValueFromInt(b.getInt("int_changeCode"));
                    int changingDeviceID = b.getInt("int_changingDeviceID");
                    this.mAvailableDeviceListReceiver.onNewAvailableDeviceList(deviceIDs, deviceIdentifierStrings, changeCode, changingDeviceID);
                }
                break;
            case 202:
                if (mSimpleProgressUpdateReceiver != null) {
                    b = eventMsg.getData();
                    statusCode = b.getInt("int_workUnitsFinished");
                    int totalUnitsWork = b.getInt("int_totalUnitsWork");
                    mSimpleProgressUpdateReceiver.onNewSimpleProgressUpdate(statusCode, totalUnitsWork);
                }
                break;
            case 203:
                if (mProgrammingFinishedReceiver != null) {
                    mCommandLock.release();
                    b = eventMsg.getData();
                    geocacheRequestStatus = AntPlusGeocachePcc.GeocacheRequestStatus.getValueFromInt(b.getInt("int_statusCode"));
                    mProgrammingFinishedReceiver.onNewProgrammingFinished(geocacheRequestStatus);
                }
                break;
            case 204:
                if (mDataDownloadFinishedReceiver != null) {
                    mCommandLock.release();
                    b = eventMsg.getData();
                    b.setClassLoader(getClass().getClassLoader());
                    geocacheRequestStatus = AntPlusGeocachePcc.GeocacheRequestStatus.getValueFromInt(b.getInt("int_statusCode"));
                    if (geocacheRequestStatus.getIntValue() >= 0) {
                        Bundle downloadedData = b.getBundle("bundle_downloadedData");
                        AntPlusGeocachePcc.GeocacheDeviceData d;
                        if (reportedServiceVersion == 0) {
                            d = GeocacheDeviceDataCompat_v1.readGddFromBundleCompat_v1(downloadedData);
                        } else {
                            d = downloadedData.getParcelable("parcelable_GeocacheDeviceData");
                        }

                        mDataDownloadFinishedReceiver.onNewDataDownloadFinished(geocacheRequestStatus, d);
                    } else {
                        mDataDownloadFinishedReceiver.onNewDataDownloadFinished(geocacheRequestStatus, null);
                    }
                }
                break;
            case 205:
                if (mAuthTokenRequestFinishedReceiver != null) {
                    mCommandLock.release();
                    b = eventMsg.getData();
                    statusCode = b.getInt("int_statusCode");
                    long authToken = b.getLong("long_authToken");
                    mAuthTokenRequestFinishedReceiver.onNewAuthTokenRequestFinished(AntPlusGeocachePcc.GeocacheRequestStatus.getValueFromInt(statusCode), authToken);
                }
                break;
            default:
                LogAnt.d(TAG, "Unrecognized event received: " + eventMsg.arg1);
        }

    }

    public boolean requestCurrentDeviceList() {
        Message cmdMsg = Message.obtain();
        cmdMsg.what = 20002;
        Message ret = sendPluginCommand(cmdMsg);
        if (ret == null) {
            LogAnt.e(TAG, "Cmd requestCurrentDeviceList died in sendPluginCommand()");
            return false;
        } else if (ret.arg1 != 0) {
            LogAnt.e(TAG, "Cmd requestCurrentDeviceList failed with code " + ret.arg1);
            throw new RuntimeException("requestCurrentDeviceList cmd failed internally");
        } else {
            ret.recycle();
            return true;
        }
    }

    public boolean requestDeviceData(int targetDeviceID, boolean updateVisitCount, AntPlusGeocachePcc.IDataDownloadFinishedReceiver resultReceiver, AntPlusGeocachePcc.ISimpleProgressUpdateReceiver progressReceiver) {
        if (!mCommandLock.tryAcquire()) {
            LogAnt.e(TAG, "Cmd failed to start because a local command is still processing.");
            return false;
        } else {
            mDataDownloadFinishedReceiver = resultReceiver;
            mSimpleProgressUpdateReceiver = progressReceiver;
            Message cmdMsg = Message.obtain();
            cmdMsg.what = 20003;
            Bundle params = new Bundle();
            cmdMsg.setData(params);
            params.putInt("int_TARGETDEVICEID", targetDeviceID);
            params.putBoolean("bool_updateVisitCount", updateVisitCount);
            params.putBoolean("bool_subscribeProgressUpdates", progressReceiver != null);
            Message ret = sendPluginCommand(cmdMsg);
            if (ret == null) {
                LogAnt.e(TAG, "Cmd requestDeviceData died in sendPluginCommand()");
                mCommandLock.release();
                return false;
            } else if (ret.arg1 != 0) {
                LogAnt.e(TAG, "Cmd requestDeviceData failed with code " + ret.arg1);
                mCommandLock.release();
                throw new RuntimeException("requestDeviceData cmd failed internally");
            } else {
                ret.recycle();
                return true;
            }
        }
    }

    public boolean requestAuthToken(int targetDeviceID, int nonce, long serialNumber, AntPlusGeocachePcc.IAuthTokenRequestFinishedReceiver resultReceiver, AntPlusGeocachePcc.ISimpleProgressUpdateReceiver progressReceiver) {
        if (!mCommandLock.tryAcquire()) {
            LogAnt.e(TAG, "Cmd failed to start because a local command is still processing.");
            return false;
        } else {
            mAuthTokenRequestFinishedReceiver = resultReceiver;
            mSimpleProgressUpdateReceiver = progressReceiver;
            Message cmdMsg = Message.obtain();
            cmdMsg.what = 20004;
            Bundle params = new Bundle();
            cmdMsg.setData(params);
            params.putInt("int_TARGETDEVICEID", targetDeviceID);
            params.putInt("int_nonce", nonce);
            params.putLong("long_serialNumber", serialNumber);
            params.putBoolean("bool_subscribeProgressUpdates", progressReceiver != null);
            Message ret = sendPluginCommand(cmdMsg);
            if (ret == null) {
                LogAnt.e(TAG, "Cmd requestAuthToken died in sendPluginCommand()");
                mCommandLock.release();
                return false;
            } else if (ret.arg1 != 0) {
                LogAnt.e(TAG, "Cmd requestAuthToken failed with code " + ret.arg1);
                mCommandLock.release();
                throw new RuntimeException("requestAuthToken cmd failed internally");
            } else {
                ret.recycle();
                return true;
            }
        }
    }

    public boolean requestDeviceProgramming(int targetDeviceID, long programmingPIN, boolean clearAllExistingData, AntPlusGeocachePcc.ProgrammableGeocacheDeviceData programmingData, AntPlusGeocachePcc.IProgrammingFinishedReceiver resultReceiver, AntPlusGeocachePcc.ISimpleProgressUpdateReceiver progressReceiver) {
        if (!mCommandLock.tryAcquire()) {
            LogAnt.e(TAG, "Cmd failed to start because a local command is still processing.");
            return false;
        } else {
            mProgrammingFinishedReceiver = resultReceiver;
            mSimpleProgressUpdateReceiver = progressReceiver;
            Message cmdMsg = Message.obtain();
            cmdMsg.what = 20005;
            Bundle params = new Bundle();
            cmdMsg.setData(params);
            params.putInt("int_TARGETDEVICEID", targetDeviceID);
            params.putLong("long_ProgrammingPIN", programmingPIN);
            params.putBoolean("bool_clearAllExistingData", clearAllExistingData);
            if (reportedServiceVersion == 0) {
                Bundle programDataBundle = new Bundle();
                GeocacheDeviceDataCompat_v1.writePgddToBundle(programmingData, programDataBundle);
                params.putBundle("bundle_programmingData", programDataBundle);
            } else {
                params.putParcelable("parcelable_ProgrammableGeocacheDeviceData", programmingData);
            }

            params.putBoolean("bool_subscribeProgressUpdates", progressReceiver != null);
            Message ret = sendPluginCommand(cmdMsg);
            if (ret == null) {
                LogAnt.e(TAG, "Cmd requestDeviceProgramming died in sendPluginCommand()");
                mCommandLock.release();
                return false;
            } else if (ret.arg1 != 0) {
                LogAnt.e(TAG, "Cmd requestDeviceProgramming failed with code " + ret.arg1);
                mCommandLock.release();
                throw new RuntimeException("requestDeviceProgramming cmd failed internally");
            } else {
                ret.recycle();
                return true;
            }
        }
    }

    public interface IAuthTokenRequestFinishedReceiver {
        void onNewAuthTokenRequestFinished(AntPlusGeocachePcc.GeocacheRequestStatus var1, long var2);
    }

    public interface IDataDownloadFinishedReceiver {
        void onNewDataDownloadFinished(AntPlusGeocachePcc.GeocacheRequestStatus var1, AntPlusGeocachePcc.GeocacheDeviceData var2);
    }

    public interface IProgrammingFinishedReceiver {
        void onNewProgrammingFinished(AntPlusGeocachePcc.GeocacheRequestStatus var1);
    }

    public interface ISimpleProgressUpdateReceiver {
        void onNewSimpleProgressUpdate(int var1, int var2);
    }

    public interface IAvailableDeviceListReceiver {
        void onNewAvailableDeviceList(int[] var1, String[] var2, AntPlusGeocachePcc.DeviceChangingCode var3, int var4);
    }

    public static enum GeocacheRequestStatus {
        SUCCESS(0),
        FAIL_CANCELLED(-2),
        UNRECOGNIZED(-3),
        FAIL_OTHER(-10),
        FAIL_ALREADY_BUSY_EXTERNAL(-20),
        FAIL_DEVICE_COMMUNICATION_FAILURE(-40),
        FAIL_DEVICE_TRANSMISSION_LOST(-41),
        FAIL_BAD_PARAMS(-50),
        FAIL_NO_PERMISSION(-60),
        FAIL_NOT_SUPPORTED(-61),
        FAIL_DEVICE_NOT_IN_LIST(10030),
        FAIL_DEVICE_DATA_NOT_DOWNLOADED(10070);

        private int intValue;

        private GeocacheRequestStatus(int intValue) {
            this.intValue = intValue;
        }

        public int getIntValue() {
            return intValue;
        }

        public static AntPlusGeocachePcc.GeocacheRequestStatus getValueFromInt(int intValue) {
            AntPlusGeocachePcc.GeocacheRequestStatus[] var1 = values();
            int var2 = var1.length;

            for(int var3 = 0; var3 < var2; ++var3) {
                AntPlusGeocachePcc.GeocacheRequestStatus status = var1[var3];
                if (status.getIntValue() == intValue) {
                    return status;
                }
            }

            AntPlusGeocachePcc.GeocacheRequestStatus unrecognized = UNRECOGNIZED;
            unrecognized.intValue = intValue;
            return unrecognized;
        }
    }

    public static enum DeviceChangingCode {
        NO_CHANGE(0),
        ADDED_TO_LIST(10),
        REMOVED_FROM_LIST(20),
        PROGRAMMED(100),
        UNRECOGNIZED(-1);

        private int intValue;

        private DeviceChangingCode(int intValue) {
            this.intValue = intValue;
        }

        public int getIntValue() {
            return intValue;
        }

        public static AntPlusGeocachePcc.DeviceChangingCode getValueFromInt(int intValue) {
            AntPlusGeocachePcc.DeviceChangingCode[] var1 = values();
            int var2 = var1.length;

            for(int var3 = 0; var3 < var2; ++var3) {
                AntPlusGeocachePcc.DeviceChangingCode code = var1[var3];
                if (code.getIntValue() == intValue) {
                    return code;
                }
            }

            AntPlusGeocachePcc.DeviceChangingCode unrecognized = UNRECOGNIZED;
            unrecognized.intValue = intValue;
            return unrecognized;
        }
    }

    public static class IpcDefines {
        public static final String PATH_ANTPLUS_GEOCACHEPLUGIN_PKG = "com.dsi.ant.plugins.antplus";
        public static final String PATH_ANTPLUS_GEOCACHEPLUGIN_SERVICE = "com.dsi.ant.plugins.antplus.geocache.GeocacheService";
        public static final int MSG_REQACC_PARAM_MODE_iCONTINUOUSSEARCH = 300;
        public static final int MSG_EVENT_GEOCACHE_whatAVAILABLEDEVICELIST = 201;
        public static final String MSG_EVENT_GEOCACHE_AVAILABLEDEVICELIST_PARAM_arrayIntDEVICEIDS = "arrayInt_deviceIDs";
        public static final String MSG_EVENT_GEOCACHE_AVAILABLEDEVICELIST_PARAM_arrayStringDEVICEIDENTIFIERSTRINGS = "arrayString_deviceIdentifierStrings";
        public static final String MSG_EVENT_GEOCACHE_AVAILABLEDEVICELIST_PARAM_intCHANGECODE = "int_changeCode";
        public static final String MSG_EVENT_GEOCACHE_AVAILABLEDEVICELIST_PARAM_intCHANGINGDEVICEID = "int_changingDeviceID";
        public static final int MSG_EVENT_GEOCACHE_whatSIMPLEPROGRESSUPDATE = 202;
        public static final String MSG_EVENT_GEOCACHE_SIMPLEPROGRESSUPDATE_PARAM_intWORKUNITSFINISHED = "int_workUnitsFinished";
        public static final String MSG_EVENT_GEOCACHE_SIMPLEPROGRESSUPDATE_PARAM_intTOTALUNITSWORK = "int_totalUnitsWork";
        public static final int MSG_EVENT_GEOCACHE_whatPROGRAMMINGFINISHED = 203;
        public static final String MSG_EVENT_GEOCACHE_PROGRAMMINGFINISHED_PARAM_intSTATUSCODE = "int_statusCode";
        public static final int MSG_EVENT_GEOCACHE_whatDATADOWNLOADFINISHED = 204;
        public static final String MSG_EVENT_GEOCACHE_DATADOWNLOADFINISHED_PARAM_intSTATUSCODE = "int_statusCode";
        public static final String MSG_EVENT_GEOCACHE_DATADOWNLOADFINISHED_PARAM_bundleDOWNLOADEDDATA = "bundle_downloadedData";
        public static final int MSG_EVENT_GEOCACHE_whatAUTHTOKENREQUESTFINISHED = 205;
        public static final String MSG_EVENT_GEOCACHE_AUTHTOKENREQUESTFINISHED_PARAM_intSTATUSCODE = "int_statusCode";
        public static final String MSG_EVENT_GEOCACHE_AUTHTOKENREQUESTFINISHED_PARAM_longAUTHTOKEN = "long_authToken";
        public static final int MSG_CMD_GEOCACHE_whatREQUESTCURRENTDEVICELIST = 20002;
        public static final int MSG_CMD_GEOCACHE_whatREQUESTDEVICEDATA = 20003;
        public static final String MSG_CMD_GEOCACHE_REQUESTDEVICEDATA_PARAM_boolUPDATEVISITCOUNT = "bool_updateVisitCount";
        public static final int MSG_CMD_GEOCACHE_whatREQUESTAUTHTOKEN = 20004;
        public static final String MSG_CMD_GEOCACHE_REQUESTAUTHTOKEN_PARAM_intNONCE = "int_nonce";
        public static final String MSG_CMD_GEOCACHE_REQUESTAUTHTOKEN_PARAM_longSERIALNUMBER = "long_serialNumber";
        public static final int MSG_CMD_GEOCACHE_whatREQUESTDEVICEPROGRAMMING = 20005;
        public static final String MSG_CMD_GEOCACHE_REQUESTDEVICEPROGRAMMING_PARAM_longPROGRAMMINGPIN = "long_ProgrammingPIN";
        public static final String MSG_CMD_GEOCACHE_REQUESTDEVICEPROGRAMMING_PARAM_boolCLEARALLEXISTINGDATA = "bool_clearAllExistingData";
        public static final String MSG_CMD_GEOCACHE_REQUESTS_PARAM_boolSUBSCRIBEPROGRESSUPDATES = "bool_subscribeProgressUpdates";
        public static final String MSG_CMD_GEOCACHE_REQUESTS_PARAM_intTARGETDEVICEID = "int_TARGETDEVICEID";

        public IpcDefines() {
        }
    }

    public static class ProgrammableGeocacheDeviceData implements Parcelable {
        public static final String KEY_DEFAULT_PROGRAMMABLEGEOCACHEDEVICEDATAKEY = "parcelable_ProgrammableGeocacheDeviceData";
        public String identificationString = null;
        public Long PIN = null;
        public BigDecimal latitude = null;
        public BigDecimal longitude = null;
        public String hintString = null;
        public GregorianCalendar lastVisitTimestamp = null;
        public Integer numberOfVisits = null;
        public static final Creator<AntPlusGeocachePcc.ProgrammableGeocacheDeviceData> CREATOR = new Creator<AntPlusGeocachePcc.ProgrammableGeocacheDeviceData>() {
            public AntPlusGeocachePcc.ProgrammableGeocacheDeviceData createFromParcel(Parcel in) {
                return new AntPlusGeocachePcc.ProgrammableGeocacheDeviceData(in);
            }

            public AntPlusGeocachePcc.ProgrammableGeocacheDeviceData[] newArray(int size) {
                return new AntPlusGeocachePcc.ProgrammableGeocacheDeviceData[size];
            }
        };

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            int ipcVersionNumber = 1;
            dest.writeInt(ipcVersionNumber);
            dest.writeString(identificationString);
            dest.writeValue(PIN);
            dest.writeValue(latitude);
            dest.writeValue(longitude);
            dest.writeString(hintString);
            dest.writeValue(lastVisitTimestamp);
            dest.writeValue(numberOfVisits);
        }

        public ProgrammableGeocacheDeviceData(Parcel in) {
            int incomingVersion = in.readInt();
            if (incomingVersion != 1) {
                LogAnt.i(AntPlusGeocachePcc.TAG, "Decoding version " + incomingVersion + " ProgrammableGeocacheDeviceData parcel with version 1 parser.");
            }

            identificationString = in.readString();
            PIN = (Long)in.readValue((ClassLoader) null);
            latitude = (BigDecimal)in.readValue((ClassLoader)null);
            longitude = (BigDecimal)in.readValue((ClassLoader)null);
            hintString = in.readString();
            lastVisitTimestamp = (GregorianCalendar)in.readValue((ClassLoader)null);
            numberOfVisits = (Integer)in.readValue((ClassLoader)null);
        }

        public ProgrammableGeocacheDeviceData() {
        }
    }

    public static class GeocacheDeviceData implements Parcelable {
        public static final String KEY_DEFAULT_GEOCACHEDEVICEDATAKEY = "parcelable_GeocacheDeviceData";
        private final int ipcVersionNumber;
        public int deviceId;
        public int hardwareRevision;
        public int manufacturerID;
        public int modelNumber;
        public int softwareRevision;
        public long serialNumber;
        public long cumulativeOperatingTime;
        public BigDecimal batteryVoltage;
        public BatteryStatus batteryStatus;
        public int cumulativeOperatingTimeResolution;
        public AntPlusGeocachePcc.ProgrammableGeocacheDeviceData programmableData;
        public static final Creator<AntPlusGeocachePcc.GeocacheDeviceData> CREATOR = new Creator<AntPlusGeocachePcc.GeocacheDeviceData>() {
            public AntPlusGeocachePcc.GeocacheDeviceData createFromParcel(Parcel in) {
                return new AntPlusGeocachePcc.GeocacheDeviceData(in);
            }

            public AntPlusGeocachePcc.GeocacheDeviceData[] newArray(int size) {
                return new AntPlusGeocachePcc.GeocacheDeviceData[size];
            }
        };

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(ipcVersionNumber);
            dest.writeInt(deviceId);
            dest.writeInt(hardwareRevision);
            dest.writeInt(manufacturerID);
            dest.writeInt(modelNumber);
            dest.writeInt(softwareRevision);
            dest.writeLong(serialNumber);
            dest.writeLong(cumulativeOperatingTime);
            dest.writeValue(batteryVoltage);
            dest.writeInt(batteryStatus.getIntValue());
            dest.writeInt(cumulativeOperatingTimeResolution);
            Bundle b = new Bundle();
            b.putParcelable("parcelable_ProgrammableGeocacheDeviceData", programmableData);
            dest.writeBundle(b);
        }

        public GeocacheDeviceData(Parcel in) {
            batteryStatus = BatteryStatus.INVALID;
            programmableData = new AntPlusGeocachePcc.ProgrammableGeocacheDeviceData();
            ipcVersionNumber = 1;
            int incomingVersion = in.readInt();
            if (incomingVersion != 1) {
                LogAnt.i(AntPlusGeocachePcc.TAG, "Decoding version " + incomingVersion + " GeocacheDeviceData parcel with version 1 parser.");
            }

            deviceId = in.readInt();
            hardwareRevision = in.readInt();
            manufacturerID = in.readInt();
            modelNumber = in.readInt();
            softwareRevision = in.readInt();
            serialNumber = in.readLong();
            cumulativeOperatingTime = in.readLong();
            batteryVoltage = (BigDecimal)in.readValue((ClassLoader)null);
            batteryStatus = BatteryStatus.getValueFromInt(in.readInt());
            cumulativeOperatingTimeResolution = in.readInt();
            Bundle b = in.readBundle();
            b.setClassLoader(getClass().getClassLoader());
            programmableData = (AntPlusGeocachePcc.ProgrammableGeocacheDeviceData)b.getParcelable("parcelable_ProgrammableGeocacheDeviceData");
        }

        public GeocacheDeviceData() {
            batteryStatus = BatteryStatus.INVALID;
            programmableData = new AntPlusGeocachePcc.ProgrammableGeocacheDeviceData();
            ipcVersionNumber = 1;
        }

        public GeocacheDeviceData(int ipcVersionNumber) {
            this.batteryStatus = BatteryStatus.INVALID;
            this.programmableData = new AntPlusGeocachePcc.ProgrammableGeocacheDeviceData();
            this.ipcVersionNumber = ipcVersionNumber;
        }
    }
}
