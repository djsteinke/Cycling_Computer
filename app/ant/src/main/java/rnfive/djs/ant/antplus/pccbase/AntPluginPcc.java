package rnfive.djs.ant.antplus.pccbase;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

import rnfive.djs.ant.antplus.pcc.defines.DeviceState;
import rnfive.djs.ant.antplus.pcc.defines.RequestAccessResult;
import rnfive.djs.ant.internal.pluginsipc.AntPluginDeviceDbProvider.DeviceDbDeviceInfo;
import rnfive.djs.ant.utility.log.LogAnt;

public abstract class AntPluginPcc {
    private static final String TAG = AntPluginPcc.class.getSimpleName();
    public static final String PATH_ANTPLUS_PLUGINS_PKG = "rn5.djs.ant.antplus";
    private static final long CLOSE_CONNECTION_TIMEOUT_MS = 500L;
    static volatile String lastMissingDependencyPkgName = "";
    static volatile String lastMissingDependencyName = "";
    ServiceConnection serviceBindConn;
    HandlerThread mPluginMsgHandlerThread = new HandlerThread("PluginPCCMsgHandler");
    volatile Handler mPluginMsgHandler;
    Handler.Callback mPluginMsgHandlerCb = msg -> {
        LogAnt.v(AntPluginPcc.TAG, "Plugin Msg Handler received: " + msg.what + ", " + msg.arg1);
        if (AntPluginPcc.this.mPluginCommLock.tryLock()) {
            try {
                AntPluginPcc.this.handleNonCmdPluginMessage(msg);
            } finally {
                AntPluginPcc.this.mPluginCommLock.unlock();
            }
        } else {
            try {
                AntPluginPcc.this.mPluginCommMsgExch.exchange(msg);
                AntPluginPcc.this.mPluginCommProcessingBarrier.await();
            } catch (BrokenBarrierException var6) {
                AntPluginPcc.this.handleConnectionBroke("BrokenBarrierException in mPluginMsgHandler trying to fwd message " + msg.what);
                return true;
            } catch (InterruptedException var7) {
                AntPluginPcc.this.handleConnectionBroke("InterruptedException in mPluginMsgHandler trying to fwd message " + msg.what);
                Thread.currentThread().interrupt();
                return true;
            }
        }

        return true;
    };
    HandlerThread mPluginEventHandlerThread = new HandlerThread("PluginPCCEventHandler");
    volatile Handler mPluginEventHandler;
    Handler.Callback mPluginEventHandlerCb = msg -> {
        PccReleaseHandle<?> temp = AntPluginPcc.this.mReleaseHandle;
        if (temp != null) {
            try {
                AntPluginPcc.this.deviceInitializedLatch.await();
                synchronized (temp.stateLock) {
                    if (!temp.isActive()) {
                        return true;
                    }

                    switch (msg.what) {
                        case 1:
                            AntPluginPcc.this.handlePluginEvent(msg);
                            break;
                        case 3:
                            int newState = msg.arg1;
                            AntPluginPcc.this.mCachedState = newState;
                            LogAnt.v(AntPluginPcc.TAG, "State event: " + newState);
                            if (newState == -100) {
                                AntPluginPcc.this.handleConnectionBroke("Device dead");
                            } else {
                                AntPluginPcc.this.mStateChangeReceiver.onDeviceStateChange(DeviceState.getValueFromInt(newState));
                            }
                            break;
                        default:
                            LogAnt.w(AntPluginPcc.TAG, "Unrecognized plugin event received: " + msg.arg1);
                    }
                }
            } catch (InterruptedException var7) {
                LogAnt.i(AntPluginPcc.TAG, "Plugin event thread interrupted while waiting for initialization to complete.");
                Thread.currentThread().interrupt();
            }

        }
        return true;
    };
    Messenger mReqAccessMessenger;
    UUID mAccessToken;
    Messenger mPluginMsgr;
    private final ReentrantLock mPluginCommLock = new ReentrantLock();
    Exchanger<Message> mPluginCommMsgExch = new Exchanger();
    CyclicBarrier mPluginCommProcessingBarrier = new CyclicBarrier(2);
    boolean isInitialized = false;
    CountDownLatch deviceInitializedLatch = new CountDownLatch(1);
    Context mOwnerContext;
    DeviceDbDeviceInfo deviceInfo;
    Integer mCachedState = null;
    private Thread mCurrentCmdThread;
    protected int reportedServiceVersion;
    protected boolean supportsRssiEvent;
    protected IDeviceStateChangeReceiver mStateChangeReceiver;
    protected volatile PccReleaseHandle<?> mReleaseHandle;
    private boolean isReleased = false;
    private final Object mReleaseLock = new Object();
    private boolean mIsPluginServiceBound = false;
    private final  Object mPluginServiceBindChange_LOCK = new Object();

