package tamhoang.bvn.data

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Environment
import org.json.JSONException
import org.json.JSONObject
import tamhoang.bvn.data.enum.TYPE
import tamhoang.bvn.ui.main.MainState
import java.io.File
import kotlin.reflect.typeOf

open class DbOpenHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {
    companion object {
        val DATABASE_NAME =
            Environment.getExternalStorageDirectory().toString() + File.separator + "ldpro"
        const val DATABASE_VERSION = 1
    }

    open var mcontext: Context? = null

    init {
        mcontext = context
    }

    override fun onCreate(p0: SQLiteDatabase?) {
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
    }

    fun deleteAllTable() {
        //Delete all table
        queryData("Delete From tbl_kh_new")
        queryData("Delete From tbl_soctS")
        queryData("Drop table if exists Chat_database")
        queryData("Drop table if exists So_om")
        queryData("Drop table if exists tbl_chaytrang_acc")
        queryData("Drop table if exists tbl_chuyenthang")
        queryData("Drop table if exists tbl_tinnhanS")
    }

    fun createTableChat() {
        try {
            val cursor = getData("Select * From Chat_database")
            if (cursor.columnCount == 8) {
                queryData("Drop table Chat_database")
            }
            cursor.close()
        } catch (e: Exception) {
        }
        queryData("CREATE TABLE IF NOT EXISTS Chat_database( ID INTEGER PRIMARY KEY AUTOINCREMENT,\n ngay_nhan DATE NOT NULL,\n gio_nhan VARCHAR(8) NOT NULL,\n type_kh INTEGER DEFAULT 0,\n ten_kh VARCHAR(20) NOT NULL,\n so_dienthoai VARCHAR(20) NOT NULL,\n use_app VARCHAR(20) NOT NULL,\n nd_goc VARCHAR(500) DEFAULT NULL,\n del_sms INTEGER DEFAULT 0);")
    }

    fun createTinNhanGoc() {
        queryData("CREATE TABLE IF NOT EXISTS tbl_tinnhanS(\n ID INTEGER PRIMARY KEY AUTOINCREMENT,\n ngay_nhan DATE NOT NULL,\n gio_nhan VARCHAR(8) NOT NULL,\n type_kh INTEGER DEFAULT 0,\n ten_kh VARCHAR(20) NOT NULL,\n so_dienthoai VARCHAR(20) NOT NULL,\n use_app VARCHAR(20) NOT NULL,\n so_tin_nhan INTEGER DEFAULT 0,\n nd_goc VARCHAR(500) DEFAULT NULL,\n nd_sua VARCHAR(500) DEFAULT NULL,\n nd_phantich VARCHAR(500) DEFAULT NULL,\n phat_hien_loi VARCHAR(100) DEFAULT NULL,\n tinh_tien INTEGER DEFAULT 0,\n ok_tn INTEGER DEFAULT 0,\n del_sms INTEGER DEFAULT 0,  phan_tich TEXT);")
    }

    fun createSoCT() {
        queryData("CREATE TABLE IF NOT EXISTS tbl_soctS(\n ID INTEGER PRIMARY KEY AUTOINCREMENT,\n ngay_nhan DATE NOT NULL,\n type_kh INTEGER DEFAULT 1,\n ten_kh VARCHAR(20) NOT NULL,\n so_dienthoai VARCHAR(20) NOT NULL,\n so_tin_nhan INTEGER DEFAULT 0,\n the_loai VARCHAR(4) DEFAULT NULL,\n so_chon VARCHAR(20) DEFAULT NULL,\n diem DOUBLE DEFAULT 0,\n diem_quydoi DOUBLE DEFAULT 0,\n diem_khachgiu DOUBLE DEFAULT 0,\n diem_dly_giu DOUBLE DEFAULT 0,\n diem_ton DOUBLE DEFAULT 0,\n gia DOUBLE DEFAULT 0,\n lan_an DOUBLE DEFAULT 0,\n so_nhay DOUBLE DEFAULT 0,\n tong_tien DOUBLE DEFAULT 0,\n ket_qua DOUBLE DEFAULT 0)")
        queryData("CREATE TABLE IF NOT EXISTS tbl_chuyenthang ( ID INTEGER PRIMARY KEY AUTOINCREMENT, kh_nhan VARCHAR(20) NOT NULL, sdt_nhan VARCHAR(15) NOT NULL, kh_chuyen VARCHAR(20) NOT NULL, sdt_chuyen VARCHAR(15) NOT NULL)")
    }

