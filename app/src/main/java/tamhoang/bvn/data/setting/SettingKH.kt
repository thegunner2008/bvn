package tamhoang.bvn.data.setting

import org.json.JSONObject
import tamhoang.bvn.data.DbOpenHelper
import tamhoang.bvn.data.enum.TL
import java.util.*

enum class TLGiu {
    Lo,
    De,
    Xi,
    Bc,
    Xn;

    override fun toString(): String {
        return name.toLowerCase(Locale.ROOT)
    }

    companion object {
        fun fromString(value: String): TLGiu? {
            return values().find { it.toString() == value }
        }

        fun fromTL(tl: TL): TLGiu? {
            return when (tl) {
                TL.De, TL.DeB, TL.DeA, TL.DeC, TL.DeD, TL.Det -> De
                TL.LoA, TL.Lo -> Lo
                TL.XN -> Xn
                TL.BCA , TL.BC -> Bc
                TL.XIA, TL.XI, TL.XG2, TL.XG3, TL.XG4, TL.XQA, TL.XQ -> Xi
                TL.HC, TL.NULL -> null
            }
        }
    }
}

class SettingKH(val json: JSONObject) {
    val daiLyGiu = mutableMapOf<String, Int>()
    val khachGiu = mutableMapOf<String, Int>()
    var tgLoXien: String
        get() = json.getString("tg_loxien")
        set(value) {
            json.put("tg_loxien", value)
        }
    var tgDeBc: String
        get() = json.getString("tg_debc")
        set(value) {
            json.put("tg_debc", value)
        }

    init {
        TLGiu.values().map {
            it.toString()
        }.forEach {
            daiLyGiu[it] = json.getInt("dlgiu_$it")
            khachGiu[it] = json.getInt("khgiu_$it")
        }
    }

    fun putDaiLyGiu(tl: TLGiu, value: Int) {
        daiLyGiu[tl.toString()] = value
        json.put("dlgiu_$tl", value)
    }

    fun putKhachGiu(tl: TLGiu, value: Int) {
        khachGiu[tl.toString()] = value
        json.put("khgiu_$tl", value)
    }

    fun daiLyGiu(tl: TLGiu?) = daiLyGiu[tl.toString()] ?: 0
    fun khachGiu(tl: TLGiu?) = khachGiu[tl.toString()] ?: 0

    inner class Model(
        val key: String,
        val list: Array<String>,
        private val isTrue: Int? = null,
        private val isFalse: Int? = null
    ) {
        fun value() = try {
            json.getInt(key)
        } catch (e: Exception) {
            0
        }

        fun save(value: Int): JSONObject = json.put(key, value)
        fun isTrue() = isTrue == value() || (isFalse != value() && isFalse != null)
    }

    fun saveALl(db: DbOpenHelper) {
//        db.saveSetting(key, value)
    }

    val traLoiTin = Model(
        "ok_tin",
        arrayOf(
            "1. Ok tin và nd phân tích",
            "2. Chỉ ok tin",
            "3. Không trả lời",
            "4. Ok tin nguyên mẫu",
            "5. Chỉ ok tin (ngay khi nhận)",
            "6. OK nguyên mẫu (ngay khi nhận)"
        )
    )

    val nhanXien = Model(
        "xien_nhan",
        arrayOf("1. Giữ nguyên giá", "2. Nhân 10 khi là điểm", "3. Nhân 10 tất cả xiên"),
        isTrue = 0
    )

    val chotSoDu = Model(
        "chot_sodu",
        arrayOf("1. Chốt tiền trong ngày", "2. Chốt có công nợ")
    )

    val baoLoiDonVi = Model(
        "loi_donvi",
        arrayOf("1. Ko báo lỗi sai đơn vị", "2. Báo lỗi khi sai đơn vị"),
        isTrue = 1
    )

    val khachDe = Model(
        "khach_de",
        arrayOf("1. Thường (de = deb, de8 = det)", "2. Đề 8 (de = det)"),
        isTrue = 0
    )

    val heSoDe = Model(
        "heso_de",
        arrayOf("1. Giữ nguyên (HS=1)", "2. Đề 8->7 (HS=1,143)", "3. Đề 7->8 (HS=0,875)"),
        isFalse = 0
    )

    fun quyDoiDiem(theLoai: TL, diemGoc: Double): Double {// Chuyển Đề 8 thành Đề, ngược lại
        return if (theLoai == TL.DeB && heSoDe.value() == 2) (0.875 * diemGoc).toInt().toDouble()
        else if (theLoai == TL.Det && heSoDe.value() == 1) (1.143 * diemGoc).toInt().toDouble()
        else diemGoc
    }
}