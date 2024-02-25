package tamhoang.bvn.ui.khachHang

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import tamhoang.bvn.R
import tamhoang.bvn.data.DbOpenHelper
import tamhoang.bvn.databinding.FragKhachHangBinding
import tamhoang.bvn.databinding.FragKhachHangLvBinding
import tamhoang.bvn.ui.base.dialog.Dialog
import tamhoang.bvn.ui.main.MainState

class FragKhachHang : Fragment() {
    private var _bind: FragKhachHangBinding? = null
    private val bind get() = _bind!!

    var db: DbOpenHelper? = null
    var mAddress = ArrayList<String>()
    var mAppuse = ArrayList<String>()
    var mDate = ArrayList<String>()
    var mPerson = ArrayList<String>()
    var mtype = ArrayList<Int>()
    var mPoistion = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _bind = FragKhachHangBinding.inflate(inflater)
        db = DbOpenHelper(activity!!)
        bind.btnThemKH.setOnClickListener {
            val intent = Intent(activity, ActivityAddKH::class.java)
            intent.putExtra("tenKH", "")
            intent.putExtra("use_app", "sms")
            intent.putExtra("kh_new", "")
            startActivity(intent)
        }
        bind.lvSetting1.onItemClickListener =
            OnItemClickListener { _, view: View?, position: Int, _ ->
                mPoistion = position
                bind.lvSetting1.showContextMenuForChild(view)
            }
        bind.lvSetting1.onItemLongClickListener =
            OnItemLongClickListener { _, _, position: Int, _ ->
                mPoistion = position
                false
            }
        xemLv()
        registerForContextMenu(bind.lvSetting1)
        return bind.root
    }

    override fun onResume() {
        xemLv()
        super.onResume()
    }

    override fun onCreateContextMenu(menu: ContextMenu, v2: View, menuInfo: ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v2, menuInfo)
        menu.add(0, 1, 0, "Cài đặt lại giá")
        menu.add(0, 2, 0, "Cài đặt thời gian, giữ %")
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        super.onContextItemSelected(item)
        if (item.itemId == 1) {
            val intent = Intent(activity, ActivityAddKH::class.java).apply {
                putExtra("tenKH", mPerson[mPoistion])
                putExtra("kh_new", "")
                putExtra("use_app", mAppuse[mPoistion])
            }
            startActivity(intent)
        } else if (item.itemId == 2) {
            val intent = Intent(activity, ActivityCaiDatKhach::class.java)
            intent.putExtra("tenKH", mPerson[mPoistion])
            startActivity(intent)
        }
        return true
    }

    fun xemLv() {
        mAddress.clear()
        mPerson.clear()
        mtype.clear()
        mAppuse.clear()
        val cursor = db!!.getData("select * from tbl_kh_new Order by type_kh DESC, ten_kh")
        while (cursor.moveToNext()) {
            mPerson.add(cursor.getString(0))
            mAddress.add(cursor.getString(1))
            mtype.add(cursor.getInt(3))
            mAppuse.add(cursor.getString(2))
        }
        if (!cursor.isClosed) cursor.close()
        if (activity != null) {
            bind.lvSetting1.adapter = KHAdapter(
                activity,
                R.layout.frag_khach_hang_lv,
                mPerson
            )
        }
    }

    inner class KHAdapter(context: Context?, resource: Int, objects: List<String?>?) : ArrayAdapter<Any?>(
        context!!, resource, objects!!
    ) {
        @SuppressLint("ViewHolder")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val bindLv = FragKhachHangLvBinding.inflate(layoutInflater)

            bindLv.st1Tenkh.text = mPerson[position]
            bindLv.st1Sdt.text = mAddress[position]
            bindLv.root.setOnClickListener { view: View? ->
                mPoistion = position
                bind.lvSetting1.showContextMenuForChild(view)
            }
            bindLv.root.setOnLongClickListener {
                mPoistion = position
                false
            }
            bindLv.tvXoaKH.setOnClickListener {
                Dialog.simple(
                    activity!!,
                    "Xoá Khách",
                    "Xoá bỏ ${mPerson[position]} ra khỏi danh sách?",
                    negativeText = "Có",
                    negativeAction = {
                        db!!.queryData("Delete FROM tbl_kh_new where ten_kh = '${mPerson[position]}'")
                        db!!.queryData("Delete FROM tbl_tinnhanS where ten_kh = '${mPerson[position]}'")
                        db!!.queryData("Delete FROM tbl_soctS where ten_kh = '${mPerson[position]}'")
                        db!!.queryData("Delete FROM tbl_chuyenthang where kh_nhan = '${mPerson[position]}'")
                        db!!.queryData("Delete FROM tbl_chuyenthang where kh_chuyen = '${mPerson[position]}'")
                        MainState.refreshDsKhachHang()
                        xemLv()
                        Toast.makeText(activity, "Xoá thành công!", Toast.LENGTH_LONG).show()
                    },
                    positiveText = "Không"
                )
            }
            if (mtype[position] != 1) {
                bindLv.st1Tenkh.setTextColor(-16776961)
            }
            return bindLv.root
        }
    }
}