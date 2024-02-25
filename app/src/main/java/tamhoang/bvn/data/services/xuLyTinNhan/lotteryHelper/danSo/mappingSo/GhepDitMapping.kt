package tamhoang.bvn.data.services.xuLyTinNhan.lotteryHelper.danSo.mappingSo

import tamhoang.bvn.constants.Const
import tamhoang.bvn.util.extensions.countIndex
import tamhoang.bvn.util.ld.CongThuc

class GhepDitMapping : MappingNumber() {
    override fun condition(str: String) = str.contains("ghep dit")

    override fun convert(originMsg: String, currentMsg: String, index: Int): String {
        var str = currentMsg
        val iGhepDit = str.indexOf("ghep dit")
        val iDau = (if (str.last().isDigit()) "$str " else str).substring(0, iGhepDit).lastIndexOf("dau")
        if (iDau == -1) {
            error = "${Const.KHONG_HIEU} $str"
            return ""
        }
        val eGhepDitSo = str.countIndex(iGhepDit + 9) {
            ", ".contains(it) || it.isDigit()
        }
        val dauSo = str.substring(iDau, iGhepDit).replace("dau", "")
        val ghepDitSo =
            str.substring(iGhepDit, eGhepDitSo).replace("ghep dit", "")
        if (dauSo.isEmpty()) {
            error = "${Const.KHONG_HIEU} $str"
            return ""
        } else if (ghepDitSo.isEmpty()) {
            error = "${Const.KHONG_HIEU} $originMsg"
            return ""
        } else if (!CongThuc.isNumericComma(dauSo)) {
            error = "${Const.KHONG_HIEU} " + str.substring(iDau, iGhepDit)
            return ""
        } else if (!CongThuc.isNumericComma(ghepDitSo)) {
            error = "${Const.KHONG_HIEU} " + str.substring(iGhepDit, eGhepDitSo)
            return ""
        } else {
            if (CongThuc.isNumericComma(dauSo) && CongThuc.isNumericComma(ghepDitSo)) {
                val soDau = MappingFunc.layDau(dauSo)
                val soDit = MappingFunc.layDit(ghepDitSo)
                if (soDit.contains(Const.KHONG_HIEU) || soDau.contains(Const.KHONG_HIEU)) {
                    error = "${Const.KHONG_HIEU} $originMsg"
                    return ""
                }
                result += MappingFunc.lay100so { soDau.contains(it) && soDit.contains(it) }
            }
            str = (str.substring(0, iDau) + " " + str.substring(eGhepDitSo)).trim()
        }

        return str
    }
}