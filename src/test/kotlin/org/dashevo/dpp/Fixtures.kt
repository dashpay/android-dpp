package org.dashevo.dpp

import org.bitcoinj.core.Sha256Hash
import org.dashevo.dpp.contract.DataContract
import org.dashevo.dpp.contract.ContractStateTransition
import org.dashevo.dpp.document.Document
import org.dashevo.dpp.document.DocumentFactory
import org.dashevo.dpp.document.DocumentTransition
import org.dashevo.dpp.document.DocumentsBatchTransition
import org.dashevo.dpp.identity.Identity
import org.dashevo.dpp.identity.IdentityCreateTransition
import org.dashevo.dpp.identity.IdentityPublicKey
import org.dashevo.dpp.statetransition.StateTransition
import org.dashevo.dpp.statetransition.StateTransitionFactory
import org.json.JSONObject
import java.io.File

object Fixtures {

    val userId = "4mZmxva49PBb7BE7srw9o3gixvDfj1dAx1K2dmAAauGp"
    val contractId = "9rjz23TQ3rA2agxXD56XeDfw63hHJUwuj7joxSBEfRgX"

    fun getDataContractFixtures() : DataContract {
        val json = File("src/test/resources/data/documentsforcontract.json").readText()
        val jsonObject = JSONObject(json)
        val map = jsonObject.toMap()
        val dataContract = DataContract(
                Sha256Hash.of("me".toByteArray()).toStringBase58(),
                Sha256Hash.of("owner".toByteArray()).toStringBase58(),
                DataContract.PROTOCOL_VERSION,
                DataContract.SCHEMA,
                map
        )

        dataContract.definitions = JSONObject("{lastName: { type: 'string', }, }").toMap()

        return dataContract
    }


    fun getDocumentsFixture() : List<Document> {
        val dataContract = getDataContractFixtures()

        val factory = DocumentFactory()

        return listOf(
                factory.create(dataContract, userId, "niceDocument", JSONObject("{ name: 'Cutie' }").toMap()),
                factory.create(dataContract, userId, "prettyDocument", JSONObject("{ lastName: 'Shiny' }").toMap()) ,
                factory.create(dataContract, userId, "prettyDocument", JSONObject("{ lastName: 'Sweety' }").toMap()) ,
                factory.create(dataContract, userId, "indexedDocument", JSONObject("{ firstName: 'William', lastName: 'Birkin' }").toMap()) ,
                factory.create(dataContract, userId, "indexedDocument", JSONObject("{ firstName: 'Leon', lastName: 'Kennedy' }").toMap())
        )
    }

    fun getDocumentTransitionFixture(documents: MutableMap<String, List<Document>> = hashMapOf()): List<DocumentTransition> {
        var createDocuments = documents["create"] ?: listOf()
        val replaceDocuments = documents["replace"] ?: listOf()
        val deleteDocuments = documents["delete"] ?: listOf()
        val fixtureDocuments = getDocumentsFixture()
        if (createDocuments.isEmpty())
            createDocuments = fixtureDocuments
        val factory = DocumentFactory()

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

    fun getIdentityCreateSTFixture() : IdentityCreateTransition {
        val rawStateTransition = HashMap<String, Any?>()

        rawStateTransition["protocolVersion"] = 0
        rawStateTransition["type"] = StateTransition.Types.IDENTITY_CREATE.value
        rawStateTransition["lockedOutPoint"] = ByteArray(36).toBase64()

        val publicKeysMap = ArrayList<Any>(1)
        publicKeysMap.add(IdentityPublicKey(1, IdentityPublicKey.TYPES.ECDSA_SECP256K1, ByteArray(32).toBase64()).toJSON())
        rawStateTransition["publicKeys"] = publicKeysMap

        return IdentityCreateTransition(rawStateTransition)
    }

    fun getIdentityCreateSTSignedFixture() : IdentityCreateTransition {
        val json = File("src/test/resources/data/identity-transition.json").readText()
        val jsonObject = JSONObject(json)
        val rawIdentity = jsonObject.toMap()

        return IdentityCreateTransition(rawIdentity)
    }

    fun getDataContractSTSignedFixture() : ContractStateTransition {
        val json = File("src/test/resources/data/datacontract-transition.json").readText()
        val jsonObject = JSONObject(json)
        val rawContractST = jsonObject.toMap()

        return ContractStateTransition(rawContractST)
    }

    fun getDataContractSTSignedFixtureTwo() : ContractStateTransition {
        val json = File("src/test/resources/data/datacontract-transition.json").readText()
        val jsonObject = JSONObject(json)
        val rawContractST = jsonObject.toMap()

        return StateTransitionFactory().createStateTransition(rawContractST) as ContractStateTransition
    }

    fun getDocumentsSTSignedFixture() : DocumentsBatchTransition {
        val jsonObject = JSONObject(File("src/test/resources/data/documents-transition.json").readText())
        val rawDocumentST = jsonObject.toMap()

        return DocumentsBatchTransition(rawDocumentST)
    }

    fun getDocumentsSTSignedFixtureTwo() : DocumentsBatchTransition {
        val jsonObject = JSONObject(File("src/test/resources/data/documents-transition.json").readText())
        val rawDocumentST = jsonObject.toMap()
        return StateTransitionFactory().createStateTransition(rawDocumentST) as DocumentsBatchTransition
    }
}