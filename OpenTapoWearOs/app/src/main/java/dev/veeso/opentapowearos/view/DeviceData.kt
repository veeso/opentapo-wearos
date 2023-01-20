package dev.veeso.opentapowearos.view

import android.os.Parcel
import android.os.Parcelable
import dev.veeso.opentapowearos.tapo.device.DeviceModel

class DeviceData(
    val appServerUrl: String,
    val token: String,
    val alias: String,
    val id: String,
    val model: DeviceModel
) : Parcelable {

    constructor(parcel: Parcel) : this(
        appServerUrl = parcel.readString()!!,
        token = parcel.readString()!!,
        alias = parcel.readString()!!,
        id = parcel.readString()!!,
        model = parcel.readSerializable()!! as DeviceModel
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(appServerUrl)
        parcel.writeString(token)
        parcel.writeString(alias)
        parcel.writeString(id)
        parcel.writeSerializable(model)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DeviceData> {
        override fun createFromParcel(parcel: Parcel): DeviceData {
            return DeviceData(parcel)
        }

        override fun newArray(size: Int): Array<DeviceData?> {
            return arrayOfNulls(size)
        }
    }

}
