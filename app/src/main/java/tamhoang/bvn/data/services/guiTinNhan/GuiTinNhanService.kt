package tamhoang.bvn.data.services.guiTinNhan

import android.content.ContentValues
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import org.json.JSONException
import org.json.JSONObject
import tamhoang.bvn.data.BaseStore
import tamhoang.bvn.data.DbOpenHelper
import tamhoang.bvn.data.model.Chat
import tamhoang.bvn.data.model.TinNhanS
import tamhoang.bvn.data.services.SoctService
import tamhoang.bvn.data.setting.Setting
import tamhoang.bvn.data.store.KhachHangStore
import tamhoang.bvn.data.store.TinNhanStore
import tamhoang.bvn.messageCenter.notification.NotificationNewReader
import tamhoang.bvn.messageCenter.telegram.TelegramHandle
import tamhoang.bvn.ui.main.MainState
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class GuiTinNhanService(val db: DbOpenHelper) {
    companion object {
        lateinit var I: GuiTinNhanService
    }

    fun replyKhach(idTinNhan: Int) { //test
        var useApp: String? = null
        val ngayNow = LocalDate.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val gioNow = LocalTime.now()
            .format(DateTimeFormatter.ofPattern("HH:mm:ss"))

        val tns = TinNhanStore.I.selectByID(idTinNhan) ?: return
        val soTN = tns.soTinNhan
        val soDT = tns.sdt
        val gioNhan = tns.gioNhan
        val ngayNhan = tns.ngayNhan
        val ndPhanTich = tns.ndPhanTich
        val ndGoc = tns.ndGoc

        val kieuTraLoi = try {
            KhachHangStore.I.getSettingKHBySdt(soDT).traLoiTin.value() + 1
        } catch (_: JSONException) {
            2
        }
        if (tns.okTn == 1) {
            val ndTraLoi = when (kieuTraLoi) {
                5 -> "Ok Tin $soTN"
                6 -> "Ok Tin " + soTN + "\n" + tns.ndGoc
                else -> ""
            }
            if (ndTraLoi !== "")
                sendAndInsertDB(tns, soDT, ndTraLoi, ngayNow, gioNow, ngayNhan, soTN)
        }
        if (tns.phatHienLoi == "ok" && tns.okTn == 1) {

            val ndTraLoi = if (kieuTraLoi == 4) {
                if (!ndPhanTich.contains("Bỏ "))
                    "Ok tin $soTN\n$ndGoc"
                else
                    ndPhanTich.substring(0, ndPhanTich.indexOf("\n") - 1) +
                            "\nOK Tin $soTN\n$ndGoc"
            } else if (kieuTraLoi == 2 && !ndPhanTich.contains("Bỏ "))
                "Ok tin $soTN"
            else if (kieuTraLoi == 1)
                "Ok tin $soTN\n$ndPhanTich"
            else
                ""
            if (ndTraLoi !== "")
                sendAndInsertDB(tns, soDT, ndTraLoi, ngayNow, gioNow, ngayNhan, soTN)

            val chuyenThang = BaseStore.selectChuyenThang(soDT)
            if (chuyenThang != null) {
                if (tns.delSms == 1) {
                    val maxSoTn = BaseStore.getMaxSoTinNhan(
                        ngayNhan, 2, "so_dienthoai = '${chuyenThang.sdt_chuyen}'"
                    )
                    val soTn = maxSoTn + 1
                    useApp = if (chuyenThang.kh_chuyen.contains("TL")) "TL"
                    else if (chuyenThang.kh_chuyen.contains("ZL")) "ZL"
                    else if (chuyenThang.kh_chuyen.contains("VB")) "VB"
                    else if (chuyenThang.kh_chuyen.contains("WA")) "WA"
                    else "sms"

                    TinNhanStore.I.insert(
                        tns.copy(
                            ID = null,
                            typeKh = 2,
                            tenKh = chuyenThang.kh_chuyen,
                            sdt = chuyenThang.sdt_chuyen,
                            useApp = useApp,
                            phatHienLoi = "ok",
                            tinhTien = 0,
                            okTn = 0,
                            delSms = 1
                        )
                    )

                    db.queryData(
                        "Update tbl_tinnhanS set del_sms = 0 WHERE ngay_nhan = '$ngayNhan' AND so_dienthoai = '$soDT' AND so_tin_nhan = $soTN"
                    )

                    val tns2 = TinNhanStore.I.select(tns.ngayNhan, chuyenThang.kh_chuyen, soTn, 2) ?: return
                    try {
                        if (tns2.ID == null) return
                        SoctService.I.nhapSoChiTiet(tns2.ID!!)
                    } catch (throwable: Throwable) {
                    }
                    val chuyenNdGoc = BaseStore.chuyenThangNdGoc()
                    val ndTin = if (chuyenNdGoc) tns.ndGoc else tns.ndPhanTich
                    val tinNhan = "Tin $soTn:\n$ndTin"
                    val chat = Chat(
                        null, ngayNow, gioNow, 2, chuyenThang.kh_chuyen,
                        chuyenThang.sdt_chuyen, useApp, tinNhan, 1
                    )
                    send(chuyenThang.sdt_chuyen, tinNhan, tns2.useApp, chat)
                }
            }
            if (tns.typeKh != 2 && Setting.I.baoTinThieu.isTrue()) {

                val listSoTn = db.getOneColumn<Int>(
                    TinNhanS.TABLE_NAME,
                    "ngay_nhan = '$ngayNhan' AND so_dienthoai = '$soDT' AND type_kh = 1 ORDER BY so_tin_nhan",
                    TinNhanS.SO_TIN_NHAN
                )
                val maxTin = listSoTn.max() ?: 1
                val tinThieu = (1..maxTin).filterNot { listSoTn.contains(it) }
                if (tinThieu.isNotEmpty()) {
                    val ndThieu = "Thiếu tin " + tinThieu.joinToString(",") + ","
                    val chat = Chat(
                        null, ngayNow, gioNow, 2, tns.tenKh, tns.sdt, tns.useApp, ndThieu, 1
                    )
                    send(tns.sdt, ndThieu, tns.useApp, chat, thieu = true)
                }
            }
        }

        val delSms = db.getOneData<Int>(
            TinNhanS.TABLE_NAME,
            "ngay_nhan = '$ngayNhan' AND so_dienthoai = '$soDT' AND so_tin_nhan = $soTN",
            TinNhanS.DEL_SMS
        )
        val chuyenThang = BaseStore.selectChuyenThang(soDT)
        val chuyenNdGoc = BaseStore.chuyenThangNdGoc()
        if (chuyenThang != null && chuyenNdGoc && delSms == 1) {
            val maxSoTn = BaseStore.getMaxSoTinNhan(
                ngayNhan, null, "so_dienthoai = '${chuyenThang.sdt_chuyen}'"
            )
            val soTn = maxSoTn + 1
            try {
                val myApp = if (chuyenThang.kh_chuyen.contains("ZL")) "ZL"
                else if (chuyenThang.kh_chuyen.contains("VB")) "VB"
                else if (chuyenThang.kh_chuyen.contains("WA")) "WA"
                else if (chuyenThang.kh_chuyen.contains("TL")) "TL" else "sms"
                TinNhanStore.I.insert(
                    tns.copy(
                        ID = null,
                        gioNhan = gioNhan,
                        typeKh = 2,
                        tenKh = chuyenThang.kh_chuyen,
                        sdt = chuyenThang.sdt_chuyen,
                        useApp = myApp,
                        soTinNhan = soTn,
                        ndSua = null,
                        phatHienLoi = "ko",
                        tinhTien = 0,
                        okTn = 0,
                        delSms = 1,
                        phanTich = null
                    )
                )

                val tinNhan = "Tin " + soTn + ":\n" + tns.ndGoc
                val chat = Chat(
                    null, ngayNow, gioNow, 2, chuyenThang.kh_chuyen,
                    chuyenThang.sdt_chuyen, useApp!!, tinNhan, 1
                )

                send(chuyenThang.sdt_chuyen, tinNhan, myApp, chat, checkTime = false)
            } catch (exception: Exception) {
                Log.e(ContentValues.TAG, "Gui_Tin_Nhan: error" + exception.message)
            }
            db.queryData(
                "Update tbl_tinnhanS set del_sms = 0 WHERE ngay_nhan = '$ngayNhan' AND so_dienthoai = '$soDT' AND so_tin_nhan = $soTN"
            )
            TraLai.run(idTinNhan, db)
            return
        }
        TraLai.run(idTinNhan, db)
    }

    @Throws(JSONException::class)
    private fun sendAndInsertDB(
        tinNhan: TinNhanS, soDT: String, ndTraLoi: String, ngayNhan: String, gioNhan: String, ngay: String, soTn: Int
    ) {
        if (tinNhan.useApp.contains("sms")) {
            sendSMS(soDT, ndTraLoi)
        } else if (tinNhan.useApp.contains("TL")) {
            Handler(Looper.getMainLooper()).post {
                TelegramHandle.sendMessage(
                    soDT.toLong(),
                    ndTraLoi
                )
            }
        } else {
            val jsonObject = JSONObject(MainState.jsonTinnhan.getString(soDT))
            if (jsonObject.getInt("Time") < 3) {
                NotificationNewReader().reply(tinNhan.sdt, ndTraLoi)
            } else {
                jsonObject.put(ndTraLoi, "OK")
                MainState.jsonTinnhan.put(soDT, jsonObject)
            }
            val chat = Chat(
                null, ngayNhan, gioNhan, 2, tinNhan.tenKh, tinNhan.sdt, tinNhan.useApp, ndTraLoi, 1
            )
            BaseStore.insertChat(chat)
        }
        db.queryData(
            "Update tbl_tinnhanS set ok_tn = 0 WHERE ngay_nhan = '$ngay' AND so_dienthoai = '$soDT' AND so_tin_nhan = $soTn"
        )
    }

    private fun send(
        soDT: String, ndTraLoi: String, useApp: String, chat: Chat,
        checkTime: Boolean = true, thieu: Boolean = false
    ) {
        if (useApp.contains("sms")) {
            sendSMS(soDT, ndTraLoi)
        } else if (useApp.contains("TL")) {
            Handler(Looper.getMainLooper()).post {
                TelegramHandle.sendMessage(
                    soDT.toLong(),
                    ndTraLoi
                )
            }
        } else {
            if (checkTime) {
                val jsonObject = JSONObject(MainState.jsonTinnhan.getString(soDT))
                if (jsonObject.getInt("Time") < 3) {
                    NotificationNewReader().reply(soDT, ndTraLoi)
                    if (thieu) BaseStore.insertChat(chat)
                } else {
                    jsonObject.put(ndTraLoi, "OK")
                    MainState.jsonTinnhan.put(soDT, jsonObject)
                }
            } else {
                NotificationNewReader().reply(soDT, ndTraLoi)
                BaseStore.insertChat(chat)
            }
        }
    }

    fun sendMessage(
        context: Context?, useApp: String, tenKH: String, sdt: String,
        message: String, actionDone: (Boolean) -> Unit
    ) {
        if (useApp.contains("sms")) {
            sendSMS(sdt, message)
            actionDone(true)
        } else if (useApp.contains("TL")) {
            TelegramHandle.sendMessage(sdt.toLong(), message)
            actionDone(true)
            try {
                Thread.sleep(1000L)
            } catch (_: InterruptedException) {
            }
        } else if (MainState.contactsMap.containsKey(tenKH)) {
            NotificationNewReader().reply(sdt, message)
            actionDone(true)
        } else {
            actionDone(false)
            context?.let {
                Toast.makeText(context, "Không có người này trong Chatbox", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun sendSMS(Sdt: String?, Mess: String?) { // gui sms
        val sms = SmsManager.getDefault()
        sms.sendMultipartTextMessage(Sdt, null, sms.divideMessage(Mess), null, null)
    }
}