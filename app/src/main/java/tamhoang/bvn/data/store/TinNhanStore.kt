package tamhoang.bvn.data.store

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import tamhoang.bvn.data.BaseStore
import tamhoang.bvn.data.DbOpenHelper
import tamhoang.bvn.data.model.TinNhanS

class TinNhanStore(val db: DbOpenHelper) {
    companion object {
        lateinit var I: TinNhanStore
        const val TABLE = TinNhanS.TABLE_NAME
    }

    fun selectByID(ID: Int): TinNhanS? {
        val query = "Select * From ${TinNhanS.TABLE_NAME} Where ID = $ID"

        val cursor = BaseStore.briteDb.query(query)
        val result = if (cursor != null && cursor.moveToFirst())
            TinNhanS.parseCursor(cursor) else null
        cursor.close()
        return result
    }

    fun selectByID(id: String): TinNhanS? {
        val query = "Select * From ${TinNhanS.TABLE_NAME} Where ID = $id"

        val cursor = BaseStore.briteDb.query(query)
        val result = if (cursor != null && cursor.moveToFirst())
            TinNhanS.parseCursor(cursor) else null
        cursor.close()
        return result
    }

    fun select(
        ngayNhan: String,
        tenKh: String,
        soTinNhan: Int,
        typeKh: Int
    ): TinNhanS? {
        val query = "Select * From ${TinNhanS.TABLE_NAME} Where" +
                " ${TinNhanS.NGAY_NHAN} = '$ngayNhan'" +
                " AND ${TinNhanS.TEN_KH} = '$tenKh'" +
                " AND ${TinNhanS.SO_TIN_NHAN} = $soTinNhan" +
                " AND ${TinNhanS.TYPE_KH} = $typeKh"

        val cursor = db.getData(query)
        val result = if (cursor.moveToFirst())
            TinNhanS.parseCursor(cursor) else null
        cursor.close()
        return result
    }

    fun selectWhere(where: String): TinNhanS? {
        val queryRaw = "Select * From ${TinNhanS.TABLE_NAME} Where $where"

        val cursor = db.getData(queryRaw)
        val result = if (cursor.moveToFirst())
            TinNhanS.parseCursor(cursor) else null
        cursor.close()
        return result
    }

    fun selectListWhere(date: String, where: String) =
        selectListWhere("ngay_nhan = '$date' AND $where")

    fun selectListWhere(where: String): List<TinNhanS> {
        val queryRaw = "Select * From $TABLE Where $where"

        val list = mutableListOf<TinNhanS>()
        val cursor = db.getData(queryRaw)
        while (cursor.moveToNext()) {
            list.add(TinNhanS.parseCursor(cursor))
        }
        cursor.close()
        return list
    }

    fun insert(tinNhan: TinNhanS) {
        val transaction = BaseStore.briteDb.newTransaction()
        try {
            BaseStore.briteDb.insert(
                TinNhanS.TABLE_NAME,
                TinNhanS.toContentValues(tinNhan),
                SQLiteDatabase.CONFLICT_REPLACE
            )
            transaction.markSuccessful()
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error $e")
        } finally {
            transaction.end()
        }
    }

    fun insertList(listTinNhan: List<TinNhanS>) {
        val transaction = BaseStore.briteDb.newTransaction()
        try {
            listTinNhan.forEach {
                BaseStore.briteDb.insert(
                    TinNhanS.TABLE_NAME,
                    TinNhanS.toContentValues(it),
                    SQLiteDatabase.CONFLICT_REPLACE
                )
            }
            transaction.markSuccessful()
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error $e")
        } finally {
            transaction.end()
        }
    }

    fun update(
        id: Int,
        ndPhanTich: String? = null,
        ndSua: String? = null,
        phanTich: String? = null,
        phatHienLoi: String? = null
    ) {
        val updateList = mutableListOf<String>()
        if (ndPhanTich != null)
            updateList.add("${TinNhanS.ND_PHANTICH} = '$ndPhanTich'")
        if (phanTich != null)
            updateList.add("${TinNhanS.PHAN_TICH} = '$phanTich'")
        if (ndSua != null)
            updateList.add("${TinNhanS.ND_SUA} = '$ndSua'")
        if (phatHienLoi != null)
            updateList.add("${TinNhanS.PHAT_HIEN_LOI} = '$phatHienLoi'")

        val queryStr =
            "Update ${TinNhanS.TABLE_NAME} set ${updateList.joinToString(separator = ", ")} Where ${TinNhanS.ID} = $id"
        db.queryData(queryStr)
    }

    fun deleteWhere(query: String) {
        db.queryData("Delete FROM $TABLE WHERE $query")
    }
}