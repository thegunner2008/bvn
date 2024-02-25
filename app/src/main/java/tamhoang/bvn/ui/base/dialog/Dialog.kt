package tamhoang.bvn.ui.base.dialog

import android.content.Context
import androidx.appcompat.app.AlertDialog
import tamhoang.bvn.ui.main.MainState

object Dialog {
    fun simple(
        context: Context,
        title: String? = null,
        message: String? = null,
        positiveText: String? = null,
        negativeText: String? = null,
        positiveAction: () -> Unit = {},
        negativeAction: () -> Unit = {},
        cancelable: Boolean = true
    ) {
        val bui = AlertDialog.Builder(context)
        if (title != null) bui.setTitle(title)
        if (message != null) bui.setMessage(message)
        if (negativeText != null)
            bui.setNegativeButton(negativeText) { dialog, _ ->
                negativeAction()
                dialog.cancel()
            }
        if (positiveText != null)
            bui.setPositiveButton(positiveText) { dialog, _ ->
                positiveAction()
                dialog.cancel()
            }

        bui.setCancelable(cancelable)
        bui.create().show()
    }

    fun hetHSD(
        context: Context
    ) {
        try {
            val msg =
                if (MainState.tenAcc().isEmpty()) "Kiểm tra tài khoản"
                else
                    "Đã hết hạn sử dụng phần mềm.\nHãy liên hệ đại lý hoặc SĐT: ${MainState.thongTinAcc.phone} để gia hạn"

            simple(context, "Thông báo", msg, negativeText = "Đóng")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun ktInternet(
        context: Context
    ) {
        try {
            val msg =
                if (MainState.tenAcc().isEmpty()) "Kiểm tra kết nối Internet!"
                else
                    "Kiểm tra kết nối Internet!"

            simple(context, "Thông báo", msg, negativeText = "Đóng")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}