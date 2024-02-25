package tamhoang.bvn.ui.baocao

import android.annotation.SuppressLint
import android.content.*
import android.database.SQLException
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import tamhoang.bvn.data.services.guiTinNhan.GuiTinNhanService
import tamhoang.bvn.data.services.SoctService
import tamhoang.bvn.data.services.TinhTienService
import tamhoang.bvn.data.setting.SettingKH
import tamhoang.bvn.data.setting.TLGiu
import tamhoang.bvn.data.store.KhachHangStore
import tamhoang.bvn.data.store.KqStore
import tamhoang.bvn.data.store.TinNhanStore
import tamhoang.bvn.databinding.FragBaoCaoCuBinding
import tamhoang.bvn.databinding.FragBaoCaoCuLvBinding
import tamhoang.bvn.databinding.FragNoMenuBinding
import tamhoang.bvn.messageCenter.telegram.TelegramHandle
import tamhoang.bvn.ui.coSoDuLieu.FragDatabase
import tamhoang.bvn.ui.main.MainState
import tamhoang.bvn.util.extensions.component6
import tamhoang.bvn.util.extensions.component7
import tamhoang.bvn.util.extensions.toDecimal
import tamhoang.bvn.util.ld.CongThuc.checkTime
import tamhoang.bvn.util.ld.CongThuc.isNumeric

class FragBaoCaoCu : Fragment() {
    private var _bind: FragBaoCaoCuBinding? = null
    private val bind get() = _bind!!

    lateinit var settingKH: SettingKH

