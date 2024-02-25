package tamhoang.bvn.ui.login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import tamhoang.bvn.R
import tamhoang.bvn.akaman.AkaManSec
import tamhoang.bvn.data.BaseStore.getPassWord
import tamhoang.bvn.data.BaseStore.savePassWord
import tamhoang.bvn.data.DbOpenHelper
import tamhoang.bvn.databinding.ActivityDangNhapBinding
import tamhoang.bvn.ui.main.MainActivity
import tamhoang.bvn.ui.main.MainState

class LoginActivity : AppCompatActivity() {
    private var _bind: ActivityDangNhapBinding? = null
    private val bind get() = _bind!!

    @SuppressLint("SetTextI18n")
    private fun clickLogin() {
        if (bind.cbSavePass.isChecked) {
            savePassWord(bind.edtPassword.text.toString())
        } else {
            savePassWord("")
        }
        val textEncode = AkaManSec.md5(bind.edtPassword.text.toString().trim())
        val pass = AkaManSec.userPwd
        val passTruncate = AkaManSec.truncatePwd
        if (textEncode == pass) {
            goMainActivity(false)
        } else if (AkaManSec.useTruncate == 1 && textEncode == passTruncate) {
            if (AkaManSec.truncateMode == 1) {
                bind.btnLogin.isEnabled = false
                bind.btnLogin.text = "Gang xử lý..."
                val database = DbOpenHelper(this)
                database.deleteAllTable()
                bind.btnLogin.isEnabled = true
            }
            goMainActivity(true)
        } else {
            bind.passwordError.isErrorEnabled = true
            bind.passwordError.error = "Mật khẩu không đúng!"
            Toast.makeText(this, "Mật khẩu không đúng!", Toast.LENGTH_LONG).show()
        }
    }

    private fun goMainActivity(truncateMode: Boolean) {
        MainState.truncate_mode = truncateMode
        startActivities(arrayOf(Intent(this, MainActivity::class.java)))
    }

    public override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        setContentView(R.layout.activity_dang_nhap)
        AkaManSec.queryAkaManPwd(DbOpenHelper(this))
        bind.btnLogin.setOnClickListener { clickLogin() }
        val savePass = getPassWord()
        if (savePass != "") {
            bind.cbSavePass.isChecked = true
            bind.edtPassword.setText(savePass)
        }
    }
}