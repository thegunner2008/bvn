package tamhoang.bvn.ui.congNo

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import tamhoang.bvn.R
import tamhoang.bvn.data.DbOpenHelper
import tamhoang.bvn.databinding.FragCongNoBinding
import tamhoang.bvn.databinding.FragCongNoLvBinding
import tamhoang.bvn.messageCenter.telegram.TelegramHandle
import tamhoang.bvn.ui.base.dialog.Dialog
import tamhoang.bvn.ui.main.MainState
import tamhoang.bvn.util.extensions.toDecimal

class FragCongno : Fragment() {
    private var _bind: FragCongNoBinding? = null
    val bind get() = _bind!!

    var db: DbOpenHelper? = null
    var handler: Handler? = null
    var mKhachHang = ArrayList<String>()
    var mNocu = ArrayList<String>()
    var mPhatSinh = ArrayList<String>()
    var mPoistion = 0
    var mSdt = ArrayList<String>()
    var mSoCuoi = ArrayList<String>()
    var mtype = ArrayList<String>()
    var ngayChon: String? = null
    private val runnable: Runnable = object : Runnable {
        override fun run() {
            if (TelegramHandle.sms) {
                xemMoneyLv()
                TelegramHandle.sms = false
            }
            handler!!.postDelayed(this, 1000)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _bind = FragCongNoBinding.inflate(inflater)
        db = DbOpenHelper(activity!!)
        xemMoneyLv()
        ngayChon = MainState.dateYMD
        bind.lvCongNo.onItemLongClickListener =
            OnItemLongClickListener { _, _, position: Int, _ ->
                mPoistion = position
                false
            }
        bind.lvCongNo.setOnItemClickListener { _, view: View, i: Int, _ ->
            mPoistion = i
            itemClick(view)
        }
        val handler2 = Handler()
        handler = handler2
        handler2.postDelayed(runnable, 1000)
        return bind.root
    }

    private fun itemClick(v: View) {
        val popupL = PopupMenu(activity, v)
        arrayOf("Xem phát sinh chi tiết", "Xóa khách này")
            .forEachIndexed { index, s -> popupL.menu.add(1, index, index, s) }

        popupL.show()
        popupL.setOnMenuItemClickListener { menuItem: MenuItem ->
            val currentKhach = mKhachHang[mPoistion]
            if (menuItem.order == 0) {
                val intent = Intent(activity, ActivityCongno::class.java)
                intent.putExtra("tenKH", currentKhach)
                startActivity(intent)
            } else if (menuItem.order == 1) {
                Dialog.simple(
                    this@FragCongno.activity!!,
                    "Xóa hết số liệu khách này?",
                    negativeText = "Cancel",
                    positiveText = "OK",
                    positiveAction = {
                        db!!.queryData("Delete FROM tbl_tinnhanS WHERE ten_kh = '$currentKhach'")
                        db!!.queryData("Delete FROM tbl_soctS WHERE ten_kh = '$currentKhach'")
                        xemMoneyLv()
                        Toast.makeText(this@FragCongno.activity, "Xoá thành công", Toast.LENGTH_LONG).show()
                    }
                )
            }
            return@setOnMenuItemClickListener false
        }
    }

    override fun onStop() {
        super.onStop()
        handler!!.removeCallbacks(runnable)
    }

    override fun onDestroy() {
        try {
            clearLv()
        } catch (e: Exception) {
        }
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        xemMoneyLv()
    }

    private fun clearLv() {
        mKhachHang.clear()
        mSdt.clear()
        mNocu.clear()
        mPhatSinh.clear()
        mSoCuoi.clear()
        mtype.clear()
    }

    fun xemMoneyLv() {
        var tienNo = 0.0
        var tienPS = 0.0
        var tienCuoi = 0.0
        clearLv()
        val mDate = MainState.dateYMD
        val cursor =
            db!!.getData("Select tbl_soctS.ten_kh\n, SUM((tbl_soctS.ngay_nhan < '$mDate') * tbl_soctS.ket_qua * (100-tbl_soctS.diem_khachgiu)/100)/1000  as NoCu   \n, SUM((tbl_soctS.ngay_nhan = '$mDate') * tbl_soctS.ket_qua * (100-tbl_soctS.diem_khachgiu)/100)/1000  as PhatSinh   \n, SUM((tbl_soctS.ngay_nhan <= '$mDate')*tbl_soctS.ket_qua*(100-tbl_soctS.diem_khachgiu)/100)/1000 as SoCuoi, tbl_soctS.so_dienthoai, tbl_kh_new.type_kh  \nFROM tbl_soctS INNER JOIN tbl_kh_new ON tbl_soctS.so_dienthoai = tbl_kh_new.sdt\nGROUP BY tbl_soctS.ten_kh ORDER BY tbl_soctS.type_kh DESC")
        while (cursor.moveToNext()) {
            mKhachHang.add(cursor.getString(0))
            mSdt.add(cursor.getString(4))
            mNocu.add(cursor.getDouble(1).toDecimal())
            mPhatSinh.add(cursor.getDouble(2).toDecimal())
            mSoCuoi.add(cursor.getDouble(3).toDecimal())
            mtype.add(cursor.getString(5))
            tienNo += cursor.getDouble(1)
            tienPS += cursor.getDouble(2)
            tienCuoi += cursor.getDouble(3)
        }
        bind.TienNoCu.text = (-tienNo).toDecimal()
        bind.TienPhatSinh.text = (-tienPS).toDecimal()
        bind.TienSoCuoi.text = (-tienCuoi).toDecimal()
        if (activity != null) {
            bind.lvCongNo.adapter =
                MoneyReport(activity, R.layout.frag_cong_no_lv, mKhachHang)
        }
    }

    inner class MoneyReport(context: Context?, resource: Int, objects: List<String?>?) : ArrayAdapter<Any?>(
        context!!, resource, objects!!
    ) {
        @SuppressLint("ResourceAsColor", "ViewHolder")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val bindLv = FragCongNoLvBinding.inflate(layoutInflater)
            bindLv.tvKhachHang.text = mKhachHang[position]
            bindLv.tvNocu.text = mNocu[position]
            bindLv.tvPhatsinh.text = mPhatSinh[position]
            bindLv.tvTienton.text = mSoCuoi[position]
            if (!mtype[position].contains("1")) {
                bindLv.tvKhachHang.setTextColor(R.color.mtrl_scrim_color)
                bindLv.tvNocu.setTextColor(R.color.mtrl_scrim_color)
                bindLv.tvPhatsinh.setTextColor(R.color.mtrl_scrim_color)
                bindLv.tvTienton.setTextColor(R.color.mtrl_scrim_color)
            }
            return bindLv.root
        }
    }
}