package tamhoang.bvn.ui.chat

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.Html
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ArrayAdapter
import android.widget.Toast
import org.json.JSONException
import org.json.JSONObject
import tamhoang.bvn.R
import tamhoang.bvn.constants.Const.KHONG_HIEU
import tamhoang.bvn.constants.Const.LDPRO
import tamhoang.bvn.data.DbOpenHelper
import tamhoang.bvn.data.services.xuLyTinNhan.XuLyTinNhanService
import tamhoang.bvn.data.store.TinNhanStore
import tamhoang.bvn.databinding.ActivityTinnhanBinding
import tamhoang.bvn.databinding.FragSuatinLv1Binding
import tamhoang.bvn.ui.base.toolbar.BaseToolBarActivity

class ActivityTinnhan : BaseToolBarActivity() {
    private var _bind: ActivityTinnhanBinding? = null
    private val bind get() = _bind!!

    var db: DbOpenHelper? = null
    var id: String? = ""
    var json: JSONObject? = null
    var lvPosition = -1
    private val mDanGoc = ArrayList<String>()
    private val mPhantich = ArrayList<String>()
    var ngayNhan = ""
    private var soTN = 0
    var tenKH = ""
    var typeKH = 0

    override fun getLayoutId(): Int {
        return R.layout.activity_tinnhan
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _bind = ActivityTinnhanBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_tinnhan)
        db = DbOpenHelper(this)
        id = intent.getStringExtra("m_ID") ?: return

        val tinNhan = TinNhanStore.I.selectByID(id!!) ?: return
        if (tinNhan.useApp.contains("ChayTrang")) {
            Toast.makeText(this, "Không sửa được tin chạy vào trang", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        ngayNhan = tinNhan.ngayNhan
        tenKH = tinNhan.tenKh
        soTN = tinNhan.soTinNhan
        typeKH = tinNhan.typeKh
        if (tinNhan.phatHienLoi.contains("ok")) {
            try {
                mDanGoc.clear()
                mPhantich.clear()
                json = JSONObject(tinNhan.phanTich ?: return)
                bind.edtSuatin.setText(tinNhan.ndSua)
                val keys = json!!.keys()
                while (keys.hasNext()) {
                    val dan = json!!.getJSONObject(keys.next())
                    val list = mDanGoc
                    list.add(dan.getString("du_lieu") + " (" + dan.getString("so_luong") + ")")
                    val list2 = mPhantich
                    list2.add(dan.getString("dan_so") + "x" + dan.getString("so_tien"))
                }
                bind.lvSuatin.adapter = TnAdapter(this, R.layout.frag_suatin_lv1, mDanGoc)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        } else {
            bind.edtSuatin.setText(tinNhan.ndSua)
        }
        @SuppressLint("WrongConstant") val imm = getSystemService("input_method") as InputMethodManager
        var view = currentFocus
        if (view == null) {
            view = View(this)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
        bind.btnSuatin.setOnClickListener {
            db!!.queryData("DELETE FROM tbl_soctS WHERE ngay_nhan = '$ngayNhan' AND ten_kh = '$tenKH'  AND so_tin_nhan = $soTN And type_kh = $typeKH")
            val sb =
                "Update tbl_tinnhanS Set nd_phantich = '${bind.edtSuatin.text}', phat_hien_loi = 'ko' WHERE id = $id"
            db!!.queryData(sb)
            try {
                XuLyTinNhanService.I.upsertTinNhan(id!!.toInt(), typeKH)
            } catch (e: Exception) {
                Toast.makeText(this, "Đã xảy ra lỗi!", Toast.LENGTH_LONG).show()
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
            }
            val tinNhan1 = TinNhanStore.I.selectByID(id!!) ?: return@setOnClickListener
            if (tinNhan1.phatHienLoi.contains(KHONG_HIEU)) {
                bind.edtSuatin.setText(Html.fromHtml(tinNhan1.ndPhanTich.replace(LDPRO, "<font color='#FF0000'>")))
                if (tinNhan1.ndPhanTich.contains(LDPRO)) {
                    bind.edtSuatin.setSelection(tinNhan1.ndPhanTich.indexOf(LDPRO))
                }
                mDanGoc.clear()
                mPhantich.clear()
                bind.lvSuatin.adapter = TnAdapter(this, R.layout.frag_suatin_lv1, mDanGoc)
                return@setOnClickListener
            }
            bind.edtSuatin.setText(tinNhan1.ndSua)
            mDanGoc.clear()
            mPhantich.clear()
            try {
                json = JSONObject(tinNhan1.phanTich ?: return@setOnClickListener)
                val keys = json!!.keys()
                while (keys.hasNext()) {
                    val dan = json!!.getJSONObject(keys.next())
                    val list = mDanGoc
                    list.add(dan.getString("du_lieu") + " (" + dan.getString("so_luong") + ")")
                    val list2 = mPhantich
                    list2.add(dan.getString("dan_so") + "x" + dan.getString("so_tien"))
                }
                bind.lvSuatin.adapter = TnAdapter(this@ActivityTinnhan, R.layout.frag_suatin_lv1, mDanGoc)
            } catch (e2: JSONException) {
                e2.printStackTrace()
            }
        }
        bind.lvSuatin.onItemLongClickListener =
            OnItemLongClickListener { _, _, position: Int, _ ->
                lvPosition = position
                false
            }
        bind.btnSuatinXoatin.setOnClickListener { finish() }
        registerForContextMenu(bind.lvSuatin)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu.add(0, 1, 0, "Copy ?")
    }

    @SuppressLint("WrongConstant")
    override fun onContextItemSelected(item: MenuItem): Boolean {
        super.onContextItemSelected(item)
        if (item.itemId == 1) {
            (getSystemService("clipboard") as ClipboardManager).setPrimaryClip(
                ClipData.newPlainText(
                    "Tin chốt:",
                    "${mDanGoc[lvPosition]}\n${mPhantich[lvPosition]}"
                )
            )
            Toast.makeText(this, "Đã copy thành công", Toast.LENGTH_LONG).show()
        }
        return true
    }

    internal inner class TnAdapter(context: Context?, resource: Int, objects: List<String?>?) : ArrayAdapter<Any?>(
        context!!, resource, objects!!
    ) {
        @SuppressLint("ViewHolder")
        override fun getView(position: Int, v: View?, parent: ViewGroup): View {
            val bindLv = FragSuatinLv1Binding.inflate(layoutInflater)
            bindLv.danGoc.text = mDanGoc[position]
            bindLv.danGoc.setOnClickListener {
                if (bindLv.danPhantich.visibility == View.VISIBLE) {
                    bindLv.danPhantich.visibility = View.GONE
                } else {
                    bindLv.danPhantich.visibility = View.VISIBLE
                }
            }
            bindLv.danPhantich.text = mPhantich[position]
            bindLv.danPhantich.visibility = View.GONE
            return bindLv.root
        }
    }
}