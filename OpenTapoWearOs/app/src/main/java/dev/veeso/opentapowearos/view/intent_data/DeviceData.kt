package dev.veeso.opentapowearos.view.intent_data

import android.os.Parcel
import android.os.Parcelable
import dev.veeso.opentapowearos.tapo.device.DeviceModel
import dev.veeso.opentapowearos.tapo.device.DeviceStatus

class DeviceData(
    val alias: String,
    val id: String,
    val model: DeviceModel,
    val endpoint: String,
    val ipAddress: String,
    val status: DeviceStatus
) : Parcelable {

    constructor(parcel: Parcel) : this(
        alias = parcel.readString()!!,
        id = parcel.readString()!!,
        model = parcel.readSerializable()!! as DeviceModel,
        endpoint = parcel.readString()!!,
        ipAddress = parcel.readString()!!,
        status = parcel.readParcelable(DeviceStatus::class.java.classLoader)!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(alias)
        parcel.writeString(id)
        parcel.writeSerializable(model)
        parcel.writeString(endpoint)
        parcel.writeString(ipAddress)
        parcel.writeParcelable(status, 0)
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
