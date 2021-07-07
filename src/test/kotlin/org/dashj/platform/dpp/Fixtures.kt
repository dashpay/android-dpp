package org.dashj.platform.dpp

import java.io.File
import org.dashj.platform.dpp.contract.DataContract
import org.dashj.platform.dpp.contract.DataContractCreateTransition
import org.dashj.platform.dpp.document.Document
import org.dashj.platform.dpp.document.DocumentFactory
import org.dashj.platform.dpp.document.DocumentTransition
import org.dashj.platform.dpp.document.DocumentsBatchTransition
import org.dashj.platform.dpp.identifier.Identifier
import org.dashj.platform.dpp.identity.Identity
import org.dashj.platform.dpp.identity.IdentityCreateTransition
import org.dashj.platform.dpp.identity.IdentityPublicKey
import org.dashj.platform.dpp.statetransition.StateTransition
import org.dashj.platform.dpp.statetransition.StateTransitionFactory
import org.json.JSONObject

object Fixtures {

    val userId = Identifier.from("4mZmxva49PBb7BE7srw9o3gixvDfj1dAx1K2dmAAauGp")
    val contractId = Identifier.from("9rjz23TQ3rA2agxXD56XeDfw63hHJUwuj7joxSBEfRgX")
    val stateRepository = StateRepositoryMock()

    fun getDataContractFixtures(): DataContract {
        val json = File("src/test/resources/data/documentsforcontract.json").readText()
        val jsonObject = JSONObject(json)
        val map = jsonObject.toMap()
        val dataContract = DataContract(
            contractId,
            userId,
            DataContract.PROTOCOL_VERSION,
            DataContract.SCHEMA,
            map
        )

        dataContract.definitions = JSONObject("{lastName: { type: 'string', }, }").toMap()

        return dataContract
    }

    fun getDocumentsFixture(): List<Document> {
        val dataContract = getDataContractFixtures()

        val factory = DocumentFactory(stateRepository)

        return listOf(
            factory.create(dataContract, userId, "niceDocument", JSONObject("{ name: 'Cutie' }").toMap()),
            factory.create(dataContract, userId, "prettyDocument", JSONObject("{ lastName: 'Shiny' }").toMap()),
            factory.create(dataContract, userId, "prettyDocument", JSONObject("{ lastName: 'Sweety' }").toMap()),
            factory.create(dataContract, userId, "indexedDocument", JSONObject("{ firstName: 'William', lastName: 'Birkin' }").toMap()),
            factory.create(dataContract, userId, "indexedDocument", JSONObject("{ firstName: 'Leon', lastName: 'Kennedy' }").toMap())
        )
    }

    fun getDocumentTransitionFixture(documents: MutableMap<String, List<Document>> = hashMapOf()): List<DocumentTransition> {
        var createDocuments = documents["create"] ?: listOf()
        val replaceDocuments = documents["replace"] ?: listOf()
        val deleteDocuments = documents["delete"] ?: listOf()
        val fixtureDocuments = getDocumentsFixture()
        if (createDocuments.isEmpty()) {
            createDocuments = fixtureDocuments
        }
        val factory = DocumentFactory(stateRepository)

        val documentsForTransition = hashMapOf(
            "create" to createDocuments,
            "replace" to replaceDocuments,
            "delete" to deleteDocuments
        )

        val stateTransition = factory.createStateTransition(documentsForTransition)

        return stateTransition.transitions
    }

    fun getIdentityFixture(): Identity {
        val json = File("src/test/resources/data/identity.json").readText()
        val jsonObject = JSONObject(json)
        val rawIdentity = jsonObject.toMap()

        return Identity(rawIdentity)
    }

    fun getIdentityForSignaturesFixture(): Identity {
        val json = File("src/test/resources/data/identity-for-signatures.json").readText()
        val jsonObject = JSONObject(json)
        val rawIdentity = jsonObject.toMap()

        return Identity(rawIdentity)
    }

    fun getIdentityCreateSTFixture(): IdentityCreateTransition {
        val rawStateTransition = HashMap<String, Any?>()

        rawStateTransition["protocolVersion"] = 0
        rawStateTransition["type"] = StateTransition.Types.IDENTITY_CREATE.value
        rawStateTransition["lockedOutPoint"] = ByteArray(36).toBase64()

        val publicKeysMap = ArrayList<Any>(1)
        publicKeysMap.add(IdentityPublicKey(1, IdentityPublicKey.TYPES.ECDSA_SECP256K1, ByteArray(32)).toJSON())
        rawStateTransition["publicKeys"] = publicKeysMap

        return IdentityCreateTransition(rawStateTransition)
    }

    fun getIdentityCreateSTSignedFixture(): IdentityCreateTransition {
        val json = File("src/test/resources/data/identity-transition.json").readText()
        val jsonObject = JSONObject(json)
        val rawIdentity = jsonObject.toMap()

        return IdentityCreateTransition(rawIdentity)
    }

    fun getDataContractSTSignedFixture(): DataContractCreateTransition {
        val json = File("src/test/resources/data/dpns-contract-transition.json").readText()
        val jsonObject = JSONObject(json)
        val rawContractST = jsonObject.toMap()

        return DataContractCreateTransition(rawContractST)
    }

    fun getDataContractSTSignedFixtureTwo(): DataContractCreateTransition {
        val json = File("src/test/resources/data/dpns-contract-transition.json").readText()
        val jsonObject = JSONObject(json)
        val rawContractST = jsonObject.toMap()

        return StateTransitionFactory(stateRepository).createStateTransition(rawContractST) as DataContractCreateTransition
    }

    fun getDocumentsSTSignedFixture(): DocumentsBatchTransition {
        val jsonObject = JSONObject(File("src/test/resources/data/documents-transition.json").readText())
        val rawDocumentST = jsonObject.toMap()

        return DocumentsBatchTransition(rawDocumentST)
    }

    fun getDocumentsSTSignedFixtureTwo(): DocumentsBatchTransition {
        val jsonObject = JSONObject(File("src/test/resources/data/documents-transition.json").readText())
        val rawDocumentST = jsonObject.toMap()
        return StateTransitionFactory(stateRepository).createStateTransition(rawDocumentST) as DocumentsBatchTransition
    }
}
