package org.dashevo.schema

import org.dashevo.schema.Object.DAPOBJECTS
import org.dashevo.schema.Object.STPACKET
import org.dashevo.schema.model.Result
import org.dashevo.schema.model.Rules
import org.dashevo.schema.util.JsonSchemaUtils
import org.everit.json.schema.Schema
import org.everit.json.schema.loader.SchemaLoader
import org.json.JSONArray
import org.json.JSONObject


object Validate {

    const val ERRORS = "errors"
    private const val OBJTYPE = "objtype"

    var schemaValidator: Schema //Schema.System

    init {
        val schemaLoader= SchemaLoader.builder()
                .schemaJson("path/to/schema")
                .draftV7Support()
                .build()
        schemaValidator = schemaLoader.load().build()
    }

    /**
     * Validates both System and Dap objects
     * @param obj Schema object instance
     * @param dapSchema Dap Schema definition (optional)
     * @returns {*}
     */
    private fun validateCore(obj: JSONObject?, dapSchema: JSONObject? = null) : Result {
        if (obj == null) {
            return Result(false)
        }

        val clonedObj = org.dashevo.schema.Schema.Object.fromObject(obj, dapSchema)
        return JsonSchemaUtils.validateSchemaObject(clonedObj!!, dapSchema)
    }

    /**
     * Validates a System Schema object instance
     * @param sysObj System Schema object instance
     * @param subSchemaName Subschema keyword
     * @returns {*}
     * @private
     */
    private fun validateSysObject(sysObj: JSONObject, subSchemaName: String?): Result {
        if (subSchemaName != null) {
            if (sysObj.javaClass.kotlin.members.any { it.name == subSchemaName }) {
                return Result(Rules.DAPOBJECT_MISSING_SUBSCHEMA.code, subSchemaName)
            }
        }
        return validateCore(sysObj)
    }

    /**
     * Validate a Subscription Transaction
     * @param obj Schema object instance
     * @returns {{valid, validateErrors}}
     */
    fun validateSubTx(obj: JSONObject): Result {
        return validateSysObject(obj, "subtx")
    }

    /**
     * Validate a Blockchain User
     * @param obj Schema object instance
     * @returns {{valid, validateErrors}}
     */
    fun validateBlockchainUser(obj: JSONObject): Result {
        return validateSysObject(obj, "blockchainuser")
    }

    /**
     * Validate a State Transition Header
     * @param obj Schema object instance
     * @returns {{valid, validateErrors}}
     */
    fun validateSTHeader(obj: JSONObject) : Result {
        return validateSysObject(obj, "stheader")
    }

    /**
     * Validate a State Transition Packet.  When the packets contain dapobjects,
     * the DapSchema parameter is required
     * @param obj Schema object instance
     * @param dapSchema DapSchema (optional)
     * @returns {{valid, validateErrors}}
     */
    fun validateSTPacket(obj: JSONObject, dapSchema: JSONObject? = null): Result {
        // deep extract a schema object from the object
        val outerObj = org.dashevo.schema.Schema.Object.fromObject(obj, dapSchema)

        // if this is a dapobjects packet...
        val objSTPacket = obj.getJSONObject(STPACKET)
        val objDapObjects = objSTPacket?.getJSONArray(DAPOBJECTS)

        if (objDapObjects != null && outerObj != null) {
            // require dapSchema
            if (dapSchema == null) {
                //TODO: * (Confirm rule)
                return Result(Rules.DAPOBJECT_MISSING_SUBSCHEMA.code)
            }

            // temporarily remove the inner dapobjects,
            // so we can validate the containing packet using the System Schema, and the
            // contained Dap objects using the dapSchema.
            outerObj.getJSONObject(DAPOBJECTS).put(DAPOBJECTS, JSONArray())

            // validate the empty packet as a sys object...
            val outerValid = validateSysObject(outerObj, STPACKET)

            if (!outerValid.valid) {
                return outerValid
            }

            //...then validate the contents as dabobjects
            return validateSTPacketObjects(objDapObjects, dapSchema)
        }

        // not a dapobjects packet so validate as a sysobject
        return validateSysObject(obj, Object.STPACKET)

    }

    /**
     * Validate the objects from a Transition packet
     * against a DapSchema and additional packet consensus rules
     * @param obj Schema object instance
     * @param dapSchema DapSchema
     * @returns {*}
     */
    fun validateSTPacketObjects(dapobjects: JSONArray, dapSchema: JSONObject): Result {
        for (i in 0..dapobjects.length()) {
            val dapObj = dapobjects.getJSONObject(i)
            val objValid = validateDapObject(dapObj, dapSchema)
            if (!objValid.valid) {
                return objValid
            }
        }
        return Result()
    }

    /**
     * Validate a DapContract instance
     * @param obj Schema object instance
     * @returns {{valid, validateErrors}}
     */
    fun validateDapContract(obj: JSONObject): Result {
        return validateSysObject(obj, "dapcontract")
    }

    /**
     * Validate a DapObject instance
     * @param dapObj Schema object instance
     * @param dapSchema DapSchema
     * @returns {*}
     */
    private fun validateDapObject(dapObj: JSONObject, dapSchema: JSONObject): Result {
        if (dapObj[OBJTYPE] !is String) {
            return Result(Rules.DAPOBJECT_MISSING_OBJTYPE.code, "objtype",
                    null, dapSchema.getString("title"))
        }

        val subSchema = Definition.getDapSubSchema(dapObj, dapSchema)

        if (subSchema != null) {
            return Result(Rules.DAPOBJECT_UNKNOWN_OBJTYPE.code, "objtype",
                    null, dapSchema.getString("title"))
        }

        return validateCore(dapObj, dapSchema)
    }

    /**
     * Validate a username using DIP 011 rules
     * @param uname Blockchain Username
     */
    fun validateBlockhainUsername(uname: String?): Boolean {
        if (uname == null) {
            return false
        }

        if (uname.length !in 3..24) {
            return false
        }

        val invalid = Regex("[^a-z0-9._]").containsMatchIn(uname)

        return !invalid
    }

    fun createValidator(dapSchema: JSONObject): Schema {
        return SchemaLoader.builder().schemaJson(dapSchema).build().load().build()
    }

}