/**
 * Copyright (c) 2018-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashevo.dpp.contract

import org.dashevo.dpp.BaseObject
import org.dashevo.dpp.errors.InvalidDocumentTypeError

class DataContract(var contractId: String = "", var documents: MutableMap<String, Any?>) : BaseObject() {

    companion object DEFAULTS {
        const val VERSION = 1
        const val SCHEMA = "https://schema.dash.org/dpp-0-4-0/meta/data-contract"
        const val SCHEMA_ID = "dataContract"
    }

    constructor(rawContract: MutableMap<String, Any?>) : this(rawContract["contractId"] as String,
            rawContract["documents"] as MutableMap<String, Any?>)

    var id: String = ""
       get() {
           if (field.isEmpty()) {
               field = contractId
           }
           return field
       }
    var version: Int = VERSION
    var schema: String = SCHEMA
    var definitions = mapOf<String, Any>()

    override fun toJSON(): Map<String, Any> {
        val json = hashMapOf<String, Any>()

        json["\$schema"] = this.schema
        json["contractId"] = this.contractId
        json["version"] = this.version
        json["documents"] = this.documents

        if (!this.definitions.isEmpty()) {
            json["definitions"] = this.definitions
        }

        return json
    }

    fun setDocumentSchema(type: String, schema: Map<String, Any>) {
        this.documents[type] = schema
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