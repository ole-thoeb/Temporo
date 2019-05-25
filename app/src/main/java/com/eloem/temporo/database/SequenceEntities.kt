package com.eloem.temporo.database

import androidx.room.*

@Entity(primaryKeys = ["id", "sequenceId"])
@Fts4
@ForeignKey(entity = SequenceSql::class, parentColumns = ["id"], childColumns = ["sequenceId"])
class SerialisedComponent(
    val id: Long,
    val sequenceId: Long,
    val type: Int,
    val position: Int,
    val title: String? = null,
    val showNext: Boolean? = null,
    val mode: Int? = null,
    val times: Int? = null,
    val branchId: Long? = null,
    val length: Long? = null,
    val startSound: Int? = null,
    val endSound: Int? = null
) {

    enum class Type {
        LOOP, WAIT, MARKER, COUNTDOWN
    }

    companion object {
        const val TYPE_LOOP = 0
        const val TYPE_WAIT = 1
        const val TYPE_MARKER = 2
        const val TYPE_COUNTDOWN = 3
    }
}

@Entity
@Fts4
class SequenceSql(
    @PrimaryKey val id: Long,
    val title: String
)

class SequenceWithComponents {
    @Embedded lateinit var sequence: SequenceSql
    @Relation(parentColumn = "id", entityColumn = "sequenceId")
    lateinit var components: List<SerialisedComponent>
}