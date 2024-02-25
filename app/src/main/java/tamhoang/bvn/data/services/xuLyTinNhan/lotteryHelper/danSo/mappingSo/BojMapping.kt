package tamhoang.bvn.data.services.xuLyTinNhan.lotteryHelper.danSo.mappingSo

import tamhoang.bvn.constants.Const
import tamhoang.bvn.util.extensions.countTrueIndex
import tamhoang.bvn.util.ld.CongThuc

class BojMapping : MappingNumber() {
    override fun condition(str: String) = str.indexOf("boj") in 0..4

    override fun convert(originMsg: String, currentMsg: String, index: Int): String {
        var str = currentMsg
        if (str.length >= 5) {
            var i = -1
            while (true) {
                val iBo = str.indexOf("boj", i + 1)
                if (iBo == -1) {
                    break
                }

                val eBo = str.countTrueIndex(iBo + 4) {
                    ", ".contains(it) || it.isDigit()
                }
                try {
                    val so = str.substring(iBo, eBo).replace("boj", "")
                        .trim()
                    if (so.trim().isEmpty()) {
                        error = "${Const.KHONG_HIEU} $str"
                        return ""
                    }
                    val dan = so.trim().trim()
                        .replace(Regex("\\s+"), ",").replace(",+", ",")
                    val s2 = dan.split(",").filter { it.isNotEmpty() }

                    result += s2.filter { CongThuc.isNumeric(it) && it.length == 2 }
                        .joinToString("") { MappingFunc.layBo(it) }
                    str = (str.substring(0, iBo) + " " + str.substring(eBo)).trim()
                    i = 0
                } catch (e: Exception) {
                    error = "${Const.KHONG_HIEU} $originMsg"
                    return ""
                }
            }
            return str
        } else {
            error = "${Const.KHONG_HIEU} $originMsg"
            return ""
        }
    }

}