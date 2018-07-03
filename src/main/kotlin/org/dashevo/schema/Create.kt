package org.dashevo.schema

import org.dashevo.schema.model.*
import org.json.JSONObject

object Create {

    private const val DAPCONTRACT = "dapcontract"
    private const val STHEADER = "stheader"
    private const val STPACKET = "stpacket"
    private const val BLOCKCHAINUSER = "blockchainuser"

    /**
     * Create a State Transition Header instance
     * @param pakid ST Packet id
     * @param uid Blockchain User id
     * @param ptsid Previous State Transition id
     */
    fun createSTHeaderInstance(pakid: String, uid: String, ptsid: String?): HashMap<String, Any> {
        val obj = STHeader(uid, ptsid ?: "", pakid)
        val stHeader: HashMap<String, Any> = hashMapOf(STHEADER to obj)
        //Object.setID(stHeader)
        return stHeader
    }

    /**
     * Create an empty State Transition Packet instance
     */
    fun createSTPacketInstance(): HashMap<String, Any> {
        val obj = DapBaseInstance()
        //TODO: createBaseInstance('stpacket')
        return hashMapOf(STPACKET to obj)
    }

    /**
     * Create a new DapContract instance from a DapSchema instance
     * @param dapSchema DapSchema
     */
    fun createDapContract(dapSchema: JSONObject): HashMap<String, Any> {
        val obj = DapContract(dapSchema.getString("title"), dapSchema)
        val dap: HashMap<String, Any> = hashMapOf(DAPCONTRACT to obj)
        //Object.setID(dap)
        return dap
    }

    /**
     * Create a new DapObject instance
     * @param typeName Schema Keyword
     */
    fun createDapObject(typeName: String): DapObject {
        return DapObject(typeName)
    }

    /**
     * Create a new Blockchain User instance from a subtx
     * @param subtx Subscription Transaction
     * @returns {{blockchainuser: {pver: number, uname: string, uid: *|string, pubkey: string, credits: number}}}
     */
    fun createBlockchainUser(subtx: SubTx): HashMap<String, BlockchainUser> {
        val bu = BlockchainUser(subtx.pver, subtx.uname, Schema.Hash.subtx(subtx), subtx.pubkey)
        return hashMapOf(BLOCKCHAINUSER to bu)
    }

}