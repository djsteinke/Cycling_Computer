package rnfive.htfu.ant.antplus.pccbase;

import android.os.Parcel;
import android.os.Parcelable;

import rnfive.htfu.ant.antplus.pcc.defines.DeviceType;
import rnfive.htfu.ant.internal.pluginsipc.AntPluginDeviceDbProvider.DeviceDbDeviceInfo;
import rnfive.htfu.ant.utility.log.LogAnt;
import rnfive.htfu.ant.utility.parcel.ParcelPacker;
import rnfive.htfu.ant.utility.parcel.ParcelUnpacker;

public class MultiDeviceSearch {

    public MultiDeviceSearch() {}

    public static class MultiDeviceSearchResult implements Parcelable {
        private static final String TAG = MultiDeviceSearch.MultiDeviceSearchResult.class.getSimpleName();
        private static final int IPC_VERSION = 1;
        protected final boolean mAlreadyConnected;
        protected final DeviceType mDeviceType;
        protected final DeviceDbDeviceInfo mInfo;
        public final int resultID;
        public static final Creator<MultiDeviceSearch.MultiDeviceSearchResult> CREATOR = new Creator<MultiDeviceSearch.MultiDeviceSearchResult>() {
            public MultiDeviceSearch.MultiDeviceSearchResult[] newArray(int size) {
                return new MultiDeviceSearch.MultiDeviceSearchResult[size];
            }

            public MultiDeviceSearch.MultiDeviceSearchResult createFromParcel(Parcel source) {
                return new MultiDeviceSearch.MultiDeviceSearchResult(source);
            }
        };

        public MultiDeviceSearchResult(int resultID, DeviceType type, DeviceDbDeviceInfo info, boolean alreadyConnected) {
            this.resultID = resultID;
            this.mAlreadyConnected = alreadyConnected;
            this.mDeviceType = type;
            this.mInfo = info;
        }

        public DeviceType getAntDeviceType() {
            return this.mDeviceType;
        }

        public int getAntDeviceNumber() {
            return this.mInfo.antDeviceNumber;
        }

        public boolean isAlreadyConnected() {
            return this.mAlreadyConnected;
        }

        public boolean isPreferredDevice() {
            return this.mInfo.isPreferredDevice;
        }

        public boolean isUserRecognizedDevice() {
            return this.mInfo.device_dbId != null;
        }

        public String getDeviceDisplayName() {
            return this.mInfo.visibleName;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            ParcelPacker packer = new ParcelPacker(dest);
            dest.writeInt(1);
            dest.writeInt(this.resultID);
            dest.writeInt(this.mAlreadyConnected ? 1 : 0);
            dest.writeInt(this.mDeviceType.getIntValue());
            ParcelPacker innerpacker = new ParcelPacker(dest);
            dest.writeParcelable(this.mInfo, flags);
            innerpacker.finish();
            packer.finish();
        }

        protected MultiDeviceSearchResult(Parcel source) {
            ParcelUnpacker unpacker = new ParcelUnpacker(source);
            int ipcVersion = source.readInt();
            if (ipcVersion > 1) {
                LogAnt.w(TAG, "Loading DeviceInfo with ipcVersion " + ipcVersion + " as a version 1 parcel.");
            }

            this.resultID = source.readInt();
            this.mAlreadyConnected = source.readInt() != 0;
            this.mDeviceType = DeviceType.getValueFromInt(source.readInt());
            ParcelUnpacker innerunpack = new ParcelUnpacker(source);
            this.mInfo = source.readParcelable(DeviceDbDeviceInfo.class.getClassLoader());
            innerunpack.finish();
            unpacker.finish();
        }
    }
}
