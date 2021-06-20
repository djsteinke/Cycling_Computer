package rnfive.djs.ant.antplus.pcc;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;

import java.math.BigDecimal;
import java.util.EnumSet;

import rnfive.djs.ant.antplus.pcc.defines.EventFlag;
import rnfive.djs.ant.antplus.pcc.defines.RequestStatus;
import rnfive.djs.ant.antplus.pccbase.AntPlusCommonPcc;
import rnfive.djs.ant.antplus.pccbase.AsyncScanController;
import rnfive.djs.ant.antplus.pccbase.PccReleaseHandle;
import rnfive.djs.ant.utility.log.LogAnt;

public class AntPlusBikePowerPcc extends AntPlusCommonPcc {
    static final String TAG = AntPlusBikePowerPcc.class.getSimpleName();
    private AntPlusBikePowerPcc.IRawPowerOnlyDataReceiver mRawPowerOnlyDataReceiver;
    private AntPlusBikePowerPcc.IPedalPowerBalanceReceiver mPedalPowerBalanceReceiver;
    private AntPlusBikePowerPcc.IInstantaneousCadenceReceiver mInstantaneousCadenceReceiver;
    private AntPlusBikePowerPcc.IRawWheelTorqueDataReceiver mRawWheelTorqueDataReceiver;
    private AntPlusBikePowerPcc.IRawCrankTorqueDataReceiver mRawCrankTorqueDataReceiver;
    private AntPlusBikePowerPcc.ITorqueEffectivenessReceiver mTorqueEffectivenessReceiver;
    private AntPlusBikePowerPcc.IPedalSmoothnessReceiver mPedalSmoothnessReceiver;
    private AntPlusBikePowerPcc.IRawCtfDataReceiver mRawCtfDataReceiver;
    private AntPlusBikePowerPcc.ICalibrationMessageReceiver mCalibrationMessageReceiver;
    private AntPlusBikePowerPcc.ICalibrationMessageReceiver mRequestCalibrationMessageReceiver;
    private AntPlusBikePowerPcc.IAutoZeroStatusReceiver mAutoZeroStatusReceiver;
    private AntPlusBikePowerPcc.IAutoZeroStatusReceiver mRequestAutoZeroStatusReceiver;
    private AntPlusBikePowerPcc.ICalculatedPowerReceiver mCalculatedPowerReceiver;
    private AntPlusBikePowerPcc.ICalculatedTorqueReceiver mCalculatedTorqueReceiver;
    private AntPlusBikePowerPcc.ICalculatedCrankCadenceReceiver mCalculatedCrankCadenceReceiver;
    private AntPlusBikePowerPcc.CalculatedWheelSpeedReceiver mCalculatedWheelSpeedReceiver;
    private AntPlusBikePowerPcc.CalculatedWheelDistanceReceiver mCalculatedWheelDistanceReceiver;
    private AntPlusBikePowerPcc.IMeasurementOutputDataReceiver mMeasurementOutputDataReceiver;
    private AntPlusBikePowerPcc.IMeasurementOutputDataReceiver mRequestMeasurementOutputDataReceiver;
    private AntPlusBikePowerPcc.ICrankParametersReceiver mCrankParametersReceiver;
    private AntPlusBikePowerPcc.ICrankParametersReceiver mRequestCrankParametersReceiver;
    private final Handler pccHandler = new Handler();
    private final Runnable unsubscribeRequestCalibrationMessageReceiver = () -> subscribeRequestCalibrationMessageEvent(null);
    private final Runnable unsubscribeRequestAutoZeroStatusReceiver = () -> subscribeRequestAutoZeroStatusEvent(null);
    private final Runnable unsubscribeRequestMeasurementOutputDataReceiver = () -> subscribeRequestMeasurementOutputDataEvent(null);
    private final Runnable unsubscribeRequestCrankParametersReceiver = () -> subscribeRequestCrankParametersEvent(null);

    protected final int getRequiredServiceVersionForBind() {
        return 10800;
    }

    public static PccReleaseHandle<AntPlusBikePowerPcc> requestAccess(Activity userActivity, Context bindToContext, boolean skipPreferredSearch, int searchProximityThreshold, IPluginAccessResultReceiver<AntPlusBikePowerPcc> resultReceiver, IDeviceStateChangeReceiver stateReceiver) {
        AntPlusBikePowerPcc potentialRetObj = new AntPlusBikePowerPcc();
        return requestAccess_Helper_SearchActivity(userActivity, bindToContext, skipPreferredSearch, searchProximityThreshold, potentialRetObj, resultReceiver, stateReceiver);
    }

    public static PccReleaseHandle<AntPlusBikePowerPcc> requestAccess(Activity userActivity, Context bindToContext, IPluginAccessResultReceiver<AntPlusBikePowerPcc> resultReceiver, IDeviceStateChangeReceiver stateReceiver) {
        return requestAccess(userActivity, bindToContext, false, -1, resultReceiver, stateReceiver);
    }

    public static PccReleaseHandle<AntPlusBikePowerPcc> requestAccess(Context bindToContext, int antDeviceNumber, int searchProximityThreshold, IPluginAccessResultReceiver<AntPlusBikePowerPcc> resultReceiver, IDeviceStateChangeReceiver stateReceiver) {
        AntPlusBikePowerPcc potentialRetObj = new AntPlusBikePowerPcc();
        return requestAccess_Helper_AsyncSearchByDevNumber(bindToContext, antDeviceNumber, searchProximityThreshold, potentialRetObj, resultReceiver, stateReceiver);
    }

    public static AsyncScanController<AntPlusBikePowerPcc> requestAsyncScanController(Context bindToContext, int searchProximityThreshold, AsyncScanController.IAsyncScanResultReceiver scanResultReceiver) {
        AntPlusBikePowerPcc potentialRetObj = new AntPlusBikePowerPcc();
        return requestAccess_Helper_AsyncScanController(bindToContext, searchProximityThreshold, potentialRetObj, scanResultReceiver);
    }

    private AntPlusBikePowerPcc() {
    }

    protected Intent getServiceBindIntent() {
        Intent it = new Intent();
        it.setComponent(new ComponentName("com.dsi.ant.plugins.antplus", "com.dsi.ant.plugins.antplus.bikepower.BikePowerService"));
        return it;
    }

    protected String getPluginPrintableName() {
        return "ANT+ Plugin: Bike Power";
    }

