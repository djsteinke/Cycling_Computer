package rnfive.htfu.ant.antplus.common;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import rnfive.htfu.ant.utility.log.LogAnt;

public class FitFileCommon {

    public FitFileCommon() {
    }

    public static class FitFile implements Parcelable {
        static final String TAG = FitFileCommon.class.getSimpleName();
        private final byte[] mFitFileByteArray;
        private short fileType;
        public static final Creator<FitFileCommon.FitFile> CREATOR = new FitFileCreator();

        public FitFile(byte[] fitFileBytes) {
            mFitFileByteArray = fitFileBytes.clone();
        }

        public final byte[] getRawBytes() {
            return mFitFileByteArray.clone();
        }

        public final void setFileType(short fitFileType) {
            fileType = fitFileType;
        }

        public final short getFileType() {
            return fileType;
        }

        public final InputStream getInputStream() {
            return new ByteArrayInputStream(mFitFileByteArray);
        }

        public FitFile(Parcel src) {
            int incomingVersion = src.readInt();
            if (1 != incomingVersion) {
                LogAnt.i(TAG, "Decoding version " + incomingVersion + " FitFile parcel with version 1 parser.");
            }

            fileType = (short)src.readInt();
            mFitFileByteArray = new byte[src.readInt()];
            src.readByteArray(mFitFileByteArray);
        }

        public final void writeToParcel(Parcel dest, int flags) {
            int ipcVersionNumber = 1;
            dest.writeInt(ipcVersionNumber);
            dest.writeInt(fileType);
            dest.writeInt(mFitFileByteArray.length);
            dest.writeByteArray(mFitFileByteArray);
        }

        public final int describeContents() {
            return 0;
        }

        private static final class FitFileCreator implements Creator<FitFile> {
            FitFileCreator() {
            }

            public FitFile createFromParcel(Parcel source) {
                return new FitFile(source);
            }

            public FitFile[] newArray(int size) {
                return new FitFile[size];
            }
        }
    }

    public enum FitFileDataType {
        FIT_DATA_TYPE(128),
        BLOOD_PRESSURE(14),
        INVALID(-1),
        UNRECOGNIZED(-2);

        private final int intValue;

        FitFileDataType(int val) {
            intValue = val;
        }

        public int getIntValue() {
            return intValue;
        }

        public static FitFileDataType getValueFromInt(int intValue) {
            FitFileDataType[] var1 = values();

            for (FitFileDataType dataType : var1) {
                if (dataType.getIntValue() == intValue) {
                    return dataType;
                }
            }

            return UNRECOGNIZED;
        }
    }

    public interface IFitFileDownloadedReceiver {
        void onNewFitFileDownloaded(FitFileCommon.FitFile fitFile);
    }

    public static class IpcDefines {
        public IpcDefines() {
        }
    }
}
