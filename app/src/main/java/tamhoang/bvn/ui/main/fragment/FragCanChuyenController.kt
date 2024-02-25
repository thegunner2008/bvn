package tamhoang.bvn.ui.main.fragment

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import org.json.JSONException
import org.json.JSONObject
import tamhoang.bvn.data.BaseStore
import tamhoang.bvn.data.DbOpenHelper
import tamhoang.bvn.data.model.Chat
import tamhoang.bvn.data.model.TinNhanS
import tamhoang.bvn.data.services.XuatSoService
import tamhoang.bvn.data.services.guiTinNhan.GuiTinNhanService
import tamhoang.bvn.data.services.xuLyTinNhan.XuLyTinNhanService
import tamhoang.bvn.data.setting.Setting
import tamhoang.bvn.data.store.KhachHangStore
import tamhoang.bvn.data.store.TinNhanStore
import tamhoang.bvn.messageCenter.notification.NotificationNewReader
import tamhoang.bvn.messageCenter.telegram.TelegramHandle
import tamhoang.bvn.ui.main.MainState
import tamhoang.bvn.util.extensions.clear
import tamhoang.bvn.util.ld.CongThuc
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class FragCanChuyenController(val db: DbOpenHelper) {
    private val chuyenXiTheoDiem = Setting.I.chuyenXien.value() > 0

    fun xuatTin(
        context: FragmentActivity?,
        mDangXuat: String?,
        textTien: String,
        min: Int,
        max: Int,
        mTienTon: ArrayList<String>,
        mSo: ArrayList<String>
    ): String? {
        val tienChuyen: Int
        var maxTien: Int
        var maxTien2: Int
        var maxTien3: Int
        var xuatDan = ""
        val curDate = MainState.dateYMD
        val mDate = LocalDate.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val mLamtron =
            when (Setting.I.lamTron.value()) {
                0 -> 1
                1 -> 10
                2 -> 50
                3 -> 100
                else -> 1
            }
        if (mDate.contains(curDate)) {
//            if (this.edt_tien.getText().toString().length() != 0) {
//                if (this.edt_tien.getText().toString() != "0") {

            val clearTien = textTien.clear(listOf("%", "n", "k", "d", ">", "."))
            tienChuyen = if (CongThuc.isNumeric(clearTien)) {
                clearTien.toInt()
            } else {
                0
            }
            when (mDangXuat) {
                "the_loai = 'xi'" -> {
                    xuatDan = "Xien:\n"
                    for (i in min until mSo.size) {
                        maxTien3 = if (textTien.contains("%")) {
                            mTienTon[i].replace(".", "").toInt() / mLamtron * mLamtron
                        } else if (textTien.contains(">")) {
                            mTienTon[i].replace(".", "").toInt() / mLamtron * mLamtron
                        } else if (tienChuyen == 0) {
                            mTienTon[i].replace(".", "").toInt() / mLamtron * mLamtron
                        } else if (mTienTon[i].replace(".", "").toInt() > tienChuyen) {
                            tienChuyen / mLamtron * mLamtron
                        } else {
                            mTienTon[i].replace(".", "").toInt() / mLamtron * mLamtron
                        }
                        try {
                            if (textTien.contains("%")) {
                                if (maxTien3 > 0) {
                                    xuatDan += if (chuyenXiTheoDiem)
                                        mSo[i] + "x" + maxTien3 * tienChuyen / 1000 + "d "
                                    else
                                        mSo[i] + "x" + maxTien3 * tienChuyen / 100 + "n "
                                }
                            } else if (textTien.contains(">")) {
                                if (maxTien3 > tienChuyen) {
                                    xuatDan += if (chuyenXiTheoDiem)
                                        mSo[i] + "x" + (maxTien3 - tienChuyen) / 10 + "d "
                                    else
                                        mSo[i] + "x" + (maxTien3 - tienChuyen) + "n "
                                }
                            } else if (maxTien3 > 0) {
                                xuatDan += if (chuyenXiTheoDiem)
                                    mSo[i] + "x" + maxTien3 / 10 + "d "
                                else
                                    mSo[i] + "x" + maxTien3 + "n "

                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }
                "the_loai = 'bc'" -> {
                    xuatDan = "Cang:\n"
                    var tien = 0
                    for (i in min until mSo.size) {
                        maxTien2 = if (tienChuyen == 0) {
                            mTienTon[i].replace(".", "").toInt() / mLamtron * mLamtron
                        } else if (textTien.contains("%")) {
                            mTienTon[i].replace(".", "").toInt() * tienChuyen / mLamtron / 100 * mLamtron
                        } else if (textTien.contains(">")) {
                            (mTienTon[i].replace(".", "").toInt() - tienChuyen) / mLamtron * mLamtron
                        } else if (mTienTon[i].replace(".", "").toInt() > tienChuyen) {
                            tienChuyen / mLamtron * mLamtron
                        } else {
                            mTienTon[i].replace(".", "").toInt() / mLamtron * mLamtron
                        }
                        if (maxTien2 > 0) {
                            xuatDan += if (tien > maxTien2) "x${tien}n ${mSo[i]}," else "${mSo[i]},"
                            tien = maxTien2
                        }
                    }
                    if (xuatDan.length > 4) {
                        xuatDan += "x${tien}n"
                    }
                    if (xuatDan.contains(":") && (xuatDan.substring(xuatDan.indexOf(":")).length > 7)) {
                        return xuatDan
                    }
                    Toast.makeText(context, "Không có số liệu!", Toast.LENGTH_LONG).show()
                    return null
                }
                "the_loai = 'xn'" -> {
                    xuatDan = "Xnhay:\n"
                    for (i in min until mSo.size) {
                        maxTien = if (textTien.contains("%"))
                            mTienTon[i].replace(".", "").toInt() / mLamtron * mLamtron
                        else if (textTien.contains(">"))
                            mTienTon[i].replace(".", "").toInt() / mLamtron * mLamtron
                        else if (tienChuyen == 0)
                            mTienTon[i].replace(".", "").toInt() / mLamtron * mLamtron
                        else if (mTienTon[i].replace(".", "").toInt() > tienChuyen)
                            tienChuyen / mLamtron * mLamtron
                        else
                            mTienTon[i].replace(".", "").toInt() / mLamtron * mLamtron

                        try {
                            if (textTien.contains("%")) {
                                if (maxTien > 0) {
                                    xuatDan += if (chuyenXiTheoDiem)
                                        "${mSo[i]}x${maxTien * tienChuyen / 1000}d\n"
                                    else
                                        "${mSo[i]}x${maxTien * tienChuyen / 100}n\n"
                                }
                            } else if (textTien.contains(">")) {
                                if (maxTien > tienChuyen) {
                                    xuatDan += if (chuyenXiTheoDiem)
                                        "${mSo[i]}x${(maxTien - tienChuyen) / 10}d\n"
                                    else
                                        "${mSo[i]}x${maxTien - tienChuyen}n\n"
                                }
                            } else if (maxTien > 0) {
                                xuatDan += if (chuyenXiTheoDiem)
                                    "${mSo[i]}x${maxTien / 10}d\n"
                                else
                                    "${mSo[i]}x${maxTien}n\n"
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }
                "(the_loai = 'deb' or the_loai = 'det')" -> xuatDan =
                    XuatSoService.I.xuat("deb", textTien, min, max)
                "the_loai = 'dea'" -> xuatDan = XuatSoService.I.xuat("dea", textTien, min, max)
                "the_loai = 'dec'" -> xuatDan = XuatSoService.I.xuat("dec", textTien, min, max)
                "the_loai = 'ded'" -> xuatDan = XuatSoService.I.xuat("ded", textTien, min, max)
                "the_loai = 'loa'" -> xuatDan = XuatSoService.I.xuat("loa", textTien, min, max)
                "the_loai = 'lo'" -> xuatDan = XuatSoService.I.xuat("lo", textTien, min, max)
                else -> Toast.makeText(context, "Không có số liệu!", Toast.LENGTH_LONG).show()
            }
            return xuatDan
        }
        Toast.makeText(context, "Không làm việc với dữ liệu ngày cũ!", Toast.LENGTH_LONG).show()
        return null
    }

    fun taoTinDe(ten_kh: String?, jsonKhongmax: JSONObject?, textTien: String, min: Int, max: Int): String {
        var soDe = JSONObject()
        try {
            soDe = JSONObject(jsonKhongmax!!.getString("soDe"))
        } catch (ignored: JSONException) {
        }
        val khongMaxs = HashMap<String?, Int?>()
        val keys = soDe.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            khongMaxs[key] = soDe.optInt(key)
        }
        val soToDiem =
            db.getData("Select so_chon, Sum(diem) FROM tbl_soctS Where ten_kh = '" + ten_kh + "' AND type_kh = 2 AND (the_loai = 'deb' or the_loai = 'det') AND ngay_nhan = '${MainState.dateYMD}' Group by so_chon")
        while (soToDiem.moveToNext()) {
            try {
                val soChon = soToDiem.getString(0)
                val seChuyen = khongMaxs[soChon]
                if (seChuyen != null) {
                    val daChuyen = soToDiem.getInt(1)
                    val conLai = seChuyen - daChuyen
                    khongMaxs[soChon] = conLai.coerceAtLeast(0)
                }
            } catch (th: Throwable) {
                if (!soToDiem.isClosed) soToDiem.close()
            }
        }
        return XuatSoService.I.xuat("deb", textTien, min, max, khongMaxs)
    }

    fun taoTinDeDauDB(ten_kh: String?, jsonKhongmax: JSONObject?, textTien: String, min: Int, max: Int): String {
        var soDe = JSONObject()
        try {
            soDe = JSONObject(jsonKhongmax!!.getString("soDauDB"))
        } catch (ignored: JSONException) {
        }
        val khongMaxs = HashMap<String?, Int?>()
        val keys = soDe.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            khongMaxs[key] = soDe.optInt(key)
        }
        val soToDiem =
            db.getData("Select so_chon, Sum(diem) FROM tbl_soctS Where ten_kh = '" + ten_kh + "' AND type_kh = 2 AND the_loai = 'dea' AND ngay_nhan = '${MainState.dateYMD}' Group by so_chon")
        while (soToDiem.moveToNext()) {
            try {
                val soChon = soToDiem.getString(0)
                val seChuyen = khongMaxs[soChon]
                if (seChuyen != null) {
                    val daChuyen = soToDiem.getInt(1)
                    val conLai = seChuyen - daChuyen
                    khongMaxs[soChon] = conLai.coerceAtLeast(0)
                }
            } catch (th: Throwable) {
                if (!soToDiem.isClosed) soToDiem.close()
            }
        }
        return XuatSoService.I.xuat("dea", tienXuat = textTien, from = min, to = max, khongMaxs = khongMaxs)
    }

    fun taoTinDeDauG1(ten_kh: String?, jsonKhongmax: JSONObject?, textTien: String, min: Int, max: Int): String {
        var soDe = JSONObject()
        try {
            soDe = JSONObject(jsonKhongmax!!.getString("soDauG1"))
        } catch (ignored: JSONException) {
        }
        val khongMaxs = HashMap<String?, Int?>()
        val keys = soDe.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            khongMaxs[key] = soDe.optInt(key)
        }
        val soToDiem =
            db.getData("Select so_chon, Sum(diem) FROM tbl_soctS Where ten_kh = '" + ten_kh + "' AND type_kh = 2 AND the_loai = 'dec' AND ngay_nhan = '${MainState.dateYMD}' Group by so_chon")
        while (soToDiem.moveToNext()) {
            try {
                val soChon = soToDiem.getString(0)
                val seChuyen = khongMaxs[soChon]
                if (seChuyen != null) {
                    val daChuyen = soToDiem.getInt(1)
                    val conLai = seChuyen - daChuyen
                    khongMaxs[soChon] = conLai.coerceAtLeast(0)
                }
            } catch (th: Throwable) {
                if (!soToDiem.isClosed) soToDiem.close()
            }
        }
        return XuatSoService.I.xuat("dec", tienXuat = textTien, from = min, to = max, khongMaxs = khongMaxs)
    }

    fun taoTinDeDitG1(ten_kh: String?, jsonKhongmax: JSONObject?, textTien: String, min: Int, max: Int): String {
        var soDe = JSONObject()
        try {
            soDe = JSONObject(jsonKhongmax!!.getString("soDitG1"))
        } catch (ignored: JSONException) {
        }
        val khongMaxs = HashMap<String?, Int?>()
        val keys = soDe.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            khongMaxs[key] = soDe.optInt(key)
        }
        val soToDiem =
            db.getData("Select so_chon, Sum(diem) FROM tbl_soctS Where ten_kh = '" + ten_kh + "' AND type_kh = 2 AND the_loai = 'ded' AND ngay_nhan = '${MainState.dateYMD}' Group by so_chon")
        while (soToDiem.moveToNext()) {
            try {
                val soChon = soToDiem.getString(0)
                val seChuyen = khongMaxs[soChon]
                if (seChuyen != null) {
                    val daChuyen = soToDiem.getInt(1)
                    val conLai = seChuyen - daChuyen
                    khongMaxs[soChon] = conLai.coerceAtLeast(0)
                }
            } catch (th: Throwable) {
                if (!soToDiem.isClosed) soToDiem.close()
            }
        }
        return XuatSoService.I.xuat("ded", tienXuat = textTien, from = min, to = max, khongMaxs = khongMaxs)
    }

    fun taoTinLo(ten_kh: String?, jsonKhongmax: JSONObject?, textTien: String, min: Int, max: Int): String {
        var soLo = JSONObject()
        try {
            soLo = JSONObject(jsonKhongmax!!.getString("soLo"))
        } catch (ignored: JSONException) {
        }
        val khongMaxs = HashMap<String?, Int?>()
        val keys = soLo.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            khongMaxs[key] = soLo.optInt(key)
        }
        val qr =
            "Select so_chon, Sum(diem) FROM tbl_soctS Where ten_kh = '" + ten_kh + "' AND type_kh = 2 AND the_loai = 'lo' AND ngay_nhan = '${MainState.dateYMD}' Group by so_chon"
        val cursor = db.getData(qr)
        while (cursor.moveToNext()) {
            try {
                val soChon = cursor.getString(0)
                val seChuyen = khongMaxs[soChon]
                if (seChuyen != null) {
                    val daChuyen = cursor.getInt(1)
                    val conLai = seChuyen - daChuyen
                    khongMaxs[soChon] = conLai.coerceAtLeast(0)
                }
            } catch (th: Throwable) {
                if (!cursor.isClosed) cursor.close()
            }
        }
        return XuatSoService.I.xuat("lo", tienXuat = textTien, from = min, to = max, khongMaxs = khongMaxs)
    }

    fun xulytin(
        activity: FragmentActivity?,
        mMobile: ArrayList<String>,
        mContact: ArrayList<String>,
        mSpiner: Int,
        soTinNhan: Int,
        noidung: String
    ) {
        val mDate = MainState.dateYMD
        val mGionhan = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        if (mSpiner > -1) {
            val khachHang = KhachHangStore.I.selectBySdt(mMobile[mSpiner]) ?: return
            val typeKh =
                if (khachHang.type_kh == 3) 2
                else
                    khachHang.type_kh
            TinNhanStore.I.insert(
                TinNhanS(
                    null, mDate, mGionhan, typeKh, mContact[mSpiner], mMobile[mSpiner], "2", soTinNhan,
                    "Tin $soTinNhan:$noidung", null, noidung, "ko", 0, 0, 1, null
                )
            )
            val tinNhan = TinNhanStore.I.selectWhere(
                "ngay_nhan = '$mDate' AND so_dienthoai = '${mMobile[mSpiner]}' AND type_kh = 2 AND so_tin_nhan = $soTinNhan"
            ) ?: return
            if (CongThuc.checkDate(MainState.hanSuDung)) {
                try {
                    XuLyTinNhanService.I.upsertTinNhan(tinNhan.ID ?: 0, khachHang.type_kh)
                    val noiDungTin = "Tin $soTinNhan:\n$noidung"
                    if (khachHang.use_app.contains("TL")) {
                        Handler(Looper.getMainLooper()).post {
                            TelegramHandle.sendMessage(
                                mMobile[mSpiner].toLong(), noiDungTin
                            )
                        }
                        BaseStore.insertChat(
                            Chat(
                                null, mDate, mGionhan, 2, mContact[mSpiner], mMobile[mSpiner],
                                khachHang.use_app, noiDungTin, 1
                            )
                        )
                    } else if (khachHang.use_app.contains("sms")) {
                        GuiTinNhanService.I.sendSMS(mMobile[mSpiner], noiDungTin)
                    } else {
                        NotificationNewReader().reply(mMobile[mSpiner], noiDungTin)
                        BaseStore.insertChat(
                            Chat(
                                null, mDate, mGionhan, 2, mContact[mSpiner], mMobile[mSpiner],
                                khachHang.use_app, noiDungTin, 1
                            )
                        )
                    }
                } catch (e: Exception) {
                    db.queryData("Update tbl_tinnhanS set phat_hien_loi = 'ko' WHERE id = ${tinNhan.ID}")
                    db.queryData("Delete From tbl_soctS WHERE ngay_nhan = '" + mDate + "' AND so_dienthoai = '" + mMobile[mSpiner] + "' AND so_tin_nhan = " + soTinNhan)
                    Toast.makeText(activity, "Đã xảy ra lỗi!", Toast.LENGTH_LONG).show()
                } catch (throwable: Throwable) {
                    throwable.printStackTrace()
                }
            } else {
                try {
                    Toast.makeText(
                        activity,
                        "Đã hết hạn sử dụng \n\nHãy liên hệ đại lý hoặc SĐT: ${MainState.thongTinAcc.phone} để gia hạn",
                        Toast.LENGTH_LONG
                    ).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}