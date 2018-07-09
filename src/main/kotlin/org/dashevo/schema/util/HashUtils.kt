package org.dashevo.schema.util

import org.bitcoinj.core.Sha256Hash
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

object HashUtils {

    private fun buildTree(obj: JSONObject, tree: ArrayList<ByteArray> = arrayListOf()): ArrayList<ByteArray> {
        obj.keys().forEach { key ->
            if (obj[key] is JSONObject) {
                buildTree(obj.getJSONObject(key), tree)
                return@forEach
            }
            val ba = ("{" + "\"key\":" + (
                    if (obj[key] is String) {
                        ("\"" + obj[key] + "\"")
                    } else {
                        obj[key]
                    })+"}").toByteArray(Charset.forName("UTF-8"))
            val hash = ba.asList().reversed().toByteArray()
            tree.add(hash)
        }

        return tree
    }

    /**
     * Return a double SHA256 hash of a Schema object instance
     * @param obj {object} Schema object instance
     * @returns {*|string}
     */
    fun toHash(obj: JSONObject): String {
        val tree = buildTree(obj)
        var j = 0
        var size = tree.size
        while (size > 1) {
            var i = 0
            while (i < size) {
                val i2 = Math.min(i + 1, size - 1)
                val buf = ByteArrayOutputStream()
                buf.write(tree[j + i])
                buf.write(tree[j + i2])
                tree.add(Sha256Hash.hashTwice(buf.toByteArray()))
                i += 2
            }
            j += size
            size = Math.floor(((size + 1) / 2).toDouble()).toInt()
        }

        //TODO: * What to do when data is smaller than 32 bytes
        if (tree.last().size < 32) {
            val byteArray = ByteArray(32)
            var i = 0
            tree.last().iterator().forEach {
                byteArray[i++] = it
            }
            tree[tree.lastIndex] = byteArray
        }

        return Sha256Hash.wrapReversed(tree.last()).toString()
    }

    fun toHash(objList: List<JSONObject>): String {
        val bos = ByteArrayOutputStream()
        objList.forEach {
            bos.write(Sha256Hash.wrap(toHash(it)).bytes)
        }
        return Sha256Hash.hashTwice(bos.toByteArray()).toString()
    }

}