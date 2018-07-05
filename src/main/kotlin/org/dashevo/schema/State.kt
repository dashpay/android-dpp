package org.dashevo.schema

import org.dashevo.schema.Object.ACT
import org.dashevo.schema.Object.DAPOBJECTS
import org.dashevo.schema.Object.INDEX
import org.dashevo.schema.Object.IS_PROFILE
import org.dashevo.schema.Object.OBJECTS
import org.dashevo.schema.Object.OBJTYPE
import org.dashevo.schema.Object.STHEADER
import org.dashevo.schema.Object.STPACKET
import org.dashevo.schema.Object.TYPE
import org.dashevo.schema.Object.UID
import org.dashevo.schema.Object.USER_ID
import org.dashevo.schema.model.Result
import org.json.JSONArray
import org.json.JSONObject

object State {

    fun validateTransitionData(st: JSONObject, stp: JSONObject,
                               dapSchema: JSONObject, dapSpace: JSONObject? = null): Result {

        val stPacket = stp.getJSONObject(STPACKET)
        if (stPacket != null && stPacket.has(DAPOBJECTS)) {
            val stPacketDapObjects = stPacket.getJSONArray(DAPOBJECTS)
            val tspValid = Validate.validateSTPacketObjects(stPacketDapObjects, dapSchema)
            if (!tspValid.valid) {
                return tspValid
            }

            if (dapSpace != null) {
                val stateValid = validateDapSpace(stPacketDapObjects, dapSchema)
                if (!stateValid.valid) {
                    return stateValid
                }
            }

            val indexesValid = validateIndexes(stp, dapSpace)
            if (!indexesValid.valid) {
                return indexesValid
            }

            val relationsValid = validateRelations(st, stp, dapSpace, dapSchema)
            if (!relationsValid.valid) {
                return relationsValid
            }
        } else {
            // TODO: verify dapcontract
        }

        return Result()
    }

    /**
     * Validate a DapSpace data
     * @param obj {object[]} Array of Schema object instance
     * @param dapSchema {object} DapSchema instance
     * @returns {*}
     */
    private fun validateDapSpace(objs: JSONArray, dapSchema: JSONObject): Result {
        val objsValid = Validate.validateSTPacketObjects(objs, dapSchema)
        if (!objsValid.valid) {
            return objsValid
        }

        for (i in 0..objs.length()) {
            for (j in 0..objs.length()) {
                if (i != j) {
                    if (objs[i] == objs[j]) {
                        val result = Result(false)
                        result.errMsg = "duplicate object index in DapSpace"
                        return result
                    }
                }
            }
        }

        return Result()
    }

    /**
     * Validate indexes in a DapSpace instance
     * @param ts {object} State Transition instance
     * @param tsp {object} State Transition Packet instance
     * @param dapSpace {object} DapSpace instance
     * @returns {ValidationResult}
     */
    private fun validateIndexes(stp: JSONObject, dapSpace: JSONObject?): Result {
        // TODO: recursively check updated state as packet objects are applied
        var maxIdx = dapSpace?.getInt("maxidx") ?: 0

        //TODO: check https://github.com/dashevo/dash-schema/issues/31 before moving everything inside the if
        //TODO: or early returning the result

        val stPacket = stp.getJSONObject(STPACKET)
        val dapObjects = stPacket.getJSONArray(DAPOBJECTS)
        val size = dapObjects.length()

        if (dapSpace != null) {
            for (i in 0..size) {
                val pakObj = dapObjects.getJSONObject(i)

                if (pakObj.getInt("act") == 1) {
                    if (pakObj.getInt("idx") != (maxIdx + 1)) {
                        val result = Result(false)
                        result.errMsg = "incorrect index on new object"
                        return result
                    }
                    maxIdx++
                }
            }
        }

        return Result()
    }

