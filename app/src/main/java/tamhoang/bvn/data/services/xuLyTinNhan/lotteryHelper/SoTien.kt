package tamhoang.bvn.data.services.xuLyTinNhan.lotteryHelper

import tamhoang.bvn.constants.Const
import tamhoang.bvn.data.enum.TL
import tamhoang.bvn.data.setting.Setting
import tamhoang.bvn.util.extensions.clear

object SoTien {
    fun parse(
        str: String,
        theLoai: TL
    ): String { // tra ve so tien vd: 100k => 100; 1,2tr => 1200
        if (str.isEmpty()) return ""
        val soTien: String
        return if (str.count { it == 'x' } > 1 || str.isEmpty()) {
            "${Const.KHONG_HIEU} $str"
        } else {
            val tien = str.clear(listOf("x", " ")).trim()
            if (tien.isEmpty()) {
                return Const.KHONG_HIEU
            }
            if (tien.endsWith("tr")) { // trieu
                val trim = tien.clear(listOf("tr", ".")).trim()
                val mTien = trim.split(",").filter { it.isNotEmpty() }
                return when (mTien.size) {
                    0 ->
                        "${Const.KHONG_HIEU} $tien"
                    1 -> // 1tr
                        mTien[0] + "000"
                    2 -> { // 1,2tr
                        when (val length = mTien[1].length) {
                            in 0..3 -> mTien[0] + "0".repeat(3 - length) + mTien[1]
                            else -> "${Const.KHONG_HIEU} $trim"
                        }
                    }
                    else -> "${Const.KHONG_HIEU} $trim"
                }
            } else {
                soTien = tien.dropWhile { !it.isDigit() }
                    .takeWhile { it.isDigit() }

                if (Setting.I.canhBaoDonVi.isTrue()) {
                    try {
                        val tienInt = soTien.toInt()
                        val checkTien = (theLoai.isDe() && tienInt > 5000
                                || theLoai.isLo() && tienInt > 1000
                                || theLoai.isBaCang() && tienInt > 2000)
                        if (checkTien && tien.clear(listOf(soTien, ",", ".", "/", " "))
                                .isEmpty()
                        ) return "${Const.KHONG_HIEU} $str"
                    } catch (e: Exception) {
                        return "${Const.KHONG_HIEU} $str"
                    }
                }
                if (tien.clear(listOf(soTien, "ng", "n", "d", "k", ",", ".", "/", " "))
                        .isNotEmpty()
                ) {
                    return "${Const.KHONG_HIEU} $str"
                }
            }
            return try {
                if (soTien.toInt() > 0) {
                    soTien
                } else "${Const.KHONG_HIEU} $str"
            } catch (e: Exception) {
                "${Const.KHONG_HIEU} $str"
            }
        }
    }
}