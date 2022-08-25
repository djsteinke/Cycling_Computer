package rnfive.htfu.ant.utility.parcel;

import android.os.Parcel;

public class ParcelPacker {
    private final Parcel mParcel;
    private final int mStartIndex;

    public ParcelPacker(Parcel p) {
        this.mParcel = p;
        this.mStartIndex = p.dataPosition();
        this.mParcel.writeInt(0);
    }

    public void finish() {
        int endIndex = this.mParcel.dataPosition();
        this.mParcel.setDataPosition(this.mStartIndex);
        this.mParcel.writeInt(endIndex - this.mStartIndex);
        this.mParcel.setDataPosition(endIndex);
    }
}
