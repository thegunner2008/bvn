package tamhoang.bvn.data.services.xuLyTinNhan.lotteryHelper.danSo.theLoai

import tamhoang.bvn.constants.Const
import tamhoang.bvn.data.services.xuLyTinNhan.lotteryHelper.danSo.MappingDanSo
import tamhoang.bvn.util.extensions.between
import tamhoang.bvn.util.extensions.clear

object LoDe {
    fun parse(str: String): String {
        if (!str.contains("trung") && !str.contains("bor")) {
            return try {
                val danSo = MappingDanSo.all(str)
                if (danSo.contains(Const.KHONG_HIEU) && danSo.length < 11)
                    "${Const.KHONG_HIEU} $str"
                else
                    danSo
            } catch (e: Exception) {
                "${Const.KHONG_HIEU} $str"
            }
        }

        if (str.contains("bor trung")) {
            return convertBoTrung(str)
        }

        if (!str.contains("trung") || !str.contains("bor")) {
            val keyWord = if (str.contains("trung")) "trung" else "bor"
            return removeSoTrung(str, keyWord)
        }

        return if (str.indexOf("trung") < str.indexOf("bor")) {
            convertTrungBeforeBor(str)
        } else {
            convertBorBeforeTrung(str)
        }
    }

    private fun convertBoTrung(str: String): String {
        val listBT = str.split("bor trung").filter { it.isNotEmpty() }
        val leftBT = listBT.getOrNull(0) ?: ""
        val rightBT = listBT.getOrNull(1) ?: ""

        val danSo = MappingDanSo.all(leftBT)

        if (danSo.contains(Const.KHONG_HIEU)) return danSo

        var danBo = ""
        var danTrung = ""
        if (rightBT.contains("bor")) {
            danBo = MappingDanSo.all(rightBT.replace("bor", ""))
            if (danBo.contains(Const.KHONG_HIEU)) return danBo
        } else if (rightBT.contains("trung")) {
            danTrung = MappingDanSo.all(rightBT.replace("trung", ""))
            if (danTrung.contains(Const.KHONG_HIEU)) return danTrung
        }

        return danSo.split(",")
            .filter { it.isNotEmpty() || danBo.isEmpty() && danTrung.isEmpty() }
            .filter {
                !danBo.contains(it) || danBo.isEmpty() && danTrung.contains(it)
            }.distinct().joinToString(",")
    }

    private fun removeSoTrung(str: String, keyWord: String): String { // 11, 22, 33, 44 <bor/trung> 22, 44 => 11, 33
        val splits = str.split(keyWord)
            .filter { it.isNotEmpty() }
            .map {
                if (it.contains(Const.KHONG_HIEU)) {
                    return it
                }
                MappingDanSo.all(it)
            }
        return try {
            val listBor = splits.subList(1, splits.size)
            val danSo = splits[0].split(",")
                .filter {
                    it.isNotEmpty() && listBor.all { bo -> !bo.contains(it) }
                }
                .joinToString(",")
            danSo.ifEmpty { "${Const.KHONG_HIEU} " + str.substring(str.indexOf(keyWord)) }
        } catch (e: Exception) {
            "${Const.KHONG_HIEU} $str"
        }
    }

    private fun convertTrungBeforeBor(str: String): String {
        val leftTrung = str.between(0, "trung")
        val rightBor = str.between("bor").clear("bor")
        val betweenTB = str.between("trung", "bor")
        var error = ""

        if (leftTrung.length > 1) {
            try {
                val soLeftTrung = MappingDanSo.all(leftTrung)
                if (soLeftTrung.contains(Const.KHONG_HIEU)) {
                    error = soLeftTrung
                } else if (betweenTB.length > 1) {
                    val soBetweenTB = MappingDanSo.all(betweenTB)
                    if (soBetweenTB.contains(Const.KHONG_HIEU)) {
                        error = soBetweenTB
                    } else if (rightBor.length > 1) {
                        val soRightBor = MappingDanSo.all(rightBor)
                        if (soRightBor.contains(Const.KHONG_HIEU)) {
                            error = soRightBor
                        } else {
                            return soLeftTrung.split(",")
                                .filter {
                                    it.isNotEmpty() && soBetweenTB.contains(it) && !soRightBor.contains(it)
                                }
                                .joinToString(",")
                        }
                    }
                }
            } catch (_: Exception) {
            }
        }
        return "${Const.KHONG_HIEU} ${if (error.length > 11) error else str}"
    }

    private fun convertBorBeforeTrung(str: String): String {
        val leftBor = str.between(0, "bor")
        val rightTrung = str.between("trung").clear("trung")
        val betweenBT = str.between("bor", "trung")
        var error = ""

        if (leftBor.length > 1) {
            try {
                val soLeftBor = MappingDanSo.all(leftBor)
                if (soLeftBor.contains(Const.KHONG_HIEU)) {
                    error = soLeftBor
                } else if (betweenBT.length > 1) {
                    val soBetweenBT = MappingDanSo.all(betweenBT)
                    if (soBetweenBT.contains(Const.KHONG_HIEU)) {
                        error = soBetweenBT
                    } else if (rightTrung.length > 1) {
                        val soRightTrung = MappingDanSo.all(rightTrung)
                        if (soRightTrung.contains(Const.KHONG_HIEU)) {
                            error = soRightTrung
                        } else  {
                            return soLeftBor.split(",")
                                .filter {
                                    it.isNotEmpty() && !soBetweenBT.contains(it) && soRightTrung.contains(it)
                                }
                                .joinToString(",")
                        }
                    }
                }
            } catch (_: Exception) { }
        }
        return "${Const.KHONG_HIEU} ${if (error.length > 11) error else str}"
    }
}