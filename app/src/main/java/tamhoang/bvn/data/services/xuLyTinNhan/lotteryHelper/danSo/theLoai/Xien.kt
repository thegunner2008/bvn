package tamhoang.bvn.data.services.xuLyTinNhan.lotteryHelper.danSo.theLoai

import tamhoang.bvn.constants.Const
import tamhoang.bvn.data.entities.Lottery
import tamhoang.bvn.data.services.xuLyTinNhan.lotteryHelper.danSo.MappingDanSo
import tamhoang.bvn.util.ld.CongThuc

object Xien {
    fun parse(item: Lottery): String {
        var danSo: String
        val danXi = item.left.split(item.theLoai.t).filter { it.isNotBlank() }

        danSo = if (danXi.size > 2) {
            danXi.filter { it.length > 4 }.joinToString(" ") {
                val xuly = MappingDanSo.all(it)
                if (xuly.contains(Const.KHONG_HIEU)) "${Const.KHONG_HIEU} $it"
                else xuly
            }
        } else item.left.replace(item.theLoai.t, "")

        if (danXi.size < 3) danSo = XienFunc.xien(danSo.trim())

        val arrXien3 = danSo.split(" ").filter { it.isNotBlank() }
            .map { it.replace(",", "") }
        val check = arrXien3.any { it.length != 2 || !CongThuc.isNumeric(it) }

        if (!check && arrXien3.size < 5) {
            danSo = MappingDanSo.all(danSo)
        }
        val arrXien4 = danSo.split(" ").filter { it.isNotBlank() }
        for (e in arrXien4) {
            val ss2 = MappingDanSo.all(e)
            if (ss2.length in 5..12 && !ss2.contains(Const.KHONG_HIEU)) {
                val check1 = ss2.split(",").filter { it.isNotEmpty() }
                    .all { it.length == 2 && CongThuc.isNumeric(it) }

                if (!check1) {
                    danSo =
                        "${Const.KHONG_HIEU} " + if (danSo.length > 4) item.left else item.duLieu
                }

            }
            if (e.length < 4) {
                danSo = "${Const.KHONG_HIEU} " + item.left
            }
            if (!danSo.contains(Const.KHONG_HIEU)) {
                danSo = ""
                var i = 0
                var soxien2 = ""
                while (i < arrXien4.size) {
                    try {
                        soxien2 = MappingDanSo.all(arrXien4[i])
                    } catch (e2: java.lang.Exception) {
                        danSo = "${Const.KHONG_HIEU} " + arrXien4[i]
                    }
                    if (soxien2.contains(Const.KHONG_HIEU)) {
                        break
                    }
                    var checkTrung = false
                    for (str2 in soxien2.split(",").filter { it.isNotEmpty() }) {
                        if (soxien2.length - soxien2.replace(str2, "").length > 2
                        ) {
                            checkTrung = true
                        }
                    }
                    if (soxien2.length < 5 || soxien2.length > 12 || checkTrung) {
                        danSo = "${Const.KHONG_HIEU} " + item.left
                        break
                    } else {
                        danSo += CongThuc.sortXien(soxien2) + " "
                        i++
                    }
                }
            }
        }
        return danSo
    }
}