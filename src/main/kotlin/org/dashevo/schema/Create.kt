package org.dashevo.schema

import org.dashevo.schema.Object.OBJTYPE
import org.json.JSONObject

object Create {

    private const val DAPCONTRACT = "dapcontract"
    private const val STHEADER = "stheader"
    private const val STPACKET = "stpacket"
    private const val BLOCKCHAINUSER = "blockchainuser"
    private const val PVER = "pver"

    fun createBaseInstance(keyword: String): JSONObject {
        val subSchema = JSONObject()
        subSchema.put(keyword, JSONObject(hashMapOf(
                PVER to Schema.system.get(PVER)
        )))
        return subSchema
    }

    /**
     * Create a State Transition Header instance
     * @param pakid ST Packet id
     * @param uid Blockchain User id
     * @param ptsid Previous State Transition id
     */
    fun createSTHeaderInstance(pakid: String, uid: String, ptsid: String?): JSONObject {
        val stHeader = createBaseInstance(STHEADER)

        stHeader.put("fee", 0)
        stHeader.put("uid", uid)
        stHeader.put("ptsid", ptsid ?: "")
        stHeader.put("pakid", pakid)
        stHeader.put("usig", "")
        stHeader.put("qsig", "")

        Object.setID(stHeader)

        return stHeader
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
        obj.put("dapid", "") // specify when revising an existing dap (orig dapcontract id)
        obj.put("dapname", dapSchema.getString("title"))
        obj.put("dapschema", dapSchema)
        obj.put("dapver", "")

        Object.setID(dapContract)

        return dapContract;
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
        obj.put("act", 1)

        return obj
    }

    /**
     * Create a new Blockchain User instance from a subtx
     * @param subtx Subscription Transaction
     * @returns {{blockchainuser: {pver: number, uname: string, uid: *|string, pubkey: string, credits: number}}}
     */
    fun createBlockchainUser(subTx: JSONObject): JSONObject {
        val blockchainUser = JSONObject()
        val obj = subTx.getJSONObject("subtx")

        blockchainUser.put("pver", obj["pver"])
        blockchainUser.put("uname", obj["uname"])
        blockchainUser.put("uid", Hash.subtx(subTx))
        blockchainUser.put("pubkey", obj["pubkey"])
        blockchainUser.put("credits", 100000)

        return JSONObject(hashMapOf(BLOCKCHAINUSER to blockchainUser))
    }

}