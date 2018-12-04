package fsight.youtrack.db.exposed.pg

import fsight.youtrack.toTimeAccountingExtendedModel
import fsight.youtrack.toWorkTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.format.DateTimeFormat
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional


@Repository
@Transactional
class TimeAccountingExtendedRepo(@Qualifier("pgDataSource") private val db: Database) {
    fun getAll(): List<TimeAccountingExtendedModel> {
        return transaction(db) {
            TimeAccountingExtendedView.selectAll().map { it.toTimeAccountingExtendedModel() }
        }
    }

    fun getGroupedByProjectType(projects: String, dateFrom: String, dateTo: String): List<AggregatedResult> {
        val df = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(dateFrom)
        val dt = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(dateTo)
        val filter = projects.removeSurrounding("[", "]").split(",")
        return transaction(db) {
            TimeAccountingExtendedView
                .slice(TimeAccountingExtendedView.projectType, TimeAccountingExtendedView.units.sum())
                .select(where = {
                    TimeAccountingExtendedView.createDate.between(
                        from = df,
                        to = dt
                    ) and TimeAccountingExtendedView.ytProject.inList(filter)
                })
                .groupBy(TimeAccountingExtendedView.projectType)
                .map { item ->
                    AggregatedResult(
                        item[TimeAccountingExtendedView.projectType],
                        item[TimeAccountingExtendedView.units.sum()] ?: 0
                    )
                }
        }
    }

    data class AggregatedResult(
        val key: String,
        val value: Int,
        /*val presentation: String = "$key - ${value.toWorkTime()}"*/
        val presentation: String = value.toWorkTime()
    )
}
