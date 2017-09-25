实际上android中的textview是提供了加载html代码的功能的，使用的方法也很简单
```
textView.setText(Html.fromHtml("html",null,null));
```
这样调用应对简单的html文本是没有问题的，但是遇到包含图片的html代码就会导致图片加载失败，显示一个小方框

![图片加载失败](http://upload-images.jianshu.io/upload_images/2492054-795b67d25facb1b3.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/320)
类似上面这样，虽然可以加载html，但是会导致图片加载失败。
那么，问题来了，怎么才能加载图片呢？不要心急，咱们一一道来。
##加载图片
仔细看上面的代码可以发现，最后两个参数传入的是null，本来应该传入什么呢？查看一下源码发现第二个参数传入的是  Html.ImageGetter ，这个接口的作用就是当解析到<img>标签时就会回调getDrawable()方法，并返回一个Drawable对象；那就简单了，写一个类实现这个接口吧。
```
public class URLImageGetter implements ImageGetter {
    Context c;
    TextView tv_image;
    private List<Target> targets = new ArrayList<>();

    public URLImageGetter(TextView t, Context c) {
        this.tv_image = t;
        this.c = c;
        tv_image.setTag(targets);
    }

    @Override
    public Drawable getDrawable(final String source) {
        final URLDrawable urlDrawable = new URLDrawable();
        final Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Drawable drawable = new BitmapDrawable(bitmap);
                drawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
                urlDrawable.setDrawable(drawable);
                urlDrawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
                tv_image.invalidate();
                tv_image.setText(tv_image.getText());
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                errorDrawable.setBounds(0, 0, errorDrawable.getIntrinsicWidth(), errorDrawable.getIntrinsicHeight());
                urlDrawable.setBounds(0, 0, errorDrawable.getIntrinsicWidth(), errorDrawable.getIntrinsicHeight());
                urlDrawable.setDrawable(errorDrawable);
                tv_image.invalidate();
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                placeHolderDrawable.setBounds(0, 0, placeHolderDrawable.getIntrinsicWidth(), placeHolderDrawable.getIntrinsicHeight());
                urlDrawable.setBounds(0, 0, placeHolderDrawable.getIntrinsicWidth(), placeHolderDrawable.getIntrinsicHeight());
                urlDrawable.setDrawable(placeHolderDrawable);
                tv_image.invalidate();
            }
        };

        targets.add(target);
        ImageLoad.loadPlaceholder(c, source, target);
        return urlDrawable;
    }
}
```
构造方法就不用讲了，着重看一下getDrawable(final String source) 这个方法。
提供一个img路径，返回一个drawable对象，这不就是图片加载吗。好办，正好项目中导入了Picasso，查文档法发现可以是实现的方法有两个，一个是picasso同步的get方式，另外一个就是传入一个Target 实现回调。
但是问题出现了，怎么把异步回调的drawable返回呢？简单，自己包装一个drawable，当返回正确drawable的时候填充进去，刷新一下显示内容不就好了吗。
```
public class URLDrawable extends BitmapDrawable {
    private Drawable drawable;

    @Override
    public void draw(Canvas canvas) {
        if (drawable != null) {
            drawable.draw(canvas);
        }
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }

}
```
so easy
构造一个Target实例实现三个方法，onBitmapLoaded加载成功的回调方法，onBitmapFailed加载失败的回调，onPrepareLoad加载中的回调，用于占位图。
```
final Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Drawable drawable = new BitmapDrawable(bitmap);
                drawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
                urlDrawable.setDrawable(drawable);
                urlDrawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
                tv_image.invalidate();
                tv_image.setText(tv_image.getText());
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                errorDrawable.setBounds(0, 0, errorDrawable.getIntrinsicWidth(), errorDrawable.getIntrinsicHeight());
                urlDrawable.setBounds(0, 0, errorDrawable.getIntrinsicWidth(), errorDrawable.getIntrinsicHeight());
                urlDrawable.setDrawable(errorDrawable);
                tv_image.invalidate();
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                placeHolderDrawable.setBounds(0, 0, placeHolderDrawable.getIntrinsicWidth(), placeHolderDrawable.getIntrinsicHeight());
                urlDrawable.setBounds(0, 0, placeHolderDrawable.getIntrinsicWidth(), placeHolderDrawable.getIntrinsicHeight());
                urlDrawable.setDrawable(placeHolderDrawable);
                tv_image.invalidate();
            }
        };
```
看一下 ImageLoad.loadPlaceholder(c, source, target); 里面是怎么实现的
```
public static void loadPlaceholder(Context context, String url, Target target) {

        Picasso picasso = new Picasso.Builder(context).loggingEnabled(true).build();
        picasso.load(url)
                .placeholder(R.drawable.moren)
                .error(R.drawable.moren)
                .transform(new ImageTransform())
                .into(target);
    }
```
很简单，就几行代码，看一下就明白。
其中transform(new ImageTransform()) 是图片变换的，可以自定义实现图片展示的样子，这里我加了一个图片比例缩放的变换，和屏幕宽度匹配。
####注意，这里有坑了。
Picasso中的target为弱引用，如果虚拟机发生GC，target就会被系统回收，导致收不到回调，图片加载失败，解决的办法就是把target改变为强引用，不让系统回收，构造一个list，把target放到list中设置为传入textview的tag，解决问题。
然后把新建的ImageGetter 实例化传入
```
textView.setText(Html.fromHtml("html",new URLImageGetter(textView, context),null));
```
编译，运行

![加载成功](http://upload-images.jianshu.io/upload_images/2492054-b7912c9a99667f47.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/320)

撒花，鼓掌。
###添加点击事件
既然图片已经加载成功，接下来的步骤就是添加点击事件了。
点击事件的实现需要用到
```
textView.setText(Html.fromHtml("html",new URLImageGetter(textView, context),null));
```
中第三个参数，自定义一个类实现Html.TagHandler 接口，
```
public class URLTagHandler implements TagHandler {

    private Context mContext;

    public URLTagHandler(Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
        // 处理标签<img>
        if (tag.toLowerCase(Locale.getDefault()).equals("img")) {
            // 获取长度
            int len = output.length();
            // 获取图片地址
            ImageSpan[] images = output.getSpans(len - 1, len, ImageSpan.class);
            String imgURL = images[0].getSource();
            // 使图片可点击并监听点击事件
            output.setSpan(new ClickableImage(mContext, imgURL), len - 1, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
}
```
handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) 方法中获取到图片的路径并添加点击事件，set一个ClickableSpan即可
```
 private class ClickableImage extends ClickableSpan {
        private String url;
        private Context context;

        public ClickableImage(Context context, String url) {
            this.context = context;
            this.url = url;
        }

        @Override
        public void onClick(View widget) {
            // 进行图片点击之后的处理
        }
    }
```
#####注意，添加点击事件，textview必须设置
```
//设置超链接可以打开网页//click must
textView.setMovementMethod(LinkMovementMethod.getInstance());
```
否则不能获取点击焦点
那么图片放大怎么搞呢？参考了网上的解决思路，使用popwindow。
```
//popwindow layout
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/image_scale_rll"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#66000000"
    android:clickable="true">

    <com.yuan.htmldemo.html.ZoomImageView
        android:id="@+id/image_scale_image"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:scaleType="matrix"
        android:layout_centerInParent="true" />

</RelativeLayout>
```
重新写TarHandler的实现类
```
public class URLTagHandler implements TagHandler {

    private Context mContext;
    private PopupWindow popupWindow;
    //需要放大的图片
    private ZoomImageView tecent_chat_image;

    public URLTagHandler(Context context) {
        mContext = context.getApplicationContext();
        View popView = LayoutInflater.from(context).inflate(R.layout.pub_zoom_popwindow_layout, null);
        tecent_chat_image = (ZoomImageView) popView.findViewById(R.id.image_scale_image);

        popView.findViewById(R.id.image_scale_rll).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }
            }
        });
        popupWindow = new PopupWindow(popView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);// 设置允许在外点击消失
        ColorDrawable dw = new ColorDrawable(0x50000000);
        popupWindow.setBackgroundDrawable(dw);
    }

    @Override
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
        // 处理标签<img>
        if (tag.toLowerCase(Locale.getDefault()).equals("img")) {
            // 获取长度
            int len = output.length();
            // 获取图片地址
            ImageSpan[] images = output.getSpans(len - 1, len, ImageSpan.class);
            String imgURL = images[0].getSource();
            // 使图片可点击并监听点击事件
            output.setSpan(new ClickableImage(mContext, imgURL), len - 1, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private class ClickableImage extends ClickableSpan {
        private String url;
        private Context context;

        public ClickableImage(Context context, String url) {
            this.context = context;
            this.url = url;
        }

        @Override
        public void onClick(View widget) {
            // 进行图片点击之后的处理
            popupWindow.showAtLocation(widget, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
            final Target target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    tecent_chat_image.setImageBitmap(bitmap);
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    tecent_chat_image.setImageDrawable(errorDrawable);
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                    tecent_chat_image.setImageDrawable(placeHolderDrawable);
                }
            };
            tecent_chat_image.setTag(target);
            ImageLoad.loadPlaceholder(context, url, target);
        }
    }
}
```
添加popwindow展示图片放大即可，编译运行

![图片缩放](http://upload-images.jianshu.io/upload_images/2492054-c695f7e5d0370ae8.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/320)
完美收工！！！

最后，在介绍一种同步加载方法，这样加载的好处是不用把target设置为强引用，但是有bug，图片显示不完整，drawable的setBound 方法无效，暂时没找到原因。其他代码省略，只展示加载图片的代码
```
ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.submit(new Runnable() {
            @Override
            public void run() {

                try {
                    Bitmap bitmap = Picasso.with(c).load(source).get();
                    Drawable drawable = new BitmapDrawable(bitmap);
                    drawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
                    urlDrawable.setDrawable(drawable);
                    urlDrawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
                    tv_image.invalidate();
                    tv_image.setText(tv_image.getText());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Bitmap bitmap = null;
                try {
                    bitmap = Picasso.with(c).load(source).get();
                } catch (IOException e) {
                    e.printStackTrace();
                    //fail
                    Drawable drawable = c.getResources().getDrawable(R.drawable.moren);
                    drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                    urlDrawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                    urlDrawable.setDrawable(drawable);
                    tv_image.invalidate();
                }

                if(bitmap == null){
                    //fail
                    Drawable drawable = c.getResources().getDrawable(R.drawable.moren);
                    drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                    urlDrawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                    urlDrawable.setDrawable(drawable);
                    tv_image.invalidate();
                }else {
                    //success
                    Drawable drawable = new BitmapDrawable(bitmap);
                    drawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
                    urlDrawable.setDrawable(drawable);
                    urlDrawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
                    tv_image.invalidate();
                    tv_image.setText(tv_image.getText());
                }
            }
        });
```
这里的例子是使用的Picasso，如果项目中使用的其他图片加载库也没关系，只要能把url加载成drawable 就可以了。核心思想是一样的。