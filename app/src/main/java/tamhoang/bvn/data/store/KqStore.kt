package tamhoang.bvn.data.store

import tamhoang.bvn.data.DbOpenHelper

class KqStore(val db: DbOpenHelper) {
    companion object {
        lateinit var I: KqStore
        const val TABLE = "KetQua"
    }

    fun getKq(ngay: String): Array<String> {
        val cursor = db.getData("Select * From $TABLE WHERE ngay = '$ngay'")
        cursor.moveToFirst()
        val listGiai = Array(27) { "" }
        for (i in 0..26) {
            listGiai[i] = cursor.getString(i + 2) ?: return arrayOf()
        }
        cursor.close()

        return listGiai
    }
}