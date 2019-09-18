package com.micabytes.util

import android.graphics.Typeface
import com.micabytes.Game
import java.util.*


object FontHandler {
  private const val FONT_DIR = "fonts"
  private val CACHE = HashMap<String, Typeface>()

  operator fun get(fontName: String): Typeface? {
    return if (CACHE.containsKey(fontName)) {
      CACHE[fontName]
    } else {
      val typeface = Typeface.createFromAsset(Game.instance.assets, FONT_DIR + StringHandler.SLASH + fontName + ".ttf")
      CACHE[fontName] = typeface
      typeface
    }
  }

  /*
  @BindingAdapter("app:font")
  @JvmStatic
  fun setFont(view: TextView, fontName: String) {
    view.typeface = FontHandler[fontName]
  }

  @BindingAdapter("app:font")
  fun setFont(view: Button, fontName: String) {
    view.typeface = FontHandler[fontName]
  }
  */

}

