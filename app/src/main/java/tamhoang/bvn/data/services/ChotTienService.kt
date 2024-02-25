package tamhoang.bvn.data.services

import org.json.JSONException
import org.json.JSONObject
import tamhoang.bvn.data.DbOpenHelper
import tamhoang.bvn.data.enum.TYPE
import tamhoang.bvn.data.model.SoctS
import tamhoang.bvn.data.setting.Setting
import tamhoang.bvn.data.setting.TLGiu
import tamhoang.bvn.data.store.KhachHangStore
import tamhoang.bvn.ui.main.MainState
import tamhoang.bvn.util.extensions.component6
import tamhoang.bvn.util.extensions.component7
import tamhoang.bvn.util.extensions.component8
import tamhoang.bvn.util.extensions.toDecimal

class ChotTienService(val db: DbOpenHelper) {
    companion object {
        lateinit var I: ChotTienService

        private const val AnNhan = "AnNhan"
        private const val AnChuyen = "AnChuyen"
        private const val KQNhan = "KQNhan"
        private const val KQChuyen = "KQChuyen"
        private const val DiemChuyen = "DiemChuyen"
        private const val DiemNhan = "DiemNhan"
        private const val PhanTram = "PhanTram"
    }

    fun chot(tenKH: String) =
        if (!Setting.I.tachXienTinChot.isTrue())
            chotKhongTachXien(tenKH)
        else
            chotTachXien(tenKH)

