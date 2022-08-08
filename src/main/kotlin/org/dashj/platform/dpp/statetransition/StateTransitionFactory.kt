/**
 * Copyright (c) 2018-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashj.platform.dpp.statetransition

import org.dashj.platform.dpp.DashPlatformProtocol
import org.dashj.platform.dpp.Factory
import org.dashj.platform.dpp.StateRepository
import org.dashj.platform.dpp.contract.ContractFactory
import org.dashj.platform.dpp.contract.DataContractCreateTransition
import org.dashj.platform.dpp.document.DocumentsBatchTransition
import org.dashj.platform.dpp.errors.DataContractNotPresentException
import org.dashj.platform.dpp.errors.InvalidStateTransitionTypeError
import org.dashj.platform.dpp.identifier.Identifier
import org.dashj.platform.dpp.identity.IdentityCreateTransition
import org.dashj.platform.dpp.identity.IdentityTopUpTransition
import org.dashj.platform.dpp.statetransition.errors.MissingDataContractIdException
import org.dashj.platform.dpp.util.Converters

class StateTransitionFactory(dpp: DashPlatformProtocol, stateRepository: StateRepository) :
    Factory(dpp, stateRepository) {

    fun createStateTransition(
        rawStateTransition: MutableMap<String, Any?>,
        options: Options = Options()
    ): StateTransition {
        val stateTransition: StateTransitionIdentitySigned
        when (StateTransition.Types.getByCode(rawStateTransition["type"] as Int)) {
            StateTransition.Types.DATA_CONTRACT_CREATE -> {
                val rawDataContract = rawStateTransition["dataContract"] as MutableMap<String, Any?>
                val dataContract = ContractFactory(dpp, stateRepository).create(
                    Identifier.from(rawDataContract["ownerId"]),
                    rawDataContract
                )

                stateTransition = DataContractCreateTransition(dpp.getNetworkParameters(), dataContract)
            }
            StateTransition.Types.DOCUMENTS_BATCH -> {
                val dataContracts = (rawStateTransition["transitions"] as List<Map<String, Any?>>).map { transition ->
                    if (!transition.containsKey("\$dataContractId")) {
                        throw MissingDataContractIdException(transition)
                    }

                    val dataContractId = Identifier.from(transition["\$dataContractId"])

                    val dataContract = stateRepository.fetchDataContract(
                        dataContractId
                    ) ?: throw DataContractNotPresentException(dataContractId)

                    dataContract
                }
                stateTransition = DocumentsBatchTransition(
                    dpp.getNetworkParameters(),
                    rawStateTransition,
                    dataContracts
                )
            }
            StateTransition.Types.IDENTITY_CREATE -> {
                stateTransition = IdentityCreateTransition(dpp.getNetworkParameters(), rawStateTransition)
            }
            StateTransition.Types.IDENTITY_TOP_UP -> {
                stateTransition = IdentityTopUpTransition(dpp.getNetworkParameters(), rawStateTransition)
            }
            else -> {
                throw InvalidStateTransitionTypeError(rawStateTransition["type"] as Int, rawStateTransition)
            }
        }

        stateTransition.signature = rawStateTransition["signature"]?.let {
            Converters.byteArrayFromBase64orByteArray(it)
        }
        if (rawStateTransition.containsKey("signaturePublicKeyId")) {
            stateTransition.signaturePublicKeyId = rawStateTransition["signaturePublicKeyId"] as Int
        } else {
            if (stateTransition.type != StateTransition.Types.IDENTITY_CREATE) {
                throw IllegalArgumentException("signaturePublicKeyId is missing from transition")
            }
        }

        return stateTransition
    }

    fun createFromObject(rawStateTransition: MutableMap<String, Any?>, options: Options = Options()): StateTransition {
        return createStateTransition(rawStateTransition)
    }

    fun createFromBuffer(payload: ByteArray, options: Options = Options()): StateTransition {
        val (protocolVersion, rawStateTransition) = decodeProtocolEntity(payload)
        rawStateTransition["protocolVersion"] = protocolVersion
        return createFromObject(rawStateTransition, options)
    }

    class Options(val skipValidation: Boolean = false)
}
