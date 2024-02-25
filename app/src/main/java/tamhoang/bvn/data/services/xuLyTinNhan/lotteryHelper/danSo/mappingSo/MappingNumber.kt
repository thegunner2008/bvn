package tamhoang.bvn.data.services.xuLyTinNhan.lotteryHelper.danSo.mappingSo

abstract class MappingNumber {
    abstract fun condition(str: String): Boolean
    abstract fun convert(originMsg: String, currentMsg: String, index: Int): String

    var error: String? = null
    var result: String = ""
    var newIndex: Int? = null
}