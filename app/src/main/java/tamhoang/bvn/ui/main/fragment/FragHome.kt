package tamhoang.bvn.ui.main.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import tamhoang.bvn.BuildConfig
import tamhoang.bvn.R
import tamhoang.bvn.data.BusEvent
import tamhoang.bvn.databinding.FragHomeBinding
import tamhoang.bvn.ui.base.dialog.Dialog
import tamhoang.bvn.ui.launcher.LauncherActivity
import tamhoang.bvn.ui.main.MainActivity
import tamhoang.bvn.ui.main.MainState
import tamhoang.bvn.util.Convert
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class FragHome : Fragment() {
    private var _bind: FragHomeBinding? = null
    val bind get() = _bind!!

    var imei: String? = null

    @SuppressLint("SetTextI18n")
    override fun onCreateView(layoutInflater: LayoutInflater, viewGroup: ViewGroup?, bundle: Bundle?): View? {
        if (MainState.truncate_mode) {
            return layoutInflater.inflate(R.layout.frag_home_truncate, viewGroup, false)
        }
        _bind = FragHomeBinding.inflate(layoutInflater)
        initCheck()
        bind.tvPhone.setOnClickListener {
            if (MainState.phone().isNotEmpty())
                startActivity(
                    Intent(Intent.ACTION_CALL, Uri.parse("tel:" + MainState.phone()))
                )
        }
        bind.tvVersion.text =
            "Phiên bản: " + BuildConfig.VERSION_NAME + " - " + Convert.versionCodeToDate(BuildConfig.VERSION_CODE)
        bind.btnCheck.setOnClickListener {
            recheck()
        }
        return bind.root
    }

    private fun initCheck() {
        imei = LauncherActivity.Imei
        bind.edtImei.text = imei
        if (MainState.isDebug)
            checkImeiFake()
        else
            (activity as MainActivity).controller.initFireBase(activity!!)
    }

    private fun recheck() {
        imei = LauncherActivity.Imei
        bind.edtImei.text = imei
        if (MainState.isDebug)
            checkImeiFake()
        else
            checkImei()
    }

    private fun checkImei() {
        (activity as MainActivity).controller.getDevice(activity!!)
    }

    private fun checkImeiFake() {
        (activity as MainActivity).controller.fakeTaiKhoan()
        bind.tvHansudung.text = MainState.textHSD
        bind.tvTaikhoan.text = MainState.tenAcc()
        bind.tvPhone.text = MainState.phone()
        if (imei == null) {
            startActivity(Intent(activity, LauncherActivity::class.java))
        } else if (!isNetworkConnected) {
            Toast.makeText(activity, "Kiểm tra kết nối Internet!", Toast.LENGTH_SHORT).show()
        }
    }

    private val isNetworkConnected: Boolean
        get() {
            @SuppressLint("WrongConstant") val activeNetworkInfo =
                (activity!!.getSystemService("connectivity") as ConnectivityManager).activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }

    //firebase
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun changeAccount(event: BusEvent.ChangeAccount?) {
        Log.e("FirebaseDatabase", "changeAccount: $event")
        if (event != null) {
            bind.tvHansudung.text = MainState.textHSD
            bind.tvHansudung.setTextColor(if (MainState.checkHSD()) Color.RED else Color.GRAY)
            bind.tvTaikhoan.text = MainState.tenAcc()

            try {
                val time = try {
                    SimpleDateFormat("yyyy-MM-dd").parse(MainState.thongTinAcc.date).time
                } catch (e: ParseException) {
                    Date().time
                }
                val dayLeft = (time - Date().time) / 1000 / 60 / 60 / 24
                if (dayLeft in 1..7) {
                    val msg = "Hạn sử dụng còn lại ${dayLeft.toInt()} ngày!.\n" +
                            "Hãy liên hệ đại lý hoặc SĐT: ${MainState.thongTinAcc.phone} để gia hạn"
                    Dialog.simple(activity!!, "Thông báo", msg, negativeText = "Đóng")
                } else if (time <= 0.0f) {
                    Dialog.hetHSD(activity!!)
                }
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun changeManager(event: BusEvent.ChangeManager?) {
        Log.e("FirebaseDatabase", "changeManager: " + MainState.phone())
        if (event != null) {
            bind.tvPhone.text = MainState.phone()
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }
}