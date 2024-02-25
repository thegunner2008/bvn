package tamhoang.bvn.util;

import android.content.Context;

public class PixelUtil {
    private PixelUtil() {
    }

    public static int dpToPx(Context context, int dp) {
        return Math.round(((float) dp) * getPixelScaleFactor(context));
    }

    public static int pxToDp(Context context, int px) {
        return Math.round(((float) px) / getPixelScaleFactor(context));
    }

    private static float getPixelScaleFactor(Context context) {
        return context.getResources().getDisplayMetrics().xdpi / 160.0f;
    }
}