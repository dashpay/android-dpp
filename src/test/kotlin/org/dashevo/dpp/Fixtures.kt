package org.dashevo.dpp

import org.bitcoinj.core.Sha256Hash
import org.dashevo.dpp.contract.Contract
import org.dashevo.dpp.contract.ContractFactory
import org.dashevo.dpp.document.Document
import org.dashevo.dpp.document.DocumentFactory
import org.dashevo.dpp.identity.Identity
import org.dashevo.dpp.identity.IdentityCreateTransition
import org.dashevo.dpp.identity.IdentityPublicKey
import org.dashevo.dpp.statetransition.StateTransition
import org.dashevo.dpp.util.Entropy
import org.dashevo.dpp.util.JsonUtils
import org.dashevo.dpp.util.Utils
import org.json.JSONObject
import java.io.File

object Fixtures {

    val userId = "4mZmxva49PBb7BE7srw9o3gixvDfj1dAx1K2dmAAauGp"
    val contractId = "9rjz23TQ3rA2agxXD56XeDfw63hHJUwuj7joxSBEfRgX"

    fun getDataContractFixtures() : Contract {
        val json = File("src/test/resources/data/documentsforcontract.json").readText()//"{\r\n\"name\" : \"abc\" ,\r\n\"email id \" : [\"abc@gmail.com\",\"def@gmail.com\",\"ghi@gmail.com\"]\r\n}"
        val jsonObject = JSONObject(json)
        val map = JsonUtils.jsonToMap(jsonObject)
        val dataContract = Contract(contractId, map)

        dataContract.definitions = JsonUtils.jsonToMap(JSONObject("{lastName: { type: 'string', }, }"))

        return dataContract
    }


    fun getDocumentsFixture() : Array<Document> {
        val dataContract = getDataContractFixtures()

        val factory = DocumentFactory()

        return arrayOf(
                factory.create(dataContract, userId, "niceDocument", JsonUtils.jsonTextToMap("{ name: 'Cutie' }")),
                factory.create(dataContract, userId, "prettyDocument", JsonUtils.jsonTextToMap("{ lastName: 'Shiny' }")) ,
                factory.create(dataContract, userId, "prettyDocument", JsonUtils.jsonTextToMap("{ lastName: 'Sweety' }")) ,
                factory.create(dataContract, userId, "indexedDocument", JsonUtils.jsonTextToMap("{ firstName: 'William', lastName: 'Birkin' }")) ,
                factory.create(dataContract, userId, "indexedDocument", JsonUtils.jsonTextToMap("{ firstName: 'Leon', lastName: 'Kennedy' }"))
        )
    }

    fun getIdentityFixture(): Identity {
        val json = File("src/test/resources/data/identity.json").readText()
        val jsonObject = JSONObject(json)
        val rawIdentity = JsonUtils.jsonToMap(jsonObject)

        return Identity(rawIdentity)
    }

    fun getIdentityCreateSTFixture() : IdentityCreateTransition {
        val rawStateTransition = HashMap<String, Any>()

        rawStateTransition["protocolVersion"] = 0
        rawStateTransition["type"] = StateTransition.Types.IDENTITY_CREATE
        rawStateTransition["lockedOutPoint"] = ByteArray(36).toBase64()
        rawStateTransition["identityType"] = Identity.IdentityType.USER

        val publicKeysMap = ArrayList<Any>(1)
        publicKeysMap.add(IdentityPublicKey(1, IdentityPublicKey.TYPES.ECDSA_SECP256K1, ByteArray(32).toBase64(), true).toJSON())
        rawStateTransition["publicKeys"] = publicKeysMap

        return IdentityCreateTransition(rawStateTransition)
    }
}