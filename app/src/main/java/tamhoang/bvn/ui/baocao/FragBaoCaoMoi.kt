package tamhoang.bvn.ui.baocao

import android.annotation.SuppressLint
import android.content.*
import android.database.SQLException
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import org.json.JSONException
import org.json.JSONObject
import tamhoang.bvn.R
import tamhoang.bvn.data.DbOpenHelper
import tamhoang.bvn.data.enum.TYPE
import tamhoang.bvn.data.model.SoctS
import tamhoang.bvn.data.services.ChotTienService
import tamhoang.bvn.data.services.SoctService
import tamhoang.bvn.data.services.TinhTienService
import tamhoang.bvn.data.services.guiTinNhan.GuiTinNhanService
import tamhoang.bvn.data.setting.SettingKH
import tamhoang.bvn.data.setting.TLGiu
import tamhoang.bvn.data.store.KhachHangStore
import tamhoang.bvn.data.store.KqStore
import tamhoang.bvn.data.store.TinNhanStore
import tamhoang.bvn.databinding.FragBaoCaoMoiBinding
import tamhoang.bvn.databinding.FragBaoCaoMoiLvBinding
import tamhoang.bvn.databinding.FragNoMenuBinding
import tamhoang.bvn.messageCenter.telegram.TelegramHandle
import tamhoang.bvn.ui.coSoDuLieu.FragDatabase
import tamhoang.bvn.ui.main.MainState
import tamhoang.bvn.util.extensions.component6
import tamhoang.bvn.util.extensions.component7
import tamhoang.bvn.util.extensions.toDecimal
import tamhoang.bvn.util.ld.CongThuc.checkTime
import tamhoang.bvn.util.ld.CongThuc.isNumeric

class FragBaoCaoMoi : Fragment() {
    private var _bind: FragBaoCaoMoiBinding? = null
    private val bind get() = _bind!!

