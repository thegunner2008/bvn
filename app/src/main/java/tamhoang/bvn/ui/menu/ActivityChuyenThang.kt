package tamhoang.bvn.ui.menu

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import tamhoang.bvn.R
import tamhoang.bvn.data.DbOpenHelper
import tamhoang.bvn.data.model.KhachHang
import tamhoang.bvn.data.store.KhachHangStore
import tamhoang.bvn.databinding.ActivityChuyenthangBinding
import tamhoang.bvn.databinding.ActivityChuyenthangLvBinding
import tamhoang.bvn.ui.base.toolbar.BaseToolBarActivity
import tamhoang.bvn.util.extensions.toDecimal

class ActivityChuyenThang : BaseToolBarActivity() {
    private var _bind: ActivityChuyenthangBinding? = null
    val bind get() = _bind!!

    var db: DbOpenHelper? = null
    var listKhachAll = ArrayList<KhachHang>()
    private val tenKhachAll: List<String>
        get() = listKhachAll.map { it.ten_kh }
    val sdtKhachAll: List<String>
        get() = listKhachAll.map { it.sdt }

    var listChuAll = ArrayList<KhachHang>()
    private val tenChuAll: List<String>
        get() = listChuAll.map { it.ten_kh }
    val sdtChuAll: List<String>
        get() = listChuAll.map { it.sdt }

    var iChu = 0
    var iKH = 0

    var sdtChu = ArrayList<String>()
    var sdtKh = ArrayList<String>()
    var tenChu = ArrayList<String>()
    var tenKh = ArrayList<String>()
    override fun getLayoutId(): Int {
        return R.layout.activity_chuyenthang
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _bind = ActivityChuyenthangBinding.inflate(layoutInflater)
        setContentView(bind.root)
        db = DbOpenHelper(this)

        listKhachAll = KhachHangStore.I.selectListWhere("type_kh = 1 ORDER by ten_kh")
        bind.spinterKH.adapter = ArrayAdapter(this, R.layout.spinner_item, listKhachAll.map { it.ten_kh })
        bind.spinterKH.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, position: Int, id: Long) {
                iKH = position
                val khach = KhachHangStore.I.selectBySdt(sdtKhachAll[position]) ?: return
                bind.spinterChu.setSelection(sdtChuAll.indexOf(khach.sdt))
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }

        listChuAll = KhachHangStore.I.selectListWhere("type_kh <> 1 ORDER by ten_kh")
        bind.spinterChu.adapter = ArrayAdapter(this, R.layout.spinner_item, listChuAll.map { it.ten_kh })
        bind.spinterChu.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, position: Int, id: Long) {
                iChu = position
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
        bind.addChuyenthang.setOnClickListener {
            try {
                val cursor = db!!.getData("Select * From tbl_chuyenthang WHERE kh_nhan = '" + tenKhachAll[iKH] + "'")
                cursor.moveToFirst()
                if (cursor.count != 0 || iChu <= -1) {
                    db!!.queryData("UPDATE tbl_chuyenthang set kh_chuyen = '${tenChuAll[iChu]}', sdt_chuyen = '${sdtChuAll[iChu]}' Where sdt_nhan = '${sdtKhachAll[iKH]}'")
                    Toast.makeText(this@ActivityChuyenThang, "Đã sửa!", Toast.LENGTH_LONG).show()
                } else {
                    db!!.queryData("Insert into tbl_chuyenthang Values (null, '${tenKhachAll[iKH]}', '${sdtKhachAll[iKH]}', '${tenChuAll[iChu]}', '${sdtChuAll[iChu]}')")
                    Toast.makeText(this@ActivityChuyenThang, "Đã thêm!", Toast.LENGTH_LONG).show()
                }
                cursor.close()
            } catch (e: Exception) {
                Toast.makeText(this@ActivityChuyenThang, "Thêm lỗi!", Toast.LENGTH_LONG).show()
            }
            xemLv()
        }
        bind.lvChuyenThang.onItemClickListener =
            OnItemClickListener { _, _, position, _ ->
                val indexKH = tenKhachAll.indexOf(tenKh[position])
                val indexChu = tenChuAll.indexOf(tenChu[position])
                bind.spinterKH.setSelection(indexKH)
                bind.spinterChu.setSelection(indexChu)
            }

        bind.radChuyenngay.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                db!!.queryData("UPDATE So_Om set Om_Xi3 = 0 WHERE ID = 13")
            }
        }
        bind.radChuyensauxl.setOnCheckedChangeListener { _, isChecked: Boolean ->
            if (isChecked) {
                db!!.queryData("UPDATE So_Om set Om_Xi3 = 1 WHERE ID = 13")
            }
        }
        val chuyenthang = db!!.getData("Select Om_Xi3 From so_om WHERE id = 13")
        if (chuyenthang.moveToFirst()) {
            if (chuyenthang.getInt(0) == 0) {
                bind.radChuyenngay.isChecked = true
                bind.radChuyensauxl.isChecked = false
            } else {
                bind.radChuyensauxl.isChecked = true
                bind.radChuyenngay.isChecked = false
            }
            if (!chuyenthang.isClosed) {
                chuyenthang.close()
            }
        }
        xemLv()
    }

    fun xemLv() {
        tenKh.clear()
        sdtKh.clear()
        tenChu.clear()
        sdtChu.clear()
        val cursor = db!!.getData("Select * From tbl_chuyenthang")
        while (cursor.moveToNext()) {
            tenKh.add(cursor.getString(1))
            sdtKh.add(cursor.getString(2))
            tenChu.add(cursor.getString(3))
            sdtChu.add(cursor.getString(4))
        }
        cursor.close()
        bind.lvChuyenThang.adapter = CTAdapter(this, R.layout.activity_chuyenthang_lv, tenKh)
    }

    inner class CTAdapter(context: Context?, resource: Int, objects: List<String>?) : ArrayAdapter<Any?>(
        context!!, resource, objects!!
    ) {
        @SuppressLint("ViewHolder")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val bindLv = ActivityChuyenthangLvBinding.inflate(layoutInflater)
            val stt = (position + 1).toDecimal()
            bindLv.tvStt.text = stt
            bindLv.tvKhach.text = tenKh[position]
            bindLv.tvChu.text = tenChu[position]
            bindLv.tvDelete.setOnClickListener {
                val database = db
                database!!.queryData("Delete FROM tbl_chuyenthang WHERE sdt_nhan = '${sdtKh[position]}'")
                xemLv()
            }
            return bindLv.root
        }
    }
}