package tamhoang.bvn.ui.main.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.database.SQLException
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.internal.view.SupportMenu
import androidx.fragment.app.Fragment
import androidx.vectordrawable.graphics.drawable.PathInterpolatorCompat
import com.github.florent37.viewtooltip.ViewTooltip
import org.json.JSONException
import org.json.JSONObject
import tamhoang.bvn.R
import tamhoang.bvn.data.BaseStore.getMaxSoTinNhan
import tamhoang.bvn.data.DbOpenHelper
import tamhoang.bvn.data.setting.Setting
import tamhoang.bvn.data.store.KhachHangStore
import tamhoang.bvn.data.store.SoctStore
import tamhoang.bvn.databinding.FragCanchuyenBinding
import tamhoang.bvn.databinding.FragCanchuyenDialogBinding
import tamhoang.bvn.databinding.FragCanchuyenLvBinding
import tamhoang.bvn.messageCenter.telegram.TelegramHandle
import tamhoang.bvn.ui.main.MainState
import tamhoang.bvn.util.extensions.clear
import tamhoang.bvn.util.extensions.contains
import tamhoang.bvn.util.extensions.toArrayList
import tamhoang.bvn.util.extensions.toDecimal
import tamhoang.bvn.util.ld.CongThuc.isNumeric
import tamhoang.bvn.util.view.RangeSeekBar

class FragCanChuyen : Fragment() {
    private var _bind: FragCanchuyenBinding? = null
    private val bind get() = _bind!!

    lateinit var controller: FragCanChuyenController

    var mDachuyen = false
    var mDangXuat: String? = null
    var db: DbOpenHelper? = null
    var handler: Handler? = null
    var jsonKhongmax: JSONObject? = null
    var layX2: String? = null
    var layX3: String? = null
    var layX4: String? = null
    var layout: LinearLayout? = null
    var mAppuse = ArrayList<String>()
    var mContact = ArrayList<String>()
    var mKhongMax = ArrayList<String>()
    var mMobile = ArrayList<String>()
    var mNhay = ArrayList<Int>()
    var mSo = ArrayList<String>()
    var mSpiner = 0
    var mTienNhan = ArrayList<String>()
    var mTienOm = ArrayList<String>()
    var mTienTon = ArrayList<String>()
    var mTienchuyen = ArrayList<String>()
    var max = 100
    var min = 0
    var rangeSeekBar: RangeSeekBar<Int>? = null
    var tooltipView: ViewTooltip? = null
    private val runnable: Runnable = object : Runnable {
        override fun run() {
            if (TelegramHandle.sms) {
                xemlv()
                TelegramHandle.sms = false
            }
            handler!!.postDelayed(this, 1000)
        }
    }
    var sapxep: String? = null
    var xuatDan: String? = null

    private val textTien: String
        get() = bind.edtTien.text.toString()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _bind = FragCanchuyenBinding.inflate(inflater, container, false)
        db = DbOpenHelper(activity!!)
        controller = FragCanChuyenController(db!!)

