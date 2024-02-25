package tamhoang.bvn.data.services.xuLyTinNhan

import tamhoang.bvn.constants.Const
import tamhoang.bvn.data.enum.TL

object CheckTin {
    fun loiTheLoai(ndPhanTich: String): String {
        if (ndPhanTich.length < 8)
            return "${Const.KHONG_HIEU} $ndPhanTich"

        val substring = ndPhanTich.substring(0, 5)
        if (TL.values().all { !substring.contains(it.t) })
            return "${Const.KHONG_HIEU} dạng"

        if (ndPhanTich.contains(" bo "))
            return "${Const.KHONG_HIEU} bo "

        return ""
    }

    fun loiBo(ndPhanTich: String): String {
        if (ndPhanTich.contains("bo") && !ndPhanTich.contains("bor")) {
            val indexBo = ndPhanTich.indexOf("bo")
            val notNumber = ndPhanTich.substring(indexBo + 3)
                .any { !it.isWhitespace() && !it.isDigit() }

            if (notNumber) return "${Const.KHONG_HIEU} " + ndPhanTich.substring(indexBo)
        }
        return ""
    }
}