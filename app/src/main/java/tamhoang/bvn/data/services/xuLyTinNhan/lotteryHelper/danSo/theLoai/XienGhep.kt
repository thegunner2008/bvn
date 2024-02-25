package tamhoang.bvn.data.services.xuLyTinNhan.lotteryHelper.danSo.theLoai

import tamhoang.bvn.constants.Const
import tamhoang.bvn.data.entities.Lottery
import tamhoang.bvn.data.services.xuLyTinNhan.lotteryHelper.danSo.MappingDanSo

object XienGhep {
    fun parse(item: Lottery): String {
        var danSo: String
        val noidung = item.left.replaceFirst("${item.theLoai.t} ", "")

        danSo = MappingDanSo.all(noidung)

        val checkTrung = danSo.split(",").filter { it.isNotEmpty() }.any {
            danSo.length - danSo.replace(it, "").length > 2
        }

        if (checkTrung) {
            danSo = "${Const.KHONG_HIEU} " + item.left
        }
        if (!danSo.contains(Const.KHONG_HIEU)) {
            danSo = XienFunc.xienGhep(danSo, item.theLoai).joinToString(" ")
        }
        return danSo
    }
}