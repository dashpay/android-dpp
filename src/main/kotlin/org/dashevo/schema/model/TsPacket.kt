package org.dashevo.schema.model

import org.json.JSONObject

data class TsPacket(
        val pver: Int,
        var dapobjects: List<JSONObject>,
        val dapobjmerkleroot: String,
        val dapid: String,
        val meta: Meta
)