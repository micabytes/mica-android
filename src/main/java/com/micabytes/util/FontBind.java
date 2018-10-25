package com.micabytes.util;


import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.databinding.BindingAdapter;
import de.hdodenhof.circleimageview.CircleImageView;

public class FontBind {

    @BindingAdapter("font")
    public static void setFont(TextView view, String fontName) {
        view.setTypeface(FontHandler.INSTANCE.get(fontName));
    }

    @BindingAdapter("src")
    public static void setImageViewResource(ImageView imageView, int resource) {
        imageView.setImageResource(resource);
    }

    @BindingAdapter("pix")
    public static void setImageViewResource(ImageView imageView, Bitmap bmp) {
        imageView.setImageBitmap(bmp);
    }

    @BindingAdapter("pix")
    public static void setCircleImageViewResource(CircleImageView imageView, Bitmap bmp) {
        imageView.setImageBitmap(bmp);
    }

}
