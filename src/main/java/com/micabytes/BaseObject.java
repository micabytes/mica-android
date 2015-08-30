/*
 * Copyright 2013 MicaByte Systems
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.micabytes;

import android.support.annotation.NonNull;

import com.micabytes.util.GameConstants;
import com.micabytes.util.StringHandler;

import org.jetbrains.annotations.NonNls;

import java.util.Locale;

/**
 * BaseObject is a generic Object that contains a number of frequently used attributes.
 *
 * @author micabyte
 */
public class BaseObject {
  protected static final BaseObject ERROR_OBJECT = new BaseObject();
  protected static final BaseObject NO_OBJECT = new BaseObject();

  @NonNls private String id = "";
  private String name = "";
  private int value;

  protected BaseObject() {
    // NOOP
  }

  protected BaseObject(String oid, String nam) {
    id = oid;
    name = nam;
  }

  protected BaseObject(String oid, String nam, int val) {
    id = oid;
    name = nam;
    value = val;
  }

  @Override
  public String toString() {
    return StringHandler.HASH_MARK + id;
  }

  public String getId() {
    return id;
  }

  public void setId(String oid) {
    id = oid;
  }

  public boolean equalsId(@NonNls String oid) {
    return id.equalsIgnoreCase(oid);
  }

  public String getName() {
    return name;
  }

  public void setName(String nam) {
    name = nam;
  }

  public int getValue() {
    return value;
  }

  public void setValue(int val) {
    value = val;
  }

  public boolean isEmpty() {
    return id.isEmpty() && name.isEmpty() && value == 0;
  }

  /*
   * Basic methods for extracting various kinds of data from a BaseObject. Used for scripting and
   * text replacement in strings (see StringHandler) where it is useful to retrieve data from many
   * different types of game objects.
   */
  private enum ValueToken {
    ERROR,
    NAME,
    VALUE,
    ME;

    public static ValueToken get(String str) {
      try {
        String str1 = str.trim();
        String str2 = str1.toUpperCase(Locale.US);
        return valueOf(str2);
      } catch (IllegalArgumentException ignored) {
        return ERROR;
      }
    }
  }

  public int getInteger(@NonNls String s) {
    if (ValueToken.get(s) == ValueToken.VALUE)
      return value;
    return Integer.MIN_VALUE;
  }

  @NonNull
  public String getString(@NonNls String s) {
    switch (ValueToken.get(s)) {
      case NAME:
        return getName();
      case VALUE:
        return Integer.toString(value);
      default:
        return GameConstants.ERROR;
    }
  }

  @NonNull
  public BaseObject getObject(@NonNls String s) {
    //ValueToken token = ValueToken.get(s);
    return GameConstants.THIS.equalsIgnoreCase(id) ? this : ERROR_OBJECT;
  }

}
