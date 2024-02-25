package tamhoang.bvn.data.services.xuLyTinNhan.lotteryHelper

import org.json.JSONObject
import tamhoang.bvn.constants.Const
import tamhoang.bvn.constants.Const.T
import tamhoang.bvn.data.entities.DuLieuSplit
import tamhoang.bvn.data.entities.Lottery
import tamhoang.bvn.data.enum.TL
import tamhoang.bvn.data.services.xuLyTinNhan.lotteryHelper.danSo.DanSo
import tamhoang.bvn.data.services.xuLyTinNhan.lotteryHelper.danSo.theLoai.XienFunc
import tamhoang.bvn.data.setting.SettingKH
import tamhoang.bvn.util.extensions.*

object LotteryHelper {
    fun splitToDuLieu(message: String): DuLieuSplit {
        var eDan = 0
        var kyTuThua = ""
        val cleanMessage = message.replace(" , ", " ").replace(" ,", " ")
            .clearDuplicate(' ', 9).clearDuplicate(',', 9)
            .trim() + " "

        val listDuLieu = cleanMessage.indexOfs(" x ").filter { it > 0 }.map { indexX ->
            var soTien = ""
            var eSoTien = indexX
            while (eSoTien < cleanMessage.length) {
                val charAt = cleanMessage[eSoTien]
                if (charAt.isWhitespace() && soTien.isNotEmpty()) break
                if ("0123456789,tr".contains(charAt)) {
                    soTien += charAt
                }
                eSoTien++
            }
            var dvTien = ""
            var eDvTien = cleanMessage.countTrueIndex(startIndex = eSoTien,
                condition = { it.isLetter() || dvTien.isEmpty() },
                action = { dvTien += it }
            )

            if (dvTien.contains(
                    listOf(
                        "lo", "de", "xi", "xn", "hc", "xg", "cang", "bc", "dau",
                        "dit", "tong", "cham", "dan", "boj", " x", "kep", "sat",
                        "to", "nho", "chan", "le", "ko", "chia", "duoi", "be"
                    )
                )
            ) {
                eDvTien -= dvTien.length
            }

            var duLieu = ""
            if (dvTien.contains("x ")) {
                val i = cleanMessage.substring(0, eSoTien).indexOfLast { !it.isDigit() }
                if (i != -1) {
                    duLieu = cleanMessage.substring(eDan, i + 1)
                    kyTuThua = cleanMessage.substring(i + 1)
                    eDan = i + 1
                }
            } else {
                duLieu = cleanMessage.substring(eDan, eDvTien)
                eDan = eDvTien
            }

            duLieu
        }

        return DuLieuSplit(listDuLieu, kyTuThua)
    }

