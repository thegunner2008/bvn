package tamhoang.bvn.ui.chat

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.internal.view.SupportMenu.CATEGORY_MASK
import org.json.JSONException
import org.json.JSONObject
import tamhoang.bvn.R
import tamhoang.bvn.data.DbOpenHelper
import tamhoang.bvn.data.store.TinNhanStore
import tamhoang.bvn.databinding.ActivityCttinnhanBinding
import tamhoang.bvn.ui.base.toolbar.BaseToolBarActivity
import tamhoang.bvn.util.extensions.toDecimal

class ActivityCTTinnhan : BaseToolBarActivity() {
    private var _bind: ActivityCttinnhanBinding? = null
    private val bind get() = _bind!!

    var db: DbOpenHelper? = null
    var id: String? = ""
    var typeKh = ""
    override fun getLayoutId(): Int {
        return R.layout.activity_cttinnhan
    }

    private fun createUI(
        theLoai: String,
        json: JSONObject,
        layout: LinearLayout,
        tvDiem: TextView,
        tvAn: TextView,
        tvNo: TextView,
        tvThangThua: TextView,
        result: (Double, Double) -> Unit
    ) {
        if (json.has(theLoai)) {
            layout.visibility = View.VISIBLE
            val jsonDang = JSONObject(json.getString(theLoai))
            tvDiem.text = jsonDang.getDouble("diem").toDecimal()
            tvAn.text = jsonDang.getDouble("diem_an").toDecimal()
            tvNo.text = jsonDang.getDouble("tong_tien").toDecimal()
            tvThangThua.text = jsonDang.getDouble("ket_qua").toDecimal()
            result(jsonDang.getDouble("tong_tien"), jsonDang.getDouble("ket_qua"))
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        var ndPt: String
        super.onCreate(savedInstanceState)
        _bind = ActivityCttinnhanBinding.inflate(layoutInflater)
        setContentView(bind.root)
        id = intent.getStringExtra("m_ID") ?: return
        db = DbOpenHelper(this)

        var mTongTien = 0.0
        var mKetQua = 0.0

        val tinNhan = TinNhanStore.I.selectByID(id!!) ?: return

        val queryStr = """Select CASE 
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
            Where ten_kh = '${tinNhan.tenKh}' and ngay_nhan = '${tinNhan.ngayNhan}' and so_tin_nhan = ${tinNhan.soTinNhan} Group by theloai"""
        val ctTin = db!!.getData(queryStr)
        val json = JSONObject()
        while (ctTin.moveToNext()) {
            try {
                val jsonDang = JSONObject().apply {
                    put("diem", ctTin.getDouble(1))
                    put("diem_an", ctTin.getDouble(2))
                    put("tong_tien", ctTin.getDouble(3))
                    put("ket_qua", ctTin.getDouble(4))
                }
                json.put(ctTin.getString(0), jsonDang.toString())
            } catch (e: JSONException) {
                e.printStackTrace()
                ctTin.close()
            }
        }
        try {
            bind.apply {
                createUI("dea", json, linerDeA, tvDiemDeA, tvAnDeA, tvNoDeA, tvNoThangThuaDeA) { tien, ketQua ->
                    mTongTien += tien
                    mKetQua += ketQua
                }
                createUI("deb", json, linerDeB, tvDiemDe, tvAnDe, tvNoDe, tvNoThangThuaDe) { tien, ketQua ->
                    mTongTien += tien
                    mKetQua += ketQua
                }
                createUI("dec", json, linerDeC, tvDiemDeC, tvAnDeC, tvNoDeC, tvNoThangThuaDeC) { tien, ketQua ->
                    mTongTien += tien
                    mKetQua += ketQua
                }
                createUI("ded", json, linerDeD, tvDiemDeD, tvAnDeD, tvNoDeD, tvNoThangThuaDeD) { tien, ketQua ->
                    mTongTien += tien
                    mKetQua += ketQua
                }
                createUI("det", json, linerDeT, tvDiemDeT, tvAnDeT, tvNoDeT, tvNoThangThuaDeT) { tien, ketQua ->
                    mTongTien += tien
                    mKetQua += ketQua
                }
                createUI("xn", json, linerXN, tvDiemxn, tvAnxn, tvNoXn, tvNoThangThuaXn) { tien, ketQua ->
                    mTongTien += tien
                    mKetQua += ketQua
                }
                createUI("lo", json, linerLo, tvDiemlo, tvAnlo, tvNoLo, tvNoThangThuaLo) { tien, ketQua ->
                    mTongTien += tien
                    mKetQua += ketQua
                }
                createUI("loa", json, linerLoa, tvDiemloa, tvAnloa, tvNoLoa, tvNoThangThuaLoa) { tien, ketQua ->
                    mTongTien += tien
                    mKetQua += ketQua
                }
                createUI("bc", json, lnBc, tvDiembc, tvAnbc, tvNoBc, tvNoThangThuaBc) { tien, ketQua ->
                    mTongTien += tien
                    mKetQua += ketQua
                }
                createUI("bca", json, linerBca, tvDiembca, tvAnbca, tvNoBca, tvNoThangThuaBca) { tien, ketQua ->
                    mTongTien += tien
                    mKetQua += ketQua
                }
                createUI("xi2", json, lnxi2, tvDiemxi2, tvAnxi2, tvNoXi2, tvNoThangThuaXi2) { tien, ketQua ->
                    mTongTien += tien
                    mKetQua += ketQua
                }
                createUI("xi3", json, lnxi3, tvDiemxi3, tvAnxi3, tvNoXi3, tvNoThangThuaXi3) { tien, ketQua ->
                    mTongTien += tien
                    mKetQua += ketQua
                }
                createUI("xi4", json, lnxi4, tvDiemxi4, tvAnxi4, tvNoXi4, tvNoThangThuaXi4) { tien, ketQua ->
                    mTongTien += tien
                    mKetQua += ketQua
                }
                createUI("xia2", json, lnxia2, tvDiemxia2, tvAnxia2, tvNoXia2, tvNoThangThuaXia2) { tien, ketQua ->
                    mTongTien += tien
                    mKetQua += ketQua
                }
                createUI("xia3", json, lnxia3, tvDiemxia3, tvAnxia3, tvNoXia3, tvNoThangThuaXia3) { tien, ketQua ->
                    mTongTien += tien
                    mKetQua += ketQua
                }
                createUI("xia4", json, lnxia4, tvDiemxia4, tvAnxia4, tvNoXia4, tvNoThangThuaXia4) { tien, ketQua ->
                    mTongTien += tien
                    mKetQua += ketQua
                }
                tvNoRpNd.text = tinNhan.ndGoc
                ndPt = tinNhan.ndPhanTich
                tvNoKH.text = tinNhan.tenKh
                tvNoTinNhan.text = tinNhan.soTinNhan.toDecimal()
                tvNoTGNhan.text = tinNhan.gioNhan
            }
            val wordtoSpan = SpannableString(tinNhan.ndPhanTich)
            var i1 = 0
            while (i1 < ndPt.length - 1) {
                if (ndPt.substring(i1, i1 + 2).contains("*")) {
                    var i2 = i1
                    while (i2 > 0 && ndPt[i2] !in arrayOf(',', ':')) {
                        wordtoSpan.setSpan(ForegroundColorSpan(CATEGORY_MASK), i2, i1 + 1, 33)
                        i2--
                    }
                }
                i1++
            }
            bind.tvNdpt.text = wordtoSpan
        } catch (e: JSONException) {
            e.printStackTrace()
            ctTin.close()
        }
        ctTin.close()
    }
}