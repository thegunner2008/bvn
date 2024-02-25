package tamhoang.bvn.data.store

import tamhoang.bvn.data.DbOpenHelper

class ThayTheStore(val db: DbOpenHelper) {
    companion object {
        lateinit var I: ThayTheStore
        const val TABLE = "thay_the_phu"
    }

    fun getList(): List<Pair<String, String>> {
        val cursor = db.getData("Select * From $TABLE")
        val list = arrayListOf<Pair<String, String>>()
        while (cursor.moveToNext()) {
            list.add(cursor.getString(1) to cursor.getString(2))
        }
        if (!cursor.isClosed) cursor.close()
        return list
    }
}