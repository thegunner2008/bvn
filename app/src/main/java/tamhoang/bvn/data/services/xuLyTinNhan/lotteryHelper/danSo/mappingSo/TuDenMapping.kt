package tamhoang.bvn.data.services.xuLyTinNhan.lotteryHelper.danSo.mappingSo

import tamhoang.bvn.util.extensions.to2ChuSo
import tamhoang.bvn.util.ld.CongThuc

class TuDenMapping : MappingNumber() {
    override fun condition(str: String) = str.contains("den")

    override fun convert(originMsg: String, currentMsg: String, index: Int): String {
        var str = currentMsg

        if (str.substring(0, 3).contains("tu")) {
            str = str.substring(3)
        }
        var iDen = str.indexOf("den")
        var i1 = iDen

        while (i1 > -1 && !CongThuc.isNumeric(str.substring(i1, i1 + 2))) {
            i1--
        }
        val so12 = str.substring(i1, i1 + 2)
        while (iDen < str.length && CongThuc.isNumeric(str.substring(iDen, iDen + 2))) {
            iDen++
        }
        val so22 = str.substring(iDen, iDen + 2)
        if (so12.toInt() < so22.toInt() && so12.isNotEmpty() && so22.isNotEmpty()) {
            for (i in so12.toInt()..so22.toInt()) {
                result += i.to2ChuSo() + ","
            }
            str = str.substring(0, i1) + " " + str.substring(iDen + 2)
        }

        return str
    }
}