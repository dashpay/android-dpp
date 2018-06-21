package org.dashevo.schema.model

import org.json.JSONObject

data class DapContract(
        val dapname: String,
        val dapschema: JSONObject,
        val dapid: String = "",
        val dapver: String = "",
        val idx: Int = 0
) : DapBaseInstance()