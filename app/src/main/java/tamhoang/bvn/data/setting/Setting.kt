package tamhoang.bvn.data.setting

import org.json.JSONException
import org.json.JSONObject
import tamhoang.bvn.data.DbOpenHelper
import tamhoang.bvn.ui.main.MainState

class Setting(val db: DbOpenHelper) {
    companion object {
        lateinit var I: Setting
    }

    init {
        try {
            val cursor = db.getData("Select * From tbl_Setting WHERE ID = 1")
            if (cursor.moveToFirst()) {
                MainState.jSon_Setting = JSONObject(cursor.getString(1))
                cursor.close()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    inner class Model(
        val key: String,
        val list: Array<String>,
        private val isTrue: Int? = null,
        private val isFalse: Int? = null
    ) {
        fun value() = json.getInt(key)
        fun save(value: Int) = save(key, value)
        fun isTrue() = isTrue == value() || (isFalse != value() && isFalse != null)
    }

    val json: JSONObject
        get() = MainState.jSon_Setting

    fun save(key: String, value: Int) {
        db.saveSetting(key, value)
    }

    val apMan = Model(
        "ap_man",
        arrayOf(
            "0 trả thưởng",
            "Nhân 1 lần",
            "Nhân 2 lần",
            "Nhân 3 lần",
            "Nhân 4 lần",
            "Nhân 5 lần",
            "Nhân 6 lần",
            "Nhân 7 lần",
            "Nhân 8 lần",
            "Nhân 9 lần",
            "Nhân 10 lần"
        ),
        isFalse = 0
    )

    val tinQuaGio = Model(
        "tin_qua_gio",
        arrayOf("1. Không nhắn hết giờ", "2. Nhắn báo hết giờ"),
        isTrue = 1
    )

    val nhanTinTrung = Model(
        "tin_trung",
        arrayOf("1. Có nhận tin trùng", "2. Không nhận tin trùng"),
        isTrue = 0
    )

    val gioiHanTin = Model(
        "gioi_han_tin",
        arrayOf(
            "1. Không giới hạn",
            "2. 160 ký tự",
            "3. 320 ký tự",
            "4. 480 ký tự",
            "5. 1000 ký tự",
            "6. 2000 ký tự (Zalo)",
            ""
        ),
        isFalse = 0
    )

    val baoCaoSo = Model(
        "bao_cao_so",
        arrayOf("1. Theo tổng tiền nhận", "2. Theo tổng tiền tồn")
    )

    val chuyenXien = Model(
        "chuyen_xien",
        arrayOf("1. Chuyển theo tiền", "2. Chuyển theo điểm")
    )

    val lamTron = Model(
        "lam_tron",
        arrayOf("1. Không làm tròn", "2. Làm tròn đến 10", "3. Làm tròn đến 50", "4. Làm tròn đến 100"),
        isFalse = 0
    )

    val kieuBaoCao = Model(
        "kieu_bao_cao",
        arrayOf("1. Báo cáo kiểu cũ", "2. Báo cáo kiểu mới")
    )

    val traThuongLo = Model(
        "tra_thuong_lo",
        arrayOf("1. Trả đủ", "2. Nhiều nhất 2 nháy", "3. Nhiều nhất 3 nháy", "4. Nhiều nhất 4 nháy")
    )

    val canhBaoDonVi = Model(
        "canhbaodonvi",
        arrayOf("1. Không cảnh báo", "2. Có cảnh báo"),
        isTrue = 1
    )

    val tachXienTinChot = Model(
        "tachxien_tinchot",
        arrayOf("1. Không tách xiên", "2. Tách riêng xiên 2-3-4"),
        isTrue = 1
    )

    val baoTinThieu = Model(
        "baotinthieu",
        arrayOf("1. Không báo thiếu tin", "2. Có báo thiếu tin"),
        isTrue = 1
    )

}