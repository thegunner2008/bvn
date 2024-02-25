package tamhoang.bvn.data.services.xuLyTinNhan.lotteryHelper.danSo.mappingSo

import tamhoang.bvn.constants.Const
import tamhoang.bvn.util.extensions.countIndex
import tamhoang.bvn.util.extensions.insert
import tamhoang.bvn.util.ld.CongThuc
import kotlin.math.max

class RestNumber : MappingNumber() {
    private var iSo = 0

    override fun condition(str: String): Boolean {
        iSo = str.countIndex(0) {
            ", ".contains(it) || it.isDigit()
        }
        iSo = max(0, iSo)
        return str.substring(0, iSo).length > 2
    }

    override fun convert(originMsg: String, currentMsg: String, index: Int): String {
        var str = currentMsg
        val array = str.substring(0, iSo).trim()
            .replace(" ", ",")
            .split(",").filter { it.isNotEmpty() }
        var i = 0
        while (i < array.size) {
            val item = array[i]
            if (CongThuc.isNumeric(item) && item.length == 2) {
                result += "$item,"
            } else if (CongThuc.isNumeric(item) && item.length == 4) {
                if (item.substring(2, 4).contains("00")) {
                    error = "${Const.KHONG_HIEU} $item"
                    return ""
                }
                result += item.insert(2, ",") + ","
            } else if (CongThuc.isNumeric(item) && item.length == 3) {
                if (item[0] != item[2]) {
                    error = "${Const.KHONG_HIEU} $item"
                    return ""
                } else if (item[0] == item[1] && item[0] == item[2]) {
                    error = "${Const.KHONG_HIEU} ,$item,"
                    return ""
                }
                result += item.substring(0, 2) + "," + item.substring(1, 3) + ","
            } else if (item.isNotEmpty()) {
                if (item.length != 1) {
                    error = "${Const.KHONG_HIEU} $item"
                    return ""
                }

                val kytuktra = listOf(
                    " $item ",
                    " $item,",
                    ",$item ",
                    ",$item,"
                ).firstOrNull {
                    str.contains(it)
                } ?: ""
                error = "${Const.KHONG_HIEU} $kytuktra"
                return ""
            }

            i++
        }

        if (i < array.size) {
            val item = array[i]
            str = "  $str  "
            val str2 = str.trim()
            if (str2.trim().length > 10) {
                if (item.length == 1) {
                    iSo = -1
                    while (true) {
                        val indexOf = str.indexOf(item, iSo + 1)
                        iSo = indexOf
                        if (indexOf == -1) break

                        val kytuktra = listOf(
                            " $item ",
                            " $item,",
                            ",$item ",
                            ",$item,"
                        ).firstOrNull {
                            str.contains(it)
                        }
                        if (kytuktra != null) {
                            error = "${Const.KHONG_HIEU} $kytuktra"
                            return ""
                        }
                    }
                } else {
                    return "${Const.KHONG_HIEU} $item"
                }
            } else if (str.trim().length == 1) {
                error = "${Const.KHONG_HIEU}  " + str.trim() + " "
                return ""
            } else {
                error = "${Const.KHONG_HIEU} " + str.trim()
                return ""
            }
        }
        return str.substring(iSo).trim()
    }
}