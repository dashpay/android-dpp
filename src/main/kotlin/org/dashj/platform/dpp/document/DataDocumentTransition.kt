/**
 * Copyright (c) 2018-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashj.platform.dpp.document

abstract class DataDocumentTransition(rawStateTransition: MutableMap<String, Any?>) :
    DocumentTransition(rawStateTransition) {

    val data: Map<String, Any?>

    init {
        val data = HashMap(rawStateTransition)
        this.data = data.filter { !it.key.startsWith("\$") }
    }

    override fun toObject(skipIdentifierConversion: Boolean): MutableMap<String, Any?> {
        val map = super.toObject(skipIdentifierConversion)

        val deepCopy = Document.deepCopy(data)
        map.putAll(deepCopy)

        return map
    }

    override fun toJSON(): Map<String, Any?> {
        val json = super.toJSON().toMutableMap()
        Document.convertDataToString(json)
        return json
    }
}
