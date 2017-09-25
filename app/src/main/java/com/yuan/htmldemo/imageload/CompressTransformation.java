package com.yuan.htmldemo.imageload;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import com.squareup.picasso.Transformation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by shucheng.qu on 2017/9/18.
 */

public class CompressTransformation implements Transformation {

    public CompressTransformation() {
    }

    @Override
    public Bitmap transform(Bitmap source) {
        return WeChatBitmapToByteArray(source);
    }


    private Bitmap WeChatBitmapToByteArray(Bitmap source) {

        // 首先进行一次大范围的压缩
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        source.compress(Bitmap.CompressFormat.JPEG, 100, output);
        float zoom = (float) Math.sqrt(100 * 1024 / (float) output.toByteArray().length); //获取缩放比例
        // 设置矩阵数据
        Matrix matrix = new Matrix();
        matrix.setScale(zoom, zoom);
        // 根据矩阵数据进行新bitmap的创建
        Bitmap resultBitmap = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        output.reset();
        resultBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        if (source != resultBitmap) {
            source.recycle();
        }
        // 如果进行了上面的压缩后，依旧大于100K，就进行小范围的微调压缩
//        while (output.toByteArray().length > 100 * 1024) {
//            matrix.setScale(0.9f, 0.9f);//每次缩小 1/10
//
//            resultBitmap = Bitmap.createBitmap(
//                    resultBitmap, 0, 0,
//                    resultBitmap.getWidth(), resultBitmap.getHeight(), matrix, true);
//
//            output.reset();
//            resultBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
//        }
        try {
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultBitmap;
    }

    @Override
    public String key() {
        return "CompressTransformation";
    }
}
