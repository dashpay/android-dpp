package org.dashj.platform.dpp.contract

import org.dashj.platform.dpp.util.Entropy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CreateDataContractSpec {
    private lateinit var rawDataContract: MutableMap<String, Any?>

    @BeforeEach
    fun beforeEach() {
        rawDataContract = hashMapOf<String, Any?>(
            "\$id" to Entropy.generateRandomIdentifier().toBuffer(),
            "ownerId" to Entropy.generateRandomIdentifier().toBuffer(),
            "contractId" to Entropy.generateRandomIdentifier().toBuffer(),
            "version" to 1,
            "documents" to mapOf(
                "niceDocument" to mapOf(
                    "name" to mapOf(
                        "to" to "string"
                    )
                )
            )
        )
    }

    @Test
    fun `should return new DataContract with dataContractId and documents`() {
        val dataContract = DataContract(rawDataContract)

        assertEquals(dataContract.ownerId, rawDataContract["ownerId"])
        assertEquals(dataContract.documents, rawDataContract["documents"])
    }

    @Test
    fun `should return new DataContract with $schema if present`() {
        rawDataContract["\$schema"] = "http://test.com/schema"

        val dataContract = DataContract(rawDataContract)

        assertEquals(dataContract.getJsonMetaSchema(), rawDataContract["\$schema"])

        assertEquals(dataContract.ownerId, rawDataContract["ownerId"])
        assertEquals(dataContract.documents, rawDataContract["documents"])
    }

    @Test
    fun `should return new DataContract with $defs if present`() {
        rawDataContract["\$defs"] = mapOf(
            "subSchema" to mapOf("type" to "object")
        )

        val dataContract = DataContract(rawDataContract)

        assertEquals(dataContract.definitions, rawDataContract["\$defs"])

        assertEquals(dataContract.ownerId, rawDataContract["ownerId"])
        assertEquals(dataContract.documents, rawDataContract["documents"])
    }
}
