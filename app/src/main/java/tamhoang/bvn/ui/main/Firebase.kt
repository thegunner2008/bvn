package tamhoang.bvn.ui.main

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import tamhoang.bvn.data.BusEvent
import tamhoang.bvn.data.remote.ApiClient
import tamhoang.bvn.ui.launcher.LauncherActivity
import tamhoang.bvn.util.Convert

abstract class Firebase {
    private fun createPartner() = ApiClient.service().createPartner(requestParam)
        .enqueue(object : Callback<Any?> {
            override fun onResponse(call: Call<Any?>, response: Response<Any?>) {}
            override fun onFailure(call: Call<Any?>, t: Throwable) {}
        })

    private val requestParam: HashMap<String, String>
        get() {
            val parameters = HashMap<String, String>()
            parameters["device_id"] = LauncherActivity.Imei ?: ""
            parameters["manager_id"] = "0"
            return parameters
        }

    fun initFireBase(context: Context) {
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        database.getReference("device/" + LauncherActivity.Imei).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                receiveDeviceSnapshot(database, dataSnapshot)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(context, "Lỗi fb:" + databaseError.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun getDevice(context: Context) {
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        database.getReference("device/" + LauncherActivity.Imei).addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    try {
                        receiveDeviceSnapshot(database, dataSnapshot)
                        Toast.makeText(context, "Hoàn thành", Toast.LENGTH_SHORT).show()
                    } catch (e: java.lang.Exception) {
                        Toast.makeText(context, "Lỗi:" + e.message, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(context, "Lỗi:" + databaseError.message, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun receiveDeviceSnapshot(database: FirebaseDatabase, dataSnapshot: DataSnapshot) {
        if (dataSnapshot.value == null) {
            createPartner()
            return
        }
        Log.e("FirebaseDatabase", "receiveDeviceSnapshot: " + dataSnapshot.value)
        if (dataSnapshot.hasChild("exp")) MainState.thongTinAcc.date =
            Convert.dateIntToString1(dataSnapshot.child("exp").value as Long + 1000)
        if (dataSnapshot.hasChild("is_free"))
            MainState.thongTinAcc.type =
                if (dataSnapshot.child("is_free").getValue(Boolean::class.java) == true) "Free" else "Vip"
        if (dataSnapshot.hasChild("manager_id")) {
            val managerId = dataSnapshot.child("manager_id").value.toString()
            checkManager(database, managerId)
        }
        Log.e("FirebaseDatabase", "receiveDeviceSnapshot: " + MainState.thongTinAcc.toString())

        EventBus.getDefault().post(BusEvent.ChangeAccount())
    }


    private fun checkManager(database: FirebaseDatabase, managerId: String) {
        database.getReference("manager/$managerId").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.value == null) return
                if (dataSnapshot.hasChild("phone")) {
                    MainState.thongTinAcc.phone = dataSnapshot.child("phone").value.toString()
                    Log.e("FirebaseDatabase", "checkManager phone: ${MainState.thongTinAcc.phone}")
                    EventBus.getDefault().post(BusEvent.ChangeManager())
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
