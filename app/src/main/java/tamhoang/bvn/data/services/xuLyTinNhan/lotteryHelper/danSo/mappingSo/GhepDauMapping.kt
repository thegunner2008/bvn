package tamhoang.bvn.data.services.xuLyTinNhan.lotteryHelper.danSo.mappingSo

import tamhoang.bvn.constants.Const
import tamhoang.bvn.util.extensions.countIndex
import tamhoang.bvn.util.ld.CongThuc

class GhepDauMapping: MappingNumber() {
    override fun condition(str: String) = str.contains("ghep dau")

    override fun convert(originMsg: String, currentMsg: String, index: Int): String {
        var str = currentMsg
        val iGhepDau = str.indexOf("ghep dau")
        val iDit = str.substring(0, iGhepDau).lastIndexOf("dit")
        if (iDit == -1) {
            error = "${Const.KHONG_HIEU} $str"
            return ""
        }
        val eGhepDau = (if (str.last().isDigit()) "$str " else str).countIndex(iGhepDau + 9) {
            ", ".contains(it) || it.isDigit()
        }

        val ditSo = str.substring(iDit, iGhepDau).replace("dit", "")
        val ghepDauSo =
            str.substring(iGhepDau, eGhepDau).replace("ghep dau", "")
        if (ditSo.isEmpty()) {
            return "${Const.KHONG_HIEU} $str"
        } else if (ghepDauSo.isEmpty()) {
            error = "${Const.KHONG_HIEU} $originMsg"
            return ""
        } else if (!CongThuc.isNumericComma(ditSo)) {
            error = "${Const.KHONG_HIEU} " + str.substring(iGhepDau, iGhepDau)
            return ""
        } else if (!CongThuc.isNumericComma(ghepDauSo)) {
            error = "${Const.KHONG_HIEU} " + str.substring(iGhepDau, eGhepDau)
            return ""
        } else {
            if (CongThuc.isNumericComma(ditSo) && CongThuc.isNumericComma(ghepDauSo)) {
                val soDau = MappingFunc.layDau(ghepDauSo)
                val soDit = MappingFunc.layDit(ditSo)
                if (soDit.contains(Const.KHONG_HIEU) || soDau.contains(Const.KHONG_HIEU)) {
                    error = "${Const.KHONG_HIEU} $originMsg"
                    return ""
                }
                result += MappingFunc.lay100so { soDau.contains(it) && soDit.contains(it) }
            }
            str = (str.substring(0, iDit) + " " + str.substring(eGhepDau)).trim()
        }
        return str
    }
}