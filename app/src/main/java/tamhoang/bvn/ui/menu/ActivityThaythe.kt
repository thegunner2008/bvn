package tamhoang.bvn.ui.menu

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import tamhoang.bvn.R
import tamhoang.bvn.data.DbOpenHelper
import tamhoang.bvn.data.store.ThayTheStore
import tamhoang.bvn.databinding.ActivityThaytheBinding
import tamhoang.bvn.databinding.ActivityThaytheLvBinding
import tamhoang.bvn.ui.base.toolbar.BaseToolBarActivity
import tamhoang.bvn.util.extensions.toDecimal

class ActivityThaythe : BaseToolBarActivity() {
    private var _bind: ActivityThaytheBinding? = null
    val bind get() = _bind!!

    var db: DbOpenHelper? = null
    private var listNoidung = listOf<String>()
    private var listThaythe = listOf<String>()

    override fun getLayoutId(): Int {
        return R.layout.activity_thaythe
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _bind = ActivityThaytheBinding.inflate(layoutInflater)
        setContentView(bind.root)
        bind.btnLuu.setOnClickListener {
            val textOrigin = bind.tvThaythe.text.toString()
            val textReplace = bind.tvNdThaythe.text.toString()
            val cursor =
                db!!.getData("Select count(id) From thay_the_phu WHERE str = '$textOrigin'")
            cursor.moveToFirst()
            if (cursor.getInt(0) == 0) {
                db!!.queryData("Insert into thay_the_phu values (null, '$textOrigin', '$textReplace')")
            } else {
                val database = db
                database!!.queryData("Update thay_the_phu set str_rpl = '$textReplace' WHERE str = '$textOrigin'")
            }
            bind.tvThaythe.setText("")
            bind.tvNdThaythe.setText("")
            listviewThaythe()
        }
        bind.lvThaythe.onItemClickListener = OnItemClickListener { _, _, position, _ ->
            bind.tvThaythe.setText(listNoidung[position])
            bind.tvNdThaythe.setText(listThaythe[position])
        }
        db = DbOpenHelper(this)
        listviewThaythe()
    }

    private fun listviewThaythe() {
        val list = ThayTheStore.I.getList()
        listNoidung = list.map { it.first }
        listThaythe = list.map { it.second }
        bind.lvThaythe.adapter = ThaytheAdapter(this, R.layout.activity_thaythe_lv, listNoidung)
    }

    inner class ThaytheAdapter(context: Context?, resource: Int, objects: List<String?>?) : ArrayAdapter<Any?>(
        context!!, resource, objects!!
    ) {
        @SuppressLint("ViewHolder")
        override fun getView(position: Int, v: View?, parent: ViewGroup): View {
            val bindLv = ActivityThaytheLvBinding.inflate(layoutInflater)
            val stt = (position + 1).toDecimal()
            bindLv.tvStt.text = stt
            bindLv.tvCumtu.text = listNoidung[position]
            bindLv.tvThaybang.text = listThaythe[position]
            bindLv.tvDelete.setOnClickListener {
                db!!.queryData("Delete From thay_the_phu WHERE str = '${listNoidung[position]}'")
                listviewThaythe()
            }
            return bindLv.root
        }
    }
}