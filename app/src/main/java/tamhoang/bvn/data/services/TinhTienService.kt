package tamhoang.bvn.data.services

import tamhoang.bvn.data.DbOpenHelper
import tamhoang.bvn.data.enum.TL
import tamhoang.bvn.data.setting.Setting
import tamhoang.bvn.data.store.KqStore
import tamhoang.bvn.data.store.SoctStore
import tamhoang.bvn.data.store.TinNhanStore
import tamhoang.bvn.util.extensions.indexOfFulls

class TinhTienService(val db: DbOpenHelper) {
    companion object {
        lateinit var I: TinhTienService
    }

    fun run(mDate: String) {
        val listGiai = KqStore.I.getKq(mDate)

        val mang = Array(1000) { Array(8) { "" } }

        TinNhanStore.I.deleteWhere("Length(nd_phantich) <5 ")
        SoctStore.I.setSoNhayWhere(
            mDate, soNhay = 0, ketQua = 0, where = "the_loai <> 'tt' AND the_loai <> 'cn'"
        )

        for (i in 0..26) {
            val giai = listGiai[i]
            mang[giai.substring(giai.length - 2).toInt()][0] += "*"
            mang[giai.substring(0, 2).toInt()][6] += "*"

            SoctStore.I.increaseSoNhay(mDate, TL.Lo, giai)
            SoctStore.I.increaseSoNhay(mDate, TL.LoA, giai)
        }

        val giaiDB = listGiai[0]
        val giaiNhat = listGiai[1]
        mang[giaiDB.substring(0, 2).toInt()][1] += "*"
        mang[giaiDB.substring(giaiDB.length - 2).toInt()][2] += "*"
        mang[giaiDB.substring(giaiDB.length - 3).toInt()][5] += "*"
        mang[giaiDB.substring(0, 3).toInt()][7] += "*"

        SoctStore.I.setSoNhay(mDate, TL.BC, giaiDB)
        SoctStore.I.setSoNhay(mDate, TL.BCA, giaiDB)

        val apMan = Setting.I.apMan
        if (apMan.isTrue()) { // tính 3 càng nhu đề đít db
            val baSoCuoi = giaiDB.substring(giaiDB.length - 3)
            val haiSoCuoi = giaiDB.substring(giaiDB.length - 2)

            for (i in 0..9) {
                val giaiFake = "$i$haiSoCuoi"
                if (giaiFake != baSoCuoi) {
                    mang[giaiFake.toInt()][5] += "*"
                    SoctStore.I.setSoNhay(mDate, TL.BC, giaiFake, lanAn = apMan.value())
                }
            }
        }
        tinhDe(TL.DeA, mDate, giaiDB)
        tinhDe(TL.DeB, mDate, giaiDB)
        tinhDe(TL.DeC, mDate, giaiNhat)
        tinhDe(TL.DeD, mDate, giaiNhat)
        tinhDe(TL.Det, mDate, giaiDB)

        val listSoctXi =
            SoctStore.I.selectListWhere(mDate, where = "(the_loai = 'xn' OR the_loai = 'xi')")

        listSoctXi.forEach { soct ->
            val listSo = soct.so_chon.split(",").filter { it.isNotEmpty() }
            val check =
                if ("xi".contains(soct.the_loai))
                    listSo.all { so ->
                        mang[so.trim().toInt()][0].isNotEmpty()
                    }
                else if ("xn".contains(soct.the_loai))
                    checkXn(listSo, mang)
                else
                    false

            if (check)
                db.queryData("Update tbl_soctS Set so_nhay = 1 WHERE ID = " + soct.id)
        }

        val listSoctXia =
            SoctStore.I.selectListWhere(mDate, where = "the_loai = 'xia'")

        listSoctXia.forEach { soct ->
            val listSo = soct.so_chon.split(",").filter { it.isNotEmpty() }
            val check = listSo.all { so ->
                mang[so.trim().toInt()][6].isNotEmpty()
            }

            if (check)
                db.queryData("Update tbl_soctS Set so_nhay = 1 WHERE ID = " + soct.id)
        }

        if (Setting.I.traThuongLo.value() > 0) {
            val sonhaymax = Setting.I.traThuongLo.value() + 1
            db.queryData("Update tbl_soctS Set so_nhay = $sonhaymax Where so_nhay > $sonhaymax")
        }
        db.queryData("Update tbl_soctS set ket_qua = diem * lan_an * so_nhay - tong_tien WHERE ngay_nhan = '$mDate' AND type_kh = 1 AND the_loai <> 'tt' AND the_loai <> 'cn'")
        db.queryData("Update tbl_soctS set ket_qua = -diem * lan_an * so_nhay + tong_tien WHERE ngay_nhan = '$mDate' AND type_kh = 2 AND the_loai <> 'tt' AND the_loai <> 'cn'")
        db.queryData("Update tbl_tinnhanS set tinh_tien = 0 Where ngay_nhan = '$mDate'")

        val listTinNhan =
            TinNhanStore.I.selectListWhere(mDate, where = "tinh_tien = 0 AND phat_hien_loi = 'ok'")

        for (tinNhan in listTinNhan) {
//            if (tinNhan.tinhTien != 0) continue
            var noiDung = tinNhan.ndPhanTich.replace("*", "")
            if (noiDung.indexOf("Bỏ ") == 0) {
                noiDung = noiDung.substring(noiDung.indexOf("\n") + 1)
            }
            var ndPhantich = ""

            noiDung.indexOfFulls("\n") { previous, index ->
                if (previous >= index) return@indexOfFulls
                val str1 = noiDung.substring(previous, index)
                val tien = str1.substring(str1.lastIndexOf("x") + 1)

                if (str1.contains("de dau db"))
                    ndPhantich += themDan("de dau db", str1, mang, 1, tien)
                else if (str1.contains("de dit db"))
                    ndPhantich += themDan("de dit db", str1, mang, 2, tien)
                else if (str1.contains("de 8"))
                    ndPhantich += themDan("de 8", str1, mang, 2, tien)
                else if (str1.contains("de dau nhat"))
                    ndPhantich += themDan("de dau nhat", str1, mang, 3, tien)
                else if (str1.contains("de dit nhat"))
                    ndPhantich += themDan("de dit nhat", str1, mang, 4, tien)
                else if (str1.contains("bc"))
                    ndPhantich += themDan("bc", str1, mang, 5, tien)
                else if (str1.contains("lo dau"))
                    ndPhantich += themDan("lo dau", str1, mang, 6, tien)
                else if (str1.contains("bc dau"))
                    ndPhantich += themDan("bc dau", str1, mang, 7, tien)
                else if (str1.contains("lo"))
                    ndPhantich += themDan("lo", str1, mang, 0, tien)
                else if (str1.contains("xien dau"))
                    ndPhantich += themDanXien("xien dau", str1, mang, 6)
                else if (str1.contains("xi"))
                    ndPhantich += themDanXien("xi", str1, mang, 0)
                else if (str1.contains("xn")) {
                    val subStr = str1.substring(3)
                    val indexX = if (subStr.contains(",x"))
                        subStr.indexOf(",x")
                    else
                        subStr.indexOf("x")
                    val listSo = subStr.substring(0, indexX).split(",").filter { it.isNotEmpty() }
                    val check = checkXn(listSo, mang)
                    ndPhantich += if (check)
                        "${str1.substring(0, str1.indexOf("\n"))}*\n"
                    else str1
                } else if (str1.contains("xq dau"))
                    ndPhantich += themDanXien("xq dau", str1, mang, 6)
                else if (str1.contains("xq"))
                    ndPhantich += themDanXien("xq", str1, mang, 6)
            }
            db.queryData(
                "Update tbl_tinnhanS Set nd_phantich ='$ndPhantich', tinh_tien = 1 WHERE ID = ${tinNhan.ID}"
            )
        }
    }