    protected void handlePluginEvent(Message eventMsg) {
        EnumSet<EventFlag> eventFlags;
        int dataType;
        BigDecimal timeStamp;
        BigDecimal measurementValue;
        BigDecimal accumulatedCrankTorque;
        Bundle b;
        long estTimestamp;
        AntPlusBikePowerPcc.DataSource dataSource;
        BigDecimal calculatedWheelDistance;
        long ctfUpdateEventCount;
        long accumulatedCrankTicks;
        BigDecimal rightPedalSmoothness;
        switch(eventMsg.arg1) {
            case 201:
                if (mRawPowerOnlyDataReceiver != null) {
                    b = eventMsg.getData();
                    estTimestamp = b.getLong("long_EstTimestamp");
                    eventFlags = EventFlag.getEventFlagsFromLong(b.getLong("long_EventFlags"));
                    ctfUpdateEventCount = b.getLong("long_powerOnlyUpdateEventCount");
                    int instantaneousPower = b.getInt("int_instantaneousPower");
                    long accumulatedPower = b.getLong("long_accumulatedPower");
                    mRawPowerOnlyDataReceiver.onNewRawPowerOnlyData(estTimestamp, eventFlags, ctfUpdateEventCount, instantaneousPower, accumulatedPower);
                }
                break;
            case 202:
                if (mPedalPowerBalanceReceiver != null) {
                    b = eventMsg.getData();
                    estTimestamp = b.getLong("long_EstTimestamp");
                    eventFlags = EventFlag.getEventFlagsFromLong(b.getLong("long_EventFlags"));
                    boolean rightPedalIndicator = b.getBoolean("bool_rightPedalIndicator");
                    dataType = b.getInt("int_pedalPowerPercentage");
                    mPedalPowerBalanceReceiver.onNewPedalPowerBalance(estTimestamp, eventFlags, rightPedalIndicator, dataType);
                }
                break;
            case 203:
                if (mInstantaneousCadenceReceiver != null) {
                    b = eventMsg.getData();
                    estTimestamp = b.getLong("long_EstTimestamp");
                    eventFlags = EventFlag.getEventFlagsFromLong(b.getLong("long_EventFlags"));
                    dataSource = AntPlusBikePowerPcc.DataSource.getValueFromInt(b.getInt("int_dataSource"));
                    dataType = b.getInt("int_instantaneousCadence");
                    mInstantaneousCadenceReceiver.onNewInstantaneousCadence(estTimestamp, eventFlags, dataSource, dataType);
                }
                break;
            case 204:
                if (mRawWheelTorqueDataReceiver != null) {
                    b = eventMsg.getData();
                    estTimestamp = b.getLong("long_EstTimestamp");
                    eventFlags = EventFlag.getEventFlagsFromLong(b.getLong("long_EventFlags"));
                    ctfUpdateEventCount = b.getLong("long_wheelTorqueUpdateEventCount");
                    accumulatedCrankTicks = b.getLong("long_accumulatedWheelTicks");
                    rightPedalSmoothness = (BigDecimal)b.getSerializable("decimal_accumulatedWheelPeriod");
                    accumulatedCrankTorque = (BigDecimal)b.getSerializable("decimal_accumulatedWheelTorque");
                    mRawWheelTorqueDataReceiver.onNewRawWheelTorqueData(estTimestamp, eventFlags, ctfUpdateEventCount, accumulatedCrankTicks, rightPedalSmoothness, accumulatedCrankTorque);
                }
                break;
            case 205:
                if (mRawCrankTorqueDataReceiver != null) {
                    b = eventMsg.getData();
                    estTimestamp = b.getLong("long_EstTimestamp");
                    eventFlags = EventFlag.getEventFlagsFromLong(b.getLong("long_EventFlags"));
                    ctfUpdateEventCount = b.getLong("long_crankTorqueUpdateEventCount");
                    accumulatedCrankTicks = b.getLong("long_accumulatedCrankTicks");
                    rightPedalSmoothness = (BigDecimal)b.getSerializable("decimal_accumulatedCrankPeriod");
                    accumulatedCrankTorque = (BigDecimal)b.getSerializable("decimal_accumulatedCrankTorque");
                    mRawCrankTorqueDataReceiver.onNewRawCrankTorqueData(estTimestamp, eventFlags, ctfUpdateEventCount, accumulatedCrankTicks, rightPedalSmoothness, accumulatedCrankTorque);
                }
                break;
            case 206:
                if (mTorqueEffectivenessReceiver != null) {
                    b = eventMsg.getData();
                    estTimestamp = b.getLong("long_EstTimestamp");
                    eventFlags = EventFlag.getEventFlagsFromLong(b.getLong("long_EventFlags"));
                    ctfUpdateEventCount = b.getLong("long_powerOnlyUpdateEventCount");
                    timeStamp = (BigDecimal)b.getSerializable("decimal_leftTorqueEffectiveness");
                    measurementValue = (BigDecimal)b.getSerializable("decimal_rightTorqueEffectiveness");
                    mTorqueEffectivenessReceiver.onNewTorqueEffectiveness(estTimestamp, eventFlags, ctfUpdateEventCount, timeStamp, measurementValue);
                }
                break;
            case 207:
                if (mPedalSmoothnessReceiver != null) {
                    b = eventMsg.getData();
                    estTimestamp = b.getLong("long_EstTimestamp");
                    eventFlags = EventFlag.getEventFlagsFromLong(b.getLong("long_EventFlags"));
                    ctfUpdateEventCount = b.getLong("long_powerOnlyUpdateEventCount");
                    boolean separatePedalSmoothnessSupport = b.getBoolean("bool_separatePedalSmoothnessSupport");
                    measurementValue = (BigDecimal)b.getSerializable("decimal_leftOrCombinedPedalSmoothness");
                    rightPedalSmoothness = (BigDecimal)b.getSerializable("decimal_rightPedalSmoothness");
                    mPedalSmoothnessReceiver.onNewPedalSmoothness(estTimestamp, eventFlags, ctfUpdateEventCount, separatePedalSmoothnessSupport, measurementValue, rightPedalSmoothness);
                }
                break;
            case 208:
                if (mRawCtfDataReceiver != null) {
                    b = eventMsg.getData();
                    estTimestamp = b.getLong("long_EstTimestamp");
                    eventFlags = EventFlag.getEventFlagsFromLong(b.getLong("long_EventFlags"));
                    ctfUpdateEventCount = b.getLong("long_ctfUpdateEventCount");
                    timeStamp = (BigDecimal)b.getSerializable("decimal_instantaneousSlope");
                    measurementValue = (BigDecimal)b.getSerializable("decimal_accumulatedTimeStamp");
                    long accumulatedTorqueTicksStamp = b.getLong("long_accumulatedTorqueTicksStamp");
                    mRawCtfDataReceiver.onNewRawCtfData(estTimestamp, eventFlags, ctfUpdateEventCount, timeStamp, measurementValue, accumulatedTorqueTicksStamp);
                }
                break;
            case 209:
                if (mCalibrationMessageReceiver != null || mRequestCalibrationMessageReceiver != null) {
                    b = eventMsg.getData();
                    b.setClassLoader(getClass().getClassLoader());
                    estTimestamp = b.getLong("long_EstTimestamp");
                    eventFlags = EventFlag.getEventFlagsFromLong(b.getLong("long_EventFlags"));
                    if (mCalibrationMessageReceiver != null) {
                        mCalibrationMessageReceiver.onNewCalibrationMessage(estTimestamp, eventFlags, b.getParcelable("parcelable_CalibrationMessage"));
                    }

                    if (mRequestCalibrationMessageReceiver != null) {
                        mRequestCalibrationMessageReceiver.onNewCalibrationMessage(estTimestamp, eventFlags, b.getParcelable("parcelable_CalibrationMessage"));
                        pccHandler.removeCallbacksAndMessages(unsubscribeRequestCalibrationMessageReceiver);
                        pccHandler.postDelayed(unsubscribeRequestCalibrationMessageReceiver, 5000L);
                    }
                }
                break;
            case 210:
                if (mAutoZeroStatusReceiver != null || mRequestAutoZeroStatusReceiver != null) {
                    b = eventMsg.getData();
                    estTimestamp = b.getLong("long_EstTimestamp");
                    eventFlags = EventFlag.getEventFlagsFromLong(b.getLong("long_EventFlags"));
                    AntPlusBikePowerPcc.AutoZeroStatus autoZeroStatus = AntPlusBikePowerPcc.AutoZeroStatus.getValueFromInt(b.getInt("int_autoZeroStatus"));
                    if (mAutoZeroStatusReceiver != null) {
                        mAutoZeroStatusReceiver.onNewAutoZeroStatus(estTimestamp, eventFlags, autoZeroStatus);
                    }

                    if (mRequestAutoZeroStatusReceiver != null) {
                        mRequestAutoZeroStatusReceiver.onNewAutoZeroStatus(estTimestamp, eventFlags, autoZeroStatus);
                        pccHandler.removeCallbacksAndMessages(unsubscribeRequestAutoZeroStatusReceiver);
                        pccHandler.postDelayed(unsubscribeRequestAutoZeroStatusReceiver, 5000L);
                    }
                }
                break;
            case 211:
                if (mCalculatedPowerReceiver != null) {
                    b = eventMsg.getData();
                    estTimestamp = b.getLong("long_EstTimestamp");
                    eventFlags = EventFlag.getEventFlagsFromLong(b.getLong("long_EventFlags"));
                    dataSource = AntPlusBikePowerPcc.DataSource.getValueFromInt(b.getInt("int_dataSource"));
                    calculatedWheelDistance = (BigDecimal)b.getSerializable("decimal_calculatedPower");
                    mCalculatedPowerReceiver.onNewCalculatedPower(estTimestamp, eventFlags, dataSource, calculatedWheelDistance);
                }
                break;
            case 212:
                if (mCalculatedTorqueReceiver != null) {
                    b = eventMsg.getData();
                    estTimestamp = b.getLong("long_EstTimestamp");
                    eventFlags = EventFlag.getEventFlagsFromLong(b.getLong("long_EventFlags"));
                    dataSource = AntPlusBikePowerPcc.DataSource.getValueFromInt(b.getInt("int_dataSource"));
                    calculatedWheelDistance = (BigDecimal)b.getSerializable("decimal_calculatedTorque");
                    mCalculatedTorqueReceiver.onNewCalculatedTorque(estTimestamp, eventFlags, dataSource, calculatedWheelDistance);
                }
                break;
            case 213:
                if (mCalculatedCrankCadenceReceiver != null) {
                    b = eventMsg.getData();
                    estTimestamp = b.getLong("long_EstTimestamp");
                    eventFlags = EventFlag.getEventFlagsFromLong(b.getLong("long_EventFlags"));
                    dataSource = AntPlusBikePowerPcc.DataSource.getValueFromInt(b.getInt("int_dataSource"));
                    calculatedWheelDistance = (BigDecimal)b.getSerializable("decimal_calculatedCrankCadence");
                    mCalculatedCrankCadenceReceiver.onNewCalculatedCrankCadence(estTimestamp, eventFlags, dataSource, calculatedWheelDistance);
                }
                break;
            case 214:
                if (mCalculatedWheelSpeedReceiver != null) {
                    b = eventMsg.getData();
                    estTimestamp = b.getLong("long_EstTimestamp");
                    eventFlags = EventFlag.getEventFlagsFromLong(b.getLong("long_EventFlags"));
                    dataSource = AntPlusBikePowerPcc.DataSource.getValueFromInt(b.getInt("int_dataSource"));
                    calculatedWheelDistance = (BigDecimal)b.getSerializable("decimal_calculatedWheelSpeed");
                    mCalculatedWheelSpeedReceiver.onNewRawCalculatedWheelSpeed(estTimestamp, eventFlags, dataSource, calculatedWheelDistance);
                }
                break;
            case 215:
                if (mCalculatedWheelDistanceReceiver != null) {
                    b = eventMsg.getData();
                    estTimestamp = b.getLong("long_EstTimestamp");
                    eventFlags = EventFlag.getEventFlagsFromLong(b.getLong("long_EventFlags"));
                    dataSource = AntPlusBikePowerPcc.DataSource.getValueFromInt(b.getInt("int_dataSource"));
                    calculatedWheelDistance = (BigDecimal)b.getSerializable("decimal_calculatedWheelDistance");
                    mCalculatedWheelDistanceReceiver.onNewRawCalculatedWheelDistance(estTimestamp, eventFlags, dataSource, calculatedWheelDistance);
                }
                break;
            case 216:
                if (mMeasurementOutputDataReceiver != null || mRequestMeasurementOutputDataReceiver != null) {
                    b = eventMsg.getData();
                    estTimestamp = b.getLong("long_EstTimestamp");
                    eventFlags = EventFlag.getEventFlagsFromLong(b.getLong("long_EventFlags"));
                    int numOfDataTypes = b.getInt("int_numOfDataTypes");
                    dataType = b.getInt("int_dataType");
                    timeStamp = (BigDecimal)b.getSerializable("decimal_timeStamp");
                    measurementValue = (BigDecimal)b.getSerializable("decimal_measurementValue");
                    if (mMeasurementOutputDataReceiver != null) {
                        mMeasurementOutputDataReceiver.onNewMeasurementOutputData(estTimestamp, eventFlags, numOfDataTypes, dataType, timeStamp, measurementValue);
                    }

                    if (mRequestMeasurementOutputDataReceiver != null) {
                        mRequestMeasurementOutputDataReceiver.onNewMeasurementOutputData(estTimestamp, eventFlags, numOfDataTypes, dataType, timeStamp, measurementValue);
                        pccHandler.removeCallbacksAndMessages(unsubscribeRequestMeasurementOutputDataReceiver);
                        pccHandler.postDelayed(unsubscribeRequestMeasurementOutputDataReceiver, 5000L);
                    }
                }
                break;
            case 217:
                if (mCrankParametersReceiver != null || mRequestCrankParametersReceiver != null) {
                    b = eventMsg.getData();
                    b.setClassLoader(getClass().getClassLoader());
                    estTimestamp = b.getLong("long_EstTimestamp");
                    eventFlags = EventFlag.getEventFlagsFromLong(b.getLong("long_EventFlags"));
                    if (mCrankParametersReceiver != null) {
                        mCrankParametersReceiver.onNewCrankParameters(estTimestamp, eventFlags, b.getParcelable("parcelable_CrankParameters"));
                    }

                    if (mRequestCrankParametersReceiver != null) {
                        mRequestCrankParametersReceiver.onNewCrankParameters(estTimestamp, eventFlags, b.getParcelable("parcelable_CrankParameters"));
                        pccHandler.removeCallbacksAndMessages(unsubscribeRequestCrankParametersReceiver);
                        pccHandler.postDelayed(unsubscribeRequestCrankParametersReceiver, 5000L);
                    }
                }
                break;
            case 218:
                IRequestFinishedReceiver tempReceiver = mRequestFinishedReceiver;
                mRequestFinishedReceiver = null;
                mCommandLock.release();
                if (tempReceiver != null) {
                    Bundle bundle = eventMsg.getData();
                    int requestStatus = bundle.getInt("int_requestStatus");
                    tempReceiver.onNewRequestFinished(RequestStatus.getValueFromInt(requestStatus));
                }
                break;
            default:
                super.handlePluginEvent(eventMsg);
        }

    }

