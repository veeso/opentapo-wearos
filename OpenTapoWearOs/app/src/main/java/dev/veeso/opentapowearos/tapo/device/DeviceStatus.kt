package dev.veeso.opentapowearos.tapo.device

import android.os.Parcel
import android.os.Parcelable

@kotlinx.serialization.Serializable
data class DeviceStatus(
    val deviceOn: Boolean,
    val brightness: Int? = null,
    val hue: Int? = null,
    val saturation: Int? = null,
    val colorTemperature: Int? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        deviceOn = parcel.readBoolean(),
        brightness = parcel.readInt(),
        hue = parcel.readInt(),
        saturation = parcel.readInt(),
        colorTemperature = parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeBoolean(deviceOn)
        if (brightness != null) {
            parcel.writeInt(brightness)
        }
        if (hue != null) {
            parcel.writeInt(hue)
        }
        if (saturation != null) {
            parcel.writeInt(saturation)
        }
        if (colorTemperature != null) {
            parcel.writeInt(colorTemperature)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DeviceStatus> {
        override fun createFromParcel(parcel: Parcel): DeviceStatus {
            return DeviceStatus(parcel)
        }

        override fun newArray(size: Int): Array<DeviceStatus?> {
            return arrayOfNulls(size)
        }
    }

}
