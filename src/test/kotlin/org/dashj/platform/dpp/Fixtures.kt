/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashj.platform.dpp

import org.bitcoinj.core.Coin
import org.bitcoinj.core.DumpedPrivateKey
import org.bitcoinj.core.ECKey
import org.bitcoinj.core.Sha256Hash
import org.bitcoinj.core.Transaction
import org.bitcoinj.core.TransactionOutPoint
import org.bitcoinj.crypto.BLSLazySignature
import org.bitcoinj.params.TestNet3Params
import org.bitcoinj.quorums.InstantSendLock
import org.bitcoinj.script.ScriptBuilder
import org.bitcoinj.script.ScriptOpCodes.OP_RETURN
import org.dashj.platform.dpp.contract.ContractFactory
import org.dashj.platform.dpp.contract.DataContract
import org.dashj.platform.dpp.contract.DataContractCreateTransition
import org.dashj.platform.dpp.document.Document
import org.dashj.platform.dpp.document.DocumentFactory
import org.dashj.platform.dpp.document.DocumentTransition
import org.dashj.platform.dpp.document.DocumentsBatchTransition
import org.dashj.platform.dpp.identifier.Identifier
import org.dashj.platform.dpp.identity.ChainAssetLockProof
import org.dashj.platform.dpp.identity.Identity
import org.dashj.platform.dpp.identity.IdentityCreateTransition
import org.dashj.platform.dpp.identity.IdentityPublicKey
import org.dashj.platform.dpp.identity.IdentityTopUpTransition
import org.dashj.platform.dpp.identity.InstantAssetLockProof
import org.dashj.platform.dpp.statetransition.StateTransition
import org.dashj.platform.dpp.statetransition.StateTransitionFactory
import org.dashj.platform.dpp.util.Converters
import org.dashj.platform.dpp.util.Entropy
import org.json.JSONObject
import java.io.File

object Fixtures {

    private val ownerId = Identifier.from("4mZmxva49PBb7BE7srw9o3gixvDfj1dAx1K2dmAAauGp")
    private val contractId = Identifier.from("9rjz23TQ3rA2agxXD56XeDfw63hHJUwuj7joxSBEfRgX")
    private val stateRepository = StateRepositoryMock()
    val dpp = DashPlatformProtocol(stateRepository)
    private val PARAMS = TestNet3Params.get()
    private val contractFactory = ContractFactory(dpp, stateRepository)
    private val documentFactory = DocumentFactory(dpp, stateRepository)

    private fun loadFile(filename: String): MutableMap<String, Any?> {
        val json = File(this::class.java.getResource(filename)!!.file).readText()
        val jsonObject = JSONObject(json)
        return jsonObject.toMap()!!
    }

    fun getDataContractFixture(ownerId: Identifier = Entropy.generateRandomIdentifier()): DataContract {
        val json = File(this::class.java.getResource("datacontract-fixture.json")!!.file).readText()
        val jsonObject = JSONObject(json)
        val documents = jsonObject.toMap()

        val dataContract = contractFactory.create(ownerId, documents)
        dataContract.definitions = JSONObject("{lastName: { type: 'string', }, }").toMap()

        return dataContract
    }

    fun getDashPayContractFixture(): DataContract {
        val dashPaySchema = loadFile("dashpay-contract.json")
        return contractFactory.create(ownerId, dashPaySchema)
    }

    fun getContactRequestDocumentFixture(options: Map<String, Any?>): Document {
        val factory = DocumentFactory(dpp, stateRepository)

        val data = hashMapOf<String, Any?>(
            "toUserId" to Entropy.generateRandomIdentifier(),
            "encryptedPublicKey" to ByteArray(96),
            "senderKeyIndex" to 0,
            "recipientKeyIndex" to 0,
            "accountReference" to 0,
        )
        data.putAll(options)

        return factory.create(getDashPayContractFixture(), ownerId, "contactRequest", data)
    }

