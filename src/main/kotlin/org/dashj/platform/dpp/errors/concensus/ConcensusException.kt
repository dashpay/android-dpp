/**
 * Copyright (c) 2021-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashj.platform.dpp.errors.concensus

import org.dashj.platform.dpp.errors.ConcensusErrorMetadata
import org.dashj.platform.dpp.errors.DPPException
import org.dashj.platform.dpp.errors.concensus.basic.IncompatibleProtocolVersionException
import org.dashj.platform.dpp.errors.concensus.basic.JsonSchemaException
import org.dashj.platform.dpp.errors.concensus.basic.UnsupportedProtocolVersionException
import org.dashj.platform.dpp.errors.concensus.basic.datacontract.InvalidDataContractIdException
import org.dashj.platform.dpp.errors.concensus.basic.document.MissingDataContractIdException
import org.dashj.platform.dpp.errors.concensus.basic.identity.IdentityAssetLockTransactionOutPointAlreadyExistsException
import org.dashj.platform.dpp.errors.concensus.basic.identity.InvalidIdentityKeySignatureException
import org.dashj.platform.dpp.errors.concensus.basic.identity.InvalidInstantAssetLockProofException
import org.dashj.platform.dpp.errors.concensus.basic.identity.InvalidInstantAssetLockProofSignatureException
import org.dashj.platform.dpp.errors.concensus.basic.statetransition.InvalidStateTransitionTypeException
import org.dashj.platform.dpp.errors.concensus.basic.statetransition.MissingStateTransitionTypeException
import org.dashj.platform.dpp.errors.concensus.document.DataContractNotPresentException
import org.dashj.platform.dpp.errors.concensus.fee.BalanceIsNotEnoughException
import org.dashj.platform.dpp.errors.concensus.signature.IdentityNotFoundException
import org.dashj.platform.dpp.errors.concensus.signature.InvalidIdentityPublicKeyTypeException
import org.dashj.platform.dpp.errors.concensus.signature.InvalidSignaturePublicKeySecurityLevelException
import org.dashj.platform.dpp.errors.concensus.signature.InvalidStateTransitionSignatureException
import org.dashj.platform.dpp.errors.concensus.signature.MissingPublicKeyException
import org.dashj.platform.dpp.errors.concensus.signature.PublicKeyIsDisabledException
import org.dashj.platform.dpp.errors.concensus.state.document.DocumentAlreadyPresentException
import org.dashj.platform.dpp.errors.concensus.state.document.InvalidDocumentRevisionException
import org.dashj.platform.dpp.errors.concensus.state.identity.DuplicatedIdentityPublicKeyException
import org.dashj.platform.dpp.errors.concensus.state.identity.IdentityAlreadyExistsException
import org.dashj.platform.dpp.toBase64
import org.dashj.platform.dpp.util.Cbor

abstract class ConcensusException(message: String) : DPPException(message) {

    companion object {
        @JvmStatic
        fun create(code: Int, arguments: List<Any>): ConcensusException {
            try {
                val codeEnum = Codes.getByCode(code)
                return createException(codeEnum, arguments)
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Error is not defined: $code", e)
            }
        }

        @JvmStatic
        fun create(code: Codes, arguments: List<Any>): ConcensusException {
            try {
                return createException(code, arguments)
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Error is not defined: $code", e)
            }
        }

        @JvmStatic
        fun create(code: Codes, driveErrorData: Map<String, Any>): ConcensusException {
            try {
                return createException(code, driveErrorData)
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Error is not defined: $code", e)
            }
        }

        @JvmStatic
        fun create(errorMetadata: ConcensusErrorMetadata): ConcensusException {
            return create(errorMetadata.code, errorMetadata.arguments)
        }

        private fun createException(code: Codes, driveErrorData: Map<String, Any>): ConcensusException {
            return createException(code, driveErrorData["arguments"] as List<Any>)
        }

        private fun createException(code: Codes, arguments: List<Any>): ConcensusException {
            return when (code) {
                // remove unneeded errors
//                Codes.ProtocolVersionParsingError -> ProtocolVersionParsingException(arguments)
//                Codes.SerializedObjectParsingError -> SerializedObjectParsingException(arguments)
                Codes.UnsupportedProtocolVersionError -> UnsupportedProtocolVersionException(arguments)
                Codes.IncompatibleProtocolVersionError -> IncompatibleProtocolVersionException(arguments)
//                Codes.JsonSchemaCompilationError -> JsonSchemaCompilationException(arguments)
                Codes.JsonSchemaError -> JsonSchemaException(arguments)
//                Codes.InvalidIdentifierError -> InvalidIdentifierException(arguments)
//
//                Codes.DataContractMaxDepthExceedError -> DataContractMaxDepthExceedException(arguments)
//                Codes.DuplicateIndexError -> DuplicateIndexException(arguments)
//                Codes.IncompatibleRe2PatternError -> IncompatibleRe2PatternException(arguments)
//                Codes.InvalidCompoundIndexError -> InvalidCompoundIndexException(arguments)
                Codes.InvalidDataContractIdError -> InvalidDataContractIdException(arguments)
//                Codes.InvalidIndexedPropertyConstraintError -> InvalidIndexedPropertyConstraintException(arguments)
//                Codes.InvalidIndexPropertyTypeError -> InvalidIndexPropertyTypeException(arguments)
//                Codes.InvalidJsonSchemaRefError -> InvalidJsonSchemaRefException(arguments)
//                Codes.SystemPropertyIndexAlreadyPresentError -> SystemPropertyIndexAlreadyPresentException(arguments)
//                Codes.UndefinedIndexPropertyError -> UndefinedIndexPropertyException(arguments)
//                Codes.UniqueIndicesLimitReachedError -> UniqueIndicesLimitReachedException(arguments)
                Codes.DataContractNotPresentError -> DataContractNotPresentException(arguments)
//                Codes.DuplicateDocumentTransitionsWithIdsError -> DuplicateDocumentTransitionsWithIdsException(arguments)
//                Codes.DuplicateDocumentTransitionsWithIndicesError -> DuplicateDocumentTransitionsWithIndicesException(arguments)
//                Codes.InconsistentCompoundIndexDataError -> InconsistentCompoundIndexDataException(arguments)
//                Codes.InvalidDocumentTransitionActionError -> InvalidDocumentTransitionActionException(arguments)
//                Codes.InvalidDocumentTransitionIdError -> InvalidDocumentTransitionIdException(arguments)
//                Codes.InvalidDocumentTypeError -> InvalidDocumentTypeException(arguments)
                Codes.MissingDataContractIdError -> MissingDataContractIdException(arguments)
//                Codes.MissingDocumentTransitionActionError -> MissingDocumentTransitionActionException(arguments)
//                Codes.MissingDocumentTransitionTypeError -> MissingDocumentTransitionTypeException(arguments)
//                Codes.MissingDocumentTypeError -> MissingDocumentTypeException(arguments)
                Codes.DuplicatedIdentityPublicKeyError -> DuplicatedIdentityPublicKeyException(arguments)
//                Codes.DuplicatedIdentityPublicKeyIdError -> DuplicatedIdentityPublicKeyIdException(arguments)
//                Codes.IdentityAssetLockProofLockedTransactionMismatchError -> IdentityAssetLockProofLockedTransactionMismatchException(arguments)
//                Codes.IdentityAssetLockTransactionIsNotFoundError -> IdentityAssetLockTransactionIsNotFoundException(arguments)
                Codes.IdentityAssetLockTransactionOutPointAlreadyExistsError ->
                    IdentityAssetLockTransactionOutPointAlreadyExistsException(arguments)
//                Codes.IdentityAssetLockTransactionOutputNotFoundError -> IdentityAssetLockTransactionOutputNotFoundException(arguments)
//                Codes.InvalidAssetLockProofCoreChainHeightError -> InvalidAssetLockProofCoreChainHeightException(arguments)
//                Codes.InvalidAssetLockProofTransactionHeightError -> InvalidAssetLockProofTransactionHeightException(arguments)
                Codes.InvalidIdentityKeySignatureError -> InvalidIdentityKeySignatureException(arguments)
//                Codes.InvalidIdentityAssetLockTransactionError -> InvalidIdentityAssetLockTransactionException(arguments)
//                Codes.InvalidIdentityAssetLockTransactionOutputError -> InvalidIdentityAssetLockTransactionOutputException(arguments)
//                Codes.InvalidIdentityPublicKeyDataError -> InvalidIdentityPublicKeyDataException(arguments)
                Codes.InvalidInstantAssetLockProofError -> InvalidInstantAssetLockProofException(arguments)
                Codes.InvalidInstantAssetLockProofSignatureError ->
                    InvalidInstantAssetLockProofSignatureException(arguments)
                Codes.InvalidStateTransitionTypeError -> InvalidStateTransitionTypeException(arguments)
                Codes.MissingStateTransitionTypeError -> MissingStateTransitionTypeException()
//                Codes.StateTransitionMaxSizeExceededError -> StateTransitionMaxSizeExceededException(arguments)
                Codes.IdentityNotFoundError -> IdentityNotFoundException(arguments)
                Codes.InvalidIdentityPublicKeyTypeError -> InvalidIdentityPublicKeyTypeException(arguments)
                Codes.InvalidStateTransitionSignatureError -> InvalidStateTransitionSignatureException()
                Codes.MissingPublicKeyError -> MissingPublicKeyException(arguments)
                Codes.PublicKeyIsDisabledError -> PublicKeyIsDisabledException(arguments)
                Codes.InvalidSignaturePublicKeySecurityLevelError -> InvalidSignaturePublicKeySecurityLevelException(arguments)
                Codes.BalanceIsNotEnoughError -> BalanceIsNotEnoughException(arguments)
//                Codes.DataContractAlreadyPresentError -> DataContractAlreadyPresentException(arguments)
//                Codes.DataTriggerConditionError -> DataTriggerConditionException(arguments)
//                Codes.DataTriggerExecutionError -> DataTriggerExecutionException(arguments)
//                Codes.DataTriggerInvalidResultError -> DataTriggerInvalidResultException(arguments)
                Codes.DocumentAlreadyPresentError -> DocumentAlreadyPresentException(arguments)
//                Codes.DocumentNotFoundError -> DocumentNotFoundException(arguments)
//                Codes.DocumentOwnerIdMismatchError -> DocumentOwnerIdMismatchException(arguments)
//                Codes.DocumentTimestampsMismatchError -> DocumentTimestampsMismatchException(arguments)
//                Codes.DocumentTimestampWindowViolationError -> DocumentTimestampWindowViolationException(arguments)
//                Codes.DuplicateUniqueIndexError -> DuplicateUniqueIndexException(arguments)
                Codes.InvalidDocumentRevisionError -> InvalidDocumentRevisionException(arguments)
                Codes.IdentityAlreadyExistsError -> IdentityAlreadyExistsException(arguments)
                else -> UnknownConcensusError(code.code, arguments)
            }
        }
    }

    val arguments = arrayListOf<Any>()

    fun getCode(): Int {
        try {
            val code = Codes.getByName(name)
            return code.code
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Error is not defined: $name", e)
        }
    }

    fun setArguments(arguments: List<Any>) {
        this.arguments.addAll(arguments)
    }

    override fun toString(): String {
        val data = hashMapOf<String, Any?>(
            "arguments" to arguments
        )
        return "Metadata(code=${getCode()}, drive-error-data-bin=${Cbor.encode(data).toBase64()}"
    }
}
