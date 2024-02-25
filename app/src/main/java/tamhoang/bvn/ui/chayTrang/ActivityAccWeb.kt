package tamhoang.bvn.ui.chayTrang

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.core.internal.view.SupportMenu
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import tamhoang.bvn.R
import tamhoang.bvn.data.DbOpenHelper
import tamhoang.bvn.data.remote.ApiClient
import tamhoang.bvn.databinding.ActivityAccWebBinding
import tamhoang.bvn.ui.base.toolbar.BaseToolBarActivity
import tamhoang.bvn.ui.main.MainState
import java.io.IOException
import java.util.concurrent.CompletableFuture

class ActivityAccWeb : BaseToolBarActivity() {
    private var _bind: ActivityAccWebBinding? = null
    private val bind get() = _bind!!

    var db: DbOpenHelper? = null
    var newWeb: String? = ""

    override fun getLayoutId(): Int {
        return R.layout.activity_acc_web
    }

    @SuppressLint("RestrictedApi", "SetTextI18n")
    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        _bind = ActivityAccWebBinding.inflate(layoutInflater)
        setContentView(bind.root)
        db = DbOpenHelper(this)

        newWeb = intent.getStringExtra("new_web")
        if (newWeb!!.isEmpty()) {
            bind.linerCaidat.visibility = View.GONE
        } else {
            val database = db
            val cursor = database!!.getData("Select * from tbl_chaytrang_acc Where Username = '$newWeb'")
            cursor.moveToFirst()
            bind.edtAccount.setText(cursor.getString(0))
            bind.edtPassword.setText(cursor.getString(1))
            bind.edtAccount.isEnabled = false
            bind.edtPassword.isEnabled = false
            bind.btnThemTrang.text = "Thêm / Sửa trang"
            bind.btnThemTrang.setTextColor(SupportMenu.CATEGORY_MASK)
            try {
                val jSONObject = JSONObject(cursor.getString(2))
                bind.edtGiadea.setText(jSONObject.getString("gia_dea"))
                bind.edtGiadeb.setText(jSONObject.getString("gia_deb"))
                bind.edtGiadec.setText(jSONObject.getString("gia_dec"))
                bind.edtGiaded.setText(jSONObject.getString("gia_ded"))
                bind.edtGialo.setText(jSONObject.getString("gia_lo"))
                bind.edtGiaxi2.setText(if (!jSONObject.has("gia_xi2")) "560" else jSONObject.getString("gia_xi2"))
                bind.edtGiaxi3.setText(if (!jSONObject.has("gia_xi3")) "520" else jSONObject.getString("gia_xi3"))
                bind.edtGiaxi4.setText(if (!jSONObject.has("gia_xi4")) "450" else jSONObject.getString("gia_xi4"))
                bind.tviewMaxdea.text = jSONObject.getString("max_dea")
                bind.tviewMaxdeb.text = jSONObject.getString("max_deb")
                bind.tviewMaxdec.text = jSONObject.getString("max_dec")
                bind.tviewMaxded.text = jSONObject.getString("max_ded")
                bind.tviewMaxlo.text = jSONObject.getString("max_lo")
                bind.tviewMaxxi2.text = jSONObject.getString("max_xi2")
                bind.tviewMaxxi3.text = jSONObject.getString("max_xi3")
                bind.tviewMaxxi4.text = jSONObject.getString("max_xi4")
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        bind.btnThemTrang.setOnClickListener {
            if (newWeb!!.isEmpty()) {
                if (db!!.getData("Select * from tbl_chaytrang_acc Where Username = '${bind.edtAccount.text}'").count == 0) {
                    bind.btnThemTrang.isEnabled = false
                    runAsync()
                } else {
                    Toast.makeText(this, "Đã có tài khoản này trong hệ thống", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                val jSONObject = JSONObject()
                try {
                    jSONObject.apply {
                        put("gia_dea", bind.edtGiadea.text.toString().replace(".", ""))
                        put("gia_deb", bind.edtGiadeb.text.toString().replace(".", ""))
                        put("gia_dec", bind.edtGiadec.text.toString().replace(".", ""))
                        put("gia_ded", bind.edtGiaded.text.toString().replace(".", ""))
                        put("gia_lo", bind.edtGialo.text.toString().replace(".", ""))
                        put("gia_xi2", bind.edtGiaxi2.text.toString().replace(".", ""))
                        put("gia_xi3", bind.edtGiaxi3.text.toString().replace(".", ""))
                        put("gia_xi4", bind.edtGiaxi4.text.toString().replace(".", ""))
                        put("max_dea", bind.tviewMaxdea.text.toString().replace(".", ""))
                        put("max_deb", bind.tviewMaxdeb.text.toString().replace(".", ""))
                        put("max_dec", bind.tviewMaxdec.text.toString().replace(".", ""))
                        put("max_ded", bind.tviewMaxded.text.toString().replace(".", ""))
                        put("max_lo", bind.tviewMaxlo.text.toString().replace(".", ""))
                        put("max_xi2", bind.tviewMaxxi2.text.toString().replace(".", ""))
                        put("max_xi3", bind.tviewMaxxi3.text.toString().replace(".", ""))
                        put("max_xi4", bind.tviewMaxxi4.text.toString().replace(".", ""))
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                Toast.makeText(this, "Đã lưu thành công", Toast.LENGTH_SHORT).show()
                db!!.queryData("INSERT OR REPLACE Into tbl_chaytrang_acc (Username, Password, Setting) Values ('${bind.edtAccount.text}', '${bind.edtPassword.text}', '$jSONObject')")
                finish()
            }
        }
    }

    private fun runAsync() = CompletableFuture.runAsync { addAccWeb() }

    @SuppressLint("RestrictedApi", "SetTextI18n")
    fun addAccWeb() {
        try {
            val parameters = hashMapOf(
                "Username" to bind.edtAccount.text.toString(),
                "Password" to bind.edtPassword.text.toString()
            )
            val resSignIn = ApiClient.service().lotusSignIn(parameters).execute()
            val token = resSignIn.body()?.get("IdToken")
            if (!resSignIn.isSuccessful || token == null) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(
                        this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT
                    ).show()
                }
                return
            }
            MainState.MyToken = token
            newWeb = bind.edtAccount.text.toString()

            val bodyPlayer = ApiClient.service().lotusPlayer("Bearer " + MainState.MyToken).execute().body()
            if (bodyPlayer != null) {
                Handler(Looper.getMainLooper()).post {
                    bind.linerCaidat.visibility = View.VISIBLE
                    bind.edtAccount.isEnabled = false
                    bind.edtPassword.isEnabled = false
                    bind.btnThemTrang.text = "Thêm / Sửa trang"
                    bind.btnThemTrang.setTextColor(SupportMenu.CATEGORY_MASK)
                    Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show()
                }
                val jSONArray = JSONArray(bodyPlayer.string())
                for (i in 0 until jSONArray.length()) {
                    val jSONObject3 = jSONArray.getJSONObject(i)
                    if (jSONObject3.getInt("GameType") == 0) {
                        if (jSONObject3.getInt("BetType") == 0) {
                            bind.tviewMaxdeb.text = jSONObject3.getString("MaxPointPerNumber")
                        }
                    }
                    if (jSONObject3.getInt("GameType") == 0 && jSONObject3.getInt("BetType") == 1) {
                        bind.tviewMaxlo.text = jSONObject3.getString("MaxPointPerNumber")
                    }
                    if (jSONObject3.getInt("GameType") == 0 && jSONObject3.getInt("BetType") == 2) {
                        bind.tviewMaxxi2.text = jSONObject3.getString("MaxPointPerNumber")
                    }
                    if (jSONObject3.getInt("GameType") == 0 && jSONObject3.getInt("BetType") == 3) {
                        bind.tviewMaxxi3.text = jSONObject3.getString("MaxPointPerNumber")
                    }
                    if (jSONObject3.getInt("GameType") == 0 && jSONObject3.getInt("BetType") == 4) {
                        bind.tviewMaxxi4.text = jSONObject3.getString("MaxPointPerNumber")
                    }
                    if (jSONObject3.getInt("GameType") == 0 && jSONObject3.getInt("BetType") == 21) {
                        bind.tviewMaxdea.text = jSONObject3.getString("MaxPointPerNumber")
                    }
                    if (jSONObject3.getInt("GameType") == 0 && jSONObject3.getInt("BetType") == 22) {
                        bind.tviewMaxded.text = jSONObject3.getString("MaxPointPerNumber")
                    }
                    if (jSONObject3.getInt("GameType") == 0 && jSONObject3.getInt("BetType") == 23) {
                        bind.tviewMaxdec.text = jSONObject3.getString("MaxPointPerNumber")
                    }
                }
                Handler(Looper.getMainLooper()).post { bind.btnThemTrang.isEnabled = true }
                return
            } else {
                Handler(Looper.getMainLooper()).post {
                    bind.btnThemTrang.visibility = View.GONE
                    Toast.makeText(this, "Không thể lấy được Max dạng, vui lòng thử lại sau", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e2: JSONException) {
            e2.printStackTrace()
        }
    }
}