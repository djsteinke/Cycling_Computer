package rnfive.djs.ant.antplus.pccbase;

import rnfive.djs.ant.antplus.pcc.defines.DeviceState;
import rnfive.djs.ant.antplus.pcc.defines.RequestAccessResult;

public abstract class PccReleaseHandle<T extends AntPluginPcc> {
    protected volatile boolean isClosed = false;
    protected boolean resultSent = false;
    private T receivedDevice = null;
    protected final Object stateLock = new Object();
    private final AntPluginPcc.IPluginAccessResultReceiver<T> mResultReceiver;
    private final AntPluginPcc.IDeviceStateChangeReceiver mStateReceiver;

    protected final AntPluginPcc.IPluginAccessResultReceiver<T> resultSink = new AntPluginPcc.IPluginAccessResultReceiver<T>() {
        public void onResultReceived(T result, RequestAccessResult resultCode, DeviceState initialDeviceState) {
            synchronized(PccReleaseHandle.this.stateLock) {
                if (PccReleaseHandle.this.isClosed) {
                    if (PccReleaseHandle.this.receivedDevice != null) {
                        PccReleaseHandle.this.receivedDevice.handleConnectionBroke("received device after death");
                    }
                } else {
                    PccReleaseHandle.this.receivedDevice = result;
                    PccReleaseHandle.this.resultSent = true;
                    PccReleaseHandle.this.mResultReceiver.onResultReceived(result, resultCode, initialDeviceState);
                }

            }
        }
    };
    protected final AntPluginPcc.IDeviceStateChangeReceiver stateSink = new AntPluginPcc.IDeviceStateChangeReceiver() {
        private boolean deadStateSent = false;

        public void onDeviceStateChange(DeviceState newDeviceState) {
            synchronized(PccReleaseHandle.this.stateLock) {
                if (PccReleaseHandle.this.isActive() && !this.deadStateSent) {
                    if (DeviceState.DEAD.equals(newDeviceState)) {
                        this.deadStateSent = true;
                    }

                    PccReleaseHandle.this.mStateReceiver.onDeviceStateChange(newDeviceState);
                }

            }
        }
    };

    protected PccReleaseHandle(AntPluginPcc.IPluginAccessResultReceiver<T> resultReceiver, AntPluginPcc.IDeviceStateChangeReceiver stateReceiver) {
        this.mResultReceiver = resultReceiver;
        this.mStateReceiver = stateReceiver;
    }

    public void close() {
        synchronized(this.stateLock) {
            if (!this.isClosed) {
                if (this.receivedDevice != null) {
                    this.receivedDevice.releaseToken();
                    this.stateSink.onDeviceStateChange(DeviceState.DEAD);
                }

                if (!this.resultSent) {
                    this.resultSent = true;
                    this.mResultReceiver.onResultReceived(null, RequestAccessResult.USER_CANCELLED, DeviceState.DEAD);
                }

                this.isClosed = true;
                this.requestCancelled();
            }

        }
    }

    protected boolean isActive() {
        synchronized(this.stateLock) {
            return this.resultSent && !this.isClosed;
        }
    }

    protected abstract void requestCancelled();
}
