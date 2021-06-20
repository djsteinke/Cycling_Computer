package rnfive.djs.ant.internal.pluginsipc;

public class AntPluginMsgDefines {
    public static final int MSG_REQACC_PARAM_whatREQACC = 0;
    public static final int MSG_REQACC_PARAM_whatSETDEBUG = -1;
    public static final int MSG_REQACC_PARAM_whatCLOSECONNECTION = 1;
    public static final String MSG_REQACC_PARAM_intMODE = "int_RequestAccessMode";
    public static final int MSG_REQACC_PARAM_MODE_iACTIVITYSEARCH = 1;
    public static final int MSG_REQACC_PARAM_MODE_iASYNCSEARCHCONTROLLER = 2;
    public static final int MSG_REQACC_PARAM_MODE_iASYNCSEARCHBYANTDEVID = 3;
    public static final String MSG_REQACC_PARAM_intRSSIMODE = "int_RssiMode";
    public static final int MSG_REQACC_PARAM_RSSI_MODE_DONT_CARE = 0;
    public static final int MSG_REQACC_PARAM_RSSI_MODE_PREFERRED = 1;
    public static final String MSG_REQACC_PARAM_strAPPNAMEPKG = "str_ApplicationNamePackage";
    public static final String MSG_REQACC_PARAM_strAPPNAMETITLE = "str_ApplicationNameTitle";
    public static final String MSG_REQACC_PARAM_msgrPLUGINMSGRECEIVER = "msgr_PluginMsgHandler";
    public static final String MSG_REQACC_PARAM_msgrRESULTRECEIVER = "msgr_ReqAccResultReceiver";
    public static final String MSG_REQACC_PARAM_intPLUGINLIBVERSION = "int_PluginLibVersion";
    public static final String MSG_REQACC_PARAM_stringPLUGINLIBVERSION = "string_PluginLibVersion";
    public static final String MSG_REQACC_SEARCHCOMMON_PARAM_intPROXIMITYBIN = "int_ProximityBin";
    public static final String MSG_REQACC_ASYNCSEARCHBYDEVNUMBER_PARAM_intANTDEVICENUMBER = "int_AntDeviceID";
    public static final String MSG_REQACC_ACTIVITYSEARCH_PARAM_bFORCESEARCHALL = "b_ForceManualSelect";
    public static final int MSG_REQACC_RESULT_whatSUCCESS = 0;
    public static final int MSG_REQACC_RESULT_whatREQACTIVITYLAUNCH = 1;
    public static final int MSG_REQACC_RESULT_whatSCANRESULT = 2;
    public static final int MSG_REQACC_RESULT_whatUSERCANCELLED = -2;
    public static final int MSG_REQACC_RESULT_whatCHANNELNOTAVAILABLE = -3;
    public static final int MSG_REQACC_RESULT_whatOTHERFAILURE = -4;
    public static final int MSG_REQACC_RESULT_whatDEPENDENCYNOTINSTALLED = -5;
    public static final int MSG_REQACC_RESULT_whatDEVICEALREADYINUSE = -6;
    public static final int MSG_REQACC_RESULT_whatSEARCHTIMEOUT = -7;
    public static final int MSG_REQACC_RESULT_whatALREADYSUBSCRIBED = -8;
    public static final int MSG_REQACC_RESULT_whatBADPARAMS = -9;
    public static final int MSG_REQACC_RESULT_whatADAPTERNOTAVAILABLE = -10;
    public static final int MSG_REQACC_RESULT_whatSEARCHFAILURE = -100;
    public static final int MSG_REQACC_RESULT_whatUNRECOGNIZEDMSG = -99999999;
    public static final String MSG_REQACC_RESULT_intSERVICEVERSION = "int_ServiceVersion";
    public static final String MSG_REQACC_RESULT_msgrPCCMSGRECEIVER = "msgr_PluginComm";
    public static final String MSG_REQACC_RESULT_uuidACCESSTOKEN = "uuid_AccessToken";
    public static final String MSG_REQACC_RESULT_strCONNDEVICENAME = "str_DeviceName";
    public static final String MSG_REQACC_RESULT_intentACTIVITYTOLAUNCH = "intent_ActivityToLaunch";
    public static final String MSG_REQACC_RESULT_intINITIALDEVICESTATECODE = "int_InitialDeviceStateCode";
    public static final String MSG_REQACC_RESULT_intANTDEVICENUMBER = "int_AntDeviceID";
    public static final String MSG_REQACC_RESULT_boolRSSISUPPORT = "bool_RssiSupport";
    public static final String MSG_REQACC_RESULT_DEPENDENCYNOTINSTALLED_PARAM_stringDEPENDENCYPKGNAME = "string_DependencyPackageName";
    public static final String MSG_REQACC_RESULT_DEPENDENCYNOTINSTALLED_PARAM_stringDEPENDENCYNAME = "string_DependencyName";
    public static final int MSG_CMD_RESULT_whatSUCCESS = 0;
    public static final int MSG_CMD_RESULT_whatFAIL_BADPARAMS = -3;
    public static final int MSG_CMD_RESULT_whatFAIL_NOPERMISSION = -4;
    public static final int MSG_CMD_RESULT_whatFAIL_UNRECOGNIZED = -99999999;
    public static final int MSG_CMD_whatSUBSCRIBEPLUGINEVENT = 10000;
    public static final int MSG_CMD_whatUNSUBSCRIBEPLUGINEVENT = 10001;
    public static final int MSG_CMD_whatRELEASETOKEN = 10002;
    public static final int MSG_CMD_whatCONNECTTOASYNCRESULT = 10100;
    public static final int MSG_CMD_whatCLOSEASYNCSCAN = 10101;
    public static final String MSG_CMD_PARAM_uuidACCESSTOKEN = "uuid_AccessToken";
    public static final int MSG_EVENT_whatPLUGINEVENT = 1;
    public static final int MSG_EVENT_whatDEVICESTATECHANGE = 3;
    public static final int MSG_ARS_SEARCHANDDB_RESULT_whatPROGRESS_SCAN_FOUND_DEVICE = 9;
    public static final int MSG_ARS_SEARCHANDDB_RESULT_whatSUCCESS_DEVICE_CONNECTED = 10;
    public static final int MSG_ARS_SEARCHANDDB_RESULT_whatFAIL_SEARCH_TIMEOUT = -4;
    public static final int MSG_ARS_SEARCHANDDB_RESULT_whatFAIL_INTERRUPTED = -22;
    public static final int MSG_ARS_SEARCHANDDB_RESULT_whatFAIL_CONTROLSMODEMISMATCH = -26;
    public static final String MSG_ARS_SEARCHANDDB_RESULT_PROGRESS_KEY_msgrCONTROLLER = "msgr_Controller";
    public static final int MSG_ARS_SEARCHANDDB_CONTROLLER_CMD_whatCANCELSEARCH = 6;
    public static final String PATH_ARS_PLUGINMGR = "com.dsi.ant.plugins.antplus";
    public static final String PATH_ARS_PLUGINMGR_SETTINGS_ACTIVITY = "com.dsi.ant.plugins.antplus.utility.db.Activity_PluginMgrDashboard";
    public static final String PATH_ARS_PLUGINMGR_REQDEV_SEARCHALL = "com.dsi.ant.plugins.antplus.utility.search.Activity_SearchAllDevices";
    public static final String PATH_ARS_PLUGINMGR_REQDEV_SEARCHPREFERRED = "com.dsi.ant.plugins.antplus.utility.search.Dialog_SearchPreferredDevice";
    public static final String MSG_ARS_REQDEV_PARAMPKGNAME = "com.dsi.ant.plugins.antplus.pcc.plugindata";
    public static final String MSG_ARS_REQDEV_PARAM_networkNETKEY = "predefinednetwork_NetKey";
    public static final String MSG_ARS_REQDEV_PARAM_antchannelCHANNEL = "antchannel_Channel";
    public static final String MSG_ARS_REQDEV_PARAM_strPLUGINNAME = "str_PluginName";
    public static final String MSG_ARS_REQDEV_PARAM_intCHANDEVNUMBER = "int_ChannelDeviceId";
    public static final String MSG_ARS_REQDEV_PARAM_intDEVTYPE = "int_DevType";
    public static final String MSG_ARS_REQDEV_PARAM_intTRANSTYPE = "int_TransType";
    public static final String MSG_ARS_REQDEV_PARAM_intPERIOD = "int_Period";
    public static final String MSG_ARS_REQDEV_PARAM_intRFFREQ = "int_RfFreq";
    public static final String MSG_ARS_REQDEV_PARAM_msgrRESULTRECEIVER = "msgr_SearchResultReceiver";
    public static final int MSG_ARS_REQDEV_RESULT_SUCCESS_arg1CONNECTEDTONEWDEVICE = 0;
    public static final int MSG_ARS_REQDEV_RESULT_SUCCESS_arg1SELECTEDEXISTINGDEVICE = 1;
    public static final int MSG_ARS_REQDEV_RESULT_SUCCESS_arg1NEWSCANRESULT = 2;
    public static final String MSG_ARS_REQDEV_RESULT_strCONNDEVICENAME = "str_SelectedDeviceName";

    public AntPluginMsgDefines() {
    }

    public class DeviceStateCodes {
        public static final int DEAD = -100;
        public static final int CLOSED = 1;
        public static final int SEARCHING = 2;
        public static final int TRACKING = 3;
        public static final int PROCESSING_REQUEST = 300;

        public DeviceStateCodes() {
        }
    }
}
