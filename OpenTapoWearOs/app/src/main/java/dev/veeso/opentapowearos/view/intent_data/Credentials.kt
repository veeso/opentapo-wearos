package dev.veeso.opentapowearos.view.intent_data

import android.os.Parcel
import android.os.Parcelable

class Credentials(val username: String, val password: String) : Parcelable {

    constructor(parcel: Parcel) : this(
        username = parcel.readString()!!,
        password = parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(username)
        parcel.writeString(password)
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
