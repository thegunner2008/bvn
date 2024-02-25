package tamhoang.bvn.ui.menu

import android.os.Bundle
import android.widget.Toast
import tamhoang.bvn.R
import tamhoang.bvn.constants.Const.KHONG_HIEU
import tamhoang.bvn.data.DbOpenHelper
import tamhoang.bvn.databinding.ActivityGiuSoBinding
import tamhoang.bvn.ui.base.toolbar.BaseToolBarActivity
import tamhoang.bvn.ui.khachHang.KhachHangController
import tamhoang.bvn.util.extensions.convertLatin

class ActivityGiuSo : BaseToolBarActivity() {
    private var _bind: ActivityGiuSoBinding? = null
    private val bind get() = _bind!!
    var db: DbOpenHelper? = null
    private val controller = KhachHangController()

    override fun getLayoutId(): Int {
        return R.layout.activity_giu_so
    }

    private fun checkCurrentType(): Type? {
        return if (bind.radioDeB.isChecked) Type.DE_B
        else if (bind.radioDeA.isChecked) Type.DE_A
        else if (bind.radioDeC.isChecked) Type.DE_C
        else if (bind.radioDeD.isChecked) Type.DE_D
        else if (bind.radioLo.isChecked) Type.LO else null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _bind = ActivityGiuSoBinding.inflate(layoutInflater)
        setContentView(bind.root)
        db = DbOpenHelper(this)
        fun textNhapDan() = bind.edtNhapDan.text.toString()

        bind.btnThemOm.setOnClickListener {
            var ktra = true
            var phanTich = "de " + textNhapDan()
            if (phanTich.length > 7) {
                try {
                    phanTich = controller.parseSo(phanTich.convertLatin())
                        .replace("de dit db:", "de:")
                    if (phanTich.contains(KHONG_HIEU)) {
                        Toast.makeText(this@ActivityGiuSo, phanTich, Toast.LENGTH_LONG).show()
                        ktra = false
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@ActivityGiuSo, "Thêm bị lỗi, hãy sửa lại", Toast.LENGTH_LONG).show()
                    ktra = false
                }
            }
            if (ktra) {
                Toast.makeText(this@ActivityGiuSo, "Đã sửa dàn giữ!", Toast.LENGTH_LONG).show()
                val type = checkCurrentType()
                if (type != null && phanTich.length > 7) {
                    db!!.queryData("Update So_om Set " + type.colunm + " =0")
                    db!!.queryData("UPDATE So_om SET Sphu1 = '" + textNhapDan() + "' WHERE ID = " + type.ID)
                    do {
                        val line = phanTich.substring(0, phanTich.indexOf("\n") + 1) // 'de: 11,22,33 x 100'
                        val daysoTien =
                            phanTich.substring(line.indexOf(":") + 1, line.indexOf("\n") + 1) // '11,22,33 x 100'
                        val so_arr = daysoTien.substring(0, daysoTien.indexOf(",x")).split(",")
                            .toTypedArray() // ['11','22','33']
                        val tien = line.substring(line.indexOf(",x") + 2, line.indexOf("\n")) // '100'
                        for (so in so_arr) {
                            db!!.queryData("Update So_om Set " + type.colunm + " = " + type.colunm + " +" + tien + " WHERE So = '" + so + "'")
                        }
                        phanTich = phanTich.replace(line.toRegex(), "")
                    } while (phanTich.isNotEmpty())
                }
            }
        }
        bind.btnXoa.setOnClickListener {
            val type = checkCurrentType()
            if (type != null) {
                db!!.queryData("UPdate so_Om set " + type.colunm + " = 0")
                db!!.queryData("UPDATE So_om SET Sphu1 = null WHERE ID = " + type.ID)
            }
            bind.edtNhapDan.setText("")
            Toast.makeText(this@ActivityGiuSo, "Đã xóa dàn giữ!", Toast.LENGTH_LONG).show()
        }
        bind.btnGiuXien.setOnClickListener {
            var mXien2 = 0
            var mXien3 = 0
            var mXien4 = 0
            var m3Cang = 0
            if (bind.giuxien2.text.toString().isNotEmpty()) {
                mXien2 = bind.giuxien2.text.toString().toInt()
            }
            if (bind.giuxien3.text.toString().isNotEmpty()) {
                mXien3 = bind.giuxien3.text.toString().toInt()
            }
            if (bind.giuxien4.text.toString().isNotEmpty()) {
                mXien4 = bind.giuxien4.text.toString().toInt()
            }
            if (bind.giu3cang.text.toString().isNotEmpty()) {
                m3Cang = bind.giu3cang.text.toString().toInt()
            }
            db!!.queryData("Update So_om Set Om_Xi2 = $mXien2, Om_Xi3 = $mXien3, Om_Xi4 = $mXien4, Om_bc = $m3Cang WHERE ID = 1")
            Toast.makeText(this@ActivityGiuSo, "Đã lưu giữ xiên/càng!", Toast.LENGTH_LONG).show()
        }
        bind.btnXoaXien.setOnClickListener {
            db!!.queryData("Update So_om Set Om_Xi2 = 0, Om_Xi3 = 0, Om_Xi4 = 0, Om_bc = 0 WHERE ID = 1")
            Toast.makeText(this@ActivityGiuSo, "Đã xóa giữ xiên/càng!", Toast.LENGTH_LONG).show()
            bind.giuxien2.setText("")
            bind.giuxien3.setText("")
            bind.giuxien4.setText("")
            bind.giu3cang.setText("")
        }
        bind.radioDeA.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                val sphu = db!!.getOneData<String?>("So_om", "ID = 20", "Sphu1")
                if (!sphu.isNullOrEmpty()) {
                    bind.edtNhapDan.setText(sphu)
                }
            }
        }
        bind.radioDeB.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                val sphu = db!!.getOneData<String?>("So_om", "ID = 21", "Sphu1")
                if (!sphu.isNullOrEmpty()) {
                    bind.edtNhapDan.setText(sphu)
                }
            }
        }
        bind.radioDeC.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                val sphu = db!!.getOneData<String?>("So_om", "ID = 22", "Sphu1")
                if (!sphu.isNullOrEmpty()) {
                    bind.edtNhapDan.setText(sphu)
                }
            }
        }
        bind.radioDeD.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                val sphu = db!!.getOneData<String?>("So_om", "ID = 23", "Sphu1")
                if (!sphu.isNullOrEmpty()) {
                    bind.edtNhapDan.setText(sphu)
                }
            }
        }
        bind.radioLo.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                val sphu = db!!.getOneData<String?>("So_om", "ID = 24", "Sphu1")
                if (!sphu.isNullOrEmpty()) {
                    bind.edtNhapDan.setText(sphu)
                }
            }
        }
        val sphu = db!!.getOneData<String?>("So_om", "ID = 21", "Sphu1")
        if (!sphu.isNullOrEmpty()) {
            bind.edtNhapDan.setText(sphu)
        }
        val cursor1 = db!!.getData("Select * From so_om WHERE id = 1")
        if (cursor1.moveToFirst()) {
            bind.giuxien2.setText(cursor1.getString(7))
            bind.giuxien3.setText(cursor1.getString(8))
            bind.giuxien4.setText(cursor1.getString(9))
            bind.giu3cang.setText(cursor1.getString(10))
            if (!cursor1.isClosed) {
                cursor1.close()
            }
        }
    }

    internal enum class Type(var colunm: String, var ID: Int) {
        DE_A("Om_DeA", 20), DE_B("Om_DeB", 21), DE_C("Om_DeC", 22), DE_D("Om_DeD", 23), LO("Om_Lo", 24);
    }
}