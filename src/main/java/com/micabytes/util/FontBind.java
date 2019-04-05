package com.micabytes.util;


import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.TextView;

import com.micabytes.gfx.ImageHandler;

import androidx.core.content.ContextCompat;
import androidx.databinding.BindingAdapter;
import de.hdodenhof.circleimageview.CircleImageView;

//import com.bumptech.glide.Glide;

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

  @BindingAdapter("pixId")
  public static void setImageViewResource(ImageView imageView, Integer bmpId) {
    if (bmpId != null) imageView.setImageBitmap(ImageHandler.getJ(bmpId));
  }

  @BindingAdapter("pixId")
  public static void setCircleImageViewResource(CircleImageView imageView, Integer bmpId) {
    imageView.setImageBitmap(ImageHandler.getJ(bmpId));
  }

    /*
    @BindingAdapter("pixT")
    public static void setImageViewResource(ImageView imageView, String url) {
        if (url == null) return;
        int key = Game.getInstance().getResources().getIdentifier(url, "drawable", Game.getInstance().getPackageName());
        Glide.with(imageView)
            .load(key)
            .into(imageView);
    }
    */

  @BindingAdapter({"android:drawableLeft"})
  public static void setDrawableLeft(TextView view, int resourceId) {
    Drawable drawable = ContextCompat.getDrawable(view.getContext(), resourceId);
    setIntrinsicBounds(drawable);
    Drawable[] drawables = view.getCompoundDrawables();
    view.setCompoundDrawables(drawable, drawables[1], drawables[2], drawables[3]);
  }

  private static void setIntrinsicBounds(Drawable drawable) {
    if (drawable != null) {
      drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
    }
  }
}
