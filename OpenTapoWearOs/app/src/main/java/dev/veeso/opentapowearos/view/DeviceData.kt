package dev.veeso.opentapowearos.view

import android.os.Parcel
import android.os.Parcelable
import dev.veeso.opentapowearos.tapo.device.DeviceModel

class DeviceData(
    val alias: String,
    val id: String,
    val model: DeviceModel,
    val macAddress: String,
    val ipAddress: String,
) : Parcelable {

    constructor(parcel: Parcel) : this(
        alias = parcel.readString()!!,
        id = parcel.readString()!!,
        model = parcel.readSerializable()!! as DeviceModel,
        macAddress = parcel.readString()!!,
        ipAddress = parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(alias)
        parcel.writeString(id)
        parcel.writeSerializable(model)
        parcel.writeString(macAddress)
        parcel.writeString(ipAddress)
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
