package rnfive.htfu.ant.utility.parcel;

import android.os.Parcel;

public class ParcelUnpacker {
    private final Parcel mParcel;
    private final int mEndIndex;

    public ParcelUnpacker(Parcel p) {
        this.mParcel = p;
        this.mEndIndex = this.mParcel.dataPosition() + this.mParcel.readInt();
    }

    public void finish() {
        this.mParcel.setDataPosition(this.mEndIndex);
    }
}
