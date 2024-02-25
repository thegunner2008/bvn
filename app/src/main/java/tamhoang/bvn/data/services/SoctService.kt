package tamhoang.bvn.data.services

import org.json.JSONObject
import tamhoang.bvn.data.BaseStore
import tamhoang.bvn.data.DbOpenHelper
import tamhoang.bvn.data.enum.TL
import tamhoang.bvn.data.model.SoctS
import tamhoang.bvn.data.setting.TLGiu
import tamhoang.bvn.data.store.KhachHangStore
import tamhoang.bvn.data.store.SoctStore
import tamhoang.bvn.data.store.TinNhanStore

class SoctService(val db: DbOpenHelper) {
    companion object {
        lateinit var I: SoctService
    }

    private var jsonDanSo: JSONObject? = null

    @Throws(Throwable::class)
    fun nhapSoChiTiet(id: Int) {
        var soTien: String?
        var danSo: String?
        var diemDlyGiu: Int
        var diemKhachGiu: Int
        val (_, ngayNhan, _, typeKh, tenKh, sdt, _, soTinNhan, _, _, _, _, _, _, _, phanTich) = TinNhanStore.I.selectByID(
            id
        ) ?: return
        val queryCount =
            "ten_kh = '$tenKh' And ngay_nhan = '$ngayNhan' And type_kh = $typeKh And so_tin_nhan = $soTinNhan"
        val countSoctS = SoctStore.I.countWhere(queryCount)
        if (countSoctS > 0) return
        val (settingKH, settingGia) = KhachHangStore.I.getPairSettingByName(tenKh)

        var theLoaiTruoc: TL = TL.NULL
        jsonDanSo = JSONObject(phanTich ?: "{}")
        val listInsert: MutableList<SoctS> = ArrayList()
        val keys = jsonDanSo!!.keys()
        while (keys.hasNext()) {
            val dan = JSONObject(jsonDanSo!!.getString(keys.next()))
            danSo = dan.getString("dan_so")
            soTien = dan.getString("so_tien")
            val theLoaiDan = dan.getString("the_loai")

            var theLoai = TL.getTLToInsertSoct(theLoaiDan)
            if (theLoai == TL.NULL) theLoai = theLoaiTruoc

            var khachGiu = settingKH.khachGiu(TLGiu.fromTL(theLoai)).toDouble()
            var dlyGiu = settingKH.daiLyGiu(TLGiu.fromTL(theLoai)).toDouble()
            var gia = settingGia.getGia(theLoai)
            var lanAn = settingGia.getLanAn(theLoai)

            val diem = settingKH.quyDoiDiem(theLoai, soTien.toInt().toDouble())
            if (typeKh == 1) {
                diemDlyGiu = (diem * dlyGiu / 100.0).toInt()
                diemKhachGiu = (diem * khachGiu / 100.0).toInt()
            } else {
                dlyGiu = 0.0
                khachGiu = 0.0
                diemDlyGiu = 0
                diemKhachGiu = 0
            }
            val diemton = (diem - diemKhachGiu - diemDlyGiu).toInt()

            danSo.split(if (theLoai.isXien()) " " else ",")
                .filter { it.isNotBlank() }
                .map { it.trim() }
                .map { if (it.endsWith(",")) it.substring(0, it.length - 1) else it }
                .forEach { soChon ->
                    if (theLoai == TL.XI || theLoai == TL.XIA) {
                        gia = settingGia.getGiaXI(soChon)
                        lanAn = settingGia.getLanAnXI(soChon)
                    }
                    listInsert.add(
                        SoctS(
                            null, ngayNhan, typeKh, tenKh, sdt, soTinNhan, theLoai.t, soChon, diem, diem, khachGiu,
                            dlyGiu, diemton.toDouble(), gia * 1000.0, lanAn * 1000.0, 0.0,
                            diem * gia * 1000.0, 0.0
                        )
                    )
                }
            theLoaiTruoc = theLoai
        }
        BaseStore.insertListSoctS(listInsert)
    }

}