    private fun chotKhongTachXien(tenKH: String): String {
        val mDate = MainState.dateYMD
        val mNgay = MainState.date2DMY
        val settingKH = KhachHangStore.I.getSettingKHByName(tenKH)

        val (nocuValue, socuoiValue) = db.getOneRow(
            SoctS.TABLE,
            "ten_kh = '$tenKH'  GROUP BY ten_kh",
            Pair(TYPE.Double, "SUM((ngay_nhan < '$mDate') * ket_qua * (100-diem_khachgiu)/100)/1000"),
            Pair(TYPE.Double, "SUM((ngay_nhan <= '$mDate')*ket_qua*(100-diem_khachgiu)/100)/1000")
        )

        val nocu = "So cu: " + (nocuValue as Double).toDecimal()
        val socuoi = "So cuoi: " + (socuoiValue as Double).toDecimal()

        val data = db.getFullData(
            SoctS.TABLE,
            "ngay_nhan = '$mDate' AND ten_kh = '$tenKH'\n  AND the_loai <> 'tt' GROUP by ten_kh, the_loai",
            Pair(TYPE.String, "the_loai"),
            Pair(TYPE.Double, "sum((type_kh = 1)*diem)"),
            Pair(
                TYPE.Double,
                "CASE WHEN the_loai = 'xi' OR the_loai = 'xia' \nTHEN sum((type_kh = 1)*diem*so_nhay*lan_an/1000) \nELSE sum((type_kh = 1)*diem*so_nhay) END nAn"
            ),
            Pair(TYPE.Double, "sum((type_kh = 1)*ket_qua/1000)"),
            Pair(TYPE.Double, "sum((type_kh = 2)*diem)"),
            Pair(
                TYPE.Double,
                "CASE WHEN the_loai = 'xi' OR the_loai = 'xia' \nTHEN sum((type_kh = 2)*diem*so_nhay*lan_an/1000) \nELSE sum((type_kh = 2)*diem*so_nhay)  END nAn"
            ),
            Pair(TYPE.Double, "sum((type_kh = 2)*ket_qua/1000)"),
            Pair(TYPE.Double, "100-(diem_khachgiu*(type_kh=1))")
        )


        val jsonKhach = JSONObject()
        var tienNhan = 0.0
        var tienChuyen = 0.0
        data.forEach {
            val (theloai, diem1, an1, ketQua1, diem2, an2, ketQua2, pt1) = it

            val phanTram = if ((theloai as String).contains("de"))
                (100 - settingKH.khachGiu(TLGiu.De)).toDouble()
            else if (theloai.contains("lo"))
                (100 - settingKH.khachGiu(TLGiu.Lo)).toDouble()
            else if (theloai.contains("xi"))
                (100 - settingKH.khachGiu(TLGiu.Xi)).toDouble()
            else if (theloai.contains("bc"))
                (100 - settingKH.khachGiu(TLGiu.Bc)).toDouble()
            else if (theloai.contains("xn"))
                (100 - settingKH.khachGiu(TLGiu.Xn)).toDouble()
            else (pt1 as Double)

            val jsonDang = JSONObject().apply {
                put(DiemNhan, diem1 as Double)
                put(AnNhan, an1 as Double)
                put(KQNhan, ketQua1 as Double)
                put(DiemChuyen, diem2 as Double)
                put(AnChuyen, an2 as Double)
                put(KQChuyen, ketQua2 as Double)
                put(PhanTram, phanTram)
            }

            tienNhan += jsonDang.getDouble(KQNhan) * phanTram / 100
            tienChuyen += jsonDang.getInt(KQChuyen).toDouble()
            jsonKhach.put(theloai, jsonDang.toString())
        }
        val jsonObject = JSONObject().apply {
            put("dea", "Dau DB: ")
            put("deb", "De: ")
            put("det", "De 8: ")
            put("dec", "Dau Nhat: ")
            put("ded", "Dit Nhat: ")
            put("lo", "Lo: ")
            put("xi", "Xien: ")
            put("xn", "X.nhay: ")
            put("bc", "3Cang: ")
            put("loa", "Lo dau: ")
            put("xia", "Xien dau: ")
            put("bca", "Cang dau: ")
        }
        val keys = jsonObject.keys()
        var nhan = ""
        var chuyen = ""
        while (keys.hasNext()) {
            val key = keys.next()
            if (jsonKhach.has(key)) {
                val jsonDang = JSONObject(jsonKhach.getString(key))
                if (jsonDang.getInt(PhanTram) != 100) {
                    if (jsonDang.getDouble(DiemNhan) > 0.0) {
                        nhan += """
                        ${jsonObject.getString(key)}${
                            jsonDang.getDouble(DiemNhan).toDecimal()
                        }(${jsonDang.getDouble(AnNhan).toDecimal()}) =${
                            jsonDang.getDouble(KQNhan).toDecimal()
                        }x${jsonDang.getString(PhanTram)}%=${
                            (jsonDang.getDouble(KQNhan) * jsonDang.getDouble(PhanTram) / 100.0).toDecimal()
                        }
                        
                        """.trimIndent()
                    }
                } else if (jsonDang.getDouble(DiemNhan) > 0.0) {
                    nhan += """
                    ${jsonObject.getString(key)}${
                        jsonDang.getDouble(DiemNhan).toDecimal()
                    }(${jsonDang.getDouble(AnNhan).toDecimal()})=${
                        jsonDang.getDouble(KQNhan).toDecimal()
                    }
                    
                    """.trimIndent()
                }
                if (jsonDang.getDouble(DiemChuyen) > 0.0) {
                    chuyen += """
                    ${jsonObject.getString(key)}${jsonDang.getDouble(DiemChuyen).toDecimal()}(${
                        jsonDang.getDouble(AnChuyen).toDecimal()
                    })=${
                        jsonDang.getDouble(KQChuyen).toDecimal()
                    }
                    
                    """.trimIndent()
                }
            }
        }
        val ttoanValue = db.getOneData<Int>(
            SoctS.TABLE,
            "ten_kh = '$tenKH' AND ngay_nhan ='$mDate'",
            "SUM((the_loai = 'tt') * ket_qua)"
        )
        val ttoan = if (ttoanValue != 0)
            "T.toan: ${(ttoanValue.toDouble() / 1000.0).toDecimal()}\n"
        else ""
        val chotSoDu = settingKH.chotSoDu.isTrue()

        val nocuStr = if (chotSoDu) nocu else ""
        val tongNhan =
            if (nhan.isNotEmpty()) "\n${nhan}Tong nhan:${tienNhan.toDecimal()}" else ""
        val tongChuyen =
            if (chuyen.isNotEmpty()) "\n\n${chuyen}Tong chuyen:${tienChuyen.toDecimal()}" else ""
        val tongTien =
            if (nhan.isNotEmpty() && chuyen.isNotEmpty()) "\nTong tien: ${(tienNhan + tienChuyen).toDecimal()}" else ""
        val endStr = if (chotSoDu) "\n$ttoan$socuoi" else ""

        return mNgay + "\n" + nocuStr + tongNhan + tongChuyen + tongTien + endStr
    }

