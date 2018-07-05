package org.dashevo.schema.model

data class Result(
        val errCode: Int?,
        val objType: String? = null,
        val propName: String? = null,
        val schemaName: String? = null,
        val valid: Boolean = false
) {

    constructor() : this(null, null, null, null, true)

    constructor(valid: Boolean) : this(null, null, null, null, valid)

    constructor(errMsg: String) : this(null, null, null, null, false) {
        this.errMsg = errMsg
    }

    constructor(errCode: Int, schemaName: String) : this(errCode, null, null, schemaName)

    var errMsg: String? = null

    init {
        if (errCode != null) {
            errMsg = Rules.getByCode(errCode).message
        }
    }

}
