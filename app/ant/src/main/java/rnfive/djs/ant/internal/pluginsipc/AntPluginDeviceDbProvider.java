package rnfive.djs.ant.internal.pluginsipc;

import android.os.Parcel;
import android.os.Parcelable;

import rnfive.djs.ant.utility.log.LogAnt;

public class AntPluginDeviceDbProvider {
    private static final String TAG = AntPluginDeviceDbProvider.class.getSimpleName();

    public AntPluginDeviceDbProvider() {
    }

    public static class AntPluginDeviceDbException extends Exception {
        private static final long serialVersionUID = 8657725579688499890L;
        public int deviceDbQueryResult;

        public AntPluginDeviceDbException(String s, int deviceDbQueryResult) {
            super(s);
            this.deviceDbQueryResult = deviceDbQueryResult;
        }

        public class DeviceDbQueryResult {
            public static final int SUCCESS = 0;
            public static final int FAIL_PROCESSING = -1;
            public static final int FAIL_FUNCTIONALITY_NOT_AVAILABLE_FOR_INSTALLED_PLUGIN_VERSION = -2;
            public static final int FAIL_PLUGINS_NOT_INSTALLED = -3;
            public static final int FAIL_OTHER = -4;

            public DeviceDbQueryResult() {
            }
        }
    }

    public static class DeviceDbDeviceInfo implements Parcelable {
        public static final String KEY_DEFAULT_DEVICEDBKEY = "parcelable_DeviceDbInfo";
        private int ipcVersionNumber;
        public Long device_dbId;
        public Long plugin_dbId;
        public Integer antDeviceNumber;
        public String visibleName;
        public Boolean isPreferredDevice;
        public static final Creator<AntPluginDeviceDbProvider.DeviceDbDeviceInfo> CREATOR = new Creator<AntPluginDeviceDbProvider.DeviceDbDeviceInfo>() {
            public AntPluginDeviceDbProvider.DeviceDbDeviceInfo createFromParcel(Parcel in) {
                return new AntPluginDeviceDbProvider.DeviceDbDeviceInfo(in);
            }

            public AntPluginDeviceDbProvider.DeviceDbDeviceInfo[] newArray(int size) {
                return new AntPluginDeviceDbProvider.DeviceDbDeviceInfo[size];
            }
        };

        public DeviceDbDeviceInfo() {
            this.ipcVersionNumber = 1;
            this.isPreferredDevice = false;
        }

        public DeviceDbDeviceInfo(int ipcVersionNumber) {
            this.ipcVersionNumber = ipcVersionNumber;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.ipcVersionNumber);
            dest.writeValue(this.device_dbId);
            dest.writeValue(this.plugin_dbId);
            dest.writeValue(this.antDeviceNumber);
            dest.writeValue(this.visibleName);
            dest.writeValue(this.isPreferredDevice);
        }

        public DeviceDbDeviceInfo(Parcel in) {
            this.ipcVersionNumber = 1;
            int incomingVersion = in.readInt();
            if (incomingVersion != 1) {
                LogAnt.i(AntPluginDeviceDbProvider.TAG, "Decoding version " + incomingVersion + " AsyncScanResultDeviceInfo parcel with version 1 parser.");
            }

            this.device_dbId = (Long)in.readValue((ClassLoader)null);
            this.plugin_dbId = (Long)in.readValue((ClassLoader)null);
            this.antDeviceNumber = (Integer)in.readValue((ClassLoader)null);
            this.visibleName = (String)in.readValue((ClassLoader)null);
            this.isPreferredDevice = (Boolean)in.readValue((ClassLoader)null);
        }
    }

    public static class IpcDefines {
        public static final String PATH_ARS_DEVICEDBSRVC_PKG = "com.dsi.ant.plugins.antplus";
        public static final String PATH_ARS_DEVICEDBSRVC_SERVICE = "com.dsi.ant.plugins.antplus.utility.db.Service_DeviceDbProvider";
        public static final String MSG_ARS_DEVICEDBSRVC_PARAMPKGNAME = "com.dsi.ant.plugins.antplus.utility.db.devicedbrequest";
        public static final String MSG_ARS_DEVICEDBSRVC_REQ_PARAM_KEY_msgrRESULTRECEIVER = "msgr_ResultReceiver";
        public static final String MSG_ARS_DEVICEDBSRVC_REQ_PARAM_KEY_intREQUESTTYPE = "int_RequestType";
        public static final int MSG_ARS_DEVICEDBSRVC_REQ_TYPE_GETDEVICEINFO = 18;
        public static final int MSG_ARS_DEVICEDBSRVC_REQ_TYPE_ADDDEVICE = 20;
        public static final int MSG_ARS_DEVICEDBSRVC_REQ_TYPE_CHANGEDEVICEINFO = 22;
        public static final String MSG_ARS_DEVICEDBSRVC_PARAM_intANTDEVNUMBER = "int_AntDeviceNumber";
        public static final String MSG_ARS_DEVICEDBSRVC_PARAM_stringPLUGINNAME = "string_PluginName";
        public static final String MSG_ARS_DEVICEDBSRVC_PARAM_stringDISPLAYNAME = "string_DISPLAYNAME";
        public static final String MSG_ARS_DEVICEDBSRVC_PARAM_booleanISUSERPREFFEREDDEVICE = "boolean_IsUserPreferredDevice";
        public static final String MSG_ARS_DEVICEDBSRVC_PARAM_bundlePLUGINDEVICEPARAMS = "bundle_PluginDeviceParams";
        public static final String MSG_ARS_DEVICEDBSRVC_REQ_RESULT_intRESULTCODE = "int_ResultCode";

        public IpcDefines() {
        }
    }
}
