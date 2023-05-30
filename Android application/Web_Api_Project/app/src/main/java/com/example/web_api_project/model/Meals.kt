package com.example.web_api_project.model

import android.os.Parcel
import android.os.Parcelable

data class Meals(var id:String?,var name:String?,var details:String?,var price:Float,var quantity:Int,var image:String?,var rate:Float,var created_at:String?):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readFloat(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readFloat(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(details)
        parcel.writeFloat(price)
        parcel.writeInt(quantity)
        parcel.writeString(image)
        parcel.writeFloat(rate)
        parcel.writeString(created_at)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Meals> {
        override fun createFromParcel(parcel: Parcel): Meals {
            return Meals(parcel)
        }

        override fun newArray(size: Int): Array<Meals?> {
            return arrayOfNulls(size)
        }
    }
}