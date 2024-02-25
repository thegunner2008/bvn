package tamhoang.bvn.data.services.xuLyTinNhan.lotteryHelper.danSo.mappingSo

import tamhoang.bvn.constants.Const
import tamhoang.bvn.util.extensions.countIndex
import tamhoang.bvn.util.ld.CongThuc

class DauMapping : MappingNumber() {
    override fun condition(str: String) = str.indexOf("dau") in 0..4
            && !str.contains("ghep") && !str.contains("dau dit")

    override fun convert(originMsg: String, currentMsg: String, index: Int): String {
        var str = currentMsg
        if (str.length >= 5) {
            var i = -1
            while (true) {
                val iDau = str.indexOf("dau", i + 1)
                if (iDau == -1) {
                    break
                }
                val eDau = (if (str.last().isDigit()) "$str " else str).countIndex(iDau + 4) {
                    ", ".contains(it) || it.isDigit()
                }
                val dan = if (str.length > 4) {
                    val sub = str.substring(iDau, eDau)
                    if (sub.substring(0, 4).contains("dau")) {
                        sub.replace("dau", "").trim()
                            .replace(" ", ",")
                    } else sub
                } else ""

                if (dan.isEmpty()) {
                    error = "${Const.KHONG_HIEU} $str"
                    return ""
                }
                val danArr = dan.split(",").filter { it.isNotEmpty() }
                danArr.forEachIndexed { i1, s ->
                    if (s.length in 2..3 && CongThuc.isNumeric(s) && i1 > 0) {
                        error = "${Const.KHONG_HIEU} " + str.substring(str.indexOf(s), eDau)
                        return ""
                    } else if (!CongThuc.isNumeric(s)) {
                        error = "${Const.KHONG_HIEU} " + str.substring(str.indexOf(s), eDau)
                        return ""
                    }
                }
                val soDau1 = MappingFunc.layDau(dan)
                if (soDau1.contains(Const.KHONG_HIEU)) {
                    error = "${Const.KHONG_HIEU} $originMsg"
                    return ""
                }
                result += soDau1
                str = (str.substring(0, iDau) + " " + str.substring(eDau)).trim()
                i = 0
            }
        } else {
            error = "${Const.KHONG_HIEU} $originMsg"
            return ""
        }
        return str
    }
}