        bind.lnXi.visibility = View.GONE
        bind.liLoaide.visibility = View.GONE
        handler = Handler()
        handler!!.postDelayed(runnable, 1000)
        rangeSeekBar = RangeSeekBar(activity)
        rangeSeekBar!!.setRangeValues(0, 100)
        rangeSeekBar!!.selectedMinValue = 0
        rangeSeekBar!!.selectedMaxValue = 100
        bind.seekbar.addView(rangeSeekBar)
        rangeSeekBar!!.setOnRangeSeekBarChangeListener { _, minValue: Int, maxValue: Int ->
            min = minValue
            max = maxValue
        }
        bind.radioDe.setOnCheckedChangeListener { _, isChecked: Boolean ->
            if (isChecked) {
                mDangXuat = "(the_loai = 'deb' or the_loai = 'det')"
                bind.seekbar.visibility = View.VISIBLE
                val mDate = MainState.dateYMD
                try {
                    val cursor =
                        db!!.getData("Select sum((the_loai = 'dea')* diem) as de_a\n,sum((the_loai = 'deb')* diem) as de_b\n,sum((the_loai = 'dec')* diem) as de_c\n,sum((the_loai = 'ded')* diem) as de_d\nFrom tbl_soctS \nWhere ngay_nhan = '$mDate'")
                    if (!cursor.moveToFirst()) {
                        return@setOnCheckedChangeListener
                    }
                    val hasDea = cursor.getDouble(0) > 0.0
                    val hasDeb = cursor.getDouble(1) > 0.0
                    val hasDec = cursor.getDouble(2) > 0.0
                    val hasDed = cursor.getDouble(3) > 0.0

                    bind.radioDea.isEnabled = hasDea
                    bind.radioDeb.isEnabled = hasDeb
                    bind.radioDec.isEnabled = hasDec
                    bind.radioDed.isEnabled = hasDed

                    bind.lnXi.visibility = View.GONE
                    bind.radioDeb.isChecked = true

                    bind.liLoaide.visibility = if (hasDea || hasDec || hasDed)
                        View.VISIBLE
                    else
                        View.GONE
                    xemRecycview()
                    if (!cursor.isClosed && !cursor.isClosed) {
                        cursor.close()
                    }
                } catch (e: SQLException) {
                }
            }
        }
        bind.radioDea.setOnCheckedChangeListener { _, isChecked: Boolean ->
            if (isChecked) {
                mDangXuat = "the_loai = 'dea'"
                bind.lnXi.visibility = View.GONE
                xemRecycview()
            }
        }
        bind.radioDeb.setOnCheckedChangeListener { _, isChecked: Boolean ->
            if (isChecked) {
                mDangXuat = "(the_loai = 'deb' or the_loai = 'det')"
                bind.lnXi.visibility = View.GONE
                xemRecycview()
            }
        }
        bind.radioDec.setOnCheckedChangeListener { _, isChecked: Boolean ->
            if (isChecked) {
                mDangXuat = "the_loai = 'dec'"
                bind.lnXi.visibility = View.GONE
                xemRecycview()
            }
        }
        bind.radioDed.setOnCheckedChangeListener { _, isChecked: Boolean ->
            if (isChecked) {
                mDangXuat = "the_loai = 'ded'"
                bind.lnXi.visibility = View.GONE
                xemRecycview()
            }
        }
        bind.radioLo.setOnCheckedChangeListener { _, isChecked: Boolean ->
            if (isChecked) {
                mDangXuat = "the_loai = 'lo'"
                bind.lnXi.visibility = View.GONE
                bind.liLoaide.visibility = View.GONE
                xemRecycview()
            }
        }
        bind.radioLoa.setOnCheckedChangeListener { _, isChecked: Boolean ->
            if (isChecked) {
                mDangXuat = "the_loai = 'loa'"
                bind.lnXi.visibility = View.GONE
                bind.liLoaide.visibility = View.GONE
                xemRecycview()
            }
        }
        bind.radioXi.setOnCheckedChangeListener { _, isChecked: Boolean ->
            if (isChecked) {
                mDangXuat = "the_loai = 'xi'"
                bind.seekbar.visibility = View.GONE
                bind.lnXi.visibility = View.VISIBLE
                bind.liLoaide.visibility = View.GONE
                try {
                    val count =
                        SoctStore.I.countWhere("the_loai = 'xn' AND ngay_nhan = '${MainState.dateYMD}'", "id")
                    if (count > 0) {
                        bind.checkXn.visibility = View.VISIBLE
                    }
                    xemRecycview()
                } catch (ignored: SQLException) {
                }
            }
        }
        bind.radioBc.setOnCheckedChangeListener { _, isChecked: Boolean ->
            if (isChecked) {
                mDangXuat = "the_loai = 'bc'"
                bind.seekbar.visibility = View.GONE
                bind.lnXi.visibility = View.GONE
                bind.liLoaide.visibility = View.GONE
                xemRecycview()
            }
        }
        bind.checkX2.setOnCheckedChangeListener { _, isChecked: Boolean ->
            if (isChecked) {
                mDangXuat = "the_loai = 'xi'"
                layX2 = "length(so_chon) = 5 "
                bind.checkXn.isChecked = false
            } else {
                layX2 = ""
            }
            xemRecycview()
        }
        bind.checkX3.setOnCheckedChangeListener { _, isChecked: Boolean ->
            if (isChecked) {
                mDangXuat = "the_loai = 'xi'"
                layX3 = "OR length(so_chon) = 8 "
                bind.checkXn.isChecked = false
            } else {
                layX3 = ""
            }
            xemRecycview()
        }
        bind.checkX4.setOnCheckedChangeListener { _, isChecked: Boolean ->
            if (isChecked) {
                mDangXuat = "the_loai = 'xi'"
                layX4 = "OR length(so_chon) = 11 "
                bind.checkXn.isChecked = false
            } else {
                layX4 = ""
            }
            xemRecycview()
        }
        bind.checkXn.setOnClickListener { v: View? ->
            if (bind.checkXn.isChecked) {
                mDangXuat = "the_loai = 'xn'"
                bind.checkX2.isChecked = false
                bind.checkX3.isChecked = false
                bind.checkX4.isChecked = false
                xemRecycview()
            }
        }
        bind.btnXuatso.setOnClickListener {
            if (mSo.size == 0 || mTienTon.all { it == "0" || it.contains("-") }) {
                Toast.makeText(context, "Không có số liệu!", Toast.LENGTH_SHORT).show()
            } else if (isNumeric(textTien.clear(listOf("%", "n", "k", "d", ">", "."))) || textTien.isEmpty()
            ) {
                btnClick()
            } else {
                Toast.makeText(context, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show()
            }
        }
        layX2 = "length(so_chon) = 5 "
        layX3 = "OR length(so_chon) = 8 "
        layX4 = "OR length(so_chon) = 11 "
        bind.lview.onItemClickListener =
            OnItemClickListener { _, view: View?, position: Int, _ ->
                try {
                    val c = db!!.getData(
                        "Select ten_kh, sum(diem_quydoi) From tbl_soctS WHERE so_chon = '" + mSo[position]
                                + "' AND ngay_nhan = '${MainState.dateYMD}' AND type_kh = 1 AND " + mDangXuat + " GROUP BY so_dienthoai"
                    )
                    val tenToDiem = c.toArrayList { it.getString(0) + ": <b>" + it.getString(1) + "</b>" }
                    val ndung = tenToDiem.joinToString("<br>")
                    val lineCount = tenToDiem.count()
                    if (tooltipView != null) {
                        tooltipView!!.close()
                    }
                    tooltipView = ViewTooltip.on(this, view)
                        .autoHide(true, 5000)
                        .corner(30)
                        .color(Color.parseColor("#04bf6b"))
                        .position(ViewTooltip.Position.BOTTOM)
                        .arrowHeight(0)
                        .distanceWithView(-120 - 52 * lineCount)
                        .text(ndung)
                        .textColor(Color.parseColor("#FFFFFF"))
                        .align(ViewTooltip.ALIGN.CENTER)
                        .clickToHide(true)
                    tooltipView!!.show()
                } catch (e: SQLException) {
                    println(e)
                }
            }
        try {
            sapxep = if (Setting.I.baoCaoSo.value() == 0)
                "diem DESC"
            else
                "ton DESC, diem DESC"
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        xemlv()
        return bind.root
    }

    override fun onPause() {
        super.onPause()
        if (tooltipView != null) {
            tooltipView!!.close()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler!!.removeCallbacks(runnable)
    }

    fun xemlv() {
        if (mDangXuat != null) {
            xemRecycview()
        } else {
            bind.radioDe.isChecked = true
        }
    }

    private fun btnClick() {
        xemlv()
        controller.xuatTin(
            activity,
            mDangXuat,
            textTien,
            min,
            max,
            mTienTon,
            mSo
        )?.let {
            xuatDan = it
            showDialogXuatTin()
        }
    }

    fun taoTinCang(ten_kh: String?): String? {
        val mDate = MainState.dateYMD
        val qr =
            "Select so_chon, Sum(diem) FROM tbl_soctS Where ten_kh = '$ten_kh' AND type_kh = 2 AND the_loai = 'bc' AND ngay_nhan = '$mDate' Group by so_chon"
        val cursor = db!!.getData(qr)
        val jSon = JSONObject()
        while (cursor.moveToNext()) {
            try {
                val jsonSoCt = JSONObject()
                jsonSoCt.put("Da_chuyen", cursor.getInt(1))
                jsonSoCt.put("Se_chuyen", 0)
                jSon.put(cursor.getString(0), jsonSoCt)
            } catch (e: JSONException) {
                e.printStackTrace()
            } catch (th: Throwable) {
                throw th
            }
        }
        return xuatDan
    }

    fun taoTinXi(ten_kh: String?): String? {
        val jSon = JSONObject()
        val mDate = MainState.dateYMD
        val cursor =
            db!!.getData("Select so_chon, Sum(diem) FROM tbl_soctS Where ten_kh = '$ten_kh' AND type_kh = 2 AND the_loai = 'xi' AND ngay_nhan = '$mDate' Group by so_chon")
        while (cursor.moveToNext()) {
            try {
                val jsonSoCt = JSONObject()
                jsonSoCt.put("Da_chuyen", cursor.getInt(1))
                jsonSoCt.put("Se_chuyen", 0)
                jSon.put(cursor.getString(0), jsonSoCt)
            } catch (e: JSONException) {
                e.printStackTrace()
            } catch (ignored: Throwable) {
            }
        }
        return xuatDan
    }

    @SuppressLint("RestrictedApi")
    fun showDialogXuatTin() {
        val dialog = android.app.Dialog(activity!!)
        val bindDialog = FragCanchuyenDialogBinding.inflate(layoutInflater)
        fun textXuatDan() = bindDialog.edtXuatDan.text.toString()

        dialog.setContentView(bindDialog.root)
        dialog.window!!.setLayout(-1, -2)
        val chuyendi = xuatDan!!.replace(",x", "x")
        mDachuyen = false

        bindDialog.edtXuatDan.setText(xuatDan!!.replace(",x", "x"))
        try {
            val khachHangs = KhachHangStore.I.selectListWhere("type_kh <> 1 ORDER BY ten_kh")
            mContact.clear()
            mMobile.clear()
            mKhongMax.clear()
            mAppuse.clear()
            khachHangs.forEach {
                val hasContactsMap = MainState.contactsMap.containsKey(it.sdt)
                val isSmsOrTL = it.use_app.contains(listOf("sms", "TL"))
                if (hasContactsMap || isSmsOrTL) {
                    mContact.add(it.ten_kh)
                    mMobile.add(it.sdt)
                    mAppuse.add(it.use_app)
                    mKhongMax.add(it.tbl_XS)
                }
            }
            bindDialog.sprinTenkhach.adapter = ArrayAdapter(activity!!, R.layout.spinner_item, mContact)
        } catch (e: SQLException) {
        }
        bindDialog.sprinTenkhach.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, position: Int, id: Long) {
                mSpiner = position
                try {
                    jsonKhongmax = JSONObject(mKhongMax[mSpiner])
                    if (bind.radioDe.isChecked) {
                        xuatDan = if (bind.radioDeb.isChecked && jsonKhongmax!!.getString("danDe").isNotEmpty())
                            controller.taoTinDe(mContact[mSpiner], jsonKhongmax, textTien, min, max)
                        else if (bind.radioDea.isChecked && jsonKhongmax!!.getString("danDauDB").isNotEmpty())
                            controller.taoTinDeDauDB(mContact[mSpiner], jsonKhongmax, textTien, min, max)
                        else if (bind.radioDec.isChecked && jsonKhongmax!!.getString("danDauG1").isNotEmpty())
                            controller.taoTinDeDitG1(mContact[mSpiner], jsonKhongmax, textTien, min, max)
                        else if (bind.radioDea.isChecked && jsonKhongmax!!.getString("danDitG1").isNotEmpty())
                            controller.taoTinDeDauG1(mContact[mSpiner], jsonKhongmax, textTien, min, max)
                        else xuatDan
                        bindDialog.edtXuatDan.setText(xuatDan)
                    } else if (bind.radioLo.isChecked && jsonKhongmax!!.getString("danLo").isNotEmpty()) {
                        xuatDan = controller.taoTinLo(mContact[mSpiner], jsonKhongmax, textTien, min, max)
                        bindDialog.edtXuatDan.setText(xuatDan)
                    } else if (bind.radioXi.isChecked && (jsonKhongmax!!.getInt("xien2") > 0
                                || jsonKhongmax!!.getInt("xien3") > 0 || jsonKhongmax!!.getInt("xien4") > 0)
                    ) {
                        xuatDan = taoTinXi(mContact[mSpiner])
                        bindDialog.edtXuatDan.setText(xuatDan)
                    } else if (!bind.radioBc.isChecked || jsonKhongmax!!.getInt("cang") <= 0) {
                        bindDialog.edtXuatDan.setText(chuyendi)
                    } else {
                        bindDialog.edtXuatDan.setText(taoTinCang(mContact[mSpiner]))
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (throwable: Throwable) {
                    throwable.printStackTrace()
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
        bindDialog.btnChuyendi.setOnClickListener {
            val mDate = MainState.dateYMD
            val dodai = when (Setting.I.gioiHanTin.value()) {
                1 -> PathInterpolatorCompat.MAX_NUM_POINTS
                2 -> 155
                3 -> 315
                4 -> 475
                5 -> 995
                6 -> 2000
                else -> 2000
            }
            if (mMobile.size <= 0 || textXuatDan().isEmpty() || mDachuyen) {
                if (textXuatDan().isNotEmpty()) {
                    if (mDachuyen) {
                        dialog.cancel()
                    } else {
                        Toast.makeText(activity, "Chưa có chủ để chuyển tin!", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                mDachuyen = true
                var tinNhan = textXuatDan().replace("'", " ").trim()
                bindDialog.edtXuatDan.setText("")
                dialog.dismiss()
                if (tinNhan.trim().length < dodai) {
                    val maxSoTn = getMaxSoTinNhan(mDate, 2, "so_dienthoai = '" + mMobile[mSpiner] + "'")
                    controller.xulytin(
                        activity,
                        mMobile,
                        mContact,
                        mSpiner,
                        maxSoTn + 1,
                        tinNhan.replace("'", " ").trim()
                    )
                } else {
                    val maxSoTn = getMaxSoTinNhan(mDate, 2, "so_dienthoai = '" + mMobile[mSpiner] + "'")
                    var sotinNhan = maxSoTn + 1
                    var theLoai = ""
                    var chitiets = listOf<String>()
                    if (tinNhan.substring(0, 3).contains("De")) {
                        theLoai = "De:"
                        tinNhan = tinNhan.replace("De:", "")
                        chitiets = tinNhan.split(" ")
                    } else if (tinNhan.substring(0, 3).contains("Lo")) {
                        theLoai = "Lo:"
                        tinNhan = tinNhan.replace("Lo:", "")
                        chitiets = tinNhan.split(" ")
                    } else if (tinNhan.substring(0, 5).contains("Cang")) {
                        theLoai = "Cang:"
                        tinNhan = tinNhan.replace("Cang:\n", "")
                        chitiets = tinNhan.split(" ")
                    } else if (tinNhan.substring(0, 3).contains("Xi")) {
                        theLoai = "Xien:"
                        tinNhan = tinNhan.replace("Xien:\n", "").replace("d:", "0")
                            .replace("\n", " ")
                        chitiets = tinNhan.split(" ")
                    }
                    var ndung = ""
                    if (theLoai != "Xien:") {
                        chitiets.forEach { chiTiet ->
                            val danChiTiet = chiTiet.substring(0, chiTiet.indexOf("x"))
                            val tienChiTiet = chiTiet.substring(chiTiet.indexOf("x")).replace(",", "")
                            val listSo = danChiTiet.split(",")
                            listSo.forEachIndexed { index, so ->
                                var ndClear = ndung.replace(",x", "x")
                                if (ndClear.isNotEmpty()) {
                                    if (ndClear.length + tienChiTiet.length * 2 < dodai) {
                                        ndung = if (index >= listSo.size - 1)
                                            "$ndClear$so,$tienChiTiet "
                                        else
                                            "$ndClear$so,"
                                    } else {
                                        if (index > 0) {
                                            ndClear += tienChiTiet
                                        }
                                        controller.xulytin(
                                            activity,
                                            mMobile,
                                            mContact,
                                            mSpiner,
                                            sotinNhan, ndClear
                                        )
                                        sotinNhan++
                                        ndung = if (index >= listSo.size - 1)
                                            "$theLoai\n$so,$tienChiTiet "
                                        else
                                            "$theLoai\n$so,"
                                    }
                                } else {
                                    ndung = if (listSo.size == 1) "$theLoai\n$so,$tienChiTiet " else "$theLoai\n$so,"
                                }
                            }
                        }
                    } else {
                        for (s in chitiets) {
                            if (ndung.isEmpty()) {
                                ndung = "$theLoai\n$s "
                            } else if (ndung.length + s.length < dodai) {
                                ndung += "$s "
                            } else {
                                controller.xulytin(
                                    activity,
                                    mMobile,
                                    mContact,
                                    mSpiner, sotinNhan, ndung
                                )
                                sotinNhan++
                                ndung = "$theLoai\n$s "
                            }
                        }
                    }
                    if (ndung.isNotEmpty()) {
                        controller.xulytin(
                            activity,
                            mMobile,
                            mContact,
                            mSpiner, sotinNhan, ndung
                        )
                    }
                }
                Toast.makeText(activity, "Đã chuyển tin!", Toast.LENGTH_LONG).show()
            }
            xemlv()
            min = 0
            max = 100
            rangeSeekBar!!.selectedMinValue = 0
            rangeSeekBar!!.selectedMaxValue = 100
            bind.edtTien.setText("")
        }
        dialog.window!!.setLayout(-1, -2)
        dialog.setCancelable(true)
        dialog.setTitle("Xem dạng:")
        dialog.show()
    }

    override fun onResume() {
        xemRecycview()
        super.onResume()
    }

    private fun xemRecycview() {
        val noi: String
        val mDate = MainState.dateYMD
        var query: String? = null
        mSo.clear()
        mTienNhan.clear()
        mTienOm.clear()
        mTienchuyen.clear()
        mTienTon.clear()
        mNhay.clear()
        when (mDangXuat) {
            "(the_loai = 'deb' or the_loai = 'det')" -> query = """Select tbl_soctS.So_chon
                , Sum((tbl_soctS.type_kh = 1) * (100-tbl_soctS.diem_khachgiu)*diem_quydoi/100) as diem
                , so_om.Om_deB + sum(tbl_soctS.diem_dly_giu*tbl_soctS.diem_quydoi/100) as So_Om
                , Sum((tbl_soctS.type_kh =2) * tbl_soctS.diem_quydoi) as chuyen
                , Sum((tbl_soctS.type_kh =1) * (100-tbl_soctS.diem_khachgiu-tbl_soctS.diem_dly_giu)*diem_quydoi/100) - Sum((tbl_soctS.type_kh =2) * tbl_soctS.diem_quydoi) - so_om.Om_deB as ton
                , so_nhay  From so_om Left Join tbl_soctS On tbl_soctS.so_chon = so_om.So
                Where tbl_soctS.ngay_nhan='$mDate' AND (tbl_soctS.the_loai='deb' OR tbl_soctS.the_loai='det') GROUP by so_om.So Order by $sapxep"""
            "the_loai = 'lo'" -> query = """Select tbl_soctS.So_chon
                , Sum((tbl_soctS.type_kh = 1) * (100-tbl_soctS.diem_khachgiu)*diem_quydoi/100) as diem
                , so_om.Om_Lo + sum(tbl_soctS.diem_dly_giu*tbl_soctS.diem_quydoi/100) as So_Om
                , Sum((tbl_soctS.type_kh =2) * tbl_soctS.diem_quydoi) as chuyen
                , Sum((tbl_soctS.type_kh =1) * (100-tbl_soctS.diem_khachgiu-tbl_soctS.diem_dly_giu)*diem_quydoi/100) - Sum((tbl_soctS.type_kh =2) * tbl_soctS.diem_quydoi) - so_om.Om_Lo as ton
                , so_nhay  From so_om Left Join tbl_soctS On tbl_soctS.so_chon = so_om.So 
                Where tbl_soctS.ngay_nhan='$mDate' AND tbl_soctS.the_loai='lo' 
                GROUP by so_om.So Order by $sapxep"""
            "the_loai = 'loa'" -> query = """Select tbl_soctS.So_chon
                , Sum((tbl_soctS.type_kh = 1) * (100-tbl_soctS.diem_khachgiu)*diem_quydoi/100) as diem
                , so_om.Om_Lo + sum(tbl_soctS.diem_dly_giu*tbl_soctS.diem_quydoi/100) as So_Om
                , Sum((tbl_soctS.type_kh =2) * tbl_soctS.diem_quydoi) as chuyen
                , Sum((tbl_soctS.type_kh =1) * (100-tbl_soctS.diem_khachgiu-tbl_soctS.diem_dly_giu)*diem_quydoi/100) - Sum((tbl_soctS.type_kh =2) * tbl_soctS.diem_quydoi) - so_om.Om_Lo as ton
                , so_nhay  From so_om Left Join tbl_soctS On tbl_soctS.so_chon = so_om.So 
                Where tbl_soctS.ngay_nhan='$mDate' AND tbl_soctS.the_loai='loa' 
                GROUP by so_om.So Order by $sapxep"""
            "the_loai = 'dea'" -> query = """Select tbl_soctS.So_chon
                , Sum((tbl_soctS.type_kh = 1) * (100-tbl_soctS.diem_khachgiu)*diem_quydoi/100) as diem
                , so_om.Om_DeA + sum(tbl_soctS.diem_dly_giu*tbl_soctS.diem_quydoi/100) as So_Om
                , Sum((tbl_soctS.type_kh =2) * tbl_soctS.diem_quydoi) as chuyen
                , Sum((tbl_soctS.type_kh =1) * (100-tbl_soctS.diem_khachgiu-tbl_soctS.diem_dly_giu)*diem_quydoi/100) - Sum((tbl_soctS.type_kh =2) * tbl_soctS.diem_quydoi) - so_om.Om_DeA as ton
                , so_nhay  From so_om Left Join tbl_soctS On tbl_soctS.so_chon = so_om.So 
                Where tbl_soctS.ngay_nhan='$mDate' AND tbl_soctS.the_loai='dea' GROUP by so_chon Order by $sapxep"""
            "the_loai = 'dec'" -> query = """Select tbl_soctS.So_chon
                , Sum((tbl_soctS.type_kh = 1) * (100-tbl_soctS.diem_khachgiu)*diem_quydoi/100) as diem
                , so_om.Om_DeC + sum(tbl_soctS.diem_dly_giu*tbl_soctS.diem_quydoi/100) as So_Om
                , Sum((tbl_soctS.type_kh =2) * tbl_soctS.diem_quydoi) as chuyen
                , Sum((tbl_soctS.type_kh =1) * (100-tbl_soctS.diem_khachgiu-tbl_soctS.diem_dly_giu)*diem_quydoi/100) - Sum((tbl_soctS.type_kh =2) * tbl_soctS.diem_quydoi) - so_om.Om_DeC as ton
                , so_nhay  From so_om Left Join tbl_soctS On tbl_soctS.so_chon = so_om.So 
                Where tbl_soctS.ngay_nhan='$mDate' AND tbl_soctS.the_loai='dec' GROUP by so_chon Order by $sapxep"""
            "the_loai = 'ded'" -> query = """Select tbl_soctS.So_chon
                , Sum((tbl_soctS.type_kh = 1) * (100-tbl_soctS.diem_khachgiu)*diem_quydoi/100) as diem
                , so_om.Om_DeD + sum(tbl_soctS.diem_dly_giu*tbl_soctS.diem_quydoi/100) as So_Om
                , Sum((tbl_soctS.type_kh =2) * tbl_soctS.diem_quydoi) as chuyen
                , Sum((tbl_soctS.type_kh =1) * (100-tbl_soctS.diem_khachgiu-tbl_soctS.diem_dly_giu)*diem_quydoi/100) - Sum((tbl_soctS.type_kh =2) * tbl_soctS.diem_quydoi) - so_om.Om_DeD as ton
                , so_nhay  From so_om Left Join tbl_soctS On tbl_soctS.so_chon = so_om.So 
                Where tbl_soctS.ngay_nhan='$mDate' AND tbl_soctS.the_loai='ded' GROUP by so_chon Order by $sapxep"""
            "the_loai = 'xi'" -> {
                noi = if (layX2 == "" && layX3 == "" && layX4 == "") ""
                else (" And ($layX2$layX3$layX4)").replace("(OR", "(")
                val c1 = db!!.getData("Select * From So_om WHERE ID = 1")
                c1.moveToFirst()
                query =
                    "SELECT so_chon, sum((type_kh =1)*(100-diem_khachgiu)*diem_quydoi)/100 AS diem, ((length(so_chon) = 5) * " + c1.getString(
                        7
                    ) + " +(length(so_chon) = 8) * " + c1.getString(8) + " +(length(so_chon) = 11) * " + c1.getString(9) + " + sum(diem_dly_giu*diem_quydoi/100)) AS Om, SUm((type_kh =2)*diem) as chuyen , SUm((type_kh =1)*(100-diem_khachgiu-diem_dly_giu)*diem_quydoi/100)-SUm((type_kh =2)*diem) -  ((length(so_chon) = 5) * " + c1.getString(
                        7
                    ) + " +(length(so_chon) = 8) * " + c1.getString(8) + " +(length(so_chon) = 11) * " + c1.getString(9) + ") AS ton, so_nhay   From tbl_soctS Where ngay_nhan='" + mDate + "' AND the_loai='xi'" + noi + "  GROUP by so_chon Order by ton DESC, diem DESC"
                if (!c1.isClosed) c1.close()
            }
            "the_loai = 'bc'" -> {
                var c12 = db!!.getData("Select * From So_om WHERE ID = 1")
                c12.moveToFirst()
                if (c12.getInt(10) == 1) {
                    db!!.queryData("Update so_om set om_bc=0 WHERE id = 1")
                    c12 = db!!.getData("Select * From So_om WHERE ID = 1")
                    c12.moveToFirst()
                }
                query =
                    "SELECT so_chon, sum((type_kh = 1)*(100-diem_khachgiu)*diem_quydoi/100) AS diem, " + c12.getString(
                        10
                    ) + " + sum(diem_dly_giu*diem_quydoi)/100 AS Om, SUm((type_kh = 2)*diem) as Chuyen, sum((type_kh =1)*(100-diem_khachgiu-diem_dly_giu)*diem_quydoi/100) - sum((type_kh =2)*diem) -" + c12.getString(
                        10
                    ) + " AS ton, so_nhay   From tbl_soctS Where ngay_nhan='" + mDate + "' AND the_loai='bc' GROUP by so_chon Order by ton DESC, diem DESC"
                if (!c12.isClosed) {
                    c12.close()
                }
            }
            "the_loai = 'xn'" -> query =
                "SELECT so_chon, sum((type_kh =1)*(diem_quydoi)) AS diem, sum(tbl_soctS.diem_dly_giu) AS Om, SUm((type_kh =2)*diem) as chuyen " +
                        ", SUm((type_kh =1)*diem_ton-(type_kh =2)*diem_ton) AS ton, so_nhay From tbl_soctS " +
                        "Where ngay_nhan='" + mDate + "' AND the_loai='xn' GROUP by so_chon Order by ton DESC, diem DESC"
        }
        val cursor = db!!.getData(query)
        while (cursor.moveToNext()) {
            mSo.add(cursor.getString(0))
            mTienNhan.add(cursor.getInt(1).toDecimal())
            mTienOm.add(cursor.getInt(2).toDecimal())
            mTienchuyen.add(cursor.getInt(3).toDecimal())
            mTienTon.add(cursor.getInt(4).toDecimal())
            mNhay.add(cursor.getInt(5))
            //Where tbl_soctS.ngay_nhan='" + mDate + "' AND (tbl_soctS.the_loai='deb' OR tbl_soctS.the_loai='det') GROUP by so_om.So Order by " + this.sapxep;
        }
        if (!cursor.isClosed) {
            cursor.close()
        }
        if (activity != null) {
            bind.lview.adapter = SoOmAdapter(activity, R.layout.frag_canchuyen_lv, mSo)
        }
    }

    inner class SoOmAdapter(context: Context?, resource: Int, objects: List<String?>?) : ArrayAdapter<Any?>(
        context!!, resource, objects!!
    ) {
        internal inner class ViewHolder(val bind: FragCanchuyenLvBinding)

        @SuppressLint("SetTextI18n", "RestrictedApi")
        override fun getView(position: Int, mView: View?, parent: ViewGroup): View {
            val view: View?

            val holder: ViewHolder
            if (mView == null) {
                val bind = FragCanchuyenLvBinding.inflate(layoutInflater)
                holder = ViewHolder(bind)
                view = bind.root
                view.tag = holder
            } else {
                holder = mView.tag as ViewHolder
            }

            if (mNhay[position] > 0) {
                holder.bind.TvSo.setTextColor(SupportMenu.CATEGORY_MASK)
                holder.bind.tvDiemNhan.setTextColor(SupportMenu.CATEGORY_MASK)
                holder.bind.tvDiemOm.setTextColor(SupportMenu.CATEGORY_MASK)
                holder.bind.tvDiemChuyen.setTextColor(SupportMenu.CATEGORY_MASK)
                holder.bind.tvDiemTon.setTextColor(SupportMenu.CATEGORY_MASK)
                holder.bind.TvSo.text = mSo[position] + "*".repeat(mNhay[position])
            } else {
                holder.bind.TvSo.setTextColor(View.MEASURED_STATE_MASK)
                holder.bind.tvDiemNhan.setTextColor(View.MEASURED_STATE_MASK)
                holder.bind.tvDiemOm.setTextColor(View.MEASURED_STATE_MASK)
                holder.bind.tvDiemChuyen.setTextColor(View.MEASURED_STATE_MASK)
                holder.bind.tvDiemTon.setTextColor(View.MEASURED_STATE_MASK)
                holder.bind.TvSo.text = mSo[position]
            }
            holder.bind.tvDiemNhan.text = mTienNhan[position]
            holder.bind.tvDiemOm.text = mTienOm[position]
            holder.bind.tvDiemChuyen.text = mTienchuyen[position]
            holder.bind.tvDiemTon.text = mTienTon[position]
            holder.bind.stt.text = (position + 1).toString()
            return holder.bind.root
        }
    }
}