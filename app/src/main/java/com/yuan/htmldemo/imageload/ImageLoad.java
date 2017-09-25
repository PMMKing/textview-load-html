package com.yuan.htmldemo.imageload;

import android.content.Context;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.yuan.htmldemo.R;

/**
 * Created by shucheng.qu on 2017/8/24.
 */

public class ImageLoad {

    public static void loadPlaceholder(Context context, String url, Target target) {

        Picasso picasso = new Picasso.Builder(context).loggingEnabled(true).build();
        picasso.load(url)
                .placeholder(R.drawable.moren)
                .error(R.drawable.moren)
                .transform(new ImageTransform())
//                .transform(new CompressTransformation())
                .into(target);
    }

}
