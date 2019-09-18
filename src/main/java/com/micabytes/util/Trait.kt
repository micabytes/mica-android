package com.micabytes.util

import android.graphics.Bitmap
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonGenerator
import com.micabytes.Game
import com.micabytes.R
import com.micabytes.gfx.ImageHandler
import com.micabytes.math.Triangular
import timber.log.Timber
import java.io.IOException
import java.lang.Exception
import java.lang.NumberFormatException
import java.util.*

class Trait {
  val id: String
  val name: String
  var description: String
  private val values: HashMap<String, Any> = HashMap()
  /*
  var value: Int
  var isKnown: Boolean = false
  val type: TraitType
  private val references: HashMap<String, String> = HashMap()
  */
  private val pixId: Int

  @JsonCreator
  constructor(
    @JsonProperty(GameConstants.ID) oid: String,
    @JsonProperty(GameConstants.NAME) nam: String,
    @JsonProperty(GameConstants.DESCRIPTION) dsc: String?,
    @JsonProperty(GameConstants.VALUES) vls: List<String>?
  ) {
    id = oid
    name = nam
    description = dsc ?: ""
    vls?.forEach {
      val vle = it.split(":")
      if (vle.size > 1) {
        val k = vle[0].trim()
        val v = vle[1].trim()
        if (v.equals("true", ignoreCase = true) || v.equals("false", ignoreCase = true)) {
          values[k.toLowerCase(Locale.US)] = v.toBoolean()
        }
        else {
          try {
            values[k.toLowerCase(Locale.US)] = v.toInt()
          } catch (e: NumberFormatException) {
            values[k.toLowerCase(Locale.US)] = v
          }
        }
      } else values[it.trim().toLowerCase(Locale.US)] = true
    }
    val con = Game.instance
    val drawId = con.resources.getIdentifier(
      "tr_" + id.toLowerCase(Locale.US),
      GameConstants.DRAWABLE,
      con.packageName
    )
    pixId = if (drawId != 0) drawId else R.drawable.ic_blank
    //if (pixId == R.drawable.ic_blank) Timber.e("Trait", "Unable to identify bitmap for trait: " + id)
  }

  constructor(old: Trait) {
    id = old.id
    name = old.name
    description = old.description
    values.putAll(old.values)
    pixId = old.pixId
  }

  @Throws(IOException::class)
  fun saveStream(g: JsonGenerator) {
    var saveString = id
    for ((keyR, valueR) in values) {
      if (keyR.first().isLowerCase()) saveString += "|$keyR:$valueR"
    }
    g.writeString(saveString)
  }

  @Throws(IOException::class)
  fun saveFull(g: JsonGenerator) {
    g.writeString("$id|$name|$description|value:${getIntValue("VALUE")}")
  }

  fun getRefName(): String {
    if (values.isEmpty()) return name
    val world = Game.world
    return try {
      val ref: Map<String, Any> = values.mapValues {
        if (it.value is String) {
          world?.get(it.value.toString())?.name ?: ""
        } else ""
      }.filter { it.value.isNotEmpty() }
      name.mapVars(ref)
    } catch (e: GameObjectNotFoundException) {
      Timber.e(e)
      name
    }
  }

  val info: String
    get() {
      if (values.isEmpty()) return description
      val world = Game.world
      return try {
        val ref: Map<String, Any> = values.mapValues {
          if (it.value is String) {
            world?.get(it.value.toString())?.name ?: ""
          } else ""
        }.filter { it.value.isNotEmpty() }
        description.mapVars(ref)
      } catch (e: GameObjectNotFoundException) {
        Timber.e(e)
        description
      }
    }

  fun hasValue(s: String): Boolean = values.containsKey(s.toLowerCase(Locale.US))

  fun getBooleanValue(s: String): Boolean {
    if (values.containsKey(s)) {
      val ret = values[s]
      if (ret is Boolean)
        return ret
      else
        return true
    }
    return false
  }

  fun getIntValue(s: String): Int {
    val k = s.toLowerCase(Locale.US)
    if (values.containsKey(k)) {
      val ret = values[k]
      if (ret is Int)
        return ret
      else
        return 0
    }
    return 0
  }

  fun getStringValue(s: String): String {
    val k = s.toLowerCase(Locale.US)
    if (values.containsKey(k)) {
      val ret = values[k]
      if (ret is String)
        return ret
      else
        return ret.toString()
    }
    return ""
  }

  fun getValues(pattern: String): List<String> {
    val ret = ArrayList<String>()
    for ((key, _) in values) {
      if (key.startsWith(pattern.toLowerCase(Locale.US))) ret.add(key)
    }
    return ret
  }

  fun setValue(key: String, value: Any) {
    values[key.toLowerCase(Locale.US)] = value
  }

  fun hasValue(key: String, value: Any): Boolean {
    val k = key.toLowerCase(Locale.US)
    if (values.containsKey(k))
      return values[k] == value
    return false
  }

  val bitmap: Bitmap
    get() = ImageHandler[pixId]

  companion object {
    val NONE = Trait("", "", "", null)
  }

}