    protected abstract Intent getServiceBindIntent();

    protected abstract int getRequiredServiceVersionForBind();

    protected abstract void handlePluginEvent(Message var1);

    protected abstract String getPluginPrintableName();

    public static int getInstalledPluginsVersionNumber(Context currentContext) {
        PackageManager pm = currentContext.getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(0);
        Iterator var3 = packages.iterator();

        PackageInfo packageInfo;
        do {
            if (!var3.hasNext()) {
                return -1;
            }

            packageInfo = (PackageInfo)var3.next();
        } while(!packageInfo.packageName.equals("com.dsi.ant.plugins.antplus"));

        if (!packageInfo.applicationInfo.enabled) {
            return -2;
        } else {
            return packageInfo.versionCode;
        }
    }

    public static String getInstalledPluginsVersionString(Context currentContext) {
        PackageManager pm = currentContext.getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(0);
        Iterator var3 = packages.iterator();

        PackageInfo packageInfo;
        do {
            if (!var3.hasNext()) {
                return null;
            }

            packageInfo = (PackageInfo)var3.next();
        } while(!packageInfo.packageName.equals("com.dsi.ant.plugins.antplus"));

        return packageInfo.versionName;
    }

    public static boolean startPluginManagerActivity(Activity activity) {
        if (getInstalledPluginsVersionNumber(activity) > 0) {
            Intent it = new Intent();
            it.setClassName("com.dsi.ant.plugins.antplus", "com.dsi.ant.plugins.antplus.utility.db.Activity_PluginMgrDashboard");
            activity.startActivity(it);
            return true;
        } else {
            return false;
        }
    }

    public static String getMissingDependencyPackageName() {
        return lastMissingDependencyPkgName;
    }

    public static String getMissingDependencyName() {
        return lastMissingDependencyName;
    }

    protected static <T extends AntPluginPcc> PccReleaseHandle<T> requestAccess_Helper_SearchActivity(Activity foregroundActivity, Context bindingContext, boolean skipPreferredSearch, int searchProximityThreshold, T retPccObject, AntPluginPcc.IPluginAccessResultReceiver<T> resultReceiver, AntPluginPcc.IDeviceStateChangeReceiver stateReceiver) {
        Bundle b = new Bundle();
        b.putInt("int_RequestAccessMode", 1);
        b.putBoolean("b_ForceManualSelect", skipPreferredSearch);
        b.putInt("int_ProximityBin", searchProximityThreshold);
        return requestAccess_Helper_Main(bindingContext, b, retPccObject, new AntPluginPcc.RequestAccessResultHandler_UI(foregroundActivity), resultReceiver, stateReceiver);
    }

    protected static <T extends AntPluginPcc> PccReleaseHandle<T> requestAccess_Helper_AsyncSearchByDevNumber(Context bindingContext, int antDeviceNumber, int searchProximityThreshold, T retPccObject, AntPluginPcc.IPluginAccessResultReceiver<T> resultReceiver, AntPluginPcc.IDeviceStateChangeReceiver stateReceiver) {
        Bundle b = new Bundle();
        b.putInt("int_RequestAccessMode", 3);
        b.putInt("int_AntDeviceID", antDeviceNumber);
        b.putInt("int_ProximityBin", searchProximityThreshold);
        return requestAccess_Helper_Main(bindingContext, b, retPccObject, new AntPluginPcc.RequestAccessResultHandler_AsyncSearchByDevNumber(), resultReceiver, stateReceiver);
    }

    protected static <T extends AntPluginPcc> AsyncScanController<T> requestAccess_Helper_AsyncScanController(Context bindingContext, int searchProximityThreshold, T retPccObject, AsyncScanController.IAsyncScanResultReceiver scanResultReceiver) {
        AsyncScanController<T> controller = new AsyncScanController(scanResultReceiver, retPccObject);
        return requestAsyncScan_Helper_SubMain(bindingContext, searchProximityThreshold, new Bundle(), retPccObject, controller);
    }