    fun createSoOm() {
        queryData("CREATE TABLE IF NOT EXISTS So_om(  ID INTEGER PRIMARY KEY AUTOINCREMENT,  So VARCHAR(2) DEFAULT NULL,  Om_DeA INTEGER DEFAULT 0,  Om_DeB INTEGER DEFAULT 0,  Om_DeC INTEGER DEFAULT 0,  Om_DeD INTEGER DEFAULT 0,  Om_Lo INTEGER Default 0,  Om_Xi2 INTEGER Default 0,  Om_Xi3 INTEGER Default 0,  Om_Xi4 INTEGER Default 0,  Om_bc INTEGER Default 0,  Sphu1 VARCHAR(200) DEFAULT NULL,  Sphu2 VARCHAR(200) DEFAULT NULL)")
    }

    fun creatChaytrangAcc() {
        queryData("CREATE TABLE IF NOT EXISTS tbl_chaytrang_acc( \n Username VARCHAR(30) PRIMARY KEY,\n Password VARCHAR(20) NOT NULL,\n Setting TEXT NOT NULL,\n Status VARCHAR(20) DEFAULT NULL)")
    }

    fun createChaytrangTicket() {
        queryData("CREATE TABLE IF NOT EXISTS tbl_chaytrang_ticket( ID INTEGER PRIMARY KEY AUTOINCREMENT, \nngay_nhan DATE NOT NULL, \nCreatedAt VARCHAR(20) DEFAULT NULL, \nUsername VARCHAR(30), \nTicketNumber INTEGER DEFAULT 0, \nGameType INTEGER DEFAULT 0,\nNumbers Text DEFAULT NULL, \nPoint DOUBLE DEFAULT 0, \nAmount DOUBLE DEFAULT 0, \nCancelledAt INTEGER DEFAULT 1)")
    }

    fun createThayThePhu() {
        queryData("CREATE TABLE IF NOT EXISTS thay_the_phu(  ID INTEGER PRIMARY KEY AUTOINCREMENT,  str VARCHAR(20) NOT NULL,  str_rpl VARCHAR(20) NOT NULL)")
    }

    fun createKhachHang() {
        queryData("CREATE TABLE IF NOT EXISTS tbl_kh_new (ten_kh VARCHAR(30) PRIMARY KEY,sdt VARCHAR(15),use_app Varchar(10), type_kh INTEGER default 0, type_pt Integer default 0, tbl_MB TEXT, tbl_XS TEXT)")
        queryData("Delete From tbl_kh_new Where substr(sdt,0,3) = 'TL'")
    }

