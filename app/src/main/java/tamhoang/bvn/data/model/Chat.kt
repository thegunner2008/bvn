package tamhoang.bvn.data.model

import android.content.ContentValues
import android.database.Cursor
import java.io.Serializable

data class Chat (
    var ID: Int?,
    var ngay_nhan: String,
    var gio_nhan: String,
    var type_kh: Int,
    var ten_kh: String,
    var so_dienthoai: String,
    var use_app: String,
    var nd_goc: String,
    var del_sms: Int): Serializable {

    companion object {
        const val TABLE_NAME = "Chat_database"
        const val NGAY_NHAN = "ngay_nhan"
        const val GIO_NHAN = "gio_nhan"
        const val TYPE_KH = "type_kh"
        const val TEN_KH = "ten_kh"
        const val SO_DIENTHOAI = "so_dienthoai"
        const val USE_APP = "use_app"
        const val ND_GOC = "nd_goc"
        const val DEL_SMS = "del_sms"

        fun parseCursor(cursor: Cursor): Chat {
            return Chat(
                ID = cursor.getInt(0),
                ngay_nhan = cursor.getString(1),
                gio_nhan = cursor.getString(2),
                type_kh = cursor.getInt(3),
                ten_kh = cursor.getString(4),
                so_dienthoai = cursor.getString(5),
                use_app = cursor.getString(6),
                nd_goc = cursor.getString(7),
                del_sms = cursor.getInt(8)
            )
        }

        fun toContentValues(chat: Chat): ContentValues {
            val values = ContentValues()
            chat.apply {
                values.put("ID", ID)
                values.put(NGAY_NHAN, ngay_nhan)
                values.put(GIO_NHAN, gio_nhan)
                values.put(TYPE_KH, type_kh)
                values.put(TEN_KH, ten_kh)
                values.put(SO_DIENTHOAI, so_dienthoai)
                values.put(USE_APP, use_app)
                values.put(ND_GOC, nd_goc)
                values.put(DEL_SMS, del_sms)

            }
            return values
        }
    }
}