    <T extends AntPluginPcc> void connectToAsyncResult(AsyncScanController.AsyncScanResultDeviceInfo deviceToConnectTo, Messenger resultMessenger, Bundle connectParams, AntPluginPcc.IDeviceStateChangeReceiver stateReceiver) {
        this.mStateChangeReceiver = stateReceiver;
        Bundle connectData = connectParams;
        if (connectParams == null) {
            connectData = new Bundle();
        }

        connectData.putParcelable("parcelable_AsyncScanResultDeviceInfo", deviceToConnectTo);
        connectData.putParcelable("msgr_ReqAccResultReceiver", resultMessenger);
        Message ret = this.sendPluginCommand(10100, connectData);
        if (ret == null) {
            LogAnt.e(TAG, "connectToAsyncResult died in sendPluginCommand()");
            Message failMsg = Message.obtain();
            failMsg.what = -4;

            try {
                resultMessenger.send(failMsg);
            } catch (RemoteException var9) {
                this.handleConnectionBroke("Remote exception sending async connect failure msg to client");
            }
        } else {
            if (ret.arg1 != 0) {
                throw new RuntimeException("Request to connectToAsync Result cmd failed with code " + ret.arg1);
            }

            ret.recycle();
        }

    }

    protected static <T extends AntPluginPcc> PccReleaseHandle<T> requestAccess_Helper_Main(Context bindingContext, Bundle reqParams, T retPccObject, AntPluginPcc.RequestAccessResultHandler<T> reqAccessResultHandler, AntPluginPcc.IPluginAccessResultReceiver<T> resultReceiver, AntPluginPcc.IDeviceStateChangeReceiver stateReceiver) {
        if (resultReceiver != null && stateReceiver != null) {
            PccReleaseHandle<T> ret = retPccObject.new StandardReleaseHandle(resultReceiver, stateReceiver);
            retPccObject.mReleaseHandle = ret;
            retPccObject.mStateChangeReceiver = ret.stateSink;
            reqAccessResultHandler.setReturnInfo(retPccObject, ret.resultSink);
            requestAccess_Helper_SubMain(bindingContext, reqParams, retPccObject, reqAccessResultHandler);
            return ret;
        } else {
            throw new IllegalArgumentException("Invalid argument: " + (resultReceiver == null ? "resultReceiver " : "stateReceiver ") + " is null ");
        }
    }

    protected static <T extends AntPluginPcc> AsyncScanController<T> requestAsyncScan_Helper_SubMain(Context bindingContext, int searchProximityThreshold, Bundle reqParams, T retPccObject, AsyncScanController<T> controller) {
        if (getInstalledPluginsVersionNumber(bindingContext) < 10800) {
            LogAnt.e(TAG, "Binding to plugin failed, version requirement not met for async scan controller mode");
            Intent it = retPccObject.getServiceBindIntent();
            lastMissingDependencyPkgName = it.getComponent().getPackageName();
            lastMissingDependencyName = retPccObject.getPluginPrintableName() + " minimum v.10800";
            controller.scanResultReceiver.onSearchStopped(RequestAccessResult.DEPENDENCY_NOT_INSTALLED);
            return null;
        } else {
            reqParams.putInt("int_RequestAccessMode", 2);
            reqParams.putInt("int_ProximityBin", searchProximityThreshold);
            requestAccess_Helper_SubMain(bindingContext, reqParams, retPccObject, controller.getScanResultHandler());
            return controller;
        }
    }

    protected static <T extends AntPluginPcc> void requestAccess_Helper_SubMain(Context bindingContext, Bundle reqParams, T retPccObject, Handler resultHandler) {
        if (resultHandler == null) {
            throw new IllegalArgumentException("resultHandler passed from client was null");
        } else {
            String appNamePkg = bindingContext.getPackageName();
            ApplicationInfo ai = bindingContext.getApplicationInfo();
            String appNameLabel = bindingContext.getPackageManager().getApplicationLabel(ai).toString();
            reqParams.putString("str_ApplicationNamePackage", appNamePkg);
            reqParams.putString("str_ApplicationNameTitle", appNameLabel);
            if (!reqParams.containsKey("int_RssiMode")) {
                reqParams.putInt("int_RssiMode", 1);
            }

            retPccObject.bindAndRequest(bindingContext, reqParams, resultHandler);
        }
    }

