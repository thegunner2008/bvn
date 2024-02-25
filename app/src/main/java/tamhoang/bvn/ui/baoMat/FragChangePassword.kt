package tamhoang.bvn.ui.baoMat

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import tamhoang.bvn.akaman.AkaManSec
import tamhoang.bvn.data.DbOpenHelper
import tamhoang.bvn.databinding.FragChangePasswordBinding

class FragChangePassword : Fragment() {
    private var _bind: FragChangePasswordBinding? = null
    val bind get() = _bind!!

    private var db: DbOpenHelper? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _bind = FragChangePasswordBinding.inflate(inflater, container, false)
        db = DbOpenHelper(activity!!)

        initViews()
        enableControls()
        return bind.root
    }

    override fun onResume() {
        super.onResume()
        enableControls()
    }

    private fun enableControls() {
        if (AkaManSec.pwdMode == 1) {
            bind.chkPwdMode.isChecked = true
            bind.password.isEnabled = true
            bind.rePassword.isEnabled = true
        } else {
            bind.chkPwdMode.isChecked = false
            bind.password.isEnabled = false
            bind.rePassword.isEnabled = false
        }
        if (AkaManSec.useTruncate == 1) {
            bind.truncatePassword.isEnabled = true
            bind.reTruncatePassword.isEnabled = true
            bind.chkTruncate.isEnabled = true
            bind.chkTruncate.isChecked = true
            bind.swTruncate.isChecked = true
            return
        }
        bind.truncatePassword.isEnabled = false
        bind.reTruncatePassword.isEnabled = false
        bind.chkTruncate.isEnabled = false
        bind.chkTruncate.isChecked = false
        bind.swTruncate.isChecked = false
    }

    private fun initViews() {
        bind.btnAbout.setOnClickListener {
            val builder = AlertDialog.Builder(activity)
            builder.setMessage("Mật khẩu phải:\n- Ít nhất 8 ký tự\n- Ít nhất phải có 1 chữ thường\n- Ít nhất phải có 1 chữ in hoa\n- Ít nhất phải có một ký tự đặc biệt sau: @#$%^&+=\n- Không chứa khoảng trống\n")
                .setCancelable(true)
            val create = builder.create()
            create.setCanceledOnTouchOutside(true)
            create.show()
        }
        bind.chkPwdMode.isChecked = AkaManSec.pwdMode == 1
        bind.chkPwdMode.setOnCheckedChangeListener { _, b: Boolean ->
            bind.password.isEnabled = b
            bind.rePassword.isEnabled = b
        }
        bind.btnSaveTruncate.isEnabled = false
        bind.swTruncate.setOnCheckedChangeListener { _, b: Boolean ->
            try {
                bind.truncatePassword.isEnabled = b
                bind.reTruncatePassword.isEnabled = b
                bind.btnSaveTruncate.isEnabled = b
                bind.chkTruncate.isEnabled = b
                if (b) {
                    if (AkaManSec.truncatePwd == "") {
                        Toast.makeText(
                            this.activity,
                            "Bạn chưa có mật khẩu, hãy nhập mật khẩu rồi lưu lại!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        AkaManSec.useTruncate = 1
                        AkaManSec.updateAkaManSec(db)
                        AkaManSec.queryAkaManPwd(db)
                    }
                } else {
                    AkaManSec.useTruncate = 0
                    AkaManSec.updateAkaManSec(db)
                    AkaManSec.queryAkaManPwd(db)
                    Toast.makeText(this.activity, "Đã tắt tính năng này!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        bind.btnActive.setOnClickListener { }
        bind.btnSavePwd.setOnClickListener {
            try {
                if (bind.chkPwdMode.isChecked) {
                    if (validatePassword(bind.password.text.toString().trim())) {
                        val md5 = AkaManSec.md5(bind.password.text.toString().trim())
                        val md5Re = AkaManSec.md5(bind.rePassword.text.toString().trim())
                        if (md5Re != "" && md5 != "") {
                            if (md5Re != md5) {
                                bind.passwordError.isErrorEnabled = true
                                bind.passwordError.error = "\"Mật khẩu\" và \"Nhập lại mật khẩu\" không giống nhau!"
                                bind.rePasswordError.isErrorEnabled = true
                                bind.rePasswordError.error = "\"Mật khẩu\" và \"Nhập lại mật khẩu\" không giống nhau!"
                                Toast.makeText(
                                    this.activity,
                                    "\"Mật khẩu\" và \"Nhập lại mật khẩu\" không giống nhau!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@setOnClickListener
                            } else if (md5 == AkaManSec.truncatePwd) {
                                bind.passwordError.isErrorEnabled = true
                                bind.passwordError.error = "Mật khẩu này đã sử dụng, hãy dùng mật khẩu khác!"
                                Toast.makeText(
                                    this.activity,
                                    "Mật khẩu này đã sử dụng, hãy dùng mật khẩu khác!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@setOnClickListener
                            } else {
                                bind.passwordError.isErrorEnabled = false
                                bind.passwordError.error = ""
                                AkaManSec.pwdMode = 1
                                AkaManSec.userPwd = md5
                            }
                        } else {
                            bind.passwordError.isErrorEnabled = true
                            bind.passwordError.error = "Mật khẩu phải lớn hơn 5 ký tự và không chứa khoảng trống!"
                            bind.rePasswordError.isErrorEnabled = true
                            bind.rePasswordError.error = "Mật khẩu phải lớn hơn 5 ký tự và không chứa khoảng trống!"
                            Toast.makeText(this.activity, "Mật khẩu phải lớn hơn 5 ký tự!", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                    } else {
                        bind.passwordError.isErrorEnabled = true
                        bind.passwordError.error = "Mật khẩu phải lớn hơn 5 ký tự và không chứa khoảng trống!"
                        bind.rePasswordError.isErrorEnabled = true
                        bind.rePasswordError.error = "Mật khẩu phải lớn hơn 5 ký tự và không chứa khoảng trống!"
                        Toast.makeText(this.activity, "Mật khẩu phải lớn hơn 5 ký tự!", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                } else {
                    AkaManSec.pwdMode = 0
                    AkaManSec.userPwd = ""
                }
                AkaManSec.updateAkaManSec(db)
                AkaManSec.queryAkaManPwd(db)
                Toast.makeText(this.activity, "Đã lưu cài đặt!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        bind.btnSaveTruncate.setOnClickListener {
            try {
                if (validatePassword(bind.truncatePassword.text.toString().trim())) {
                    val md5 = AkaManSec.md5(bind.truncatePassword.text.toString().trim())
                    val md52 = AkaManSec.md5(bind.reTruncatePassword.text.toString().trim())
                    if (md52 != "" && md5 != "") {
                        if (md52 != md5) {
                            bind.truncatePasswordError.isErrorEnabled = true
                            bind.truncatePasswordError.error = "\"Mật khẩu\" và \"Nhập lại mật khẩu\" không giống nhau!"
                            bind.reTruncatePasswordError.isErrorEnabled = true
                            bind.reTruncatePasswordError.error =
                                "\"Mật khẩu\" và \"Nhập lại mật khẩu\" không giống nhau!"
                            Toast.makeText(
                                this.activity,
                                "\"Mật khẩu\" và \"Nhập lại mật khẩu\" không giống nhau!",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@setOnClickListener
                        } else if (md5 != AkaManSec.userPwd) {
                            if (bind.chkTruncate.isChecked) {
                                AkaManSec.truncateMode = 1
                            } else {
                                AkaManSec.truncateMode = 0
                            }
                            bind.truncatePasswordError.isErrorEnabled = false
                            bind.truncatePasswordError.error = ""
                            AkaManSec.useTruncate = 1
                            AkaManSec.truncatePwd = md5
                            AkaManSec.updateAkaManSec(db)
                            AkaManSec.queryAkaManPwd(db)
                            Toast.makeText(this.activity, "Đã lưu mật khẩu!", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        } else {
                            bind.truncatePasswordError.isErrorEnabled = true
                            bind.truncatePasswordError.error = "Mật khẩu này đã sử dụng, hãy dùng mật khẩu khác!"
                            Toast.makeText(
                                this.activity,
                                "Mật khẩu này đã sử dụng, hãy dùng mật khẩu khác!",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@setOnClickListener
                        }
                    }
                    bind.truncatePasswordError.isErrorEnabled = true
                    bind.truncatePasswordError.error = "Mật khẩu phải lớn hơn 5 ký tự và không chứa khoảng trống!"
                    bind.reTruncatePasswordError.isErrorEnabled = true
                    bind.reTruncatePasswordError.error = "Mật khẩu phải lớn hơn 5 ký tự và không chứa khoảng trống!"
                    Toast.makeText(
                        this.activity,
                        "Mật khẩu phải lớn hơn 5 ký tự và không chứa khoảng trống!",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                bind.truncatePasswordError.isErrorEnabled = true
                bind.truncatePasswordError.error = "Mật khẩu phải lớn hơn 5 ký tự và không chứa khoảng trống!"
                bind.reTruncatePasswordError.isErrorEnabled = true
                bind.reTruncatePasswordError.error = "Mật khẩu phải lớn hơn 5 ký tự và không chứa khoảng trống!"
                Toast.makeText(this.activity, "Mật khẩu phải lớn hơn 5 ký tự!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun validatePassword(passwd: String) = !passwd.trim().contains(" ") && passwd.trim().length >= 6
}