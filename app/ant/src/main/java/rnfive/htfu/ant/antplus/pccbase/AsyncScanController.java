package rnfive.htfu.ant.antplus.pccbase;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.UUID;

import rnfive.htfu.ant.antplus.pcc.defines.DeviceState;
import rnfive.htfu.ant.antplus.pcc.defines.RequestAccessResult;
import rnfive.htfu.ant.internal.pluginsipc.AntPluginDeviceDbProvider.DeviceDbDeviceInfo;
import rnfive.htfu.ant.utility.log.LogAnt;

public class AsyncScanController<T extends AntPluginPcc> {
    private static final String TAG = AsyncScanController.class.getSimpleName();
    private boolean isRunning = false;
    private boolean isClosed = false;
    private AsyncScanController<T>.AsyncPccReleaseHandle currentRequest = null;
    private boolean shouldShutDown = false;
    private final Object stateLock = new Object();
    protected AsyncScanController.IAsyncScanResultReceiver scanResultReceiver;
    private T retPccObject;

    AsyncScanController(AsyncScanController.IAsyncScanResultReceiver resultReceiver, T pccObject) {
        if (resultReceiver == null) {
            throw new IllegalArgumentException("ScanResultReceiver passed from client was null");
        } else {
            this.scanResultReceiver = resultReceiver;
            this.retPccObject = pccObject;
        }
    }

    protected AsyncScanController(T pccObject) {
        this.retPccObject = pccObject;
    }

    public void closeScanController() {
        synchronized(this.stateLock) {
            if (this.currentRequest != null) {
                this.currentRequest.close();
            } else {
                this.closeScanControllerInternal();
            }

        }
    }

    private void closeScanControllerInternal() {
        synchronized(this.stateLock) {
            if (!this.isClosed) {
                this.isClosed = true;
                if (this.isRunning) {
                    this.retPccObject.stopAsyncScan();
                    this.reportScanFailure(-2);
                } else if (this.retPccObject != null) {
                    this.retPccObject.closePluginConnection();
                }

            }
        }
    }

    private void closeScanControllerDelayed() {
        this.shouldShutDown = true;
        if (this.retPccObject != null) {
            this.retPccObject.closePluginConnection();
        }

    }

    private void reportScanFailure(int resultCode) {
        synchronized(this.stateLock) {
            this.isRunning = false;
            if (this.retPccObject == null) {
                LogAnt.e(TAG, "Unexpected Event: ScanFailure on already null object, code: " + resultCode);
            } else {
                this.retPccObject.closePluginConnection();
                this.retPccObject = null;
                if (this.currentRequest == null) {
                    this.sendFailureToReceiver(RequestAccessResult.getValueFromInt(resultCode));
                }

            }
        }
    }

    protected void sendFailureToReceiver(RequestAccessResult requestAccessResult) {
        this.scanResultReceiver.onSearchStopped(requestAccessResult);
    }

    protected void sendResultToReceiver(Bundle result) {
        AsyncScanResultDeviceInfo newResult = result.getParcelable("parcelable_AsyncScanResultDeviceInfo");
        this.scanResultReceiver.onSearchResult(newResult);
    }

    protected void handleReqAccSuccess(Message msg, T retPccObject, AntPluginPcc.IPluginAccessResultReceiver<T> resultReceiver) {
        int serviceVersion = msg.getData().getInt("int_ServiceVersion", 0);
        Messenger pluginComm = (Messenger)msg.getData().get("msgr_PluginComm");
        UUID accessToken = (UUID)msg.getData().get("uuid_AccessToken");
        int initialStateCode = msg.getData().getInt("int_InitialDeviceStateCode");
        DeviceDbDeviceInfo deviceInfo = msg.getData().getParcelable("parcelable_DeviceDbInfo");
        if (deviceInfo == null) {
            deviceInfo = new DeviceDbDeviceInfo(0);
            deviceInfo.antDeviceNumber = msg.getData().getInt("int_AntDeviceID", -1);
            deviceInfo.visibleName = msg.getData().getString("str_DeviceName");
            deviceInfo.isPreferredDevice = false;
        }

        retPccObject.init(deviceInfo, accessToken, pluginComm, initialStateCode, serviceVersion);
        resultReceiver.onResultReceived(retPccObject, RequestAccessResult.SUCCESS, DeviceState.getValueFromInt(initialStateCode));
        retPccObject.deviceInitializedLatch.countDown();
    }

    public PccReleaseHandle<T> requestDeviceAccess(AsyncScanResultDeviceInfo deviceToConnectTo, AntPluginPcc.IPluginAccessResultReceiver<T> resultReceiver, AntPluginPcc.IDeviceStateChangeReceiver stateReceiver) {
        return this.requestDeviceAccess(deviceToConnectTo, null, resultReceiver, stateReceiver);
    }

