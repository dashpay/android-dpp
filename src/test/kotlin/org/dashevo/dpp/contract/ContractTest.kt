/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashevo.dpp.contract
import org.dashevo.dpp.Fixtures
import org.dashevo.dpp.util.JsonUtils
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class ContractTest {
    @Test
    fun testContract() {
        var contract = Fixtures.getDataContractFixtures()

        assertEquals(Contract.SCHEMA, contract.schema)
        assertEquals(3, contract.documents.size)
        assertEquals("9rjz23TQ3rA2agxXD56XeDfw63hHJUwuj7joxSBEfRgX", contract.id)
    }

    @Test
    fun testContactFactory() {
        var factory = ContractFactory()

        val json = File("src/test/resources/data/documentsforcontract.json").readText()//"{\r\n\"name\" : \"abc\" ,\r\n\"email id \" : [\"abc@gmail.com\",\"def@gmail.com\",\"ghi@gmail.com\"]\r\n}"
        val jsonObject = JSONObject(json)
        val map = JsonUtils.jsonToMap(jsonObject)

        val rawContract = HashMap<String, Any>()
        rawContract["documents"] = map
        rawContract["contractId"] = "9rjz23TQ3rA2agxXD56XeDfw63hHJUwuj7joxSBEfRgX"
        rawContract["\$schema"] = Contract.SCHEMA
        rawContract["version"] = Contract.VERSION
        rawContract["definitions"] = JsonUtils.jsonToMap(JSONObject("{lastName: { type: 'string', }, }"))

        val factoryCreatedContract = factory.createDataContract(rawContract)
        val fixtureCreatedContract = Fixtures.getDataContractFixtures()

        assertEquals(fixtureCreatedContract.contractId, factoryCreatedContract.contractId)

        val anotherFactoryCreatedContract = factory.create("9rjz23TQ3rA2agxXD56XeDfw63hHJUwuj7joxSBEfRgX", map)

        assertEquals(fixtureCreatedContract.contractId, anotherFactoryCreatedContract.contractId)
    }
}
