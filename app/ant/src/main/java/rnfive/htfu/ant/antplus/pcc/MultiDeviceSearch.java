package rnfive.htfu.ant.antplus.pcc;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.util.EnumSet;
import java.util.Iterator;

import rnfive.htfu.ant.antplus.pcc.defines.DeviceState;
import rnfive.htfu.ant.antplus.pcc.defines.DeviceType;
import rnfive.htfu.ant.antplus.pcc.defines.RequestAccessResult;
import rnfive.htfu.ant.antplus.pccbase.AntPluginPcc;
import rnfive.htfu.ant.antplus.pccbase.PccReleaseHandle;
import rnfive.htfu.ant.utility.log.LogAnt;

public class MultiDeviceSearch {
    private static final String TAG = MultiDeviceSearch.class.getSimpleName();
    private final AntPluginPcc.IPluginAccessResultReceiver<SearchPcc> mAccessResultReceiver;
    private final AntPluginPcc.IDeviceStateChangeReceiver mStateChangeReceiver;
    private final MultiDeviceSearch.SearchPcc mPcc;
    final MultiDeviceSearch.SearchCallbacks mCallbacks;
    private final MultiDeviceSearch.RssiCallback mRssiCallback;
    private final PccReleaseHandle<SearchPcc> mReleaseHandle;

    public MultiDeviceSearch(Context context, EnumSet<DeviceType> deviceTypes, MultiDeviceSearch.SearchCallbacks callbacks) throws IllegalArgumentException {
        this(context, deviceTypes, callbacks, null);
    }

    public MultiDeviceSearch(Context context, EnumSet<DeviceType> deviceTypes, MultiDeviceSearch.SearchCallbacks callbacks, MultiDeviceSearch.RssiCallback rssiCallback) throws IllegalArgumentException {
        mAccessResultReceiver = new AntPluginPcc.IPluginAccessResultReceiver<SearchPcc>() {
            public void onResultReceived(MultiDeviceSearch.SearchPcc result, RequestAccessResult resultCode, DeviceState initialDeviceState) {
                if (resultCode == RequestAccessResult.SUCCESS) {
                    if (result.rssiSupportKnown) {
                        mCallbacks.onSearchStarted(result.supportsRssi() ? MultiDeviceSearch.RssiSupport.AVAILABLE : MultiDeviceSearch.RssiSupport.UNAVAILABLE);
                    } else {
                        mCallbacks.onSearchStarted(MultiDeviceSearch.RssiSupport.UNKNOWN_OLDSERVICE);
                    }
                } else {
                    mCallbacks.onSearchStopped(resultCode);
                }

            }
        };
        mStateChangeReceiver = newDeviceState -> {
        };
        if (context != null && deviceTypes != null && callbacks != null) {
            int rssiMode = 0;
            if (rssiCallback != null) {
                rssiMode = 1;
            }

            mCallbacks = callbacks;
            mRssiCallback = rssiCallback;
            Bundle params = new Bundle();
            params.putInt("int_RequestAccessMode", 2);
            params.putInt("int_RssiMode", rssiMode);
            int[] types = new int[deviceTypes.size()];
            int index = 0;

            DeviceType dt;
            for(Iterator<DeviceType> var9 = deviceTypes.iterator(); var9.hasNext(); types[index++] = dt.getIntValue()) {
                dt = var9.next();
            }

            params.putIntArray("intarr_deviceTypeList", types);
            mPcc = new MultiDeviceSearch.SearchPcc(this);
            mReleaseHandle = mPcc.requestAccess(context, params);
        } else {
            throw new IllegalArgumentException("Null parameter passed into MultiDeviceSearch constructor");
        }
    }

    public void close() {
        mReleaseHandle.close();
    }

    public String getMissingDependencyPackageName() {
        return AntPluginPcc.getMissingDependencyPackageName();
    }

    public String getMissingDependencyName() {
        return AntPluginPcc.getMissingDependencyName();
    }

    private class SearchReleaseHandle extends PccReleaseHandle<MultiDeviceSearch.SearchPcc> implements AntPluginPcc.IPluginAccessResultReceiver<SearchPcc>, AntPluginPcc.IDeviceStateChangeReceiver {
        protected boolean successReceived = false;

        public SearchReleaseHandle(AntPluginPcc.IPluginAccessResultReceiver<SearchPcc> resultReceiver, AntPluginPcc.IDeviceStateChangeReceiver stateReceiver) {
            super(resultReceiver, stateReceiver);
        }

        protected void requestCancelled() {
            mPcc.terminate();
        }

        protected boolean isActive() {
            synchronized(stateLock) {
                return !isClosed && (resultSent || successReceived);
            }
        }

        public void onDeviceStateChange(DeviceState newDeviceState) {
            if (newDeviceState == DeviceState.DEAD) {
                onResultReceived((MultiDeviceSearch.SearchPcc)null, RequestAccessResult.OTHER_FAILURE, (DeviceState)null);
            }

        }

        public void onResultReceived(MultiDeviceSearch.SearchPcc result, RequestAccessResult resultCode, DeviceState initialDeviceState) {
            synchronized(stateLock) {
                resultSink.onResultReceived(result, resultCode, initialDeviceState);
                if (resultCode == RequestAccessResult.SUCCESS) {
                    resultSent = false;
                    successReceived = true;
                }

            }
        }
    }

    private static class SearchPcc extends AntPluginPcc {
        public MultiDeviceSearch multiDeviceSearchParentClass;
        public volatile boolean rssiSupportKnown;

        public SearchPcc(MultiDeviceSearch parentClass) {
            multiDeviceSearchParentClass = parentClass;
        }