    public AntPluginPcc() {
        this.mPluginEventHandlerThread.start();
        this.mPluginEventHandler = new Handler(this.mPluginEventHandlerThread.getLooper(), this.mPluginEventHandlerCb);
        this.mPluginMsgHandlerThread.start();
        this.mPluginMsgHandler = new Handler(this.mPluginMsgHandlerThread.getLooper(), this.mPluginMsgHandlerCb);
    }

    protected void bindAndRequest(Context bindingContext, final Bundle b, Handler resultHandler) {
        this.mOwnerContext = bindingContext;
        Messenger commChannel = new Messenger(resultHandler);
        b.putParcelable("msgr_PluginMsgHandler", this.getPluginMsgReceiver());
        b.putParcelable("msgr_ReqAccResultReceiver", commChannel);
        LogAnt.setVersion("BBD30800");

        try {
            Context pluginContext = this.mOwnerContext.createPackageContext("com.dsi.ant.plugins.antplus", Context.CONTEXT_RESTRICTED);
            LogAnt.getDebugLevel(pluginContext);
        } catch (PackageManager.NameNotFoundException var11) {
            LogAnt.e(TAG, "Unable to configure logging, plugins package not found: " + var11);
        }

        b.putInt("int_PluginLibVersion", 30800);
        b.putString("string_PluginLibVersion", "3.8.0");
        b.putInt("more", 1);
        Intent it = this.getServiceBindIntent();
        PackageInfo targetService = null;
        PackageManager pm = this.mOwnerContext.getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(0);
        Iterator var9 = packages.iterator();

        while(var9.hasNext()) {
            PackageInfo packageInfo = (PackageInfo)var9.next();
            if (packageInfo.packageName.equals(it.getComponent().getPackageName())) {
                targetService = packageInfo;
                break;
            }
        }

        Messenger resultMsgr;
        if (targetService == null) {
            LogAnt.e(TAG, "Binding to plugin failed, not installed");
            resultMsgr = (Messenger)b.getParcelable("msgr_ReqAccResultReceiver");
            this.sendDependencyNotInstalledMessage(resultMsgr, it.getComponent().getPackageName(), "ANT+ Plugins Service");
        } else if (targetService.versionCode < this.getRequiredServiceVersionForBind()) {
            LogAnt.e(TAG, "Binding to plugin failed, version requirement not met");
            resultMsgr = (Messenger)b.getParcelable("msgr_ReqAccResultReceiver");
            this.sendDependencyNotInstalledMessage(resultMsgr, it.getComponent().getPackageName(), "ANT+ Plugins Service minimum v." + this.getRequiredServiceVersionForBind());
        } else {
            this.serviceBindConn = new ServiceConnection() {
                public void onServiceConnected(ComponentName arg0, IBinder arg1) {
                    synchronized(AntPluginPcc.this.mReleaseLock) {
                        if (!AntPluginPcc.this.isReleased) {
                            AntPluginPcc.this.mReqAccessMessenger = new Messenger(arg1);
                            Message msg = Message.obtain();
                            msg.what = 0;
                            msg.setData(b);

                            try {
                                AntPluginPcc.this.mReqAccessMessenger.send(msg);
                            } catch (RemoteException var7) {
                                AntPluginPcc.this.notifyBindAndRequestFailed(b);
                            }

                        }
                    }
                }

                public void onServiceDisconnected(ComponentName arg0) {
                    if (!AntPluginPcc.this.isInitialized) {
                        AntPluginPcc.this.notifyBindAndRequestFailed(b);
                    } else {
                        AntPluginPcc.this.handleConnectionBroke("OnServiceDisconnected fired");
                    }

                }
            };
            this.bindPluginService(it, b);
        }
    }

    private void sendDependencyNotInstalledMessage(Messenger resultMsgr, String dpndcyPkgName, String dpndcyDisplayName) {
        Message msgErr = Message.obtain();
        msgErr.what = -5;
        Bundle s = new Bundle();
        s.putString("string_DependencyPackageName", dpndcyPkgName);
        s.putString("string_DependencyName", dpndcyDisplayName);
        msgErr.setData(s);

        try {
            resultMsgr.send(msgErr);
        } catch (RemoteException var7) {
            this.handleConnectionBroke("Remote exception sending plugin 'dependency not installed' msg to client");
        }

    }

