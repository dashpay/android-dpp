/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashevo.dpp.document

import org.dashevo.dpp.Fixtures
import org.dashevo.dpp.util.JsonUtils
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class DocumentTest {

    @Test
    fun testDocument() {
        val documents = Fixtures.getDocumentsFixture()

        assertEquals(5, documents.size)
        assertEquals(Document.Action.CREATE, documents[0].action)
        assertEquals("4mZmxva49PBb7BE7srw9o3gixvDfj1dAx1K2dmAAauGp", documents[2].userId)
        assertEquals("9rjz23TQ3rA2agxXD56XeDfw63hHJUwuj7joxSBEfRgX", documents[3].contractId)
    }

    @Test
    fun testDocumentFactory() {
        var factory = DocumentFactory()

        val contract = Fixtures.getDataContractFixtures()

        val factoryCreatedDocument = factory.create(contract, "4mZmxva49PBb7BE7srw9o3gixvDfj1dAx1K2dmAAauGp", "niceDocument", JSONObject("{ name: 'Cutie' }").toMap())
        val fixtureCreatedDocuments = Fixtures.getDocumentsFixture()

        // compare the first document
        assertEquals(fixtureCreatedDocuments[0].contractId, factoryCreatedDocument.contractId)
        assertEquals(fixtureCreatedDocuments[0].userId, factoryCreatedDocument.userId)
        assertEquals(fixtureCreatedDocuments[0].type, factoryCreatedDocument.type)
        assertEquals(fixtureCreatedDocuments[0].data["name"], factoryCreatedDocument.data["name"])

    }

    @Test
    fun applyStateTransition() {
        val documents = Fixtures.getDocumentsFixture()
        val result = DocumentFactory().createStateTransition(documents)
        assertEquals(result.documents, documents)
    }
}