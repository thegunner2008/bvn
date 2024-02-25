package tamhoang.bvn.ui.canBang

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import tamhoang.bvn.R
import tamhoang.bvn.data.DbOpenHelper
import tamhoang.bvn.databinding.FragCanBangBinding
import tamhoang.bvn.databinding.FragCanBangLvBinding
import tamhoang.bvn.messageCenter.telegram.TelegramHandle
import tamhoang.bvn.ui.main.MainState
import tamhoang.bvn.util.extensions.toDecimal
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class FragCanBang : Fragment() {
    private var _bind: FragCanBangBinding? = null
    val bind get() = _bind!!

    var db: DbOpenHelper? = null
    var handler: Handler? = null
    var mDate: String? = null
    var mDemso = ArrayList<Int>()
    var mDiem = ArrayList<String>()
    var mThangthua = ArrayList<Double>()
    var mTongTien = ArrayList<Double>()
    private val runnable: Runnable = object : Runnable {
        override fun run() {
            if (TelegramHandle.sms) {
                xemLv()
                TelegramHandle.sms = false
            }
            handler!!.postDelayed(this, 1000)
        }
    }
    var so = 0
    var thLoai = "the_loai = 'deb'"
    override fun onCreateView(layoutInflater: LayoutInflater, viewGroup: ViewGroup?, bundle: Bundle?): View {
        super.onCreate(bundle)
        _bind = FragCanBangBinding.inflate(layoutInflater)
        db = DbOpenHelper(activity!!)
        mDate = LocalDate.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        bind.lvLivestrem.onItemClickListener = OnItemClickListener { _, _, _, _ -> }
        bind.switchX.setOnCheckedChangeListener { _, isChecked ->
            bind.liTip.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
        handler = Handler()
        handler!!.postDelayed(runnable, 1000)
        bind.radioDea.setOnCheckedChangeListener { _, isChecked ->
            onCheckedChanged("dea", isChecked)
        }
        bind.radioDeb.setOnCheckedChangeListener { _, isChecked ->
            onCheckedChanged("deb", isChecked)
        }
        bind.radioDec.setOnCheckedChangeListener { _, isChecked ->
            onCheckedChanged("dec", isChecked)
        }
        bind.radioDed.setOnCheckedChangeListener { _, isChecked ->
            onCheckedChanged("ded", isChecked)
        }
        xemLv()
        return bind.root
    }

    private fun onCheckedChanged(theLoai: String, isChecked: Boolean) {
        if (isChecked) {
            thLoai = "the_loai = '$theLoai'"
            xemLv()
        }
    }

    override fun onStop() {
        super.onStop()
        handler!!.removeCallbacks(runnable)
    }

    override fun onDestroy() {
        clearData()
        bind.lvLivestrem.adapter = null
        super.onDestroy()
    }

    private fun clearData() {
        mDiem.clear()
        mDemso.clear()
        mTongTien.clear()
        mThangthua.clear()
    }

    fun xemLv() {
        val decimalFormat = DecimalFormat("###,###")
        val dateYMD = MainState.dateYMD
        val cursorDem =
            db!!.getData("SELECT count(mycount) AS dem FROM (SELECT sum((type_kh = 1) *diem_ton) - sum((type_kh = 2) * diem_ton) AS mycount FROM tbl_soctS WHERE $thLoai AND ngay_nhan = '$dateYMD' GROUP BY so_chon ) a")
        cursorDem.moveToFirst()
        so = cursorDem.getInt(0)
        bind.tvChuY.text = if (so > 0)
            "Có " + (100 - cursorDem.getInt(0)) + " số 0 đồng"
        else
            "Chưa có dữ liệu ngày hôm nay."

        clearData()

        val cursorMoctien = db!!.getData(
            """SELECT moctien, count(moctien) AS dem
                FROM (Select (Sum((tbl_soctS.type_kh =1) * tbl_soctS.diem_ton) - Sum((tbl_soctS.type_kh =2) * tbl_soctS.diem_ton) - so_om.Om_DeB ) as moctien
                From so_om Left Join tbl_soctS On tbl_soctS.so_chon = so_om.So 
                Where tbl_soctS.ngay_nhan='$dateYMD' AND (tbl_soctS.${thLoai} OR tbl_soctS.the_loai='det') 
                GROUP by so_om.So order by moctien DESC) as a 
                GROUP BY moctien ORDER BY moctien DESC"""
        )
        while (cursorMoctien.moveToNext()) {
            mDiem.add(decimalFormat.format(cursorMoctien.getDouble(0)))
            mDemso.add(cursorMoctien.getInt(1))
        }
        if (mDiem.size > 0) {
            for (i in mDiem.indices) {
                val parseInt = mDiem[i].replace(".", "").toInt()
                var d = (parseInt * 100).toDouble()
                if (i < mDiem.size) {
                    for (i2 in i + 1 until mDiem.size) {
                        val parseInt2 =
                            ((parseInt - mDiem[i2].replace(".", "").toInt()) * mDemso[i2]).toDouble()
                        d -= parseInt2
                    }
                }
                val list = mTongTien
                val d2 = ((100 - so) * parseInt).toDouble()
                list.add((d - d2) * 715.0 / 1000.0)
                val list2 = mThangthua
                val d3 = ((100 - so) * parseInt).toDouble()
                val d4 = (parseInt * 70).toDouble()
                list2.add((d - d3) * 715.0 / 1000.0 - d4)
            }
        }
        if (activity != null) {
            bind.lvLivestrem.adapter = TNGAdapter(activity, R.layout.frag_can_bang_lv, mDiem)
        }
        cursorMoctien.close()
        cursorDem.close()
    }

    internal inner class TNGAdapter(context: Context?, i: Int, list: List<String?>?) : ArrayAdapter<Any?>(
        context!!, i, list!!
    ) {
        @SuppressLint("ViewHolder")
        override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {
            val bindLv = FragCanBangLvBinding.inflate(layoutInflater)
            bindLv.tvDiem.text = mDiem[i]
            bindLv.tvSo.text = mDemso[i].toString()
            bindLv.tvTiengiu.text = mTongTien[i].toDecimal()
            bindLv.tvThangThua.text = mThangthua[i].toDecimal()
            return bindLv.root
        }
    }
}