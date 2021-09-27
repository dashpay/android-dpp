package org.dashj.platform.dpp.statetransition.errors

import org.dashj.platform.dpp.errors.concensus.ConcensusException

class UnknownAssetLockProofException(val type: Int) : ConcensusException("Unknown Asset lock proof type: $type")
