package com.jetbrains.rider.rdtext.impl.ot

import com.jetbrains.rider.rdtext.impl.intrinsics.RdChangeOrigin
import com.jetbrains.rider.rdtext.intrinsics.RdTextChange
import com.jetbrains.rider.rdtext.intrinsics.RdTextChangeKind

enum class OtOperationKind {
    Normal,
    Reset
}

// todo make it intrinsic
class OtOperation(changes: List<OtChange>, val origin: RdChangeOrigin, val timestamp: Int, val kind: OtOperationKind, dontNormalize: Boolean = false) {
    val changes = if (dontNormalize) changes else changes.filter { !it.isId() }

    fun documentLengthBefore() = changes.sumBy(OtChange::getTextLengthBefore)
    fun documentLengthAfter() = changes.sumBy(OtChange::getTextLengthAfter)

    override fun toString(): String = changes.joinToString(";", "Op(changes=[", "], origin=$origin, timestamp=$timestamp)")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OtOperation

        if (origin != other.origin) return false
        if (timestamp != other.timestamp) return false
        if (kind != other.kind) return false
        if (changes != other.changes) return false

        return true
    }

    override fun hashCode(): Int {
        var result = origin.hashCode()
        result = 31 * result + timestamp
        result = 31 * result + kind.hashCode()
        result = 31 * result + changes.hashCode()
        return result
    }
}

fun OtOperation.toRdTextChanges(): List<RdTextChange> {
    if (kind == OtOperationKind.Reset) {
        require(changes.size == 1 && changes[0] is InsertText)
        val new = (changes[0] as InsertText).text
        return listOf(RdTextChange(RdTextChangeKind.Reset, 0, "", new, new.length))
    }

    var documentLength = this.documentLengthBefore()
    var currOffset = 0
    var lastInsert: InsertText? = null
    var lastDelete: DeleteText? = null

    val res = mutableListOf<RdTextChange>()
    for (change in this.changes) {
        when (change) {
            is Retain -> {
                if (lastDelete != null || lastInsert != null) {
                    res.add(createTextChange(currOffset, lastInsert, lastDelete, documentLength))
                    lastDelete = null
                    lastInsert = null
                }
            }
            is InsertText -> {
                if (lastInsert != null) {
                    res.add(createTextChange(currOffset, lastInsert, lastDelete, documentLength))
                    lastDelete = null
                }
                documentLength += change.getTextLengthAfter()
                lastInsert = change
            }
            is DeleteText -> {
                if (lastDelete != null) {
                    res.add(createTextChange(currOffset, lastInsert, lastDelete, documentLength))
                    lastInsert = null
                }
                documentLength -= change.getTextLengthBefore()
                lastDelete = change
            }
        }

        currOffset += change.getTextLengthAfter()
    }

    if (lastDelete != null || lastInsert != null)
        res.add(createTextChange(currOffset, lastInsert, lastDelete, documentLength))
    return res
}

private fun createTextChange(offset: Int, insert: InsertText?, delete: DeleteText?, documentLength: Int): RdTextChange {
    val newText = insert?.text ?: ""
    val oldText = delete?.text ?: ""
    val startOffset = offset - (insert?.getTextLengthAfter() ?: 0)
    val kind = when {
        insert != null && delete != null -> RdTextChangeKind.Replace
        insert != null -> RdTextChangeKind.Insert
        delete != null -> RdTextChangeKind.Remove
        else -> throw IllegalArgumentException()
    }
    return RdTextChange(kind, startOffset, oldText, newText, documentLength)
}

fun RdTextChange.toOperation(origin: RdChangeOrigin, ts: Int, priority: OtChangePriority = OtChangePriority.Normal): OtOperation {
    val changes = kotlin.collections.mutableListOf<OtChange>().apply {
        add(Retain(startOffset))

        when (this@toOperation.kind) {
            RdTextChangeKind.Insert -> add(InsertText(new, priority))
            RdTextChangeKind.Remove -> add(DeleteText(old, priority))
            RdTextChangeKind.Replace -> { add(InsertText(new, priority)); add(DeleteText(old, priority)) }
            RdTextChangeKind.Reset -> add(InsertText(new, priority))
        }

        val currentOffset = this.sumBy(OtChange::getTextLengthAfter)
        add(Retain(this@toOperation.fullTextLength - currentOffset))
    }

    val kind = if (kind == RdTextChangeKind.Reset) OtOperationKind.Reset else OtOperationKind.Normal
    val operation = OtOperation(changes, origin, ts, kind)

    kotlin.assert(operation.documentLengthAfter() == fullTextLength)
    return operation
}