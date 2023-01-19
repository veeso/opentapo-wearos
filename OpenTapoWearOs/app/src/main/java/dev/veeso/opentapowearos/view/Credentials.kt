package dev.veeso.opentapowearos.view

import android.os.Parcel
import android.os.Parcelable

class Credentials(val token: String) : Parcelable {

    constructor(parcel: Parcel) : this(
        token = parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(token)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Credentials> {
        override fun createFromParcel(parcel: Parcel): Credentials {
            return Credentials(parcel)
        }

        override fun newArray(size: Int): Array<Credentials?> {
            return arrayOfNulls(size)
        }
    }
}
