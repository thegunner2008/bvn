package tamhoang.bvn.data.services.xuLyTinNhan.lotteryHelper.danSo.mappingSo

import tamhoang.bvn.constants.Const
import tamhoang.bvn.util.extensions.countIndex
import tamhoang.bvn.util.ld.CongThuc

class DitMapping : MappingNumber() {
    override fun condition(str: String) = str.indexOf("dit") in 0..4
            && !str.contains("ghep") && !str.contains("dau dit")

    override fun convert(originMsg: String, currentMsg: String, index: Int): String {
        var str = currentMsg

        if (str.length >= 5) {
            var i = -1
            while (true) {
                val iDit = str.indexOf("dit", i + 1)
                if (iDit == -1) {
                    break
                }
                val eDit = (if (str.last().isDigit()) "$str " else str).countIndex(iDit + 5) {
                    ", ".contains(it) || it.isDigit()
                }
                val dan = if (str.length > 4) {
                    val sub = str.substring(iDit, eDit)
                    if (sub.substring(0, 4).contains("dit"))
                        sub.replace("dit", "").trim()
                            .replace(" ", ",")
                    else sub
                } else ""
                if (dan.isEmpty()) {
                    error = "${Const.KHONG_HIEU} $originMsg"
                    return ""
                }
                val danArr = dan.split(",").filter { it.isNotEmpty() }
                danArr.forEachIndexed { i1, s ->
                    if (s.length in 2..3 && CongThuc.isNumeric(s) && i1 > 0) {
                        error = "${Const.KHONG_HIEU} " + str.substring(str.indexOf(s), eDit)
                        return ""
                    } else if (!CongThuc.isNumeric(s)) {
                        error = "${Const.KHONG_HIEU} " + str.substring(str.indexOf(s), eDit)
                        return ""
                    }
                }

                val soD = MappingFunc.layDit(dan)
                if (soD.contains(Const.KHONG_HIEU)) {
                    error = "${Const.KHONG_HIEU} $originMsg"
                    return ""
                }
                result += soD
                str = (str.substring(0, iDit) + " " + str.substring(eDit)).trim()
                i = 0
            }
        } else {
            error = "${Const.KHONG_HIEU} $originMsg"
            return ""
        }

        return str
    }


}