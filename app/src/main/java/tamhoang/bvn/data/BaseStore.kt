package tamhoang.bvn.data

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.squareup.sqlbrite2.BriteDatabase
import com.squareup.sqlbrite2.SqlBrite
import io.reactivex.schedulers.Schedulers
import org.json.JSONException
import org.json.JSONObject
import tamhoang.bvn.data.services.XuatSoService
import tamhoang.bvn.data.model.*
import tamhoang.bvn.data.services.*
import tamhoang.bvn.data.services.TinhTienService
import tamhoang.bvn.data.services.guiTinNhan.GuiTinNhanService
import tamhoang.bvn.data.services.xuLyTinNhan.XuLyTinNhanService
import tamhoang.bvn.data.setting.Setting
import tamhoang.bvn.data.store.*

object BaseStore {
    lateinit var sqlBrite: SqlBrite
    lateinit var briteDb: BriteDatabase

    fun init(context: Context) {
        if (::briteDb.isInitialized) return
        val db = DbOpenHelper(context)
        sqlBrite = SqlBrite.Builder().build()
        briteDb = sqlBrite.wrapDatabaseHelper(DbOpenHelper(context), Schedulers.io())

        Setting.I = Setting(db)
        SoctStore.I = SoctStore(db)
        KqStore.I = KqStore(db)
        TinNhanStore.I = TinNhanStore(db)
        KhachHangStore.I = KhachHangStore(db)
        ThayTheStore.I = ThayTheStore(db)

        XuLyTinNhanService.I = XuLyTinNhanService(db)
        SoctService.I = SoctService(db)
        TinhTienService.I = TinhTienService(db)
        GuiTinNhanService.I = GuiTinNhanService(db)
        ChotTienService.I = ChotTienService(db)
        XuatSoService.I = XuatSoService(db)
    }

    private fun getCursorField(table: String, field: String, where: String): Cursor {
        val query = "SELECT $field FROM $table WHERE $where"
        return briteDb.query(query)
    }

    fun getStringField(table: String, field: String, where: String): String? {
        getCursorField(table, field, where).use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getString(0)
            }
        }
        return ""
    }

    fun getIntField(table: String, field: String, where: String): Int {
        getCursorField(table, field, where).use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getInt(0)
            }
        }
        return -1
    }

    fun getDoubleField(table: String, field: String, where: String): Double {
        getCursorField(table, field, where).use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getDouble(0)
            }
        }
        return -1.0
    }

    fun getMaxSoTinNhan(ngay_nhan: String, type_kh: Int?, query: String): Int {
        val queryRaw =
            "Select max(${TinNhanS.SO_TIN_NHAN}) from ${TinNhanS.TABLE_NAME} WHERE ngay_nhan = '$ngay_nhan' AND " +
                    (if (type_kh != null) "type_kh = $type_kh AND " else "") + query
        val cursor = briteDb.query(queryRaw)
        val result =
            if (cursor.moveToFirst() && cursor != null) cursor.getInt(0) else 0
        cursor.close()
        return result
    }

    fun getIdTinNhanS(fullQuery: String): Int? {
        val queryRaw = "Select id From ${TinNhanS.TABLE_NAME} $fullQuery"

        val cursor = briteDb.query(queryRaw)
        val result = if (cursor != null && cursor.moveToFirst())
            cursor.getInt(0) else null
        cursor.close()
        return result
    }

    fun selectChatsQuery(query: String): List<Chat> {
        val fullQuery = "Select * From ${Chat.TABLE_NAME} $query"

        val list = mutableListOf<Chat>()
        val cursor: Cursor = briteDb.query(fullQuery)
        while (cursor.moveToNext()) {
            list.add(Chat.parseCursor(cursor))
        }
        return list
    }

    fun selectChats(mDate: String): List<Chat> {
        val query =
            "Select * From ${Chat.TABLE_NAME} WHERE ngay_nhan = '" + mDate + "' ORDER BY Gio_nhan DESC, ID DESC"

        val list = mutableListOf<Chat>()
        val cursor: Cursor = briteDb.query(query)
        while (cursor.moveToNext()) {
            list.add(Chat.parseCursor(cursor))
        }
        return list
    }

    fun insertChat(chat: Chat) {
        val transaction = briteDb.newTransaction()
        try {
            briteDb.insert(Chat.TABLE_NAME, Chat.toContentValues(chat), SQLiteDatabase.CONFLICT_REPLACE)
            transaction.markSuccessful()
        } catch (e: Exception) {
            Log.e(TAG, "Error $e")
        } finally {
            transaction.end()
        }
    }

    fun selectChuyenThang(sdt_nhan: String): ChuyenThang? {
        val query =
            "Select * From ${ChuyenThang.TABLE_NAME} WHERE ${ChuyenThang.SDT_NHAN} = '$sdt_nhan'"

        val cursor = briteDb.query(query)
        return if (cursor != null && cursor.moveToFirst())
            ChuyenThang.parseCursor(cursor) else null
    }

    fun chuyenThangNdGoc(): Boolean {
        val cursor = briteDb.query("Select Om_Xi3 FROM so_Om WHERE id = 13")
        return if (cursor != null && cursor.moveToFirst())
            cursor.getInt(0) == 0 else true
    }

    fun insertListSoctS(listSoctS: List<SoctS>) {
        val transaction = briteDb.newTransaction()
        try {
            listSoctS.forEach {
                briteDb.insert(
                    SoctS.TABLE,
                    SoctS.toContentValues(it),
                    SQLiteDatabase.CONFLICT_REPLACE
                )
            }
            transaction.markSuccessful()
        } catch (e: Exception) {
            Log.e(TAG, "Error $e")
        } finally {
            transaction.end()
        }
    }

    fun savePassWord(passWord: String) {
        val transaction = briteDb.newTransaction()
        try {
            briteDb.insert(
                "tbl_Setting",
                ContentValues().apply {
                    put("id", 2)
                    put("Setting", "{'PassWord' : '$passWord'}")
                },
                SQLiteDatabase.CONFLICT_REPLACE
            )
            transaction.markSuccessful()
        } catch (e: Exception) {
            Log.e(TAG, "Error $e")
        } finally {
            transaction.end()
        }
    }

    fun getPassWord(): String {
        return try {
            val cursor = briteDb.query("Select * FROM tbl_Setting WHERE id = 2")
            val str = if (cursor != null && cursor.moveToFirst()) cursor.getString(1) else "{}"
            val json = JSONObject(str)
            json.getString("PassWord")
        } catch (e: JSONException) {
            ""
        }
    }

}