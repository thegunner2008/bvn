package tamhoang.bvn.data.entities

import org.json.JSONObject
import tamhoang.bvn.constants.Const.KHONG_HIEU
import tamhoang.bvn.data.enum.TL
import tamhoang.bvn.util.extensions.contains
import tamhoang.bvn.util.extensions.highlightError

//duLieu: de 11, 22 x10 | theLoai: de| dau: de 11, 22| cuoi: x10| danSo: 11, 22| soTien: 10

data class Lottery(
    var duLieu: String = "",
    var theLoai: TL = TL.NULL,
    var left: String = "",
    var right: String = "",
    var danSo: String = "",
    var soTien: String = ""
) {
    fun toJsonData(
        dulieu: String = this.duLieu,
        theLoai: TL = this.theLoai,
        danSo: String = this.danSo,
        soLuong: String = "${danSo.split(",").filter { it.isNotBlank() }.size} số."
    ) = JSONObject().apply {
        put("du_lieu", dulieu)
        put("the_loai", theLoai.str)
        put("dan_so", danSo)
        put("so_tien", soTien)
        put("so_luong", soLuong)
    }

    fun checkLoiTien(checkLoiDv: Boolean) {
        if (duLieu.isEmpty() || right.isEmpty()) return
        try {
            if (checkLoiDv) checkLoiDonVi()
            if (soTien.contains(KHONG_HIEU) && soTien.trim().length < 13) {
                duLieu = duLieu.highlightError(soTien.substring(11))
            } else if (soTien.contains(KHONG_HIEU) && soTien.trim().length > 12) {
                duLieu = duLieu.highlightError(soTien)
            }
        } catch (e: java.lang.Exception) {
            duLieu = duLieu.highlightError(soTien)
        }
    }

    private fun checkLoiDonVi() {
        if (theLoai.isLo() && right.contains(listOf("n", "k", "tr")))
            soTien = "$KHONG_HIEU $right"
        if (theLoai.isDe() && right.contains("d"))
            soTien = "$KHONG_HIEU $right"
    }

    fun checkX10Xien(caiDatXienNhan: Int) {
        if (theLoai == TL.NULL || right.isEmpty() || soTien.isEmpty()) return
        if (theLoai.isXien() && !soTien.contains(KHONG_HIEU)) {
            try {
                if (caiDatXienNhan == 1 && right.contains("d"))
                    soTien = (soTien.toInt() * 10).toString() + ""
                else if (caiDatXienNhan == 2)
                    soTien = (soTien.toInt() * 10).toString() + ""
            } catch (e: Exception) {
                soTien = "$KHONG_HIEU $right"
            }
        }
    }
}
