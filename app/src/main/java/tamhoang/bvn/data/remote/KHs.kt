package tamhoang.bvn.data.remote

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

class KHs() : Parcelable {
    @SerializedName("acc") var acc : String? = null
    @SerializedName("date")  var date : String? = null
    @SerializedName("k_tra") var kTra: String? = null

    constructor(parcel: Parcel) : this() {
        acc = parcel.readString()
        date = parcel.readString()
        kTra = parcel.readString()
    }

    override fun toString(): String {
        return "KHs{" +
                "acc='" + acc + '\'' +
                ", date='" + date + '\'' +
                ", k_tra='" + kTra + '\'' +
                '}'
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(acc)
        parcel.writeString(date)
        parcel.writeString(kTra)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<KHs> {
        override fun createFromParcel(parcel: Parcel): KHs {
            return KHs(parcel)
        }

        override fun newArray(size: Int): Array<KHs?> {
            return arrayOfNulls(size)
        }
    }

}