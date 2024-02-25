package tamhoang.bvn.data.services.xuLyTinNhan.lotteryHelper.danSo.theLoai

import android.util.Log
import tamhoang.bvn.constants.Const
import tamhoang.bvn.data.entities.Lottery
import tamhoang.bvn.data.services.xuLyTinNhan.lotteryHelper.danSo.MappingDanSo
import tamhoang.bvn.util.ld.CongThuc

object XienQuay {
    fun parse(item: Lottery): String {
        var danSo: String
        val danXi = item.left.split(item.theLoai.t).filter { it.isNotBlank() }
        val noidung =
            if (danXi.size > 2)
                danXi.filter { (it.length > 4) }.joinToString(" ") { MappingDanSo.all(it) }
            else
                item.left.replace(item.theLoai.t, "")

        danSo =
            if (noidung.contains(Const.KHONG_HIEU))
                "${Const.KHONG_HIEU} ${item.left}"
            else
                XienFunc.xien(noidung.trim())

        val splits = danSo.split(" ").filter { it.isNotBlank() }

        splits.forEach { s ->
            val ss = MappingDanSo.all(s)
            if (ss.length in 8..12 && !ss.contains(Const.KHONG_HIEU)) {
                val check = ss.split(",").filter { it.isNotEmpty() }
                    .all {
                        it.length == 2 || CongThuc.isNumeric(it)
                    }

                if (!check) {
                    danSo = "${Const.KHONG_HIEU} " + if (danSo.length > 4) item.left else item.duLieu
                }
            } else {
                danSo = "${Const.KHONG_HIEU} " + item.duLieu
                return@forEach
            }
        }
        try {
            if (!danSo.contains(Const.KHONG_HIEU)) {
                danSo = ""
                var soxien3 = ""

                splits.forEach {
                    try {
                        soxien3 = MappingDanSo.all(it)
                    } catch (e3: Exception) {
                        danSo = "${Const.KHONG_HIEU} $it"
                    }
                    if (soxien3.contains(Const.KHONG_HIEU)) {
                        return@forEach
                    }
                    val checkTrung =
                        soxien3.split(",").filter { it.isNotEmpty() }.any { e ->
                            soxien3.length - soxien3.replace(e, "").length > 2
                        }
                    if (soxien3.length !in 5..12 || checkTrung) {
                        danSo = "${Const.KHONG_HIEU} " + item.left
                        return@forEach
                    } else {
                        danSo += CongThuc.sortXien(soxien3) + " "
                    }
                }
            }
        } catch (throwable: Throwable) {
            Log.e("Error", throwable.message.toString())
        }
        return danSo
    }
}