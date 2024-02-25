package tamhoang.bvn.data.remote

import java.lang.Exception

class Partner {
    var date: String? = null
    var id_partner = 0.0
    var type: String? = null

    constructor(date: String?, id_partner: Int, type: String?) {
        this.date = date
        this.id_partner = id_partner.toDouble()
        this.type = type
    }

    constructor(map: Map<String?, Any?>) {
        try {
            date = if (map["date"] == null) "" else map["date"] as String?
            id_partner = if (map["id_partner"] == null) 0.0 else map["id_partner"] as Double
            type = if (map["type"] == null) "new" else map["type"] as String?
        } catch (e: Exception) {
            date = "01-01-2022"
            id_partner = -1.0
            type = "new"
        }
    }

    override fun toString(): String {
        return "Partner{" +
                "date='" + date + '\'' +
                ", id_partner=" + id_partner +
                ", type='" + type + '\'' +
                '}'
    }
}