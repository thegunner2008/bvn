package tamhoang.bvn.data.services.xuLyTinNhan.lotteryHelper.danSo.mappingSo

import tamhoang.bvn.constants.Const
import tamhoang.bvn.util.extensions.countIndex
import tamhoang.bvn.util.ld.CongThuc

class DauDitMapping : MappingNumber() {
    override fun condition(str: String) = !str.contains("ghep") && str.contains("dau dit")

    override fun convert(originMsg: String, currentMsg: String, index: Int): String {
        var str = currentMsg
        var i = -1
        while (true) {
            val iDauDit = str.indexOf("dau dit", i + 1)
            if (iDauDit == -1) {
                break
            }
            val eDauDit = (if (str.last().isDigit()) "$str " else str).countIndex(iDauDit + 8) {
                ", ".contains(it) || it.isDigit()
            }
            val dan = if (str.length > 8) {
                val sub = str.substring(iDauDit, eDauDit)
                if (sub.substring(0, 8).contains("dau dit"))
                    sub.replace("dau dit", "").trim()
                        .replace(" ", ",")
                else sub
            } else ""
            if (dan.isEmpty()) {
                error = "${Const.KHONG_HIEU} $originMsg"
                return ""
            }
            val danArr = dan.split(",").filter { it.isNotEmpty() }
            danArr.forEachIndexed { i1, s ->
                if (s.length in 2..3 && CongThuc.isNumeric(s) && i1 > 0)
                    return "${Const.KHONG_HIEU} " + str.substring(str.indexOf(s), eDauDit)
                else if (!CongThuc.isNumeric(s))
                    return "${Const.KHONG_HIEU} " + str.substring(str.indexOf(s), eDauDit)
            }
            val soDau1 = MappingFunc.layDau(dan)
            if (soDau1.contains(Const.KHONG_HIEU)) {
                error = "${Const.KHONG_HIEU} $originMsg"
                return ""
            }
            val soDit1 = MappingFunc.layDit(dan)
            if (soDit1.contains(Const.KHONG_HIEU)) {
                error = "${Const.KHONG_HIEU} $originMsg"
                return ""
            }
            result += soDau1 + soDit1
            str = (str.substring(0, iDauDit) + " " + str.substring(eDauDit)).trim()
            i = 0
        }
        return str
    }
}