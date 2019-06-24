package org.dashevo.dpp

import org.json.JSONObject
import java.io.File

open class BaseTest {
    val data: JSONObject = JSONObject(File("src/test/resources/data/contactsdap-test-data.json").readText())
    val contactsDap: JSONObject = JSONObject(File("src/test/resources/data/contactsdap.json").readText())
}