    fun getDocumentsFixture(): List<Document> {
        val dataContract = getDataContractFixture()

        return listOf(
            documentFactory.create(
                dataContract, ownerId, "niceDocument",
                JSONObject("{ name: 'Cutie' }").toMap()
            ),
            documentFactory.create(
                dataContract, ownerId, "prettyDocument",
                JSONObject("{ lastName: 'Shiny' }").toMap()
            ),
            documentFactory.create(
                dataContract, ownerId, "prettyDocument",
                JSONObject("{ lastName: 'Sweety' }").toMap()
            ),
            documentFactory.create(
                dataContract, ownerId, "indexedDocument",
                JSONObject("{ firstName: 'William', lastName: 'Birkin' }").toMap()
            ),
            documentFactory.create(
                dataContract, ownerId, "indexedDocument",
                JSONObject("{ firstName: 'Leon', lastName: 'Kennedy' }").toMap()
            ),
            documentFactory.create(
                dataContract, ownerId, "noTimeDocument",
                JSONObject("{ name: 'ImOutOfTime' }").toMap()
            ),
            documentFactory.create(
                dataContract, ownerId, "uniqueDates",
                JSONObject("{ firstName: 'John' }").toMap()
            ),
            documentFactory.create(
                dataContract, ownerId, "indexedDocument",
                JSONObject("{ firstName: 'Bill', lastName: 'Gates' }").toMap()
            ),
            documentFactory.create(
                dataContract, ownerId, "withByteArrays",
                JSONObject("{ byteArrayField: crypto.randomBytes(10), identifierField: generateRandomIdentifier().toBuffer() }").toMap()
            ),
            documentFactory.create(
                dataContract, ownerId, "optionalUniqueIndexedDocument",
                JSONObject("{ firstName: 'Jacques-Yves', lastName: 'Cousteau' }").toMap()
            ),
        )
    }

    fun getDocumentTransitionFixture(documents: MutableMap<String, List<Document>> = hashMapOf()):
        List<DocumentTransition> {
            var createDocuments = documents["create"] ?: listOf()
            val replaceDocuments = documents["replace"] ?: listOf()
            val deleteDocuments = documents["delete"] ?: listOf()
            val fixtureDocuments = getDocumentsFixture()
            if (createDocuments.isEmpty()) {
                createDocuments = fixtureDocuments
            }
            val factory = DocumentFactory(dpp, stateRepository)

            val documentsForTransition = hashMapOf(
                "create" to createDocuments,
                "replace" to replaceDocuments,
                "delete" to deleteDocuments
            )

            val stateTransition = factory.createStateTransition(documentsForTransition)

            return stateTransition.transitions
        }

    fun getDpnsContractFixture(): DataContract {
        val dashPaySchema = loadFile("dpns-contract.json")
        return contractFactory.create(ownerId, dashPaySchema)
    }

    fun getFeatureFlagsContractFixture(): DataContract {
        val dashPaySchema = loadFile("featureflags-contract.json")
        return contractFactory.create(ownerId, dashPaySchema)
    }

    fun getFeatureFlagsDocumentsFixture(): List<Document> {
        return listOf(
            documentFactory.create(
                getFeatureFlagsContractFixture(), ownerId, "fixCumulativeFeesBug",
                JSONObject("{ enabled: true, enableAtHeight: 77) }").toMap()
            )
        )
    }

    fun getIdentityCreateTransitionFixture(oneTimePrivateKey: ECKey = ECKey()): IdentityCreateTransition {
        val rawStateTransition = hashMapOf<String, Any?>(
            "protocolVersion" to ProtocolVersion.latestVersion,
            "type" to StateTransition.Types.IDENTITY_CREATE,
            "assetLockProof" to getInstantAssetLockProofFixture(oneTimePrivateKey).toObject(),
            "publicKeys" to listOf(
                mapOf(
                    "id" to 0,
                    "type" to IdentityPublicKey.TYPES.ECDSA_SECP256K1,
                    "data" to Converters.fromBase64("AuryIuMtRrl/VviQuyLD1l4nmxi9ogPzC9LT7tdpo0di"),
                )
            )
        )

        return IdentityCreateTransition(PARAMS, rawStateTransition)
    }

