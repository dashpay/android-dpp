/**
 * Copyright (c) 2018-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashevo.dpp.contract

import org.bitcoinj.core.Base58
import org.dashevo.dpp.errors.InvalidDocumentTypeError
import org.dashevo.dpp.toHexString
import org.dashevo.dpp.util.HashUtils

class Contract(var name: String = "", var documents: MutableMap<String, Any>) {

    companion object DEFAULTS {
        const val VERSION = 1.0
        const val SCHEMA = "https://schema.dash.org/dpp-0-4-0/meta/contract"
        const val SCHEMA_ID = "contract"
    }

    var id: String = ""
       get() {
           if (field.isEmpty()) {
               field = Base58.encode(this.serialize())
           }
           return field
       }
    var version: Double = VERSION
    var schema: String = SCHEMA
    var definitions = mapOf<String, Any>()

    fun toJSON(): Map<String, Any> {
        val json = hashMapOf<String, Any>()

        json.put("\$schema", this.schema)
        json.put("\$name", this.name)
        json.put("\$version", this.version)
        json.put("\$documents", this.documents)

        if (!this.definitions.isEmpty()) {
            json.put("definitions", this.definitions)
        }

        return json
    }

    fun serialize(): ByteArray {
        return HashUtils.encode(this.toJSON())
    }

    fun hash(): String {
        return HashUtils.toHash(this.serialize()).toHexString()
    }

    fun setDocumentSchema(type: String, schema: Map<String, Any>) {
        this.documents.put(type, schema)
    }

    fun getJsonSchemaId(): String {
        return SCHEMA_ID
    }

    private fun checkContainsDocumentType(type: String) {
        if (!this.documents.contains(type)) {
            throw InvalidDocumentTypeError(this, type)
        }
    }

    fun getDocumentSchema(type: String): Map<String, Any>{
        checkContainsDocumentType(type)
        return this.documents.get(type) as Map<String, Any>
    }

    fun getDocumentSchemaRef(type: String): Map<String, String> {
        checkContainsDocumentType(type)
        return mapOf("\$ref" to "${this.getJsonSchemaId()}#/documents/$type")
    }

    fun isDocumentDefined(type: String): Boolean {
        return this.documents.contains(type)
    }

}