    public void subscribeRawPowerOnlyDataEvent(AntPlusBikePowerPcc.IRawPowerOnlyDataReceiver RawPowerOnlyDataReceiver) {
        mRawPowerOnlyDataReceiver = RawPowerOnlyDataReceiver;
        if (RawPowerOnlyDataReceiver != null) {
            subscribeToEvent(201);
        } else {
            unsubscribeFromEvent(201);
        }

    }

    public void subscribePedalPowerBalanceEvent(AntPlusBikePowerPcc.IPedalPowerBalanceReceiver PedalPowerBalanceReceiver) {
        mPedalPowerBalanceReceiver = PedalPowerBalanceReceiver;
        if (PedalPowerBalanceReceiver != null) {
            subscribeToEvent(202);
        } else {
            unsubscribeFromEvent(202);
        }

    }

    public void subscribeInstantaneousCadenceEvent(AntPlusBikePowerPcc.IInstantaneousCadenceReceiver InstantaneousCadenceReceiver) {
        mInstantaneousCadenceReceiver = InstantaneousCadenceReceiver;
        if (InstantaneousCadenceReceiver != null) {
            subscribeToEvent(203);
        } else {
            unsubscribeFromEvent(203);
        }

    }

    public void subscribeRawWheelTorqueDataEvent(AntPlusBikePowerPcc.IRawWheelTorqueDataReceiver RawWheelTorqueDataReceiver) {
        mRawWheelTorqueDataReceiver = RawWheelTorqueDataReceiver;
        if (RawWheelTorqueDataReceiver != null) {
            subscribeToEvent(204);
        } else {
            unsubscribeFromEvent(204);
        }

    }

    public void subscribeRawCrankTorqueDataEvent(AntPlusBikePowerPcc.IRawCrankTorqueDataReceiver RawCrankTorqueDataReceiver) {
        mRawCrankTorqueDataReceiver = RawCrankTorqueDataReceiver;
        if (RawCrankTorqueDataReceiver != null) {
            subscribeToEvent(205);
        } else {
            unsubscribeFromEvent(205);
        }

    }

    public void subscribeTorqueEffectivenessEvent(AntPlusBikePowerPcc.ITorqueEffectivenessReceiver TorqueEffectivenessReceiver) {
        mTorqueEffectivenessReceiver = TorqueEffectivenessReceiver;
        if (TorqueEffectivenessReceiver != null) {
            subscribeToEvent(206);
        } else {
            unsubscribeFromEvent(206);
        }

    }

    public void subscribePedalSmoothnessEvent(AntPlusBikePowerPcc.IPedalSmoothnessReceiver PedalSmoothnessReceiver) {
        mPedalSmoothnessReceiver = PedalSmoothnessReceiver;
        if (PedalSmoothnessReceiver != null) {
            subscribeToEvent(207);
        } else {
            unsubscribeFromEvent(207);
        }

    }

    public void subscribeRawCtfDataEvent(AntPlusBikePowerPcc.IRawCtfDataReceiver RawCtfDataReceiver) {
        mRawCtfDataReceiver = RawCtfDataReceiver;
        if (RawCtfDataReceiver != null) {
            subscribeToEvent(208);
        } else {
            unsubscribeFromEvent(208);
        }

    }

    public void subscribeCalibrationMessageEvent(AntPlusBikePowerPcc.ICalibrationMessageReceiver calibrationMessageReceiver) {
        if (mRequestCalibrationMessageReceiver == null) {
            if (calibrationMessageReceiver != null && mCalibrationMessageReceiver == null) {
                subscribeToEvent(209);
            } else if (calibrationMessageReceiver == null && mCalibrationMessageReceiver != null) {
                unsubscribeFromEvent(209);
            }
        }

        mCalibrationMessageReceiver = calibrationMessageReceiver;
    }

    private void subscribeRequestCalibrationMessageEvent(AntPlusBikePowerPcc.ICalibrationMessageReceiver calibrationMessageReceiver) {
        if (mCalibrationMessageReceiver == null) {
            if (calibrationMessageReceiver != null && mRequestCalibrationMessageReceiver == null) {
                subscribeToEvent(209);
            } else if (calibrationMessageReceiver == null && mRequestCalibrationMessageReceiver != null) {
                unsubscribeFromEvent(209);
            }
        }

        mRequestCalibrationMessageReceiver = calibrationMessageReceiver;
    }

