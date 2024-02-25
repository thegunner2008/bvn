package tamhoang.bvn.data.services.xuLyTinNhan.lotteryHelper.danSo.mappingSo

import tamhoang.bvn.constants.Const
import tamhoang.bvn.util.extensions.countIndex
import tamhoang.bvn.util.ld.CongThuc
import kotlin.math.min

class GhepDDMapping: MappingNumber() {
    override fun condition(str: String) = str.contains("ghepdd")

    override fun convert(originMsg: String, currentMsg: String, index: Int): String {
        var str = currentMsg

        val iGhep = str.indexOf("ghepdd")
        val count = str.countIndex(iGhep + 11) {
            ", ".contains(it) || it.isDigit()
        }
        val eGhep = min(count - 1, str.length - 1)

        val so = str.substring(iGhep, eGhep)
            .replace("ghepdd", "").trim()
        if (so.isEmpty()) {
            error =  "${Const.KHONG_HIEU} $originMsg"
            return ""
        } else if (CongThuc.isNumericComma(so)) {
            val danArr = so.split(",").filter { it.isNotEmpty() }

            danArr.forEachIndexed { i, s ->
                if (s.length == 2 && CongThuc.isNumeric(s) && i > 0) {
                    error = "${Const.KHONG_HIEU} " + str.substring(str.indexOf(s), eGhep)
                    return ""
                }
                else if (!CongThuc.isNumeric(s)) {
                    error = "${Const.KHONG_HIEU} " + str.substring(str.indexOf(s), eGhep)
                    return ""
                }
            }

            val soDau1 = MappingFunc.layDau(so)
            val soDit1 = MappingFunc.layDit(so)
            if (soDau1.contains(Const.KHONG_HIEU) || soDit1.contains(Const.KHONG_HIEU)) {
                error =  "${Const.KHONG_HIEU} $originMsg"
                return ""
            }
            result += MappingFunc.lay100so { soDau1.contains(it) && soDit1.contains(it) }

            str = (str.substring(0, iGhep) + " " + str.substring(eGhep)).trim()
        } else {
            error = "${Const.KHONG_HIEU} " + str.substring(str.indexOf("ghepdd"), eGhep)
            return  ""
        }
        return str
    }
}