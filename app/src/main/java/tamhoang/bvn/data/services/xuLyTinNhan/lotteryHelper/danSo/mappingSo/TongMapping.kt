package tamhoang.bvn.data.services.xuLyTinNhan.lotteryHelper.danSo.mappingSo

import tamhoang.bvn.constants.Const
import tamhoang.bvn.util.extensions.countIndex
import tamhoang.bvn.util.ld.CongThuc

class TongMapping: MappingNumber() {
    override fun condition(str: String) = str.indexOf("tong") in 0..4

    override fun convert(originMsg: String, currentMsg: String, index: Int): String {
        var str = currentMsg
        if (str.contains("tong 10")) {
            error = "${Const.KHONG_HIEU} tong 10"
            return ""
        }
        if (str.length >= 6) {
            var i = -1
            while (true) {
                val iTong = str.indexOf("tong", i + 1)
                if (iTong == -1) {
                    break
                }
                val eTong = (if (str.last().isDigit()) "$str " else str).countIndex(iTong + 5) {
                    ", ".contains(it) || it.isDigit()
                }

                val dan = if (str.substring(iTong, eTong).length > 5) {
                    str.substring(iTong, eTong).replace("tong", "")
                        .trim()
                        .replace(" ", ",")
                } else {
                    ""
                }
                if (dan.isEmpty()) {
                    error = "${Const.KHONG_HIEU} $originMsg"
                    return ""
                }
                val danArr = dan.split(",").filter { it.isNotEmpty() }

                danArr.forEachIndexed { i1, s ->
                    if (s.length == 2 && CongThuc.isNumeric(s) && i1 > 0) {
                        error = "${Const.KHONG_HIEU} " + str.substring(str.indexOf(s), eTong)
                        return ""
                    }
                    else if (!CongThuc.isNumeric(s)) {
                        error = "${Const.KHONG_HIEU} " + str.substring(str.indexOf(s), eTong)
                        return ""
                    }
                }

                val soTong = MappingFunc.layTong(dan)
                if (soTong.contains(Const.KHONG_HIEU)) {
                    error = "${Const.KHONG_HIEU} $originMsg"
                    return ""
                }
                result += soTong
                str = (str.substring(0, iTong) + " " + str.substring(eTong)).trim()
                i = 0
            }
            return str
        } else {
            error = "${Const.KHONG_HIEU} $originMsg"
            return ""
        }
    }
}