    public void subscribeAutoZeroStatusEvent(AntPlusBikePowerPcc.IAutoZeroStatusReceiver autoZeroStatusReceiver) {
        if (mRequestAutoZeroStatusReceiver == null) {
            if (autoZeroStatusReceiver != null && mAutoZeroStatusReceiver == null) {
                subscribeToEvent(210);
            } else if (autoZeroStatusReceiver == null && mAutoZeroStatusReceiver != null) {
                unsubscribeFromEvent(210);
            }
        }

        mAutoZeroStatusReceiver = autoZeroStatusReceiver;
    }

    private void subscribeRequestAutoZeroStatusEvent(AntPlusBikePowerPcc.IAutoZeroStatusReceiver autoZeroStatusReceiver) {
        if (mAutoZeroStatusReceiver == null) {
            if (autoZeroStatusReceiver != null && mRequestAutoZeroStatusReceiver == null) {
                subscribeToEvent(210);
            } else if (autoZeroStatusReceiver == null && mRequestAutoZeroStatusReceiver != null) {
                unsubscribeFromEvent(210);
            }
        }

        mRequestAutoZeroStatusReceiver = autoZeroStatusReceiver;
    }

    public void subscribeCalculatedPowerEvent(AntPlusBikePowerPcc.ICalculatedPowerReceiver CalculatedPowerReceiver) {
        mCalculatedPowerReceiver = CalculatedPowerReceiver;
        if (CalculatedPowerReceiver != null) {
            subscribeToEvent(211);
        } else {
            unsubscribeFromEvent(211);
        }

    }

    public void subscribeCalculatedTorqueEvent(AntPlusBikePowerPcc.ICalculatedTorqueReceiver CalculatedTorqueReceiver) {
        mCalculatedTorqueReceiver = CalculatedTorqueReceiver;
        if (CalculatedTorqueReceiver != null) {
            subscribeToEvent(212);
        } else {
            unsubscribeFromEvent(212);
        }

    }

    public void subscribeCalculatedCrankCadenceEvent(AntPlusBikePowerPcc.ICalculatedCrankCadenceReceiver CalculatedCrankCadenceReceiver) {
        mCalculatedCrankCadenceReceiver = CalculatedCrankCadenceReceiver;
        if (CalculatedCrankCadenceReceiver != null) {
            subscribeToEvent(213);
        } else {
            unsubscribeFromEvent(213);
        }

    }

    public void subscribeCalculatedWheelSpeedEvent(AntPlusBikePowerPcc.CalculatedWheelSpeedReceiver CalculatedWheelSpeedReceiver) {
        mCalculatedWheelSpeedReceiver = CalculatedWheelSpeedReceiver;
        if (CalculatedWheelSpeedReceiver != null) {
            subscribeToEvent(214);
        } else {
            unsubscribeFromEvent(214);
        }

    }

    public void subscribeCalculatedWheelDistanceEvent(AntPlusBikePowerPcc.CalculatedWheelDistanceReceiver CalculatedWheelDistanceReceiver) {
        mCalculatedWheelDistanceReceiver = CalculatedWheelDistanceReceiver;
        if (CalculatedWheelDistanceReceiver != null) {
            subscribeToEvent(215);
        } else {
            unsubscribeFromEvent(215);
        }

    }

    public void subscribeMeasurementOutputDataEvent(AntPlusBikePowerPcc.IMeasurementOutputDataReceiver measurementOutputDataReceiver) {
        if (mRequestMeasurementOutputDataReceiver == null) {
            if (measurementOutputDataReceiver != null && mMeasurementOutputDataReceiver == null) {
                subscribeToEvent(216);
            }

            if (measurementOutputDataReceiver == null && mMeasurementOutputDataReceiver != null) {
                unsubscribeFromEvent(216);
            }
        }

        mMeasurementOutputDataReceiver = measurementOutputDataReceiver;
    }

    private void subscribeRequestMeasurementOutputDataEvent(AntPlusBikePowerPcc.IMeasurementOutputDataReceiver measurementOutputDataReceiver) {
        if (mMeasurementOutputDataReceiver == null) {
            if (measurementOutputDataReceiver != null && mRequestMeasurementOutputDataReceiver == null) {
                subscribeToEvent(216);
            } else if (measurementOutputDataReceiver == null && mRequestMeasurementOutputDataReceiver != null) {
                unsubscribeFromEvent(216);
            }
        }

        mRequestMeasurementOutputDataReceiver = measurementOutputDataReceiver;
    }

    public void subscribeCrankParametersEvent(AntPlusBikePowerPcc.ICrankParametersReceiver crankParametersReceiver) {
        if (mRequestCrankParametersReceiver == null) {
            if (crankParametersReceiver != null && mCrankParametersReceiver == null) {
                subscribeToEvent(217);
            } else if (crankParametersReceiver == null && mCrankParametersReceiver != null) {
                unsubscribeFromEvent(217);
            }
        }

        mCrankParametersReceiver = crankParametersReceiver;
    }

    private void subscribeRequestCrankParametersEvent(AntPlusBikePowerPcc.ICrankParametersReceiver crankParametersReceiver) {
        if (mCrankParametersReceiver == null) {
            if (crankParametersReceiver != null && mRequestCrankParametersReceiver == null) {
                subscribeToEvent(217);
            } else if (crankParametersReceiver == null && mRequestCrankParametersReceiver != null) {
                unsubscribeFromEvent(217);
            }
        }

        mRequestCrankParametersReceiver = crankParametersReceiver;
    }

    public boolean requestManualCalibration(IRequestFinishedReceiver requestFinishedReceiver) {
        return requestManualCalibration(requestFinishedReceiver, mRequestCalibrationMessageReceiver, mRequestMeasurementOutputDataReceiver);
    }

    public boolean requestManualCalibration(IRequestFinishedReceiver requestFinishedReceiver, AntPlusBikePowerPcc.ICalibrationMessageReceiver calibrationMessageReceiver, AntPlusBikePowerPcc.IMeasurementOutputDataReceiver measurementOutputDataReceiver) {
        String cmdName = "requestManualCalibration";
        int whatCmd = 20001;
        subscribeRequestCalibrationMessageEvent(calibrationMessageReceiver);
        subscribeRequestMeasurementOutputDataEvent(measurementOutputDataReceiver);
        return sendRequestCommand(cmdName, whatCmd, requestFinishedReceiver);
    }

    public boolean requestSetAutoZero(boolean autoZeroEnable, IRequestFinishedReceiver requestFinishedReceiver) {
        return requestSetAutoZero(autoZeroEnable, requestFinishedReceiver, mRequestCalibrationMessageReceiver, mRequestAutoZeroStatusReceiver);
    }

    public boolean requestSetAutoZero(boolean autoZeroEnable, IRequestFinishedReceiver requestFinishedReceiver, AntPlusBikePowerPcc.ICalibrationMessageReceiver calibrationMessageReceiver, AntPlusBikePowerPcc.IAutoZeroStatusReceiver autoZeroStatusReceiver) {
        String cmdName = "requestSetAutoZero";
        int whatCmd = 20002;
        Bundle params = new Bundle();
        params.putBoolean("bool_autoZeroEnable", autoZeroEnable);
        subscribeRequestCalibrationMessageEvent(calibrationMessageReceiver);
        subscribeRequestAutoZeroStatusEvent(autoZeroStatusReceiver);
        return sendRequestCommand(cmdName, whatCmd, params, requestFinishedReceiver);
    }

    public boolean requestSetCtfSlope(BigDecimal slope, IRequestFinishedReceiver requestFinishedReceiver) {
        return requestSetCtfSlope(slope, requestFinishedReceiver, mRequestCalibrationMessageReceiver);
    }

    public boolean requestSetCtfSlope(BigDecimal slope, IRequestFinishedReceiver requestFinishedReceiver, AntPlusBikePowerPcc.ICalibrationMessageReceiver calibrationMessageReceiver) {
        String cmdName = "requestSetCtfSlope";
        int whatCmd = 20003;
        Bundle params = new Bundle();
        params.putSerializable("decimal_slope", slope);
        subscribeRequestCalibrationMessageEvent(calibrationMessageReceiver);
        return sendRequestCommand(cmdName, whatCmd, params, requestFinishedReceiver);
    }

    public boolean requestCustomCalibrationParameters(byte[] manufacturerSpecificParameters, IRequestFinishedReceiver requestFinishedReceiver) {
        return requestCustomCalibrationParameters(manufacturerSpecificParameters, requestFinishedReceiver, mRequestCalibrationMessageReceiver);
    }

