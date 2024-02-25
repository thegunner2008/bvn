package tamhoang.bvn.ui.main

import android.os.Handler
import org.json.JSONException
import org.json.JSONObject
import tamhoang.bvn.data.Contact
import tamhoang.bvn.data.remote.Account
import tamhoang.bvn.data.store.KhachHangStore
import tamhoang.bvn.messageCenter.notification.NotificationNewReader
import tamhoang.bvn.messageCenter.notification.NotificationReader
import tamhoang.bvn.messageCenter.telegram.TelegramHandle
import tamhoang.bvn.util.extensions.to2ChuSo
import tamhoang.bvn.util.ld.CongThuc

object MainState {
    val isDebug = true
    var truncate_mode = false
    var telegramHandle: TelegramHandle? = null

    //Acc
    @JvmField
    var thongTinAcc = Account("", "", "")
    fun tenAcc() = thongTinAcc.type ?: "Free"
    fun phone() = thongTinAcc.phone ?: ""

    val hanSuDung: String
        get() {
            val dateArr = thongTinAcc.date!!.split("-").toTypedArray()
            return dateArr[2] + "/" + dateArr[1] + "/" + dateArr[0]
        }

    @JvmStatic
    fun checkHSD() = CongThuc.checkDate(hanSuDung)

    val textHSD: String
        get() = if (CongThuc.checkDate(hanSuDung)) hanSuDung else ""

    @JvmField
    var DSkhachhang = ArrayList<String>()

    @JvmStatic
    fun refreshDsKhachHang() {
        DSkhachhang = KhachHangStore.I.getListName()
    }

    @JvmField
    var Json_Tinnhan = JSONObject()

    @JvmField
    var MyToken = ""

    @JvmField
    var mNotifi: NotificationReader? = null
    var timeRemove = 0

    @JvmField
    var contactsMap = HashMap<String, Contact>()
    var formArray = ArrayList<HashMap<String, String>>()
    var formList = ArrayList<HashMap<String, String>>()

    @JvmField
    var handler: Handler? = null
    lateinit var jSon_Setting: JSONObject

    @JvmField
    var jsonTinnhan = JSONObject()
    var day = 0
    var month = 0
    var year = 0

    @JvmField
    var runnable: Runnable = object : Runnable {
        override fun run() {
            try {
                val keys = jsonTinnhan.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val json = JSONObject(jsonTinnhan.getString(key))
                    val newTime = json.optInt("Time") + 1
                    json.put("Time", newTime)
                    jsonTinnhan.put(key, json.apply { put("Time", newTime) }.toString())

                    if (newTime > 3 && json.length() > 1) {
                        val tinnhans = json.keys()
                        while (tinnhans.hasNext()) {
                            val tinnhan = tinnhans.next()
                            if (!tinnhan.contains("Time")) {
                                NotificationNewReader().reply(key, tinnhan)
                            }
                        }
                        val timeJson = JSONObject().apply {
                            put("Time", 0)
                        }
                        jsonTinnhan.put(key, timeJson.toString())
                    } else if (json.getInt("Time") > 100) {
                        jsonTinnhan.remove(key)
                        break
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            timeRemove = if (jsonTinnhan.length() == 0) timeRemove + 1 else 0
            if (timeRemove < 100) {
                handler!!.postDelayed(this, 1000)
            } else {
                handler!!.removeCallbacks(this)
                handler = null
            }
        }
    }

    //"yyyy-mm-dd"
    @JvmStatic
    val dateYMD: String
        get() = "$year-${month.to2ChuSo()}-${day.to2ChuSo()}"

    //"dd/mm/yyyy"
    val date2DMY: String
        get() = "${day.to2ChuSo()}/${month.to2ChuSo()}/$year"

    //"mm-dd-yyyy"
    val date3MDY: String
        get() = "${month.to2ChuSo()}-${day.to2ChuSo()}-$year"
}