        private PccReleaseHandle<MultiDeviceSearch.SearchPcc> requestAccess(Context context, Bundle params) {
            MultiDeviceSearch.SearchReleaseHandle releaser = multiDeviceSearchParentClass.new SearchReleaseHandle(multiDeviceSearchParentClass.mAccessResultReceiver, multiDeviceSearchParentClass.mStateChangeReceiver);
            mStateChangeReceiver = releaser;
            MultiDeviceSearch.SearchPcc.RequestResultHandler resultHandler = new MultiDeviceSearch.SearchPcc.RequestResultHandler(this);
            resultHandler.setReturnInfo((MultiDeviceSearch.SearchPcc)this, releaser);
            mReleaseHandle = releaser;
            requestAccess_Helper_SubMain(context, params, this, resultHandler);
            return releaser;
        }

        protected Intent getServiceBindIntent() {
            Intent i = new Intent();
            i.setComponent(new ComponentName("com.dsi.ant.plugins.antplus", "com.dsi.ant.plugins.antplus.multisearch.MultiSearchService"));
            return i;
        }

        protected int getRequiredServiceVersionForBind() {
            return 20205;
        }

        protected void handlePluginEvent(Message eventMsg) {
            Bundle b;
            switch(eventMsg.arg1) {
                case 1:
                    b = eventMsg.getData();
                    b.setClassLoader(rnfive.htfu.ant.antplus.pccbase.MultiDeviceSearch.MultiDeviceSearchResult.class.getClassLoader());
                    rnfive.htfu.ant.antplus.pccbase.MultiDeviceSearch.MultiDeviceSearchResult info = (rnfive.htfu.ant.antplus.pccbase.MultiDeviceSearch.MultiDeviceSearchResult)b.getParcelable("dev_Device");
                    multiDeviceSearchParentClass.mCallbacks.onDeviceFound(info);
                    break;
                case 2:
                    if (multiDeviceSearchParentClass.mRssiCallback == null) {
                        return;
                    }

                    b = eventMsg.getData();
                    int id = b.getInt("int_resultID");
                    int rssi = b.getInt("int_rssi");
                    multiDeviceSearchParentClass.mRssiCallback.onRssiUpdate(id, rssi);
                case 3:
                    break;
                case 4:
                    ((MultiDeviceSearch.SearchReleaseHandle)mReleaseHandle).onResultReceived((MultiDeviceSearch.SearchPcc)null, RequestAccessResult.getValueFromInt(eventMsg.arg2), (DeviceState)null);
                    multiDeviceSearchParentClass.close();
                    break;
                default:
                    LogAnt.d(MultiDeviceSearch.TAG, "Unrecognized event received: " + eventMsg.arg1);
            }

        }

        protected String getPluginPrintableName() {
            return "ANT+ Plugin: Multiple Device Search";
        }

        boolean supportsRssi() {
            return supportsRssiEvent;
        }

        private void terminate() {
            closePluginConnection();
        }

        private static class RequestResultHandler extends RequestAccessResultHandler<MultiDeviceSearch.SearchPcc> {
            public WeakReference<SearchPcc> searchPccParentClass_weakRef;

            public RequestResultHandler(MultiDeviceSearch.SearchPcc parentClass) {
                searchPccParentClass_weakRef = new WeakReference(parentClass);
            }

            public boolean handleRequestAccessResult(Message msg) {
                if (msg.what == 0) {
                    MultiDeviceSearch.SearchPcc parentClass = searchPccParentClass_weakRef.get();
                    if (parentClass != null) {
                        parentClass.rssiSupportKnown = msg.getData().containsKey("bool_RssiSupport");
                    }
                }

                return super.handleRequestAccessResult(msg);
            }

            protected void setReturnInfo(MultiDeviceSearch.SearchPcc retPccObject, IPluginAccessResultReceiver<MultiDeviceSearch.SearchPcc> resultReceiver) {
                super.setReturnInfo(retPccObject, resultReceiver);
            }
        }
    }

    public interface RssiCallback {
        void onRssiUpdate(int var1, int var2);
    }

    public interface SearchCallbacks {
        void onSearchStarted(MultiDeviceSearch.RssiSupport var1);

        void onDeviceFound(rnfive.htfu.ant.antplus.pccbase.MultiDeviceSearch.MultiDeviceSearchResult var1);

        void onSearchStopped(RequestAccessResult var1);
    }

    public enum RssiSupport {
        AVAILABLE,
        UNAVAILABLE,
        UNKNOWN_OLDSERVICE;

        RssiSupport() {
        }
    }

    public static final class IpcDefines {
        public static final String PATH_MULTISEARCH_PLUGIN_PKG = "com.dsi.ant.plugins.antplus";
        public static final String PATH_MULTISEARCH_PLUGIN_SERVICE = "com.dsi.ant.plugins.antplus.multisearch.MultiSearchService";
        public static final String REQACC_PARAM_DEVICE_TYPE_LIST = "intarr_deviceTypeList";
        public static final int MSG_EVENT_MULTISEARCH_whatNEWDEVICE = 1;
        public static final String MSG_EVENT_MULTISEARCH_NEWDEVICE_PARAM_DEVICE = "dev_Device";
        public static final int MSG_EVENT_MULTISEARCH_whatRSSIUPDATE = 2;
        public static final String MSG_EVENT_MULTISEARCH_RSSIUPDATE_PARAM_ID = "int_resultID";
        public static final String MSG_EVENT_MULTISEARCH_RSSIUPDATE_PARAM_RSSI = "int_rssi";
        public static final int MSG_EVENT_MULTISEARCH_whatKEEPALIVE = 3;
        public static final int MSG_EVENT_MULTISEARCH_whatSCANSTOPPED = 4;

        private IpcDefines() {
        }
    }
}
