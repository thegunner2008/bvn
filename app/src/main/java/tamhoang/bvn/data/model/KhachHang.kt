package tamhoang.bvn.data.model

import android.content.ContentValues
import android.database.Cursor
import java.io.Serializable

data class KhachHang(
    var ten_kh: String,
    var sdt: String,
    var use_app: String,
    var type_kh: Int,
    var type_pt: Int,
    var tbl_MB: String,
    var tbl_XS: String): Serializable {

    companion object {
        const val TABLE = "tbl_kh_new"
        const val TEN_KH = "ten_kh"
        const val SDT = "sdt"
        const val USE_APP = "use_app"
        const val TYPE_KH = "type_kh"
        const val TYPE_PT = "type_pt"
        const val TBL_MB = "tbl_MB"
        const val TBL_XS = "tbl_XS"

        fun parseCursor(cursor: Cursor): KhachHang {
            return KhachHang(
                ten_kh = cursor.getString(0),
                sdt = cursor.getString(1),
                use_app = cursor.getString(2),
                type_kh = cursor.getInt(3),
                type_pt = cursor.getInt(4),
                tbl_MB = cursor.getString(5),
                tbl_XS = cursor.getString(6)
            )
        }

        fun toContentValues(khachhang: KhachHang): ContentValues {
            val values = ContentValues()
            khachhang.apply {
                values.put(TEN_KH, ten_kh)
                values.put(SDT, sdt)
                values.put(USE_APP, use_app)
                values.put(TYPE_KH, type_kh)
                values.put(TYPE_PT, type_pt)
                values.put(TBL_MB, tbl_MB)
                values.put(TBL_XS, tbl_XS)
            }
            return values
        }
    }
}