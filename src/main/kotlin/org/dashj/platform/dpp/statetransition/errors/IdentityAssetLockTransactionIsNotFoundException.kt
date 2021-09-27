package org.dashj.platform.dpp.statetransition.errors

import org.bitcoinj.core.TransactionOutPoint
import org.dashj.platform.dpp.errors.concensus.ConcensusException

class IdentityAssetLockTransactionIsNotFoundException(val outPoint: TransactionOutPoint) :
    ConcensusException("Asset Lock transaction with specified outPoint was not found")
