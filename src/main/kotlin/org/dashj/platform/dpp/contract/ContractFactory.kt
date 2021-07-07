/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashj.platform.dpp.contract

import org.dashj.platform.dpp.Factory
import org.dashj.platform.dpp.StateRepository
import org.dashj.platform.dpp.identifier.Identifier
import org.dashj.platform.dpp.util.Cbor
import org.dashj.platform.dpp.util.Entropy
import org.dashj.platform.dpp.util.HashUtils

class ContractFactory(stateRepository: StateRepository) : Factory(stateRepository) {

    fun createDataContract(ownerId: ByteArray, rawDataContract: MutableMap<String, Any?>): DataContract {
        val dataContractEntropy = Entropy.generate()
        val dataContractId = HashUtils.generateDataContractId(ownerId, dataContractEntropy)

        val dataContract = DataContract(
            Identifier.from(dataContractId),
            Identifier(ownerId),
            DataContract.PROTOCOL_VERSION,
            DataContract.SCHEMA,
            rawDataContract["documents"] as MutableMap<String, Any?>
        )

        dataContract.entropy = dataContractEntropy

        return dataContract
    }

    fun createFromObject(rawContract: MutableMap<String, Any?>, options: Options = Options()): DataContract {
        return DataContract(rawContract)
    }

    fun createFromBuffer(payload: ByteArray, options: Options = Options()): DataContract {
        val rawDocument = Cbor.decode(payload).toMutableMap()
        return createFromObject(rawDocument, options)
    }

    fun createStateTransition(dataContract: DataContract): DataContractCreateTransition {
        return DataContractCreateTransition(dataContract)
    }
}
