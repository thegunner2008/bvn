package tamhoang.bvn.ui.baocao

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.internal.view.SupportMenu
import tamhoang.bvn.R
import tamhoang.bvn.data.DbOpenHelper
import tamhoang.bvn.data.enum.TYPE
import tamhoang.bvn.data.model.SoctS
import tamhoang.bvn.databinding.ActivityKhachBinding
import tamhoang.bvn.databinding.ActivityKhachLvBinding
import tamhoang.bvn.ui.base.toolbar.BaseToolBarActivity
import tamhoang.bvn.ui.main.MainState
import tamhoang.bvn.util.extensions.toDecimal

class ActivityKhach : BaseToolBarActivity() {
    private var _bind: ActivityKhachBinding? = null
    val bind: ActivityKhachBinding get() = _bind!!

    var dangXuat: String? = null
    var db: DbOpenHelper? = null
    var mDiem = ArrayList<String>()
    var mDiemGiu = ArrayList<String>()
    var mNhay = ArrayList<Int>()
    var mSo = ArrayList<String>()
    var mThanhTien = ArrayList<String>()
    var message: String? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_khach
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        _bind = ActivityKhachBinding.inflate(layoutInflater)
        setContentView(bind.root)
        db = DbOpenHelper(this)
        message = intent.getStringExtra("tenKH")
        bind.tvHeader.text = "Khách hàng: $message"
        bind.radioDe.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                dangXuat = "(the_loai = 'deb' or the_loai = 'det')"
                xemLvKhach()
            }
        }
        bind.radioLo.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                dangXuat = "the_loai = 'lo'"
                xemLvKhach()
            }
        }
        bind.radioXi.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                dangXuat = "the_loai = 'xi'"
                xemLvKhach()
            }
        }
        bind.radioBc.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                dangXuat = "the_loai = 'bc'"
                xemLvKhach()
            }
        }
        bind.radioDe.isChecked = true
    }

    private fun xemLvKhach() {
        val dateYMD = MainState.dateYMD
        val data = db!!.getFullData(
            SoctS.TABLE,
            "ngay_nhan = '$dateYMD' AND ten_kh = '$message' AND $dangXuat Group by so_chon order by diem DESC",
            Pair(TYPE.String, "so_chon"),
            Pair(TYPE.Double, "sum(diem_quydoi)"),
            Pair(TYPE.Double, "sum(diem_khachgiu*diem_quydoi)/100"),
            Pair(TYPE.Double, "sum((100-diem_khachgiu)*diem_quydoi/100)"),
            Pair(TYPE.Int, "so_nhay")
        )
        mSo.clear()
        mDiem.clear()
        mDiemGiu.clear()
        mThanhTien.clear()
        mNhay.clear()
        data.forEach {
            val (soChon, diem, diemGiu, thanhTien, soNhay) = it
            mSo.add(soChon as String)
            mDiem.add((diem as Double).toDecimal())
            mDiemGiu.add((diemGiu as Double).toDecimal())
            mThanhTien.add((thanhTien as Double).toDecimal())
            mNhay.add(soNhay as Int)
        }
        bind.lvKhach.adapter = KhachAdapter(this, R.layout.activity_khach_lv, mSo)
    }

    inner class ViewHolder(val bindLv: ActivityKhachLvBinding)

    internal inner class KhachAdapter(context: Context?, i: Int, list: List<String?>?) : ArrayAdapter<Any?>(
        context!!, i, list!!
    ) {
        @SuppressLint("RestrictedApi", "SetTextI18n")
        override fun getView(i: Int, mView: View?, viewGroup: ViewGroup): View {
            var view = mView
            val holder: ViewHolder?
            if (view == null) {
                val bindLv = ActivityKhachLvBinding.inflate(layoutInflater)
                holder = ViewHolder(bindLv)
                view = bindLv.root
                view.tag = holder
            } else {
                holder = view.tag as ViewHolder
            }
            if (mNhay[i] > 0) {
                holder.bindLv.TvSo.setTextColor(SupportMenu.CATEGORY_MASK)
                holder.bindLv.tvDiemNhan.setTextColor(SupportMenu.CATEGORY_MASK)
                holder.bindLv.tvDiemGiu.setTextColor(SupportMenu.CATEGORY_MASK)
                holder.bindLv.tvThanhtien.setTextColor(SupportMenu.CATEGORY_MASK)
                if (mNhay[i] in 1..4) {
                    holder.bindLv.TvSo.text = mSo[i] + "*".repeat(mNhay[i])
                }
                holder.bindLv.stt.text = (i + 1).toDecimal()
                holder.bindLv.tvDiemNhan.text = mDiem[i]
                holder.bindLv.tvDiemGiu.text = mDiemGiu[i]
                holder.bindLv.tvThanhtien.text = mThanhTien[i]
            } else {
                holder.bindLv.TvSo.setTextColor(View.MEASURED_STATE_MASK)
                holder.bindLv.tvDiemNhan.setTextColor(View.MEASURED_STATE_MASK)
                holder.bindLv.tvDiemGiu.setTextColor(View.MEASURED_STATE_MASK)
                holder.bindLv.tvThanhtien.setTextColor(View.MEASURED_STATE_MASK)
                holder.bindLv.stt.text = (i + 1).toDecimal()
                holder.bindLv.TvSo.text = mSo[i]
                holder.bindLv.tvDiemNhan.text = mDiem[i]
                holder.bindLv.tvDiemGiu.text = mDiemGiu[i]
                holder.bindLv.tvThanhtien.text = mThanhTien[i]
            }
            return holder.bindLv.root
        }
    }
}