    public boolean requestCustomCalibrationParameters(byte[] manufacturerSpecificParameters, IRequestFinishedReceiver requestFinishedReceiver, AntPlusBikePowerPcc.ICalibrationMessageReceiver calibrationMessageReceiver) {
        String cmdName = "requestCustomCalibrationParameters";
        int whatCmd = 20004;
        Bundle params = new Bundle();
        params.putByteArray("arrayByte_manufacturerSpecificParameters", manufacturerSpecificParameters);
        subscribeRequestCalibrationMessageEvent(calibrationMessageReceiver);
        return sendRequestCommand(cmdName, whatCmd, params, requestFinishedReceiver);
    }

    public boolean requestSetCustomCalibrationParameters(byte[] manufacturerSpecificParameters, IRequestFinishedReceiver requestFinishedReceiver) {
        return requestSetCustomCalibrationParameters(manufacturerSpecificParameters, requestFinishedReceiver, mRequestCalibrationMessageReceiver);
    }

    public boolean requestSetCustomCalibrationParameters(byte[] manufacturerSpecificParameters, IRequestFinishedReceiver requestFinishedReceiver, AntPlusBikePowerPcc.ICalibrationMessageReceiver calibrationMessageReceiver) {
        String cmdName = "requestSetCustomCalibrationParameters";
        int whatCmd = 20005;
        Bundle params = new Bundle();
        params.putByteArray("arrayByte_manufacturerSpecificParameters", manufacturerSpecificParameters);
        subscribeRequestCalibrationMessageEvent(calibrationMessageReceiver);
        return sendRequestCommand(cmdName, whatCmd, params, requestFinishedReceiver);
    }

    public boolean requestCrankParameters(IRequestFinishedReceiver requestFinishedReceiver) {
        return requestCrankParameters(requestFinishedReceiver, mRequestCrankParametersReceiver);
    }

    public boolean requestCrankParameters(IRequestFinishedReceiver requestFinishedReceiver, AntPlusBikePowerPcc.ICrankParametersReceiver crankParametersReceiver) {
        String cmdName = "requestCrankParameters";
        int whatCmd = 20006;
        subscribeRequestCrankParametersEvent(crankParametersReceiver);
        return sendRequestCommand(cmdName, whatCmd, requestFinishedReceiver);
    }

    public boolean requestSetCrankParameters(AntPlusBikePowerPcc.CrankLengthSetting crankLengthSetting, BigDecimal fullCrankLength, IRequestFinishedReceiver requestFinishedReceiver) {
        String cmdName = "requestSetCrankParameters";
        int whatCmd = 20007;
        Bundle params = new Bundle();
        params.putInt("int_crankLengthSetting", crankLengthSetting.getIntValue());
        params.putSerializable("decimal_fullCrankLength", fullCrankLength);
        return sendRequestCommand(cmdName, whatCmd, params, requestFinishedReceiver);
    }

    public boolean requestCommandBurst(int requestedCommandId, byte[] commandData, IRequestFinishedReceiver requestFinishedReceiver) {
        String cmdName = "requestCommandBurst";
        int whatCmd = 104;
        Bundle params = new Bundle();
        params.putInt("int_requestedCommandId", requestedCommandId);
        params.putByteArray("arrayByte_commandData", commandData);
        return sendRequestCommand(cmdName, whatCmd, params, requestFinishedReceiver, Integer.valueOf(20206));
    }

    public interface ICrankParametersReceiver {
        void onNewCrankParameters(long var1, EnumSet<EventFlag> var3, AntPlusBikePowerPcc.CrankParameters var4);
    }

    public interface IMeasurementOutputDataReceiver {
        void onNewMeasurementOutputData(long var1, EnumSet<EventFlag> var3, int var4, int var5, BigDecimal var6, BigDecimal var7);
    }

    public abstract static class CalculatedWheelDistanceReceiver {
        BigDecimal wheelCircumference;
        BigDecimal initialDistanceZeroVal = null;

        public CalculatedWheelDistanceReceiver(BigDecimal wheelCircumference) {
            this.wheelCircumference = wheelCircumference;
        }

        public abstract void onNewCalculatedWheelDistance(long var1, EnumSet<EventFlag> var3, AntPlusBikePowerPcc.DataSource var4, BigDecimal var5);

        private void onNewRawCalculatedWheelDistance(long estTimestamp, EnumSet<EventFlag> eventFlags, AntPlusBikePowerPcc.DataSource dataSource, BigDecimal rawCalculatedWheelDistance) {
            if (initialDistanceZeroVal == null) {
                initialDistanceZeroVal = rawCalculatedWheelDistance.multiply(wheelCircumference);
            }

            onNewCalculatedWheelDistance(estTimestamp, eventFlags, dataSource, rawCalculatedWheelDistance.multiply(wheelCircumference).subtract(initialDistanceZeroVal));
        }
    }

    public abstract static class CalculatedWheelSpeedReceiver {
        BigDecimal wheelCircumference;

        public CalculatedWheelSpeedReceiver(BigDecimal wheelCircumference) {
            this.wheelCircumference = wheelCircumference;
        }

        public abstract void onNewCalculatedWheelSpeed(long var1, EnumSet<EventFlag> var3, AntPlusBikePowerPcc.DataSource var4, BigDecimal var5);

        private void onNewRawCalculatedWheelSpeed(long estTimestamp, EnumSet<EventFlag> eventFlags, AntPlusBikePowerPcc.DataSource dataSource, BigDecimal rawCalculatedWheelSpeed) {
            onNewCalculatedWheelSpeed(estTimestamp, eventFlags, dataSource, rawCalculatedWheelSpeed.multiply(wheelCircumference));
        }
    }

    public interface ICalculatedCrankCadenceReceiver {
        void onNewCalculatedCrankCadence(long var1, EnumSet<EventFlag> var3, AntPlusBikePowerPcc.DataSource var4, BigDecimal var5);
    }

    public interface ICalculatedTorqueReceiver {
        void onNewCalculatedTorque(long var1, EnumSet<EventFlag> var3, AntPlusBikePowerPcc.DataSource var4, BigDecimal var5);
    }

    public interface ICalculatedPowerReceiver {
        void onNewCalculatedPower(long var1, EnumSet<EventFlag> var3, AntPlusBikePowerPcc.DataSource var4, BigDecimal var5);
    }

    public interface IAutoZeroStatusReceiver {
        void onNewAutoZeroStatus(long var1, EnumSet<EventFlag> var3, AntPlusBikePowerPcc.AutoZeroStatus var4);
    }

    public interface ICalibrationMessageReceiver {
        void onNewCalibrationMessage(long var1, EnumSet<EventFlag> var3, AntPlusBikePowerPcc.CalibrationMessage var4);
    }

    public interface IRawCtfDataReceiver {
        void onNewRawCtfData(long var1, EnumSet<EventFlag> var3, long var4, BigDecimal var6, BigDecimal var7, long var8);
    }

    public interface IPedalSmoothnessReceiver {
        void onNewPedalSmoothness(long var1, EnumSet<EventFlag> var3, long var4, boolean var6, BigDecimal var7, BigDecimal var8);
    }

    public interface ITorqueEffectivenessReceiver {
        void onNewTorqueEffectiveness(long var1, EnumSet<EventFlag> var3, long var4, BigDecimal var6, BigDecimal var7);
    }

    public interface IRawCrankTorqueDataReceiver {
        void onNewRawCrankTorqueData(long var1, EnumSet<EventFlag> var3, long var4, long var6, BigDecimal var8, BigDecimal var9);
    }

    public interface IRawWheelTorqueDataReceiver {
        void onNewRawWheelTorqueData(long var1, EnumSet<EventFlag> var3, long var4, long var6, BigDecimal var8, BigDecimal var9);
    }

    public interface IInstantaneousCadenceReceiver {
        void onNewInstantaneousCadence(long var1, EnumSet<EventFlag> var3, AntPlusBikePowerPcc.DataSource var4, int var5);
    }

    public interface IPedalPowerBalanceReceiver {
        void onNewPedalPowerBalance(long var1, EnumSet<EventFlag> var3, boolean var4, int var5);
    }

    public interface IRawPowerOnlyDataReceiver {
        void onNewRawPowerOnlyData(long var1, EnumSet<EventFlag> var3, long var4, int var6, long var7);
    }

    public enum CrankLengthSetting {
        AUTO_CRANK_LENGTH(254),
        MANUAL_CRANK_LENGTH(65280),
        INVALID(255);

        private final int intValue;

        CrankLengthSetting(int intValue) {
            this.intValue = intValue;
        }

        public int getIntValue() {
            return intValue;
        }

