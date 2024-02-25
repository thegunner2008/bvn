package tamhoang.bvn.data.remote

import java.lang.Exception

class Account {
    @JvmField
    var date: String? = null
    @JvmField
    var phone: String? = null
    @JvmField
    var type: String? = null

    constructor(date: String?, phone: String?, type: String?) {
        this.date = date
        this.phone = phone
        this.type = type
    }

    constructor(map: Map<String?, Any?>) {
        try {
            date =
                if (map["date"] != null) map["date"] as String? else if (map["date_end"] != null) map["date_end"] as String? else null
            phone = if (map["phone"] != null) map["phone"] as String? else ""
        } catch (e: Exception) {
            date = null
            phone = ""
        }
    }

    override fun toString(): String {
        return "Account{date_end='$date', phone='$phone', type='$type'}"
    }
}