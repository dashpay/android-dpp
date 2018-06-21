package org.dashevo.schema

import org.dashevo.schema.model.*
import org.json.JSONObject

object Create {

    private const val DAPCONTRACT = "dapcontract"
    private const val TSHEADER = "tsheader"
    private const val TSPACKET = "tspacket"
    private const val BLOCKCHAINUSER = "blockchainuser"

    /**
     * Create a State Transition Header instance
     * @param pakid ST Packet id
     * @param uid Blockchain User id
     * @param ptsid Previous State Transition id
     */
    fun createTsHeaderInstance(pakid: String, uid: String, ptsid: String?): HashMap<String, Any> {
        val obj = TsHeader(uid, ptsid ?: "", pakid)
        val tsHeader: HashMap<String, Any> = hashMapOf(TSHEADER to obj)
        Object.setID(tsHeader)
        return tsHeader
    }

    /**
     * Create an empty State Transition Packet instance
     */
    fun createTsPacketInstance(): HashMap<String, Any> {
        val obj = DapBaseInstance()
        return hashMapOf(TSPACKET to obj)
    }

    /**
     * Create a new DapContract instance from a DapSchema instance
     * @param dapSchema DapSchema
     */
    fun createDapContract(dapSchema: JSONObject): HashMap<String, Any> {
        val obj = DapContract(dapSchema.getString("title"), dapSchema)
        val dap: HashMap<String, Any> = hashMapOf(DAPCONTRACT to obj)
        Object.setID(dap)
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