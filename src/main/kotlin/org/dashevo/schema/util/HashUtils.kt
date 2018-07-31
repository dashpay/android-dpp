package org.dashevo.schema.util

import org.bitcoinj.core.Sha256Hash
import org.dashevo.schema.Serialize
import org.json.JSONObject
import java.io.ByteArrayOutputStream

object HashUtils {

    /**
     * Return a double SHA256 hash of a Schema object instance
     * @param obj {object} Schema object instance
     * @returns {*|string}
     */
    fun toHash(obj: JSONObject): String {
        val byteArr = Serialize.encode(obj)
        //TODO: * Jackson's Cbor seems to be adding additional bytes to the end
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