        public static AntPlusBikePowerPcc.CrankLengthSetting getValueFromInt(int intValue) {
            switch(intValue) {
                case 254:
                    return AUTO_CRANK_LENGTH;
                case 255:
                default:
                    return INVALID;
                case 65280:
                    return MANUAL_CRANK_LENGTH;
            }
        }
    }

    public enum CustomCalibrationStatus {
        UNDEFINED,
        CUSTOM_CALIBRATION_NOT_REQUIRED,
        CUSTOM_CALIBRATION_REQUIRED;

        CustomCalibrationStatus() {
        }

        public int getIntValue() {
            return ordinal();
        }

        public static AntPlusBikePowerPcc.CustomCalibrationStatus getValueFromInt(int intValue) {
            switch(intValue) {
                case 0:
                    return UNDEFINED;
                case 1:
                    return CUSTOM_CALIBRATION_NOT_REQUIRED;
                case 2:
                    return CUSTOM_CALIBRATION_REQUIRED;
                case 3:
                    return UNDEFINED;
                default:
                    throw new IllegalArgumentException("Undefined Custom Calibration Status");
            }
        }
    }

    public enum SensorAvailabilityStatus {
        UNDEFINED,
        LEFT_SENSOR_PRESENT,
        RIGHT_SENSOR_PRESENT,
        LEFT_AND_RIGHT_SENSOR_PRESENT;

        SensorAvailabilityStatus() {
        }

        public int getIntValue() {
            return ordinal();
        }

        public static AntPlusBikePowerPcc.SensorAvailabilityStatus getValueFromInt(int intValue) {
            switch(intValue) {
                case 0:
                    return UNDEFINED;
                case 1:
                    return LEFT_SENSOR_PRESENT;
                case 2:
                    return RIGHT_SENSOR_PRESENT;
                case 3:
                    return LEFT_AND_RIGHT_SENSOR_PRESENT;
                default:
                    throw new IllegalArgumentException("Undefined Sensor Availability Status");
            }
        }
    }

    public enum SensorSoftwareMismatchStatus {
        UNDEFINED,
        MISMATCH_RIGHT_SENSOR_OLDER,
        MISMATCH_LEFT_SENSOR_OLDER,
        SW_MATCHES;

        SensorSoftwareMismatchStatus() {
        }

        public int getIntValue() {
            return ordinal();
        }

        public static AntPlusBikePowerPcc.SensorSoftwareMismatchStatus getValueFromInt(int intValue) {
            switch(intValue) {
                case 0:
                    return UNDEFINED;
                case 1:
                    return MISMATCH_RIGHT_SENSOR_OLDER;
                case 2:
                    return MISMATCH_LEFT_SENSOR_OLDER;
                case 3:
                    return SW_MATCHES;
                default:
                    throw new IllegalArgumentException("Undefined Sensor Software Mismatch Status");
            }
        }
    }

    public enum CrankLengthStatus {
        INVALID_CRANK_LENGTH,
        DEFAULT_USED,
        SET_MANUALLY,
        SET_AUTOMATICALLY;

        CrankLengthStatus() {
        }

        public int getIntValue() {
            return ordinal();
        }

        public static AntPlusBikePowerPcc.CrankLengthStatus getValueFromInt(int intValue) {
            switch(intValue) {
                case 0:
                    return INVALID_CRANK_LENGTH;
                case 1:
                    return DEFAULT_USED;
                case 2:
                    return SET_MANUALLY;
                case 3:
                    return SET_AUTOMATICALLY;
                default:
                    throw new IllegalArgumentException("Undefined Crank Length Status");
            }
        }
    }

    public enum MeasurementDataType {
        COUNTDOWN_PERCENTAGE(0),
        COUNTDOWN_TIME(1),
        TORQUE_WHOLE_SENSOR(8),
        TORQUE_LEFT(9),
        TORQUE_RIGHT(10),
        FORCE_WHOLE_SENSOR(16),
        FORCE_LEFT(17),
        FORCE_RIGHT(18),
        ZERO_OFFSET(24),
        TEMPERATURE(25),
        VOLTAGE(26),
        INVALID(-1),
        UNRECOGNIZED(-2);

        private final int intValue;

        MeasurementDataType(int intValue) {
            this.intValue = intValue;
        }

        public int getIntValue() {
            return intValue;
        }

        public static MeasurementDataType getValueFromInt(int intValue) {
            MeasurementDataType[] var1 = values();

            for (MeasurementDataType dataType : var1) {
                if (dataType.getIntValue() == intValue) {
                    return dataType;
                }
            }

            return UNRECOGNIZED;
        }
    }

    public enum DataSource {
        POWER_ONLY_DATA(16),
        WHEEL_TORQUE_DATA(17),
        CRANK_TORQUE_DATA(18),
        CTF_DATA(32),
        COAST_OR_STOP_DETECTED(65536),
        INITIAL_VALUE_POWER_ONLY_DATA(65296),
        INITIAL_VALUE_WHEEL_TORQUE_DATA(65297),
        INITIAL_VALUE_CRANK_TORQUE_DATA(65298),
        INITIAL_VALUE_CTF_DATA(65312),
        INVALID(-1),
        INVALID_CTF_CAL_REQ(-2),
        UNRECOGNIZED(-3);

        private final int intValue;

        DataSource(int intValue) {
            this.intValue = intValue;
        }

        public int getIntValue() {
            return intValue;
        }

        public static AntPlusBikePowerPcc.DataSource getValueFromInt(int intValue) {
            AntPlusBikePowerPcc.DataSource[] var1 = values();

            for (DataSource source : var1) {
                if (source.getIntValue() == intValue) {
                    return source;
                }
            }

            return UNRECOGNIZED;
        }
    }

    public enum AutoZeroStatus {
        OFF(0),
        ON(1),
        NOT_SUPPORTED(255),
        INVALID(-1),
        UNKNOWN(-2),
        UNRECOGNIZED(-3);

        private final int intValue;

        AutoZeroStatus(int intValue) {
            this.intValue = intValue;
        }

        public int getIntValue() {
            return intValue;
        }

        public static AntPlusBikePowerPcc.AutoZeroStatus getValueFromInt(int intValue) {
            AntPlusBikePowerPcc.AutoZeroStatus[] var1 = values();

            for (AutoZeroStatus status : var1) {
                if (status.getIntValue() == intValue) {
                    return status;
                }
            }

            return UNRECOGNIZED;
        }
    }

    public enum CalibrationId {
        GENERAL_CALIBRATION_SUCCESS(172),
        GENERAL_CALIBRATION_FAIL(175),
        CTF_MESSAGE(16),
        CTF_ZERO_OFFSET(4097),
        CTF_SLOPE_ACK(1092610),
        CTF_SERIAL_NUMBER_ACK(1092611),
        CAPABILITIES(18),
        CUSTOM_CALIBRATION_RESPONSE(187),
        CUSTOM_CALIBRATION_UPDATE_SUCCESS(189),
        INVALID(-1),
        UNRECOGNIZED(-2);

        private final int intValue;

        CalibrationId(int intValue) {
            this.intValue = intValue;
        }

        public int getIntValue() {
            return intValue;
        }

        public static AntPlusBikePowerPcc.CalibrationId getValueFromInt(int intValue) {
            AntPlusBikePowerPcc.CalibrationId[] var1 = values();

            for (CalibrationId ident : var1) {
                if (ident.getIntValue() == intValue) {
                    return ident;
                }
            }

            return UNRECOGNIZED;
        }
    }