    fun convertFromListDulieu(
        listDuLieu: List<String>,
        settingKH: SettingKH,
        kyTuThua: String,
        onError: (String) -> Unit
    ): MutableList<Lottery> {
        var theLoaiTruoc = TL.NULL
        val lotteries = listDuLieu
            .asSequence()
            .filterNot { it.trim().startsWith(T) }
            .map {
                val duLieuTrim = it.trim()
                if (duLieuTrim.length > 6) {
                    var duLieu = it
                    for (i in 6 downTo 1) {
                        val s = duLieuTrim.substring(0, i)
                        if (s.trim().contains(T) && s.clear(listOf(T, " ", ",")).isNumeric()) {
                            duLieu = " " + duLieuTrim.substring(i + 1) + " "
                        }
                    }
                    duLieu
                } else it
            }
            .filter { it.contains(" x ") && it.trim().indexOf("x ") >= 2 }
            .map { duLieu ->
                val lottery = Lottery(duLieu = duLieu)

                val theLoai = TL.getTL(duLieu, settingKH.khachDe.value() == 1)

                lottery.theLoai =
                    if (theLoai == TL.NULL && !duLieu.contains(Const.FONT))
                        theLoaiTruoc
                    else
                        theLoai
                theLoaiTruoc = lottery.theLoai

                lottery.left = duLieu.substring(0, duLieu.indexOf(" x ")).trim() //  "deb 25"
                lottery.right = duLieu.substring(duLieu.indexOf(" x ")) // " x 5"

                lottery.danSo = DanSo.parse(lottery)
                lottery.soTien = SoTien.parse(lottery.right, lottery.theLoai)

                lottery.checkLoiTien(checkLoiDv = settingKH.baoLoiDonVi.isTrue())
                lottery.checkX10Xien(caiDatXienNhan = settingKH.nhanXien.value())

                if (lottery.danSo.isNotEmpty()) {
                    val ketquaDaySo = lottery.danSo.trim()
                    if (ketquaDaySo.contains(Const.KHONG_HIEU)) {
                        onError.invoke(ketquaDaySo)
                    } else {
                        if (ketquaDaySo[ketquaDaySo.length - 1] == ',') {
                            lottery.danSo = ketquaDaySo.substring(0, ketquaDaySo.length - 1)
                        }
                    }
                } else {
                    lottery.danSo = Const.KHONG_HIEU + lottery.left.ifEmpty { lottery.theLoai.t }
                }
                lottery
            }.toMutableList()

        if (kyTuThua.clear(listOf(" ", ".", ",", ";")).isNotEmpty()) {
            lotteries.add(
                Lottery(
                    duLieu = kyTuThua,
                    left = kyTuThua,
                    right = kyTuThua,
                    danSo = "${Const.KHONG_HIEU} $kyTuThua"
                )
            )
        }
        return lotteries
    }

    fun convertLotteriesToData(
        lotteries: MutableList<Lottery>,
        quaGioLo: Boolean
    ): Pair<String, JSONObject> {
        var ndPhantich = ""
        val jsonDaTa = JSONObject()
        var key = 0
        for (item in lotteries) {
            if (item.theLoai == TL.NULL) break

            if (item.theLoai == TL.HC) {
                val jsonDeB = item.toJsonData(
                    dulieu = item.duLieu.replaceFirst("hc", "de"),
                    theLoai = TL.DeB
                )
                val jsonDeD = item.toJsonData(
                    dulieu = item.duLieu.replaceFirst("hc", "nhat"),
                    theLoai = TL.DeD
                )
                ndPhantich += TL.DeB.str + item.danSo.trim() + "x" + item.soTien + "\n"
                jsonDaTa.put(key.toString(), jsonDeB)
                key++

                if (quaGioLo) continue

                ndPhantich += TL.DeD.str + item.danSo.trim() + "x" + item.soTien + "\n"
                jsonDaTa.put(key.toString(), jsonDeD)
            } else if (item.theLoai.isXien()) {
                val capXien = item.danSo.split(" ").filter { it.isNotBlank() }

                val theLoaiStr =
                    if (listOf(TL.XG2, TL.XG3, TL.XG4).contains(item.theLoai)) TL.XI.str
                    else item.theLoai.str
                ndPhantich += capXien.joinToString("\n") { theLoaiStr + ":" + it + "x" + item.soTien } + "\n"

                if (item.theLoai.isXienQuay()) {
                    capXien.forEach { s ->
                        val capXienQuay = XienFunc.xienQuay(s).split(" ").filter { it.isNotBlank() }
                        val jsonXq = item.toJsonData(
                            dulieu = item.theLoai.str + ":" + s + "x" + item.soTien,
                            theLoai = if (item.theLoai == TL.XQA) TL.XIA else TL.XI,
                            danSo = XienFunc.xienQuay(s),
                            soLuong = "${capXienQuay.size} cặp."
                        )
                        jsonDaTa.put(key.toString(), jsonXq)
                        key++
                    }
                } else {
                    val jsonXien = item.toJsonData(
                        soLuong = "${capXien.size} cặp."
                    )
                    jsonDaTa.put(key.toString(), jsonXien)
                }
            } else {
                val json = item.toJsonData()
                jsonDaTa.put(key.toString(), json)
                ndPhantich += item.theLoai.str + ":" + item.danSo.trim() + "x" + item.soTien + "\n"
            }
            key++
        }
        return Pair(ndPhantich, jsonDaTa)
    }
}