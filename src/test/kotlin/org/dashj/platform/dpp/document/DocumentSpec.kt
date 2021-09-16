package org.dashj.platform.dpp.document

import org.dashj.platform.dpp.DashPlatformProtocol
import org.dashj.platform.dpp.ProtocolVersion
import org.dashj.platform.dpp.StateRepositoryMock
import org.dashj.platform.dpp.contract.ContractFactory
import org.dashj.platform.dpp.contract.DataContract
import org.dashj.platform.dpp.identifier.Identifier
import org.dashj.platform.dpp.util.Entropy.generateRandomIdentifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Date

class DocumentSpec {

    private val stateRepository = StateRepositoryMock()
    val dpp = DashPlatformProtocol(stateRepository)
    lateinit var dataContract: DataContract
    lateinit var rawDocument: HashMap<String, Any?>
    lateinit var document: Document

    @BeforeEach
    fun beforeEach() {
        val now = Date().time
        val ownerId = generateRandomIdentifier().toBuffer()
        val dataContractFactory = ContractFactory(dpp, stateRepository)

        dataContract = dataContractFactory.create(
            ownerId,
            mapOf(
                "test" to mapOf(
                    "properties" to mapOf(
                        "name" to mapOf(
                            "type" to "string"
                        ),
                        "dataObject" to mapOf(
                            "type" to "object",
                            "properties" to mapOf(
                                "binaryObject" to mapOf(
                                    "type" to "object",
                                    "properties" to mapOf(
                                        "identifier" to mapOf(
                                            "type" to "array",
                                            "byteArray" to true,
                                            "contentMediaType" to Identifier.MEDIA_TYPE,
                                            "minItems" to 32,
                                            "maxItems" to 32,
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )

        rawDocument = hashMapOf(
            "\$protocolVersion" to ProtocolVersion.latestVersion,
            "\$id" to generateRandomIdentifier(),
            "\$type" to "test",
            "\$dataContractId" to dataContract.id,
            "\$ownerId" to ownerId,
            "\$revision" to DocumentCreateTransition.INITIAL_REVISION,
            "\$createdAt" to now,
            "\$updatedAt" to now,
        )

        document = Document(rawDocument, dataContract)
    }

    @Test
    fun `should create Document with all parameters`() {
        val data = mapOf(
            "test" to 1,
        )
        rawDocument = hashMapOf(
            "\$id" to ByteArray(32),
            "\$type" to "test",
            "\$dataContractId" to generateRandomIdentifier().toBuffer(),
            "\$ownerId" to generateRandomIdentifier().toBuffer(),
            "\$revision" to 42,
            "\$createdAt" to Date().time,
            "\$updatedAt" to Date().time
        )
        rawDocument.putAll(data)
        document = Document(rawDocument, dataContract)

        assertEquals(document.id, rawDocument["\$id"])
        assertEquals(document.type, rawDocument["\$type"])
        assertEquals(document.dataContractId, rawDocument["\$dataContractId"])
        assertEquals(document.ownerId, rawDocument["\$ownerId"])
        assertEquals(document.revision, rawDocument["\$revision"])
        assertEquals(document.createdAt, rawDocument["\$createdAt"])
        assertEquals(document.createdAt, rawDocument["\$updatedAt"])
    }

    @Test
    fun `#getId should return ID`() {
        val id = Identifier.from(ByteArray(32) { 1 })
        document.id = id

        val actualId = document.id
        assertEquals(id, actualId)
    }

    @Test
    fun `#getType should return $type`() {
        assertEquals(document.type, rawDocument["\$type"])
    }

    @Test
    fun `#getOwnerId should return $ownerId`() {
        assertEquals(document.ownerId, rawDocument["\$ownerId"])
    }

    @Test
    fun `#getDataContractId should return $dataContractId`() {
        assertEquals(document.dataContractId, rawDocument["\$dataContractId"])
    }

    @Test
    fun `#setRevision should set $revision`() {
        val revision = 5
        val result = document.setRevision(revision)

        assertEquals(result, document)
        assertEquals(document.revision, revision)
    }

    @Test
    fun `#getRevision should return $revision`() {
        val revision = 5
        document.revision = revision

        assertEquals(document.revision, revision)
    }
}
