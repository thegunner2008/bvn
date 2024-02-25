package tamhoang.bvn.data.services.xuLyTinNhan

import tamhoang.bvn.constants.Const
import tamhoang.bvn.data.DbOpenHelper
import tamhoang.bvn.data.entities.Lottery
import tamhoang.bvn.data.services.xuLyTinNhan.lotteryHelper.LotteryHelper
import tamhoang.bvn.data.model.TinNhanS
import tamhoang.bvn.data.services.SoctService
import tamhoang.bvn.data.store.KhachHangStore
import tamhoang.bvn.data.store.ThayTheStore
import tamhoang.bvn.data.store.TinNhanStore
import tamhoang.bvn.messageCenter.notification.NotificationReader
import tamhoang.bvn.util.extensions.convertLatin
import tamhoang.bvn.util.extensions.highlightError
import tamhoang.bvn.util.ld.CongThuc

class XuLyTinNhanService(val db: DbOpenHelper) {
    companion object {
        lateinit var I: XuLyTinNhanService
    }

    fun upsertTinNhan(id: Int, type_kh: Int) {
        val tinNhan = TinNhanStore.I.selectByID(id) ?: return
        if (tinNhan.phatHienLoi.contains("ok")) {
            reInsertDb(tinNhan)
            return
        }
        val ndPhanTich = mappingKeyword(tinNhan)

        if (checkErrorAfterMapping(tinNhan.copy(ndPhanTich = ndPhanTich))) return

        var convertError: String? = null
        val lotteries = convertToLotteries(tinNhan.copy(ndPhanTich = ndPhanTich), onError = {
            convertError = it
        })

        if (checkErrorConvertData(lotteries = lotteries, idTinNhan = id, convertError = convertError)) return

        checkOverTimeAndInsertDb(lotteries, tinNhan.copy(ndPhanTich = ndPhanTich, ndSua = ndPhanTich), type_kh)

        SoctService.I.nhapSoChiTiet(id)
    }

    private fun reInsertDb(tinNhan: TinNhanS) {
        val ndPhantich = tinNhan.ndPhanTich.replace("*", "")
        val id = tinNhan.ID ?: return
        TinNhanStore.I.update(id, ndPhanTich = ndPhantich, ndSua = ndPhantich)
        SoctService.I.nhapSoChiTiet(id)
        TinNhanStore.I.update(id, phatHienLoi = "ok")
    }

    private fun mappingKeyword(tinNhan: TinNhanS): String {
        var output = CongThuc.fixDanSo(tinNhan.ndPhanTich.convertLatin())

        ThayTheStore.I.getList().forEach {
            output = output
                .replace(it.first, it.second).replace("  ", " ")
        }

        output = CongThuc.fixDanSo(output)
        output = MappingTin.fromMapKytu(output)
        output = MappingTin.fromJsonFile(output)

        output = MappingTin.det(output, tenKh = tinNhan.tenKh)
        output = MappingTin.removeOkTin(output)
        output = MappingTin.xuLyDauX(output)

        return output
    }

    private fun checkErrorAfterMapping(tinNhan: TinNhanS): Boolean {
        val id = tinNhan.ID ?: return true
        val ndPhanTich = tinNhan.ndPhanTich

        if (ndPhanTich.contains(Const.KHONG_HIEU)) {
            val error = CheckTin.loiBo(ndPhanTich)
            TinNhanStore.I.update(id, ndPhanTich = ndPhanTich, ndSua = ndPhanTich, phatHienLoi = error)
            NotificationReader.createNotification(ndPhanTich, db.mcontext)
            return true
        }

        val theLoaiError = CheckTin.loiTheLoai(ndPhanTich)

        if (theLoaiError.contains(Const.KHONG_HIEU)) {
            val duLieuError = ndPhanTich.highlightError(theLoaiError.substring(11))

            val error = if (theLoaiError.contains("${Const.KHONG_HIEU} dạng"))
                "${Const.KHONG_HIEU} " + ndPhanTich.substring(0, 5)
            else
                theLoaiError

            TinNhanStore.I.update(id, ndPhanTich = duLieuError, phatHienLoi = error)
            NotificationReader.createNotification(error, db.mcontext)
            return true
        }

        return false
    }

    private fun convertToLotteries(tinNhan: TinNhanS, onError: (String) -> Unit): MutableList<Lottery> {
        val ndPhanTich = tinNhan.ndPhanTich
        val settingKH = KhachHangStore.I.getSettingKHByName(tinNhan.tenKh)

        val duLieuSplit = LotteryHelper.splitToDuLieu(ndPhanTich)

        return LotteryHelper.convertFromListDulieu(
            listDuLieu = duLieuSplit.listDuLieu,
            settingKH = settingKH,
            kyTuThua = duLieuSplit.kyTuThua,
            onError = onError
        )
    }

    private fun checkOverTimeAndInsertDb(lotteries: MutableList<Lottery>, tinNhan: TinNhanS, type_kh: Int) {
        val id = tinNhan.ID ?: return
        val settingKH = KhachHangStore.I.getSettingKHByName(tinNhan.tenKh)

        val quaGioLo = CongThuc.checkTime(settingKH.tgLoXien) && !CongThuc.checkTime("18:30") && type_kh == 1

        val (content, jsonData) = LotteryHelper.convertLotteriesToData(
            lotteries,
            quaGioLo = quaGioLo
        )

        val hasLo = lotteries.any { it.theLoai.isLo() }
        val hasXien = lotteries.any { it.theLoai.isXien() }
        val hasNhat = lotteries.any { it.theLoai.isNhat() }

        if (quaGioLo && (hasLo || hasXien || hasNhat)) {
            var newContent = "Bỏ "
            if (hasLo) newContent += "lô,"
            if (hasXien) newContent += "xiên,"
            if (hasNhat) newContent += "giải nhất"
            newContent += " vì quá giờ!\n$content"
            TinNhanStore.I.update(id, ndPhanTich = newContent, phatHienLoi = "ok")
            return
        }

        if (jsonData.length() > 0)
            TinNhanStore.I.update(id, ndPhanTich = content, phanTich = jsonData.toString(), phatHienLoi = "ok")
        else {
            val ndPtLoi = Const.LDPRO + content + Const.FONT
            val phLoi = "${Const.KHONG_HIEU} $content"
            TinNhanStore.I.update(id, ndPhanTich = ndPtLoi, phatHienLoi = phLoi)
            NotificationReader.createNotification(phLoi, db.mcontext)
        }
    }

    private fun checkErrorConvertData(lotteries: MutableList<Lottery>, idTinNhan: Int, convertError: String?): Boolean {
        var error = convertError
        val ndPhantichLoi = lotteries
            .filter { it.duLieu.isNotEmpty() }
            .joinToString {
                if (it.danSo.isEmpty()) {
                    it.danSo = Const.KHONG_HIEU + it.duLieu
                }
                if (it.danSo.contains(Const.KHONG_HIEU) || it.soTien.contains(Const.KHONG_HIEU)) {
                    error =
                        if (it.danSo.contains(Const.KHONG_HIEU)) it.danSo else it.soTien
                    if (!it.duLieu.contains(Const.LDPRO)) {
                        val str = error!!.replace(Const.KHONG_HIEU, "").trim()
                        it.duLieu = it.duLieu.replace(str, Const.LDPRO + str + Const.FONT)
                    }
                }
                it.duLieu
            }

        if (error != null) {
            TinNhanStore.I.update(idTinNhan, ndPhanTich = ndPhantichLoi, phatHienLoi = error!!)
            NotificationReader.createNotification(error, db.mcontext)
        }

        return error != null
    }
}