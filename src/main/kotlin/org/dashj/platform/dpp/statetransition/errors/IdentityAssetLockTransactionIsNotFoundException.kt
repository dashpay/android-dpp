package org.dashevo.dpp.statetransition.errors

import org.bitcoinj.core.TransactionOutPoint
import org.dashevo.dpp.errors.ConcensusException

class IdentityAssetLockTransactionIsNotFoundException(val outPoint: TransactionOutPoint) : ConcensusException("Asset Lock transaction with specified outPoint was not found")
