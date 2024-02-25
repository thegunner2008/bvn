package tamhoang.bvn.ui.tinNhan.suaTin

import android.content.ContentValues
import android.database.DatabaseUtils
import android.net.Uri
import android.text.format.DateFormat
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import org.json.JSONException
import org.json.JSONObject
import tamhoang.bvn.data.BaseStore
import tamhoang.bvn.data.DbOpenHelper
import tamhoang.bvn.data.model.KhachHang
import tamhoang.bvn.data.model.TinNhanS
import tamhoang.bvn.data.services.xuLyTinNhan.XuLyTinNhanService
import tamhoang.bvn.data.setting.Setting
import tamhoang.bvn.data.store.SoctStore
import tamhoang.bvn.data.store.TinNhanStore
import tamhoang.bvn.messageCenter.telegram.TelegramHandle
import tamhoang.bvn.ui.base.dialog.Dialog
import tamhoang.bvn.ui.main.MainState
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class SuaTinController(private val db: DbOpenHelper, private val activity: FragmentActivity) {
    @Throws(ParseException::class)
    fun getFullSms(str: String, typeKh: Int, khachHang: KhachHang, reLoadLv: () -> Unit) {
        val ngay = MainState.date2DMY
        val date = MainState.dateYMD
        var phone: String
        if (!MainState.jSon_Setting.has("tin_trung")) {
            try {
                MainState.jSon_Setting.put("tin_trung", 0)
                db.queryData("Update tbl_Setting set Setting = '" + MainState.jSon_Setting.toString() + "' WHERE ID = 1")
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        if (ContextCompat.checkSelfPermission(activity, "android.permission.READ_SMS") != 0) {
            return
        }
        val parse = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(date + "T00:00:00")
        val str24 = "date>=" + parse.time
        val sb: String = if (str.contains("Full")) {
            db.queryData("DELETE FROM tbl_soctS WHERE ngay_nhan = '$date'")
            db.queryData("DELETE FROM tbl_tinnhanS WHERE ngay_nhan = '$date'")
            "Select * From tbl_kh_new"
        } else {
            db.queryData("DELETE FROM tbl_soctS WHERE ngay_nhan = '$date' AND so_dienthoai = '${khachHang.sdt}'")
            db.queryData("DELETE FROM tbl_tinnhanS WHERE ngay_nhan = '$date' AND so_dienthoai = '${khachHang.sdt}'")
            "Select * From tbl_kh_new Where sdt = '" + khachHang.sdt + "'"
        }
        val getKhach = db.getData(sb)
        val jsonALlKH = JSONObject()
        try {
            while (getKhach.moveToNext()) {
                val jsonKh = JSONObject()
                jsonKh.put("type_kh", getKhach.getString(3))
                jsonKh.put("ten_kh", getKhach.getString(0))
                jsonKh.put("so_tn", 0)
                jsonALlKH.put(getKhach.getString(1), jsonKh)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val query = activity.contentResolver.query(
            Uri.parse("content://sms"),
            null,
            str24,
            null,
            "date ASC"
        )
        activity.startManagingCursor(query)
        val count = query!!.count
        val writableDatabase = db.writableDatabase
        val insertHelper = DatabaseUtils.InsertHelper(writableDatabase, "tbl_tinnhanS")
        val moveToFirst = query.moveToFirst()
        if (moveToFirst) {
            try {
                writableDatabase.beginTransaction()
                for (i in 0 until count) {
                    try {
                        val dateValue = query.getLong(query.getColumnIndexOrThrow("date"))
                        val sb4 = DateFormat.format("dd/MM/yyyy HH:mm:ss", Date(dateValue))
                            .toString() + ""
                        val gioNhan = DateFormat.format("HH:mm:ss", Date(dateValue)).toString() + ""
                        phone = query.getString(query.getColumnIndexOrThrow("address"))
                            .replace(" ".toRegex(), "")
                        val ndSms = query.getString(query.getColumnIndexOrThrow("body"))
                            .replace("'".toRegex(), " ").replace("\"".toRegex(), " ")
                        val type = query.getString(query.getColumnIndexOrThrow("type"))
                        if (phone.length < 12) {
                            phone = "+84" + phone.substring(1)
                        }
                        if (jsonALlKH.has(phone) && sb4.contains(ngay) && !ndSms.contains("Ok Tin")) {
                            val jsonKHsms = jsonALlKH.getJSONObject(phone)
                            if (!jsonKHsms.getString(TYPE_KH).contains("3")) {
                                if (jsonKHsms.getString(TYPE_KH).contains("2")) {
                                    reLoadLv()
                                    Toast.makeText(
                                        activity,
                                        "Đã tải xong tin nhắn!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                val inList = jsonKHsms.getString(TYPE_KH).contains(phone) && type.contains(phone)

                                val trungTin = Setting.I.nhanTinTrung.isTrue()

                                if (trungTin && jsonKHsms.has(ndSms) && inList) {
                                    jsonKHsms.put("so_tn", jsonKHsms.getInt("so_tn") + 1)
                                    jsonKHsms.put(ndSms, ndSms)
                                    insertHelper.prepareForInsert()
                                    try {
                                        insertHelper.bind(
                                            insertHelper.getColumnIndex(NGAY_NHAN),
                                            date
                                        )
                                        insertHelper.bind(
                                            insertHelper.getColumnIndex(GIO_NHAN),
                                            gioNhan
                                        )
                                        insertHelper.bind(
                                            insertHelper.getColumnIndex(TYPE_KH),
                                            type
                                        )
                                        insertHelper.bind(
                                            insertHelper.getColumnIndex(TEN_KH),
                                            jsonKHsms.getString(TEN_KH)
                                        )
                                        insertHelper.bind(
                                            insertHelper.getColumnIndex(SO_DIENTHOAI),
                                            phone
                                        )
                                        insertHelper.bind(insertHelper.getColumnIndex(USE_APP), SMS)
                                        insertHelper.bind(
                                            insertHelper.getColumnIndex(SO_TIN_NHAN),
                                            jsonKHsms.getInt("so_tn")
                                        )
                                        insertHelper.bind(
                                            insertHelper.getColumnIndex(ND_GOC),
                                            ndSms
                                        )
                                        insertHelper.bind(
                                            insertHelper.getColumnIndex(ND_SUA),
                                            ndSms
                                        )
                                        insertHelper.bind(
                                            insertHelper.getColumnIndex("nd_phantich"),
                                            ndSms
                                        )
                                        insertHelper.bind(
                                            insertHelper.getColumnIndex("phat_hien_loi"),
                                            "ko"
                                        )
                                        insertHelper.bind(
                                            insertHelper.getColumnIndex("tinh_tien"),
                                            0
                                        )
                                        insertHelper.bind(insertHelper.getColumnIndex("ok_tn"), 0)
                                        insertHelper.bind(insertHelper.getColumnIndex("del_sms"), 0)
                                        insertHelper.execute()
                                        jsonALlKH.put(phone, jsonKHsms)
                                    } catch (th2: Throwable) {
                                        insertHelper.close()
                                    }
                                }
                            }
                        }
                        query.moveToNext()
                    } catch (e18: Exception) {
                        writableDatabase.setTransactionSuccessful()
                        writableDatabase.endTransaction()
                        writableDatabase.close()
                        reLoadLv()
                        Toast.makeText(activity, "Đã tải xong tin nhắn!", Toast.LENGTH_LONG).show()
                    }
                }
                writableDatabase.setTransactionSuccessful()
                writableDatabase.endTransaction()
                writableDatabase.close()
                insertHelper.close()
            } catch (ignored: Throwable) {
            }
        }
        try {
            val listChat =
                BaseStore.selectChatsQuery("Where ngay_nhan = '$date' And use_app <> 'sms'")
            val listTinNhanS_i: MutableList<TinNhanS> = ArrayList()
            if (listChat.isNotEmpty()) {
                for ((_, ngay_nhan, gio_nhan, _, _, so_dt, _, nd_goc) in listChat) {
                    if (jsonALlKH.has(so_dt) && ngay_nhan.contains(date) && !nd_goc.contains(
                            OK_TIN
                        )
                    ) {
                        val jsonKH = jsonALlKH.getJSONObject(so_dt)
                        if (!jsonKH.getString(TYPE_KH).contains("3") && (!jsonKH.getString(TYPE_KH)
                                .contains("2") || typeKh != 2)
                        ) {
                            val isTypeKhach = jsonKH.getString(TYPE_KH).contains("1") && typeKh == 1

                            val trungTin = !Setting.I.nhanTinTrung.isTrue() && jsonKH.has(nd_goc)

                            if (!trungTin || !isTypeKhach) {
                                jsonKH.put("so_tn", jsonKH.getInt("so_tn") + 1)
                                jsonKH.put(nd_goc, nd_goc)
                                jsonALlKH.put(so_dt, jsonKH)
                                listTinNhanS_i.add(
                                    TinNhanS(
                                        null,
                                        date,
                                        gio_nhan,
                                        typeKh,
                                        jsonKH.getString(TEN_KH),
                                        so_dt,
                                        SMS,
                                        jsonKH.getInt("so_tn"),
                                        nd_goc,
                                        nd_goc,
                                        nd_goc,
                                        "ko",
                                        0,
                                        0,
                                        0,
                                        null
                                    )
                                )
                            }
                        }
                    }
                }
                TinNhanStore.I.insertList(listTinNhanS_i)
            }
            reLoadLv()
            Toast.makeText(activity, "Đã tải xong tin nhắn!", Toast.LENGTH_LONG).show()
        } catch (ignored: Throwable) {
            Log.e(ContentValues.TAG, "getFullSms: Throwable $ignored")
        }
    }

    fun getAllChat(typeKh: Int, khachHang: KhachHang, resetLv: () -> Unit) {
        val mDate = MainState.dateYMD
        var soTN = 0
        db.queryData("DELETE FROM tbl_soctS WHERE ngay_nhan = '" + mDate + "' AND so_dienthoai = '" + khachHang.sdt + "'")
        db.queryData("DELETE FROM tbl_tinnhanS WHERE ngay_nhan = '" + mDate + "' AND so_dienthoai = '" + khachHang.sdt + "'")
        val useApp = BaseStore.getStringField(
            KhachHang.TABLE,
            KhachHang.USE_APP,
            "sdt = '" + khachHang.sdt + "'"
        )
        var query = "Where ngay_nhan = '" + mDate + "' AND ten_kh = '" + khachHang.ten_kh + "'"
        if (typeKh != 3) query += " AND type_kh = $typeKh"
        val listChat = BaseStore.selectChatsQuery(
            query
        )
        val writableDatabase = db.writableDatabase
        val ih = DatabaseUtils.InsertHelper(writableDatabase, "tbl_tinnhanS")
        writableDatabase.beginTransaction()
        try {
            if (listChat.isNotEmpty()) {
                for ((_, _, gio_nhan, type_kh1, _, _, _, nd_goc) in listChat) {
                    soTN++
                    ih.prepareForInsert()
                    ih.bind(ih.getColumnIndex("ngay_nhan"), mDate)
                    ih.bind(ih.getColumnIndex("gio_nhan"), gio_nhan)
                    ih.bind(ih.getColumnIndex("type_kh"), type_kh1)
                    ih.bind(ih.getColumnIndex("ten_kh"), khachHang.ten_kh)
                    ih.bind(ih.getColumnIndex("so_dienthoai"), khachHang.sdt)
                    ih.bind(ih.getColumnIndex("use_app"), useApp)
                    ih.bind(ih.getColumnIndex("so_tin_nhan"), soTN)
                    ih.bind(ih.getColumnIndex("nd_goc"), nd_goc)
                    ih.bind(ih.getColumnIndex("nd_sua"), nd_goc)
                    ih.bind(ih.getColumnIndex("nd_phantich"), nd_goc)
                    ih.bind(ih.getColumnIndex("phat_hien_loi"), "ko")
                    ih.bind(ih.getColumnIndex("tinh_tien"), 0)
                    ih.bind(ih.getColumnIndex("ok_tn"), 0)
                    ih.bind(ih.getColumnIndex("del_sms"), 0)
                    ih.execute()
                }
            }
            writableDatabase.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } catch (th: Throwable) {
            writableDatabase.endTransaction()
            ih.close()
            writableDatabase.close()
            throw th
        }
        writableDatabase.endTransaction()
        ih.close()
        writableDatabase.close()
        db.queryData("Delete From tbl_tinnhanS where substr(nd_goc,0,7) = 'Ok Tin'")
        db.queryData("Delete From tbl_tinnhanS where length(nd_goc) < 4")
        resetLv()
        Toast.makeText(activity, "Đã tải xong tin nhắn!", Toast.LENGTH_LONG).show()
    }

    fun addTin(
        mDate: String,
        mGionhan: String,
        type_kh: Int,
        khachHang: KhachHang,
        khachHangDB: KhachHang,
        textSua: String
    ) {
        val maxSoTn = BaseStore.getMaxSoTinNhan(
            mDate,
            type_kh,
            "so_dienthoai = '" + khachHang.sdt + "'"
        )
        TinNhanStore.I.insert(
            TinNhanS(
                null,
                mDate,
                mGionhan,
                type_kh,
                khachHang.ten_kh,
                khachHang.sdt,
                khachHangDB.use_app,
                maxSoTn + 1,
                textSua,
                textSua,
                textSua,
                "ko",
                0,
                0,
                0,
                null
            )
        )
        val query = "ngay_nhan = '" + mDate + "' AND so_dienthoai = '" + khachHang.sdt +
                "' AND so_tin_nhan = " + (maxSoTn + 1) + " AND type_kh = " + type_kh
        val idTinNhan = BaseStore.getIntField(TinNhanS.TABLE_NAME, TinNhanS.ID, query)
        if (MainState.checkHSD() && idTinNhan > 0) {
            try {
                XuLyTinNhanService.I.upsertTinNhan(idTinNhan, khachHangDB.type_kh)
            } catch (e: Exception) {
                TinNhanStore.I.update(id = idTinNhan, phatHienLoi = "ko")
                SoctStore.I.deleteWhere(
                    "ngay_nhan = '$mDate' AND so_dienthoai = '${khachHang.sdt}' AND so_tin_nhan = ${maxSoTn + 1} AND type_kh = $type_kh"
                )
                Toast.makeText(activity, "Đã xảy ra lỗi!!! ${e.message}", Toast.LENGTH_LONG).show()
            } catch (throwable: Throwable) {
            }
        } else {
            Dialog.hetHSD(activity)
        }
        TelegramHandle.sms = true
    }

    companion object {
        const val NGAY_NHAN = "ngay_nhan"
        const val SO_DIENTHOAI = "so_dienthoai"
        const val ND_SUA = "nd_sua"
        const val ND_GOC = "nd_goc"
        const val SO_TIN_NHAN = "so_tin_nhan"
        const val SMS = "sms"
        const val USE_APP = "use_app"
        const val TEN_KH = "ten_kh"
        const val TYPE_KH = "type_kh"
        const val GIO_NHAN = "gio_nhan"
        const val OK_TIN = "Ok Tin"
    }
}