    fun createAnotherSetting() {
        queryData("CREATE TABLE IF NOT EXISTS tbl_Setting(\n ID INTEGER PRIMARY KEY AUTOINCREMENT,\n Setting TEXT)")
        val cursor = getData("SELECT * FROM 'tbl_Setting'")
        if (cursor.count == 0) {
            val setting = JSONObject()
            try {
                setting.put("ap_man", 0)
                setting.put("chuyen_xien", 0)
                setting.put("lam_tron", 0)
                setting.put("gioi_han_tin", 1)
                setting.put("tin_qua_gio", 0)
                setting.put("tin_trung", 0)
                setting.put("kieu_bao_cao", 0)
                setting.put("bao_cao_so", 0)
                setting.put("tra_thuong_lo", 0)
                setting.put("canhbaodonvi", 0)
                setting.put("tudongxuly", 0)
                setting.put("tachxien_tinchot", 0)
                setting.put("baotinthieu", 0)
                setting.put("thoigiancho", 0)
                queryData("insert into tbl_Setting Values( null,'$setting')")
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            cursor.close()
            return
        }
        try {
            cursor.moveToFirst()
            val setting2 = JSONObject(cursor.getString(1))
            if (!setting2.has("canhbaodonvi")) {
                setting2.put("canhbaodonvi", 0)
            }
            if (!setting2.has("tachxien_tinchot")) {
                setting2.put("tachxien_tinchot", 0)
            }
            if (!setting2.has("baotinthieu")) {
                setting2.put("baotinthieu", 0)
            }
            queryData("Update tbl_Setting set Setting = '$setting2' WHERE ID = 1")
        } catch (e2: JSONException) {
            e2.printStackTrace()
        }
    }

    fun saveSetting(Keys: String?, values: Int) {
        val cursor = getData("Select * From tbl_Setting WHERE ID = 1")
        if (cursor.moveToFirst()) {
            try {
                MainState.jSon_Setting.put(Keys, values)
                queryData("Update tbl_Setting set Setting = '" + MainState.jSon_Setting.toString() + "' WHERE ID = 1")
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        cursor.close()
    }

    fun createBangKQ() {
        queryData("CREATE TABLE IF NOT EXISTS KetQua(  ID INTEGER PRIMARY KEY AUTOINCREMENT,  Ngay DATE DEFAULT NULL,  GDB VARCHAR(5) DEFAULT NULL,  G11 VARCHAR(5) DEFAULT NULL,  G21 VARCHAR(5) DEFAULT NULL,  G22 VARCHAR(5) DEFAULT NULL,  G31 VARCHAR(5) DEFAULT NULL,  G32 VARCHAR(5) DEFAULT NULL,  G33 VARCHAR(5) DEFAULT NULL,  G34 VARCHAR(5) DEFAULT NULL,  G35 VARCHAR(5) DEFAULT NULL,  G36 VARCHAR(5) DEFAULT NULL,  G41 VARCHAR(4) DEFAULT NULL,  G42 VARCHAR(4) DEFAULT NULL,  G43 VARCHAR(4) DEFAULT NULL,  G44 VARCHAR(4) DEFAULT NULL,  G51 VARCHAR(4) DEFAULT NULL,  G52 VARCHAR(4) DEFAULT NULL,  G53 VARCHAR(4) DEFAULT NULL,  G54 VARCHAR(4) DEFAULT NULL,  G55 VARCHAR(4) DEFAULT NULL,  G56 VARCHAR(4) DEFAULT NULL,  G61 VARCHAR(3) DEFAULT NULL,  G62 VARCHAR(3) DEFAULT NULL,  G63 VARCHAR(3) DEFAULT NULL,  G71 VARCHAR(2) DEFAULT NULL,  G72 VARCHAR(2) DEFAULT NULL,  G73 VARCHAR(2) DEFAULT NULL,  G74 VARCHAR(2) DEFAULT NULL);")
    }

    fun getOneRow(table: String, where: String, vararg columns: Pair<TYPE, String>): ArrayList<Any> {
        val objects = ArrayList<Any>()
        val cursor = getData("SELECT ${columns.joinToString(",") { it.second }} FROM '$table' WHERE $where")
        if (cursor.moveToFirst()) {
            for (index in columns.indices) {
                when (columns[index].first) {
                    TYPE.String -> objects.add(cursor.getString(index))
                    TYPE.Int -> objects.add(cursor.getInt(index))
                    TYPE.Double -> objects.add(cursor.getDouble(index))
                }
            }
        }
        cursor.close()
        return objects
    }

    fun getFullData(table: String, where: String, vararg columns: Pair<TYPE, String>): ArrayList<ArrayList<Any>> {
        val arrayList = ArrayList<ArrayList<Any>>()
        val cursor = getData("SELECT ${columns.joinToString(",") { it.second }} FROM '$table' WHERE $where")
        while (cursor.moveToNext()) {
            val objects = ArrayList<Any>(columns.size)
            for (index in columns.indices) {
                when (columns[index].first) {
                    TYPE.String -> objects.add(cursor.getString(index))
                    TYPE.Int -> objects.add(cursor.getInt(index))
                    TYPE.Double -> objects.add(cursor.getDouble(index))
                }
            }
            arrayList.add(objects)
        }
        cursor.close()
        return arrayList
    }

    @OptIn(ExperimentalStdlibApi::class)
    inline fun <reified T> getOneColumn(table: String, where: String, column: String): ArrayList<T> {
        val cursor = getData("SELECT $column FROM '$table' WHERE $where")
        val arrayList = ArrayList<T>()
        while (cursor.moveToNext()) {
            val value = when (typeOf<T>().classifier) {
                String::class -> cursor.getString(0)
                Int::class -> cursor.getInt(0)
                Double::class -> cursor.getDouble(0)
                else -> throw IllegalArgumentException("Unsupported type")
            }
            arrayList.add(value as T)
        }
        cursor.close()
        return arrayList
    }

    @OptIn(ExperimentalStdlibApi::class)
    inline fun <reified T> getOneData(table: String, where: String, column: String): T {
        val cursor = getData("SELECT $column FROM '$table' WHERE $where")
        val result = if (cursor.moveToFirst()) {
            when (typeOf<T>().classifier) {
                String::class -> cursor.getString(0)
                Int::class -> cursor.getInt(0)
                Double::class -> cursor.getDouble(0)
                else -> throw IllegalArgumentException("Unsupported type")
            }
        } else {
            when (typeOf<T>().classifier) {
                String::class -> ""
                Int::class -> 0
                Double::class -> 0.0
                else -> throw IllegalArgumentException("Unsupported type")
            }
        }
        cursor.close()
        return result as T
    }

    fun getData(sql: String?): Cursor {
        return readableDatabase.rawQuery(sql, null)
    }

    fun queryData(sql: String?) {
        writableDatabase.execSQL(sql)
    }
}