/**
 * Copyright (c) 2018-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashevo.dpp.statetransition

import org.dashevo.dpp.contract.ContractFactory
import org.dashevo.dpp.contract.ContractStateTransition
import org.dashevo.dpp.document.DocumentsBatchTransition
import org.dashevo.dpp.errors.InvalidStateTransitionTypeError
import org.dashevo.dpp.identity.IdentityCreateTransition
import org.dashevo.dpp.identity.IdentityTopupTransition
import org.dashevo.dpp.util.Cbor

class StateTransitionFactory {

    fun createStateTransition(rawStateTransition: MutableMap<String, Any?>, options: Options = Options()): StateTransition {
        var stateTransition: StateTransitionIdentitySigned
        when (StateTransition.Types.getByCode(rawStateTransition["type"] as Int)) {
            StateTransition.Types.DATA_CONTRACT_CREATE -> {
                val rawDataContract = rawStateTransition["dataContract"] as MutableMap<String, Any?>
                val dataContract = ContractFactory().createDataContract(rawDataContract["ownerId"] as String, rawDataContract)

                stateTransition = ContractStateTransition(dataContract)
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

        stateTransition.signature = rawStateTransition["signature"] as String
        if(rawStateTransition.containsKey("signaturePublicKeyId")) {
            stateTransition.signaturePublicKeyId = rawStateTransition["signaturePublicKeyId"] as Int
        } else {
            if (stateTransition.type != StateTransition.Types.IDENTITY_CREATE)
                throw IllegalArgumentException("signaturePublicKeyId is missing from transition")
        }

        return stateTransition
    }

    fun createFromObject(rawStateTransition: MutableMap<String, Any?>, options: Options = Options()): StateTransition {
        return createStateTransition(rawStateTransition)
    }

    fun createFromSerialized(payload: ByteArray, options: Options = Options()): StateTransition {
        val rawStateTransition = Cbor.decode(payload).toMutableMap()
        return createFromObject(rawStateTransition, options)
    }

    class Options(val skipValidation: Boolean = false)

}