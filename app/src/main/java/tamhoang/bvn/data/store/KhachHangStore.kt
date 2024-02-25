package tamhoang.bvn.data.store

import org.json.JSONObject
import tamhoang.bvn.data.DbOpenHelper
import tamhoang.bvn.data.model.KhachHang
import tamhoang.bvn.data.setting.SettingGia
import tamhoang.bvn.data.setting.SettingKH

class KhachHangStore(val db: DbOpenHelper) {
    companion object {
        lateinit var I: KhachHangStore
        const val TABLE = KhachHang.TABLE
    }

    fun selectWhere(where: String): KhachHang? {
        val query = "Select * From ${KhachHang.TABLE} Where $where"

        val cursor = db.getData(query)
        return if (cursor.moveToFirst())
            KhachHang.parseCursor(cursor) else null
    }

    fun selectByName(ten: String): KhachHang? {
        val query = "Select * From $TABLE Where ${KhachHang.TEN_KH} = '$ten'"

        val cursor = db.getData(query)
        val result = if (cursor.moveToFirst())
            KhachHang.parseCursor(cursor) else null
        cursor.close()
        return result
    }

    fun selectBySdt(sdt: String): KhachHang? {
        val query = "Select * From $TABLE Where ${KhachHang.SDT} = '$sdt'"

        val cursor = db.getData(query)
        val result = if (cursor.moveToFirst())
            KhachHang.parseCursor(cursor) else null

        cursor.close()
        return result
    }

    fun selectListWhere(where: String): ArrayList<KhachHang> {
        val query = "Select * From $TABLE Where $where"

        val list = arrayListOf<KhachHang>()
        val cursor = db.getData(query)
        while (cursor.moveToNext()) {
            list.add(KhachHang.parseCursor(cursor))
        }
        cursor.close()
        return list
    }

    fun selectListQuery(query: String): ArrayList<KhachHang> {
        val qr = "Select * From $TABLE $query"

        val list = arrayListOf<KhachHang>()
        val cursor = db.getData(qr)
        while (cursor.moveToNext()) {
            list.add(KhachHang.parseCursor(cursor))
        }
        cursor.close()
        return list
    }

    fun getListName(): ArrayList<String> {
        val list = arrayListOf<String>()
        val cursor = db.getData("Select * From tbl_kh_new WHERE type_kh <> 2")
        while (cursor.moveToNext()) {
            list.add(cursor.getString(1))
        }
        cursor.close()
        return list
    }

    private fun getSettingKH(where: String): SettingKH {
        val tbl = db.getOneData<String>(
            KhachHang.TABLE, where,
            KhachHang.TBL_MB
        )
        val json = JSONObject(tbl)
        val caidatTg = json.getJSONObject("caidat_tg")
        return SettingKH(caidatTg)
    }

    fun getSettingKHByName(tenKh: String) = getSettingKH("ten_kh = '$tenKh'")
    fun getSettingKHBySdt(sdt: String) = getSettingKH("${KhachHang.SDT} = '$sdt'")

    private fun getPairSetting(where: String): Pair<SettingKH, SettingGia> {
        val tbl = db.getOneData<String>(
            KhachHang.TABLE, where,
            KhachHang.TBL_MB
        )
        val json = JSONObject(tbl)
        val caidatTg = json.getJSONObject("caidat_tg")
        val caidatGia = json.getJSONObject("caidat_gia")

        return Pair(SettingKH(caidatTg), SettingGia.fromJson(caidatGia))
    }

    fun getPairSettingByName(tenKh: String) = getPairSetting("ten_kh = '$tenKh'")
}