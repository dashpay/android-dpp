package org.dashevo.schema

import org.dashevo.schema.model.Error
import org.dashevo.schema.model.SubTx
import org.dashevo.schema.util.ErrorUtils
import org.everit.json.schema.ValidationException
import org.json.JSONObject

object Object {

    const val STPACKET = "stpacket"

    fun setID(obj: JSONObject, dapSchema: JSONObject) {
        setMeta(obj, "id", toHash(obj, dapSchema))
    }

    fun <T> fromObject(obj: T, dapSchema: JSONObject?): HashMap<String, Any?> {
        TODO("not implemented")
        return hashMapOf()
    }

    /**
     * Set metadata property value in a Schema object instance
     * @param obj Schema object instance
     * @param key Meta section keyword
     * @param value
     */
    fun setMeta(obj: JSONObject?, key: String, value: Any): JSONObject? {
        val typeProp: JSONObject = if (isSysObject(obj)) {
            obj!![obj.keys().next()] as JSONObject
        } else {
            obj!!
        }

        if (!typeProp.has("meta")) {
            typeProp.put("meta", JSONObject())
        }

        typeProp.getJSONObject("meta").put(key, value)

        return obj
    }

    /**
     * Classify an object as a System Object without validation
     * @param obj
     * @returns {boolean}
     */
    fun isSysObject(obj: JSONObject?): Boolean {
        if (obj == null) {
            return false
        }

        // first property should be the subschema
        val subSchemaName = obj.keys().next()

        val keys = Schema.system.getJSONObject("properties").keys()

        keys.forEach { key ->
            if (subSchemaName == key) {
                return true
            }
        }

        return false
    }

    /**
     * Clone a Schema instance by extracting only Schema defined properties
     * Optionally specify a dapSchema to clone a DAP Object
     * @param obj Schema object instance
     * @param dapSchema DapSchema (optional)
     * @returns {*}
     */
    fun fromObject(obj: JSONObject, dapSchema: JSONObject?): JSONObject? {
        //TODO: implement Schema.util.object.toClone
        // deep clone to dereference (we only clone props not methods)
        // val objCopy = Schema.util.`object`.toClone(obj)
        val clonedObj = JSONObject(obj.toString())

        val validators = arrayListOf(Validate.schemaValidator)
        if (dapSchema != null) {
            validators.add(Validate.createValidator(dapSchema))
        }

        val validateErrors = arrayListOf<Error>()
        validators.forEach { validator ->
            try {
                validator.validate(clonedObj)
            } catch (e: ValidationException) {
                e.allMessages.forEach { errorMessage ->
                    validateErrors.add(ErrorUtils.newError(errorMessage))
                }
            }
        }

        //TODO Improve, does it work to add Error instances just like this?
        if (validateErrors.isNotEmpty()) {
            clonedObj.put("errors", validateErrors)
        }

        //TODO remove non-Schema properties
        return clonedObj
    }

    /**
     * Return a hash of a Schema object instance
     * @param obj {object} Schema object instance
     * @param dapSchema {object} DapSchema
     * @returns {*}
     */
    fun toHash(obj: JSONObject, dapSchema: JSONObject): String {
        return when (obj.keys().next()) {
            "subtx" -> Schema.Hash.subtx(obj as SubTx) //TODO: Parse JSON to subTx?
            "blockchainuser" -> Schema.Hash.blockchainuser(obj)
            "stheader" -> Schema.Hash.stheader(obj)
            "stpacket" -> Schema.Hash.stpacket(obj, dapSchema)
            "dapcontract" -> Schema.Hash.dapcontract(obj)
            "dapschema" -> Schema.Hash.dapschema(obj);
            else -> Schema.Hash.dapobject(obj, dapSchema)
        }
    }

}