package tamhoang.bvn.data.services.xuLyTinNhan.lotteryHelper.danSo.theLoai

import tamhoang.bvn.constants.Const
import tamhoang.bvn.data.enum.TL
import tamhoang.bvn.data.services.xuLyTinNhan.lotteryHelper.danSo.MappingDanSo
import tamhoang.bvn.util.extensions.clear
import tamhoang.bvn.util.extensions.combinations
import tamhoang.bvn.util.extensions.startsWith
import tamhoang.bvn.util.ld.CongThuc

object XienFunc {
    fun xien(str: String): String { //format: "12,34 56,78" "121,232" -> "12,21,23,32"
        val str1 = str.replace(", ", ",")
        var replace: String
        var strKQ = ""
        if (str1.length > 6) {
            val replaceAll = str1.trim { it <= ' ' }.replace("[,.;-]", " ")
            if (replaceAll.startsWith(listOf("2 ", "3 ", "4 "))) {
                val i4 = when (replaceAll[0]) {
                    '2' -> 5
                    '3' -> 8
                    '4' -> 11
                    else -> 0
                }

                var xulySo = MappingDanSo.all(replaceAll.substring(2))
                var i5 = 0
                while (xulySo.length - i5 > i4) {
                    val i6 = i5 + i4
                    i5 = i6 + 1
                    xulySo = xulySo.substring(0, i6) + " " + xulySo.substring(i5)
                    if (xulySo.substring(i5).length in (3 until i4)) {
                        xulySo = "${Const.KHONG_HIEU} " + xulySo.substring(i5)
                        break
                    }
                }
                return xulySo.trim()
            }
        }
        replace = if (str1.trim().startsWith("2 ")) str1.trim().clear("2 ") else str1.trim()

        if (replace.contains(";")) {
            val splitArr = replace.split(";").filter { it.isNotEmpty() }
            var kiemtra = true
            for (split in splitArr) {
                val split1 = split.replace(" ", ",")
                    .replace(",,", ",")
                val split2Arr = split1.split(",").filter { it.isNotEmpty() }
                for (s in split2Arr) {
                    val tach = CongThuc.tachXien(s)
                    if (tach == null) {
                        strKQ = ""
                        kiemtra = false
                        break
                    } else if (tach != "") {
                        replace = replace.replace(s, tach)
                    }
                }
                if (kiemtra) {
                    strKQ = "$strKQ$split1 "
                }
            }
        } else {
            strKQ = replace.replace(" ", "")
            var reCaculate = false
            val split = strKQ.split(",").filter { it.isNotEmpty() }
            for (s in split) {
                val tach = CongThuc.tachXien(s)
                if (tach == null) {
                    break
                } else if (tach != "") {
                    strKQ = strKQ.replace(s, tach)
                    reCaculate = true
                }
            }
            val kQTmp = if (reCaculate) strKQ else ""
            strKQ = ""
            val spaces = arrayOf(" ", ",")
            for (Space in spaces) {
                val splits = replace.split(Space).filter { it.isNotEmpty() }
                var kiemtra = true
                for (spl in splits) {
                    var trim = spl.trim()
                    if (trim.isEmpty()) break
                    if (trim.split(" ").size == 1) {
                        val split2 = trim.split(",").filter { it.isNotEmpty() }
                        if (split2.size == 1) {
                            strKQ = if (kQTmp.split(",").size > 1) kQTmp else ""
                            break
                        }
                        var str4 = trim
                        for (s in split2) {
                            val tach = CongThuc.tachXien(s)
                            if (tach == null) {
                                strKQ = ""
                                kiemtra = false
                                break
                            } else if (tach != "") {
                                str4 = str4.replace(s, tach)
                            }
                        }
                        if (kiemtra && str4.length > 4) {
                            strKQ = strKQ + str4.replace(" ", ",") + " "
                        }
                    } else {
                        val split7 = trim.split(" ").filter { it.isNotEmpty() }
                        if (split7.size == 1) {
                            strKQ = ""
                            break
                        }
                        for (s in split7) {
                            val tach = CongThuc.tachXien(s)
                            if (tach == null) {
                                strKQ = ""
                                kiemtra = false
                                break
                            } else if (tach != "") {
                                trim = trim.replace(s, tach)
                            }
                        }
                        if (kiemtra && trim.length > 4) {
                            strKQ = strKQ + trim.replace(" ", ",") + " "
                        }
                    }
                }
                if (strKQ.isNotEmpty()) {
                    break
                }
            }
        }
        return strKQ.trim()
    }

    fun xienGhep(str: String, tl: TL): List<String> {// Tạo tổ hợp các số xiên ghép
        val soGhep = when (tl) {
            TL.XG2 -> 2
            TL.XG3 -> 3
            TL.XG4 -> 4
            else -> 0
        }
        if (soGhep == 0) return emptyList()

        val list = str.split(",").filter { it != "" }
        return list.combinations(soGhep)
            .filter { it.toSet().size == soGhep }
            .map { it.sorted().joinToString(",") }
    }

    fun xienQuay(str: String): String {
        val soXien = str.split(",").filter { it.isNotEmpty() }
        val danXien = when (soXien.size) {
            3 -> {
                soXien.combinations(2).joinToString(" ") { it.joinToString(",") } + " " +
                        soXien[0] + "," + soXien[1] + "," + soXien[2]
            }
            4 -> {
                soXien.combinations(2).joinToString(" ") { it.joinToString(",") } + " " +
                        soXien.combinations(3).joinToString(" ") { it.joinToString(",") } + " " +
                        soXien[0] + "," + soXien[1] + "," + soXien[2] + "," + soXien[3]
            }
            else -> ""
        }

        return danXien
    }
}