package org.dashevo.schema.model

data class BlockchainUser(
        val pver: Int,
        val uname: String,
        val uid: String,
        val pubkey: String,
        val credits: Int = 100000
)