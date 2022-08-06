/**
 * Copyright (c) 2021-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashj.platform.dpp.errors.concensus

// remove unneeded errors

enum class Codes(val code: Int) {
    UnknownError(0),
    UnknownConcensusError(1),
    // Decoding
    ProtocolVersionParsingError(1000),
    SerializedObjectParsingError(1001),

    // General
    UnsupportedProtocolVersionError(1002),
    IncompatibleProtocolVersionError(1003),
    JsonSchemaCompilationError(1004),
    JsonSchemaError(1005),
    InvalidIdentifierError(1006),

    // Data Contract
    DataContractMaxDepthExceedError(1007),
    DuplicateIndexError(1008),
    IncompatibleRe2PatternError(1009),
    InvalidCompoundIndexError(1010),
    InvalidDataContractIdError(1011),
    InvalidIndexedPropertyConstraintError(1012),
    InvalidIndexPropertyTypeError(1013),
    InvalidJsonSchemaRefError(1014),
    SystemPropertyIndexAlreadyPresentError(1015),
    UndefinedIndexPropertyError(1016),
    UniqueIndicesLimitReachedError(1017),
    DuplicateIndexNameError(1048),
    InvalidDataContractVersionError(1050),
    IncompatibleDataContractSchemaError(1051),
    DataContractImmutablePropertiesUpdateError(1052),
    DataContractIndicesChangedError(1053),
    DataContractInvalidIndexDefinitionUpdateError(1054),
    DataContractHaveNewUniqueIndexError(1055),

    // Document
    DataContractNotPresentError(1018),
    DuplicateDocumentTransitionsWithIdsError(1019),
    DuplicateDocumentTransitionsWithIndicesError(1020),
    InconsistentCompoundIndexDataError(1021),
    InvalidDocumentTransitionActionError(1022),
    InvalidDocumentTransitionIdError(1023),
    InvalidDocumentTypeError(1024),
    MissingDataContractIdError(1025),
    MissingDocumentTransitionActionError(1026),
    MissingDocumentTransitionTypeError(1027),
    MissingDocumentTypeError(1028),

    // Identity
    DuplicatedIdentityPublicKeyError(1029),
    DuplicatedIdentityPublicKeyIdError(1030),
    IdentityAssetLockProofLockedTransactionMismatchError(1031),
    IdentityAssetLockTransactionIsNotFoundError(1032),
    IdentityAssetLockTransactionOutPointAlreadyExistsError(1033),
    IdentityAssetLockTransactionOutputNotFoundError(1034),
    InvalidAssetLockProofCoreChainHeightError(1035),
    InvalidAssetLockProofTransactionHeightError(1036),
    InvalidAssetLockTransactionOutputReturnSizeError(1037),
    InvalidIdentityAssetLockTransactionError(1038),
    InvalidIdentityAssetLockTransactionOutputError(1039),
    InvalidIdentityPublicKeyDataError(1040),
    InvalidInstantAssetLockProofError(1041),
    InvalidInstantAssetLockProofSignatureError(1042),
    MissingMasterPublicKeyError(1046),
    InvalidIdentityKeySignatureError(1056),

    // State Transition
    InvalidStateTransitionTypeError(1043),
    MissingStateTransitionTypeError(1044),
    StateTransitionMaxSizeExceededError(1045),

    /**
     * Signature
     */
    IdentityNotFoundError(2000),
    InvalidIdentityPublicKeyTypeError(2001),
    InvalidStateTransitionSignatureError(2002),
    MissingPublicKeyError(2003),
    InvalidSignaturePublicKeySecurityLevelError(2004),
    WrongPublicKeyPurposeError(2005),
    PublicKeyIsDisabledError(2006),
    PublicKeySecurityLevelNotMetError(2007),

    BalanceIsNotEnoughError(3000),

    // Data Contract
    DataContractAlreadyPresentError(4000),
    DataTriggerConditionError(4001),
    DataTriggerExecutionError(4002),
    DataTriggerInvalidResultError(4003),

    // Document
    DocumentAlreadyPresentError(4004),
    DocumentNotFoundError(4005),
    DocumentOwnerIdMismatchError(4006),
    DocumentTimestampsMismatchError(4007),
    DocumentTimestampWindowViolationError(4008),
    DuplicateUniqueIndexError(4009),
    InvalidDocumentRevisionError(4010),

    // Identity
    IdentityAlreadyExistsError(4011),
    IdentityPublicKeyDisabledAtWindowViolationError(4012),
    IdentityPublicKeyIsReadOnlyError(4017),
    InvalidIdentityPublicKeyIdError(4018),
    InvalidIdentityRevisionError(4019),
    StateMaxIdentityPublicKeyLimitReachedError(4020),
    DuplicatedIdentityPublicKeyStateError(4021),
    DuplicatedIdentityPublicKeyIdStateError(4022);

    companion object {

        fun getByCode(code: Int): Codes {
            return values().find { it.code == code }!!
        }

        fun getByCodeNoException(code: Int): Codes {
            return values().find { it.code == code } ?: UnknownError
        }

        fun getByName(name: String): Codes {
            return valueOf(name)
        }
    }
}