    fun getIdentityFixtureTwo(): Identity {
        val json = File("src/test/resources/data/identity.json").readText()
        val jsonObject = JSONObject(json)
        val rawIdentity = jsonObject.toMap()

        return Identity(rawIdentity)
    }

    fun getIdentityFixture(): Identity {

        val id = Entropy.generateRandomIdentifier()

        val rawIdentity = mapOf<String, Any?>(
            "protocolVersion" to ProtocolVersion.latestVersion,
            "id" to id.toBuffer(),
            "balance" to 10,
            "revision" to 0,
            "publicKeys" to listOf(
                mapOf<String, Any?>(
                    "id" to 0,
                    "type" to IdentityPublicKey.TYPES.ECDSA_SECP256K1,
                    "data" to Converters.fromBase64("AuryIuMtRrl/VviQuyLD1l4nmxi9ogPzC9LT7tdpo0di"),
                    "purpose" to IdentityPublicKey.Purpose.AUTHENTICATION,
                    "securityLevel" to IdentityPublicKey.SecurityLevel.MASTER
                ),
                mapOf(
                    "id" to 1,
                    "type" to IdentityPublicKey.TYPES.ECDSA_SECP256K1,
                    "data" to Converters.fromBase64("A8AK95PYMVX5VQKzOhcVQRCUbc9pyg3RiL7jttEMDU+L"),
                    "purpose" to IdentityPublicKey.Purpose.ENCRYPTION,
                    "securityLevel" to IdentityPublicKey.SecurityLevel.MEDIUM
                )
            )
        )

        return Identity(rawIdentity)
    }

    fun getIdentityTopUpTransitionFixture(): IdentityTopUpTransition {
        val rawStateTransition = hashMapOf<String, Any?>(
            "protocolVersion" to ProtocolVersion.latestVersion,
            "type" to StateTransition.Types.IDENTITY_CREATE,
            "assetLockProof" to getInstantAssetLockProofFixture().toObject(),
            "identityId" to Entropy.generateRandomIdentifier(),
        )

        return IdentityTopUpTransition(PARAMS, rawStateTransition)
    }

    fun getPreorderDocumentFixture(options: Map<String, Any?>): Document {
        val dataContract = getDpnsContractFixture()

        val label = if (options.containsKey("label")) {
            options["label"] as String
        } else {
            "Preorder"
        }
        val normalizedLabel = if (options.containsKey("normalizedLabel")) {
            options["normalizedLabel"] as String
        } else {
            label.toLowerCase()
        }

        val data = hashMapOf<String, Any?>(
            "label" to label,
            "normalizedLabel" to normalizedLabel,
            "parentDomainHash" to "",
            "preorderSalt" to Entropy.generate(),
            "records" to hashMapOf(
                "dashIdentity" to ownerId
            )
        )

        data.putAll(options)
        return documentFactory.create(dataContract, ownerId, "preorder", data)
    }

