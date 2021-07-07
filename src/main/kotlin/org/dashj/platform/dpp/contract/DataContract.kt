/**
 * Copyright (c) 2018-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashj.platform.dpp.contract

import org.dashj.platform.dpp.BaseObject
import org.dashj.platform.dpp.errors.InvalidDocumentTypeError
import org.dashj.platform.dpp.identifier.Identifier

class DataContract(
    val id: Identifier,
    val ownerId: Identifier,
    val protocolVersion: Int,
    val schema: String,
    val documents: MutableMap<String, Any?>,
    var definitions: MutableMap<String, Any?> = hashMapOf()
) : BaseObject() {

    companion object DEFAULTS {
        const val SCHEMA = "https://schema.dash.org/dpp-0-4-0/meta/data-contract"
        const val PROTOCOL_VERSION: Int = 0
    }

    var entropy: ByteArray? = null

    constructor(
        id: ByteArray,
        ownerId: ByteArray,
        protocolVersion: Int,
        schema: String,
        documents: MutableMap<String, Any?>,
        definitions: MutableMap<String, Any?>
    ) : this(
        Identifier.from(id), Identifier.from(ownerId),
        protocolVersion, schema,
        documents, definitions
    )

    constructor(rawContract: MutableMap<String, Any?>) : this(
        Identifier.from(rawContract["\$id"]!!),
        Identifier.from(rawContract["ownerId"]!!),
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

    override fun toObject(): Map<String, Any> {
        return toObject(false)
    }

    fun toObject(skipIdentifierConversion: Boolean): MutableMap<String, Any> {
        val rawDataContract = hashMapOf(
            "protocolVersion" to protocolVersion,
            "\$id" to id,
            "\$schema" to schema,
            "ownerId" to ownerId,
            "documents" to documents
        )

        if (!skipIdentifierConversion) {
            rawDataContract["\$id"] = id.toBuffer()
            rawDataContract["ownerId"] = ownerId.toBuffer()
        }

        if (this.definitions.isNotEmpty()) {
            rawDataContract["definitions"] = this.definitions
        }

        return rawDataContract
    }

    override fun toJSON(): Map<String, Any> {
        val json = toObject(false)
        json["\$id"] = this.id.toString()
        json["ownerId"] = this.ownerId.toString()

        return json
    }

    fun setDocumentSchema(type: String, schema: Map<String, Any?>) = apply {
        this.documents[type] = schema
    }

    fun getJsonSchemaId(): String {
        return id.toString()
    }

    fun getJsonMetaSchema(): String {
        return schema
    }

    private fun checkContainsDocumentType(type: String) {
        if (!this.documents.contains(type)) {
            throw InvalidDocumentTypeError(this, type)
        }
    }

    fun getDocumentSchema(type: String): Map<String, Any> {
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
