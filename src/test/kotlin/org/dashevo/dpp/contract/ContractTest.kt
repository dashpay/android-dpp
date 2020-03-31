/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashevo.dpp.contract
import org.bitcoinj.core.Utils
import org.dashevo.dpp.Fixtures
import org.dashevo.dpp.toHexString
import org.dashevo.dpp.util.Cbor
import org.dashevo.dpp.util.HashUtils
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class ContractTest {

    companion object {
        val factory = ContractFactory()
    }

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
        val map = jsonObject.toMap()

        val rawContract = HashMap<String, Any?>()
        rawContract["documents"] = map
        rawContract["contractId"] = "9rjz23TQ3rA2agxXD56XeDfw63hHJUwuj7joxSBEfRgX"
        rawContract["\$schema"] = Contract.SCHEMA
        rawContract["version"] = Contract.VERSION
        rawContract["definitions"] = JSONObject("{lastName: { type: 'string', }, }").toMap()

        val factoryCreatedContract = factory.createDataContract(rawContract)
        val fixtureCreatedContract = Fixtures.getDataContractFixtures()

        assertEquals(fixtureCreatedContract.contractId, factoryCreatedContract.contractId)

        val anotherFactoryCreatedContract = factory.create("9rjz23TQ3rA2agxXD56XeDfw63hHJUwuj7joxSBEfRgX", map)

        assertEquals(fixtureCreatedContract.contractId, anotherFactoryCreatedContract.contractId)
    }

    @Test
    fun applyStateTransition() {
        val dataContract = Fixtures.getDataContractFixtures()
        val stateTransition = ContractStateTransition(dataContract)

        val result =  factory.createStateTransition(dataContract)

        assertEquals(result.toJSON(), stateTransition.toJSON())
    }

    @Test
    fun dashPayContractTest() {
        val jsonDashPay = File("src/test/resources/data/dashpay-contract.json").readText()
        val jsonObject = JSONObject(jsonDashPay)
        val rawContract = jsonObject.toMap()

        val serializedHexData = "a169646f63756d656e7473a266646f6d61696ea467696e646963657383a26a70726f7065727469657381a1686e616d65486173686361736366756e69717565f5a16a70726f7065727469657382a1781a6e6f726d616c697a6564506172656e74446f6d61696e4e616d6563617363a16f6e6f726d616c697a65644c6162656c63617363a16a70726f7065727469657381a1747265636f7264732e646173684964656e74697479636173636a70726f70657274696573a6686e616d6548617368a4647479706566737472696e67696d696e4c656e6774681844696d61784c656e6774681844677061747465726e755e5b303132333435363738396162636465665d2b24656c6162656ca4647479706566737472696e67677061747465726e78265e28283f212d295b612d7a412d5a302d392d5d7b302c36327d5b612d7a412d5a302d395d2924696d696e4c656e67746803696d61784c656e67746818286f6e6f726d616c697a65644c6162656ca4647479706566737472696e67677061747465726e78205e28283f212d295b612d7a302d392d5d7b302c36327d5b612d7a302d395d2924696d696e4c656e67746803696d61784c656e6774681828781a6e6f726d616c697a6564506172656e74446f6d61696e4e616d65a3647479706566737472696e67696d696e4c656e67746800696d61784c656e67746818286c7072656f7264657253616c74a3647479706566737472696e67696d696e4c656e67746801696d61784c656e6774681844677265636f726473a46474797065666f626a6563746a70726f70657274696573a16c646173684964656e74697479a4647479706566737472696e67696d696e4c656e677468182a696d61784c656e677468182c677061747465726e783f5e5b31323334353637383941424344454647484a4b4c4d4e505152535455565758595a6162636465666768696a6b6d6e6f707172737475767778797a5d2b246d6d696e50726f7065727469657301746164646974696f6e616c50726f70657274696573f468726571756972656486686e616d6548617368656c6162656c6f6e6f726d616c697a65644c6162656c781a6e6f726d616c697a6564506172656e74446f6d61696e4e616d656c7072656f7264657253616c74677265636f726473746164646974696f6e616c50726f70657274696573f4687072656f72646572a467696e646963657381a26a70726f7065727469657381a17073616c746564446f6d61696e486173686361736366756e69717565f56a70726f70657274696573a17073616c746564446f6d61696e48617368a4647479706566737472696e67696d696e4c656e6774681840696d61784c656e6774681844677061747465726e755e5b303132333435363738396162636465665d2b24687265717569726564817073616c746564446f6d61696e48617368746164646974696f6e616c50726f70657274696573f4"
        val serializedData = HashUtils.fromHex(serializedHexData)

        val serializedFromRaw = Cbor.encode(rawContract)


        val fromRaw = factory.createFromObject(rawContract)

        val fromSerialized = factory.createFromSerialized(serializedData)
        val fromRoundTrip = factory.createFromSerialized(fromRaw.serialize())
        assertEquals(fromRaw.toJSON(), fromRoundTrip.toJSON())
        assertEquals(fromRaw.toJSON(), fromSerialized.toJSON())
        assertEquals(fromRaw.serialize().toHexString(), fromRoundTrip.serialize().toHexString())
        assertEquals(fromRaw.serialize().toHexString(), fromSerialized.serialize().toHexString())
        val fromRawData = factory.createDataContract(rawContract)
    }
}
