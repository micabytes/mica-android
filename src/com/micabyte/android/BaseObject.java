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
package com.micabyte.android;

import android.content.Context;

/**
 * BaseObject is a generic Object that contains a number of frequently used attributes.
 * 
 * @author micabyte
 */
public abstract class BaseObject {
    // ID of Object
    private String id_ = null;
    // Name of Object
    private String name_ = null;
    // Value of Object
    private int value_ = 0;
    // Tag of Object
    private String tag_ = null;

    protected BaseObject(String id, String name) {
        this.id_ = id;
        this.name_ = name;
    }

    protected BaseObject(String id, String name, int v, String t) {
        this.id_ = id;
        this.name_ = name;
        this.value_ = v;
        this.tag_ = t;
    }

    public String getId() {
        return this.id_;
    }

    public boolean equalsId(String id) {
        return this.id_.equalsIgnoreCase(id);
    }

    public String getName() {
        return this.name_;
    }

    public void setName(String name) {
        this.name_ = name;
    }

    public int getValue() {
        return this.value_;
    }

    public void setValue(int v) {
        this.value_ = v;
    }

    public String getTag() {
        return this.tag_;
    }

    public void setTag(String t) {
        this.tag_ = t;
    }

    /*
     * Basic methods for extracting various kinds of data from a BaseObject. Used for scripting and
     * text replacement in strings (see StringHandler) where it is useful to retrieve data from many
     * different types of game objects.
     */
    public abstract int getInteger(String id);

    public abstract double getDouble(String id);

    public abstract String getString(Context c, String id);

}
