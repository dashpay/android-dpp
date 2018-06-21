package org.dashevo.schema.model

data class TsHeader(
        val uid: String,
        val ptsid: String,
        val pakid: String,
        val fee: Int = 0,
        val usig: String = "",
        val qsig: String = ""
) : DapBaseInstance()