package org.dashevo.schema

import org.apache.commons.collections.CollectionUtils
import org.dashevo.schema.model.Error
import org.dashevo.schema.model.TsPacket
import org.dashevo.schema.model.ValidationResult
import org.dashevo.schema.util.ErrorUtils
import org.dashevo.schema.util.ValidationResultUtils
import org.everit.json.schema.Schema
import org.everit.json.schema.ValidationException
import org.everit.json.schema.loader.SchemaLoader
import org.json.JSONObject
import java.util.*


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
    private fun validateCore(obj: Any?, dapSchema: JSONObject? = null) : ValidationResult {
        if (obj == null) {
            return ErrorUtils.result("object is null")
        }

        val clonedObj = org.dashevo.schema.Schema.Object.fromObject(obj, dapSchema)

        if (clonedObj.containsKey(Validate.ERRORS)) {
            //TODO (?) as List<Error> probably is not going to work
            return ValidationResult(false, clonedObj[Validate.ERRORS] as List<Error>)
        }

        val validators = arrayListOf(Validate.schemaValidator)
        if (dapSchema != null) {
            validators.add(Validate.createValidator(dapSchema))
        }

        val validateErrors = arrayListOf<Error>()
        validators.forEach { validator ->
            try {
                validator.validate(clonedObj)
            } catch (e: ValidationException) {
                //TODO: Covert ValidationException to Error or List of Error objects
                e.allMessages.forEach { errorMessage ->
                    validateErrors.add(ErrorUtils.newError(errorMessage))
                }
            }
        }

        return ValidationResultUtils.result(validateErrors)
    }

    /**
     * Validates a System Schema object instance
     * @param sysObj System Schema object instance
     * @param subSchemaName Subschema keyword
     * @returns {*}
     * @private
     */
    private fun validateSysObject(sysObj: Any, subSchemaName: String?): ValidationResult {
        if (subSchemaName != null) {
            if (sysObj.javaClass.kotlin.members.any { it.name == subSchemaName }) {
                return ValidationResultUtils.result(ErrorUtils.newError("Invalid obj type"))
            }
        }
        return validateCore(sysObj)
    }

    /**
     * Validate a Subscription Transaction
     * @param obj Schema object instance
     * @returns {{valid, validateErrors}}
     */
    fun validateSubTx(obj: Any): ValidationResult {
        return validateSysObject(obj, "subtx")
    }

    /**
     * Validate a Blockchain User
     * @param obj Schema object instance
     * @returns {{valid, validateErrors}}
     */
    fun validateBlockchainUser(obj: Any): ValidationResult {
        return validateSysObject(obj, "blockchainuser")
    }

    /**
     * Validate a State Transition Header
     * @param obj Schema object instance
     * @returns {{valid, validateErrors}}
     */
    fun validateTsHeader(obj: Any) : ValidationResult {
        return validateSysObject(obj, "tsheader")
    }

    /**
     * Validate a State Transition Packet.  When the packets contain dapobjects,
     * the DapSchema parameter is required
     * @param obj Schema object instance
     * @param dapSchema DapSchema (optional)
     * @returns {{valid, validateErrors}}
     */
    fun validateTsPacket(obj: HashMap<String, TsPacket>, dapSchema: JSONObject? = null): ValidationResult {
        //TODO (?) is it needed in Kotlin?
        // deep extract a schema object from the object
        val outerObj = org.dashevo.schema.Schema.Object.fromObject(obj, dapSchema)

        // if this is a dapobjects packet...
        if (obj[Object.TSPACKET] != null) {
            // require dapSchema
            if (dapSchema == null) {
                return ValidationResultUtils.result(ErrorUtils.newError("missing dapschema"))
            }

            // temporarily remove the inner dapobjects,
            // so we can validate the containing packet using the System Schema, and the
            // contained Dap objects using the dapSchema.
            ((outerObj[Object.TSPACKET]) as TsPacket).dapobjects = listOf(JSONObject())

            // validate the empty packet as a sys object...
            val outerValid = validateSysObject(outerObj, Object.TSPACKET)

            if (CollectionUtils.isNotEmpty(outerValid.validateErrors)) {
                return ValidationResultUtils.result(outerValid.validateErrors!!)
            }

            //...then validate the contents as dabobjects
            return validateTsPacketObjects(obj[Object.TSPACKET]!!.dapobjects, dapSchema)
        }

        // not a dapobjects packet so validate as a sysobject
        return validateSysObject(obj, Object.TSPACKET)

    }

    /**
     * Validate the objects from a Transition packet
     * against a DapSchema and additional packet consensus rules
     * @param obj Schema object instance
     * @param dapSchema DapSchema
     * @returns {*}
     */
    fun validateTsPacketObjects(dapobjects: List<JSONObject>, dapSchema: JSONObject): ValidationResult {
        dapobjects.forEach { dapObj ->
            val objValid = validateDapObject(dapObj, dapSchema)
            if (!objValid.valid) {
                return ValidationResultUtils.result(objValid.validateErrors!!)
            }
        }
        return ValidationResult(true)
    }

    /**
     * Validate a DapContract instance
     * @param obj Schema object instance
     * @returns {{valid, validateErrors}}
     */
    fun validateDapContract(obj: Any): ValidationResult {
        return validateSysObject(obj, "dapcontract")
    }

    /**
     * Validate a DapSchema instance
     * @param dapSchema {object} DapSchema
     * @returns {*}
     */
    fun validateDapSchema(dapSchema: JSONObject): ValidationResult {
        // JSON Meta-Schema validation
        try {
            schemaValidator.validate(dapSchema)
        } catch (e: ValidationException) {
            return ValidationResultUtils.result(ErrorUtils.newError(e.errorMessage, "metaschema"))
        }
        // TODO: Dash-specific Schema validation
        /*
        - confirm schema is JSON draft compliant

        - confirm no reservered property names

        - ensure property names lower case?

        - require root schema "title" value?

        - confirm ALL schema properties inherit from dapcontract objects
          (otherwise dapcontract data isn't limited to system spec)

        - require additionalProperties = false

        - require oneOf constraint in root

        - require valid $id

        - prevent code injection

        - validate property names using propertyNames schema (e.g. regex to limit charset used)

        - $schema value *must* be equal $schema value in the system schema (so system schema controls the JSON schema version
         */

        return ValidationResult(true)
    }

    /**
     * Validate a DapObject instance
     * @param dapObj Schema object instance
     * @param dapSchema DapSchema
     * @returns {*}
     */
    private fun validateDapObject(dapObj: JSONObject, dapSchema: JSONObject): ValidationResult {
        if (dapObj[OBJTYPE] !is String) {
            return ErrorUtils.result("missing dapobject type keyword")
        }

        dapSchema[dapObj.getString(OBJTYPE)] ?: return ErrorUtils.result("invalid object type")
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

        val invalid = Regex("[^A-Za-z0-9._]").containsMatchIn(uname)

        return !invalid
    }

    fun createValidator(dapSchema: JSONObject): Schema {
        return SchemaLoader.builder().schemaJson(dapSchema).build().load().build()
    }

}