    var currentIndex = 0
    var db: DbOpenHelper? = null
    var handler: Handler? = null
    var json: JSONObject? = null
    var listJsonKH = ArrayList<JSONObject>()
    var position = 0
    var isSuccess = false
    private val listSdt = ArrayList<String>()
    private val listTenKH = ArrayList<String>()
    private val listTypeKH = ArrayList<String>()
    private val runnable: Runnable = object : Runnable {
        override fun run() {
            if (TelegramHandle.sms) {
                lvBaoCao()
                TelegramHandle.sms = false
            }
            handler!!.postDelayed(this, 1000L)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _bind = FragBaoCaoCuBinding.inflate(inflater, container, false)
        db = DbOpenHelper(activity!!)
        bind.noRp2Lv.onItemLongClickListener =
            OnItemLongClickListener { _, _, i: Int, _ ->
                position = i
                showDialog(listTenKH[i])
                false
            }
        if (!checkTime("18:30")) {
            val handler = Handler()
            this.handler = handler
            handler.postDelayed(runnable, 1000L)
        }
        lvBaoCao()
        bind.btnNt.setOnClickListener(AnonymousClass3())
        return bind.root
    }

    internal inner class AnonymousClass3 : View.OnClickListener {
        override fun onClick(view: View) {
            if (listJsonKH.size >= 1) {
                val builder = AlertDialog.Builder(
                    activity!!
                )
                builder.setMessage("Bạn có muốn nhắn tin chốt tiền tất cả không?").setCancelable(false)
                    .setPositiveButton("Yes") { dialog, _ ->
                        dialog.cancel()
                        bind.progressBar.visibility = View.VISIBLE
                        activity!!.window.setFlags(16, 16)
                        val khachChot = KhachHangStore.I.selectByName(listTenKH[position]) ?: return@setPositiveButton

                        for (i in listJsonKH.indices) {
                            try {
                                currentIndex = i
                                val ndung = ChotTienService.I.chot(listTenKH[position])

                                GuiTinNhanService.I.sendMessage(context,
                                    useApp = khachChot.use_app, tenKH = khachChot.sdt, sdt = listSdt[currentIndex],
                                    message = ndung, actionDone = { isSuccess = it }
                                )
                            } catch (e: JSONException) {
                                isSuccess = false
                                e.printStackTrace()
                            }
                        }

                        Handler(Looper.getMainLooper()).postDelayed({
                            if (isSuccess) {
                                Toast.makeText(activity, "Đã nhắn chốt tiền!", Toast.LENGTH_LONG).show()
                            }
                            bind.progressBar.visibility = View.GONE
                            activity!!.window.clearFlags(16)
                        }, 2000L)
                    }.setNegativeButton("No") { dialog, _ ->
                        dialog.cancel()
                    }
                builder.create().show()
            }
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
                    "Tin chốt:",
                    ChotTienService.I.chotChiTiet(listTenKH[position])
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
            handler!!.removeCallbacks(runnable)
            db!!.close()
        } catch (ignored: Exception) {
        }
        super.onDestroy()
    }

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
            Pair(TYPE.Double, "sum((type_kh = 1)*ket_qua*(100-diem_khachgiu)/100)"),
            Pair(TYPE.Double, "sum((type_kh = 2)*(100-diem_khachgiu)*diem/100)"),
            Pair(
                TYPE.Double, "CASE WHEN the_loai = 'xi' OR the_loai = 'xia' " +
                        "THEN sum((type_kh = 2)*(100-diem_khachgiu)*diem/100*so_nhay*lan_an/1000) " +
                        "ELSE sum((type_kh = 2)*(100-diem_khachgiu)*diem/100*so_nhay)  END nAn"
            ),
            Pair(TYPE.Double, "sum((type_kh = 2)*ket_qua*(100-diem_khachgiu)/100)")
        )
        try {
            val jSONObject = JSONObject()
            var tongTien = 0.0
            data.forEach {
                val (theLoai, diem1, an1, ketQua1, diem2, an2, ketQua2) = it
                val jsonChild = JSONObject().apply {
                    put("DiemNhan", ((diem1 as Double) - (diem2 as Double)).toDecimal())
                    put("AnNhan", ((an1 as Double) - (an2 as Double)).toDecimal())
                    put("KQNhan", (-(ketQua1 as Double) - (ketQua2 as Double)).toDecimal())
                }
                tongTien -= (ketQua1 as Double) + (ketQua2 as Double)
                jSONObject.put(theLoai as String, jsonChild.toString())
            }
            bind.apply {
                checkView(jSONObject, "dea", tvDiemDea, tvAnDea, tvKQDea, lnDea)
                checkView(jSONObject, "deb", tvDiemDeb, tvAnDeb, tvKQDeb)
                checkView(jSONObject, "dec", tvDiemDec, tvAnDec, tvKQDec, lnDec)
                checkView(jSONObject, "ded", tvDiemDed, tvAnDed, tvKQDed, lnDed)
                checkView(jSONObject, "det", tvDiemDet, tvAnDet, tvKQDet, lnDet)
                checkView(jSONObject, "lo", tvDiemLo1, tvAnLo1, tvKQLo1)
                checkView(jSONObject, "loa", tvDiemLoa1, tvAnLoa1, tvKQLoa1, lnLoa)
                checkView(jSONObject, "xn", tvDiemXn1, tvAnXn1, tvKQXn1, lnXn)
                checkView(jSONObject, "xi", tvDiemXi2, tvAnXi2, tvKQXi2, lnXi2)
                checkView(jSONObject, "xia", tvDiemXia2, tvAnXia2, tvKQXia2, lnXia2)
                checkView(jSONObject, "bc", tvDiemBc1, tvAnBc1, tvKQBc1)
                checkView(jSONObject, "bca", tvDiemBca, tvAnBca, tvKQBca, lnBca)
            }
            bind.tvTongTien1.text = tongTien.toDecimal()
        } catch (ignored: SQLException) {
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        xemListview()
    }