    private fun chotTachXien(tenKH: String): String {
        val mDate = MainState.dateYMD
        val mNgay = MainState.date2DMY
        val settingKH = KhachHangStore.I.getSettingKHByName(tenKH)

        val (nocuValue, socuoiValue) = db.getOneRow(
            SoctS.TABLE,
            "ten_kh = '$tenKH'  GROUP BY ten_kh",
            Pair(TYPE.Double, "SUM((ngay_nhan < '$mDate') * ket_qua * (100-diem_khachgiu)/100)/1000"),
            Pair(TYPE.Double, "SUM((ngay_nhan <= '$mDate')*ket_qua*(100-diem_khachgiu)/100)/1000")
        )

        val nocu = "So cu: " + (nocuValue as Double).toDecimal()
        val socuoi = "So cuoi: " + (socuoiValue as Double).toDecimal()

        val data = db.getFullData(
            SoctS.TABLE,
            "the_loai <> 'tt' AND ten_kh = '$tenKH' and ngay_nhan = '$mDate'\n GROUP by m_theloai",
            Pair(
                TYPE.String, """CASE
                    WHEN the_loai = 'xi' And length(so_chon) = 5 THEN 'xi2'
                    WHEN the_loai = 'xi' And length(so_chon) = 8 THEN 'xi3'
                    WHEN the_loai = 'xi' And length(so_chon) = 11 THEN 'xi4'
                    WHEN the_loai = 'xia' And length(so_chon) = 5 THEN 'xia2'
                    WHEN the_loai = 'xia' And length(so_chon) = 8 THEN 'xia3'
                    WHEN the_loai = 'xia' And length(so_chon) = 11 THEN 'xia4'
                    ELSE the_loai END m_theloai"""
            ),
            Pair(TYPE.Double, "sum((type_kh = 1)*diem)"),
            Pair(TYPE.Double, "sum((type_kh = 1)*diem*so_nhay)"),
            Pair(TYPE.Double, "sum((type_kh = 1)*ket_qua)/1000"),
            Pair(TYPE.Double, "sum((type_kh = 2)*diem)"),
            Pair(TYPE.Double, "sum((type_kh = 2)*diem*so_nhay)"),
            Pair(TYPE.Double, "sum((type_kh = 2)*ket_qua/1000)"),
            Pair(TYPE.Double, "100-(diem_khachgiu*(type_kh=1))")
        )

        val jsonKhach = JSONObject()
        var tienNhan = 0.0
        var tienChuyen = 0.0
        data.forEach {
            val (theloai, diem1, an1, ketQua1, diem2, an2, ketQua2, pt1) = it
            try {
                val phanTram = if ((theloai as String).contains("de"))
                    (100 - settingKH.khachGiu(TLGiu.De)).toDouble()
                else if (theloai.contains("lo"))
                    (100 - settingKH.khachGiu(TLGiu.Lo)).toDouble()
                else if (theloai.contains("xi"))
                    (100 - settingKH.khachGiu(TLGiu.Xi)).toDouble()
                else if (theloai.contains("bc"))
                    (100 - settingKH.khachGiu(TLGiu.Bc)).toDouble()
                else if (theloai.contains("xn"))
                    (100 - settingKH.khachGiu(TLGiu.Xn)).toDouble()
                else (pt1 as Double)

                val jsonDang = JSONObject().apply {
                    put(DiemNhan, diem1)
                    put(AnNhan, an1)
                    put(KQNhan, ketQua1)
                    put(DiemChuyen, diem2)
                    put(AnChuyen, an2)
                    put(KQChuyen, ketQua2)
                    put(PhanTram, phanTram)
                }
                tienNhan += jsonDang.getDouble(KQNhan) * phanTram / 100.0
                tienChuyen += jsonDang.getDouble(KQChuyen)
                jsonKhach.put(theloai, jsonDang.toString())
            } catch (_: JSONException) {
            }
        }
        val jsonObject = JSONObject().apply {
            put("dea", "Dau DB: ")
            put("deb", "De: ")
            put("det", "De 8: ")
            put("dec", "Dau Nhat: ")
            put("ded", "Dit Nhat: ")
            put("lo", "Lo: ")
            put("xi2", "Xien 2: ")
            put("xi3", "Xien 3: ")
            put("xi4", "Xien 4: ")
            put("xn", "X.nhay: ")
            put("bc", "3Cang: ")
            put("loa", "Lo dau: ")
            put("xia2", "Xia 2: ")
            put("xia3", "Xia 3: ")
            put("xia4", "Xia 4: ")
            put("bca", "3Cang dau: ")
        }
        val keys = jsonObject.keys()
        var nhan = ""
        var chuyen = ""
        while (keys.hasNext()) {
            val key = keys.next()
            if (jsonKhach.has(key)) {
                val jsonDang = JSONObject(jsonKhach.getString(key))
                if (jsonDang.getInt(PhanTram) != 100) {
                    if (jsonDang.getDouble(DiemNhan) > 0.0) {
                        nhan += """
                        ${jsonObject.getString(key)}${
                            jsonDang.getDouble(DiemNhan).toDecimal()
                        }(${jsonDang.getDouble(AnNhan).toDecimal()})=${

                            jsonDang.getDouble(KQNhan).toDecimal()
                        }x${jsonDang.getString(PhanTram)}%=${
                            (jsonDang.getDouble(
                                KQNhan
                            ) * jsonDang.getDouble(PhanTram) / 100.0).toDecimal()
                        }
                        
                        """.trimIndent()
                    }
                } else if (jsonDang.getDouble(DiemNhan) > 0.0) {
                    nhan += """
                    {jsonObject.getString(key)}${
                        jsonDang.getDouble(DiemNhan).toDecimal()
                    }(${jsonDang.getDouble(AnNhan).toDecimal()})=${
                        jsonDang.getDouble(KQNhan).toDecimal()
                    }
                    
                    """.trimIndent()
                }
                if (jsonDang.getDouble(DiemChuyen) > 0.0) {
                    chuyen += """
                    ${jsonObject.getString(key)}${
                        jsonDang.getDouble(DiemChuyen).toDecimal()
                    }(${jsonDang.getDouble(AnChuyen).toDecimal()})=${
                        jsonDang.getDouble(KQChuyen).toDecimal()
                    }
                    
                    """.trimIndent()
                }
            }
        }
        val ttoanValue = db.getOneData<Int>(
            SoctS.TABLE,
            "ten_kh = '$tenKH' AND ngay_nhan ='$mDate'",
            "SUM((the_loai = 'tt') * ket_qua)"
        )
        val ttoan = if (ttoanValue != 0)
            "T.toan: ${(ttoanValue.toDouble() / 1000.0).toDecimal()}\n"
        else ""
        val chotSoDu = settingKH.chotSoDu.isTrue()

        val nocuStr = if (chotSoDu) nocu else ""
        val tongNhan =
            if (nhan.isNotEmpty()) "\n${nhan}Tong nhan:${tienNhan.toDecimal()}" else ""
        val tongChuyen =
            if (chuyen.isNotEmpty()) "\n\n${chuyen}Tong chuyen:${tienChuyen.toDecimal()}" else ""
        val tongTien =
            if (nhan.isNotEmpty() && chuyen.isNotEmpty()) "\nTong tien: ${(tienNhan + tienChuyen).toDecimal()}" else ""
        val endStr = if (chotSoDu) "\n$ttoan$socuoi" else ""

        return mNgay + "\n" + nocuStr + tongNhan + tongChuyen + tongTien + endStr
    }

