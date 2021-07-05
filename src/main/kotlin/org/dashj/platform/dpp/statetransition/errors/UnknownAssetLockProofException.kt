package org.dashevo.dpp.statetransition.errors

import org.dashevo.dpp.errors.ConcensusException

class UnknownAssetLockProofException(val type: Int) : ConcensusException("Unknown Asset lock proof type: $type")