    private fun themDan(
        theloai: String,
        raw: String,
        mang: Array<Array<String>>,
        index: Int,
        tien: String
    ): String {
        val dau = "$theloai:"
        val listSo = raw.substring(dau.length, raw.indexOf("x"))
            .split(",").filter { it.isNotEmpty() }
        val bieuThuc = dau + listSo.joinToString(",") { so ->
            so + mang[so.trim().toInt()][index]
        } + ","

        return "${bieuThuc}x$tien\n"
    }

    private fun themDanXien(
        theloai: String,
        raw: String,
        mang: Array<Array<String>>,
        index: Int
    ): String {
        val dau = "$theloai:"
        val listSo =
            raw.substring(dau.length, raw.lastIndexOf("x")).split(",").filter { it.isNotEmpty() }
        val check = listSo.all { so ->
            mang[so.trim().toInt()][index].isNotEmpty()
        }
        return if (check) "${raw}*\n" else "$raw\n"
    }

    private fun checkXn(listSo: List<String>, mang: Array<Array<String>>) =
        mang[listSo[0].toInt()][0].length > 1
                || mang[listSo[1].toInt()][0].length > 1
                || (mang[listSo[0].toInt()][0].isNotEmpty() && mang[listSo[1].toInt()][0].isNotEmpty())

    private fun tinhDe(theloai: TL, date: String, giai: String) {
        SoctStore.I.updateFull(
            date,
            theloai,
            giai,
            typeKh = 1,
            soNhay = 0,
            equal = false,
            ketQua = "-tong_tien"
        )
        SoctStore.I.updateFull(
            date,
            theloai,
            giai,
            typeKh = 1,
            soNhay = 1,
            equal = true,
            ketQua = "diem * lan_an -tong_tien"
        )
        SoctStore.I.updateFull(
            date,
            theloai,
            giai,
            typeKh = 2,
            soNhay = 0,
            equal = false,
            ketQua = "tong_tien"
        )
        SoctStore.I.updateFull(
            date,
            theloai,
            giai,
            typeKh = 2,
            soNhay = 1,
            equal = true,
            ketQua = "-diem * lan_an +tong_tien"
        )
    }
}