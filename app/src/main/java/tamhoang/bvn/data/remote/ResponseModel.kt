package tamhoang.bvn.data.remote

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

class ResponseModel() : Parcelable {
    @SerializedName("listKHs")
    var listKh: List<KHs>? = null

    constructor(parcel: Parcel) : this() {
        listKh = parcel.createTypedArrayList(KHs)
    }

    override fun toString(): String {
        return "ResponseModel{" +
                "listKHs=" + listKh +
                '}'
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(listKh)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ResponseModel> {
        override fun createFromParcel(parcel: Parcel): ResponseModel {
            return ResponseModel(parcel)
        }

        override fun newArray(size: Int): Array<ResponseModel?> {
            return arrayOfNulls(size)
        }
    }
}