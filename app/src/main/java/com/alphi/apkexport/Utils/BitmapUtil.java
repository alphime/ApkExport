package com.alphi.apkexport.Utils;
/*
  IDEA 2022/03/12
 */

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

public class BitmapUtil {
    public static Bitmap drawableToBitmap150(Drawable drawable) {
        if (drawable != null && drawable.getIntrinsicHeight() > 0 && drawable.getIntrinsicWidth() > 0) {
            Bitmap bitmap = Bitmap.createBitmap(150, 150, Bitmap.Config.ARGB_8888);
            // 创建画板
            Canvas canvas = new Canvas(bitmap);
            // 设置图像大小
            drawable.setBounds(0, 0, 150, 150);
            drawable.draw(canvas);
            return bitmap;
        }
        return null;
    }
}
