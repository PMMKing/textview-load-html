package com.yuan.htmldemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.yuan.htmldemo.html.HtmlUtils;

public class MainActivity extends AppCompatActivity {


    private static String HTML = "<p>\r\n\t建华大街房间等你发链接文林街\r\n</p>\r\n<p>\r\n\t" +
            "<img src=\"https://img.alicdn.com/imgextra/i3/725677994/TB2ALE4XMn.PuJjSZFkXXc_lpXa_!!725677994.jpg\" title=\"金沙河\" alt=\"金沙河\" />\n" +
            "<img src=\"https://img.alicdn.com/imgextra/i4/1129326215/TB2MZ3.gFXXXXcYXXXXXXXXXXXX_!!1129326215.jpg\" alt=\"\" />\n" +
            "<img src=\"https://img.alicdn.com/imgextra/i4/1129326215/TB2_NkRgFXXXXcaXpXXXXXXXXXX_!!1129326215.jpg\" alt=\"\" />\n" +
            "<img src=\"https://img.alicdn.com/imgextra/i2/725677994/TB2ITHQhCVmpuFjSZFFXXcZApXa_!!725677994.jpg\" alt=\"\" />\n" +
            "<img src=\"https://img.alicdn.com/imgextra/i4/725677994/TB27EY6hypnpuFjSZFIXXXh2VXa_!!725677994.jpg\" alt=\"\" />\n" +
            "<img src=\"https://img.alicdn.com/imgextra/i1/1129326215/TB2V2JXgVXXXXcUXXXXXXXXXXXX_!!1129326215.jpg\" alt=\"\" />\n" +
            "<img src=\"https://img.alicdn.com/imgextra/i1/1129326215/TB2r38kgVXXXXaaXXXXXXXXXXXX_!!1129326215.jpg\" alt=\"\" />\n" +
            "<img src=\"https://img.alicdn.com/imgextra/i1/1129326215/TB2nz0mgVXXXXXSXXXXXXXXXXXX_!!1129326215.jpg\" alt=\"\" />\n" +
            "<img src=\"https://img.alicdn.com/imgextra/i4/1129326215/TB2njg.gFXXXXXfXpXXXXXXXXXX_!!1129326215.jpg\" alt=\"\" />\n" +
            "<img src=\"https://img.alicdn.com/imgextra/i2/1129326215/TB2KatngVXXXXadXXXXXXXXXXXX_!!1129326215.jpg\" alt=\"\" />\n" +
            "\n</p>";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView textView = (TextView) findViewById(R.id.textview);
        textView.setText(HtmlUtils.getHtml(getApplicationContext(),textView,HTML));
    }
}