    private Messenger getPluginMsgReceiver() {
        return new Messenger(this.mPluginMsgHandler);
    }

    protected void handleNonCmdPluginMessage(Message msg) {
        Handler h = this.mPluginEventHandler;
        if (h != null) {
            Message m = h.obtainMessage(msg.what, msg.arg1, msg.arg2, msg.obj);
            m.setData(msg.getData());
            m.replyTo = msg.replyTo;
            h.sendMessage(m);
        }

    }

    void init(DeviceDbDeviceInfo devInfo, UUID accessToken, Messenger pluginMsgr, int initialDeviceState, int reportedServiceVersion) {
        this.deviceInfo = devInfo;
        this.mAccessToken = accessToken;
        this.mPluginMsgr = pluginMsgr;
        this.reportedServiceVersion = reportedServiceVersion;
        if (this.mCachedState == null) {
            this.mCachedState = initialDeviceState;
        }

        this.isInitialized = true;
    }

    public String getDeviceName() {
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

    public DeviceState getCurrentDeviceState() {
        return DeviceState.getValueFromInt(this.mCachedState);
    }

    protected Message createCmdMsg(int cmdCode, Bundle msgData) {
        Message cmdMsg = Message.obtain();
        cmdMsg.what = cmdCode;
        if (msgData == null) {
            msgData = new Bundle();
        }

        msgData.putSerializable("uuid_AccessToken", this.mAccessToken);
        cmdMsg.setData(msgData);
        return cmdMsg;
    }

    protected Message sendPluginCommand(int cmdCode, Bundle msgData) {
        return this.sendPluginCommandInternal(this.createCmdMsg(cmdCode, msgData));
    }

    protected Message sendPluginCommand(Message cmdMsg) {
        Bundle msgData = cmdMsg.getData();
        if (msgData == null) {
            msgData = new Bundle();
            cmdMsg.setData(msgData);
        }

        msgData.putSerializable("uuid_AccessToken", this.mAccessToken);
        return this.sendPluginCommandInternal(cmdMsg);
    }

    private Message sendPluginCommandInternal(Message cmdMsg) {
        synchronized(this.mPluginCommLock) {
            int commandMessage = cmdMsg.what;
            boolean success = false;
            this.mCurrentCmdThread = Thread.currentThread();
            if (this.mPluginMsgr == null) {
                return null;
            } else {
                try {
                    if (!this.mPluginCommLock.tryLock(7000L, TimeUnit.MILLISECONDS)) {
                        throw new TimeoutException();
                    }
                } catch (InterruptedException var43) {
                    this.handleConnectionBroke("InterruptedException obtaining mPluginCommLock in sendPluginCommand on message " + commandMessage);
                    Thread.currentThread().interrupt();
                    return null;
                } catch (TimeoutException var44) {
                    this.handleConnectionBroke("TimeoutException obtaining mPluginCommLock in sendPluginCommand on message " + commandMessage);
                    return null;
                }

                try {
                    Message ret;
                    try {
                        this.mPluginMsgr.send(cmdMsg);

                        while(true) {
                            Message response;
                            Message var7;
                            try {
                                response = (Message)this.mPluginCommMsgExch.exchange(null, 5L, TimeUnit.SECONDS);
                            } catch (InterruptedException var37) {
                                this.handleConnectionBroke("InterruptedException in sendPluginCommand (at mPluginCommMsgExch.exchange()) on message " + commandMessage);
                                Thread.currentThread().interrupt();
                                var7 = null;
                                return var7;
                            } catch (TimeoutException var38) {
                                this.handleConnectionBroke("TimeoutException in sendPluginCommand (at mPluginCommMsgExch.exchange()) on message " + commandMessage);
                                var7 = null;
                                return var7;
                            }

                            if (response.what == commandMessage) {
                                ret = Message.obtain(response);
                                success = true;
                                var7 = ret;
                                return var7;
                            }

                            this.handleNonCmdPluginMessage(response);

                            try {
                                this.mPluginCommProcessingBarrier.await();
                            } catch (BrokenBarrierException var39) {
                                this.handleConnectionBroke("BrokenBarrierException in sendPluginCommand (at non-success mPluginCommProcessingBarrier) on message " + commandMessage);
                                var7 = null;
                                return var7;
                            } catch (InterruptedException var40) {
                                this.handleConnectionBroke("InterruptedException in sendPluginCommand (at non-success mPluginCommProcessingBarrier) on message " + commandMessage);
                                Thread.currentThread().interrupt();
                                var7 = null;
                                return var7;
                            }
                        }
                    } catch (RemoteException var41) {
                        this.handleConnectionBroke("RemoteException sending message " + commandMessage + " to plugin");
                        ret = null;
                        return ret;
                    }
                } finally {
                    this.mPluginCommLock.unlock();
                    if (success) {
                        try {
                            this.mPluginCommProcessingBarrier.await();
                        } catch (BrokenBarrierException var35) {
                            this.handleConnectionBroke("BrokenBarrierException in sendPluginCommand finally on message " + commandMessage);
                            return null;
                        } catch (InterruptedException var36) {
                            this.handleConnectionBroke("InterruptedException in sendPluginCommand finally on message " + commandMessage);
                            Thread.currentThread().interrupt();
                            return null;
                        }
                    }

                }
            }
        }
    }

    protected boolean subscribeToEvent(int eventCode) {
        Message subCmdMsg = this.createCmdMsg(10000, (Bundle)null);
        subCmdMsg.arg1 = eventCode;
        Message ret = this.sendPluginCommand(subCmdMsg);
        if (ret == null) {
            LogAnt.e(TAG, "subscribeToEvent died in sendPluginCommand()");
            return false;
        } else if (ret.arg1 != 0) {
            LogAnt.e(TAG, "Subscribing to event " + eventCode + " failed with code " + ret.arg1);
            ret.recycle();
            return false;
        } else {
            ret.recycle();
            return true;
        }
    }

    protected void unsubscribeFromEvent(int eventCode) {
        Message unsubCmdMsg = this.createCmdMsg(10001, (Bundle)null);
        unsubCmdMsg.arg1 = eventCode;
        Message ret = this.sendPluginCommand(unsubCmdMsg);
        if (ret == null) {
            LogAnt.e(TAG, "unsubscribeFromEvent died in sendPluginCommand()");
        } else if (ret.arg1 != 0) {
            throw new RuntimeException("Unsubscribing to event " + eventCode + " failed with code " + unsubCmdMsg.arg1);
        } else {
            ret.recycle();
        }
    }

    public void releaseAccess() {
        this.mReleaseHandle.close();
    }

    private void notifyBindAndRequestFailed(Bundle requestAccessParams) {
        this.closePluginConnection();
        Messenger resultMsgr = (Messenger)requestAccessParams.getParcelable("msgr_ReqAccResultReceiver");
        Message msgErr = Message.obtain();
        msgErr.what = -4;

        try {
            resultMsgr.send(msgErr);
        } catch (RemoteException var5) {
            LogAnt.e(TAG, "Remote exception sending failure msg to client");
        }

    }

    void handleConnectionBroke(String errorMessage) {
        LogAnt.w(TAG, "ConnectionDied: " + errorMessage);
        if (this.mReleaseHandle != null && !this.mReleaseHandle.isClosed) {
            this.releaseToken();
            this.mStateChangeReceiver.onDeviceStateChange(DeviceState.DEAD);
        }
    }

    void stopAsyncScan() {
        this.sendReleaseCommand(10101);
    }

    void releaseToken() {
        synchronized(this.mPluginCommLock) {
            this.mCachedState = -100;

            try {
                this.sendReleaseCommand(10002);
            } finally {
                this.closePluginConnection();
            }

        }
    }

    private void sendReleaseCommand(int releaseCode) {
        synchronized(this.mPluginCommLock) {
            try {
                if (this.mPluginMsgr != null) {
                    this.mPluginMsgr.send(this.createCmdMsg(releaseCode, (Bundle)null));
                }
            } catch (RemoteException var5) {
                LogAnt.e(TAG, "RemoteException, unable to cleanly release (cmd " + releaseCode + ")");
            }

        }
    }

    protected void closePluginConnection() {
        Messenger reqAccessMessenger;
        synchronized(this.mReleaseLock) {
            if (this.isReleased) {
                return;
            }

            this.isReleased = true;
            reqAccessMessenger = this.mReqAccessMessenger;
        }

        if (reqAccessMessenger != null) {
            final CountDownLatch latch = new CountDownLatch(1);
            Bundle b = new Bundle();
            b.putParcelable("msgr_PluginMsgHandler", this.getPluginMsgReceiver());
            b.putParcelable("msgr_ReqAccResultReceiver", new Messenger(new Handler(this.mPluginMsgHandlerThread.getLooper()) {
                public void handleMessage(Message msg) {
                    latch.countDown();
                }
            }));
            if (this.mOwnerContext != null) {
                String appNamePkg = this.mOwnerContext.getPackageName();
                ApplicationInfo ai = this.mOwnerContext.getApplicationInfo();
                String appNameLabel = this.mOwnerContext.getPackageManager().getApplicationLabel(ai).toString();
                b.putString("str_ApplicationNamePackage", appNamePkg);
                b.putString("str_ApplicationNameTitle", appNameLabel);
            }

            b.putInt("int_PluginLibVersion", 30800);
            b.putString("string_PluginLibVersion", "3.8.0");
            Message closeReq = Message.obtain();
            closeReq.what = 1;
            closeReq.setData(b);

            try {
                reqAccessMessenger.send(closeReq);
                latch.await(500L, TimeUnit.MILLISECONDS);
            } catch (RemoteException var11) {
            } catch (InterruptedException var12) {
            }
        }

        this.mPluginMsgHandlerThread.quit();

        try {
            this.mPluginMsgHandlerThread.join(1000L);
        } catch (InterruptedException var10) {
            LogAnt.e(TAG, "Plugin Msg Handler thread failed to shut down cleanly, InterruptedException");
            Thread.currentThread().interrupt();
        }

        this.mPluginEventHandler = null;
        this.mPluginEventHandlerThread.quit();

        try {
            this.mPluginEventHandlerThread.join(1000L);
        } catch (InterruptedException var9) {
            LogAnt.e(TAG, "Plugin Event Handler thread failed to shut down cleanly, InterruptedException");
            Thread.currentThread().interrupt();
        }

        this.unbindPluginService();
        if (this.mPluginCommLock.tryLock()) {
            this.mPluginCommLock.unlock();
        } else {
            this.mCurrentCmdThread.interrupt();
        }

        synchronized(this.mPluginCommLock) {
            this.mPluginMsgr = null;
        }
    }

    private void bindPluginService(Intent it, Bundle b) {
        synchronized(this.mPluginServiceBindChange_LOCK) {
            if (!this.mIsPluginServiceBound) {
                this.mIsPluginServiceBound = true;
                if (!this.mOwnerContext.bindService(it, this.serviceBindConn, Context.BIND_AUTO_CREATE)) {
                    LogAnt.e(TAG, "Binding to plugin failed");
                    this.notifyBindAndRequestFailed(b);
                }
            }

        }
    }

    private void unbindPluginService() {
        synchronized(this.mPluginServiceBindChange_LOCK) {
            if (this.mIsPluginServiceBound) {
                try {
                    this.mOwnerContext.unbindService(this.serviceBindConn);
                } catch (IllegalArgumentException var4) {
                    LogAnt.e(TAG, "Unexpected error unbinding service, " + var4);
                }

                this.mIsPluginServiceBound = false;
            }

        }
    }

    protected static class RequestAccessResultHandler_AsyncSearchByDevNumber<T extends AntPluginPcc> extends AntPluginPcc.RequestAccessResultHandler<T> {
        protected RequestAccessResultHandler_AsyncSearchByDevNumber() {
        }

        public boolean handleRequestAccessResult(Message msg) {
            if (msg.what == -7) {
                this.handleRequestAccessFailed("Search for device timed out.", RequestAccessResult.SEARCH_TIMEOUT);
                return true;
            } else {
                return super.handleRequestAccessResult(msg);
            }
        }
    }

    protected static class RequestAccessResultHandler_UI<T extends AntPluginPcc> extends AntPluginPcc.RequestAccessResultHandler<T> {
        private Activity foregroundActivity;

        public RequestAccessResultHandler_UI(Activity foregroundActivity) {
            this.foregroundActivity = foregroundActivity;
        }

        public boolean handleRequestAccessResult(Message msg) {
            if (msg.what == 1) {
                Bundle b = msg.getData();
                Intent i = (Intent)b.getParcelable("intent_ActivityToLaunch");
                if (!this.retPccObject.mReleaseHandle.isClosed) {
                    this.foregroundActivity.startActivity(i);
                }

                return true;
            } else {
                return super.handleRequestAccessResult(msg);
            }
        }
    }

    protected static class RequestAccessResultHandler<T extends AntPluginPcc> extends Handler {
        protected T retPccObject;
        protected AntPluginPcc.IPluginAccessResultReceiver<T> resultReceiver;

        public RequestAccessResultHandler() {
        }

        protected void setReturnInfo(T retPccObject, AntPluginPcc.IPluginAccessResultReceiver<T> resultReceiver) {
            this.retPccObject = retPccObject;
            this.resultReceiver = resultReceiver;
        }

        public void handleMessage(Message msg) {
            LogAnt.v(AntPluginPcc.TAG, "ReqAcc Handler received: " + msg.what);
            msg.getData().setClassLoader(this.getClass().getClassLoader());
            if (!this.handleRequestAccessResult(msg)) {
                RequestAccessResult code = RequestAccessResult.getValueFromInt(msg.what);
                if (code == RequestAccessResult.UNRECOGNIZED) {
                    this.handleRequestAccessFailed("Unrecognized return code (need app lib upgrade): " + msg.what + "!!!", code);
                } else {
                    this.handleRequestAccessFailed(code.toString(), code);
                }
            }

        }

        public boolean handleRequestAccessResult(Message msg) {
            int resultCode = msg.what;
            Bundle b;
            switch(resultCode) {
                case -5:
                    b = msg.getData();
                    AntPluginPcc.lastMissingDependencyPkgName = b.getString("string_DependencyPackageName");
                    AntPluginPcc.lastMissingDependencyName = b.getString("string_DependencyName");
                    this.handleRequestAccessFailed("Missing Dependency: " + AntPluginPcc.lastMissingDependencyPkgName + " not installed.", RequestAccessResult.DEPENDENCY_NOT_INSTALLED);
                    return true;
                case 0:
                    b = msg.getData();
                    int serviceVersion = b.getInt("int_ServiceVersion", 0);
                    Messenger pluginComm = (Messenger)b.getParcelable("msgr_PluginComm");
                    UUID accessToken = (UUID)b.get("uuid_AccessToken");
                    int initialStateCode = b.getInt("int_InitialDeviceStateCode");
                    DeviceDbDeviceInfo deviceInfo = (DeviceDbDeviceInfo)b.getParcelable("parcelable_DeviceDbInfo");
                    if (deviceInfo == null) {
                        deviceInfo = new DeviceDbDeviceInfo(0);
                        deviceInfo.antDeviceNumber = b.getInt("int_AntDeviceID", -1);
                        deviceInfo.visibleName = b.getString("str_DeviceName");
                        deviceInfo.isPreferredDevice = false;
                    }

                    this.retPccObject.supportsRssiEvent = b.getBoolean("bool_RssiSupport", false);
                    this.retPccObject.init(deviceInfo, accessToken, pluginComm, initialStateCode, serviceVersion);
                    this.resultReceiver.onResultReceived(this.retPccObject, RequestAccessResult.getValueFromInt(resultCode), DeviceState.getValueFromInt(initialStateCode));
                    this.retPccObject.deviceInitializedLatch.countDown();
                    return true;
                default:
                    return false;
            }
        }

        public void handleRequestAccessFailed(String errorMessage, RequestAccessResult result) {
            LogAnt.w(AntPluginPcc.TAG, "RequestAccess failed: " + errorMessage);
            this.retPccObject.releaseToken();
            this.resultReceiver.onResultReceived(null, result, DeviceState.DEAD);
        }
    }

    protected final class StandardReleaseHandle<T extends AntPluginPcc> extends PccReleaseHandle<T> {
        protected StandardReleaseHandle(AntPluginPcc.IPluginAccessResultReceiver<T> resultReceiver, AntPluginPcc.IDeviceStateChangeReceiver stateReceiver) {
            super(resultReceiver, stateReceiver);
        }

        protected void requestCancelled() {
            AntPluginPcc.this.closePluginConnection();
        }
    }

    public interface IDeviceStateChangeReceiver {
        void onDeviceStateChange(DeviceState var1);
    }

    public interface IPluginAccessResultReceiver<T extends AntPluginPcc> {
        void onResultReceived(T var1, RequestAccessResult var2, DeviceState var3);
    }
}