    var isRunning = true
    lateinit var settingKH: SettingKH
    var db: DbOpenHelper? = null
    var handler: Handler? = null
    var json: JSONObject? = null
    var listJsonKH = arrayListOf<JSONObject>()
    var listSdt = ArrayList<String>()
    var listTenKH = ArrayList<String>()
    var position = 0
    private val runnable = object : Runnable {
        override fun run() {
            if (TelegramHandle.sms) {
                try {
                    lvBaoCao()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                TelegramHandle.sms = false
            }
            handler!!.postDelayed(this, 1000)
        }
    }

    override fun onCreateView(layoutInflater: LayoutInflater, viewGroup: ViewGroup?, bundle: Bundle?): View {
        _bind = FragBaoCaoMoiBinding.inflate(layoutInflater, viewGroup, false)
        db = DbOpenHelper(activity!!)
        bind.lvBaocaoKhach.onItemLongClickListener = OnItemLongClickListener { _, _, i, _ ->
            position = i
            showDialog(listTenKH[i])
            false
        }
        if (!checkTime("18:30")) {
            handler = Handler()
            handler!!.postDelayed(runnable, 1000)
        }
        try {
            lvBaoCao()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return bind.root
    }

    override fun onStop() {
        isRunning = false
        super.onStop()
        try {
            handler!!.removeCallbacks(runnable)
        } catch (unused: Exception) {
        }
    }

    private fun showDialog(tenKh: String?) {
        val dialog = android.app.Dialog(activity!!)
        val bindDialog = FragNoMenuBinding.inflate(layoutInflater)
        dialog.setContentView(bindDialog.root)
        @SuppressLint("WrongConstant") val clipboardManager =
            activity!!.getSystemService("clipboard") as ClipboardManager
        val khachHang = KhachHangStore.I.selectByName(tenKh ?: "") ?: return
        try {
            json = JSONObject(khachHang.tbl_MB)
            val caidatTg = json!!.getJSONObject("caidat_tg")
            settingKH = SettingKH(caidatTg)
        } catch (_: Exception) {
        }
        bindDialog.switchX.setOnCheckedChangeListener { _, isChecked: Boolean ->
            bindDialog.ln2.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
        bindDialog.Baocaochitiet.setOnClickListener {
            val intent = Intent(activity, ActivityKhach::class.java)
            intent.putExtra("tenKH", tenKh)
            startActivity(intent)
            dialog.cancel()
        }
        bindDialog.tinhlaitien.setOnClickListener {
            try {
                tinhLaiTienKhachNay(MainState.dateYMD)
            } catch (_: Throwable) {
            }
            dialog.cancel()
        }
        bindDialog.nhanchottien.setOnClickListener {
            val khachChot = KhachHangStore.I.selectByName(listTenKH[position]) ?: return@setOnClickListener

            val ndung = ChotTienService.I.chot(listTenKH[position])

            try {
                GuiTinNhanService.I.sendMessage(context,
                    useApp = khachChot.use_app, tenKH = khachChot.sdt, sdt = listSdt[position],
                    message = ndung, actionDone = { isSuccess ->
                        if (isSuccess)
                            Toast.makeText(activity, "Đã nhắn chốt tiền!", Toast.LENGTH_LONG).show()
                    }
                )
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            dialog.cancel()
        }
        bindDialog.copytinchitiet.setOnClickListener {
            clipboardManager.setPrimaryClip(
                ClipData.newPlainText(
                    "Tin chốt:", ChotTienService.I.chotChiTiet(listTenKH[position])
                )
            )
            Toast.makeText(activity, "Đã copy vào bộ nhớ tạm!", Toast.LENGTH_LONG).show()
            dialog.cancel()
        }
        bindDialog.copytinchotien.setOnClickListener {
            try {
                val ndung = ChotTienService.I.chot(listTenKH[position])

                val clipData = ClipData.newPlainText("Tin chốt:", ndung)
                clipboardManager.setPrimaryClip(clipData)

                Toast.makeText(activity, "Đã copy vào bộ nhớ tạm! ${settingKH.chotSoDu.value()}", Toast.LENGTH_LONG)
                    .show()
            } catch (_: JSONException) {
            }
            dialog.cancel()
        }
        bindDialog.xoadulieu.setOnClickListener {
            AlertDialog.Builder(activity!!).apply {
                setTitle("Xoá dữ liệu của KH này?")
                setPositiveButton("YES") { _, _ ->
                    db!!.queryData("DELETE FROM tbl_soctS WHERE ngay_nhan = '${MainState.dateYMD}' AND ten_kh = '${listTenKH[position]}'")
                    db!!.queryData("DELETE FROM tbl_tinnhanS WHERE ngay_nhan = '${MainState.dateYMD}' AND ten_kh = '${listTenKH[position]}'")
                    try {
                        lvBaoCao()
                    } catch (_: JSONException) {
                    }
                    dialog.cancel()
                    Toast.makeText(activity, "Đã xoá", Toast.LENGTH_LONG).show()
                }
                setNegativeButton("No") { dialogInterface: DialogInterface, i1: Int -> dialogInterface.cancel() }
                create().show()
            }
        }
        bindDialog.apply {
            createSeekbar(seekGiuDekhach, ptGiuDeKhach, tenKh!!, TLGiu.De, isDaiLy = false)
            createSeekbar(seekGiuLokhach, ptGiuLoKhach, tenKh, TLGiu.Lo, isDaiLy = false)
            createSeekbar(seekGiuXikhach, ptGiuXiKhach, tenKh, TLGiu.Xi, isDaiLy = false)
            createSeekbar(seekGiu3ckhach, ptGiuBcKhach, tenKh, TLGiu.Bc, isDaiLy = false)

            createSeekbar(seekGiuDedly, ptGiuDeDly, tenKh, TLGiu.De, isDaiLy = true)
            createSeekbar(seekGiuLodly, ptGiuLoDly, tenKh, TLGiu.Lo, isDaiLy = true)
            createSeekbar(seekGiuXidly, ptGiuXiDly, tenKh, TLGiu.Xi, isDaiLy = true)
            createSeekbar(seekGiu3cdly, ptGiuBcDly, tenKh, TLGiu.Bc, isDaiLy = true)
        }
        dialog.window!!.setLayout(-1, -2)
        dialog.setCancelable(true)
        dialog.setTitle("Xem dạng:")
        dialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun createSeekbar(seekBar: SeekBar, textView: TextView, tenKH: String, tlGiu: TLGiu, isDaiLy: Boolean) {
        val value = if (isDaiLy) settingKH.daiLyGiu[tlGiu.toString()] else settingKH.khachGiu[tlGiu.toString()]
        seekBar.progress = (value ?: 0) / 5
        textView.text = "${value ?: 0}%"

        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            var max = 0
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, i: Int, z: Boolean) {
                textView.text = "${i * 5}%"
                max = i * 5
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                try {
                    if (isDaiLy)
                        settingKH.putDaiLyGiu(tlGiu, max)
                    else
                        settingKH.putKhachGiu(tlGiu, max)
                    db!!.queryData("Update tbl_kh_new set tbl_MB = '$json' WHERE ten_kh = '$tenKH'")
                    tinhLaiTienKhachNay(MainState.dateYMD)
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        })
    }

    @Throws(Throwable::class)
    fun tinhLaiTienKhachNay(ngayNhan: String) {
        db!!.queryData("Delete From tbl_soctS WHERE  ngay_nhan = '${ngayNhan}' AND ten_kh = '${listTenKH[position]}'")

        val listKh =
            TinNhanStore.I.selectListWhere(ngayNhan, "phat_hien_loi = 'ok' AND ten_kh = '${listTenKH[position]}'")

        listKh.forEach {
            val ndPhantich = it.ndPhanTich.replace("*", "")
            db!!.queryData("Update tbl_tinnhanS set nd_phantich = '$ndPhantich' WHERE id = ${it.ID ?: 0}")
            SoctService.I.nhapSoChiTiet(it.ID ?: 0)
        }
        tinhtien()
        lvBaoCao()
    }

    private fun tinhtien() {
        val listGiai = KqStore.I.getKq(MainState.dateYMD)
        if (listGiai.isEmpty()) {
            AlertDialog.Builder(activity!!).apply {
                setTitle("Không tìm thấy dữ liệu kết quả ngày ${MainState.dateYMD}")
                setMessage("Đi đến trang cơ sở dữ liệu để tính lại?")
                setNegativeButton("Có") { _, _ ->
                    activity!!.supportFragmentManager.beginTransaction().replace(R.id.main_content, FragDatabase())
                        .commit()
                }
                setPositiveButton("Không") { dialog, _ -> dialog.dismiss() }
                show()
            }
            return
        }
        if (listGiai.all { isNumeric(it) }) {
            TinhTienService.I.run(MainState.dateYMD)
        }
    }

    override fun onDestroy() {
        try {
            listTenKH.clear()
            listSdt.clear()
            bind.lvBaocaoKhach.adapter = null
        } catch (unused: Exception) {
        }
        super.onDestroy()
    }

    @SuppressLint("SetTextI18n")
    @Throws(JSONException::class)
    fun lvBaoCao() {
        val data = db!!.getFullData(
            SoctS.TABLE,
            "ngay_nhan = '${MainState.dateYMD}' AND the_loai <> 'tt' GROUP by the_loai",
            Pair(TYPE.String, "the_loai"),
            Pair(TYPE.Double, "sum((type_kh = 1)*(100-diem_khachgiu)*diem/100)"),
            Pair(
                TYPE.Double, "CASE WHEN the_loai = 'xi' OR the_loai = 'xia' " +
                        "THEN sum((type_kh = 1)*(100-diem_khachgiu)*diem/100*so_nhay*lan_an/1000) " +
                        "ELSE sum((type_kh = 1)*(100-diem_khachgiu)*diem/100*so_nhay)  END nAn"
            ),
            Pair(TYPE.Double, "sum((type_kh = 1)*ket_qua*(100-diem_khachgiu)/100/1000)"),
            Pair(TYPE.Double, "sum((type_kh = 2)*(100-diem_khachgiu)*diem/100)"),
            Pair(
                TYPE.Double, "CASE WHEN the_loai = 'xi' OR the_loai = 'xia' " +
                        "THEN sum((type_kh = 2)*(100-diem_khachgiu)*diem/100*so_nhay*lan_an/1000) " +
                        "ELSE sum((type_kh = 2)*(100-diem_khachgiu)*diem/100*so_nhay)  END nAn"
            ),
            Pair(TYPE.Double, "sum((type_kh = 2)*ket_qua*(100-diem_khachgiu)/100/1000)")
        )
        val jSONObject = JSONObject()
        var tongNhan = 0.0
        var tongChuyen = 0.0
        data.forEach {
            try {
                val (theLoai, diem1, an1, ketQua1, diem2, an2, ketQua2) = it
                val jsonChild = JSONObject().apply {
                    put("DiemNhan", (diem1 as Double).toDecimal())
                    put("AnNhan", (an1 as Double).toDecimal())
                    put("KQNhan", (ketQua1 as Double).toDecimal())
                    put("DiemChuyen", (diem2 as Double).toDecimal())
                    put("AnChuyen", (an2 as Double).toDecimal())
                    put("KQChuyen", (ketQua2 as Double).toDecimal())
                }
                tongNhan += ketQua1 as Double
                tongChuyen += ketQua2 as Double
                jSONObject.put(theLoai as String, jsonChild.toString())
            } catch (ignored: JSONException) {
            }
        }
        if (jSONObject.length() > 0) {

            bind.apply {
                checkView(jSONObject, "dea", deaNhan, deaNhanAn, deaChuyen, deaChuyenAn)
                checkView(jSONObject, "deb", debNhan, debNhanAn, debChuyen, debChuyenAn)
                checkView(jSONObject, "det", detNhan, detNhanAn, detChuyen, detChuyenAn, layout = liDet)
                checkView(jSONObject, "dec", decNhan, decNhanAn, decChuyen, decChuyenAn, layout = liDec)
                checkView(jSONObject, "ded", dedNhan, dedNhanAn, dedChuyen, dedChuyenAn, layout = liDed)
                checkView(jSONObject, "lo", loNhan, loNhanAn, loChuyen, loChuyenAn)
                checkView(jSONObject, "loa", loaNhan, loaNhanAn, loaChuyen, loaChuyenAn)
                checkView(jSONObject, "xi", xi2Nhan, xi2NhanAn, xi2Chuyen, xi2ChuyenAn)
                checkView(jSONObject, "xia", xia2Nhan, xia2NhanAn, xia2Chuyen, xia2ChuyenAn)
                checkView(jSONObject, "bc", bcNhan, bcNhanAn, bcChuyen, bcChuyenAn)
                checkView(jSONObject, "bca", bcaNhan, bcaNhanAn, bcaChuyen, bcaChuyenAn)

                tvTongTienNhan.text = tongNhan.toDecimal()
                tvTongTienChuyen.text = tongChuyen.toDecimal()
                tvTongGiu.text = (-tongNhan - tongChuyen).toDecimal()
            }
        }
        xemListview()
    }

    private fun xemListview() {
        listJsonKH.clear()
        listTenKH.clear()
        listSdt.clear()
        try {
            val data = db!!.getFullData(
                SoctS.TABLE,
                "ngay_nhan = '${MainState.dateYMD}' AND the_loai <> 'tt' GROUP by ten_kh, the_loai",
                Pair(TYPE.String, "ten_kh"),
                Pair(TYPE.String, "so_dienthoai"),
                Pair(TYPE.String, "the_loai"),
                Pair(TYPE.Double, "sum((type_kh = 1)*(100-diem_khachgiu)*diem/100)"),
                Pair(
                    TYPE.Double, "CASE WHEN the_loai = 'xi' OR the_loai = 'xia' " +
                            "THEN sum((type_kh = 1)*(100-diem_khachgiu)*diem/100*so_nhay*lan_an/1000) " +
                            "ELSE sum((type_kh = 1)*(100-diem_khachgiu)*diem/100*so_nhay)  END nAn"
                ),
                Pair(TYPE.Double, "sum((type_kh = 1)*ket_qua*(100-diem_khachgiu)/100/1000)"),
                Pair(TYPE.Double, "sum((type_kh = 2)*(100-diem_khachgiu)*diem/100)"),
                Pair(
                    TYPE.Double, "CASE WHEN the_loai = 'xi' OR the_loai = 'xia' " +
                            "THEN sum((type_kh = 2)*(100-diem_khachgiu)*diem/100*so_nhay*lan_an/1000) " +
                            "ELSE sum((type_kh = 2)*(100-diem_khachgiu)*diem/100*so_nhay)  END nAn"
                ),
                Pair(TYPE.Double, "sum((type_kh = 2)* ket_qua/1000)")
            )
            data.groupBy { it[0] }
                .forEach { mapData ->
                    val (tenKh, sdt) = mapData.value[0]
                    listTenKH.add(tenKh as String)
                    listSdt.add(sdt as String)
                    val tienNhan = mapData.value.sumByDouble { it[5] as Double }
                    val tienChuyen = mapData.value.sumByDouble { it[8] as Double }
                    val jsonKH = JSONObject().apply {
                        put("Tien_Nhan", tienNhan.toDecimal())
                        put("Tien_Chuyen", tienChuyen.toDecimal())
                        put("Tong_Tien", (tienNhan + tienChuyen).toDecimal())
                    }
                    mapData.value.forEach {
                        val jsonChild = JSONObject().apply {
                            put("DiemNhan", (it[3] as Double).toDecimal())
                            put("AnNhan", (it[4] as Double).toDecimal())
                            put("KQNhan", (it[5] as Double).toDecimal())
                            put("DiemChuyen", (it[6] as Double).toDecimal())
                            put("AnChuyen", (it[7] as Double).toDecimal())
                            put("KQChuyen", (it[8] as Double).toDecimal())
                        }
                        jsonKH.put(it[2] as String, jsonChild.toString())
                    }
                    listJsonKH.add(jsonKH)
                }
        } catch (ignored: SQLException) {
        } catch (e: JSONException) {
        }
        if (activity != null) {
            bind.lvBaocaoKhach.adapter = NoRPTNAdapter(activity, R.layout.frag_bao_cao_moi_lv, listJsonKH)
        }
    }

    internal inner class NoRPTNAdapter(context: Context?, i: Int, list: List<JSONObject?>?) : ArrayAdapter<Any?>(
        context!!, i, list!!
    ) {
        @SuppressLint("SetTextI18n", "ViewHolder")
        override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {
            val bindLv = FragBaoCaoMoiLvBinding.inflate(layoutInflater)

            val jSONObject = listJsonKH[i]
            try {
                bindLv.apply {
                    tvTenKH.text = listTenKH[i]
                    tvTongTienNhan.text = jSONObject.getString("Tien_Nhan")
                    tvTongTienChuyen.text = jSONObject.getString("Tien_Chuyen")
                    tvTongTien.text = jSONObject.getString("Tong_Tien")
                    checkView(jSONObject, "dea", deaNhan, deaNhanAn, deaChuyen, deaChuyenAn)
                    checkView(jSONObject, "deb", debNhan, debNhanAn, debChuyen, debChuyenAn)
                    checkView(jSONObject, "det", detNhan, detNhanAn, detChuyen, detChuyenAn, layout = liDet)
                    checkView(jSONObject, "dec", decNhan, decNhanAn, decChuyen, decChuyenAn, layout = liDec)
                    checkView(jSONObject, "ded", dedNhan, dedNhanAn, dedChuyen, dedChuyenAn, layout = liDed)
                    checkView(jSONObject, "lo", loNhan, loNhanAn, loChuyen, loChuyenAn)
                    checkView(jSONObject, "loa", loaNhan, loaNhanAn, loaChuyen, loaChuyenAn)
                    checkView(jSONObject, "xi", xi2Nhan, xi2NhanAn, xi2Chuyen, xi2ChuyenAn)
                    checkView(jSONObject, "xia", xia2Nhan, xia2NhanAn, xia2Chuyen, xia2ChuyenAn)
                    checkView(jSONObject, "bc", bcNhan, bcNhanAn, bcChuyen, bcChuyenAn)
                    checkView(jSONObject, "bca", bcaNhan, bcaNhanAn, bcaChuyen, bcaChuyenAn)
                }
            } catch (_: Exception) {
            }

            return bindLv.root
        }
    }

    @SuppressLint("SetTextI18n")
    private fun checkView(
        json: JSONObject,
        theloai: String,
        tvNhan: TextView,
        tvNhanAn: TextView,
        tvChuyen: TextView,
        tvChuyenAn: TextView,
        layout: View? = null
    ) {
        if (!json.has(theloai)) return
        layout?.visibility = View.VISIBLE
        val jsonChild = JSONObject(json.getString(theloai))
        if (jsonChild.getString("DiemNhan").isNotEmpty()) {
            tvNhan.text = jsonChild.getString("DiemNhan") + "(" + jsonChild.getString("AnNhan") + ")"
            tvNhanAn.text = jsonChild.getString("KQNhan")
        }
        if (jsonChild.getString("DiemChuyen").isNotEmpty()) {
            tvChuyen.text = jsonChild.getString("DiemChuyen") + "(" + jsonChild.getString("AnChuyen") + ")"
            tvChuyenAn.text = jsonChild.getString("KQChuyen")
        }
    }
}