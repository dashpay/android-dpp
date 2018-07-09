package org.dashevo.schema

import org.json.JSONObject
import java.io.File

open class BaseTest {
    val data: JSONObject
    val validSubTx: JSONObject
    val contactsDap: JSONObject

    init {
        Schema.system  = JSONObject(File("src/test/resources/data/dash-system-schema.json").readText())
        data = JSONObject(File("src/test/resources/data/contactsdap-test-data.json").readText())
        validSubTx = JSONObject(File("src/test/resources/data/dash-system-schema.json").readText())
        contactsDap = JSONObject(File("src/test/resources/data/contactsdap.json").readText())
    }

}