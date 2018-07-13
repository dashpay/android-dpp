package org.dashevo.schema

import org.dashevo.schema.Object.DAPOBJECTS
import org.dashevo.schema.Object.STPACKET
import org.dashevo.schema.util.HashUtils
import org.dashevo.schema.util.JsonSchemaUtils
import org.json.JSONObject

object Hash {

    fun subtx(subtx: JSONObject): String {
        return HashUtils.toHash(Object.fromObject(subtx))
    }

    fun blockchainuser(obj: JSONObject): String {
        return HashUtils.toHash(Object.fromObject(obj))
    }

    fun stheader(obj: JSONObject): String {
        return HashUtils.toHash(obj)
    }

    fun stpacket(obj: JSONObject, dapSchema: JSONObject): String {
        val objList = arrayListOf(obj)

        //TODO: * Different from JS Lib, however, js lib seems to be hashing string chars instead of properties: needs verification.
        val stPacket = obj.getJSONObject(STPACKET)
        return if (stPacket.has(DAPOBJECTS)) {
            val dapObjects = stPacket.getJSONArray(DAPOBJECTS)
            for (i in 0..dapObjects.length()) {
                val dapObject = Object.fromObject(dapObjects.getJSONObject(i), dapSchema)
                if (dapObject != null) {
                    objList.add(dapObject)
                }
            }
            HashUtils.toHash(objList)
        } else {
            HashUtils.toHash(obj)
        }
    }

    fun dapcontract(obj: JSONObject): String {
        return HashUtils.toHash(Object.fromObject(obj))
    }

    fun dapschema(obj: JSONObject): String {
        return HashUtils.toHash(Object.fromObject(obj))
    }

    fun dapobject(obj: JSONObject, dapSchema: JSONObject): String {
        return HashUtils.toHash(Object.fromObject(obj, dapSchema)!!)
    }

}