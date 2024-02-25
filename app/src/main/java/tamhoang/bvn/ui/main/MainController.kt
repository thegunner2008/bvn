package tamhoang.bvn.ui.main

import android.content.res.AssetManager
import android.database.Cursor
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import tamhoang.bvn.data.DbOpenHelper
import tamhoang.bvn.data.remote.Account
import tamhoang.bvn.data.remote.ApiClient
import tamhoang.bvn.data.remote.ResponseModel
import tamhoang.bvn.ui.launcher.LauncherActivity
import java.io.File
import java.io.IOException

class MainController(val db: DbOpenHelper): Firebase() {
    fun deleteDir(dir: File?): Boolean {
        return if (dir != null && dir.isDirectory) {
            val ableDel = dir.list().any { !deleteDir(File(dir, it)) }
            if (ableDel) return false
            dir.delete()
        } else if (dir == null || !dir.isFile) {
            false
        } else {
            dir.delete()
        }
    }

    fun loadMappingListToOneJson(assetManager: AssetManager) {
        try {
            val mJarry = JSONObject(loadJSONFromAsset("mappingListToOne.json", assetManager) ?: "{}")
                .getJSONArray("formules")
            var count = 0
            for (i in 0 until mJarry.length()) {
                val item = mJarry.getJSONObject(i)
                val key = item.optString("type", "")
                if (key.isNotEmpty()) {
                    val datas = item.getJSONArray("datas")
                    count += datas.length()
                    for (k in 0 until datas.length()) {
                        val value = datas.getString(k)
                        val mLi = HashMap<String, String>()
                        mLi["type"] = key
                        mLi["datas"] = value
                        MainState.formList.add(mLi)
                    }
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun loadMappingOneByOneJson(assetManager: AssetManager) {
        if (MainState.formArray.size == 0) {
            try {
                val mJarry = JSONObject(loadJSONFromAsset("mappingOneByOne.json", assetManager) ?: "{}")
                    .getJSONArray("listKHs")
                for (i in 0 until mJarry.length()) {
                    val item = mJarry.getJSONObject(i)
                    val key = item.optString("str", "")
                    val value = item.optString("repl_str", "")
                    if (key.isNotEmpty()) {
                        val mLi = HashMap<String, String>()
                        mLi["str"] = key
                        mLi["repl_str"] = value
                        MainState.formArray.add(mLi)
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    private fun loadJSONFromAsset(Filename: String?, assetManager: AssetManager): String? {
        return try {
            val fil = assetManager.open(Filename!!)
            val buffer = ByteArray(fil.available())
            fil.read(buffer)
            fil.close()
            String(buffer, Charsets.UTF_8)
        } catch (ex: IOException) {
            ex.printStackTrace()
            null
        }
    }

    fun suaGia() {
        var cur: Cursor? = null
        try {
            cur = db.getData("Select * From tbl_kh_new")
            if (cur.count > 0 && cur.moveToFirst() && JSONObject(JSONObject(cur.getString(5)).getString("caidat_gia")).getDouble(
                    "dea"
                ) > 10.0
            ) {
                val cursor = db.getData("Select * From tbl_kh_new")
                while (cursor.moveToNext()) {
                    val json = JSONObject(cursor.getString(5))
                    val caidatGia = JSONObject(json.getString("caidat_gia"))
                    val keys = caidatGia.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        if (caidatGia.getDouble(key) > 100.0) {
                            caidatGia.put(key, caidatGia.getDouble(key) / 1000.0)
                        }
                    }
                    json.put("caidat_gia", caidatGia)
                    db.queryData(
                        "update tbl_kh_new set tbl_mb = '$json' WHERE ten_kh = '" + cursor.getString(
                            0
                        ) + "'"
                    )
                }
                cursor.close()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        } catch (th: Throwable) {
            cur!!.close()
            throw th
        }
        cur!!.close()
    }

    fun getExpireDate(actionDone: () -> Unit) =
        ApiClient.service().getExpireDate(imei = LauncherActivity.Imei ?: "", serial = LauncherActivity.serial ?: "")
            ?.enqueue(object : Callback<ResponseModel?> {
                override fun onResponse(call: Call<ResponseModel?>, response: Response<ResponseModel?>) {
                    if (response.isSuccessful) {
                        val responseModel = response.body()
                        if (responseModel != null && !responseModel.listKh.isNullOrEmpty()) {
                            val kh = responseModel.listKh!![0]
                            MainState.thongTinAcc = Account(date = kh.date, phone = kh.kTra, type = kh.acc)
                            actionDone()
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseModel?>, t: Throwable) {}
            })

    fun fakeTaiKhoan() {
        MainState.thongTinAcc = Account("2024-05-05", "0912345678", "vip")
    }
}