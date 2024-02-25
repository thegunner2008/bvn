package tamhoang.bvn.data.store

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import tamhoang.bvn.data.BaseStore
import tamhoang.bvn.data.DbOpenHelper
import tamhoang.bvn.data.enum.TL
import tamhoang.bvn.data.model.SoctS

class SoctStore(val db: DbOpenHelper) {
    companion object {
        lateinit var I: SoctStore
        const val TABLE = SoctS.TABLE
    }

    fun selectListWhere(date: String, where: String): List<SoctS> {
        val queryRaw = "Select * From $TABLE Where ngay_nhan = '$date' AND $where"

        val list = mutableListOf<SoctS>()
        val cursor = db.getData(queryRaw)
        while (cursor.moveToNext()) {
            list.add(SoctS.parseCursor(cursor))
        }
        cursor.close()
        return list
    }

    fun increaseSoNhay(date: String, theLoai: TL, giai: String) {
        db.queryData(
            "Update $TABLE Set so_nhay = so_nhay + 1 WHERE ngay_nhan = '$date' AND the_loai = '${theLoai.t}' AND so_chon = '${
                getSo(
                    theLoai,
                    giai
                )
            }'"
        )
    }

    fun setSoNhay(date: String, theLoai: TL, giai: String, lanAn: Int? = null, soNhay: Int = 1) {
        val lanAnStr = if (lanAn != null) ", lan_an = $lanAn" else ""
        db.queryData(
            "Update $TABLE Set so_nhay = $soNhay$lanAnStr WHERE ngay_nhan = '$date' AND the_loai = '${theLoai.t}' AND so_chon = '${
                getSo(
                    theLoai,
                    giai
                )
            }'"
        )
    }

    fun setSoNhayWhere(date: String, ketQua: Int, soNhay: Int = 1, where: String) {
        db.queryData("Update $TABLE Set so_nhay = $soNhay, ket_qua = $ketQua WHERE ngay_nhan = '$date' AND $where")
    }

    fun updateFull(
        date: String,
        theLoai: TL,
        giai: String,
        typeKh: Int,
        soNhay: Int = 1,
        ketQua: String? = null,
        equal: Boolean = true
    ) {
        val ketQuaStr = if (ketQua != null) ", ket_qua = $ketQua" else ""
        val query =  "Update $TABLE Set so_nhay = $soNhay$ketQuaStr WHERE ngay_nhan = '$date' AND the_loai = '${theLoai.t}' AND so_chon ${if (equal) "=" else "<>"} '" + getSo(
            theLoai,
            giai
        ) + "' AND type_kh = $typeKh"

        db.queryData(
           query
        )
    }

    private fun getSo(theLoai: TL, giai: String) = when (theLoai) {
        TL.BC -> giai.substring(giai.length - 3)
        TL.BCA -> giai.substring(0, 3)
        TL.DeA, TL.DeC, TL.LoA -> giai.substring(0, 2)
        else -> giai.substring(giai.length - 2)
    }

    fun selectWhere(where: String): SoctS? {
        val query = "Select * From $TABLE Where $where"

        val cursor = BaseStore.briteDb.query(query)
        val result = if (cursor != null && cursor.moveToFirst())
            SoctS.parseCursor(cursor) else null

        cursor.close()
        return result
    }

    fun insert(contentValues: ContentValues) {
        val transaction = BaseStore.briteDb.newTransaction()
        try {
            BaseStore.briteDb.insert(
                TABLE,
                contentValues,
                SQLiteDatabase.CONFLICT_REPLACE
            )
            transaction.markSuccessful()
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error $e")
        } finally {
            transaction.end()
        }
    }

    fun countWhere(where: String, colunm: String = "*") =
        BaseStore.briteDb.query("Select count($colunm) From $TABLE Where $where").use {
            if (it.moveToFirst()) it.getInt(0) else 0
        }

    fun deleteWhere(query: String) {
        db.queryData("Delete FROM $TABLE WHERE $query")
    }
}