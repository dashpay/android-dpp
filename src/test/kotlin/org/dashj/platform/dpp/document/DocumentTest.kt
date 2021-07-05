/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashevo.dpp.document

import org.dashevo.dpp.Fixtures
import org.dashevo.dpp.StateRepositoryMock
import org.dashevo.dpp.identifier.Identifier
import org.dashevo.dpp.util.Cbor
import org.dashevo.dpp.util.Entropy
import org.dashevo.dpp.util.HashUtils
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class DocumentTest {

    val stateRepository = StateRepositoryMock()

    @Test
    fun testDocument() {
        val documents = Fixtures.getDocumentsFixture()

        assertEquals(5, documents.size)
        assertEquals("4mZmxva49PBb7BE7srw9o3gixvDfj1dAx1K2dmAAauGp", documents[2].ownerId.toString())
        assertEquals("9rjz23TQ3rA2agxXD56XeDfw63hHJUwuj7joxSBEfRgX", documents[3].dataContractId.toString())
    }

    @Test
    fun testDocumentFactory() {
        var factory = DocumentFactory(stateRepository)

        val contract = Fixtures.getDataContractFixtures()

        val factoryCreatedDocument = factory.create(contract, Identifier.from("4mZmxva49PBb7BE7srw9o3gixvDfj1dAx1K2dmAAauGp"), "niceDocument", JSONObject("{ name: 'Cutie' }").toMap())
        val fixtureCreatedDocuments = Fixtures.getDocumentsFixture()

        // compare the first document
        assertEquals(fixtureCreatedDocuments[0].dataContractId, factoryCreatedDocument.dataContractId)
        assertEquals(fixtureCreatedDocuments[0].ownerId, factoryCreatedDocument.ownerId)
        assertEquals(fixtureCreatedDocuments[0].type, factoryCreatedDocument.type)
        assertEquals(fixtureCreatedDocuments[0].data["name"], factoryCreatedDocument.data["name"])

        val firstRawDocument = fixtureCreatedDocuments[0].toObject()
        assertArrayEquals(firstRawDocument["\$dataContractId"] as ByteArray, factoryCreatedDocument.dataContractId.toBuffer())
        assertArrayEquals(firstRawDocument["\$ownerId"] as ByteArray, factoryCreatedDocument.ownerId.toBuffer())
        assertEquals(firstRawDocument["\$type"], factoryCreatedDocument.type)
        assertEquals(firstRawDocument["name"], factoryCreatedDocument.data["name"])

        val firstRawDocumentJson = fixtureCreatedDocuments[0].toJSON()
        assertEquals(firstRawDocumentJson["\$dataContractId"] as String, factoryCreatedDocument.dataContractId.toString())
        assertEquals(firstRawDocumentJson["\$ownerId"] as String, factoryCreatedDocument.ownerId.toString())

        assertEquals(fixtureCreatedDocuments[0].get("name") as String, factoryCreatedDocument.data["name"])
    }

    @Test
    fun getTest() {
        val contract = Fixtures.getDataContractFixtures()

        val rawDocument = hashMapOf(
            "\$id" to Entropy.generate(),
            "\$ownerId" to Entropy.generate(),
            "\$dataContractId" to contract.id,
            "\$revision" to 0,
            "\$protocolVersion" to 0,
            "\$type" to "essay",
            "header" to "start-section",
            "body" to hashMapOf(
                "intro" to "Hello, all!",
                "topic" to "Greetings are great for everyone.",
                "conclusion" to "The end!"
            ),
            "footer" to listOf("copyright", "license", "page number")
        )

        val document = Document(rawDocument, contract)
        assertEquals("start-section", document.get("header"))
        assertEquals("Greetings are great for everyone.", document.get("body/topic"))
        assertEquals(null, document.get("abstract/author"))
    }

    @Test
    fun applyStateTransition() {
        val documents = Fixtures.getDocumentsFixture()
        val batchTransition = hashMapOf(
                "create" to documents
        )
        val result = DocumentFactory(stateRepository).createStateTransition(batchTransition)
        for (i in result.transitions.indices)
            assertEquals((result.transitions[0] as DataDocumentTransition).data, documents[0].data)
        assertTrue(result.isDocumentStateTransition())
        assertFalse(result.isDataContractStateTransition())
        assertFalse(result.isIdentityStateTransition())
    }

    @Test @Disabled
    fun verifySignedDocumentsSTTest() {
        //TODO: This test is completely broken, getDocumentsSTSignedFixture() has bad data
        val documentST = Fixtures.getDocumentsSTSignedFixture()
        val identityST = Fixtures.getIdentityCreateSTSignedFixture()
        val identity = Fixtures.getIdentityForSignaturesFixture()
        assertTrue(documentST.verifySignature(identityST.publicKeys[0]))
        assertTrue(documentST.verifySignature(identity.publicKeys[0]))

        val documentSTTwo = Fixtures.getDocumentsSTSignedFixtureTwo()
        assertEquals(documentST.transitions[0].toJSON(), documentSTTwo.transitions[0].toJSON())
    }

    @Test
    fun deepCopyTest() {
        val transitionString = "a6647479706501676f776e65724964582031c2604b7707f3d759dcde10fb38f504fa4dd0eeabbab334e65806310be9be9d697369676e6174757265f66b7472616e736974696f6e7381ab6324696458209e29fee8e7eb0220458987b4235804b695b881c993ee2843e853e1e9f981b82365247479706566646f6d61696e656c6162656c69782d686173682d32306724616374696f6e00677265636f726473a17464617368556e697175654964656e746974794964582031c2604b7707f3d759dcde10fb38f504fa4dd0eeabbab334e65806310be9be9d6824656e74726f70795820eedeb9fffc8e565805bd0699ae01d512c672d4f2bdfb7f27941439e51d5541576c7072656f7264657253616c74582035ee657a5fddc3e6e6aeddc1196086b0d9c2968c533f7bfe691fdf517b5778a76e737562646f6d61696e52756c6573a16f616c6c6f77537562646f6d61696e73f46f2464617461436f6e747261637449645820d837bcfd590cdf658cfb696ed31780aa41bc3408949a73b1c6d47907d4d10b186f6e6f726d616c697a65644c6162656c69782d686173682d3230781a6e6f726d616c697a6564506172656e74446f6d61696e4e616d6564646173686f70726f746f636f6c56657273696f6e00747369676e61747572655075626c69634b65794964f6"

        val transition = DocumentsBatchTransition(Cbor.decode(HashUtils.fromHex(transitionString)))

        val records = (transition.transitions[0] as DocumentCreateTransition).data["records"] as MutableMap<String, Any>

        records["dashUniqueIdentityId"] = Identifier.from(records["dashUniqueIdentityId"])

        // see if the deepCopy is working, if not the type for dashUniqueIdentityId will be a String
        val json = transition.toJSON()

        records["dashUniqueIdentityId"] as Identifier
    }
}