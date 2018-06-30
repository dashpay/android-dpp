package org.dashevo.schema

import org.json.JSONObject

object Schema {

    val system: JSONObject = JSONObject(hashMapOf("pver" to ""))

    val Create = org.dashevo.schema.Create
    val Object = org.dashevo.schema.Object
    val Hash = org.dashevo.schema.Hash
    val Validate = org.dashevo.schema.Validate

}
