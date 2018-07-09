package org.dashevo.schema

import org.dashevo.schema.util.HashUtils
import org.dashevo.schema.util.JsonSchemaUtils
import org.json.JSONObject
import java.io.File

fun main(args : Array<String>) {
    val fileName = "src/main/resources/dash-system-schema.json"

    val schemaContent = File(fileName).readText()
    val schema = JSONObject(schemaContent)

    Schema.system = schema
    val data = JSONObject(File("src/test/resources/data/contactsdap-test-data.json").readText())
    val aliceSubTx = data.getJSONObject("alice_subtx_1")
    val bobSubTx = data.getJSONObject("bob_subtx_1")
    HashUtils.toHash(JSONObject(hashMapOf("alice" to aliceSubTx, "bob" to bobSubTx)))
    println(JsonSchemaUtils.extractSchemaObject(aliceSubTx))
    println(Hash.subtx(aliceSubTx))
}