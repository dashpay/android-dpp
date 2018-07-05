package org.dashevo.schema.model

enum class Rules(val code: Int, val message: String) {
    UNKNOWN(0, "Unknown error"),
    DAPOBJECT_MISSING_OBJTYPE(200, "Missing objtype property"),
    DAPOBJECT_UNKNOWN_OBJTYPE(201, "Missing objtype keyword in dap object instance"),
    DAPOBJECT_MISSING_SUBSCHEMA(300, "Missing subcschema"),
    DAPOBJECT_MISSING_PROPERTY(400, "Missing property"),
    DAPOBJECT_INVALID_TYPE(401, "Invalid type"),
    INVALID_METASCHEMA(500, "DAP Schema must have a valid \$schema"),
    INVALID_SCHEMA_TITLE(501, "Schema must have a valid title"),
    INVALID_DAP_SUBSCHEMA_COUNT(510, "Invalid DAP Subschema count"),
    INVALID_DAP_SUBSCHEMA_NAME(511, "Invalid DAP Subschema name"),
    RESERVED_DAP_SUBSCHEMA_NAME(512, "Reserved DAP Subschema name"),
    DAP_SUBSCHEMA_INHERITANCE(530, "DAP_SUBSCHEMA_INHERITANCE"),
    MISSING_TITLE(600, "Missing DAP Schema title");

    companion object {
        fun getByCode(code: Int) : Rules {
            values().iterator().forEach { rule ->
                if (rule.code == code) {
                    return rule
                }
            }
            return UNKNOWN
        }
    }

}