    protected PccReleaseHandle<T> requestDeviceAccess(AsyncScanResultDeviceInfo deviceToConnectTo, Bundle customParams, AntPluginPcc.IPluginAccessResultReceiver<T> resultReceiver, AntPluginPcc.IDeviceStateChangeReceiver stateReceiver) {
        if (deviceToConnectTo == null) {
            throw new NullPointerException("deviceToConnectTo parameter was null");
        } else {
            synchronized(this.stateLock) {
                if (this.currentRequest != null) {
                    throw new RuntimeException("Cannot request access while an access request is already in progress");
                } else {
                    AsyncScanController<T>.AsyncPccReleaseHandle ret = new AsyncPccReleaseHandle(resultReceiver, stateReceiver);
                    this.retPccObject.mReleaseHandle = ret;
                    if (!this.isRunning) {
                        LogAnt.e(TAG, "Attempted to connect to a device when the scan was no longer connected");
                        ret.onResultReceived(null, RequestAccessResult.OTHER_FAILURE, DeviceState.DEAD);
                        return ret;
                    } else {
                        this.currentRequest = ret;
                        Messenger commChannel = new Messenger(new ConnectResultHandler(ret, this.retPccObject, this));
                        this.retPccObject.connectToAsyncResult(deviceToConnectTo, commChannel, customParams, ret.stateSink);
                        return ret;
                    }
                }
            }
        }
    }

    protected Handler getScanResultHandler() {
        return new ScanResultHandler(this.retPccObject, this);
    }

    public static String getMissingDependencyPackageName() {
        return AntPluginPcc.lastMissingDependencyPkgName;
    }

    public static String getMissingDependencyName() {
        return AntPluginPcc.lastMissingDependencyName;
    }

    private static class ConnectResultHandler<T extends AntPluginPcc> extends Handler {
        AntPluginPcc.IPluginAccessResultReceiver<T> resultReceiver;
        T retPccObject;
        AsyncScanController<T> controller;

        public ConnectResultHandler(AntPluginPcc.IPluginAccessResultReceiver<T> resultReceiver, T retPccObject, AsyncScanController<T> controller) {
            this.resultReceiver = resultReceiver;
            this.retPccObject = retPccObject;
            this.controller = controller;
        }

        public void handleMessage(Message msg) {
            int resultCode = msg.what;
            msg.getData().setClassLoader(this.getClass().getClassLoader());
            synchronized(this.controller.stateLock) {
                switch(resultCode) {
                    case -7:
                        this.resultReceiver.onResultReceived(null, RequestAccessResult.SEARCH_TIMEOUT, DeviceState.SEARCHING);
                        return;
                    case 0:
                        this.controller.isRunning = false;
                        this.controller.handleReqAccSuccess(msg, this.retPccObject, this.resultReceiver);
                        return;
                    default:
                        RequestAccessResult code = RequestAccessResult.getValueFromInt(resultCode);
                        if (code == RequestAccessResult.UNRECOGNIZED) {
                            LogAnt.e(AsyncScanController.TAG, "RequestAccess failed: Unrecognized return code (need app lib upgrade): " + code.getIntValue() + "!!!");
                        } else {
                            LogAnt.e(AsyncScanController.TAG, "RequestAccess failed: " + code.toString());
                        }

                        this.controller.reportScanFailure(resultCode);
                        this.resultReceiver.onResultReceived(null, code, DeviceState.DEAD);
                }
            }
        }
    }

    private static class ScanResultHandler<T extends AntPluginPcc> extends Handler {
        AsyncScanController<T> controller;
        T retPccObject;

        public ScanResultHandler(T retPccObject, AsyncScanController<T> controller) {
            this.retPccObject = retPccObject;
            this.controller = controller;
        }

        public void handleMessage(Message msg) {
            int resultCode = msg.what;
            msg.getData().setClassLoader(this.getClass().getClassLoader());
            LogAnt.v(AsyncScanController.TAG, "Async scan controller rcv result: " + resultCode);
            switch(resultCode) {
                case -7:
                    return;
                case -6:
                case -4:
                case -3:
                case -2:
                case -1:
                case 1:
                default:
                    RequestAccessResult code = RequestAccessResult.getValueFromInt(resultCode);
                    if (code == RequestAccessResult.UNRECOGNIZED) {
                        LogAnt.e(AsyncScanController.TAG, "RequestAccess failed: Unrecognized return code (need app lib upgrade): " + code.getIntValue() + "!!!");
                    } else {
                        LogAnt.e(AsyncScanController.TAG, "RequestAccess failed: " + code.toString());
                    }

                    this.controller.reportScanFailure(resultCode);
                    return;
                case -5:
                    Bundle b = msg.getData();
                    AntPluginPcc.lastMissingDependencyPkgName = b.getString("string_DependencyPackageName");
                    AntPluginPcc.lastMissingDependencyName = b.getString("string_DependencyName");
                    LogAnt.e(AsyncScanController.TAG, "requestAccess failed, " + AntPluginPcc.lastMissingDependencyPkgName + " not installed.");
                    this.controller.reportScanFailure(resultCode);
                    return;
                case 0:
                    Bundle retInfo = msg.getData();
                    this.retPccObject.mAccessToken = (UUID)retInfo.getSerializable("uuid_AccessToken");
                    this.retPccObject.mPluginMsgr = (Messenger)retInfo.getParcelable("msgr_PluginComm");
                    synchronized(this.controller.stateLock) {
                        this.controller.isRunning = true;
                        if (this.controller.isClosed) {
                            this.controller.closeScanController();
                        }

                        return;
                    }
                case 2:
                    this.controller.sendResultToReceiver(msg.getData());
            }
        }
    }

