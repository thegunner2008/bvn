package tamhoang.bvn.messageCenter.telegram

import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.Client.ResultHandler
import org.drinkless.td.libcore.telegram.TdApi
import tamhoang.bvn.util.Util

object TelegramClient {
    var client: Client? = null

    @JvmStatic
    fun getClient(callback: Callback?): Client? {
        if (client == null) {
            try {
                client = Client.create(callback, null, null)
            } catch (e: Exception) {
                Util.writeLog(e)
            }
        }
        return client
    }

    interface Callback : ResultHandler {
        override fun onResult(`object`: TdApi.Object)
    }
}