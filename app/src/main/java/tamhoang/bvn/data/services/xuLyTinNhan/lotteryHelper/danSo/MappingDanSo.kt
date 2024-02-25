package tamhoang.bvn.data.services.xuLyTinNhan.lotteryHelper.danSo

import tamhoang.bvn.constants.Const.KHONG_HIEU
import tamhoang.bvn.data.map.MapSo
import tamhoang.bvn.data.services.xuLyTinNhan.lotteryHelper.danSo.mappingSo.*
import tamhoang.bvn.util.extensions.clear
import tamhoang.bvn.util.extensions.indexOfs
import tamhoang.bvn.util.extensions.replaceMap
import tamhoang.bvn.util.ld.CongThuc

object MappingDanSo {
    fun all(input: String): String {
        var str = input
            .replaceMap(MapSo.clearChia)
            .replaceMap(MapSo.vietTat)
            .replaceMap(MapSo.kyTu)

        if (input.trim().length < 2) {
            return "$KHONG_HIEU "
        }
        var index = 0
        var originMsg = ""
        var daySo = ""
        loop0@ while (true) {
            index++
            if (index > 50) return "$KHONG_HIEU $input"
            if ((!str.contains(originMsg) || str.length != originMsg.length) && str.isNotEmpty()) {

                val listMapping = listOf(
                    TuDenMapping(),
                    GhepDitMapping(),
                    GhepDauMapping(),
                    GhepDDMapping(),
                    DanMapping(),
                    BojMapping(),
                    ChamTongMapping(),
                    ChamMapping(),
                    TongMapping(),
                    DauDitMapping(),
                    DauMapping(),
                    DitMapping(),
                    ToNhoMapping(),
                    ChanLeMapping(),
                    KepMapping(),
                    SoConMapping(),
                    RestNumber()
                )

                originMsg = str
                str = " " + str.trim() + " "

                for (mapping in listMapping) {
                    if (!mapping.condition(str)) continue
                    str = mapping.convert(originMsg, str, index)
                    if (mapping.error != null) {
                        return mapping.error!!
                    }
                    daySo += mapping.result
                    index = mapping.newIndex ?: index
                    if (str.trim().length <= 2) break@loop0
                }

            }
        }
        return if (str.clear(" ").isEmpty()) {
            daySo.ifEmpty { "$KHONG_HIEU $input" }
        } else {
            xuLyKyTuThua(input, str, daySo)
        }
    }

    private fun xuLyKyTuThua(input: String, str: String, daySo: String): String {
        var daySo2 = daySo
        val str2 = str.trim()
            .clear(" . ").clear(" , ").clear("/")
            .replace(":", " ")
            .replace(";", " ")
            .replace(".", ",")
            .replace(" ", ",")
        if (!str.contains("so") || str.indexOf("so") >= 3) {
            if (str.contains("con") && str.indexOf("con") < 4) {
                if (str2.length <= 4) {
                    return "$KHONG_HIEU $input"
                } else if (!str.substring(0, 5).contains("con")) {
                    return "$KHONG_HIEU $input"
                }
            }
        } else if (str2.length <= 3) {
            return "$KHONG_HIEU $input"
        } else if (!str.substring(0, 4).contains("so")) {
            return "$KHONG_HIEU $input"
        }
        val listSo = str2.split(",")

        var soError: String? = null

        for (so in listSo) {
            if (so.isEmpty()) continue
            if (!CongThuc.isNumeric(so)) {
                soError = so
                break
            }
            if (so.length == 2)
                daySo2 += "$so,"
            else if (so.length == 3 && so[0] != so[1] && so[0] == so[2])
                daySo2 += so.substring(0, 2) + "," + so.substring(1, 3) + ","
            else {
                soError = so
                break
            }
        }
        if (soError == null) return daySo2.ifEmpty { "$KHONG_HIEU $input" }

        val str1 = "  $str  "
        return if (str2.trim().length > 10) {
            if (soError.length == 1) {
                str1.indexOfs(soError)
                    .filter { str1.substring(it).contains(soError) }
                    .forEach { _ ->
                        listOf(
                            " $soError ",
                            " $soError,",
                            ",$soError ",
                            ",$soError,"
                        ).forEach {
                            if (str1.contains(it)) return "$KHONG_HIEU $it"
                        }
                    }
            }
            "$KHONG_HIEU $soError"
        } else if (str.trim().length == 1) {
            "$KHONG_HIEU  " + str.trim() + " "
        } else {
            "$KHONG_HIEU " + str.trim()
        }
    }
}
