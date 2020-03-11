/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashevo.dpp.util

import java.util.ArrayList
import java.util.HashMap

import org.json.JSONArray
import org.json.JSONObject


object JsonUtils {

    //https://stackoverflow.com/questions/21720759/convert-a-json-string-to-a-hashmap

    fun jsonTextToMap(jsonText: String): MutableMap<String, Any> {
        return jsonToMap(JSONObject(jsonText))
    }

    fun jsonToMap(json: JSONObject?): MutableMap<String, Any> {
        var retMap: MutableMap<String, Any> = HashMap()

        if (json != null) {
            retMap = toMap(json)
        }
        return retMap
    }

    fun toMap(`object`: JSONObject): MutableMap<String, Any> {
        val map = HashMap<String, Any>()

        val keysItr = `object`.keySet().iterator()
        while (keysItr.hasNext()) {
            val key = keysItr.next()
            var value = `object`.get(key)

            if (value is JSONArray) {
                value = toList(value)
            } else if (value is JSONObject) {
                value = toMap(value)
            }
            map[key] = value
        }
        return map
    }

    fun toList(array: JSONArray): List<Any> {
        val list = ArrayList<Any>()
        for (i in 0 until array.length()) {
            var value = array.get(i)
            if (value is JSONArray) {
                value = toList(value)
            } else if (value is JSONObject) {
                value = toMap(value)
            }
            list.add(value)
        }
        return list
    }
}