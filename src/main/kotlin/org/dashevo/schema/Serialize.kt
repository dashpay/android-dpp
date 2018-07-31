package org.dashevo.schema

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule
import org.json.JSONObject

object Serialize {

    private val cbor = ObjectMapper(CBORFactory())

    init {
        cbor.registerModule(JsonOrgModule())
    }

    fun encode(obj: JSONObject): ByteArray {
        return cbor.writeValueAsBytes(obj)
    }

    fun decode(byteArray: ByteArray): JSONObject {
        return cbor.readValue(byteArray, JSONObject::class.java)
    }

}