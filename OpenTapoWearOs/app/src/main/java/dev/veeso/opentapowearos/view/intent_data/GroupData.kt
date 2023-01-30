package dev.veeso.opentapowearos.view.intent_data

import android.os.Parcel
import android.os.Parcelable

class GroupData(val groupName: String, val devices: List<DeviceData>) : Parcelable {

    constructor(parcel: Parcel) : this(
        groupName = parcel.readString()!!,
        devices = ArrayList<DeviceData>()
    ) {
        parcel.readList(this.devices, DeviceData::class.java.classLoader)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(groupName)
        parcel.writeList(devices)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<GroupData> {
        override fun createFromParcel(parcel: Parcel): GroupData {
            return GroupData(parcel)
        }

        override fun newArray(size: Int): Array<GroupData?> {
            return arrayOfNulls(size)
        }
    }
}
