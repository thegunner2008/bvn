package tamhoang.bvn.data.model

import android.content.ContentValues
import android.database.Cursor
import java.io.Serializable

data class ChuyenThang (
    var ID: Int?,
    var kh_nhan: String,
    var sdt_nhan: String,
    var kh_chuyen: String,
    var sdt_chuyen: String): Serializable {

    companion object {
        const val TABLE_NAME = "tbl_chuyenthang"
        const val KH_NHAN = "kh_nhan"
        const val SDT_NHAN = "sdt_nhan"
        const val KH_CHUYEN = "kh_chuyen"
        const val SDT_CHUYEN = "sdt_chuyen"

        fun parseCursor(cursor: Cursor): ChuyenThang {
            return ChuyenThang(
                ID = cursor.getInt(0),
                kh_nhan = cursor.getString(1),
                sdt_nhan = cursor.getString(2),
                kh_chuyen = cursor.getString(3),
                sdt_chuyen = cursor.getString(4)
            )
        }

        fun toContentValues(chuyenThang: ChuyenThang): ContentValues {
            val values = ContentValues()
            chuyenThang.apply {
                values.put("ID", ID)
                values.put(KH_NHAN, kh_nhan)
                values.put(SDT_NHAN, sdt_nhan)
                values.put(KH_CHUYEN, kh_chuyen)
                values.put(SDT_CHUYEN, sdt_chuyen)
            }
            return values
        }
    }
}