package tamhoang.bvn.data.services.xuLyTinNhan.lotteryHelper.danSo.mappingSo

import tamhoang.bvn.constants.Const
import tamhoang.bvn.data.map.MapSo
import tamhoang.bvn.util.extensions.clear
import tamhoang.bvn.util.extensions.contains

class ToNhoMapping : MappingNumber() {
    override fun condition(str: String) = str.indexOf("to ") in 0..4 || str.indexOf("nho") in 0..4

    override fun convert(originMsg: String, currentMsg: String, index: Int): String {
        var str = currentMsg
        var i = index
        if (str.contains("to") && str.contains("nho")) {
            while (true) {
                if (str.indexOf("to ") in 0..4 || str.indexOf("nho") in 0..4) {
                    var i1 = i + 1
                    if (i1 <= 100) {
                        if (str.indexOf("to") < str.indexOf("nho")) {
                            var iTn = -1
                            while (true) {
                                val indexOf = str.indexOf("to", iTn + 1)
                                iTn = indexOf
                                i = i1
                                if (indexOf == -1) break
                                var eTn = iTn + 1

                                while (eTn < str.length
                                    && !str.substring(iTn + 1, eTn).contains(listOf("to", "nho"))
                                ) {
                                    eTn++
                                }

                                val sss = str.substring(iTn, eTn).clear(listOf(" ", ","))
                                if (sss.contains("toto")) {
                                    str = str.substring(0, iTn) + " " + str.substring(eTn)
                                    result += MapSo.toNho["toto"]
                                    break
                                } else if (sss.contains("tonho")) {
                                    str = str.substring(0, iTn) + " " + str.substring(eTn)
                                    result += MapSo.toNho["tonho"]
                                    break
                                } else {
                                    i1 = i
                                }
                            }
                        } else {
                            i = i1
                            if (str.indexOf("nho") < str.indexOf("to")) {
                                var i = -1
                                while (true) {
                                    val indexOf = str.indexOf("nho", i + 1)
                                    i = indexOf
                                    if (indexOf == -1) {
                                        break
                                    }
                                    var i2 = i + 1
                                    while (i2 < str.length
                                        && !str.substring(i + 1, i2).contains("to")
                                        && !str.substring(i + 1, i2).contains("nho")
                                    ) {
                                        i2++
                                    }
                                    val sss = str.substring(i, i2)
                                        .replace(" ", "")
                                        .replace(",", "")
                                    if (sss.contains("nhoto")) {
                                        str = str.substring(0, i) + " " + str.substring(i2)
                                        result += MapSo.toNho["nhoto"]
                                        break
                                    } else if (sss.contains("nhonho")) {
                                        str = str.substring(0, i) + " " + str.substring(i2)
                                        result += MapSo.toNho["nhonho"]
                                        break
                                    }
                                }
                            }
                        }
                        if (!str.contains("to") || !str.contains("nho")) {
                            break
                        }
                    } else {
                        error = "${Const.KHONG_HIEU} $originMsg"
                        return ""
                    }
                }
            }
        }
        if (str.contains("to") && !str.contains("nho")) {
            val keys = listOf("toto", "to to")
            keys.forEach {
                if (str.contains(it)) {
                    result += MapSo.toNho["toto"]
                    str =
                        str.substring(0, str.indexOf(it)) + " " + str.substring(
                            str.indexOf(it) + it.length
                        )
                }
            }
        }
        if (!str.contains("to") && str.contains("nho")) {
            val keys = listOf("nhonho", "nho nho")
            keys.forEach {
                if (str.contains(it)) {
                    result += MapSo.toNho["nhonho"]
                    str =
                        str.substring(0, str.indexOf(it)) + " " + str.substring(
                            str.indexOf(it) + it.length
                        )
                }
            }
        }

        return str
    }
}