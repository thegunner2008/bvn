package tamhoang.bvn.data.services.xuLyTinNhan.lotteryHelper.danSo

import tamhoang.bvn.constants.Const.KHONG_HIEU
import tamhoang.bvn.data.entities.Lottery
import tamhoang.bvn.data.enum.TL
import tamhoang.bvn.data.services.xuLyTinNhan.lotteryHelper.danSo.theLoai.*
import tamhoang.bvn.util.extensions.clear

object DanSo {
    fun parse(item: Lottery): String {
        if (checkTheloaiError(item)) return getTheLoaiError(item)

        val danSo = when (item.theLoai) {
            TL.LoA, TL.Lo, TL.De, TL.DeA, TL.DeB, TL.DeC, TL.DeD, TL.Det, TL.HC ->
                LoDe.parse(getRawDanSo(item))
            TL.BC, TL.BCA ->
                BaCang.parse(getRawDanSo(item))
            TL.XN ->
                XienNhay.parse(item)
            TL.XI, TL.XIA ->
                Xien.parse(item)
            TL.XQ, TL.XQA ->
                XienQuay.parse(item)
            TL.XG2, TL.XG3, TL.XG4 ->
                XienGhep.parse(item)
            TL.NULL ->
                "$KHONG_HIEU " + item.duLieu
        }

        return if (danSo.trim().length == 10 && danSo.contains(KHONG_HIEU))
            "$KHONG_HIEU " + item.duLieu
        else
            danSo
    }

    private fun checkTheloaiError(item: Lottery) = item.left.trim().indexOf(item.theLoai.t) > 0

    private fun getRawDanSo(item: Lottery): String {
        var raw = item.left.clear(
            listOf("${item.theLoai.t}:", "${item.theLoai.t} :", item.theLoai.t)
        )
        if (item.theLoai.isDe())
            raw = raw.clear(listOf("${TL.De.t}:", "${TL.De.t} :", TL.De.t))
        return raw
    }

    private fun getTheLoaiError(item: Lottery) =
        "$KHONG_HIEU " +
                item.left.substring(
                    0,
                    item.left.indexOf(if (item.theLoai.isDe()) TL.De.t else item.theLoai.t)
                )
}