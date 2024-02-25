package tamhoang.bvn.ui.tinNhan.suaTin

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import android.widget.AdapterView
import android.widget.AdapterView.*
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import org.greenrobot.eventbus.EventBus
import org.json.JSONException
import tamhoang.bvn.R
import tamhoang.bvn.constants.Const.KHONG_HIEU
import tamhoang.bvn.constants.Const.LDPRO
import tamhoang.bvn.data.BaseStore.getIntField
import tamhoang.bvn.data.BusEvent.SetupErrorBagde
import tamhoang.bvn.data.DbOpenHelper
import tamhoang.bvn.data.model.KhachHang
import tamhoang.bvn.data.model.TinNhanS
import tamhoang.bvn.data.services.guiTinNhan.GuiTinNhanService
import tamhoang.bvn.data.services.xuLyTinNhan.XuLyTinNhanService
import tamhoang.bvn.data.store.KhachHangStore
import tamhoang.bvn.data.store.TinNhanStore
import tamhoang.bvn.databinding.FragSuatinBinding
import tamhoang.bvn.databinding.FragSuatinLvBinding
import tamhoang.bvn.messageCenter.telegram.TelegramHandle
import tamhoang.bvn.ui.base.dialog.Dialog
import tamhoang.bvn.ui.main.MainState
import tamhoang.bvn.util.ld.CongThuc.checkDate
import tamhoang.bvn.util.ld.CongThuc.checkTime
import java.text.ParseException
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class FragSuaTin : Fragment() {
    private lateinit var controller: SuaTinController
    private var _bind: FragSuatinBinding? = null
    private val bind get() = _bind!!

    var mDate: String? = null
    var ngayNow: String? = null
    var db: DbOpenHelper? = null
    var error = false
    var handler: Handler? = null
    var lvPosition = -1
    private var tngAdapter: TNGAdapter? = null
    var mKhachHang: List<KhachHang> = ArrayList()
    var mNameKhach: List<String> = ArrayList()
    var mTinNhanS: List<TinNhanS?> = ArrayList()
    fun tinNhanS() =
        if (mTinNhanS.size > lvPosition) mTinNhanS[lvPosition] else null

    private val runnable: Runnable = object : Runnable {
        override fun run() {
            if (TelegramHandle.sms) {
                resetLv()
                TelegramHandle.sms = false
            }
            handler!!.postDelayed(this, 1000)
        }
    }
    var spinPosition = -1
    var khachHang: KhachHang? = null
    private var typeKh = 0

    private val xulyTinnhan: Runnable = object : Runnable {
        @SuppressLint("WrongConstant")
        override fun run() {
            error = true
            if (bind.edtSuatin.text.toString().length < 6) {
                error = false
            } else if (lvPosition < 0 || !checkDate(MainState.hanSuDung)) {
                error = false
                if (!checkDate(MainState.hanSuDung) || MainState.tenAcc().isEmpty()) {
                    Dialog.hetHSD(activity!!)
                } else {
                    addTin()
                }
            } else {
                suaTin()
            }
            if (!error)
                handler!!.removeCallbacks(this)
            else
                handler!!.postDelayed(this, 300)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _bind = FragSuatinBinding.inflate(inflater)

        db = context?.let { DbOpenHelper(it) }
        controller = SuaTinController(db!!, activity!!)

        handler = Handler()
        handler!!.postDelayed(runnable, 1000)
        val mDate = MainState.dateYMD
        bind.btnSuatin.setOnClickListener {
            this.mDate = MainState.dateYMD
            ngayNow = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            handler = Handler()
            handler!!.postDelayed(xulyTinnhan, 300)
        }
        tngAdapter = TNGAdapter(activity, R.layout.frag_suatin_lv)
        bind.lvSuatin.adapter = tngAdapter
        bind.lvSuatin.onItemClickListener =
            OnItemClickListener { _, _, i: Int, _ ->
                lvPosition = i
                bind.edtSuatin.setText(
                    Html.fromHtml(
                        tinNhanS()!!.ndPhanTich.replace(
                            LDPRO,
                            "<font color='#FF0000'>"
                        )
                    )
                )
                val kk = tinNhanS()!!.ndPhanTich.indexOf(LDPRO)
                if (kk > -1) {
                    try {
                        bind.edtSuatin.setSelection(kk)
                    } catch (ignored: Exception) {
                    }
                }
                bind.sprKH.setSelection(mNameKhach.indexOf(tinNhanS()!!.tenKh))
                tngAdapter!!.notifyDataSetChanged()
            }
        bind.lvSuatin.onItemLongClickListener =
            OnItemLongClickListener { _, _, position: Int, _ ->
                lvPosition = position
                false
            }
        try {
            mKhachHang = KhachHangStore.I.selectListQuery("Order by type_kh, ten_kh")
            mNameKhach = mKhachHang.map { it.ten_kh }
            bind.sprKH.adapter = ArrayAdapter<String>(
                activity!!,
                R.layout.spinner_item,
                mKhachHang.map { it.ten_kh })

            if (mKhachHang.isNotEmpty()) {
                bind.sprKH.setSelection(0)
            }
        } catch (e: Exception) {
            Toast.makeText(activity, "Đang copy dữ liệu bản mới!", Toast.LENGTH_SHORT).show()
        }
        bind.sprKH.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                spinPosition = position
                if (mKhachHang.size > position) khachHang = mKhachHang[position]
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
        bind.radioSuaTin.setOnCheckedChangeListener { _, _ -> controlRadioButton() }
        bind.radioTaiTin.setOnCheckedChangeListener { _, _ -> controlRadioButton() }
        bind.btnLoadtin.setOnClickListener { _ ->
            if (khachHang == null || mKhachHang.isEmpty()) {
                Toast.makeText(activity, "Chưa có tên khách hàng!", Toast.LENGTH_LONG).show()
            } else if (khachHang!!.use_app.contains("sms")) {
                showDialogTaiTin(loadAll = false) {
                    controller.getFullSms(it.sdt, typeKh, it, reLoadLv = { resetLv() })
                    db!!.queryData("Update chat_database set del_sms = 1 WHERE ten_kh = '${it.ten_kh}' AND ngay_nhan = '$mDate'")
                }
            } else {
                showDialogTaiTin(loadAll = false) {
                    controller.getAllChat(it.type_kh, it, resetLv = { resetLv() })
                    db!!.queryData("Update chat_database set del_sms = 1 WHERE ten_kh = '${it.ten_kh}' AND ngay_nhan = '$mDate'")
                }
            }
        }
        bind.btnTaiAll.setOnClickListener { _ ->
            showDialogTaiTin(loadAll = true) {
                controller.getFullSms("Full", typeKh, it, reLoadLv = { resetLv() })
                db!!.queryData("Update chat_database set del_sms = 1 WHERE ngay_nhan = '$mDate'")
            }
        }
        if (ContextCompat.checkSelfPermission(context!!, "android.permission.READ_SMS") != 0) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity!!,
                    "android.permission.READ_SMS"
                )
            ) {
                ActivityCompat.requestPermissions(
                    activity!!,
                    arrayOf("android.permission.READ_SMS"),
                    1
                )
            } else {
                ActivityCompat.requestPermissions(
                    activity!!,
                    arrayOf("android.permission.READ_SMS"),
                    1
                )
            }
        }
        controlRadioButton()
        registerForContextMenu(bind.lvSuatin)
        return bind.root
    }

    private fun showDialogTaiTin(loadAll: Boolean, action: (KhachHang) -> Unit) {
        val who = if (loadAll) "tất cả khách" else khachHang?.ten_kh
        Dialog.simple(
            activity!!,
            title = "Tải lại tin nhắn của $who",
            positiveText = "YES",
            negativeText = "No",
            positiveAction = {
                try {
                    khachHang?.let {
                        action(it)
                    }
                } catch (e: ParseException) {
                    e.printStackTrace()
                }
            }
        )
    }

    private fun addTin() {
        val textSua = bind.edtSuatin.text.toString().replace("'", " ").trim()
        if (mKhachHang.isNotEmpty() && textSua.length > 6) {
            val mDate = MainState.dateYMD
            val gioNhan = LocalTime.now()
                .format(DateTimeFormatter.ofPattern("HH:mm:ss"))
            val qrStr =
                "Select * From tbl_tinnhanS WHERE nd_goc = '$textSua' AND ngay_nhan = '$mDate' AND so_dienthoai = '${khachHang!!.sdt}'"
            val ktratin = db!!.getData(qrStr)
            ktratin.moveToFirst()
            val khachHangDB = KhachHangStore.I.selectWhere("sdt = '${khachHang!!.sdt}'")
            if (ktratin.count > 0) {
                Toast.makeText(activity, "Đã có tin này trong CSDL!", Toast.LENGTH_LONG).show()
            } else if (spinPosition <= -1) {
                Toast.makeText(activity, "Hãy chọn tên khách hàng", Toast.LENGTH_LONG).show()
            } else {
                assert(khachHangDB != null)
                if (khachHangDB!!.type_kh == 3) {
                    Dialog.simple(
                        activity!!,
                        title = "Chọn loại tin nhắn:",
                        positiveText = "Tin nhận",
                        negativeText = "Tin chuyển",
                        positiveAction = {
                            typeKh = 1
                            controller.addTin(
                                mDate,
                                gioNhan,
                                typeKh,
                                khachHang!!,
                                khachHangDB,
                                textSua
                            )
                            bind.edtSuatin.setText("")
                        },
                        negativeAction = {
                            typeKh = 2
                            controller.addTin(
                                mDate,
                                gioNhan,
                                typeKh,
                                khachHang!!,
                                khachHangDB,
                                textSua
                            )
                            bind.edtSuatin.setText("")
                        })
                } else {
                    typeKh = khachHangDB.type_kh
                    controller.addTin(mDate, gioNhan, typeKh, khachHang!!, khachHangDB, textSua)
                    bind.edtSuatin.setText("")
                }
            }
            resetLv()
            ktratin.close()
        }
    }

    private fun suaTin() {
        val textSua = bind.edtSuatin.text.toString()
        val idTn = tinNhanS()!!.ID!!
        db!!.queryData("Update tbl_tinnhanS Set nd_phantich = '$textSua', nd_sua = '$textSua' WHERE id = $idTn")
        val typeKh =
            getIntField(TinNhanS.TABLE_NAME, TinNhanS.TYPE_KH, "id = $idTn")
        try {
            XuLyTinNhanService.I.upsertTinNhan(idTn, typeKh)
            EventBus.getDefault().post(SetupErrorBagde(0))
        } catch (e: Throwable) {
            db!!.queryData("Update tbl_tinnhanS set phat_hien_loi = 'ko' WHERE id = $idTn")
            db!!.queryData("Delete From tbl_soctS WHERE ngay_nhan = '${tinNhanS()!!.ngayNhan}' AND so_dienthoai = '${tinNhanS()!!.sdt}' AND so_tin_nhan = ${tinNhanS()!!.soTinNhan} AND type_kh = $typeKh")
            error = false
            Toast.makeText(activity, "Đã xảy ra lỗi!! + ${e.message}", Toast.LENGTH_SHORT)
                .show()
        }
        if (!checkTime("18:30") && ngayNow!!.contains(mDate!!)) {
            try {
                GuiTinNhanService.I.replyKhach(tinNhanS()!!.ID!!)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        val tinNhanSg = TinNhanStore.I.selectByID(tinNhanS()!!.ID!!)
        if (tinNhanSg!!.phatHienLoi.contains(KHONG_HIEU)) {
            val str = tinNhanSg.ndPhanTich.replace(LDPRO, "<font color='#FF0000'>")
            bind.edtSuatin.setText(Html.fromHtml(str))
            if (tinNhanSg.ndPhanTich.contains(LDPRO)) {
                try {
                    bind.edtSuatin.setSelection(str.indexOf("<font"))
                } catch (ignored: Exception) {
                }
            }
            error = false
        } else {
            bind.edtSuatin.setText("")
            resetLv()
            if (mTinNhanS.isNotEmpty()) {
                lvPosition = 0
                if (tinNhanS()!!.phatHienLoi.contains(KHONG_HIEU)) {
                    bind.edtSuatin.setText(
                        Html.fromHtml(
                            tinNhanS()!!.ndPhanTich.replace(
                                LDPRO, "<font color='#FF0000'>"
                            )
                        )
                    )
                    val index = tinNhanS()!!.ndPhanTich.indexOf(LDPRO)
                    if (index > -1) {
                        try {
                            bind.edtSuatin.setSelection(index)
                        } catch (ignored: Exception) {
                        }
                    }
                    bind.sprKH.setSelection(mNameKhach.indexOf(tinNhanS()!!.tenKh))
                    error = false
                } else {
                    bind.edtSuatin.setText(tinNhanS()!!.ndSua)
                }
            } else {
                lvPosition = -1
                error = false
            }
        }
    }

    private fun traLaiTin(tinNhanS: TinNhanS?) {
        val msg = "Trả lại:\n${tinNhanS!!.ndGoc}"
        GuiTinNhanService.I.sendMessage(
            context,
            useApp = tinNhanS.useApp, tenKH = tinNhanS.tenKh, sdt = tinNhanS.sdt, message = msg
        ) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        handler!!.removeCallbacks(runnable)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v2: View, menuInfo: ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v2, menuInfo)
        menu.add(0, 1, 0, "Xóa tin này?")
        menu.add(0, 2, 0, "Trả lại/Xóa tin này?")
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        super.onContextItemSelected(item)
        if (item.itemId == 1) {
            if (lvPosition >= 0) {
                val database = db
                database!!.queryData("Delete FROM tbl_tinnhanS WHERE id = " + mTinNhanS[lvPosition]!!.ID)
                lvPosition = -1
                resetLv()
                Toast.makeText(activity, "Xoá thành công", Toast.LENGTH_LONG).show()
                bind.edtSuatin.setText("")
                EventBus.getDefault().post(SetupErrorBagde(0))
            }
            resetLv()
            if (mTinNhanS.isNotEmpty()) {
                lvPosition = 0
                if (tinNhanS()!!.phatHienLoi.contains(KHONG_HIEU)) {
                    bind.edtSuatin.setText(
                        Html.fromHtml(
                            tinNhanS()!!.ndPhanTich.replace(
                                LDPRO,
                                "<font color='#FF0000'>"
                            )
                        )
                    )
                    val index = tinNhanS()!!.ndPhanTich.indexOf(LDPRO)
                    if (index > -1) {
                        try {
                            bind.edtSuatin.setSelection(index)
                        } catch (ignored: Exception) {
                        }
                    }
                    bind.sprKH.setSelection(mNameKhach.indexOf(tinNhanS()!!.tenKh))
                    error = false
                } else {
                    bind.edtSuatin.setText(tinNhanS()!!.ndSua)
                }
            } else {
                lvPosition = -1
                error = false
            }
        }
        if (item.itemId == 2) {
            if (lvPosition >= 0 && lvPosition < mTinNhanS.size && mTinNhanS[lvPosition]!!.ID != null) {
                val tinNhanS = mTinNhanS[lvPosition]
                traLaiTin(tinNhanS)
                db!!.queryData("Delete FROM tbl_tinnhanS WHERE id = " + mTinNhanS[lvPosition]!!.ID)
                lvPosition = -1
                resetLv()
                Toast.makeText(activity, "Xoá thành công", Toast.LENGTH_LONG).show()
                bind.edtSuatin.setText("")
            }
            resetLv()
        }
        return true
    }

    private fun controlRadioButton() {
        if (bind.radioSuaTin.isChecked) {
            bind.edittinnhan.visibility = View.VISIBLE
            bind.liKhachHang.visibility = View.VISIBLE
            bind.liButton.visibility = View.VISIBLE
            bind.btnLoadtin.visibility = View.GONE
            bind.btnSuatin.visibility = View.VISIBLE
            bind.edtSuatin.visibility = View.VISIBLE
            bind.btnTaiAll.visibility = View.GONE
            resetLv()
        } else if (bind.radioTaiTin.isChecked) {
            bind.edittinnhan.visibility = View.VISIBLE
            bind.liKhachHang.visibility = View.VISIBLE
            bind.liButton.visibility = View.VISIBLE
            bind.btnSuatin.visibility = View.GONE
            bind.btnLoadtin.visibility = View.VISIBLE
            bind.edtSuatin.visibility = View.GONE
            bind.btnTaiAll.visibility = View.VISIBLE
            resetLv()
        }
    }

    fun resetLv() {
        val mDate = MainState.dateYMD
        mTinNhanS =
            TinNhanStore.I.selectListWhere("phat_hien_loi <> 'ok' AND ngay_nhan = '$mDate'")
        if (activity != null) {
            tngAdapter!!.clear()
            tngAdapter!!.addAll(mTinNhanS)
            tngAdapter!!.notifyDataSetChanged()
        }
    }

    inner class TNGAdapter(context: Context?, resource: Int) : ArrayAdapter<Any?>(
        context!!, resource
    ) {

        internal inner class ViewHolder(private val binding: FragSuatinLvBinding) {
            fun bind(tinNhanS: TinNhanS, isSelected: Boolean) {
                binding.isSelected = isSelected
                binding.tinNhan = tinNhanS
                binding.executePendingBindings()
            }
        }

        override fun getView(position: Int, mView: View?, parent: ViewGroup): View {
            val holder: ViewHolder
            var view = mView
            if (mView == null) {
                val bind = FragSuatinLvBinding.inflate(layoutInflater)
                holder = ViewHolder(bind)
                view = bind.root
                view.tag = holder
            } else {
                holder = mView.tag as ViewHolder
            }
            holder.bind(mTinNhanS[position]!!, position == lvPosition)
            return view!!
        }
    }
}