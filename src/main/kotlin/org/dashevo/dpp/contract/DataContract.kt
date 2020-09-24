/**
 * Copyright (c) 2018-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashevo.dpp.contract

import org.dashevo.dpp.BaseObject
import org.dashevo.dpp.errors.InvalidDocumentTypeError

class DataContract(var id: String,
                   var ownerId: String,
                   val protocolVersion: Int,
                   var schema: String,
                   var documents: MutableMap<String, Any?>,
                   var definitions: MutableMap<String, Any?> = hashMapOf()) : BaseObject() {

    companion object DEFAULTS {
        const val SCHEMA = "https://schema.dash.org/dpp-0-4-0/meta/data-contract"
        const val PROTOCOL_VERSION: Int = 0
    }

    var entropy: String? = null

    constructor(rawContract: MutableMap<String, Any?>) : this(rawContract["\$id"] as String,
            rawContract["ownerId"] as String,
            if (rawContract.containsKey("protocolVersion"))
                rawContract["protocolVersion"] as Int
            else PROTOCOL_VERSION,
            rawContract["\$schema"] as String,
            rawContract["documents"] as MutableMap<String, Any?>,
            if (rawContract.containsKey("definitions"))
                rawContract["definitions"] as MutableMap<String, Any?>
            else
                hashMapOf()
    )

    override fun toJSON(): Map<String, Any> {
        val json = hashMapOf<String, Any>()
        json["\$id"] = this.id
        json["protocolVersion"] = this.protocolVersion
        json["\$schema"] = this.schema
        json["ownerId"] = this.ownerId
        json["documents"] = this.documents

        if (this.definitions.isNotEmpty()) {
            json["definitions"] = this.definitions
        }

        return json
    }

    fun setDocumentSchema(type: String, schema: Map<String, Any?>) = apply {
        this.documents[type] = schema
    }

    fun getJsonSchemaId(): String {
        return id
    }

    fun setJsonSchemaId(id: String) = apply {
        this.id = id
    }

    fun getJsonMetaSchema(): String {
        return schema
    }

    fun setJsonMetaSchema(schema: String) = apply {
        this.schema = schema
    }

    private fun checkContainsDocumentType(type: String) {
        if (!this.documents.contains(type)) {
            throw InvalidDocumentTypeError(this, type)
        }
    }

    fun getDocumentSchema(type: String): Map<String, Any>{
        checkContainsDocumentType(type)
        return this.documents[type] as Map<String, Any>
    }

    fun getDocumentSchemaRef(type: String): Map<String, String> {
        checkContainsDocumentType(type)
        return mapOf("\$ref" to "${this.getJsonSchemaId()}#/documents/$type")
    }

    fun isDocumentDefined(type: String): Boolean {
        return this.documents.contains(type)
    }
}