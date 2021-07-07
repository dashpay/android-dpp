/**
 * Copyright (c) 2018-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashj.platform.dpp.statetransition

import org.dashj.platform.dpp.Factory
import org.dashj.platform.dpp.StateRepository
import org.dashj.platform.dpp.contract.ContractFactory
import org.dashj.platform.dpp.contract.DataContractCreateTransition
import org.dashj.platform.dpp.document.DocumentsBatchTransition
import org.dashj.platform.dpp.errors.InvalidStateTransitionTypeError
import org.dashj.platform.dpp.identifier.Identifier
import org.dashj.platform.dpp.identity.IdentityCreateTransition
import org.dashj.platform.dpp.identity.IdentityTopupTransition
import org.dashj.platform.dpp.util.Cbor
import org.dashj.platform.dpp.util.HashUtils

class StateTransitionFactory(stateRepository: StateRepository) : Factory(stateRepository) {

    fun createStateTransition(rawStateTransition: MutableMap<String, Any?>, options: Options = Options()): StateTransition {
        val stateTransition: StateTransitionIdentitySigned
        when (StateTransition.Types.getByCode(rawStateTransition["type"] as Int)) {
            StateTransition.Types.DATA_CONTRACT_CREATE -> {
                val rawDataContract = rawStateTransition["dataContract"] as MutableMap<String, Any?>
                val dataContract = ContractFactory(stateRepository).createDataContract(Identifier.from(rawDataContract["ownerId"]).toBuffer(), rawDataContract)

                stateTransition = DataContractCreateTransition(dataContract)
            }
            StateTransition.Types.DOCUMENTS_BATCH -> {
                stateTransition = DocumentsBatchTransition(rawStateTransition)
            }
            StateTransition.Types.IDENTITY_CREATE -> {
                stateTransition = IdentityCreateTransition(rawStateTransition)
            }
            StateTransition.Types.IDENTITY_TOP_UP -> {
                stateTransition = IdentityTopupTransition(rawStateTransition)
            }
            else -> {
                throw InvalidStateTransitionTypeError(rawStateTransition["type"] as Int, rawStateTransition)
            }
        }

        stateTransition.signature = rawStateTransition["signature"]?.let { HashUtils.byteArrayfromBase64orByteArray(it) }
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
        val rawStateTransition = Cbor.decode(payload).toMutableMap()
        return createFromObject(rawStateTransition, options)
    }

    class Options(val skipValidation: Boolean = false)
}
