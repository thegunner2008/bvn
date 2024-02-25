package tamhoang.bvn.data.services.xuLyTinNhan.lotteryHelper.danSo.mappingSo

import tamhoang.bvn.constants.Const

class SoConMapping : MappingNumber() {
    override fun condition(str: String) = str.contains("so") && str.indexOf("so") < 7
            || str.contains("con") && str.indexOf("con") < 7

    override fun convert(originMsg: String, currentMsg: String, index: Int): String {
        var str = currentMsg
        if (str.contains("so") && str.indexOf("so") < 7) {
            if (str.trim().length <= 3 || !str.trim().substring(0, 2).contains("so")) {
                error = "${Const.KHONG_HIEU} $originMsg"
                return ""
            }
            str = str.trim().substring(str.trim().indexOf("so") + 2)

        } else if (str.contains("con") && str.indexOf("con") < 4) {
            if (str.trim().length <= 4 || !str.substring(0, 5).contains("con")) {
                error = "${Const.KHONG_HIEU} $originMsg"
                return ""
            }
            str = str.substring(str.indexOf("con") + 3)
        }
        return str
    }
}