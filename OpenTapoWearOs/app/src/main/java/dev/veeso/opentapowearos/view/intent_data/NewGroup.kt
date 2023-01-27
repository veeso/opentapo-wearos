package dev.veeso.opentapowearos.view.intent_data

import android.os.Parcel
import android.os.Parcelable

class NewGroupInput(
    val aliasList: List<String>,
    val existingGroups: List<String>,
) : Parcelable {

    constructor(parcel: Parcel) : this(
        aliasList = parcel.readSerializable()!! as List<String>,
        existingGroups = parcel.readSerializable()!! as List<String>
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeSerializable(aliasList as java.io.Serializable)
        parcel.writeSerializable(existingGroups as java.io.Serializable)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<NewGroupInput> {
        override fun createFromParcel(parcel: Parcel): NewGroupInput {
            return NewGroupInput(parcel)
        }

        override fun newArray(size: Int): Array<NewGroupInput?> {
            return arrayOfNulls(size)
        }
    }

}

class NewGroupOutput(
    val groupName: String
) : Parcelable {

    constructor(parcel: Parcel) : this(
        groupName = parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(groupName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<NewGroupOutput> {
        override fun createFromParcel(parcel: Parcel): NewGroupOutput {
            return NewGroupOutput(parcel)
        }

        override fun newArray(size: Int): Array<NewGroupOutput?> {
            return arrayOfNulls(size)
        }
    }

}
