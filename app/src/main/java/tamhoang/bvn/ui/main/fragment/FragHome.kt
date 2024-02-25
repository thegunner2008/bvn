package tamhoang.bvn.ui.main.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import tamhoang.bvn.BuildConfig
import tamhoang.bvn.R
import tamhoang.bvn.databinding.FragHomeBinding
import tamhoang.bvn.ui.launcher.LauncherActivity
import tamhoang.bvn.ui.main.MainActivity
import tamhoang.bvn.ui.main.MainState
import tamhoang.bvn.util.Convert

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
        bind.tvVersion.text = BuildConfig.VERSION_NAME + " - " + Convert.versionCodeToDate(BuildConfig.VERSION_CODE)
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
            checkImei()
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
        (activity as MainActivity).controller.getExpireDate {
            bind.tvHansudung.text = MainState.textHSD
            bind.tvTaikhoan.text = MainState.tenAcc()
            bind.tvPhone.text = MainState.phone()
            if (imei == null) {
                startActivity(Intent(activity, LauncherActivity::class.java))
            } else if (!isNetworkConnected) {
                Toast.makeText(activity, "Kiểm tra kết nối Internet!", Toast.LENGTH_SHORT).show()
            }
        }
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
}