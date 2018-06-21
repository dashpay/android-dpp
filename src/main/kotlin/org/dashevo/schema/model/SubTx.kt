package org.dashevo.schema.model

data class SubTx(val pver: Int, val action: Int, val uname: String, val pubkey: String, val meta: Meta)