package tamhoang.bvn.data.model

import android.content.ContentValues
import android.database.Cursor

data class TinNhanS (
    var ID: Int?,
    var ngayNhan: String,
    var gioNhan: String,
    var typeKh: Int,
    var tenKh: String,
    var sdt: String,
    var useApp: String,
    var soTinNhan: Int,
    var ndGoc: String,
    var ndSua: String?,
    var ndPhanTich: String,
    var phatHienLoi: String,
    var tinhTien: Int,
    var okTn: Int,
    var delSms: Int,
    var phanTich: String?) {
    companion object {
        const val TABLE_NAME = "tbl_tinnhanS"

        const val ID = "id"
        const val NGAY_NHAN = "ngay_nhan"
        const val GIO_NHAN = "gio_nhan"
        const val TYPE_KH = "type_kh"
        const val TEN_KH = "ten_kh"
        const val SO_DIENTHOAI = "so_dienthoai"
        const val USE_APP = "use_app"
        const val SO_TIN_NHAN = "so_tin_nhan"
        const val ND_GOC = "nd_goc"
        const val ND_SUA = "nd_sua"
        const val ND_PHANTICH = "nd_phantich"
        const val PHAT_HIEN_LOI = "phat_hien_loi"
        const val TINH_TIEN = "tinh_tien"
        const val OK_TN = "ok_tn"
        const val DEL_SMS = "del_sms"
        const val PHAN_TICH = "phan_tich"

        fun parseCursor(cursor: Cursor) = TinNhanS(
            ID = cursor.getInt(0),
            ngayNhan= cursor.getString(1),
            gioNhan = cursor.getString(2),
            typeKh = cursor.getInt(3),
            tenKh = cursor.getString(4),
            sdt = cursor.getString(5),
            useApp = cursor.getString(6),
            soTinNhan= cursor.getInt(7),
            ndGoc = cursor.getString(8),
            ndSua = cursor.getString(9),
            ndPhanTich = cursor.getString(10),
            phatHienLoi = cursor.getString(11),
            tinhTien = cursor.getInt(12),
            okTn = cursor.getInt(13),
            delSms = cursor.getInt(14),
            phanTich = cursor.getString(15))

        fun toContentValues(tinNhanS: TinNhanS): ContentValues {
            val values = ContentValues()
            tinNhanS.apply {
                values.put("ID", ID)
                values.put(NGAY_NHAN, ngayNhan)
                values.put(GIO_NHAN, gioNhan)
                values.put(TYPE_KH, typeKh)
                values.put(TEN_KH, tenKh)
                values.put(SO_DIENTHOAI, sdt)
                values.put(USE_APP, useApp)
                values.put(SO_TIN_NHAN, soTinNhan)
                values.put(ND_GOC, ndGoc)
                values.put(ND_SUA, ndSua)
                values.put(ND_PHANTICH, ndPhanTich)
                values.put(PHAT_HIEN_LOI, phatHienLoi)
                values.put(TINH_TIEN, tinhTien)
                values.put(OK_TN, okTn)
                values.put(DEL_SMS, delSms)
                values.put(PHAN_TICH, phanTich)
            }
            return values
        }
    }

}