    public static final class IpcDefines {
        public static final String PATH_ANTPLUS_BIKEPOWERPLUGIN_PKG = "com.dsi.ant.plugins.antplus";
        public static final String PATH_ANTPLUS_BIKEPOWERPLUGIN_SERVICE = "com.dsi.ant.plugins.antplus.bikepower.BikePowerService";
        public static final int MSG_EVENT_BIKEPOWER_whatRAWPOWERONLYDATA = 201;
        public static final String MSG_EVENT_BIKEPOWER_RAWPOWERONLYDATA_PARAM_longPOWERONLYUPDATEEVENTCOUNT = "long_powerOnlyUpdateEventCount";
        public static final String MSG_EVENT_BIKEPOWER_RAWPOWERONLYDATA_PARAM_intINSTANTANEOUSPOWER = "int_instantaneousPower";
        public static final String MSG_EVENT_BIKEPOWER_RAWPOWERONLYDATA_PARAM_longACCUMULATEDPOWER = "long_accumulatedPower";
        public static final int MSG_EVENT_BIKEPOWER_whatPEDALPOWERBALANCE = 202;
        public static final String MSG_EVENT_BIKEPOWER_PEDALPOWERBALANCE_PARAM_boolRIGHTPEDALINDICATOR = "bool_rightPedalIndicator";
        public static final String MSG_EVENT_BIKEPOWER_PEDALPOWERBALANCE_PARAM_intPEDALPOWERPERCENTAGE = "int_pedalPowerPercentage";
        public static final int MSG_EVENT_BIKEPOWER_whatINSTANTANEOUSCADENCE = 203;
        public static final String MSG_EVENT_BIKEPOWER_INSTANTANEOUSCADENCE_PARAM_intDATASOURCE = "int_dataSource";
        public static final String MSG_EVENT_BIKEPOWER_INSTANTANEOUSCADENCE_PARAM_intINSTANTANEOUSCADENCE = "int_instantaneousCadence";
        public static final int MSG_EVENT_BIKEPOWER_whatRAWWHEELTORQUEDATA = 204;
        public static final String MSG_EVENT_BIKEPOWER_RAWWHEELTORQUEDATA_PARAM_longWHEELTORQUEUPDATEEVENTCOUNT = "long_wheelTorqueUpdateEventCount";
        public static final String MSG_EVENT_BIKEPOWER_RAWWHEELTORQUEDATA_PARAM_longACCUMULATEDWHEELTICKS = "long_accumulatedWheelTicks";
        public static final String MSG_EVENT_BIKEPOWER_RAWWHEELTORQUEDATA_PARAM_decimalACCUMULATEDWHEELPERIOD = "decimal_accumulatedWheelPeriod";
        public static final String MSG_EVENT_BIKEPOWER_RAWWHEELTORQUEDATA_PARAM_decimalACCUMULATEDWHEELTORQUE = "decimal_accumulatedWheelTorque";
        public static final int MSG_EVENT_BIKEPOWER_whatRAWCRANKTORQUEDATA = 205;
        public static final String MSG_EVENT_BIKEPOWER_RAWCRANKTORQUEDATA_PARAM_longCRANKTORQUEUPDATEEVENTCOUNT = "long_crankTorqueUpdateEventCount";
        public static final String MSG_EVENT_BIKEPOWER_RAWCRANKTORQUEDATA_PARAM_longACCUMULATEDCRANKTICKS = "long_accumulatedCrankTicks";
        public static final String MSG_EVENT_BIKEPOWER_RAWCRANKTORQUEDATA_PARAM_decimalACCUMULATEDCRANKPERIOD = "decimal_accumulatedCrankPeriod";
        public static final String MSG_EVENT_BIKEPOWER_RAWCRANKTORQUEDATA_PARAM_decimalACCUMULATEDCRANKTORQUE = "decimal_accumulatedCrankTorque";
        public static final int MSG_EVENT_BIKEPOWER_whatTORQUEEFFECTIVENESS = 206;
        public static final String MSG_EVENT_BIKEPOWER_TORQUEEFFECTIVENESS_PARAM_longPOWERONLYUPDATEEVENTCOUNT = "long_powerOnlyUpdateEventCount";
        public static final String MSG_EVENT_BIKEPOWER_TORQUEEFFECTIVENESS_PARAM_decimalLEFTTORQUEEFFECTIVENESS = "decimal_leftTorqueEffectiveness";
        public static final String MSG_EVENT_BIKEPOWER_TORQUEEFFECTIVENESS_PARAM_decimalRIGHTTORQUEEFFECTIVENESS = "decimal_rightTorqueEffectiveness";
        public static final int MSG_EVENT_BIKEPOWER_whatPEDALSMOOTHNESS = 207;
        public static final String MSG_EVENT_BIKEPOWER_PEDALSMOOTHNESS_PARAM_longPOWERONLYUPDATEEVENTCOUNT = "long_powerOnlyUpdateEventCount";
        public static final String MSG_EVENT_BIKEPOWER_PEDALSMOOTHNESS_PARAM_boolSEPARATEPEDALSMOOTHNESSSUPPORT = "bool_separatePedalSmoothnessSupport";
        public static final String MSG_EVENT_BIKEPOWER_PEDALSMOOTHNESS_PARAM_decimalLEFTORCOMBINEDPEDALSMOOTHNESS = "decimal_leftOrCombinedPedalSmoothness";
        public static final String MSG_EVENT_BIKEPOWER_PEDALSMOOTHNESS_PARAM_decimalRIGHTPEDALSMOOTHNESS = "decimal_rightPedalSmoothness";
        public static final int MSG_EVENT_BIKEPOWER_whatRAWCTFDATA = 208;
        public static final String MSG_EVENT_BIKEPOWER_RAWCTFDATA_PARAM_longCTFUPDATEEVENTCOUNT = "long_ctfUpdateEventCount";
        public static final String MSG_EVENT_BIKEPOWER_RAWCTFDATA_PARAM_decimalINSTANTANEOUSSLOPE = "decimal_instantaneousSlope";
        public static final String MSG_EVENT_BIKEPOWER_RAWCTFDATA_PARAM_decimalACCUMULATEDTIMESTAMP = "decimal_accumulatedTimeStamp";
        public static final String MSG_EVENT_BIKEPOWER_RAWCTFDATA_PARAM_longACCUMULATEDTORQUETICKSSTAMP = "long_accumulatedTorqueTicksStamp";
        public static final int MSG_EVENT_BIKEPOWER_whatCALIBRATIONMESSAGE = 209;
        public static final int MSG_EVENT_BIKEPOWER_whatAUTOZEROSTATUS = 210;
        public static final String MSG_EVENT_BIKEPOWER_AUTOZEROSTATUS_PARAM_intAUTOZEROSTATUS = "int_autoZeroStatus";
        public static final int MSG_EVENT_BIKEPOWER_whatCALCULATEDPOWER = 211;
        public static final String MSG_EVENT_BIKEPOWER_CALCULATEDPOWER_PARAM_intDATASOURCE = "int_dataSource";
        public static final String MSG_EVENT_BIKEPOWER_CALCULATEDPOWER_PARAM_decimalCALCULATEDPOWER = "decimal_calculatedPower";
        public static final int MSG_EVENT_BIKEPOWER_whatCALCULATEDTORQUE = 212;
        public static final String MSG_EVENT_BIKEPOWER_CALCULATEDTORQUE_PARAM_intDATASOURCE = "int_dataSource";
        public static final String MSG_EVENT_BIKEPOWER_CALCULATEDTORQUE_PARAM_decimalCALCULATEDTORQUE = "decimal_calculatedTorque";
        public static final int MSG_EVENT_BIKEPOWER_whatCALCULATEDCRANKCADENCE = 213;
        public static final String MSG_EVENT_BIKEPOWER_CALCULATEDCRANKCADENCE_PARAM_intDATASOURCE = "int_dataSource";
        public static final String MSG_EVENT_BIKEPOWER_CALCULATEDCRANKCADENCE_PARAM_decimalCALCULATEDCRANKCADENCE = "decimal_calculatedCrankCadence";
        public static final int MSG_EVENT_BIKEPOWER_whatCALCULATEDWHEELSPEED = 214;
        public static final String MSG_EVENT_BIKEPOWER_CALCULATEDWHEELSPEED_PARAM_intDATASOURCE = "int_dataSource";
        public static final String MSG_EVENT_BIKEPOWER_CALCULATEDWHEELSPEED_PARAM_decimalCALCULATEDWHEELSPEED = "decimal_calculatedWheelSpeed";
        public static final int MSG_EVENT_BIKEPOWER_whatCALCULATEDWHEELDISTANCE = 215;
        public static final String MSG_EVENT_BIKEPOWER_CALCULATEDWHEELDISTANCE_PARAM_intDATASOURCE = "int_dataSource";
        public static final String MSG_EVENT_BIKEPOWER_CALCULATEDWHEELDISTANCE_PARAM_decimalCALCULATEDWHEELDISTANCE = "decimal_calculatedWheelDistance";
        public static final int MSG_EVENT_BIKEPOWER_whatMEASUREMENTOUTPUTDATA = 216;
        public static final String MSG_EVENT_BIKEPOWER_MEASUREMENTOUTPUTDATA_PARAM_intNUMOFDATATYPES = "int_numOfDataTypes";
        public static final String MSG_EVENT_BIKEPOWER_MEASUREMENTOUTPUTDATA_PARAM_intDATATYPE = "int_dataType";
        public static final String MSG_EVENT_BIKEPOWER_MEASUREMENTOUTPUTDATA_PARAM_decimalTIMESTAMP = "decimal_timeStamp";
        public static final String MSG_EVENT_BIKEPOWER_MEASUREMENTOUTPUTDATA_PARAM_decimalMEASUREMENTVALUE = "decimal_measurementValue";
        public static final int MSG_EVENT_BIKEPOWER_whatCRANKPARAMETERS = 217;
        public static final int MSG_EVENT_BIKEPOWER_whatREQUESTFINISHED = 218;
        public static final String MSG_EVENT_BIKEPOWER_REQUESTFINISHED_PARAM_intREQUESTSTATUS = "int_requestStatus";
        public static final int MSG_CMD_BIKEPOWER_whatREQUESTMANUALCALIBRATION = 20001;
        public static final int MSG_CMD_BIKEPOWER_whatSETAUTOZERO = 20002;
        public static final String MSG_CMD_BIKEPOWER_SETAUTOZERO_PARAM_boolAUTOZEROENABLE = "bool_autoZeroEnable";
        public static final int MSG_CMD_BIKEPOWER_whatSETCTFSLOPE = 20003;
        public static final String MSG_CMD_BIKEPOWER_SETCTFSLOPE_PARAM_decimalSLOPE = "decimal_slope";
        public static final int MSG_CMD_BIKEPOWER_whatREQUESTCUSTOMCALIBRATIONPARAMETERS = 20004;
        public static final String MSG_CMD_BIKEPOWER_REQUESTCUSTOMCALIBRATIONPARAMETERS_PARAM_arrayByteMANUFACTURERSPECIFICPARAMETERS = "arrayByte_manufacturerSpecificParameters";
        public static final int MSG_CMD_BIKEPOWER_whatSETCUSTOMCALIBRATIONPARAMETERS = 20005;
        public static final String MSG_CMD_BIKEPOWER_SETCUSTOMCALIBRATIONPARAMETERS_PARAM_arrayByteMANUFACTURERSPECIFICPARAMETERS = "arrayByte_manufacturerSpecificParameters";
        public static final int MSG_CMD_BIKEPOWER_whatREQUESTCRANKPARAMETERS = 20006;
        public static final int MSG_CMD_BIKEPOWER_whatSETCRANKPARAMETERS = 20007;
        public static final String MSG_CMD_BIKEPOWER_SETCRANKPARAMETERS_PARAM_intCRANKLENGTHSETTING = "int_crankLengthSetting";
        public static final String MSG_CMD_BIKEPOWER_SETCRANKPARAMETERS_PARAM_decimalFULLCRANKLENGTH = "decimal_fullCrankLength";

