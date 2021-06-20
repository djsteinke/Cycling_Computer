package rnfive.djs.ant.internal.compatibility;

import java.math.BigDecimal;
import java.util.EnumSet;

import rnfive.djs.ant.antplus.pcc.defines.EventFlag;
import rnfive.djs.ant.antplus.pcc.AntPlusHeartRatePcc;

public class LegacyHeartRateCompat implements AntPlusHeartRatePcc.IHeartRateDataReceiver {
    private final AntPlusHeartRatePcc.IHeartRateDataReceiver mHeartRateDataReceiver;
    private long mEstTimestamp = -1L;
    private EnumSet<EventFlag> mEventFlags;
    private int mComputedHeartRate;
    private long mHeartBeatCount = -1L;
    private AntPlusHeartRatePcc.DataState mDataState;

    public LegacyHeartRateCompat(AntPlusHeartRatePcc.IHeartRateDataReceiver HeartRateDataReceiver) {
        this.mHeartRateDataReceiver = HeartRateDataReceiver;
    }

    public void onNewHeartRateDataTimestamp(long estTimestamp, EnumSet<EventFlag> eventFlags, BigDecimal heartBeatEventTime) {
        if (estTimestamp == this.mEstTimestamp) {
            this.mHeartRateDataReceiver.onNewHeartRateData(this.mEstTimestamp, this.mEventFlags, this.mComputedHeartRate, this.mHeartBeatCount, heartBeatEventTime, this.mDataState);
        }

    }

    public void onNewHeartRateData(long estTimestamp, EnumSet<EventFlag> eventFlags, int computedHeartRate, long heartBeatCount, BigDecimal heartBeatEventTime, AntPlusHeartRatePcc.DataState dataState) {
        this.mEstTimestamp = estTimestamp;
        this.mEventFlags = eventFlags;
        this.mComputedHeartRate = computedHeartRate;
        this.mHeartBeatCount = heartBeatCount;
        this.mDataState = dataState;
    }

    /** @deprecated */
    @Deprecated
    private interface IHeartRateDataTimestampReceiver {
        /** @deprecated */
        @Deprecated
        void onNewHeartRateDataTimestamp(long var1, EnumSet<EventFlag> var3, BigDecimal var4);
    }
}
