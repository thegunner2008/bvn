package tamhoang.bvn.data

import android.app.PendingIntent
import android.app.RemoteInput
import android.os.Bundle
import tamhoang.bvn.util.notifLib.models.Action
import tamhoang.bvn.util.notifLib.models.RemoteInputParcel
import java.util.ArrayList

class Contact {
    @JvmField
    var app: String? = null
    @JvmField
    var name: String? = null
    @JvmField
    var pendingIntent: PendingIntent? = null
    @JvmField
    var remoteExtras: Bundle? = null
    @JvmField
    var remoteInput: RemoteInput? = null
    @JvmField
    var action: Action? = null
    @JvmField
    var remoteInput2: RemoteInputParcel? = null
    @JvmField
    var process = 0
    @JvmField
    var number = 1
    var waitingList = ArrayList<String>()
    fun getRemoteInput(): RemoteInput? {
        return remoteInput
    }

    fun setRemoteInput(remoteInput2: RemoteInput?) {
        remoteExtras = remoteExtras
    }

    override fun toString(): String {
        return "Contact{" +
                "app='" + app + '\'' +
                ", name='" + name + '\'' +
                ", pendingIntent=" + pendingIntent +
                ", remoteExtras=" + remoteExtras +
                ", remoteInput=" + remoteInput +
                ", action=" + action +
                ", remoteInput2=" + remoteInput2 +
                ", process=" + process +
                ", number=" + number +
                ", waitingList=" + waitingList +
                '}'
    }
}