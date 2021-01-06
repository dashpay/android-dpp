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
import org.json.JSONObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
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

    }

    @Test
    fun applyStateTransition() {
        val documents = Fixtures.getDocumentsFixture()
        val batchTransition = hashMapOf(
                "create" to documents
        )
        val result = DocumentFactory(stateRepository).createStateTransition(batchTransition)
        //assertEquals(result.documents, documents)
    }

    @Test @Disabled
    fun verifySignedDocumentsSTTest() {
        //TODO: This test is completely broken, getDocumentsSTSignedFixture() has bad data
        val documentST = Fixtures.getDocumentsSTSignedFixture()
        val identityST = Fixtures.getIdentityCreateSTSignedFixture()
        val identity = Fixtures.getIdentityForSignaturesFixture()
        Assertions.assertTrue(documentST.verifySignature(identityST.publicKeys[0]))
        Assertions.assertTrue(documentST.verifySignature(identity.publicKeys[0]))

        val documentSTTwo = Fixtures.getDocumentsSTSignedFixtureTwo()
        assertEquals(documentST.transitions[0].toJSON(), documentSTTwo.transitions[0].toJSON())
    }
}