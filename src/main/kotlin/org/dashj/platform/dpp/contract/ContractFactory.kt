/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashj.platform.dpp.contract

import org.dashj.platform.dpp.DashPlatformProtocol
import org.dashj.platform.dpp.Factory
import org.dashj.platform.dpp.ProtocolVersion
import org.dashj.platform.dpp.StateRepository
import org.dashj.platform.dpp.identifier.Identifier
import org.dashj.platform.dpp.util.Entropy
import org.dashj.platform.dpp.util.HashUtils

class ContractFactory(dpp: DashPlatformProtocol, stateRepository: StateRepository) : Factory(dpp, stateRepository) {

    fun create(ownerId: ByteArray, documents: Map<String, Any?>): DataContract {
        return create(Identifier.from(ownerId), documents)
    }
    fun create(ownerId: Identifier, documents: Map<String, Any?>): DataContract {
        val dataContractEntropy = Entropy.generate()
        val dataContractId = HashUtils.generateDataContractId(ownerId.toBuffer(), dataContractEntropy)

        val dataContract = DataContract(
            Identifier.from(dataContractId),
            ownerId,
            ProtocolVersion.latestVersion,
            DataContract.SCHEMA,
            documents.toMutableMap()
        )

        dataContract.entropy = dataContractEntropy

        return dataContract
    }

    fun createFromObject(rawContract: Map<String, Any?>, options: Options = Options()): DataContract {
        return DataContract(rawContract)
    }

    fun createFromBuffer(payload: ByteArray, options: Options = Options()): DataContract {
        val (protocolVersion, rawDataContract) = decodeProtocolEntity(payload)
        rawDataContract["protocolVersion"] = protocolVersion
        return createFromObject(rawDataContract, options)
    }

    fun createStateTransition(dataContract: DataContract): DataContractCreateTransition {
        return DataContractCreateTransition(dataContract)
    }
}
