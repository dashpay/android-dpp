/**
 * Copyright (c) 2018-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashevo.dpp.statetransition

import org.dashevo.dpp.contract.ContractFactory
import org.dashevo.dpp.contract.ContractStateTransition
import org.dashevo.dpp.document.Document
import org.dashevo.dpp.document.DocumentsStateTransition
import org.dashevo.dpp.errors.InvalidStateTransitionTypeError
import org.dashevo.dpp.identity.IdentityCreateTransition
import org.dashevo.dpp.util.Cbor

class StateTransitionFactory() {

    fun createStateTransition(rawStateTransition: MutableMap<String, Any?>, options: Options = Options()): StateTransition {
        var stateTransition : StateTransition
        when (StateTransition.Types.getByCode(rawStateTransition["type"] as Int)) {
            StateTransition.Types.DATA_CONTRACT -> {
                val dataContract = ContractFactory().createDataContract(rawStateTransition["dataContract"] as MutableMap<String, Any?>)

                stateTransition = ContractStateTransition(dataContract);
            }
            StateTransition.Types.DOCUMENTS -> {
                //this part will not work
                var index: Int = 0
                val actions = rawStateTransition["actions"] as List<Any>
                val documents = (rawStateTransition["documents"] as List<Any>).map {
                    val rawDocument = it as MutableMap<String, Any?>
                    val document = Document(rawDocument)
                    document.action = Document.Action.getByCode(actions[index] as Int)
                    ++index
                    document
                }

                stateTransition = DocumentsStateTransition(documents)
            }
            StateTransition.Types.IDENTITY_CREATE -> {
                stateTransition = IdentityCreateTransition(rawStateTransition)
            }
            else -> {
                throw InvalidStateTransitionTypeError(rawStateTransition["type"] as Int, rawStateTransition)
            }
        }

        stateTransition.signature = rawStateTransition["signature"] as String
        stateTransition.signaturePublicKeyId = rawStateTransition["signaturePublicKeyId"] as Int

        return stateTransition
    }

    fun createFromObject(rawStateTransition: MutableMap<String, Any?>, options: Options = Options()): StateTransition {
        return createStateTransition(rawStateTransition)
    }

    fun createFromSerialized(payload: ByteArray, options: Options = Options()): StateTransition {
        val rawStateTransition = Cbor.decode(payload).toMutableMap()
        return createFromObject(rawStateTransition, options)
    }

    inner class Options(val skipValidation: Boolean = false)

}