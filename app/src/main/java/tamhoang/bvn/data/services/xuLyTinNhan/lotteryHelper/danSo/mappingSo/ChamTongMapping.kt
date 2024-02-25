package tamhoang.bvn.data.services.xuLyTinNhan.lotteryHelper.danSo.mappingSo

import tamhoang.bvn.constants.Const
import tamhoang.bvn.util.extensions.countTrueIndex
import tamhoang.bvn.util.ld.CongThuc

class ChamTongMapping: MappingNumber() {
    override fun condition(str: String) = str.indexOf("cham tong") in 0..4

    override fun convert(originMsg: String, currentMsg: String, index: Int): String {
        var str = currentMsg
        if (str.length <= 10) {
            error = "${Const.KHONG_HIEU} $originMsg"
            return ""
        } else if (str.substring(0, 11).contains("cham tong")) {
            for (i in 0..1) {
                val iCham = str.indexOf("cham tong", i)
                if (iCham == -1) {
                    break
                }

                val eCham = str.countTrueIndex(iCham + 10) {
                    "0123456789, ".contains(it)
                }

                val so = if (str.length > 10)
                    str.substring(iCham, eCham).replace("cham tong", "")
                        .trim()
                        .replace(" ", ",")
                else
                    ""

                if (so.isEmpty()) {
                    return "${Const.KHONG_HIEU} " + str.substring(iCham, eCham)
                }
                val danArr3 = so.split(",").filter { it.isNotEmpty() }

                danArr3.takeLast(danArr3.size - 1).forEach {
                    if (it.length in 2..3 && CongThuc.isNumeric(it) && !CongThuc.isNumericComma(so))
                        return "${Const.KHONG_HIEU} " + str.substring(str.indexOf(it), eCham)
                    else if (!CongThuc.isNumeric(it))
                        return "${Const.KHONG_HIEU} " + str.substring(iCham, eCham)
                }
                val soDau1 = MappingFunc.layDau(so)
                if (soDau1.contains(Const.KHONG_HIEU)) {
                    error = "${Const.KHONG_HIEU} $originMsg"
                    return ""
                }
                val soDit1 = MappingFunc.layDit(so)
                if (soDit1.contains(Const.KHONG_HIEU)) {
                    error = "${Const.KHONG_HIEU} $originMsg"
                    return ""
                }
                val soTong = MappingFunc.layTong(so)
                if (soTong.contains(Const.KHONG_HIEU)) {
                    error = "${Const.KHONG_HIEU} $originMsg"
                    return ""
                }
                result += MappingFunc.lay100so()
                str = (str.substring(0, iCham) + " " + str.substring(eCham)).trim()
            }
            return str
        } else {
            error = "${Const.KHONG_HIEU} $originMsg"
            return ""
        }
    }

}