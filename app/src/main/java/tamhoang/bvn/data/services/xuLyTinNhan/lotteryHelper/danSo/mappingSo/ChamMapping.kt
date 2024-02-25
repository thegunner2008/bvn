package tamhoang.bvn.data.services.xuLyTinNhan.lotteryHelper.danSo.mappingSo

import tamhoang.bvn.constants.Const
import tamhoang.bvn.util.extensions.countTrueIndex
import tamhoang.bvn.util.ld.CongThuc

class ChamMapping: MappingNumber() {
    override fun condition(str: String) = str.indexOf("cham") in 0..4

    override fun convert(originMsg: String, currentMsg: String, index: Int): String {
        var str = currentMsg
        if (str.length >= 6) {
            var i = -1
            while (true) {
                val iCham = str.indexOf("cham", i + 1)
                if (iCham == -1) {
                    break
                }

                val eCham = str.countTrueIndex(iCham + 5) {
                    "0123456789, ".contains(it)
                }
                val so = if (str.length > 5)
                    str.substring(iCham, eCham).replace("cham", "")
                        .trim()
                        .replace(" ", ",")
                else
                    ""

                if (so.isEmpty()) {
                    error = "${Const.KHONG_HIEU} " + str.substring(iCham, eCham)
                    return ""
                }
                val danArr = so.split(",").filter { it.isNotEmpty() }

                danArr.takeLast(danArr.size - 1)
                    .forEach {  // 2 hoac 3 chu so hoac khong phai so => khong hiu
                        if (it.length in 2..3 && CongThuc.isNumeric(it)) {
                            error = "${Const.KHONG_HIEU} " + str.substring(str.indexOf(it), eCham)
                            return ""
                        }
                        else if (!CongThuc.isNumeric(it)) {
                            error = "${Const.KHONG_HIEU} " + str.substring(iCham, eCham)
                            return ""
                        }
                    }
                val ghepDau = MappingFunc.layDau(so)
                if (ghepDau.contains(Const.KHONG_HIEU)) {
                    error = "${Const.KHONG_HIEU} $str"
                    return ""
                }
                val ghepDit = MappingFunc.layDit(so)
                if (ghepDit.contains(Const.KHONG_HIEU)) {
                    error = "${Const.KHONG_HIEU} $originMsg"
                    return ""
                }
                result += MappingFunc.lay100so {
                    ghepDau.contains(it) || ghepDit.contains(it)
                }
                str = (str.substring(0, iCham) + " " + str.substring(eCham)).trim()
                i = 0
            }
            return str
        } else {
            error = "${Const.KHONG_HIEU} $originMsg"
            return ""
        }
    }
}