    fun chotChiTiet(tenKH: String): String {
        val mDate = MainState.dateYMD
        var noiDung = ""

        fun getData(typeKH: Int) = db.getFullData(
            SoctS.TABLE,
            """ngay_nhan = '$mDate' And ten_kh = '$tenKH' and the_loai <> 'tt' AND type_kh = $typeKH
                    GROUP by so_tin_nhan, m_theloai ORDER by type_kh DESC, ten_kh""",
            Pair(TYPE.Int, "so_tin_nhan"),
            Pair(
                TYPE.String, """CASE
                    WHEN the_loai = 'xi' And length(so_chon) = 5 THEN 'xi2'
                    WHEN the_loai = 'xi' And length(so_chon) = 8 THEN 'xi3'
                    WHEN the_loai = 'xi' And length(so_chon) = 11 THEN 'xi4'
                    WHEN the_loai = 'xia' And length(so_chon) = 5 THEN 'xia2'
                    WHEN the_loai = 'xia' And length(so_chon) = 8 THEN 'xia3'
                    WHEN the_loai = 'xia' And length(so_chon) = 11 THEN 'xia4'
                    ELSE the_loai END m_theloai"""
            ),
            Pair(TYPE.Double, "sum(diem)"),
            Pair(TYPE.Double, "sum(diem * so_nhay)"),
            Pair(TYPE.Double, "sum(ket_qua)")
        )
        try {
            val jsonObject = JSONObject().apply {
                put("dea", "Dau DB: ")
                put("deb", "De: ")
                put("det", "De 8: ")
                put("dec", "Dau Nhat: ")
                put("ded", "Dit Nhat: ")
                put("lo", "Lo: ")
                put("xi2", "Xien 2: ")
                put("xi3", "Xien 3: ")
                put("xi4", "Xien 4: ")
                put("xn", "X.nhay: ")
                put("bc", "3Cang: ")
                put("loa", "Lo dau: ")
                put("xia2", "Xia 2: ")
                put("xia3", "Xia 3: ")
                put("xia4", "Xia 4: ")
                put("bca", "3Cang dau: ")
            }
            val data1 = getData(typeKH = 1)
            val data2 = getData(typeKH = 2)
            if (data1.size > 0) {
                noiDung = "\nTin nhan:"
            }
            var sotin = 0
            data1.forEach {
                val (soTinNhan, theLoai, diem, an, _) = it
                val header = if (sotin != soTinNhan as Int) {
                    sotin = soTinNhan
                    "\n\nTin $soTinNhan:"
                } else ""

                val theLoaiFull = jsonObject.getString(theLoai as String)
                val diemStr = (diem as Double).toDecimal()
                val anStr = (an as Double).toDecimal()
                noiDung += "$header\n$theLoaiFull$diemStr($anStr)"
            }
            if (data2.size > 0) {
                noiDung = "$noiDung\n\nTin Chuyen:"
            }
            var sotin2 = 0
            data2.forEach {
                val (soTinNhan, theLoai, diem, an, _) = it
                val header = if (sotin2 != (soTinNhan as Int)) {
                    sotin2 = soTinNhan
                    "\n\nTin $sotin2:"
                } else ""

                val theLoaiFull = jsonObject.getString(theLoai as String)
                val diemStr = (diem as Double).toDecimal()
                val anStr = (an as Double).toDecimal()
                noiDung += "$header\n$theLoaiFull$diemStr($anStr)"
            }
        } catch (e: JSONException) {
        }
        return noiDung
    }
}