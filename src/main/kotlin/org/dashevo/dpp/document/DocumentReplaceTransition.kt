package org.dashevo.dpp.document

class DocumentReplaceTransition : DocumentCreateTransition {

    override val action = DocumentTransition.Action.REPLACE
    var revision: Int

    constructor(rawStateTransition: MutableMap<String, Any?>): super(rawStateTransition) {
        this.revision = rawStateTransition.remove("\$revision") as Int
    }

    override fun toJSON(): Map<String, Any> {
        var json = super.toJSON() as MutableMap<String, Any>
        json["\$revision"] = revision

        return json
    }
}