        private IpcDefines() {
        }
    }

    public static class CrankParameters implements Parcelable {
        public static final String KEY_DEFAULT_CRANKPARAMETERSKEY = "parcelable_CrankParameters";
        private final BigDecimal fullCrankLength;
        private final AntPlusBikePowerPcc.CrankLengthStatus crankLengthStatus;
        private final AntPlusBikePowerPcc.SensorSoftwareMismatchStatus sensorSoftwareMismatchStatus;
        private final AntPlusBikePowerPcc.SensorAvailabilityStatus sensorAvailabilityStatus;
        private final AntPlusBikePowerPcc.CustomCalibrationStatus customCalibrationStatus;
        private final boolean autoCrankLengthSupport;
        public static final Creator<AntPlusBikePowerPcc.CrankParameters> CREATOR = new CrankParametersCreator();

        public CrankParameters(BigDecimal fullCrankLength, AntPlusBikePowerPcc.CrankLengthStatus crankLengthStatus, AntPlusBikePowerPcc.SensorSoftwareMismatchStatus sensorSoftwareMismatchStatus, AntPlusBikePowerPcc.SensorAvailabilityStatus sensorAvailabilityStatus, AntPlusBikePowerPcc.CustomCalibrationStatus customCalibrationStatus, boolean autoCrankLengthSupport) {
            this.fullCrankLength = fullCrankLength;
            this.crankLengthStatus = crankLengthStatus;
            this.sensorSoftwareMismatchStatus = sensorSoftwareMismatchStatus;
            this.sensorAvailabilityStatus = sensorAvailabilityStatus;
            this.customCalibrationStatus = customCalibrationStatus;
            this.autoCrankLengthSupport = autoCrankLengthSupport;
        }

        public CrankParameters(Parcel in) {
            int incomingVersion = in.readInt();
            if (incomingVersion != 1) {
                LogAnt.i(TAG, "Decoding version " + incomingVersion + " CrankParameters parcel with version 1 parser.");
            }

            fullCrankLength = new BigDecimal(in.readString());
            crankLengthStatus = AntPlusBikePowerPcc.CrankLengthStatus.getValueFromInt(in.readInt());
            sensorSoftwareMismatchStatus = AntPlusBikePowerPcc.SensorSoftwareMismatchStatus.getValueFromInt(in.readInt());
            sensorAvailabilityStatus = AntPlusBikePowerPcc.SensorAvailabilityStatus.getValueFromInt(in.readInt());
            customCalibrationStatus = AntPlusBikePowerPcc.CustomCalibrationStatus.getValueFromInt(in.readInt());
            autoCrankLengthSupport = in.readByte() != 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            int ipcVersionNumber = 1;
            dest.writeInt(ipcVersionNumber);
            dest.writeString(fullCrankLength.toString());
            dest.writeInt(crankLengthStatus.getIntValue());
            dest.writeInt(sensorSoftwareMismatchStatus.getIntValue());
            dest.writeInt(sensorAvailabilityStatus.getIntValue());
            dest.writeInt(customCalibrationStatus.getIntValue());
            dest.writeByte((byte)(autoCrankLengthSupport ? 1 : 0));
        }

        public int describeContents() {
            return 0;
        }

        public BigDecimal getFullCrankLength() {
            return fullCrankLength;
        }

        public AntPlusBikePowerPcc.CrankLengthStatus getCrankLengthStatus() {
            return crankLengthStatus;
        }

        public AntPlusBikePowerPcc.SensorSoftwareMismatchStatus getSensorSoftwareMismatchStatus() {
            return sensorSoftwareMismatchStatus;
        }

        public AntPlusBikePowerPcc.SensorAvailabilityStatus getSensorAvailabilityStatus() {
            return sensorAvailabilityStatus;
        }

        public AntPlusBikePowerPcc.CustomCalibrationStatus getCustomCalibrationStatus() {
            return customCalibrationStatus;
        }

        public boolean isAutoCrankLengthSupported() {
            return autoCrankLengthSupport;
        }

        private static class CrankParametersCreator implements Creator<CrankParameters> {
            CrankParametersCreator() {
            }

            public CrankParameters createFromParcel(Parcel source) {
                return new CrankParameters(source);
            }

            public CrankParameters[] newArray(int size) {
                return new CrankParameters[size];
            }
        }
    }

    public static class CalibrationMessage implements Parcelable {
        public static final String KEY_DEFAULT_CALIBRATIONMESSAGEKEY = "parcelable_CalibrationMessage";
        public final AntPlusBikePowerPcc.CalibrationId calibrationId;
        public final Integer calibrationData;
        public final Integer ctfOffset;
        public final byte[] manufacturerSpecificData;
        public static final Creator<AntPlusBikePowerPcc.CalibrationMessage> CREATOR = new CalibrationMessageCreator();

        public CalibrationMessage(AntPlusBikePowerPcc.CalibrationId calibrationId, Integer calibrationData, Integer ctfOffset, byte[] manufacturerSpecificData) {
            this.calibrationId = calibrationId;
            this.calibrationData = calibrationData;
            this.ctfOffset = ctfOffset;
            this.manufacturerSpecificData = manufacturerSpecificData;
        }

        public CalibrationMessage(Parcel in) {
            int incomingVersion = in.readInt();
            if (incomingVersion != 1) {
                LogAnt.i(TAG, "Decoding version " + incomingVersion + " CalibrationMessage parcel with version 1 parser.");
            }

            calibrationId = AntPlusBikePowerPcc.CalibrationId.getValueFromInt(in.readInt());
            calibrationData = (Integer)in.readValue(Integer.class.getClassLoader());
            ctfOffset = (Integer)in.readValue(Integer.class.getClassLoader());
            manufacturerSpecificData = (byte[])(in.readValue(byte[].class.getClassLoader()));
        }

        public void writeToParcel(Parcel dest, int flags) {
            int ipcVersionNumber = 1;
            dest.writeInt(ipcVersionNumber);
            dest.writeInt(calibrationId.getIntValue());
            dest.writeValue(calibrationData);
            dest.writeValue(ctfOffset);
            dest.writeValue(manufacturerSpecificData);
        }

        public int describeContents() {
            return 0;
        }

        private static class CalibrationMessageCreator implements Creator<CalibrationMessage> {
            CalibrationMessageCreator() {
            }

            public CalibrationMessage createFromParcel(Parcel source) {
                return new CalibrationMessage(source);
            }

            public CalibrationMessage[] newArray(int size) {
                return new CalibrationMessage[size];
            }
        }
    }
}
