package tamhoang.bvn.data.services.xuLyTinNhan.lotteryHelper.danSo.theLoai

import tamhoang.bvn.constants.Const
import tamhoang.bvn.data.entities.Lottery
import tamhoang.bvn.data.enum.TL
import tamhoang.bvn.data.services.xuLyTinNhan.lotteryHelper.danSo.MappingDanSo
import tamhoang.bvn.util.extensions.clear
import tamhoang.bvn.util.ld.CongThuc

object XienNhay {
    fun parse(item: Lottery): String {
        var danSo: String
        var noidung = item.left.clear(listOf("xn", "xn:", "xn :"))
        if (item.left.contains("xn 2 ")) {
            noidung = MappingDanSo.all(noidung.trim().substring(2))
            danSo = XienFunc.xienGhep(noidung, TL.XG2).joinToString(" ") + " "
        } else {
            noidung = MappingDanSo.all(noidung.replace("xn", " "))
            danSo = XienFunc.xien("2 " + noidung.trim())
        }

        val arrXien = danSo.split(" ").filter { it.isNotEmpty() }
        arrXien.forEach { xien ->
            val soXien = MappingDanSo.all(xien)
            if (soXien.length in 5..6 && !soXien.contains(Const.KHONG_HIEU)) {
                val check = soXien.split(",").filter { it.isNotEmpty() }
                    .any { it.length != 2 || !CongThuc.isNumeric(it) }
                if (check) {
                    danSo = "${Const.KHONG_HIEU} " + if (danSo.length > 4) item.left else item.duLieu
                }
            }
        }

        if (!danSo.contains(Const.KHONG_HIEU)) {
            danSo = ""
            for (xien in arrXien) {
                val soxien = MappingDanSo.all(xien)
                if (soxien.contains(Const.KHONG_HIEU)) {
                    break
                }
                val check = soxien.split(",").filter { it.isNotEmpty() }
                    .any {
                        soxien.length - soxien.replace(it, "").length > 2
                    }
                if (soxien.length !in 5..6 || check) {
                    danSo = "${Const.KHONG_HIEU} " + item.left
                    break
                } else {
                    danSo += CongThuc.sortXien(soxien) + " "
                }
            }
        }
        return danSo
    }
}