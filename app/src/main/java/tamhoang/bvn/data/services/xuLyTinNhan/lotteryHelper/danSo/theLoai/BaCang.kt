package tamhoang.bvn.data.services.xuLyTinNhan.lotteryHelper.danSo.theLoai

import tamhoang.bvn.constants.Const
import tamhoang.bvn.util.extensions.clear
import tamhoang.bvn.util.ld.CongThuc

object BaCang {
    fun parse(str: String): String {
        if (str.length < 2) return Const.KHONG_HIEU

        if (str.clear(" ").isEmpty()) return ""

        val array = str.trim()
            .replace(":", " ")
            .replace(". ", "")
            .replace(" , ", "")
            .replace(";", " ")
            .replace("/", "")
            .replace(".", ",")
            .replace(" ", ",")
            .split(",")
            .filter { it.isNotEmpty() }

        var daySo = ""
        for (item in array) {
            if (CongThuc.isNumeric(item) && item.length == 3) {
                daySo = "$daySo$item,"
            } else if (item.isNotEmpty()) {
                return "${Const.KHONG_HIEU} $item"
            }
        }
        return daySo
    }
}