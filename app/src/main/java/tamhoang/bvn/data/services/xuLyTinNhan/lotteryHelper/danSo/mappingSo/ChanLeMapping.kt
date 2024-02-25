package tamhoang.bvn.data.services.xuLyTinNhan.lotteryHelper.danSo.mappingSo

import tamhoang.bvn.constants.Const
import tamhoang.bvn.data.map.MapSo

class ChanLeMapping : MappingNumber() {
    override fun condition(str: String) = str.contains("chan") || str.contains("le")
            && str.indexOf("chan") < 5 && str.indexOf("le") < 5

    override fun convert(originMsg: String, currentMsg: String, index: Int): String {
        var str = currentMsg
        newIndex = index

        var i39 = -1
        if (str.contains("chan") && str.contains("le")) {
            while (true) {
                if (str.indexOf("lechan") <= i39 && str.indexOf("chanle") <= i39 && str.indexOf(
                        "le chan"
                    ) <= i39 && str.indexOf("chan le") <= i39
                ) {
                    break
                }
                var demVonglap3 = newIndex!! + 1
                if (demVonglap3 <= 100) {
                    if (str.indexOf("chan") < str.indexOf("le")) {
                        var i119 = -1
                        while (true) {
                            val indexOf4 = str.indexOf("chan", i119 + 1)
                            i119 = indexOf4
                            if (indexOf4 == -1) {
                                newIndex = demVonglap3
                                break
                            }
                            var i213 = i119 + 1
                            while (i213 < str.length && str.substring(
                                    i119 + 1,
                                    i213
                                ).indexOf("chan") <= -1 && str.substring(
                                    i119 + 1,
                                    i213
                                ).indexOf("le") <= -1
                            ) {
                                i213++
                            }
                            val sss3 =
                                str.substring(i119, i213).replace(" ", "")
                                    .replace(",", "")
                            newIndex = demVonglap3
                            if (sss3.contains("chanchan")) {
                                str = str.substring(0, i119) + " " + str.substring(i213)
                                result += MapSo.chanLe["chanchan"]
                                break
                            } else if (sss3.contains("chanle")) {
                                str = str.substring(0, i119) + " " + str.substring(i213)
                                result += MapSo.chanLe["chanle"]
                                break
                            } else {
                                demVonglap3 = newIndex!!
                            }
                        }
                    } else {
                        newIndex = demVonglap3
                        if (str.indexOf("le") < str.indexOf("chan")) {
                            var i120 = -1
                            while (true) {
                                val indexOf5 = str.indexOf("le", i120 + 1)
                                i120 = indexOf5
                                if (indexOf5 == -1) {
                                    break
                                }
                                var i214 = i120 + 1
                                while (i214 < str.length && str.substring(
                                        i120 + 1,
                                        i214
                                    ).indexOf("chan") <= -1 && str.substring(
                                        i120 + 1,
                                        i214
                                    ).indexOf("le") <= -1
                                ) {
                                    i214++
                                }
                                val sss4 = str.substring(i120, i214)
                                    .replace(" ", "")
                                    .replace(",", "")
                                if (sss4.contains("lechan")) {
                                    str = str.substring(0, i120) + " " + str.substring(i214)
                                    result += MapSo.chanLe["chanle"]
                                    break
                                } else if (sss4.contains("lele")) {
                                    str = str.substring(0, i120) + " " + str.substring(i214)
                                    result += MapSo.chanLe["lele"]
                                    break
                                }
                            }
                        }
                    }
                    if (!str.contains("chan") || !str.contains("le")) {
                        break
                    }
                    i39 = -1
                } else {
                    error = "${Const.KHONG_HIEU} $originMsg"
                    return ""
                }
            }
        }
        if (str.contains("chan") && !str.contains("le")) {
            if (str.contains("chanchan")) {
                result += MapSo.chanLe["chanchan"]
                str = str.substring(0, str.indexOf("chanchan")) + " " + str.substring(
                    str.indexOf("chanchan") + 8
                )
            }
            if (str.contains("chan chan")) {
                result += MapSo.chanLe["chanchan"]
                str = str.substring(
                    0,
                    str.indexOf("chan chan")
                ) + " " + str.substring(str.indexOf("chan chan") + 9)
            }
        }
        if (!str.contains("chan") && str.contains("le")) {
            if (str.contains("lele")) {
                result += MapSo.chanLe["lele"]
                str =
                    str.substring(0, str.indexOf("lele")) + " " + str.substring(
                        str.indexOf("lele") + 4
                    )
            }
            if (str.contains("le le")) {
                str =
                    str.substring(0, str.indexOf("le le")) + " " + str.substring(
                        str.indexOf("le le") + 5
                    )
                result += MapSo.chanLe["lele"]
            }
        }

        return str
    }
}