package org.dashevo.schema

import org.dashevo.schema.Object.CREATE_OBJECT_ACTION
import org.dashevo.schema.Object.OBJTYPE
import org.jsonorg.JSONObject

object Create {

    private const val DAPCONTRACT = "dapcontract"
    private const val STHEADER = "stheader"
    private const val STPACKET = "stpacket"
    private const val BLOCKCHAINUSER = "blockchainuser"
    private const val PVER = "pver"

    private fun createBaseInstance(keyword: String): JSONObject {
        val subSchema = JSONObject()
        subSchema.put(keyword, JSONObject(hashMapOf(
                PVER to Schema.system.get(PVER)
        )))
        return subSchema
    }

    /**
     * Create a State Transition Header instance
     * @param stp ST Packet schema instance
     * @param buid Blockchain User id
     * @param prevstid Previous State Transition id
     */
    fun createSTHeaderInstance(stp: JSONObject, buid: String, prevstid: String? = ""): JSONObject {
        val stHeaderObject = createBaseInstance(STHEADER)
        val stHeader = stHeaderObject.getJSONObject(STHEADER)

        stHeader.put("feeperbyte", 0) // blockchainuser fee set for this ts
        stHeader.put("buid", buid) // blockchainuser id, taken from the tx hash of the blockchainuser's first subtx
        stHeader.put("prevstid", prevstid ?: "") // hash of the previous transition for this blockchainuser (chained)
        // hash of the associated data packet for this transition
        stHeader.put("packetid", Object.getMeta(stp.getJSONObject(STPACKET), "id"))
        stHeader.put("stsig", "") // sig of the blockchainuser & the dapi quorum that validated the transition data
        stHeader.put("nver", 1)
        stHeader.put("packetsize", Serialize.encode(stp).size)

        Object.setID(stHeaderObject)

        return stHeaderObject
    }

    /**
     * Create an empty State Transition Packet instance
     */
    fun createSTPacketInstance(): JSONObject {
        return createBaseInstance(STPACKET)
    }

    /**
     * Create a new DapContract instance from a DapSchema instance
     * @param dapSchema DapSchema
     */
    fun createDapContract(dapSchema: JSONObject): JSONObject {
        val dapContract = createBaseInstance(DAPCONTRACT)

        val obj = dapContract.getJSONObject(DAPCONTRACT)
        obj.put("idx", 0)
        //obj.put("upgradedapid", "") // specify when revising an existing dap (orig dapcontract id)
        obj.put("dapname", dapSchema.getString("title"))
        obj.put("dapschema", dapSchema)
        obj.put("dapver", 1)

        Object.setID(dapContract)

        return dapContract
    }

    /**
     * Create a new DapObject instance
     * @param typeName Schema Keyword
     */
    fun createDapObject(typeName: String): JSONObject {
        val obj = JSONObject()

        obj.put(OBJTYPE, typeName)
        obj.put("idx", 0)
        obj.put("rev", 0)
        obj.put("act", CREATE_OBJECT_ACTION)

        return obj
    }

    /**
     * Create a new Blockchain User instance from a subtx
     * @param subtx Subscription Transaction
     * @returns {{blockchainuser: {pver: number, uname: string, buid: *|string, pubkey: string, credits: number}}}
     */
    fun createBlockchainUser(subTx: JSONObject): JSONObject {
        val blockchainUser = JSONObject()
        val obj = subTx.getJSONObject("subtx")

        blockchainUser.put("pver", obj["pver"])
        blockchainUser.put("uname", obj["uname"])
        blockchainUser.put("buid", Hash.subtx(subTx))
        blockchainUser.put("pubkey", obj["pubkey"])
        blockchainUser.put("credits", 100000)

        return JSONObject(hashMapOf(BLOCKCHAINUSER to blockchainUser))
    }

}