    /**
     * Validate relations in a DapSpace
     * @param ts {object} State Transition instance
     * @param tsp {object} State Transition Packet instance
     * @param dapSpace {object} DapSpace instance
     * @param dapSchema {object} DapSchema instance
     * @returns {ValidationResult}
     */
    private fun validateRelations(st: JSONObject, stp: JSONObject, dapSpace: JSONObject?, dapSchema: JSONObject): Result {
        val stPacket = stp.getJSONObject(STPACKET)
        val stPacketDapObjects = stPacket.getJSONArray(DAPOBJECTS)
        val size = stPacketDapObjects.length()

        dapObjectsLoop@ for (i in 0..size) {
            val dapObject = stPacketDapObjects.getJSONObject(i)
            val relations = Definition.getSubSchemaRelations(dapSchema, dapObject.getString(OBJTYPE))

            val relationsSize = relations?.length() ?: 0

            for (j in 0..relationsSize) {
                val relationName = relations!!.getString(j)

                if (dapObject.has(relationName)
                        && dapObject.getJSONObject(relationName).getString(USER_ID) == st.getJSONObject(STHEADER)
                                .getString(UID)) {

                    return Result("object cannot relate to self")
                }
            }

            if (dapSpace == null || !dapSpace.has(OBJECTS)) {
                continue@dapObjectsLoop
            }

            // don't allow 2 objects of same type if not relations
            // (note this is limiting and should be expanded with custom rules defined in schema)
            if (relationsSize == 0) {
                if (hasDuplicateObjectInDapSpace(stPacketDapObjects.getJSONObject(i), dapSpace, dapSchema)) {
                    return Result("duplicate profile object")
                }

                continue@dapObjectsLoop
            }

            // Prevent 1-to-1 relations duplication
            if (dapObject.getInt(ACT) == 1) {
                if (hasRelationDuplication(stPacketDapObjects.getJSONObject(i), relations, dapSpace)) {
                    return Result("duplicate related object")
                }
            }
        }

        return Result()
    }

    /**
     * Find 1-to-1 relation duplicate object
     *
     * @param {object} dapObject
     * @param {array} relations
     * @param {object}dapSpace
     * @return {boolean}
     * @private
     */
    private fun hasRelationDuplication(dapObject: JSONObject, relations: JSONArray?, dapSpace: JSONObject): Boolean {
        val size = relations?.length() ?: 0
        relations@ for (i in 0..size) {
            val relationName = relations!!.getString(i)

            if (!dapObject.has(relationName) || dapObject.getJSONObject(relationName).getInt(TYPE) != 0) {
                continue@relations
            }

            val dapSpaceObjs = dapSpace.getJSONArray(OBJECTS)
            val dapOjectsSize = dapSpace.getJSONArray(OBJECTS).length()
            for (j in 0..dapOjectsSize) {
                val stateObj = dapSpaceObjs.getJSONObject(i)

                if (dapObject.getString(OBJTYPE) != stateObj.getString(OBJTYPE)) {
                    continue@relations
                }

                if (dapObject.getJSONObject(relationName).get(USER_ID) == stateObj.getJSONObject(relationName).get(USER_ID) &&
                        dapObject.getJSONObject(relationName).get(TYPE) == stateObj.getJSONObject(relationName).get(TYPE) &&
                        dapObject.getJSONObject(relationName).get(INDEX) == stateObj.getJSONObject(relationName).get(INDEX)) {
                    return true
                }
            }
        }

        return false
    }

    /**
     * Find duplicate object
     *
     * @param {object} object
     * @param {object} dapSpace
     * @param {object} dapSchema
     * @return {boolean}
     * @private
     */
    private fun hasDuplicateObjectInDapSpace(dapObject: JSONObject, dapSpace: JSONObject, dapSchema: JSONObject): Boolean {
        val act = dapObject.getInt(ACT)
        val objtype = dapObject.getString(OBJTYPE)

        if (act == 1 && isProfile(dapObject, dapSchema)) {
            val duplicate = dapSpace.getJSONArray(OBJECTS).find {
                objtype == (it as JSONObject).getString(OBJTYPE) && isProfile(it, dapSchema)
            }
            return duplicate != null
        }
        return false
    }

    /**
     * Return if a DapObject is a Profile object... profile objects are
     * always the first slot of a DapSpace, and a DapSpace can only contain one
     * profile object
     * @param obj {object} Schema object instance
     * @param dapSchema {object} DapSchema instance
     * @returns {boolean}
     */
    private fun isProfile(dapObject: JSONObject, dapSchema: JSONObject): Boolean {
        val key = dapObject.getString(OBJTYPE)

        if (dapSchema.has(key)) {
            if (dapSchema.getJSONObject(key).getBoolean(IS_PROFILE)) {
                return true
            }
        }
        return false
    }

}