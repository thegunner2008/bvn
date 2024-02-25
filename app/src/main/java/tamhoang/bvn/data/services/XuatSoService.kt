package tamhoang.bvn.data.services

import tamhoang.bvn.data.DbOpenHelper
import tamhoang.bvn.data.setting.Setting
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.min

class XuatSoService(val db: DbOpenHelper) {
    companion object {
        lateinit var I: XuatSoService
    }

    fun xuat(
        theLoai: String?,
        tienXuat: String,
        from: Int,
        to: Int,
        khongMaxs: HashMap<String?, Int?>? = null
    ): String {
        var xuatDan: String
        val ngayNow = LocalDate.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        val tienChuyen = tienXuat.ifEmpty { "0" }
            .replace("%", "").replace("n", "").replace("k", "")
            .replace("d", "").replace(">", "").toInt()
        var soOm = ""
        var tloai: String? = null
        var donvi = "n "
        when (theLoai) {
            "deb" -> {
                xuatDan = "De:"
                soOm = "Om_deB"
                tloai = "(the_loai = 'deb' or the_loai = 'det')"
            }
            "dea" -> {
                xuatDan = "Dau DB:"
                soOm = "Om_deA"
                tloai = "the_loai = 'dea'"
            }
            "dec" -> {
                xuatDan = "Dau nhat:"
                soOm = "Om_deC"
                tloai = "the_loai = 'dec'"
            }
            "ded" -> {
                xuatDan = "Dit nhat:"
                soOm = "Om_deD"
                tloai = "the_loai = 'ded'"
            }
            "lo" -> {
                xuatDan = "Lo:"
                soOm = "Om_lo"
                tloai = "the_loai = 'lo'"
                donvi = "d "
            }
            "loa" -> {
                xuatDan = "Loa:"
                soOm = "Om_lo"
                tloai = "the_loai = 'loa'"
                donvi = "d "
            }
            else -> xuatDan = ""
        }
        val cursor =
            db.getData(
                "Select tbl_soctS.So_chon\n, " +
                        "Sum((tbl_soctS.type_kh = 1) * (100-tbl_soctS.diem_khachgiu)*diem_quydoi/100) as diem\n, " +
                        "so_om.$soOm + sum(tbl_soctS.diem_dly_giu*tbl_soctS.diem_quydoi/100) as So_Om\n, " +
                        "Sum((tbl_soctS.type_kh =2) * tbl_soctS.diem_quydoi) as chuyen\n, " +
                        "Sum((tbl_soctS.type_kh =1) * (100-tbl_soctS.diem_khachgiu-tbl_soctS.diem_dly_giu)*diem_quydoi/100) - Sum((tbl_soctS.type_kh =2) * tbl_soctS.diem_quydoi) - so_om.$soOm as ton\n, " +
                        "so_nhay  From so_om Left Join tbl_soctS On tbl_soctS.so_chon = so_om.So\n Where tbl_soctS.ngay_nhan='$ngayNow' AND $tloai GROUP by so_om.So Order by ton DESC, diem DESC"
            )
        val lamtron = Setting.I.lamTron.let { if (it.isTrue()) it.value() * 10 else 1 }
        val from2 = if (from > 0) from - 1 else from
        var dem = 0
        val xuatMap = HashMap<Int, String>()
        while (cursor.moveToNext()) {
            if (dem >= from2 && dem <= to - 1) {
                val tien = if (tienChuyen == 0)
                    cursor.getInt(4) / lamtron * lamtron
                else if (tienXuat.contains("%"))
                    cursor.getInt(4) * tienChuyen / lamtron / 100 * lamtron
                else if (tienXuat.contains(">"))
                    (cursor.getInt(4) - tienChuyen) / lamtron * lamtron
                else if (cursor.getInt(4) > tienChuyen)
                    tienChuyen / lamtron * lamtron
                else
                    cursor.getInt(4) / lamtron * lamtron

                val soChon = cursor.getString(0)
                if (khongMaxs != null) {
                    val tienKhong = khongMaxs[soChon]
                    if (tienKhong != null) min(tienKhong, tien)
                }
                if (tien > 0) {
                    xuatMap[tien] = (xuatMap[tien] ?: "") + "$soChon,"
                }
            }
            dem++
        }
        xuatDan += xuatMap.entries
            .sortedByDescending { it.key }
            .joinToString("") {
                val daySo = it.value
                val tien = it.key
                "${daySo}x${tien}$donvi"
            }

        cursor.close()
        return xuatDan
    }
}