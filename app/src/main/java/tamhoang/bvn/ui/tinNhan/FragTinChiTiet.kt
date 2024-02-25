package tamhoang.bvn.ui.tinNhan

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemSelectedListener
import androidx.core.internal.view.SupportMenu
import androidx.fragment.app.Fragment
import org.json.JSONException
import org.json.JSONObject
import tamhoang.bvn.R
import tamhoang.bvn.data.DbOpenHelper
import tamhoang.bvn.data.enum.TLkq
import tamhoang.bvn.data.store.TinNhanStore
import tamhoang.bvn.databinding.ActivityTinnhanLvBinding
import tamhoang.bvn.ui.base.dialog.Dialog
import tamhoang.bvn.ui.chat.ActivityTinnhan
import tamhoang.bvn.ui.main.MainState
import tamhoang.bvn.util.extensions.toDecimal

class FragTinChiTiet : Fragment() {
    var db: DbOpenHelper? = null
    var jsonValues = ArrayList<JSONObject>()
    var lvNoTinnhan: ListView? = null
    var mDate: String? = null
    private val mID: MutableList<Int?> = ArrayList()
    private val mTen = ArrayList<String>()
    var spPosition = 0
    var spKhachhang: Spinner? = null
    var str = ""
    var tenKhach: String? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.frag_norp3, container, false)
        db = DbOpenHelper(activity!!)
        lvNoTinnhan = v.findViewById<View>(R.id.no_rp_tinnhan) as ListView
        lvNoTinnhan!!.onItemClickListener =
            OnItemClickListener { _, view: View?, position: Int, _ ->
                val menus = arrayOf("Sửa", "Xóa")
                val popupL = PopupMenu(activity, view)
                for (i in menus.indices) {
                    popupL.menu.add(1, i, i, menus[i])
                }
                popupL.setOnMenuItemClickListener { item: MenuItem ->
                    val order = item.order
                    if (order == 0) {
                        val intent = Intent(activity, ActivityTinnhan::class.java)
                        intent.putExtra("m_ID", mID[position].toString() + "")
                        startActivity(intent)
                    } else if (order == 1) {
                        Dialog.simple(
                            context = activity!!,
                            title = "Xoá tin nhắn này?",
                            positiveText = "YES",
                            positiveAction = {
                                if (mID[position] == null) return@simple
                                TinNhanStore.I.selectByID(ID = mID[position]!!)?.let {
                                    db!!.queryData(
                                        "DELETE FROM tbl_tinnhanS WHERE ngay_nhan = '${it.ngayNhan}' AND ten_kh = '${it.tenKh}' AND so_tin_nhan = ${it.soTinNhan} AND type_kh = ${it.typeKh}"
                                    )
                                    db!!.queryData(
                                        "DELETE FROM tbl_soctS WHERE ngay_nhan = '${it.ngayNhan}' AND ten_kh = '${it.tenKh}' AND so_tin_nhan = ${it.soTinNhan} AND type_kh = ${it.typeKh}"
                                    )
                                    Toast.makeText(activity, "Đã xóa tin", Toast.LENGTH_LONG).show()
                                    lvReportSms()
                                }
                            },
                            negativeText = "NO"
                        )
                    }
                    true
                }
                popupL.show()
            }
        spKhachhang = v.findViewById<View>(R.id.sp_khachhang) as Spinner
        mTen.add("Lọc theo khách")
        spKhachhang!!.adapter = ArrayAdapter(activity!!, R.layout.spinner_item, mTen)
        spKhachhang!!.setSelection(0)
        spKhachhang?.setOnTouchListener { _, _ ->
            mDate = MainState.dateYMD
            val newTen = ArrayList<String>()
            val database = db!!
            val cursor =
                database.getData("Select ten_kh From tbl_soctS WHERE ngay_nhan = '$mDate' GROUP by ten_kh Order by ten_kh")
            while (cursor.moveToNext()) {
                newTen.add(cursor.getString(0))
            }
            cursor.close()
            if (newTen.size == 0) {
                newTen.add("Hôm nay chưa có tin nhắn")
            }
            if (mTen.size == newTen.size && mTen.containsAll(mTen)) return@setOnTouchListener false
            mTen.clear()
            mTen.addAll(newTen)
            spKhachhang!!.adapter = ArrayAdapter(activity!!, R.layout.spinner_item, mTen)
            try {
                spKhachhang!!.setSelection(mTen.indexOf(tenKhach))
            } catch (ignored: Exception) {
            }
            false
        }
        spKhachhang?.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                spPosition = position
                str = " AND tbl_soctS.ten_kh = '${mTen[spPosition]}'"
                tenKhach = mTen.getOrNull(spPosition) ?: return
                lvReportSms()
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
        registerForContextMenu(lvNoTinnhan!!)
        return v
    }

    fun lvReportSms() {
        val getDate = MainState.dateYMD
        val listTinNhan = TinNhanStore.I.selectListWhere(
            date = getDate,
            where = "ten_kh = '${mTen[spPosition]}' AND phat_hien_loi = 'ok' Order by type_kh, so_tin_nhan"
        )
        jsonValues = ArrayList()
        try {
            mID.clear()
            listTinNhan.forEach {
                val cursor = db!!.getData(
                    """SElECT CASE 
                        WHEN the_loai = 'xi' And length(so_chon) = 5 THEN 'xi2' 
                        WHEN the_loai = 'xi' And length(so_chon) = 8 THEN 'xi3' 
                        WHEN the_loai = 'xi' And length(so_chon) = 11 THEN 'xi4' 
                        WHEN the_loai = 'xia' And length(so_chon) = 5 THEN 'xia2' 
                        WHEN the_loai = 'xia' And length(so_chon) = 8 THEN 'xia3' 
                        WHEN the_loai = 'xia' And length(so_chon) = 11 THEN 'xia4' 
                        ELSE the_loai END theloai, sum(diem), sum(diem*so_nhay) as An
                        , sum (tong_tien)/1000 as kq 
                        , sum(Ket_qua)/1000 as tienCuoi
                        From tbl_soctS 
                        Where ten_kh = '${mTen[spPosition]}' and ngay_nhan = '$getDate' and So_tin_nhan = ${it.soTinNhan} AND type_kh = ${it.typeKh} Group by theloai"""
                )
                val jsonTinnhan = JSONObject().apply {
                    put("ID", it.ID)
                    put("gio_nhan", it.gioNhan)
                    put("type_kh", it.typeKh)
                    put("ten_KH", it.tenKh)
                    put("so_tinnhan", it.soTinNhan)
                    put("tin_goc", it.ndGoc)
                    put("nd_phantich", it.ndPhanTich)
                }
                val jsonChitiet = JSONObject()
                var tongTien = 0.0
                var ketQua = 0.0
                while (cursor.moveToNext()) {
                    try {
                        jsonChitiet.put("the_loai", cursor.getString(0))
                        jsonChitiet.put("diem", cursor.getInt(1).toDecimal())
                        jsonChitiet.put("diem_an", cursor.getInt(2).toDecimal())
                        jsonChitiet.put("tong_tien", cursor.getInt(3).toDecimal())
                        jsonChitiet.put("ket_qua", cursor.getInt(4).toDecimal())
                        tongTien += cursor.getDouble(3)
                        ketQua += cursor.getDouble(4)
                        jsonTinnhan.put(cursor.getString(0), jsonChitiet.toString())
                    } catch (ignored: JSONException) {
                    }
                }
                try {
                    jsonTinnhan.put("tong_tien", tongTien.toDecimal())
                    jsonTinnhan.put("ket_qua", ketQua.toDecimal())
                    jsonValues.add(jsonTinnhan)
                    mID.add(it.ID)
                    cursor.close()
                } catch (ignored: JSONException) {
                }
            }
        } catch (ignored: JSONException) {
        }
        if (activity != null) {
            lvNoTinnhan!!.adapter = TinNhanAdapter(activity, R.layout.activity_tinnhan_lv, jsonValues)
        }
    }

    inner class TinNhanAdapter(context: Context?, resource: Int, objects: List<JSONObject?>?) :
        ArrayAdapter<Any?>(
            context!!, resource, objects!!
        ) {
        @SuppressLint("RestrictedApi")
        override fun getView(position: Int, mView: View?, parent: ViewGroup): View {
            val bind = ActivityTinnhanLvBinding.inflate(layoutInflater)

            val json = jsonValues[position]
            try {
                bind.tvTinGoc.text = json.getString("tin_goc")
                bind.tvTenKH.text = json.getString("ten_KH")
                if (json.getString("type_kh").contains("2")) {
                    bind.tvTenKH.setTextColor(Color.parseColor("#1a40ea"))
                }
                bind.tvTinNhan.text = json.getString("so_tinnhan")
                bind.tvGioNhan.text = json.getString("gio_nhan")
                bind.tvTongTien.text = json.getString("tong_tien")
                bind.tvKetQua.text = json.getString("ket_qua")
                val ndPhantich = json.getString("nd_phantich")
                val wordtoSpan2: Spannable = SpannableString(ndPhantich)
                var i1 = 0
                while (i1 < ndPhantich.length - 1) {
                    try {
                        if (ndPhantich.substring(i1, i1 + 2).contains("*")) {
                            var i2 = i1
                            while (i2 > 0) {
                                val ch = ndPhantich.substring(i2, i2 + 1)
                                if (ch.contains(",") || ch.contains(":")) {
                                    break
                                } else {
                                    wordtoSpan2.setSpan(
                                        ForegroundColorSpan(SupportMenu.CATEGORY_MASK),
                                        i2,
                                        i1 + 1,
                                        33
                                    )
                                    i2--
                                }
                            }
                        }
                        i1++
                    } catch (e5: Exception) {
                        e5.printStackTrace()
                        return bind.root
                    }
                }
                bind.tvTinPhanTich.text = wordtoSpan2

                json.keys().forEach { key ->
                    TLkq.values().firstOrNull { key.contains(it.t) }?.let { theLoai ->
                        val (layout, tvDiem, tvAn, tvTongTien, tvKq) =
                            with(bind) {
                                when (theLoai) {
                                    TLkq.DeA -> arrayOf(linerDeA, tvDiemDeA, tvAnDeA, tvNoDeA, tvNoThangThuaDeA)
                                    TLkq.DeB -> arrayOf(null, tvDiemDe, tvAnDe, tvNoDe, tvNoThangThuaDe)
                                    TLkq.DeC -> arrayOf(linerDeC, tvDiemDeC, tvAnDeC, tvNoDeC, tvNoThangThuaDeC)
                                    TLkq.DeD -> arrayOf(linerDeD, tvDiemDeD, tvAnDeD, tvNoDeD, tvNoThangThuaDeD)
                                    TLkq.Det -> arrayOf(linerDeT, tvDiemDeT, tvAnDeT, tvNoDeT, tvNoThangThuaDeT)
                                    TLkq.Lo -> arrayOf(null, tvDiemlo, tvAnlo, tvNoLo, tvNoThangThuaLo)
                                    TLkq.LoA -> arrayOf(linerLoa, tvDiemloa, tvAnloa, tvNoLoa, tvNoThangThuaLoa)
                                    TLkq.XI2 -> arrayOf(lnxi2, tvDiemxi2, tvAnxi2, tvNoXi2, tvNoThangThuaXi2)
                                    TLkq.XI3 -> arrayOf(lnxi3, tvDiemxi3, tvAnxi3, tvNoXi3, tvNoThangThuaXi3)
                                    TLkq.XI4 -> arrayOf(lnxi4, tvDiemxi4, tvAnxi4, tvNoXi4, tvNoThangThuaXi2)
                                    TLkq.XIA2 -> arrayOf(lnxia2, tvDiemxia2, tvAnxia2, tvNoXia2, tvNoThangThuaXia2)
                                    TLkq.XIA3 -> arrayOf(lnxia3, tvDiemxia3, tvAnxia3, tvNoXia3, tvNoThangThuaXia3)
                                    TLkq.XIA4 -> arrayOf(lnxia4, tvDiemxia4, tvAnxia4, tvNoXia4, tvNoThangThuaXia4)
                                    TLkq.XN -> arrayOf(linerXN, tvDiemxn, tvAnxn, tvNoXn, tvNoThangThuaXn)
                                    TLkq.BC -> arrayOf(null, tvDiembc, tvAnbc, tvNoBc, tvNoThangThuaBc)
                                    TLkq.BCA -> arrayOf(linerBca, tvDiembca, tvAnbca, tvNoBca, tvNoThangThuaBca)
                                }
                            }

                        val jsonDang = JSONObject(json.getString(key))
                        layout?.visibility = View.VISIBLE
                        (tvDiem as TextView?)?.text = jsonDang.getString("diem")
                        (tvAn as TextView?)?.text = jsonDang.getString("diem_an")
                        (tvTongTien as TextView?)?.text = jsonDang.getString("tong_tien")
                        (tvKq as TextView?)?.text = jsonDang.getString("ket_qua")
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                return bind.root
            }
            return bind.root
        }
    }
}