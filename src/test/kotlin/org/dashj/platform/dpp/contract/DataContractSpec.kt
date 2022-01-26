package org.dashj.platform.dpp.contract

import org.dashj.platform.dpp.Metadata
import org.dashj.platform.dpp.errors.InvalidDocumentTypeError
import org.dashj.platform.dpp.identifier.Identifier
import org.dashj.platform.dpp.util.Entropy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DataContractSpec {

    private lateinit var documentType: String
    private lateinit var documentSchema: Map<String, Any?>
    lateinit var documents: Map<String, Any?>
    lateinit var dataContract: DataContract
    lateinit var ownerId: Identifier
    lateinit var entropy: ByteArray
    private lateinit var contractId: Identifier
    private lateinit var metadataFixture: Metadata

    @BeforeEach
    fun beforeEach() {
        documentType = "niceDocument"

        documentSchema = mapOf(
            "properties" to mapOf(
                "nice" to mapOf(
                    "type" to "boolean"
                )
            )
        )

        documents = mutableMapOf(
            documentType to documentSchema
        )

        ownerId = Entropy.generateRandomIdentifier()
        entropy = ByteArray(32)
        contractId = Entropy.generateRandomIdentifier()

        dataContract = DataContract(
            mapOf(
                "\$schema" to DataContract.DEFAULTS.SCHEMA,
                "\$id" to contractId,
                "version" to 1,
                "ownerId" to ownerId,
                "documents" to documents,
                "\$defs" to mapOf<String, Any?>(),
            )
        )

        metadataFixture = Metadata(42, 0)
        dataContract.metadata = metadataFixture
    }

    @Test
    fun `constructor should create DataContract`() {
        val id = Entropy.generateRandomIdentifier()

        dataContract = DataContract(
            mapOf(
                "\$schema" to DataContract.DEFAULTS.SCHEMA,
                "\$id" to id,
                "ownerId" to ownerId,
                "documents" to documents,
                "\$defs" to mapOf<String, Any?>(),
            )
        )

        assertEquals(dataContract.id, id)
        assertEquals(dataContract.ownerId, ownerId)
        assertEquals(dataContract.schema, DataContract.DEFAULTS.SCHEMA)
        assertEquals(dataContract.documents, documents)
        assertEquals(dataContract.definitions, mapOf<String, Any?>())
    }

    @Test
    fun `#getId should return DataContract Identifier`() {
        val result = dataContract.id
        assertEquals(result, contractId)
    }

    @Test
    fun `#getJsonSchemaId should return JSON Schema ID`() {
        val result = dataContract.getJsonSchemaId()
        assertEquals(result, dataContract.id.toString())
    }

    @Test
    fun `#setJsonMetaSchema should set meta schema`() {
        val metaSchema = "http://test.com/schema"

        val result = dataContract.setJsonMetaSchema(metaSchema)

        assertEquals(result, dataContract)
        assertEquals(dataContract.schema, metaSchema)
    }

    @Test
    fun `#getJsonMetaSchema should return meta schema`() {
        val result = dataContract.getJsonMetaSchema()
        assertEquals(result, dataContract.schema)
    }

    @Test
    fun `#setDocuments should set Documents definition`() {
        val anotherDocuments = mutableMapOf<String, Any?>(
            "anotherDocument" to mapOf(
                "properties" to mapOf(
                    "name" to mapOf("type" to "string"),
                )
            )
        )

        dataContract.documents = anotherDocuments
        val result = dataContract.documents
        assertEquals(result, dataContract.documents)
        assertEquals(dataContract.documents, anotherDocuments)
    }

    @Test
    fun `#getDocuments should return Documents definition`() {
        val result = dataContract.documents

        assertEquals(result, dataContract.documents)
    }

    @Test
    fun `#isDocumentDefined should return true if Document schema is defined`() {
        val result = dataContract.isDocumentDefined("niceDocument")

        assertEquals(result, true)
    }

    @Test
    fun `#isDocumentDefined should return false if Document schema is not defined`() {
        val result = dataContract.isDocumentDefined("undefinedDocument")

        assertEquals(result, false)
    }

    @Test
    fun `#setDocumentSchema should set Document schema`() {
        val anotherType = "prettyDocument"
        val anotherDefinition = mapOf(
            "properties" to mapOf(
                "name" to mapOf("type" to "string"),
            )

        )

        val result = dataContract.setDocumentSchema(anotherType, anotherDefinition)
        assertEquals(result, dataContract)
        assertEquals(dataContract.documents[anotherType], anotherDefinition)
    }

    @Test
    fun `#getDocumentSchema should throw error if Document is not defined`() {
        assertThrows<InvalidDocumentTypeError> {
            dataContract.getDocumentSchema("undefinedObject")
        }
    }

    @Test
    fun `#getDocumentSchema should return Document Schema`() {
        val result = dataContract.getDocumentSchema(documentType)
        assertEquals(result, documentSchema)
    }

    @Test
    fun `#getDocumentSchemaRef should throw error if Document is not defined`() {
        assertThrows<InvalidDocumentTypeError> {
            dataContract.getDocumentSchemaRef("undefinedObject")
        }
    }

    @Test
    fun `#getDocumentSchemaRef should return schema with $ref to Document schema`() {
        val result = dataContract.getDocumentSchemaRef(documentType)
        assertEquals(
            result,
            mapOf(
                "\$ref" to "${dataContract.getJsonSchemaId()}#/documents/niceDocument",
            )
        )
    }

    @Test
    fun `#setDefinitions should set $defs`() {
        val defs = mapOf<String, Any?>().toMutableMap()

        dataContract.definitions = defs
        val result = dataContract.definitions

        assertEquals(result, dataContract.definitions)
        assertEquals(dataContract.definitions, defs)
    }

    @Test
    fun `#getDefinitions should return $defs`() {
        val result = dataContract.definitions
        assertEquals(result, dataContract.definitions)
    }

    @Test
    fun `#toJSON should return DataContract as plain object`() {
        val result = dataContract.toJSON()

        assertEquals(
            result,
            mapOf(
                "protocolVersion" to dataContract.protocolVersion,
                "\$id" to contractId.toString(),
                "\$schema" to DataContract.DEFAULTS.SCHEMA,
                "version" to 1,
                "ownerId" to ownerId.toString(),
                "documents" to documents
            )
        )
    }

    @Test
    fun `#toJSON should return plain object with $defs if present`() {
        val defs = mutableMapOf<String, Any?>(
            "subSchema" to mapOf("type" to "object"),
        )

        dataContract.definitions = defs

        val result = dataContract.toJSON()

        assertEquals(
            result,
            mapOf(
                "protocolVersion" to dataContract.protocolVersion,
                "\$id" to contractId.toString(),
                "\$schema" to DataContract.DEFAULTS.SCHEMA,
                "version" to 1,
                "ownerId" to ownerId.toString(),
                "documents" to documents,
                "\$defs" to defs
            )
        )
    }

    @Test
    fun `#setEntropy should set entropy`() {
        dataContract.entropy = entropy
        val result = dataContract.entropy
        assertEquals(result, dataContract.entropy)
        assertEquals(dataContract.entropy, entropy)
    }

    @Test
    fun `#getEntropy should return entropy`() {
        dataContract.entropy = entropy
        val result = dataContract.entropy
        assertEquals(result, dataContract.entropy)
    }

    @Test
    fun `#setMetadata should set metadata`() {
        val otherMetadata = Metadata(43, 1)
        dataContract.metadata = otherMetadata
        assertEquals(dataContract.metadata, otherMetadata)
    }

    @Test
    fun `#getMetadata should get metadata`() {
        assertEquals(dataContract.metadata, metadataFixture)
    }
}
