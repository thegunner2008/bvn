package tamhoang.bvn.data.setting

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.json.JSONObject
import tamhoang.bvn.data.enum.TL
import java.io.Serializable
import java.util.*

enum class TLGia {
    Lo,
    Dea,
    Deb,
    Dec,
    Ded,
    Det,
    X2,
    X3,
    X4,
    Bc,
    Xn;

    override fun toString(): String {
        return name.toLowerCase(Locale.ROOT)
    }

    companion object {
        fun fromString(value: String): TLGia? {
            return values().find { it.toString() == value }
        }
    }
}

data class SettingGia(
    @SerializedName("dea") var giaDea: Double = 0.0,
    @SerializedName("deb") var giaDeb: Double = 0.0,
    @SerializedName("dec") var giaDec: Double = 0.0,
    @SerializedName("ded") var giaDed: Double = 0.0,
    @SerializedName("det") var giaDet: Double = 0.0,
    @SerializedName("lo") var giaLo: Double = 0.0,
    @SerializedName("gia_x2") var giaX2: Double = 0.0,
    @SerializedName("gia_x3") var giaX3: Double = 0.0,
    @SerializedName("gia_x4") var giaX4: Double = 0.0,
    @SerializedName("gia_bc") var giaBc: Double = 0.0,
    @SerializedName("gia_xn") var giaXn: Double = 0.0,

    @SerializedName("an_dea") var anDea: Double = 0.0,
    @SerializedName("an_deb") var anDeb: Double = 0.0,
    @SerializedName("an_dec") var anDec: Double = 0.0,
    @SerializedName("an_ded") var anDed: Double = 0.0,
    @SerializedName("an_det") var anDet: Double = 0.0,
    @SerializedName("an_lo") var anLo: Double = 0.0,
    @SerializedName("an_x2") var anX2: Double = 0.0,
    @SerializedName("an_x3") var anX3: Double = 0.0,
    @SerializedName("an_x4") var anX4: Double = 0.0,
    @SerializedName("an_bc") var anBc: Double = 0.0,
    @SerializedName("an_xn") var anXn: Double = 0.0
) : Serializable {

    fun getGia(tl: TL): Double {
        return when (tl) {
            TL.DeA -> giaDea
            TL.DeB -> giaDeb
            TL.DeC -> giaDec
            TL.DeD -> giaDed
            TL.Det -> giaDet
            TL.Lo, TL.LoA -> giaLo
            TL.BC, TL.BCA -> giaBc
            TL.XN -> giaXn
            else -> 0.0
        }
    }

    fun getGiaXI(danSo: String) = when (danSo.trim().length) {
        5 -> giaX2
        8 -> giaX3
        11 -> giaX4
        else -> 0.0
    }

    fun getLanAn(tl: TL): Double {
        return when (tl) {
            TL.DeA -> anDea
            TL.DeB -> anDeb
            TL.DeC -> anDec
            TL.DeD -> anDed
            TL.Det -> anDet
            TL.Lo, TL.LoA -> anLo
            TL.BC, TL.BCA -> anBc
            TL.XN -> anXn
            else -> 0.0
        }
    }

    fun getLanAnXI(danSo: String) = when (danSo.trim().length) {
        5 -> anX2
        8 -> anX3
        11 -> anX4
        else -> 0.0
    }

    companion object {
        fun fromJson(json: JSONObject) = Gson().fromJson(json.toString(), SettingGia::class.java) ?: SettingGia()
    }
}