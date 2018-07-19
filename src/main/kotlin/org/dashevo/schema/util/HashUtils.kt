package org.dashevo.schema.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule
import org.bitcoinj.core.Sha256Hash
import org.json.JSONObject
import java.io.ByteArrayOutputStream

object HashUtils {

    /**
     * Return a double SHA256 hash of a Schema object instance
     * @param obj {object} Schema object instance
     * @returns {*|string}
     */
    fun toHash(obj: JSONObject): String {
        val cborFactory = CBORFactory()
        val mapper =  ObjectMapper(cborFactory)
        mapper.registerModule(JsonOrgModule())
        val byteArr = mapper.writeValueAsBytes(obj.toMap())
        //TODO: Jackson's Cbor seems to be adding additional bytes to the end
        //byteArr = Arrays.copyOfRange(byteArr, 0, byteArr.size-2)
        return Sha256Hash.wrap(Sha256Hash.hashTwice(byteArr)).toString()
        //Old code in: https://gist.github.com/sambarboza/db5c4d6880f057e899bccd70c846d3d6
    }

    fun toHash(objList: List<JSONObject>): String {
        val bos = ByteArrayOutputStream()
        objList.forEach {
            bos.write(Sha256Hash.wrap(toHash(it)).bytes)
        }
        return Sha256Hash.hashTwice(bos.toByteArray()).toString()
    }

}