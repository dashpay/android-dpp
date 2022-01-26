/**
 * Copyright (c) 2018-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashj.platform.dpp.contract

import org.dashj.platform.dpp.BaseObject
import org.dashj.platform.dpp.Metadata
import org.dashj.platform.dpp.ProtocolVersion
import org.dashj.platform.dpp.errors.InvalidDocumentTypeError
import org.dashj.platform.dpp.identifier.Identifier

class DataContract(
    protocolVersion: Int,
    val id: Identifier,
    val ownerId: Identifier,
    var version: Int,
    var schema: String,
    var documents: MutableMap<String, Any?>,
    var definitions: MutableMap<String, Any?> = hashMapOf()
) : BaseObject(protocolVersion) {

    companion object DEFAULTS {
        const val SCHEMA = "https://schema.dash.org/dpp-0-4-0/meta/data-contract"
    }

    var entropy: ByteArray? = null
    var metadata: Metadata? = null

    constructor(
        protocolVersion: Int,
        id: ByteArray,
        ownerId: ByteArray,
        version: Int,
        schema: String,
        documents: MutableMap<String, Any?>,
        definitions: MutableMap<String, Any?>
    ) : this(
        protocolVersion,
        Identifier.from(id), Identifier.from(ownerId),
        version, schema,
        documents, definitions
    )

    constructor(rawContract: Map<String, Any?>) : this(
        if (rawContract.containsKey("protocolVersion")) {
            rawContract["protocolVersion"] as Int
        } else {
            ProtocolVersion.latestVersion
        },
        Identifier.from(rawContract["\$id"]!!),
        Identifier.from(rawContract["ownerId"]!!),
        if (rawContract.containsKey("version")) {
            rawContract["version"] as Int
        } else { 1 },
        if (rawContract.containsKey("\$schema")) {
            rawContract["\$schema"] as String
        } else {
            ""
        },
        rawContract["documents"] as MutableMap<String, Any?>,
        if (rawContract.containsKey("\$defs") &&
            rawContract["\$defs"] is MutableMap<*, *> &&
            (rawContract["\$defs"] as MutableMap<String, Any?>).isNotEmpty()
        ) {
            rawContract["\$defs"] as MutableMap<String, Any?>
        } else {
            mutableMapOf()
        }
    )

    fun incrementVersion() {
        version += 1
    }

    override fun toObject(): Map<String, Any> {
        return toObject(false)
    }

    fun toObject(skipIdentifierConversion: Boolean): MutableMap<String, Any> {
        val rawDataContract = hashMapOf(
            "protocolVersion" to protocolVersion,
            "\$id" to id,
            "\$schema" to schema,
            "version" to version,
            "ownerId" to ownerId,
            "documents" to documents
        )

        if (!skipIdentifierConversion) {
            rawDataContract["\$id"] = id.toBuffer()
            rawDataContract["ownerId"] = ownerId.toBuffer()
        }

        if (this.definitions.isNotEmpty()) {
            rawDataContract["\$defs"] = this.definitions
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

    fun setJsonMetaSchema(schema: String): DataContract {
        this.schema = schema
        return this
    }

    private fun checkContainsDocumentType(type: String) {
        if (!this.documents.contains(type)) {
            throw InvalidDocumentTypeError(this, type)
        }
    }

    @Suppress("UNCHECKED_CAST")
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
