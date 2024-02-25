package tamhoang.bvn.data.services.xuLyTinNhan.lotteryHelper.danSo.mappingSo

import tamhoang.bvn.constants.Const
import tamhoang.bvn.util.extensions.countTrueIndex
import tamhoang.bvn.util.ld.CongThuc

class DanMapping: MappingNumber() {
    override fun condition(str: String) = str.indexOf("dan") in 0..4

    override fun convert(originMsg: String, currentMsg: String, index: Int): String {
        var str = currentMsg
        if (str.length >= 5) {
            var i = -1
            while (true) {
                val iDan = str.indexOf("dan", i + 1)
                if (iDan == -1) {
                    break
                }

                val eDan = (if (str.last().isDigit()) "$str " else str).countTrueIndex(iDan + 4) {
                    ", ".contains(it) || it.isDigit()
                }

                val so = str.substring(iDan + 4, eDan).trim().trim()
                    .replace(Regex("\\s+"), ",").replace(",+", ",")
                if (so.isEmpty()) {
                    error =  "${Const.KHONG_HIEU} $originMsg"
                    return ""
                }
                val so1 = if (so.length != 3 || !so.contains(","))
                    so
                else
                    so.replace(",", "")

                val danArr = so1.split(",").map { it.replace(" ", "") }

                for (dan in danArr) {
                    if (dan.length != 2 || !CongThuc.isNumeric(dan)) {
                        error =  "${Const.KHONG_HIEU} $originMsg"
                        return ""
                    } else if (dan[0] > dan[1]) {
                        error = "${Const.KHONG_HIEU} " + str.substring(str.indexOf(dan), eDan)
                        return ""
                    }

                    val ndung = (dan[0]..dan[1]).joinToString("")

                    val soDau1 = MappingFunc.layDau(ndung)
                    if (soDau1.contains(Const.KHONG_HIEU)) {
                        error =  "${Const.KHONG_HIEU} $originMsg"
                        return ""
                    }
                    val soDit1 = MappingFunc.layDit(ndung)
                    if (soDit1.contains(Const.KHONG_HIEU)) {
                        error =  "${Const.KHONG_HIEU} $originMsg"
                        return ""
                    }
                    val arr = soDau1.split(",").filter { it.isNotEmpty() }

                    result =
                        arr.filter { soDit1.contains(it) }.joinToString(",") + ","
                }
                str =
                    (str.substring(0, iDan) + " " + str.substring(eDan)).trim()
                i = 0
            }

            return str
        } else {
            error = "${Const.KHONG_HIEU} $originMsg"
            return ""
        }
    }
}