    private class AsyncPccReleaseHandle extends PccReleaseHandle<T> implements AntPluginPcc.IPluginAccessResultReceiver<T> {
        protected AsyncPccReleaseHandle(AntPluginPcc.IPluginAccessResultReceiver<T> resultReceiver, AntPluginPcc.IDeviceStateChangeReceiver stateReceiver) {
            super(resultReceiver, stateReceiver);
        }

        protected void requestCancelled() {
            synchronized(AsyncScanController.this.stateLock) {
                if (AsyncScanController.this.currentRequest != null) {
                    AsyncScanController.this.closeScanControllerDelayed();
                } else {
                    AsyncScanController.this.closeScanControllerInternal();
                }

            }
        }

        public void onResultReceived(T result, RequestAccessResult resultCode, DeviceState initialDeviceState) {
            synchronized(this.stateLock) {
                synchronized(AsyncScanController.this.stateLock) {
                    if (!this.resultSent && resultCode == RequestAccessResult.SEARCH_TIMEOUT || resultCode == RequestAccessResult.SUCCESS) {
                        AsyncScanController.this.currentRequest = null;
                    }

                    if (AsyncScanController.this.shouldShutDown) {
                        AsyncScanController.this.closeScanControllerInternal();
                    }
                }

                this.resultSink.onResultReceived(result, resultCode, initialDeviceState);
            }
        }
    }

    public static class AsyncScanResultDeviceInfo implements Parcelable {
        public static final String KEY_DEFAULT_ASYNCSCANRESULTKEY = "parcelable_AsyncScanResultDeviceInfo";
        private final int ipcVersionNumber = 1;
        public final UUID scanResultInternalIdentifier;
        private final boolean isAlreadyConnected;
        private final DeviceDbDeviceInfo deviceInfo;
        public static final Creator<AsyncScanResultDeviceInfo> CREATOR = new Creator<AsyncScanResultDeviceInfo>() {
            public AsyncScanResultDeviceInfo createFromParcel(Parcel in) {
                return new AsyncScanResultDeviceInfo(in);
            }

            public AsyncScanResultDeviceInfo[] newArray(int size) {
                return new AsyncScanResultDeviceInfo[size];
            }
        };

        public AsyncScanResultDeviceInfo(UUID scanResultIdentifier, DeviceDbDeviceInfo deviceInfo, boolean isAlreadyConnected) {
            this.scanResultInternalIdentifier = scanResultIdentifier;
            this.deviceInfo = deviceInfo;
            this.isAlreadyConnected = isAlreadyConnected;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.ipcVersionNumber);
            dest.writeValue(this.scanResultInternalIdentifier);
            dest.writeValue(this.isAlreadyConnected);
            dest.writeParcelable(this.deviceInfo, flags);
        }

        public AsyncScanResultDeviceInfo(Parcel in) {
            int incomingVersion = in.readInt();
            if (incomingVersion != 1) {
                LogAnt.i(AsyncScanController.TAG, "Decoding version " + incomingVersion + " AsyncScanResultDeviceInfo parcel with version 1 parser.");
            }

            this.scanResultInternalIdentifier = (UUID)in.readValue((ClassLoader)null);
            this.isAlreadyConnected = (Boolean)in.readValue((ClassLoader)null);
            this.deviceInfo = in.readParcelable(this.getClass().getClassLoader());
        }

        public String getDeviceDisplayName() {
            return this.deviceInfo.visibleName;
        }

        public int getAntDeviceNumber() {
            return this.deviceInfo.antDeviceNumber;
        }

        public boolean isUserRecognizedDevice() {
            return this.deviceInfo.device_dbId != null;
        }

        public boolean isUserPreferredDeviceForPlugin() {
            return this.deviceInfo.isPreferredDevice;
        }

        public boolean isAlreadyConnected() {
            return this.isAlreadyConnected;
        }
    }

    public interface IAsyncScanResultReceiver {
        void onSearchStopped(RequestAccessResult var1);

        void onSearchResult(AsyncScanResultDeviceInfo var1);
    }
}
