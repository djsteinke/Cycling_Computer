package rnfive.htfu.ant.internal.compatibility;

import android.os.Bundle;

import java.math.BigDecimal;
import java.util.GregorianCalendar;

import rnfive.htfu.ant.antplus.pcc.defines.BatteryStatus;
import rnfive.htfu.ant.antplus.pcc.AntPlusGeocachePcc;

public class LegacyGeocacheCompat {
    public LegacyGeocacheCompat() {
    }

    public static class GeocacheDeviceDataCompat_v1 {
        public GeocacheDeviceDataCompat_v1() {
        }

        public static AntPlusGeocachePcc.GeocacheDeviceData readGddFromBundleCompat_v1(Bundle b) {
            AntPlusGeocachePcc.GeocacheDeviceData d = new AntPlusGeocachePcc.GeocacheDeviceData(0);
            d.programmableData = readPgddFromBundle(b);
            d.deviceId = b.getInt("int_deviceID");
            d.hardwareRevision = b.getInt("int_hardwareRevision");
            d.manufacturerID = b.getInt("int_manufacturerID");
            d.modelNumber = b.getInt("int_modelNumber");
            d.softwareRevision = b.getInt("int_softwareRevision");
            d.serialNumber = b.getLong("long_serialNumber");
            d.cumulativeOperatingTime = b.getLong("long_cumulativeOperatingTime");
            d.batteryVoltage = (BigDecimal)b.getSerializable("decimal_batteryVoltage");
            d.batteryStatus = BatteryStatus.getValueFromInt(b.getInt("int_batteryStatusCode"));
            d.cumulativeOperatingTimeResolution = b.getInt("int_cumulativeOperatingTimeResolution");
            return d;
        }

        public static void writeGddToBundleCompat_v1(AntPlusGeocachePcc.GeocacheDeviceData d, Bundle b) {
            writePgddToBundle(d.programmableData, b);
            b.putInt("int_deviceID", d.deviceId);
            b.putInt("int_hardwareRevision", d.hardwareRevision);
            b.putInt("int_manufacturerID", d.manufacturerID);
            b.putInt("int_modelNumber", d.modelNumber);
            b.putInt("int_softwareRevision", d.softwareRevision);
            b.putLong("long_serialNumber", d.serialNumber);
            b.putLong("long_cumulativeOperatingTime", d.cumulativeOperatingTime);
            b.putSerializable("decimal_batteryVoltage", d.batteryVoltage);
            b.putInt("int_batteryStatusCode", d.batteryStatus.getIntValue());
            b.putInt("int_cumulativeOperatingTimeResolution", d.cumulativeOperatingTimeResolution);
        }

        public static AntPlusGeocachePcc.ProgrammableGeocacheDeviceData readPgddFromBundle(Bundle b) {
            AntPlusGeocachePcc.ProgrammableGeocacheDeviceData programmableData = new AntPlusGeocachePcc.ProgrammableGeocacheDeviceData();
            programmableData.identificationString = b.getString("string_identificationString");
            programmableData.PIN = b.getLong("long_PIN");
            if (programmableData.PIN == -1L) {
                programmableData.PIN = null;
            }

            programmableData.latitude = (BigDecimal)b.getSerializable("bigDecimal_latitude");
            programmableData.longitude = (BigDecimal)b.getSerializable("bigDecimal_longitude");
            programmableData.hintString = b.getString("string_hintString");
            programmableData.lastVisitTimestamp = (GregorianCalendar)b.getSerializable("gregorianCalendar_lastVisitTimestamp");
            programmableData.numberOfVisits = b.getInt("int_numberOfVisits");
            if (programmableData.numberOfVisits == -1) {
                programmableData.numberOfVisits = null;
            }

            return programmableData;
        }

        public static void writePgddToBundle(AntPlusGeocachePcc.ProgrammableGeocacheDeviceData p, Bundle b) {
            if (p.identificationString != null) {
                b.putString("string_identificationString", p.identificationString);
            }

            b.putLong("long_PIN", p.PIN == null ? -1L : p.PIN);
            if (p.latitude != null) {
                b.putSerializable("bigDecimal_latitude", p.latitude);
            }

            if (p.longitude != null) {
                b.putSerializable("bigDecimal_longitude", p.longitude);
            }

            if (p.hintString != null) {
                b.putString("string_hintString", p.hintString);
            }

            if (p.lastVisitTimestamp != null) {
                b.putSerializable("gregorianCalendar_lastVisitTimestamp", p.lastVisitTimestamp);
            }

            b.putInt("int_numberOfVisits", p.numberOfVisits == null ? -1 : p.numberOfVisits);
        }

        public static class IpcDefinesCompat_v1 {
            public static final String MSG_CMD_GEOCACHE_REQUESTDEVICEPROGRAMMING_PARAM_bundlePROGRAMMINGDATA = "bundle_programmingData";
            public static final String intDEVICEID = "int_deviceID";
            public static final String stringIDENTIFICATIONSTRING = "string_identificationString";
            public static final String longPIN = "long_PIN";
            public static final String decimalLATITUDE = "bigDecimal_latitude";
            public static final String decimalLONGITUDE = "bigDecimal_longitude";
            public static final String stringHINTSTRING = "string_hintString";
            public static final String gregorianCalendarLASTVISITTIMESTAMP = "gregorianCalendar_lastVisitTimestamp";
            public static final String intNUMBEROFVISITS = "int_numberOfVisits";
            public static final String intHARDWAREREVISION = "int_hardwareRevision";
            public static final String intMANUFACTURERID = "int_manufacturerID";
            public static final String intMODELNUMBER = "int_modelNumber";
            public static final String intSOFTWAREREVISION = "int_softwareRevision";
            public static final String longSERIALNUMBER = "long_serialNumber";
            public static final String longCUMULATIVEOPERATINGTIME = "long_cumulativeOperatingTime";
            public static final String decimalBATTERYVOLTAGE = "decimal_batteryVoltage";
            public static final String intBATTERYSTATUSCODE = "int_batteryStatusCode";
            public static final String intCUMULATIVEOPERATINGTIMERESOLUTION = "int_cumulativeOperatingTimeResolution";

            public IpcDefinesCompat_v1() {
            }
        }
    }
}
