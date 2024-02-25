package tamhoang.bvn.data.services.guiTinNhan

import android.os.Handler
import android.os.Looper
import org.json.JSONException
import org.json.JSONObject
import tamhoang.bvn.data.BaseStore
import tamhoang.bvn.data.BaseStore.getMaxSoTinNhan
import tamhoang.bvn.data.DbOpenHelper
import tamhoang.bvn.data.enum.TYPE
import tamhoang.bvn.data.model.SoctS
import tamhoang.bvn.data.model.TinNhanS
import tamhoang.bvn.data.services.SoctService
import tamhoang.bvn.data.services.xuLyTinNhan.XuLyTinNhanService
import tamhoang.bvn.data.store.KhachHangStore
import tamhoang.bvn.data.store.TinNhanStore
import tamhoang.bvn.messageCenter.notification.NotificationNewReader
import tamhoang.bvn.messageCenter.telegram.TelegramHandle
import tamhoang.bvn.ui.main.MainState
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object TraLai {
    private lateinit var jsonTralai: JSONObject

    @JvmStatic
    fun run(ID: Int, db: DbOpenHelper) {
        var traLai = ""
        jsonTralai = JSONObject()
        val tenKH = db.getOneData<String>(
            TinNhanS.TABLE_NAME,
            where = "id = $ID",
            column = TinNhanS.TEN_KH
        )
        try {
            val khachHang = KhachHangStore.I.selectByName(ten = tenKH) ?: return
            val json = JSONObject(khachHang.tbl_XS)
            if (json.getString("danDe").isNotEmpty()) {
                val traLaiDe = traDe(khachHang.ten_kh, json.getString("soDe"), db)
                if (traLaiDe.isNotEmpty()) {
                    traLai += "\n$traLaiDe"
                }
            }
            if (json.getString("danDauDB").isNotEmpty()) {
                val traLaiDea = traDea(khachHang.ten_kh, json.getString("soDauDB"), db)
                if (traLaiDea.isNotEmpty()) {
                    traLai += "\n$traLaiDea"
                }
            }
            if (json.getString("danDauG1").isNotEmpty()) {
                val traLaiDec = traDec(khachHang.ten_kh, json.getString("soDauG1"), db)
                if (traLaiDec.isNotEmpty()) {
                    traLai += "\n$traLaiDec"
                }
            }
            if (json.getString("danDitG1").isNotEmpty()) {
                val traLaiDed = traDed(khachHang.ten_kh, json.getString("soDitG1"), db)
                if (traLaiDed.isNotEmpty()) {
                    traLai += "\n$traLaiDed"
                }
            }
            if (json.getString("danLo").isNotEmpty()) {
                val traLaiLo = traLo(khachHang.ten_kh, json.getString("soLo"), db)
                if (traLaiLo.isNotEmpty()) {
                    traLai += "\n$traLaiLo"
                }
            }
            if (json.getInt("xien2") > 0 || json.getInt("xien3") > 0
                || json.getInt("xien4") > 0
            ) {
                val jsonXien = JSONObject().apply {
                    put("xien2", json.optInt("xien2"))
                    put("xien3", json.optInt("xien3"))
                    put("xien4", json.optInt("xien4"))
                }
                val traLaiXi = traXi(khachHang.ten_kh, jsonXien.toString(), db)
                if (traLaiXi.isNotEmpty()) {
                    traLai += "\n$traLaiXi"
                }
            }
            if (json.getInt("cang") > 0) {
                val traLaiCang = traCang(khachHang.ten_kh, json.getInt("cang"), db)
                if (traLaiCang.isNotEmpty()) {
                    traLai += "\n$traLaiCang"
                }
            }
            if (jsonTralai.length() > 0) {
                val ngayNow = LocalDate.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                val gioNow = LocalTime.now()
                    .format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                val maxSoTn =
                    getMaxSoTinNhan(ngayNow, 2, "ten_kh = '${khachHang.ten_kh}'")
                val soTN = maxSoTn + 1
                traLai = "Tra lai $soTN:$traLai"

                val tinNhanS = TinNhanS(
                    ID = null,
                    ngayNhan = ngayNow,
                    gioNhan = gioNow,
                    typeKh = 2,
                    tenKh = khachHang.ten_kh,
                    sdt = khachHang.sdt,
                    useApp = khachHang.use_app,
                    soTinNhan = soTN,
                    ndGoc = traLai.trim(),
                    ndSua = traLai.substring(traLai.indexOf(":") + 1),
                    ndPhanTich = traLai.substring(traLai.indexOf(":") + 1),
                    phatHienLoi = "ko",
                    tinhTien = 0,
                    okTn = 0,
                    delSms = 0,
                    phanTich = jsonTralai.toString()
                )
                TinNhanStore.I.insert(tinNhanS)

                val idTinNhanS = BaseStore.getIdTinNhanS(
                    "where ngay_nhan = '$ngayNow' AND type_kh = 2 AND ten_kh ='${khachHang.ten_kh}' AND nd_goc = '${traLai.trim()}'"
                ) ?: return

                XuLyTinNhanService.I.upsertTinNhan(idTinNhanS, 2)
                if (khachHang.use_app.contains("TL")) {
                    Handler(Looper.getMainLooper()).post {
                        TelegramHandle.sendMessage(khachHang.sdt.toLong(), traLai)
                    }
                } else if (khachHang.use_app.contains("sms")) {
                    GuiTinNhanService.I.sendSMS(khachHang.sdt, traLai)
                } else {
                    val jsonObject =
                        JSONObject(MainState.jsonTinnhan.getString(khachHang.sdt))
                    if (jsonObject.getInt("Time") > 3) {
                        NotificationNewReader().reply(khachHang.sdt, traLai)
                    } else {
                        jsonObject.put(traLai, "OK")
                        MainState.jsonTinnhan.put(khachHang.sdt, jsonObject)
                    }
                }
                SoctService.I.nhapSoChiTiet(idTinNhanS)
            }
        } catch (e: Exception) {
        } catch (throwable: Throwable) {
        }
    }


    private fun traDe(tenKH: String, danDe: String?, db: DbOpenHelper): String {
        val isDe8 = KhachHangStore.I.getSettingKHByName(tenKH).khachDe.value() == 1
        val theLoai = if (isDe8) "Det" else "De"
        val theLoaiQuery = if (isDe8) "det" else "deb"
        val theLoaiJson = if (isDe8) "de 8" else "de dit db"
        return traLoDe(
            tenKH = tenKH,
            danKhong = danDe,
            db = db,
            tLQuery = theLoaiQuery,
            tLJson = theLoaiJson,
            tLShow = theLoai
        )
    }

    private fun traDea(tenKH: String, danKhong: String?, db: DbOpenHelper) =
        traLoDe(tenKH = tenKH, danKhong = danKhong, db = db, tLQuery = "dea", tLJson = "dea", tLShow = "De dau db")

    private fun traDec(tenKH: String, danKhong: String?, db: DbOpenHelper) =
        traLoDe(tenKH = tenKH, danKhong = danKhong, db = db, tLQuery = "dec", tLJson = "dec", tLShow = "De đau nhat")

    private fun traDed(tenKH: String, danKhong: String?, db: DbOpenHelper) =
        traLoDe(tenKH = tenKH, danKhong = danKhong, db = db, tLQuery = "ded", tLJson = "ded", tLShow = "De dit nhat")

    private fun traLo(tenKH: String, danKhong: String?, db: DbOpenHelper) =
        traLoDe(tenKH = tenKH, danKhong = danKhong, db = db, tLQuery = "lo", tLJson = "lo", tLShow = "Lo")


    private fun traLoDe(
        tenKH: String,
        danKhong: String?,
        db: DbOpenHelper,
        tLQuery: String,
        tLJson: String,
        tLShow: String
    ): String {
        val ngayNow = LocalDate.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        var noiDung = ""
        try {
            val jsonKhong = JSONObject(danKhong!!)
            val data = db.getFullData(
                SoctS.TABLE,
                "ten_kh = '$tenKH' AND ngay_nhan = '$ngayNow' AND the_loai = '$tLQuery' Group by so_chon Order by so_chon",
                Pair(TYPE.String, "so_chon"),
                Pair(TYPE.Int, "Sum(diem_ton *(type_kh = 1))"),
                Pair(TYPE.Int, "Sum(diem_ton *(type_kh = 2))")
            )
            val listSoCt = data
                .asSequence()
                .map {
                    Triple(it[0] as String, it[1] as Int, it[2] as Int)
                }
                .filter { (soChon, _, _) ->
                    jsonKhong.has(soChon)
                }
                .map { (soChon, daNhan, daTra) ->
                    val khongTien = jsonKhong.getInt(soChon)
                    JSONObject().apply {
                        put("So_chon", soChon)
                        put("Da_nhan", daNhan)
                        put("Da_tra", daTra)
                        put("Khong_Tien", khongTien)
                        put("Se_tra", daNhan - daTra - khongTien)
                    }
                }
                .filter {
                    it.getInt("Se_tra") > 0
                }
                .sortedByDescending {
                    it.getInt("Se_tra")
                }
                .toList()
            if (listSoCt.isEmpty()) return ""
            listSoCt
                .groupBy { it.getInt("Se_tra") }
                .forEach { (seTra, soCTs) ->
                    val soChon = soCTs.joinToString(separator = ",") {
                        it.getString("So_chon")
                    }
                    val duLieu = soChon + "x" + seTra + "n "
                    val jsonTra = JSONObject().apply {
                        put("du_lieu", duLieu)
                        put("the_loai", tLJson)
                        put("dan_so", soChon)
                        put("so_tien", seTra)
                        put("so_luong", soCTs.size)
                    }
                    jsonTralai.put(
                        (jsonTralai.length() + 1).toString(),
                        jsonTra.toString()
                    )
                    noiDung += duLieu
                }
            return "$tLShow: $noiDung"
        } catch (e: JSONException) {
        }

        return noiDung
    }

    private fun traXi(tenKH: String, khongXi: String?, db: DbOpenHelper): String {
        fun getTheLoai(soChon: String) = when (soChon.length) {
            5 -> "xien2"
            8 -> "xien3"
            11 -> "xien4"
            else -> "###"
        }

        val ngayNow = LocalDate.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        var noiDung = ""
        try {
            val jsonKhong = JSONObject(khongXi!!)
            val data = db.getFullData(
                SoctS.TABLE,
                "ten_kh = '$tenKH' AND ngay_nhan = '$ngayNow' AND the_loai = 'xi' Group by so_chon Order by so_chon",
                Pair(TYPE.String, "so_chon"),
                Pair(TYPE.Int, "Sum(diem_ton *(type_kh = 1))"),
                Pair(TYPE.Int, "Sum(diem_ton *(type_kh = 2))")
            )
            val listSoCt = data
                .asSequence()
                .map {
                    Triple(it[0] as String, it[1] as Int, it[2] as Int)
                }
                .filter { (soChon, _, _) ->
                    jsonKhong.has(getTheLoai(soChon))
                }
                .map { (soChon, daNhan, daTra) ->
                    val khongTien = jsonKhong.optInt(getTheLoai(soChon))
                    JSONObject().apply {
                        put("So_chon", soChon)
                        put("Da_nhan", daNhan)
                        put("Da_tra", daTra)
                        put("Khong_Tien", khongTien)
                        put("Se_tra", daNhan - daTra - khongTien)
                    }
                }
                .filter {
                    it.getInt("Se_tra") > 0
                }
                .sortedByDescending {
                    it.getInt("Se_tra")
                }
                .toList()
            if (listSoCt.isEmpty()) return ""
            listSoCt
                .groupBy { it.getInt("Se_tra") }
                .forEach { (seTra, soCTs) ->
                    val soChon = soCTs.joinToString(separator = " ") {
                        it.getString("So_chon")
                    }
                    val duLieu = soChon + "x" + seTra + "n "
                    val jsonTra = JSONObject().apply {
                        put("du_lieu", duLieu)
                        put("the_loai", "xi")
                        put("dan_so", soChon)
                        put("so_tien", seTra)
                        put("so_luong", soCTs.size)
                    }
                    jsonTralai.put(
                        (jsonTralai.length() + 1).toString(),
                        jsonTra.toString()
                    )
                    noiDung += duLieu
                }
            return "Xien:\n$noiDung\n"
        } catch (e: JSONException) {
        }

        return noiDung
    }

    private fun traCang(tenKH: String, maxBc: Int, db: DbOpenHelper): String {
        val ngayNow = LocalDate.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        var noiDung = ""
        try {
            val data = db.getFullData(
                SoctS.TABLE,
                "ten_kh = '$tenKH' AND ngay_nhan = '$ngayNow' AND the_loai = 'bc' Group by so_chon Order by so_chon",
                Pair(TYPE.String, "so_chon"),
                Pair(TYPE.Int, "Sum(diem_ton *(type_kh = 1))"),
                Pair(TYPE.Int, "Sum(diem_ton *(type_kh = 2))")
            )
            val listSoCt = data
                .asSequence()
                .map {
                    Triple(it[0] as String, it[1] as Int, it[2] as Int)
                }
                .map { (soChon, daNhan, daTra) ->
                    JSONObject().apply {
                        put("So_chon", soChon)
                        put("Da_nhan", daNhan)
                        put("Da_tra", daTra)
                        put("Khong_Tien", maxBc)
                        put("Se_tra", daNhan - daTra - maxBc)
                    }
                }
                .filter {
                    it.getInt("Se_tra") > 0
                }
                .sortedByDescending {
                    it.getInt("Se_tra")
                }
                .toList()
            if (listSoCt.isEmpty()) return ""
            listSoCt
                .groupBy { it.getInt("Se_tra") }
                .forEach { (seTra, soCTs) ->
                    val soChon = soCTs.joinToString(separator = " ") {
                        it.getString("So_chon")
                    }
                    val duLieu = soChon + "x" + seTra + "n "
                    val jsonTra = JSONObject().apply {
                        put("du_lieu", duLieu)
                        put("the_loai", "bc")
                        put("dan_so", soChon)
                        put("so_tien", seTra)
                        put("so_luong", soCTs.size)
                    }
                    jsonTralai.put(
                        (jsonTralai.length() + 1).toString(),
                        jsonTra.toString()
                    )
                    noiDung += duLieu
                }
            return "Cang: $noiDung"
        } catch (e: JSONException) {
        }

        return noiDung
    }
}