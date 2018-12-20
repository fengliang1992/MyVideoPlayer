package com.fltry.demo01;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * 图片压缩
 */
public class MyBitmapUtils {

    /**
     * 采样率压缩
     *
     * @param bitmap  原图
     * @param quality 压缩质量0-100
     * @param beilv   压缩的倍率
     * @return 压缩后的图片
     */
    public static Bitmap getBitmap(Bitmap bitmap, int quality, int beilv) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = beilv;
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        return BitmapFactory.decodeStream(isBm, null, options);
    }

    /**
     * 质量压缩
     *
     * @param bitmap  原图
     * @param quality 压缩质量0-100
     * @return 压缩后的图片
     */
    public static Bitmap getBitmap(Bitmap bitmap, int quality) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        return BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.size());
    }

    /**
     * 矩阵压缩
     *
     * @param bitmap 原图
     * @return 压缩后的图片
     */
    public static Bitmap getBitmap(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);//bitmap是一张图片
        bitmap = BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.toByteArray().length);
        /*该平方根表示大约缩进zoom倍，实际存储大小会接近32KB，可以自己算一下，就是长乘以宽*/
        float zoom = (float) Math.sqrt(32 * 1024 / (float) baos.toByteArray().length);
        Matrix matrix = new Matrix();
        matrix.setScale(zoom, zoom);
        Bitmap resultBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        baos.reset();
        resultBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        /*压缩到32KB为止*/
        while (baos.toByteArray().length > 32 * 1024) {
            matrix.setScale(0.8f, 0.8f);
            resultBitmap = Bitmap.createBitmap(resultBitmap, 0, 0, resultBitmap.getWidth(), resultBitmap.getHeight(), matrix, true);
            baos.reset();
            resultBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        }
        return resultBitmap;
    }
}
