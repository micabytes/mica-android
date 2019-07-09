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
import java.util.*

class Trait {
  val id: String
  val name: String
  var value: Int
  var isKnown: Boolean = false
  var description: String
  val type: TraitType
  private val references: HashMap<String, String> = HashMap()
  private val effects: HashMap<String, Int> = HashMap()
  private val pixId: Int

  @JsonCreator
  constructor(
      @JsonProperty(GameConstants.ID) oid: String,
      @JsonProperty(GameConstants.NAME) nam: String,
      @JsonProperty(GameConstants.VALUE) vl: Int?,
      @JsonProperty(GameConstants.DESCRIPTION) dsc: String?,
      @JsonProperty(GameConstants.TYPE) typ: String?,
      @JsonProperty(GameConstants.REFERENCES) rfs: List<String>?,
      @JsonProperty(GameConstants.EFFECTS) efs: List<String>?
  ) {
    id = oid
    name = nam
    value = vl ?: 0
    description = dsc ?: ""
    type = if (typ != null) TraitType.valueOf(typ) else TraitType.NONE
    rfs?.forEach {
      val ref = it.split(":")
      references[ref[0]] = ref[1]
    }
    efs?.forEach {
      val eff = it.split(":")
      if (eff.size > 1) effects[eff[0]] = eff[1].toInt()
      else effects[it] = 1
    }
    val con = Game.instance
    val drawId = con.resources.getIdentifier("tr_" + id.toLowerCase(Locale.US), GameConstants.DRAWABLE, con.packageName)
    pixId = if (drawId != 0) drawId else R.drawable.ic_blank
    //if (pixId == R.drawable.ic_blank) Timber.e("Trait", "Unable to identify bitmap for trait: " + id)
  }

  constructor(old: Trait) {
    id = old.id
    name = old.name
    value = old.value
    isKnown = old.isKnown
    description = old.description
    type = old.type
    references.putAll(old.references)
    effects.putAll(old.effects)
    pixId = old.pixId
  }

  constructor(old: Trait, v: Int) {
    id = old.id
    name = old.name
    value = v
    isKnown = old.isKnown
    description = old.description
    type = old.type
    references.putAll(old.references)
    effects.putAll(old.effects)
    pixId = old.pixId
  }

  constructor(old: Trait, v: Int, k: Int, r: Map<String, String>) {
    id = old.id
    name = old.name
    value = v
    isKnown = (k > 0)
    description = old.description
    type = old.type
    references.putAll(old.references)
    references.putAll(r)
    effects.putAll(old.effects)
    pixId = old.pixId
  }

  @Throws(IOException::class)
  fun saveStreamed(g: JsonGenerator) {
    val known = if (isKnown) 1 else 0
    var saveString = "$id|$value|$known"
    for ((keyR, valueR) in references) saveString += "|$keyR:$valueR"
    g.writeString(saveString)
  }

  @Throws(IOException::class)
  fun saveFull(g: JsonGenerator) = g.writeString("$id|$name|$value|$description")

  fun getRefName(): String {
    if (references.isEmpty()) return name
    val world = Game.world
    return try {
      val ref: Map<String, Any> = references.mapValues { world?.get(it.value)?.name ?: "" }
      name.mapVars(ref)
    } catch (e: GameObjectNotFoundException) {
      Timber.e(e)
      name
    }
  }

  val level: Int
    get() = Triangular.reverse(value)

  val info: String
    get() {
      if (references.isEmpty()) return description
      val world = Game.world
      return try {
        val ref: Map<String, Any> = references.mapValues { world?.get(it.value)?.name ?: "" }
        description.mapVars(ref)
      } catch (e: GameObjectNotFoundException) {
        Timber.e(e)
        description
      }
    }

  fun setReference(key: String, value: String) {
    references[key] = value
  }

  fun hasReference(key: String) = references.containsKey(key)

  fun getReference(key: String): String = references[key]!!

  fun getEffect(s: String): Int {
    if (effects.containsKey(s))
      return (effects[s])!!
    return 0
  }

  fun hasEffect(s: String): Boolean = effects.containsKey(s)

  fun getEffects(pattern: String): List<String> {
    val ret = ArrayList<String>()
    for ((key, _) in effects) {
      if (key.startsWith(pattern)) ret.add(key)
    }
    return ret
  }

  val bitmap: Bitmap
    get() = ImageHandler[pixId]

  companion object {
    val NONE = Trait("", "", 0, "", "NONE", null, null)
  }

}