    private fun xemListview() {
        listJsonKH.clear()
        listTenKH.clear()
        listSdt.clear()
        listTypeKH.clear()
        val data = db!!.getFullData(
            SoctS.TABLE,
            "ngay_nhan = '${MainState.dateYMD}' AND the_loai <> 'tt' GROUP by type_kh, ten_kh, the_loai ORDER by type_kh DESC, ten_kh",
            Pair(TYPE.String, "ten_kh"),
            Pair(TYPE.String, "so_dienthoai"),
            Pair(TYPE.String, "type_kh"),
            Pair(TYPE.String, "the_loai"),
            Pair(TYPE.Double, "sum((100-diem_khachgiu)*diem/100)"),
            Pair(
                TYPE.Double, "CASE WHEN the_loai = 'xi' OR the_loai = 'xia' " +
                        "THEN sum((100-diem_khachgiu)*diem/100*so_nhay*lan_an/1000) " +
                        "ELSE sum((100-diem_khachgiu)*diem/100*so_nhay)  END nAn"
            ),
            Pair(TYPE.Double, "sum(ket_qua*(100-diem_khachgiu)/100)")
        )

        try {
            data.groupBy {
                val (tenKh, _, typeKH, _, _, _, _) = it
                "$tenKh$typeKH"
            }.forEach { mapData ->
                val (tenKh, sdt, typeKH, _, _, _, _) = mapData.value[0]
                listTenKH.add(tenKh as String)
                listSdt.add(sdt as String)
                listTypeKH.add(typeKH as String)
                val jsonKH = JSONObject().apply {
                    put("Ten_KH", tenKh)
                    put("SDT", sdt)
                    put("Type_KH", typeKH)
                    put("Tien_Nhan", mapData.value.sumByDouble { it[6] as Double }.toDecimal())
                }
                mapData.value.forEach {
                    val jsonChild = JSONObject().apply {
                        put("DiemNhan", (it[4] as Double).toDecimal())
                        put("AnNhan", (it[5] as Double).toDecimal())
                        put("KQNhan", (it[6] as Double).toDecimal())
                    }
                    jsonKH.put(it[3] as String, jsonChild.toString())
                }
                listJsonKH.add(jsonKH)
            }
        } catch (_: SQLException) {
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        if (activity != null) {
            bind.noRp2Lv.adapter = NoRPTNAdapter(activity, R.layout.frag_bao_cao_cu_lv, listJsonKH)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun checkView(
        json: JSONObject,
        theloai: String,
        tvDiem: TextView,
        tvAn: TextView,
        tvKQ: TextView,
        layout: View? = null
    ) {
        if (!json.has(theloai)) return
        layout?.visibility = View.VISIBLE
        val jsonChild = JSONObject(json.getString(theloai))
        tvDiem.text = jsonChild.getString("DiemNhan")
        tvKQ.text = jsonChild.getString("KQNhan")
        tvAn.text = jsonChild.getString("AnNhan")
    }

    inner class NoRPTNAdapter(context: Context?, resource: Int, objects: List<JSONObject?>?) : ArrayAdapter<Any?>(
        context!!, resource, objects!!
    ) {
        @SuppressLint("SetTextI18n", "ViewHolder")
        override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {
            val bindLv = FragBaoCaoCuLvBinding.inflate(layoutInflater)
            bindLv.tvKhachHang.text = listTenKH[i]
            val colorTvKh = if (listTypeKH[i] == "2") Color.BLUE else Color.BLACK
            bindLv.tvKhachHang.setTextColor(colorTvKh)
            val jSONObject = listJsonKH[i]
            return try {
                bindLv.apply {
                    checkView(jSONObject, "dea", tvDiemDea, tvAnDea, tvKQDea, lnDea)
                    checkView(jSONObject, "deb", tvDiemDeb, tvAnDeb, tvKQDeb)
                    checkView(jSONObject, "dec", tvDiemDec, tvAnDec, tvKQDec, lnDec)
                    checkView(jSONObject, "ded", tvDiemDed, tvAnDed, tvKQDed, lnDed)
                    checkView(jSONObject, "det", tvDiemDet, tvAnDet, tvKQDet, lnDet)

                    checkView(jSONObject, "lo", tvDiemLo1, tvAnLo1, tvKQLo1)
                    checkView(jSONObject, "loa", tvDiemLoa1, tvAnLoa1, tvKQLoa1, lnLoa)
                    checkView(jSONObject, "xi", tvDiemXi2, tvAnXi2, tvKQXi2, lnXi2)
                    checkView(jSONObject, "xia", tvDiemXia2, tvAnXia2, tvKQXia2, lnXia2)
                    checkView(jSONObject, "xn", tvDiemXn1, tvAnXn1, tvKQXn1, lnXn)
                    checkView(jSONObject, "bc", tvDiemBc1, tvAnBc1, tvKQBc1)
                    checkView(jSONObject, "bca", tvDiemBca, tvAnBca, tvKQBca, lnBca)
                }
                bindLv.tvTongTien1.text = jSONObject.getString("Tien_Nhan")
                bindLv.root
            } catch (e: JSONException) {
                e.printStackTrace()
                bindLv.root
            }

        }
    }
}