    fun getInstantAssetLockProofFixture(oneTimePrivateKey: ECKey = ECKey()): InstantAssetLockProof {
        val privateKeyHex = "cSBnVM4xvxarwGQuAfQFwqDg9k5tErHUHzgWsEfD4zdwUasvqRVY"
        val privateKey = DumpedPrivateKey.fromBase58(PARAMS, privateKeyHex).key

        val transaction = Transaction(PARAMS)
        transaction.addInput(
            Sha256Hash.wrapReversed(
                Converters.fromHex("a477af6b2667c29670467e4e0728b685ee07b240235771862318e29ddbe58458")
            ),
            0, ScriptBuilder.createP2PKHOutputScript(privateKey)
        )

        transaction.addOutput(Coin.valueOf(90000), ScriptBuilder.createCreditBurnOutput(oneTimePrivateKey))
        transaction.addOutput(Coin.valueOf(5000), ScriptBuilder().op(OP_RETURN).data(byteArrayOf(1, 2, 3)).build())
        transaction.addSignedInput(
            TransactionOutPoint(
                PARAMS, 0L,
                Sha256Hash.wrapReversed(
                    Converters.fromHex("a477af6b2667c29670467e4e0728b685ee07b240235771862318e29ddbe58458")
                )
            ),
            ScriptBuilder.createP2PKHOutputScript(privateKey),
            privateKey
        )

        val instantLock = InstantSendLock(
            PARAMS,
            arrayListOf(
                TransactionOutPoint(
                    PARAMS,
                    0L,
                    Sha256Hash.wrapReversed(
                        Converters.fromHex("6e200d059fb567ba19e92f5c2dcd3dde522fd4e0a50af223752db16158dabb1d")
                    )
                )
            ),
            transaction.txId,
            BLSLazySignature(
                PARAMS,
                Converters.fromHex(
                    "8967c46529a967b3822e1ba8a173066296d02593f0f59b3a78a30a7eef9c8a120847729e62e4a32954339286b79fe7590221331cd28d576887a263f45b595d499272f656c3f5176987c976239cac16f972d796ad82931d532102a4f95eec7d80"
                ),
                0
            )
        )

        return InstantAssetLockProof(
            PARAMS,
            hashMapOf<String, Any?>(
                "type" to 0,
                "instantLock" to instantLock.bitcoinSerialize(),
                "transaction" to transaction.bitcoinSerialize(),
                "outputIndex" to 0,
            )
        )
    }

    fun getRawTransactionFixture(): Map<String, Any?> {
        val transaction = loadFile("transaction.json")
        return transaction
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

        return IdentityCreateTransition(PARAMS, rawStateTransition)
    }

    fun getIdentityCreateSTSignedFixture(): IdentityCreateTransition {
        val json = File("src/test/resources/data/identity-transition.json").readText()
        val jsonObject = JSONObject(json)
        val rawIdentity = jsonObject.toMap()

        return IdentityCreateTransition(PARAMS, rawIdentity)
    }

    fun getDataContractSTSignedFixture(): DataContractCreateTransition {
        val json = File("src/test/resources/data/dpns-contract-transition.json").readText()
        val jsonObject = JSONObject(json)
        val rawContractST = jsonObject.toMap()

        return DataContractCreateTransition(PARAMS, rawContractST)
    }

    fun getDataContractSTSignedFixtureTwo(): DataContractCreateTransition {
        val json = File("src/test/resources/data/dpns-contract-transition.json").readText()
        val jsonObject = JSONObject(json)
        val rawContractST = jsonObject.toMap()

        return StateTransitionFactory(dpp, stateRepository).createStateTransition(rawContractST) as DataContractCreateTransition
    }

    fun getDocumentsSTSignedFixture(): DocumentsBatchTransition {
        val jsonObject = JSONObject(File("src/test/resources/data/documents-transition.json").readText())
        val rawDocumentST = jsonObject.toMap()

        return DocumentsBatchTransition(PARAMS, rawDocumentST)
    }

    fun getDocumentsSTSignedFixtureTwo(): DocumentsBatchTransition {
        val jsonObject = JSONObject(File("src/test/resources/data/documents-transition.json").readText())
        val rawDocumentST = jsonObject.toMap()
        return StateTransitionFactory(dpp, stateRepository).createStateTransition(rawDocumentST) as DocumentsBatchTransition
    }

    fun getChainAssetLockProofFixture(): ChainAssetLockProof {
        val outPoint = TransactionOutPoint(
            PARAMS,
            0,
            Sha256Hash.wrapReversed(
                Converters.fromHex(
                    "6e200d059fb567ba19e92f5c2dcd3dde522fd4e0a50af223752db16158dabb1d"
                )
            )
        )

        return ChainAssetLockProof(
            PARAMS,
            mapOf(
                "type" to 1,
                "coreChainLockedHeight" to 42,
                "outPoint" to outPoint.bitcoinSerialize()
            )
        )
    }
}
